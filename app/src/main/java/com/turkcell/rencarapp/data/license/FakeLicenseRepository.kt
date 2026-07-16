package com.turkcell.rencarapp.data.license

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLicenseRepository @Inject constructor() : LicenseRepository {

    private var status: LicenseStatus = LicenseStatus.NOT_SUBMITTED
    private var rejectReason: String? = null

    override suspend fun getStatus(): Result<LicenseInfo> {
        delay(FAKE_DELAY_MS)
        return Result.success(LicenseInfo(status = status, rejectReason = rejectReason))
    }

    override suspend fun upload(
        frontImageBytes: ByteArray,
        backImageBytes: ByteArray,
        selfieImageBytes: ByteArray,
    ): Result<LicenseInfo> {
        delay(FAKE_DELAY_MS)
        if (frontImageBytes.isEmpty() || backImageBytes.isEmpty() || selfieImageBytes.isEmpty()) {
            return Result.failure(
                IllegalArgumentException("Ön, arka ehliyet ve selfie fotoğrafı zorunludur."),
            )
        }
        status = LicenseStatus.APPROVED
        rejectReason = null
        return Result.success(LicenseInfo(status = status))
    }

    private companion object {
        const val FAKE_DELAY_MS = 400L
    }
}
