package com.turkcell.rencarapp.data.license

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.network.api.LicenseApi
import com.turkcell.rencarapp.data.network.dto.LicenseStatusResponseDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultLicenseRepository @Inject constructor(
    private val licenseApi: LicenseApi,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : LicenseRepository {

    override suspend fun getStatus(): Result<LicenseInfo> =
        authorizedCall { authorization ->
            licenseApi.getStatus(authorization = authorization).toDomain()
        }

    override suspend fun upload(
        frontImageBytes: ByteArray,
        backImageBytes: ByteArray,
    ): Result<LicenseInfo> {
        val frontBytes = normalizeImageBytes(frontImageBytes)
        val backBytes = normalizeImageBytes(backImageBytes)
        if (frontBytes.isEmpty() || backBytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Ön ve arka ehliyet fotoğrafı zorunludur."))
        }
        return authorizedCall { authorization ->
            licenseApi.upload(
                authorization = authorization,
                front = imagePart(fieldName = "front", fileName = "front.png", bytes = frontBytes),
                back = imagePart(fieldName = "back", fileName = "back.png", bytes = backBytes),
                selfie = imagePart(fieldName = "selfie", fileName = "selfie.png", bytes = frontBytes),
            ).toDomain()
        }
    }

    private suspend fun <T> authorizedCall(block: suspend (authorization: String) -> T): Result<T> =
        authorizedRequestExecutor.execute(block)

    private fun LicenseStatusResponseDto.toDomain(): LicenseInfo =
        LicenseInfo(
            status = status.toLicenseStatus(),
            rejectReason = rejectReason,
        )

    private fun String.toLicenseStatus(): LicenseStatus =
        when (uppercase()) {
            LicenseStatus.NOT_SUBMITTED.name -> LicenseStatus.NOT_SUBMITTED
            LicenseStatus.UNDER_REVIEW.name -> LicenseStatus.UNDER_REVIEW
            LicenseStatus.APPROVED.name -> LicenseStatus.APPROVED
            LicenseStatus.REJECTED.name -> LicenseStatus.REJECTED
            else -> LicenseStatus.NOT_SUBMITTED
        }

    private fun normalizeImageBytes(bytes: ByteArray): ByteArray =
        if (bytes.size <= 1) MINIMAL_PNG else bytes

    private fun imagePart(fieldName: String, fileName: String, bytes: ByteArray): MultipartBody.Part {
        val requestBody = bytes.toRequestBody(IMAGE_MEDIA_TYPE)
        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }

    private companion object {
        val IMAGE_MEDIA_TYPE = "image/png".toMediaType()

        // 1x1 px PNG — stub yükleme için geçici; gerçek fotoğraf seçimi sonraki sprintte.
        val MINIMAL_PNG: ByteArray = intArrayOf(
            0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
            0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, 0x9C, 0x63, 0x60, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x01, 0xE5, 0x27, 0xDE, 0xFC, 0x00,
            0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
            0x42, 0x60, 0x82,
        ).map(Int::toByte).toByteArray()
    }
}
