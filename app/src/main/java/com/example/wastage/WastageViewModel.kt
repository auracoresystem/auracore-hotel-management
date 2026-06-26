package com.example.wastage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WastageViewModel(private val repository: WasteRepository) : ViewModel() {
    val uiState: StateFlow<List<WasteRecord>> = repository.allWasteRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addWasteRecord(
        itemName: String,
        category: String,
        quantity: Double,
        unit: String,
        reason: String,
        remarks: String,
        photoUri: String?
    ) {
        viewModelScope.launch {
            val record = WasteRecord(
                itemName = itemName,
                category = category,
                quantity = quantity,
                unit = unit,
                reason = reason,
                remarks = remarks,
                photoUri = photoUri
            )
            repository.insert(record)
        }
    }

    fun updateStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
        }
    }
}

class WastageViewModelFactory(private val repository: WasteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WastageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WastageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
