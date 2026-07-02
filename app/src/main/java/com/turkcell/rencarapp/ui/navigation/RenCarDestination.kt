package com.turkcell.rencarapp.ui.navigation

/**
 * Uygulamadaki navigasyon hedeflerinin tek doğruluk kaynağı.
 *
 * Kök, Auth, Main ve Rental (nested) grafikleri bu route string'leri üzerinden bağlanır.
 */
object RenCarDestination {

    // --- Kök grafik ---
    const val Splash = "splash"

    // --- Auth grafiği ---
    const val AuthGraph = "auth"
    const val Onboarding = "auth/onboarding"
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val ARG_PHONE_NUMBER = "phoneNumber"
    const val Otp = "auth/otp/{$ARG_PHONE_NUMBER}"
    const val License = "auth/license"

    // --- Main grafiği ---
    const val MainGraph = "main"
    const val Map = "main/map"
    const val RentalHistory = "main/history"
    const val Wallet = "main/wallet"
    const val Profile = "main/profile"

    // --- Rental alt grafiği (nested) ---
    const val RentalGraph = "rental"
    const val VehicleDetail = "rental/vehicle/{vehicleId}"
    const val RentalConfirmation = "rental/confirmation/{vehicleId}"
    const val RentalSummary = "rental/summary/{vehicleId}"
    const val DeliveryPhotos = "rental/delivery_photos/{vehicleId}"
    const val ActiveRental = "rental/active/{rentalId}"

    const val ARG_VEHICLE_ID = "vehicleId"
    const val ARG_RENTAL_ID = "rentalId"

    fun vehicleDetailRoute(vehicleId: String): String = "rental/vehicle/$vehicleId"

    fun rentalConfirmationRoute(vehicleId: String): String = "rental/confirmation/$vehicleId"

    fun rentalSummaryRoute(vehicleId: String): String = "rental/summary/$vehicleId"

    fun deliveryPhotosRoute(vehicleId: String): String = "rental/delivery_photos/$vehicleId"

    fun activeRentalRoute(rentalId: String): String = "rental/active/$rentalId"

    fun otpRoute(phoneNumber: String): String = "auth/otp/$phoneNumber"

    val bottomBarRoutes = setOf(Map, RentalHistory, Wallet, Profile)
}
