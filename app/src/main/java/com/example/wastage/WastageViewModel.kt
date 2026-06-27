package com.example.wastage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

data class WasteRecord(
    val id: String = "",
    val itemName: String = "",
    val category: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val reason: String = "",
    val remarks: String = "",
    val photoUri: String = "",
    val timestamp: Long = 0L,
    val date: String = "",
    val time: String = "",
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val hotel: String = "AuraCore Hotel",
    val department: String = "Kitchen",
    val staffName: String = "",
    val staffId: String = "",
    val userId: String = "",
    val status: String = "Pending"
)

class WastageViewModel : ViewModel() {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (e: Exception) { null }
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _uiState = MutableStateFlow<List<WasteRecord>>(emptyList())
    val uiState: StateFlow<List<WasteRecord>> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val offlineWastage = listOf(
        WasteRecord(
            id = "waste_w_1",
            itemName = "Morning Buffet Leftover",
            category = "Prepared Meals",
            quantity = 6.8,
            unit = "kg",
            reason = "Overproduction",
            remarks = "Excess food from large tour group breakfast.",
            timestamp = System.currentTimeMillis() - 7200000,
            date = "Today",
            status = "Logged",
            staffName = "Chef Amit"
        )
    )

    init {
        loadWasteRecords()
    }

    private fun loadWasteRecords() {
        _uiState.value = offlineWastage
        val db = firestore ?: return
        try {
            db.collection("kitchen_wastage")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val records = snapshot.toObjects(WasteRecord::class.java)
                        _uiState.value = records
                    } else {
                        _uiState.value = offlineWastage
                    }
                }
        } catch (e: Exception) {
            _uiState.value = offlineWastage
        }
    }

    fun addWasteRecord(
        itemName: String,
        category: String,
        quantity: Double,
        unit: String,
        reason: String,
        remarks: String,
        photoUriString: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                val firebaseAuth = auth
                val firebaseStorage = storage
                
                var finalStaffName = "Chef Amit"
                var finalStaffId = "ST-101"
                var finalUserId = "local_uid"
                var finalPhotoUrl = photoUriString

                val user = firebaseAuth?.currentUser
                if (user != null && db != null) {
                    val userDoc = db.collection("users").document(user.uid).get().await()
                    finalStaffName = userDoc.getString("name") ?: "Staff"
                    finalStaffId = userDoc.getString("staffId") ?: "N/A"
                    finalUserId = user.uid

                    if (photoUriString.isNotEmpty() && firebaseStorage != null) {
                        try {
                            val uri = Uri.parse(photoUriString)
                            val storageRef = firebaseStorage.reference.child("wastage_images/${System.currentTimeMillis()}.jpg")
                            storageRef.putFile(uri).await()
                            finalPhotoUrl = storageRef.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            // Fallback to local photo uri if upload fails
                        }
                    }
                }

                val calendar = Calendar.getInstance()
                val newId = db?.collection("kitchen_wastage")?.document()?.id ?: "waste_${System.currentTimeMillis()}"
                
                val record = WasteRecord(
                    id = newId,
                    itemName = itemName,
                    category = category,
                    quantity = quantity,
                    unit = unit,
                    reason = reason,
                    remarks = remarks,
                    photoUri = finalPhotoUrl,
                    timestamp = System.currentTimeMillis(),
                    date = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}",
                    time = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}",
                    day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                    month = (calendar.get(Calendar.MONTH) + 1).toString(),
                    year = calendar.get(Calendar.YEAR).toString(),
                    staffName = finalStaffName,
                    staffId = finalStaffId,
                    userId = finalUserId
                )

                if (db != null) {
                    db.collection("kitchen_wastage").document(record.id).set(record).await()
                } else {
                    val updated = _uiState.value.toMutableList()
                    updated.add(0, record)
                    _uiState.value = updated
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to save wastage record")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            try {
                val db = firestore
                if (db != null) {
                    db.collection("kitchen_wastage").document(id).update("status", status).await()
                } else {
                    val index = _uiState.value.indexOfFirst { it.id == id }
                    if (index != -1) {
                        val updated = _uiState.value.toMutableList()
                        updated[index] = updated[index].copy(status = status)
                        _uiState.value = updated
                    }
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}

class WastageViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WastageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WastageViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
