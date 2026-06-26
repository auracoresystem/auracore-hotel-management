package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DashboardData(
    val occupancy: String = "0%",
    val revenue: String = "$0",
    val attendance: String = "0/0",
    val pendingTasks: String = "0",
    val kitchenWastage: String = "0 kg",
    val complaints: String = "0",
    val inventoryAlerts: String = "0",
    val notifications: String = "0"
)

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: DashboardData) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _dashboardState.value = DashboardState.Loading
        listenerRegistration?.remove()

        // Assume we have a document "dashboard/summary" that holds all these fields
        listenerRegistration = firestore.collection("dashboard").document("summary")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _dashboardState.value = DashboardState.Error("Failed to load data: ${error.localizedMessage}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val data = DashboardData(
                        occupancy = snapshot.getString("occupancy") ?: "0%",
                        revenue = snapshot.getString("revenue") ?: "$0",
                        attendance = snapshot.getString("attendance") ?: "0/0",
                        pendingTasks = snapshot.getString("pendingTasks") ?: "0",
                        kitchenWastage = snapshot.getString("kitchenWastage") ?: "0 kg",
                        complaints = snapshot.getString("complaints") ?: "0",
                        inventoryAlerts = snapshot.getString("inventoryAlerts") ?: "0",
                        notifications = snapshot.getString("notifications") ?: "0"
                    )
                    _dashboardState.value = DashboardState.Success(data)
                } else {
                    // Document doesn't exist yet, we can show default 0 values
                    _dashboardState.value = DashboardState.Success(DashboardData())
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
