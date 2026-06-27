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
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (e: Exception) { null }

    private val _visitors = MutableStateFlow<List<Visitor>>(emptyList())
    val visitors: StateFlow<List<Visitor>> = _visitors.asStateFlow()
    
    private val _incidents = MutableStateFlow<List<IncidentReport>>(emptyList())
    val incidents: StateFlow<List<IncidentReport>> = _incidents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val offlineVisitors = listOf(
        Visitor(id = "visitor_1", name = "Rahul Khanna", purpose = "AC Service", checkInTime = System.currentTimeMillis() - 7200000, vehicleNumber = "DL-3C-AB-1234"),
        Visitor(id = "visitor_2", name = "Vikram Singh", purpose = "Delivery", checkInTime = System.currentTimeMillis() - 3600000, vehicleNumber = "HR-26-XY-5678")
    )
    private val offlineIncidents = listOf(
        IncidentReport(id = "incident_1", title = "Parking space dispute", description = "Minor dispute between guest AC-102 & external visitor over parking spot #4.", type = "General", reportedAt = System.currentTimeMillis() - 14400000)
    )

    init {
        loadVisitors()
        loadIncidents()
    }

    private fun loadVisitors() {
        _visitors.value = offlineVisitors
        val db = firestore ?: return
        try {
            db.collection("visitors")
                .orderBy("checkInTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _visitors.value = snapshot.toObjects(Visitor::class.java)
                    } else {
                        _visitors.value = offlineVisitors
                    }
                }
        } catch (e: Exception) {
            _visitors.value = offlineVisitors
        }
    }
    
    private fun loadIncidents() {
        _incidents.value = offlineIncidents
        val db = firestore ?: return
        try {
            db.collection("incidents")
                .orderBy("reportedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _incidents.value = snapshot.toObjects(IncidentReport::class.java)
                    } else {
                        _incidents.value = offlineIncidents
                    }
                }
        } catch (e: Exception) {
            _incidents.value = offlineIncidents
        }
    }

    fun addVisitor(name: String, purpose: String, vehicle: String, photoUri: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var photoUrl = ""
                val firebaseStorage = storage
                if (photoUri != null && firebaseStorage != null) {
                    val uri = Uri.parse(photoUri)
                    val ref = firebaseStorage.reference.child("visitors/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                val db = firestore
                val newId = db?.collection("visitors")?.document()?.id ?: "visitor_${System.currentTimeMillis()}"
                val visitor = Visitor(
                    id = newId,
                    name = name,
                    purpose = purpose,
                    vehicleNumber = vehicle,
                    photoUrl = photoUrl,
                    checkInTime = System.currentTimeMillis()
                )

                if (db != null) {
                    db.collection("visitors").document(visitor.id).set(visitor).await()
                } else {
                    val updated = _visitors.value.toMutableList()
                    updated.add(0, visitor)
                    _visitors.value = updated
                }
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun checkOutVisitor(visitorId: String) {
        viewModelScope.launch {
            val db = firestore
            if (db != null) {
                db.collection("visitors").document(visitorId).update(
                    mapOf(
                        "status" to "Checked Out",
                        "checkOutTime" to System.currentTimeMillis()
                    )
                )
            } else {
                val index = _visitors.value.indexOfFirst { it.id == visitorId }
                if (index != -1) {
                    val updated = _visitors.value.toMutableList()
                    updated[index] = updated[index].copy(
                        status = "Checked Out",
                        checkOutTime = System.currentTimeMillis()
                    )
                    _visitors.value = updated
                }
            }
        }
    }
    
    fun reportIncident(title: String, description: String, type: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                val newId = db?.collection("incidents")?.document()?.id ?: "incident_${System.currentTimeMillis()}"
                val incident = IncidentReport(
                    id = newId,
                    title = title,
                    description = description,
                    type = type,
                    reportedAt = System.currentTimeMillis()
                )
                if (db != null) {
                    db.collection("incidents").document(incident.id).set(incident).await()
                } else {
                    val updated = _incidents.value.toMutableList()
                    updated.add(0, incident)
                    _incidents.value = updated
                }
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
