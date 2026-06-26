package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wastage.WastageViewModel
import com.example.ui.HotelViewModel
import kotlinx.serialization.Serializable

import com.example.ui.AuthViewModel
import com.example.ui.LoginScreen

@Serializable
object LoginRoute

@Serializable
object DashboardRoute

@Serializable
object KitchenWastageRoute

@Serializable
object AddWastageRoute

@Serializable object ReceptionRoute
@Serializable object CleaningRoute
@Serializable object RepairsRoute
@Serializable object StaffRoute
@Serializable object ReportsRoute
@Serializable object ProfileRoute
@Serializable object InventoryRoute
@Serializable object HrRoute
@Serializable object LaundryRoute
@Serializable object SecurityRoute
@Serializable object HubRoute
@Serializable object NotificationRoute

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    wastageViewModel: WastageViewModel,
    hotelViewModel: HotelViewModel,
    authViewModel: AuthViewModel,
    receptionViewModel: ReceptionViewModel,
    housekeepingViewModel: HousekeepingViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    inventoryViewModel: InventoryViewModel,
    hrViewModel: HrViewModel,
    laundryViewModel: LaundryViewModel,
    securityViewModel: SecurityViewModel,
    hubViewModel: HubViewModel,
    profileViewModel: ProfileViewModel,
    notificationViewModel: NotificationViewModel,
    reportsViewModel: ReportsViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier
    ) {
        composable<LoginRoute> {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToDashboard = { role ->
                    navController.navigate(DashboardRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<DashboardRoute> {
            DashboardScreen(
                onNavigateToKitchenWastage = {
                    navController.navigate(KitchenWastageRoute)
                },
                onNavigateToReception = { navController.navigate(ReceptionRoute) },
                onNavigateToCleaning = { navController.navigate(CleaningRoute) },
                onNavigateToRepairs = { navController.navigate(RepairsRoute) },
                onNavigateToStaff = { navController.navigate(StaffRoute) },
                onNavigateToReports = { navController.navigate(ReportsRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute) },
                onNavigateToInventory = { navController.navigate(InventoryRoute) },
                onNavigateToHr = { navController.navigate(HrRoute) },
                onNavigateToLaundry = { navController.navigate(LaundryRoute) },
                onNavigateToSecurity = { navController.navigate(SecurityRoute) },
                onNavigateToHub = { navController.navigate(HubRoute) },
                onNavigateToNotifications = { navController.navigate(NotificationRoute) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(LoginRoute) {
                        popUpTo(DashboardRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<KitchenWastageRoute> {
            KitchenWastageScreen(
                viewModel = wastageViewModel,
                onBackClick = { navController.popBackStack() },
                onAddClick = { navController.navigate(AddWastageRoute) }
            )
        }
        composable<AddWastageRoute> {
            AddWastageScreen(
                viewModel = wastageViewModel,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable<ReceptionRoute> {
            ReceptionScreen(viewModel = receptionViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<CleaningRoute> {
            HousekeepingScreen(viewModel = housekeepingViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<RepairsRoute> {
            MaintenanceScreen(viewModel = maintenanceViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<InventoryRoute> {
            InventoryScreen(viewModel = inventoryViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<StaffRoute> {
            StaffScreen(hotelViewModel = hotelViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<ReportsRoute> {
            ReportsScreen(viewModel = reportsViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<ProfileRoute> {
            ProfileScreen(viewModel = profileViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<HrRoute> {
            HrScreen(viewModel = hrViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<LaundryRoute> {
            LaundryScreen(viewModel = laundryViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<SecurityRoute> {
            SecurityScreen(viewModel = securityViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<HubRoute> {
            HubScreen(viewModel = hubViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<NotificationRoute> {
            NotificationScreen(viewModel = notificationViewModel, onBackClick = { navController.popBackStack() })
        }
    }
}

