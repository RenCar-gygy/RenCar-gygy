package com.turkcell.rencarapp.data.license

enum class LicenseStatus {
    NOT_SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
}

data class LicenseInfo(
    val status: LicenseStatus,
    val rejectReason: String? = null,
)

interface LicenseRepository {
    suspend fun getStatus(): Result<LicenseInfo>

    suspend fun upload(
        frontImageBytes: ByteArray,
        backImageBytes: ByteArray,
        selfieImageBytes: ByteArray,
    ): Result<LicenseInfo>
}
