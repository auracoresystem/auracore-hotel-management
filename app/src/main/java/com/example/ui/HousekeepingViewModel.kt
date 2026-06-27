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
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (e: Exception) { null }
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _tasks = MutableStateFlow<List<CleaningTask>>(emptyList())
    val tasks: StateFlow<List<CleaningTask>> = _tasks.asStateFlow()
    
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val offlineTasks = listOf(
        CleaningTask(id = "task_h_1", roomId = "room_101", roomNumber = "101", status = "Dirty", assignedStaffName = "Sunita Verma"),
        CleaningTask(id = "task_h_2", roomId = "room_104", roomNumber = "104", status = "Ready", assignedStaffName = "Sunita Verma")
    )
    private val offlineRooms = listOf(
        Room(id = "room_101", roomNumber = "101", status = "Available"),
        Room(id = "room_102", roomNumber = "102", status = "Occupied", currentGuestId = "guest_aarav_sharma"),
        Room(id = "room_103", roomNumber = "103", status = "Maintenance"),
        Room(id = "room_104", roomNumber = "104", status = "Available"),
        Room(id = "room_201", roomNumber = "201", status = "Available")
    )

    init {
        loadTasks()
        loadRooms()
    }

    private fun loadTasks() {
        _tasks.value = offlineTasks
        val db = firestore ?: return
        try {
            db.collection("cleaning_tasks")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _tasks.value = snapshot.toObjects(CleaningTask::class.java)
                    } else {
                        _tasks.value = offlineTasks
                    }
                }
        } catch (e: Exception) {
            _tasks.value = offlineTasks
        }
    }
    
    private fun loadRooms() {
        _rooms.value = offlineRooms
        val db = firestore ?: return
        try {
            db.collection("rooms")
                .orderBy("roomNumber")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _rooms.value = snapshot.toObjects(Room::class.java)
                    } else {
                        _rooms.value = offlineRooms
                    }
                }
        } catch (e: Exception) {
            _rooms.value = offlineRooms
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
                val firebaseStorage = storage
                
                if (beforePhotoUri != null && firebaseStorage != null) {
                    val uri = Uri.parse(beforePhotoUri)
                    val ref = firebaseStorage.reference.child("cleaning/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    updates["beforePhotoUrl"] = ref.downloadUrl.await().toString()
                }
                
                if (afterPhotoUri != null && firebaseStorage != null) {
                    val uri = Uri.parse(afterPhotoUri)
                    val ref = firebaseStorage.reference.child("cleaning/${UUID.randomUUID()}.jpg")
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

                val db = firestore
                if (db != null) {
                    db.collection("cleaning_tasks").document(taskId).update(updates).await()
                } else {
                    val index = _tasks.value.indexOfFirst { it.id == taskId }
                    if (index != -1) {
                        val updated = _tasks.value.toMutableList()
                        var task = updated[index]
                        if (updates.containsKey("beforePhotoUrl")) task = task.copy(beforePhotoUrl = updates["beforePhotoUrl"] as String)
                        if (updates.containsKey("afterPhotoUrl")) task = task.copy(afterPhotoUrl = updates["afterPhotoUrl"] as String, cleaningTime = System.currentTimeMillis())
                        if (updates.containsKey("inspectionNotes")) task = task.copy(inspectionNotes = notes)
                        if (updates.containsKey("managerApproval")) task = task.copy(managerApproval = true)
                        task = task.copy(status = newStatus)
                        updated[index] = task
                        _tasks.value = updated
                    }
                }
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
                val db = firestore
                val newId = db?.collection("cleaning_tasks")?.document()?.id ?: "task_${System.currentTimeMillis()}"
                val task = CleaningTask(
                    id = newId,
                    roomId = roomId,
                    roomNumber = roomNumber,
                    assignedStaffId = staffId,
                    assignedStaffName = staffName,
                    status = "Dirty",
                    history = listOf("Assigned to $staffName")
                )
                if (db != null) {
                    db.collection("cleaning_tasks").document(task.id).set(task).await()
                } else {
                    val updated = _tasks.value.toMutableList()
                    updated.add(0, task)
                    _tasks.value = updated
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
