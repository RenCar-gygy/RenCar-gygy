package com.turkcell.rencarapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// --- Furkan ve Çağla'nın Ekran Importları ---
import com.turkcell.rencarapp.ui.auth.login.LoginRoute
import com.turkcell.rencarapp.ui.auth.otp.OtpRoute
import com.turkcell.rencarapp.ui.license.LicenseRoute
import com.turkcell.rencarapp.ui.map.MapRoute
import com.turkcell.rencarapp.ui.onboarding.OnboardingRoute
import com.turkcell.rencarapp.ui.profile.ProfileRoute
import com.turkcell.rencarapp.ui.splash.SplashRoute

// --- Eklenen Yeni Çağla Ekranları Importları ---
import com.turkcell.rencarapp.ui.payment.wallet.WalletRoute
import com.turkcell.rencarapp.ui.rental.history.RentalHistoryRoute
import com.turkcell.rencarapp.ui.rental.summary.RentalSummaryRoute

/**
 * Sprint 0-1 navigasyon iskeleti — iç içe grafikler.
 * Ekip bölüşümüne göre tamamlanan ekranlar bağlanmıştır.
 */
@Composable
fun RenCarNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in RenCarDestination.bottomBarRoutes

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                RenCarBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { tab -> navController.navigateToMainTab(tab.destination) },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RenCarDestination.Splash,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(RenCarDestination.Splash) {
                SplashRoute(
                    onNavigateToOnboarding = {
                        navController.navigate(RenCarDestination.Onboarding) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(RenCarDestination.Login) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            navigation(
                startDestination = RenCarDestination.Onboarding,
                route = RenCarDestination.AuthGraph,
            ) {
                composable(RenCarDestination.Onboarding) {
                    OnboardingRoute(
                        onNavigateToRegister = {
                            navController.navigate(RenCarDestination.Register) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(RenCarDestination.Login) {
                    LoginRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToOtp = { phone ->
                            navController.navigate(RenCarDestination.otpRoute(phone)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(RenCarDestination.Register) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(RenCarDestination.Register) {
                    PlaceholderScreen(title = "Kayıt")
                }
                composable(
                    route = RenCarDestination.Otp,
                    arguments = listOf(
                        navArgument(RenCarDestination.ARG_PHONE_NUMBER) { type = NavType.StringType },
                    ),
                ) {
                    OtpRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = { navController.popBackStack() },
                        onNavigateToLicense = {
                            navController.navigate(RenCarDestination.License) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(RenCarDestination.License) {
                    LicenseRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToMain = {
                            navController.navigate(RenCarDestination.Map) {
                                popUpTo(RenCarDestination.AuthGraph) { inclusive = true }
                            }
                        },
                    )
                }
            }

            navigation(
                startDestination = RenCarDestination.Map,
                route = RenCarDestination.MainGraph,
            ) {
                composable(RenCarDestination.Map) {
                    MapRoute(
                        onNavigateToVehicleDetail = { vehicleId ->
                            navController.navigate(RenCarDestination.vehicleDetailRoute(vehicleId))
                        },
                    )
                }

                // --- ÇAĞLA'NIN EKRANLARI ---
                composable(RenCarDestination.RentalHistory) {
                    RentalHistoryRoute(
                        onShowSnackbar = { _ -> /* TODO */ }
                    )
                }
                composable(RenCarDestination.Wallet) {
                    WalletRoute(
                        onShowSnackbar = { _ -> /* TODO */ }
                    )
                }
                composable(RenCarDestination.Profile) {
                    ProfileRoute(
                        onNavigateToSplash = {
                            navController.navigate(RenCarDestination.Splash) {
                                popUpTo(RenCarDestination.MainGraph) { inclusive = true }
                            }
                        },
                        onShowSnackbar = { _ -> /* TODO */ },
                    )
                }

                navigation(
                    startDestination = RenCarDestination.VehicleDetail,
                    route = RenCarDestination.RentalGraph,
                ) {
                    // --- NAZLI'NIN EKRANLARI (Placeholder olarak korundu) ---
                    composable(
                        route = RenCarDestination.VehicleDetail,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                        ),
                    ) {
                        PlaceholderScreen(title = "Araç Detay")
                    }
                    composable(
                        route = RenCarDestination.RentalConfirmation,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                        ),
                    ) {
                        PlaceholderScreen(title = "Rezervasyon Onayı")
                    }

                    // --- ÇAĞLA'NIN EKRANI ---
                    composable(
                        route = RenCarDestination.RentalSummary,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                        ),
                    ) {
                        RentalSummaryRoute(
                            onNavigateToDeliveryPhotos = { vehicleId ->
                                navController.navigate(RenCarDestination.deliveryPhotosRoute(vehicleId)) {
                                    launchSingleTop = true
                                }
                            },
                            onShowSnackbar = { _ -> /* TODO */ }
                        )
                    }

                    // --- NAZLI'NIN EKRANLARI (Placeholder olarak korundu) ---
                    composable(
                        route = RenCarDestination.DeliveryPhotos,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                        ),
                    ) {
                        PlaceholderScreen(title = "Teslim Fotoğrafı (4 Yön)")
                    }
                    composable(
                        route = RenCarDestination.ActiveRental,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType },
                        ),
                    ) {
                        PlaceholderScreen(title = "Aktif Kiralama")
                    }
                }
            }
        }
    }
}

private fun NavHostController.navigateToMainTab(destination: String) {
    navigate(destination) {
        popUpTo(RenCarDestination.Map) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}