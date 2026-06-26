package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wastage.WastageViewModel
import kotlinx.serialization.Serializable

@Serializable
object DashboardRoute

@Serializable
object KitchenWastageRoute

@Serializable
object AddWastageRoute

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    wastageViewModel: WastageViewModel
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
    }
}
