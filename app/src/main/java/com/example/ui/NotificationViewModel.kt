package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val type: String = "General"
)

class NotificationViewModel : ViewModel() {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val localNotifications = mutableListOf(
        NotificationItem(
            id = "notif_1",
            title = "Task Assigned",
            message = "You have been assigned to review the monthly laundry report.",
            timestamp = System.currentTimeMillis() - 3600000L,
            isRead = false,
            type = "Task"
        ),
        NotificationItem(
            id = "notif_2",
            title = "Low Stock Alert",
            message = "Kitchen department reported low levels of organic cooking oil.",
            timestamp = System.currentTimeMillis() - 14400000L,
            isRead = false,
            type = "Alert"
        ),
        NotificationItem(
            id = "notif_3",
            title = "Meeting Scheduled",
            message = "Core Team sync meeting scheduled for today at 3:00 PM.",
            timestamp = System.currentTimeMillis() - 86400000L,
            isRead = true,
            type = "Meeting"
        )
    )

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        _notifications.value = localNotifications.toList()
        try {
            val user = auth?.currentUser
            val db = firestore
            if (user != null && db != null) {
                db.collection("users").document(user.uid).collection("notifications")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null && !snapshot.isEmpty) {
                            _notifications.value = snapshot.toObjects(NotificationItem::class.java)
                        } else if (error != null) {
                            _notifications.value = localNotifications.toList()
                        }
                    }
            }
        } catch (e: Exception) {
            _notifications.value = localNotifications.toList()
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                val user = auth?.currentUser
                val db = firestore
                if (user != null && db != null) {
                    db.collection("users").document(user.uid).collection("notifications").document(id)
                        .update("isRead", true).await()
                } else {
                    val index = localNotifications.indexOfFirst { it.id == id }
                    if (index != -1) {
                        localNotifications[index] = localNotifications[index].copy(isRead = true)
                        _notifications.value = localNotifications.toList()
                    }
                }
            } catch (e: Exception) {
                val index = localNotifications.indexOfFirst { it.id == id }
                if (index != -1) {
                    localNotifications[index] = localNotifications[index].copy(isRead = true)
                    _notifications.value = localNotifications.toList()
                }
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                val user = auth?.currentUser
                val db = firestore
                if (user != null && db != null) {
                    db.collection("users").document(user.uid).collection("notifications").document(id)
                        .delete().await()
                } else {
                    localNotifications.removeAll { it.id == id }
                    _notifications.value = localNotifications.toList()
                }
            } catch (e: Exception) {
                localNotifications.removeAll { it.id == id }
                _notifications.value = localNotifications.toList()
            }
        }
    }
}
