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
import com.example.wastage.WasteRepository

import com.example.ui.HotelViewModel
import com.example.ui.HotelViewModelFactory
import com.example.data.HotelRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val wasteRepository = WasteRepository(database.wasteDao())
        val hotelRepository = HotelRepository(database.hotelDao())
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val wastageViewModel: WastageViewModel = viewModel(
                        factory = WastageViewModelFactory(wasteRepository)
                    )
                    val hotelViewModel: HotelViewModel = viewModel(
                        factory = HotelViewModelFactory(hotelRepository)
                    )
                    AppNavigation(wastageViewModel = wastageViewModel, hotelViewModel = hotelViewModel)
                }
            }
        }
    }
}
