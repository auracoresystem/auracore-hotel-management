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

data class KitchenRequirement(
    val id: String = "",
    val itemName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val status: String = "Pending Chef Approval", // Pending Chef Approval, Passed (Chef Approved), Issued, Rejected
    val requestedBy: String = "",
    val date: Long = 0L,
    val itemId: String = ""
)

class InventoryViewModel : ViewModel() {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val _items = MutableStateFlow<List<InventoryItem>>(emptyList())
    val items: StateFlow<List<InventoryItem>> = _items.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<InventoryTransaction>>(emptyList())
    val transactions: StateFlow<List<InventoryTransaction>> = _transactions.asStateFlow()

    private val _requirements = MutableStateFlow<List<KitchenRequirement>>(emptyList())
    val requirements: StateFlow<List<KitchenRequirement>> = _requirements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val localItems = mutableListOf(
        InventoryItem(id = "item_1", name = "Basmati Rice", category = "Grains", unit = "kg", currentStock = 150.0, lowStockThreshold = 50.0, barcode = "11223344"),
        InventoryItem(id = "item_2", name = "Cooking Oil", category = "Liquids", unit = "Ltr", currentStock = 8.0, lowStockThreshold = 15.0, barcode = "22334455", status = "Low Stock"),
        InventoryItem(id = "item_3", name = "Salt", category = "Spices", unit = "kg", currentStock = 12.0, lowStockThreshold = 5.0, barcode = "33445566"),
        InventoryItem(id = "item_4", name = "Tea Bags", category = "Beverages", unit = "Pcs", currentStock = 0.0, lowStockThreshold = 100.0, barcode = "44556677", status = "Out of Stock")
    )

    private val localTransactions = mutableListOf(
        InventoryTransaction(id = "trans_1", itemId = "item_1", type = "Stock In", quantity = 100.0, date = System.currentTimeMillis() - 86400000, remarks = "Bulk Purchase"),
        InventoryTransaction(id = "trans_2", itemId = "item_2", type = "Stock Out", quantity = 2.0, date = System.currentTimeMillis() - 43200000, remarks = "Issued to Kitchen")
    )

    private val localRequirements = mutableListOf(
        KitchenRequirement(
            id = "req_1",
            itemName = "Basmati Rice",
            quantity = 25.0,
            unit = "kg",
            status = "Pending Chef Approval",
            requestedBy = "Chef Amit",
            date = System.currentTimeMillis() - 3600000,
            itemId = "item_1"
        ),
        KitchenRequirement(
            id = "req_2",
            itemName = "Cooking Oil",
            quantity = 10.0,
            unit = "Ltr",
            status = "Passed (Chef Approved)",
            requestedBy = "Chef Amit",
            date = System.currentTimeMillis() - 7200000,
            itemId = "item_2"
        )
    )

    init {
        loadItems()
        loadTransactions()
        loadRequirements()
    }

    private fun loadRequirements() {
        _requirements.value = localRequirements.toList()
        val db = firestore
        if (db == null) return
        try {
            db.collection("kitchen_requirements")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _requirements.value = snapshot.toObjects(KitchenRequirement::class.java)
                    } else {
                        _requirements.value = localRequirements.toList()
                    }
                }
        } catch (e: Exception) {
            _requirements.value = localRequirements.toList()
        }
    }

    private fun loadItems() {
        _items.value = localItems.toList()
        val db = firestore
        if (db == null) return
        try {
            db.collection("inventory_items")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val itemsList = snapshot.toObjects(InventoryItem::class.java)
                        _items.value = itemsList
                    } else {
                        _items.value = localItems.toList()
                    }
                }
        } catch (e: Exception) {
            _items.value = localItems.toList()
        }
    }
    
    private fun loadTransactions() {
        _transactions.value = localTransactions.toList()
        val db = firestore
        if (db == null) return
        try {
            db.collection("inventory_transactions")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _transactions.value = snapshot.toObjects(InventoryTransaction::class.java)
                    } else {
                        _transactions.value = localTransactions.toList()
                    }
                }
        } catch (e: Exception) {
            _transactions.value = localTransactions.toList()
        }
    }

    fun addItem(item: InventoryItem, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                val newId = db?.collection("inventory_items")?.document()?.id ?: "item_${System.currentTimeMillis()}"
                val newItem = item.copy(
                    id = newId,
                    status = if (item.currentStock <= 0) "Out of Stock" else if (item.currentStock <= item.lowStockThreshold) "Low Stock" else "In Stock"
                )
                
                if (db != null) {
                    db.collection("inventory_items").document(newItem.id).set(newItem).await()
                    if (newItem.currentStock > 0) {
                        val trans = InventoryTransaction(
                            id = db.collection("inventory_transactions").document().id,
                            itemId = newItem.id,
                            type = "Stock In",
                            quantity = newItem.currentStock,
                            date = System.currentTimeMillis(),
                            remarks = "Initial Stock"
                        )
                        db.collection("inventory_transactions").document(trans.id).set(trans).await()
                    }
                } else {
                    localItems.add(newItem)
                    _items.value = localItems.toList()
                    if (newItem.currentStock > 0) {
                        val trans = InventoryTransaction(
                            id = "trans_${System.currentTimeMillis()}",
                            itemId = newItem.id,
                            type = "Stock In",
                            quantity = newItem.currentStock,
                            date = System.currentTimeMillis(),
                            remarks = "Initial Stock"
                        )
                        localTransactions.add(0, trans)
                        _transactions.value = localTransactions.toList()
                    }
                }
                
                ToastManager.showToast("Inventory Item Added Successfully! 🛍️")
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
                val db = firestore
                if (db != null) {
                    val itemRef = db.collection("inventory_items").document(itemId)
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
                             id = db.collection("inventory_transactions").document().id,
                             itemId = itemId,
                             type = type,
                             quantity = quantity,
                             date = System.currentTimeMillis(),
                             remarks = remarks
                         )
                         db.collection("inventory_transactions").document(trans.id).set(trans).await()
                         
                         ToastManager.showToast("Stock updated: ${type}! 📈")
                         onSuccess()
                    }
                } else {
                    val itemIndex = localItems.indexOfFirst { it.id == itemId }
                    if (itemIndex != -1) {
                        val item = localItems[itemIndex]
                        val newStock = if (type == "Stock In") item.currentStock + quantity else item.currentStock - quantity
                        val newStatus = if (newStock <= 0) "Out of Stock" else if (newStock <= item.lowStockThreshold) "Low Stock" else "In Stock"
                        
                        localItems[itemIndex] = item.copy(currentStock = newStock, status = newStatus)
                        _items.value = localItems.toList()
                        
                        val trans = InventoryTransaction(
                             id = "trans_${System.currentTimeMillis()}",
                             itemId = itemId,
                             type = type,
                             quantity = quantity,
                             date = System.currentTimeMillis(),
                             remarks = remarks
                        )
                        localTransactions.add(0, trans)
                        _transactions.value = localTransactions.toList()
                        
                        ToastManager.showToast("Stock updated: ${type}! 📈")
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addRequirement(itemName: String, quantity: Double, unit: String, requestedBy: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val matchedItem = _items.value.firstOrNull { it.name.equals(itemName, ignoreCase = true) }
                val db = firestore
                val newId = db?.collection("kitchen_requirements")?.document()?.id ?: "req_${System.currentTimeMillis()}"
                val req = KitchenRequirement(
                    id = newId,
                    itemName = itemName,
                    quantity = quantity,
                    unit = unit,
                    status = "Pending Chef Approval",
                    requestedBy = requestedBy,
                    date = System.currentTimeMillis(),
                    itemId = matchedItem?.id ?: ""
                )
                
                if (db != null) {
                    try {
                        db.collection("kitchen_requirements").document(newId).set(req).await()
                    } catch (e: Exception) {
                        localRequirements.add(0, req)
                        _requirements.value = localRequirements.toList()
                    }
                } else {
                    localRequirements.add(0, req)
                    _requirements.value = localRequirements.toList()
                }
                ToastManager.showToast("Requirement Submitted for Chef Approval! 🧑‍🍳")
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun passRequirement(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                if (db != null) {
                    try {
                        db.collection("kitchen_requirements").document(id)
                            .update("status", "Passed (Chef Approved)").await()
                    } catch (e: Exception) {
                        val index = localRequirements.indexOfFirst { it.id == id }
                        if (index != -1) {
                            localRequirements[index] = localRequirements[index].copy(status = "Passed (Chef Approved)")
                            _requirements.value = localRequirements.toList()
                        }
                    }
                } else {
                    val index = localRequirements.indexOfFirst { it.id == id }
                    if (index != -1) {
                        localRequirements[index] = localRequirements[index].copy(status = "Passed (Chef Approved)")
                        _requirements.value = localRequirements.toList()
                    }
                }
                ToastManager.showToast("Requirement Passed & Approved by Chef! ✅")
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectRequirement(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                if (db != null) {
                    try {
                        db.collection("kitchen_requirements").document(id)
                            .update("status", "Rejected").await()
                    } catch (e: Exception) {
                        val index = localRequirements.indexOfFirst { it.id == id }
                        if (index != -1) {
                            localRequirements[index] = localRequirements[index].copy(status = "Rejected")
                            _requirements.value = localRequirements.toList()
                        }
                    }
                } else {
                    val index = localRequirements.indexOfFirst { it.id == id }
                    if (index != -1) {
                        localRequirements[index] = localRequirements[index].copy(status = "Rejected")
                        _requirements.value = localRequirements.toList()
                    }
                }
                ToastManager.showToast("Requirement Rejected! ❌")
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun issueRequirement(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                var req: KitchenRequirement? = null
                if (db != null) {
                    try {
                        val reqSnap = db.collection("kitchen_requirements").document(id).get().await()
                        req = reqSnap.toObject(KitchenRequirement::class.java)
                    } catch (e: Exception) {
                        req = localRequirements.firstOrNull { it.id == id }
                    }
                } else {
                    req = localRequirements.firstOrNull { it.id == id }
                }

                if (req != null) {
                    val finalItemName = req.itemName
                    val qtyToDeduct = req.quantity
                    
                    val matchedItem = _items.value.firstOrNull { 
                        it.id == req.itemId || it.name.equals(finalItemName, ignoreCase = true) 
                    }

                    if (matchedItem != null) {
                        val newStock = matchedItem.currentStock - qtyToDeduct
                        val newStatus = if (newStock <= 0) "Out of Stock" else if (newStock <= matchedItem.lowStockThreshold) "Low Stock" else "In Stock"
                        
                        if (db != null) {
                            try {
                                db.collection("inventory_items").document(matchedItem.id).update(
                                    mapOf(
                                        "currentStock" to newStock,
                                        "status" to newStatus
                                    )
                                ).await()
                                
                                val trans = InventoryTransaction(
                                     id = db.collection("inventory_transactions").document().id,
                                     itemId = matchedItem.id,
                                     type = "Stock Out",
                                     quantity = qtyToDeduct,
                                     date = System.currentTimeMillis(),
                                     remarks = "Issued to Kitchen (Req: ${req.id})"
                                 )
                                 db.collection("inventory_transactions").document(trans.id).set(trans).await()
                            } catch (e: Exception) {
                                val itemIndex = _items.value.indexOfFirst { it.id == matchedItem.id }
                                if (itemIndex != -1) {
                                    val updatedList = _items.value.toMutableList()
                                    updatedList[itemIndex] = matchedItem.copy(currentStock = newStock, status = newStatus)
                                    _items.value = updatedList
                                }
                            }
                        } else {
                            val itemIndex = localItems.indexOfFirst { it.id == matchedItem.id }
                            if (itemIndex != -1) {
                                localItems[itemIndex] = matchedItem.copy(currentStock = newStock, status = newStatus)
                                _items.value = localItems.toList()
                            }
                            
                            val trans = InventoryTransaction(
                                 id = "trans_${System.currentTimeMillis()}",
                                 itemId = matchedItem.id,
                                 type = "Stock Out",
                                 quantity = qtyToDeduct,
                                 date = System.currentTimeMillis(),
                                 remarks = "Issued to Kitchen (Req: ${req.id})"
                             )
                             localTransactions.add(0, trans)
                             _transactions.value = localTransactions.toList()
                        }
                    }

                    if (db != null) {
                        try {
                            db.collection("kitchen_requirements").document(id)
                                .update("status", "Issued").await()
                        } catch (e: Exception) {
                            val index = localRequirements.indexOfFirst { it.id == id }
                            if (index != -1) {
                                localRequirements[index] = localRequirements[index].copy(status = "Issued")
                                _requirements.value = localRequirements.toList()
                            }
                        }
                    } else {
                        val index = localRequirements.indexOfFirst { it.id == id }
                        if (index != -1) {
                            localRequirements[index] = localRequirements[index].copy(status = "Issued")
                            _requirements.value = localRequirements.toList()
                        }
                    }

                    ToastManager.showToast("Stock Issued! -${qtyToDeduct} ${req.unit} automatically deducted! 📦")
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
