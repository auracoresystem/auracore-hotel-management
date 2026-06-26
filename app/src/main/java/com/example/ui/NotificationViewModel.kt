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
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _notifications.value = snapshot.toObjects(NotificationItem::class.java)
                }
            }
    }

    fun markAsRead(id: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users").document(user.uid).collection("notifications").document(id)
                    .update("isRead", true).await()
            } catch (e: Exception) {
            }
        }
    }

    fun deleteNotification(id: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users").document(user.uid).collection("notifications").document(id)
                    .delete().await()
            } catch (e: Exception) {
            }
        }
    }
}
