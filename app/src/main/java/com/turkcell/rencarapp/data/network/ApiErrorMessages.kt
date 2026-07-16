package com.turkcell.rencarapp.data.network

/**
 * HTTP hata kodlarını ekran bağlamına göre kullanıcı dostu mesaja çevirir.
 */
class ApiException(
    val httpCode: Int,
    override val message: String,
) : Exception(message)

enum class ApiErrorContext {
    RESERVATION_CREATE,
    RESERVATION_CANCEL,
    RESERVATION_ACTIVE,
    RENTAL_CREATE,
    RENTAL_START,
    RENTAL_FINISH,
    RENTAL_CANCEL,
    RENTAL_ACTIVE,
    RENTAL_PHOTO,
    RENTAL_PAY,
    WALLET,
    VEHICLE_DETAIL,
}

fun Throwable.toUserMessage(context: ApiErrorContext): String {
    val apiException = generateSequence(this) { it.cause }
        .filterIsInstance<ApiException>()
        .firstOrNull()
    if (apiException != null) {
        return apiException.toUserMessage(context)
    }
    return message?.takeIf { it.isNotBlank() } ?: "Beklenmeyen bir hata oluştu."
}

fun ApiException.toUserMessage(context: ApiErrorContext): String =
    when (httpCode) {
        409 -> when (context) {
            ApiErrorContext.RESERVATION_CREATE ->
                "Rezervasyon oluşturulamadı. Araç müsait olmayabilir veya devam eden bir rezervasyon/kiralama var."
            ApiErrorContext.RENTAL_CREATE ->
                "Zaten aktif bir kiralama veya rezervasyonunuz var."
            ApiErrorContext.RENTAL_START ->
                "Kiralama başlatılamadı. Önce dört yön fotoğrafı gerekir; tekrar deneyin."
            ApiErrorContext.RENTAL_CANCEL ->
                "Kiralama iptal edilemedi. Yalnızca henüz başlamamış (hazırlık) yolculuk iptal edilebilir; aktif yolculuk için «Kiralamayı Bitir» kullanın."
            ApiErrorContext.RENTAL_PAY ->
                "Ödeme alınamadı. Bakiye yetersiz olabilir veya yolculuk zaten ödenmiş olabilir."
            ApiErrorContext.WALLET ->
                "Cüzdan işlemi tamamlanamadı. Tutar aralığını veya kart bilgilerini kontrol edin."
            else -> message
        }
        403 -> "Bu işlem için yetkiniz yok."
        404 -> when (context) {
            ApiErrorContext.RENTAL_ACTIVE, ApiErrorContext.RESERVATION_ACTIVE ->
                "Aktif kiralama veya rezervasyon bulunamadı."
            else -> "İstenen kayıt bulunamadı."
        }
        500 -> when (context) {
            ApiErrorContext.RENTAL_CREATE ->
                "Sunucu hatası (500). Aktif rezervasyonunuzun bu araç için geçerli olduğundan emin olun; gerekirse Swagger'dan kayıtları temizleyip tekrar rezerve edin."
            else -> message
        }
        else -> message
    }
