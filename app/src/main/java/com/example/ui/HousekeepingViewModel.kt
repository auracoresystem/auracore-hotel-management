package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class CleaningTask(
    val id: String = "",
    val roomId: String = "",
    val roomNumber: String = "",
    val status: String = "Dirty", // Dirty, Cleaning, Ready, Inspection, Out Of Order
    val assignedStaffId: String = "",
    val assignedStaffName: String = "",
    val beforePhotoUrl: String = "",
    val afterPhotoUrl: String = "",
    val cleaningTime: Long = 0L,
    val inspectionNotes: String = "",
    val managerApproval: Boolean = false,
    val history: List<String> = emptyList()
)

class HousekeepingViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _tasks = MutableStateFlow<List<CleaningTask>>(emptyList())
    val tasks: StateFlow<List<CleaningTask>> = _tasks.asStateFlow()
    
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTasks()
        loadRooms()
    }

    private fun loadTasks() {
        firestore.collection("cleaning_tasks")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _tasks.value = snapshot.toObjects(CleaningTask::class.java)
                }
            }
    }
    
    private fun loadRooms() {
        firestore.collection("rooms")
            .orderBy("roomNumber")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _rooms.value = snapshot.toObjects(Room::class.java)
                }
            }
    }

    fun updateTaskStatus(
        taskId: String, 
        newStatus: String, 
        beforePhotoUri: String? = null,
        afterPhotoUri: String? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = mutableMapOf<String, Any>("status" to newStatus)
                
                if (beforePhotoUri != null) {
                    val uri = Uri.parse(beforePhotoUri)
                    val ref = storage.reference.child("cleaning/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    updates["beforePhotoUrl"] = ref.downloadUrl.await().toString()
                }
                
                if (afterPhotoUri != null) {
                    val uri = Uri.parse(afterPhotoUri)
                    val ref = storage.reference.child("cleaning/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    updates["afterPhotoUrl"] = ref.downloadUrl.await().toString()
                    updates["cleaningTime"] = System.currentTimeMillis()
                }
                
                if (notes.isNotEmpty()) {
                    updates["inspectionNotes"] = notes
                }
                
                if (newStatus == "Ready") {
                    updates["managerApproval"] = true
                }

                firestore.collection("cleaning_tasks").document(taskId).update(updates).await()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun assignTask(roomId: String, roomNumber: String, staffId: String, staffName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val task = CleaningTask(
                    id = firestore.collection("cleaning_tasks").document().id,
                    roomId = roomId,
                    roomNumber = roomNumber,
                    assignedStaffId = staffId,
                    assignedStaffName = staffName,
                    status = "Dirty",
                    history = listOf("Assigned to $staffName")
                )
                firestore.collection("cleaning_tasks").document(task.id).set(task).await()
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
