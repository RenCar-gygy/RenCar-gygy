package com.turkcell.rencarapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.turkcell.rencarapp.ui.auth.login.LoginRoute
import com.turkcell.rencarapp.ui.auth.otp.OtpRoute
import com.turkcell.rencarapp.ui.auth.register.RegisterRoute
import com.turkcell.rencarapp.ui.license.LicenseRoute
import com.turkcell.rencarapp.ui.map.MapRoute
import com.turkcell.rencarapp.ui.onboarding.OnboardingRoute
import com.turkcell.rencarapp.ui.profile.ProfileRoute
import com.turkcell.rencarapp.ui.splash.SplashRoute

import com.turkcell.rencarapp.ui.payment.wallet.WalletRoute
import com.turkcell.rencarapp.ui.rental.active.ActiveRentalRoute
import com.turkcell.rencarapp.ui.rental.active.ActiveRentalViewModel
import com.turkcell.rencarapp.ui.rental.confirmation.RentalConfirmationRoute
import com.turkcell.rencarapp.ui.rental.delivery_photos.DeliveryPhotosRoute
import com.turkcell.rencarapp.ui.rental.start_photos.StartPhotosRoute
import com.turkcell.rencarapp.ui.vehicle.detail.VehicleDetailRoute
import com.turkcell.rencarapp.ui.rental.history.RentalHistoryRoute
import com.turkcell.rencarapp.ui.rental.summary.RentalSummaryRoute
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RenCarNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = currentDestination?.hierarchy?.any { destination ->
        destination.route in RenCarDestination.bottomBarRoutes
    } == true
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val showSnackbar: (String) -> Unit = remember(snackbarHostState) {
        { message ->
            snackbarScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                RenCarBottomBar(
                    currentDestination = currentDestination,
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
                            popUpTo(RenCarDestination.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(RenCarDestination.Login) {
                            popUpTo(RenCarDestination.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLicense = {
                        navController.navigate(RenCarDestination.License) {
                            popUpTo(RenCarDestination.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToMain = {
                        navController.navigate(RenCarDestination.Map) {
                            popUpTo(RenCarDestination.Splash) { inclusive = true }
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
                        onNavigateToOtp = { phone, expiresAt ->
                            navController.navigate(RenCarDestination.otpRoute(phone, expiresAt)) {
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
                    RegisterRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLicense = {
                            navController.navigate(RenCarDestination.License) {
                                popUpTo(RenCarDestination.Register) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToMain = {
                            navController.navigate(RenCarDestination.Map) {
                                popUpTo(RenCarDestination.AuthGraph) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(RenCarDestination.Login) {
                                popUpTo(RenCarDestination.Register) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(
                    route = RenCarDestination.Otp,
                    arguments = listOf(
                        navArgument(RenCarDestination.ARG_PHONE_NUMBER) { type = NavType.StringType },
                        navArgument(RenCarDestination.ARG_OTP_EXPIRES_AT) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
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
                        onNavigateToMain = {
                            navController.navigate(RenCarDestination.Map) {
                                popUpTo(RenCarDestination.AuthGraph) { inclusive = true }
                            }
                        },
                    )
                }
                composable(RenCarDestination.License) {
                    LicenseRoute(
                        onNavigateToLogin = {
                            navController.navigate(RenCarDestination.Login) {
                                popUpTo(RenCarDestination.AuthGraph) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
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
                        onNavigateToVehicleDetail = { vehicleId, lat, lng ->
                            navController.navigate(RenCarDestination.vehicleDetailRoute(vehicleId, lat, lng))
                        },
                        onNavigateToActiveRental = { rentalId ->
                            navController.navigate(RenCarDestination.activeRentalRoute(rentalId)) {
                                popUpTo(RenCarDestination.Map) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToConfirmation = { vehicleId ->
                            navController.navigate(RenCarDestination.rentalConfirmationRoute(vehicleId, null))
                        },
                    )
                }

                composable(RenCarDestination.RentalHistory) {
                    RentalHistoryRoute(
                        onNavigateToSummary = { rentalId ->
                            navController.navigate(RenCarDestination.rentalSummaryRoute(rentalId))
                        },
                        onShowSnackbar = showSnackbar,
                    )
                }
                composable(RenCarDestination.Wallet) {
                    WalletRoute(
                        onShowSnackbar = showSnackbar,
                    )
                }
                composable(RenCarDestination.Profile) {
                    ProfileRoute(
                        onNavigateToSplash = {
                            navController.navigate(RenCarDestination.Splash) {
                                popUpTo(RenCarDestination.MainGraph) { inclusive = true }
                            }
                        },
                        onNavigateToWallet = {
                            navController.navigateToMainTab(RenCarDestination.Wallet)
                        },
                        onShowSnackbar = showSnackbar,
                    )
                }

                // ==========================================
                // YENİ KİRALAMA AKIŞI (RENTAL GRAPH)
                // ==========================================
                navigation(
                    startDestination = RenCarDestination.VehicleDetail,
                    route = RenCarDestination.RentalGraph,
                ) {
                    // 1. ADIM: Araç Detay
                    composable(
                        route = RenCarDestination.VehicleDetail,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                            navArgument(RenCarDestination.ARG_USER_LAT) {
                                type = NavType.StringType // NavArguments only support certain types, Double is not directly there for URL queries usually, but NavType.FloatType or StringType.
                                // Actually StringType is safer for optional query params.
                                nullable = true
                                defaultValue = null
                            },
                            navArgument(RenCarDestination.ARG_USER_LNG) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    ) {
                        VehicleDetailRoute(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToConfirmation = { vehicleId, plan ->
                                navController.navigate(
                                    RenCarDestination.rentalConfirmationRoute(vehicleId, plan.name)
                                )
                            },
                            onNavigateToActiveRental = { rentalId ->
                                navController.navigate(RenCarDestination.activeRentalRoute(rentalId)) {
                                    popUpTo(RenCarDestination.Map) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 2. ADIM: Rezervasyon ve Şartlar
                    composable(
                        route = RenCarDestination.RentalConfirmation,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                            navArgument(RenCarDestination.ARG_PLAN) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    ) {
                        RentalConfirmationRoute(
                            onNavigateBack = { navController.popBackStack() },
                            // GÜNCELLENDİ: Onaydan sonra doğrudan Aktif Kiralamaya (Kilidi Aç) geçer
                            onNavigateToActiveRental = { rentalId ->
                                navController.navigate(RenCarDestination.activeRentalRoute(rentalId)) {
                                    popUpTo(RenCarDestination.Map) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 3. ADIM: Aktif Kiralama (Kullanım ve Sayaç)
                    composable(
                        route = RenCarDestination.ActiveRental,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType },
                        ),
                    ) {
                        ActiveRentalRoute(
                            onNavigateToStartPhotos = { rentalId, name, plate ->
                                navController.navigate(RenCarDestination.startPhotosRoute(rentalId, name, plate))
                            },
                            onNavigateToSummary = { rentalId ->
                                navController.navigate(RenCarDestination.rentalSummaryRoute(rentalId))
                            },
                            onNavigateBackAfterCancel = {
                                navController.popBackStack(RenCarDestination.Map, false)
                            },
                            onNavigateBackToMap = {
                                navController.popBackStack(RenCarDestination.Map, false)
                            },
                        )
                    }

                    // 3b. ADIM: Yolculuk öncesi fotoğraflar (dk/sa — API zorunlu)
                    composable(
                        route = RenCarDestination.StartPhotos,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType },
                            navArgument(RenCarDestination.ARG_VEHICLE_NAME) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                            navArgument(RenCarDestination.ARG_VEHICLE_PLATE) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    ) {
                        StartPhotosRoute(
                            onNavigateBack = { navController.popBackStack() },
                            onRideStarted = {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(ActiveRentalViewModel.RIDE_STARTED_KEY, true)
                                navController.popBackStack()
                            }
                        )
                    }

                    // 4. ADIM: Araç Fotoğrafları Çekimi (Teslim — ürün stub, API karşılığı yok)
                    composable(
                        route = RenCarDestination.DeliveryPhotos,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType },
                            navArgument(RenCarDestination.ARG_VEHICLE_NAME) { 
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                            navArgument(RenCarDestination.ARG_VEHICLE_PLATE) { 
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    ) {
                        DeliveryPhotosRoute(
                            onNavigateBack = { navController.popBackStack() },
                            // GÜNCELLENDİ: Fotoğraflar yüklendikten sonra Ödeme/Özet ekranına geçer
                            onNavigateToSummary = { rentalId ->
                                navController.navigate(RenCarDestination.rentalSummaryRoute(rentalId))
                            }
                        )
                    }

                    // 5. ADIM: Ödeme ve Kiralama Özeti (Fatura)
                    composable(
                        route = RenCarDestination.RentalSummary,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType }
                        ),
                    ) {
                        RentalSummaryRoute(
                            // Ödeme tamamlandığında Ana Ekrana (Map) döner
                            onNavigateToHome = {
                                navController.navigate(RenCarDestination.Map) {
                                    popUpTo(RenCarDestination.MainGraph) { inclusive = true }
                                }
                            },
                            onShowSnackbar = showSnackbar
                        )
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