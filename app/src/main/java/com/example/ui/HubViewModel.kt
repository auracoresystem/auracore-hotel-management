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

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val createdAt: Long = 0L
)

class HubViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        firestore.collection("announcements")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _announcements.value = snapshot.toObjects(Announcement::class.java)
                }
            }
    }

    fun createAnnouncement(title: String, content: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUser
                var authorName = "Core Team"
                if (user != null) {
                    val userDoc = firestore.collection("users").document(user.uid).get().await()
                    authorName = userDoc.getString("name") ?: "Core Team"
                }

                val announcement = Announcement(
                    id = firestore.collection("announcements").document().id,
                    title = title,
                    content = content,
                    authorName = authorName,
                    authorId = user?.uid ?: "",
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("announcements").document(announcement.id).set(announcement).await()
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("announcements").document(id).delete().await()
            } catch (e: Exception) {
            }
        }
    }
}
