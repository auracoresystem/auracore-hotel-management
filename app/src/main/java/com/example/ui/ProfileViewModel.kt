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
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val _profile = MutableStateFlow<UserProfile?>(
        UserProfile(
            uid = "demo_user",
            name = "AuraCore Demo Admin",
            email = "demo@auracore.com",
            phone = "+1 (555) 019-2834",
            department = "Administration",
            role = "Owner",
            employeeId = "AC-9901"
        )
    )
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
                val user = auth?.currentUser
                val db = firestore
                if (user != null && db != null) {
                    val doc = db.collection("users").document(user.uid).get().await()
                    if (doc.exists()) {
                        _profile.value = doc.toObject(UserProfile::class.java)
                    } else {
                        // Create basic profile if it doesn't exist
                        val basicProfile = UserProfile(
                            uid = user.uid,
                            name = user.displayName ?: "User",
                            email = user.email ?: "",
                            role = "Owner"
                        )
                        db.collection("users").document(user.uid).set(basicProfile).await()
                        _profile.value = basicProfile
                    }
                }
            } catch (e: Exception) {
                // Keep default demo profile on error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, phone: String, department: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth?.currentUser
                val db = firestore
                if (user != null && db != null) {
                    db.collection("users").document(user.uid).update(
                        mapOf(
                            "name" to name,
                            "phone" to phone,
                            "department" to department
                        )
                    ).await()
                    loadProfile()
                } else {
                    // Local fallback
                    _profile.value = _profile.value?.copy(
                        name = name,
                        phone = phone,
                        department = department
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                // Local fallback on error
                _profile.value = _profile.value?.copy(
                    name = name,
                    phone = phone,
                    department = department
                )
                onSuccess()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
