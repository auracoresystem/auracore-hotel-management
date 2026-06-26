package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class Visitor(
    val id: String = "",
    val name: String = "",
    val contact: String = "",
    val purpose: String = "",
    val checkInTime: Long = 0L,
    val checkOutTime: Long = 0L,
    val photoUrl: String = "",
    val idUrl: String = "",
    val vehicleNumber: String = "",
    val status: String = "Checked In" // Checked In, Checked Out
)

data class IncidentReport(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "General", // General, Emergency, Maintenance
    val reportedAt: Long = 0L,
    val reportedBy: String = "",
    val status: String = "Open"
)

class SecurityViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _visitors = MutableStateFlow<List<Visitor>>(emptyList())
    val visitors: StateFlow<List<Visitor>> = _visitors.asStateFlow()
    
    private val _incidents = MutableStateFlow<List<IncidentReport>>(emptyList())
    val incidents: StateFlow<List<IncidentReport>> = _incidents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadVisitors()
        loadIncidents()
    }

    private fun loadVisitors() {
        firestore.collection("visitors")
            .orderBy("checkInTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _visitors.value = snapshot.toObjects(Visitor::class.java)
                }
            }
    }
    
    private fun loadIncidents() {
        firestore.collection("incidents")
            .orderBy("reportedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _incidents.value = snapshot.toObjects(IncidentReport::class.java)
                }
            }
    }

    fun addVisitor(name: String, purpose: String, vehicle: String, photoUri: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var photoUrl = ""
                if (photoUri != null) {
                    val uri = Uri.parse(photoUri)
                    val ref = storage.reference.child("visitors/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                val visitor = Visitor(
                    id = firestore.collection("visitors").document().id,
                    name = name,
                    purpose = purpose,
                    vehicleNumber = vehicle,
                    photoUrl = photoUrl,
                    checkInTime = System.currentTimeMillis()
                )

                firestore.collection("visitors").document(visitor.id).set(visitor).await()
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun checkOutVisitor(visitorId: String) {
        viewModelScope.launch {
             firestore.collection("visitors").document(visitorId).update(
                 mapOf(
                     "status" to "Checked Out",
                     "checkOutTime" to System.currentTimeMillis()
                 )
             )
        }
    }
    
    fun reportIncident(title: String, description: String, type: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val incident = IncidentReport(
                    id = firestore.collection("incidents").document().id,
                    title = title,
                    description = description,
                    type = type,
                    reportedAt = System.currentTimeMillis()
                )
                firestore.collection("incidents").document(incident.id).set(incident).await()
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
