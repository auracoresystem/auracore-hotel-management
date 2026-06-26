package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wastage.WastageViewModel
import com.example.ui.HotelViewModel
import kotlinx.serialization.Serializable

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
@Serializable object SetupRoute

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    wastageViewModel: WastageViewModel,
    hotelViewModel: HotelViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier
    ) {
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
                onNavigateToSetup = { navController.navigate(SetupRoute) }
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
            ReceptionScreen(hotelViewModel = hotelViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<CleaningRoute> {
            CleaningScreen(hotelViewModel = hotelViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<RepairsRoute> {
            RepairsScreen(hotelViewModel = hotelViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<StaffRoute> {
            StaffScreen(hotelViewModel = hotelViewModel, onBackClick = { navController.popBackStack() })
        }
        composable<ReportsRoute> {
            ReportsScreen(onBackClick = { navController.popBackStack() })
        }
        composable<SetupRoute> {
            SetupScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

