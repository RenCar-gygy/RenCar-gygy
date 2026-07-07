package com.turkcell.rencarapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.turkcell.rencarapp.ui.rental.confirmation.RentalConfirmationRoute
import com.turkcell.rencarapp.ui.rental.delivery_photos.DeliveryPhotosRoute
import com.turkcell.rencarapp.ui.vehicle.detail.VehicleDetailRoute
import com.turkcell.rencarapp.ui.rental.history.RentalHistoryRoute
import com.turkcell.rencarapp.ui.rental.summary.RentalSummaryRoute

@RequiresApi(Build.VERSION_CODES.O)
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
                    RegisterRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToOtp = { phone ->
                            navController.navigate(RenCarDestination.otpRoute(phone)) {
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

                composable(RenCarDestination.RentalHistory) {
                    RentalHistoryRoute(
                        onShowSnackbar = {}
                    )
                }
                composable(RenCarDestination.Wallet) {
                    WalletRoute(
                        onShowSnackbar = { _ -> }
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
                            navController.navigate(RenCarDestination.Wallet) {
                                launchSingleTop = true
                            }
                        },
                        onShowSnackbar = { _ -> },
                    )
                }

                navigation(
                    startDestination = RenCarDestination.VehicleDetail,
                    route = RenCarDestination.RentalGraph,
                ) {
                    composable(
                        route = RenCarDestination.VehicleDetail,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                        ),
                    ) {
                        VehicleDetailRoute(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToConfirmation = { vehicleId ->
                                navController.navigate(RenCarDestination.rentalConfirmationRoute(vehicleId))
                            }
                        )
                    }
                    composable(
                        route = RenCarDestination.RentalConfirmation,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                        ),
                    ) {
                        RentalConfirmationRoute(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToSummary = { vehicleId, plan ->
                                navController.navigate(RenCarDestination.rentalSummaryRoute(vehicleId, plan))
                            }
                        )
                    }

                    composable(
                        route = RenCarDestination.RentalSummary,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                            navArgument(RenCarDestination.ARG_PLAN) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    ) {
                        RentalSummaryRoute(
                            onNavigateToDeliveryPhotos = { rentalId ->
                                navController.navigate(RenCarDestination.deliveryPhotosRoute(rentalId)) {
                                    launchSingleTop = true
                                }
                            },
                            onShowSnackbar = { _ -> }
                        )
                    }

                    composable(
                        route = RenCarDestination.DeliveryPhotos,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType },
                        ),
                    ) {
                        DeliveryPhotosRoute(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToActiveRental = { rentalId ->
                                navController.navigate(RenCarDestination.activeRentalRoute(rentalId))
                            }
                        )
                    }
                    composable(
                        route = RenCarDestination.ActiveRental,
                        arguments = listOf(
                            navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType },
                        ),
                    ) {
                        ActiveRentalRoute(
                            onNavigateToMain = {
                                navController.navigate(RenCarDestination.MainGraph) {
                                    popUpTo(RenCarDestination.Map) { inclusive = true }
                                }
                            }
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