package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.wastage.AppDatabase
import com.example.wastage.WastageViewModel
import com.example.wastage.WastageViewModelFactory

import com.example.ui.HotelViewModel
import com.example.ui.HotelViewModelFactory
import com.example.data.HotelRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val hotelRepository = HotelRepository(database.hotelDao())
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val wastageViewModel: WastageViewModel = viewModel(
                        factory = WastageViewModelFactory()
                    )
                    val hotelViewModel: HotelViewModel = viewModel(
                        factory = HotelViewModelFactory(hotelRepository)
                    )
                    val authViewModel: com.example.ui.AuthViewModel = viewModel()
                    val receptionViewModel: com.example.ui.ReceptionViewModel = viewModel()
                    val housekeepingViewModel: com.example.ui.HousekeepingViewModel = viewModel()
                    val maintenanceViewModel: com.example.ui.MaintenanceViewModel = viewModel()
                    val inventoryViewModel: com.example.ui.InventoryViewModel = viewModel()
                    val hrViewModel: com.example.ui.HrViewModel = viewModel()
                    val laundryViewModel: com.example.ui.LaundryViewModel = viewModel()
                    val securityViewModel: com.example.ui.SecurityViewModel = viewModel()
                    val hubViewModel: com.example.ui.HubViewModel = viewModel()
                    val profileViewModel: com.example.ui.ProfileViewModel = viewModel()
                    val notificationViewModel: com.example.ui.NotificationViewModel = viewModel()
                    val reportsViewModel: com.example.ui.ReportsViewModel = viewModel()
                    
                    AppNavigation(
                        wastageViewModel = wastageViewModel,
                        hotelViewModel = hotelViewModel,
                        authViewModel = authViewModel,
                        receptionViewModel = receptionViewModel,
                        housekeepingViewModel = housekeepingViewModel,
                        maintenanceViewModel = maintenanceViewModel,
                        inventoryViewModel = inventoryViewModel,
                        hrViewModel = hrViewModel,
                        laundryViewModel = laundryViewModel,
                        securityViewModel = securityViewModel,
                        hubViewModel = hubViewModel,
                        profileViewModel = profileViewModel,
                        notificationViewModel = notificationViewModel,
                        reportsViewModel = reportsViewModel
                    )
                }
            }
        }
    }
}
