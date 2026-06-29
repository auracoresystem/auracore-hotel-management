package com.example.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RevenueViewModel : ViewModel() {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val _revenueEntries = MutableStateFlow<List<RevenueEntry>>(emptyList())
    val revenueEntries: StateFlow<List<RevenueEntry>> = _revenueEntries.asStateFlow()

    private val _items = MutableStateFlow<List<InventoryItem>>(emptyList())
    val items: StateFlow<List<InventoryItem>> = _items.asStateFlow()

    private val _purchases = MutableStateFlow<List<StorePurchase>>(emptyList())
    val purchases: StateFlow<List<StorePurchase>> = _purchases.asStateFlow()

    private val _expenses = MutableStateFlow<List<StoreExpense>>(emptyList())
    val expenses: StateFlow<List<StoreExpense>> = _expenses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRevenue()
        loadStoreData()
    }

    private fun loadRevenue() {
        val db = firestore
        if (db == null) return
        _isLoading.value = true
        try {
            db.collection("revenue")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _revenueEntries.value = snapshot.toObjects(RevenueEntry::class.java)
                    }
                    _isLoading.value = false
                }
        } catch (e: Exception) {
            _isLoading.value = false
        }
    }

    private fun loadStoreData() {
        val db = firestore ?: return
        
        try {
            db.collection("inventory_items")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _items.value = snapshot.toObjects(InventoryItem::class.java)
                    }
                }
            
            db.collection("store_purchases")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _purchases.value = snapshot.toObjects(StorePurchase::class.java)
                    }
                }

            db.collection("store_expenses")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _expenses.value = snapshot.toObjects(StoreExpense::class.java)
                    }
                }
        } catch (e: Exception) {
            // Ignore
        }
    }
}

