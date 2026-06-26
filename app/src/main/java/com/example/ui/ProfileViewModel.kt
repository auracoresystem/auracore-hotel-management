package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val role: String = "",
    val employeeId: String = ""
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUser
                if (user != null) {
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    if (doc.exists()) {
                        _profile.value = doc.toObject(UserProfile::class.java)
                    } else {
                        // Create basic profile if it doesn't exist
                        val basicProfile = UserProfile(
                            uid = user.uid,
                            name = user.displayName ?: "User",
                            email = user.email ?: "",
                            role = "Staff"
                        )
                        firestore.collection("users").document(user.uid).set(basicProfile).await()
                        _profile.value = basicProfile
                    }
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, phone: String, department: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUser
                if (user != null) {
                    firestore.collection("users").document(user.uid).update(
                        mapOf(
                            "name" to name,
                            "phone" to phone,
                            "department" to department
                        )
                    ).await()
                    loadProfile()
                    onSuccess()
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
