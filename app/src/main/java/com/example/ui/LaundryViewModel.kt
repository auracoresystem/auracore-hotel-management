package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class LaundryTicket(
    val id: String = "",
    val ticketNumber: String = "",
    val type: String = "Linen", // Linen, Uniform, Guest
    val receivedDate: Long = 0L,
    val washStatus: String = "Pending", // Pending, Washing, Ironing, Ready, Delivered
    val ironStatus: String = "Pending",
    val deliveryStatus: String = "Pending",
    val quantity: Int = 0,
    val returnedQuantity: Int = 0,
    val damagedItems: Int = 0,
    val damagePhotoUrl: String = "",
    val notes: String = "",
    val staffId: String = "",
    val staffName: String = ""
)

class LaundryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _tickets = MutableStateFlow<List<LaundryTicket>>(emptyList())
    val tickets: StateFlow<List<LaundryTicket>> = _tickets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTickets()
    }

    private fun loadTickets() {
        firestore.collection("laundry_tickets")
            .orderBy("receivedDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _tickets.value = snapshot.toObjects(LaundryTicket::class.java)
                }
            }
    }

    fun createTicket(type: String, qty: Int, staffName: String, damagePhotoUri: String?, notes: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var photoUrl = ""
                if (damagePhotoUri != null) {
                    val uri = Uri.parse(damagePhotoUri)
                    val ref = storage.reference.child("laundry/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                val ticket = LaundryTicket(
                    id = firestore.collection("laundry_tickets").document().id,
                    ticketNumber = "LND-${System.currentTimeMillis()}",
                    type = type,
                    quantity = qty,
                    staffName = staffName,
                    notes = notes,
                    damagePhotoUrl = photoUrl,
                    receivedDate = System.currentTimeMillis()
                )

                firestore.collection("laundry_tickets").document(ticket.id).set(ticket).await()
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateTicketStatus(ticketId: String, wash: String, iron: String, delivery: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestore.collection("laundry_tickets").document(ticketId).update(
                    mapOf(
                        "washStatus" to wash,
                        "ironStatus" to iron,
                        "deliveryStatus" to delivery
                    )
                ).await()
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
