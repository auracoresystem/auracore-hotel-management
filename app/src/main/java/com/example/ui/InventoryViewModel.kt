package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val barcode: String = "",
    val unit: String = "",
    val currentStock: Double = 0.0,
    val lowStockThreshold: Double = 0.0,
    val supplier: String = "",
    val purchaseDate: Long = 0L,
    val expiryDate: Long = 0L,
    val status: String = "In Stock" // Low Stock, Out of Stock
)

data class InventoryTransaction(
    val id: String = "",
    val itemId: String = "",
    val type: String = "Stock In", // Stock In, Stock Out
    val quantity: Double = 0.0,
    val date: Long = 0L,
    val remarks: String = ""
)

class InventoryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _items = MutableStateFlow<List<InventoryItem>>(emptyList())
    val items: StateFlow<List<InventoryItem>> = _items.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<InventoryTransaction>>(emptyList())
    val transactions: StateFlow<List<InventoryTransaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadItems()
        loadTransactions()
    }

    private fun loadItems() {
        firestore.collection("inventory_items")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val itemsList = snapshot.toObjects(InventoryItem::class.java)
                    _items.value = itemsList
                    
                    // Check for low stock alerts (could update dashboard or local state)
                }
            }
    }
    
    private fun loadTransactions() {
        firestore.collection("inventory_transactions")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _transactions.value = snapshot.toObjects(InventoryTransaction::class.java)
                }
            }
    }

    fun addItem(item: InventoryItem, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newItem = item.copy(
                    id = firestore.collection("inventory_items").document().id,
                    status = if (item.currentStock <= 0) "Out of Stock" else if (item.currentStock <= item.lowStockThreshold) "Low Stock" else "In Stock"
                )
                firestore.collection("inventory_items").document(newItem.id).set(newItem).await()
                
                if (newItem.currentStock > 0) {
                     val trans = InventoryTransaction(
                         id = firestore.collection("inventory_transactions").document().id,
                         itemId = newItem.id,
                         type = "Stock In",
                         quantity = newItem.currentStock,
                         date = System.currentTimeMillis(),
                         remarks = "Initial Stock"
                     )
                     firestore.collection("inventory_transactions").document(trans.id).set(trans).await()
                }
                
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTransaction(itemId: String, type: String, quantity: Double, remarks: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val itemRef = firestore.collection("inventory_items").document(itemId)
                val itemSnapshot = itemRef.get().await()
                val item = itemSnapshot.toObject(InventoryItem::class.java)
                
                if (item != null) {
                    val newStock = if (type == "Stock In") item.currentStock + quantity else item.currentStock - quantity
                    val newStatus = if (newStock <= 0) "Out of Stock" else if (newStock <= item.lowStockThreshold) "Low Stock" else "In Stock"
                    
                    itemRef.update(
                        mapOf(
                            "currentStock" to newStock,
                            "status" to newStatus
                        )
                    ).await()
                    
                    val trans = InventoryTransaction(
                         id = firestore.collection("inventory_transactions").document().id,
                         itemId = itemId,
                         type = type,
                         quantity = quantity,
                         date = System.currentTimeMillis(),
                         remarks = remarks
                     )
                     firestore.collection("inventory_transactions").document(trans.id).set(trans).await()
                     
                     onSuccess()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
