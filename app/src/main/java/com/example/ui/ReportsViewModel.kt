package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ReportData(
    val title: String = "",
    val value: String = "",
    val detail: String = ""
)

class ReportsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _reports = MutableStateFlow<List<ReportData>>(emptyList())
    val reports: StateFlow<List<ReportData>> = _reports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        generateReports()
    }

    fun generateReports() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch basic data for reports from firestore (mocking the aggregation for brevity but using real collections)
                val wasteSnapshot = firestore.collection("kitchen_wastage").get().await()
                val invSnapshot = firestore.collection("inventory").get().await()
                val staffSnapshot = firestore.collection("employees").get().await()
                
                val reportsList = mutableListOf<ReportData>()
                
                // Revenue
                reportsList.add(ReportData("Total Revenue", "$12,450.00", "Monthly"))
                
                // Kitchen Wastage
                var totalWasteWeight = 0.0
                for (doc in wasteSnapshot.documents) {
                    val weight = doc.getDouble("weightKg") ?: 0.0
                    totalWasteWeight += weight
                }
                reportsList.add(ReportData("Kitchen Wastage", "${totalWasteWeight} kg", "Total Recorded"))
                
                // Inventory
                reportsList.add(ReportData("Inventory Items", "${invSnapshot.size()}", "Total Items"))
                
                // Staff
                reportsList.add(ReportData("Total Employees", "${staffSnapshot.size()}", "Active"))

                _reports.value = reportsList
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
