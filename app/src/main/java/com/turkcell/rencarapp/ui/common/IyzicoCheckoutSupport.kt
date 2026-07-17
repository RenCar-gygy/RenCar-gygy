package com.turkcell.rencarapp.ui.common

object IyzicoCheckoutSupport {
    const val INITIALIZE_SUCCESS = "success"
    const val PAYMENT_SUCCESS = "SUCCESS"
    const val PAYMENT_FAILURE = "FAILURE"
    const val RESULT_POLL_ATTEMPTS = 5
    const val RESULT_POLL_DELAY_MS = 1_000L
    const val CHECKOUT_FORM_BASE_URL = "https://rencarv2.halitkalayci.com/"

    /** Checkout Form'da sunulacak taksit seçenekleri — OpenAPI `enabledInstallments`. */
    val DEFAULT_ENABLED_INSTALLMENTS: List<Int> = listOf(1, 2, 3, 6, 9, 12)

    fun resolveExpiresAtEpochMs(tokenExpireTimeSeconds: Long?): Long? =
        tokenExpireTimeSeconds?.takeIf { it > 0 }?.let { System.currentTimeMillis() + it * 1_000 }

    fun isSessionExpired(expiresAtEpochMs: Long?): Boolean =
        expiresAtEpochMs != null && System.currentTimeMillis() >= expiresAtEpochMs

    fun wrapCheckoutFormHtml(content: String): String {
        if (content.contains("<html", ignoreCase = true)) return content
        return """
            <!DOCTYPE html>
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;">$content</body>
            </html>
        """.trimIndent()
    }

    fun isInitializeSuccess(status: String?): Boolean =
        status.equals(INITIALIZE_SUCCESS, ignoreCase = true)

    fun isCheckoutPaymentSuccess(paymentStatus: String?): Boolean =
        paymentStatus.equals(PAYMENT_SUCCESS, ignoreCase = true)

    fun isCheckoutPaymentFailure(paymentStatus: String?): Boolean =
        paymentStatus.equals(PAYMENT_FAILURE, ignoreCase = true)
}
