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
    val createdAt: Long = 0L,
    val targetAudience: String = "All Staff"
)

class HubViewModel : ViewModel() {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Fallback local list in case Firebase isn't available
    private val localAnnouncements = mutableListOf(
        Announcement(
            id = "demo_1",
            title = "Annual Staff Gala Night",
            content = "We are pleased to announce that our Annual Staff Gala Night will be held on July 15th in the Grand Ballroom. All departments are requested to finalize their attendance sheets.",
            authorName = "General Manager",
            createdAt = System.currentTimeMillis() - 86400000L * 2,
            targetAudience = "All Staff"
        ),
        Announcement(
            id = "demo_2",
            title = "New Kitchen Wastage Policy",
            content = "Effective immediately, all kitchen wastage over 5 kg must be approved by the Department Head. Please log daily reports before 9:00 PM.",
            authorName = "Executive Chef",
            createdAt = System.currentTimeMillis() - 86400000L,
            targetAudience = "All Staff"
        ),
        Announcement(
            id = "demo_3",
            title = "Executive Budget Review",
            content = "Quarterly budget meeting for HODs and Managers in the Boardroom tomorrow at 10 AM.",
            authorName = "Owner",
            createdAt = System.currentTimeMillis(),
            targetAudience = "Core Team"
        )
    )

    init {
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        _announcements.value = localAnnouncements.toList()
        try {
            val db = firestore ?: return
            db.collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _announcements.value = snapshot.toObjects(Announcement::class.java)
                    } else if (error != null) {
                        // Fallback to local
                        _announcements.value = localAnnouncements.toList()
                    }
                }
        } catch (e: Exception) {
            _announcements.value = localAnnouncements.toList()
        }
    }

    fun createAnnouncement(title: String, content: String, targetAudience: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                val firebaseAuth = auth
                var authorName = "Core Team"
                var userId = ""
                
                if (firebaseAuth != null && db != null) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        userId = user.uid
                        val userDoc = db.collection("users").document(user.uid).get().await()
                        authorName = userDoc.getString("name") ?: "Core Team"
                    }
                }

                val announcement = Announcement(
                    id = db?.collection("announcements")?.document()?.id ?: "local_${System.currentTimeMillis()}",
                    title = title,
                    content = content,
                    authorName = authorName,
                    authorId = userId,
                    createdAt = System.currentTimeMillis(),
                    targetAudience = targetAudience
                )

                if (db != null) {
                    db.collection("announcements").document(announcement.id).set(announcement).await()
                } else {
                    // Local fallback
                    localAnnouncements.add(0, announcement)
                    _announcements.value = localAnnouncements.toList()
                }
                onSuccess()
            } catch (e: Exception) {
                // Local fallback on any write error
                val announcement = Announcement(
                    id = "local_${System.currentTimeMillis()}",
                    title = title,
                    content = content,
                    authorName = "Core Team",
                    createdAt = System.currentTimeMillis(),
                    targetAudience = targetAudience
                )
                localAnnouncements.add(0, announcement)
                _announcements.value = localAnnouncements.toList()
                onSuccess()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            try {
                val db = firestore
                if (db != null) {
                    db.collection("announcements").document(id).delete().await()
                } else {
                    localAnnouncements.removeAll { it.id == id }
                    _announcements.value = localAnnouncements.toList()
                }
            } catch (e: Exception) {
                localAnnouncements.removeAll { it.id == id }
                _announcements.value = localAnnouncements.toList()
            }
        }
    }
}
