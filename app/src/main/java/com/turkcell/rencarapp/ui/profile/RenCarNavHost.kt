package com.turkcell.rencarapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.turkcell.rencarapp.ui.profile.ProfileRoute

/**
 * RenCarApp kök navigasyon bileşeni.
 * * [docs/decisions.md] kararları gereği:
 * 1. Dışta tek bir Scaffold bulunur.
 * 2. BottomBar yalnızca [RenCarDestination.bottomBarRoutes] ile eşleşen rotalarda görünür.
 * 3. Navigasyon yapısı 3 ana grafiğe ayrılmıştır: Auth, Main ve nested Rental.
 */
@Composable
fun RenCarNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in RenCarDestination.bottomBarRoutes) {
                // RenCarBottomBar Sprint 0'da oluşturulmuş varsayılmaktadır.
                // onNavigate lambda'sı sekmeler arası geçiş state'ini korur.
                RenCarBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(RenCarDestination.Map) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RenCarDestination.Splash,
            modifier = modifier.padding(innerPadding)
        ) {
            // ==========================================
            // KÖK GRAFİK
            // ==========================================
            composable(route = RenCarDestination.Splash) {
                // Placeholder: SplashRoute
                // onNavigateToAuth = { navController.navigate(RenCarDestination.AuthGraph) { popUpTo(RenCarDestination.Splash) { inclusive = true } } }
                // onNavigateToMain = { navController.navigate(RenCarDestination.MainGraph) { popUpTo(RenCarDestination.Splash) { inclusive = true } } }
            }

            // ==========================================
            // 1. AUTH GRAFİĞİ (Nested)
            // ==========================================
            navigation(
                startDestination = RenCarDestination.Onboarding,
                route = RenCarDestination.AuthGraph
            ) {
                composable(route = RenCarDestination.Onboarding) { /* Placeholder: OnboardingRoute */ }
                composable(route = RenCarDestination.Login) { /* Placeholder: LoginRoute */ }
                composable(route = RenCarDestination.Register) { /* Placeholder: RegisterRoute */ }
                composable(route = RenCarDestination.Otp) { /* Placeholder: OtpRoute */ }
                composable(route = RenCarDestination.License) { /* Placeholder: LicenseRoute */ }
            }

            // ==========================================
            // 2. MAIN GRAFİĞİ (Nested)
            // ==========================================
            navigation(
                startDestination = RenCarDestination.Map,
                route = RenCarDestination.MainGraph
            ) {
                // --- Alt Çubuk Sekmeleri ---
                composable(route = RenCarDestination.Map) { /* Placeholder: MapRoute */ }
                composable(route = RenCarDestination.RentalHistory) { /* Placeholder: RentalHistoryRoute */ }
                composable(route = RenCarDestination.Wallet) { /* Placeholder: WalletRoute */ }

                composable(route = RenCarDestination.Profile) {
                    ProfileRoute(
                        onNavigateToSplash = {
                            navController.navigate(RenCarDestination.Splash) {
                                // Çıkış yapıldığında Main grafiği geçmişini tamamen temizle
                                popUpTo(RenCarDestination.MainGraph) { inclusive = true }
                            }
                        },
                        onShowSnackbar = { message ->
                            // TODO: Scaffold'un SnackbarHostState'ine mesajı ilet
                        }
                    )
                }

                // ==========================================
                // 3. RENTAL ALT GRAFİĞİ (Main İçinde Nested)
                // ==========================================
                navigation(
                    startDestination = RenCarDestination.VehicleDetail,
                    route = RenCarDestination.RentalGraph
                ) {
                    composable(route = RenCarDestination.VehicleDetail) {
                        // Placeholder: VehicleDetailRoute
                        // val vehicleId = it.arguments?.getString(RenCarDestination.ARG_VEHICLE_ID)
                    }
                    composable(route = RenCarDestination.RentalConfirmation) { /* Placeholder: RentalConfirmationRoute */ }
                    composable(route = RenCarDestination.RentalSummary) { /* Placeholder: RentalSummaryRoute */ }
                    composable(route = RenCarDestination.DeliveryPhotos) { /* Placeholder: DeliveryPhotosRoute */ }
                    composable(route = RenCarDestination.ActiveRental) { /* Placeholder: ActiveRentalRoute */ }
                }
            }
        }
    }
}