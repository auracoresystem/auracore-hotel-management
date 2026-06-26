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
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<List<WasteRecord>>(emptyList())
    val uiState: StateFlow<List<WasteRecord>> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWasteRecords()
    }

    private fun loadWasteRecords() {
        firestore.collection("kitchen_wastage")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val records = snapshot.toObjects(WasteRecord::class.java)
                    _uiState.value = records
                }
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
                val user = auth.currentUser
                if (user == null) {
                    onError("User not authenticated.")
                    _isLoading.value = false
                    return@launch
                }
                
                val userDoc = firestore.collection("users").document(user.uid).get().await()
                val staffName = userDoc.getString("name") ?: "Staff"
                val staffId = userDoc.getString("staffId") ?: "N/A"

                val uri = Uri.parse(photoUriString)
                val storageRef = storage.reference.child("wastage_images/${System.currentTimeMillis()}.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                val calendar = Calendar.getInstance()
                
                val record = WasteRecord(
                    id = firestore.collection("kitchen_wastage").document().id,
                    itemName = itemName,
                    category = category,
                    quantity = quantity,
                    unit = unit,
                    reason = reason,
                    remarks = remarks,
                    photoUri = downloadUrl,
                    timestamp = System.currentTimeMillis(),
                    date = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}",
                    time = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}",
                    day = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                    month = (calendar.get(Calendar.MONTH) + 1).toString(),
                    year = calendar.get(Calendar.YEAR).toString(),
                    staffName = staffName,
                    staffId = staffId,
                    userId = user.uid
                )

                firestore.collection("kitchen_wastage").document(record.id).set(record).await()
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
                firestore.collection("kitchen_wastage").document(id).update("status", status).await()
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
