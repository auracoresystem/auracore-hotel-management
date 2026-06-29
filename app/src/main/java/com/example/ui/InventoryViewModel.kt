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
    val unitPrice: Double = 0.0,
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

data class Vendor(
    val id: String = "",
    val name: String = "",
    val contact: String = "",
    val category: String = ""
)

data class StorePurchase(
    val id: String = "",
    val vendorName: String = "",
    val itemName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val rate: Double = 0.0,
    val totalAmount: Double = 0.0,
    val date: Long = 0L
)

data class StoreExpense(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: Long = 0L,
    val category: String = "Misc"
)

class InventoryViewModel : ViewModel() {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val _items = MutableStateFlow<List<InventoryItem>>(emptyList())
    val items: StateFlow<List<InventoryItem>> = _items.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<InventoryTransaction>>(emptyList())
    val transactions: StateFlow<List<InventoryTransaction>> = _transactions.asStateFlow()

    private val _requirements = MutableStateFlow<List<KitchenRequirement>>(emptyList())
    val requirements: StateFlow<List<KitchenRequirement>> = _requirements.asStateFlow()

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors: StateFlow<List<Vendor>> = _vendors.asStateFlow()

    private val _purchases = MutableStateFlow<List<StorePurchase>>(emptyList())
    val purchases: StateFlow<List<StorePurchase>> = _purchases.asStateFlow()

    private val _expenses = MutableStateFlow<List<StoreExpense>>(emptyList())
    val expenses: StateFlow<List<StoreExpense>> = _expenses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadItems()
        loadTransactions()
        loadRequirements()
        loadVendors()
        loadPurchases()
        loadExpenses()
    }

    private fun loadRequirements() {
        val db = firestore
        if (db == null) return
        try {
            db.collection("kitchen_requirements")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _requirements.value = snapshot.toObjects(KitchenRequirement::class.java)
                    }
                }
        } catch (e: Exception) {
            // Log or ignore
        }
    }

    private fun loadItems() {
        val db = firestore
        if (db == null) return
        try {
            db.collection("inventory_items")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _items.value = snapshot.toObjects(InventoryItem::class.java)
                    }
                }
        } catch (e: Exception) {
        }
    }
    
    private fun loadTransactions() {
        val db = firestore
        if (db == null) return
        try {
            db.collection("inventory_transactions")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _transactions.value = snapshot.toObjects(InventoryTransaction::class.java)
                    }
                }
        } catch (e: Exception) {
        }
    }

    private fun loadVendors() {
        val db = firestore
        if (db == null) return
        try {
            db.collection("vendors")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _vendors.value = snapshot.toObjects(Vendor::class.java)
                    }
                }
        } catch (e: Exception) {
        }
    }

    private fun loadPurchases() {
        val db = firestore
        if (db == null) return
        try {
            db.collection("store_purchases")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _purchases.value = snapshot.toObjects(StorePurchase::class.java)
                    }
                }
        } catch (e: Exception) {
        }
    }

    private fun loadExpenses() {
        val db = firestore
        if (db == null) return
        try {
            db.collection("store_expenses")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _expenses.value = snapshot.toObjects(StoreExpense::class.java)
                    }
                }
        } catch (e: Exception) {
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
                    }
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
                    }
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
                            }
                        }
                    }

                    if (db != null) {
                        try {
                            db.collection("kitchen_requirements").document(id)
                                .update("status", "Issued").await()
                        } catch (e: Exception) {
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
    
    fun addVendor(name: String, contact: String, category: String) {
        viewModelScope.launch {
            val db = firestore ?: return@launch
            val newVendor = Vendor(id = db.collection("vendors").document().id, name = name, contact = contact, category = category)
            try {
                db.collection("vendors").document(newVendor.id).set(newVendor).await()
                ToastManager.showToast("Vendor Added! ✅")
            } catch(e: Exception) {
            }
        }
    }

    fun addPurchase(vendorName: String, itemName: String, quantity: Double, unit: String, rate: Double) {
        viewModelScope.launch {
            val db = firestore ?: return@launch
            val total = quantity * rate
            val newPurchase = StorePurchase(
                id = db.collection("store_purchases").document().id,
                vendorName = vendorName,
                itemName = itemName,
                quantity = quantity,
                unit = unit,
                rate = rate,
                totalAmount = total,
                date = System.currentTimeMillis()
            )
            try {
                db.collection("store_purchases").document(newPurchase.id).set(newPurchase).await()
                ToastManager.showToast("Purchase Recorded! ✅")
            } catch (e: Exception) {
            }
        }
    }

    fun addExpense(description: String, amount: Double, category: String) {
        viewModelScope.launch {
            val db = firestore ?: return@launch
            val newExpense = StoreExpense(
                id = db.collection("store_expenses").document().id,
                description = description,
                amount = amount,
                category = category,
                date = System.currentTimeMillis()
            )
            try {
                db.collection("store_expenses").document(newExpense.id).set(newExpense).await()
                ToastManager.showToast("Expense Recorded! ✅")
            } catch (e: Exception) {
            }
        }
    }
}
