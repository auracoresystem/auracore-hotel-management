package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.RoyalBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onBackClick: () -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val requirements by viewModel.requirements.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }

    BaseScreen(
        title = "Store Inventory & Requisitions",
        onBackClick = onBackClick,
        fabAction = {
            if (selectedTab == 0) {
                showAddItemDialog = true
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
            }

            // Tabs for Stock & Kitchen Requests
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = RoyalBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RoyalBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalMall, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Store Stock", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val pendingCount = requirements.count { it.status == "Passed (Chef Approved)" }
                            if (pendingCount > 0) {
                                Badge(containerColor = Color.Red, contentColor = Color.White) {
                                    Text("$pendingCount")
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text("Requisitions", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Stock list
                if (items.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No inventory items found.", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items) { item ->
                            InventoryCard(
                                item = item,
                                onClick = {
                                    selectedItem = item
                                    showTransactionDialog = true
                                }
                            )
                        }
                    }
                }
            } else {
                // Kitchen Requisitions list
                if (requirements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No kitchen requirements found.", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requirements) { req ->
                            StoreRequisitionCard(
                                req = req,
                                onIssue = { viewModel.issueRequirement(req.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAdd = { item ->
                viewModel.addItem(item, onSuccess = { showAddItemDialog = false })
            }
        )
    }

    if (showTransactionDialog && selectedItem != null) {
        TransactionDialog(
            item = selectedItem!!,
            onDismiss = { showTransactionDialog = false },
            onSubmit = { type, qty, remarks ->
                viewModel.addTransaction(selectedItem!!.id, type, qty, remarks, onSuccess = { showTransactionDialog = false })
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryCard(item: InventoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    if (item.status == "Low Stock" || item.status == "Out of Stock") Icons.Default.Warning else Icons.Default.ShoppingCart, 
                    contentDescription = null, 
                    tint = if (item.status == "In Stock") Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("${item.currentStock} ${item.unit} • ${item.category}", fontSize = 13.sp, color = Color(0xFF64748B))
                }
            }
            Surface(
                color = (if (item.status == "In Stock") Color(0xFF10B981) else Color(0xFFEF4444)).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = item.status,
                    color = if (item.status == "In Stock") Color(0xFF10B981) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StoreRequisitionCard(
    req: KitchenRequirement,
    onIssue: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(req.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = req.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                
                val chipColor = when (req.status) {
                    "Passed (Chef Approved)" -> Color(0xFF10B981)
                    "Issued" -> Color(0xFF475569)
                    "Rejected" -> Color.Red
                    else -> Color(0xFFF59E0B) // Pending Approval
                }
                Surface(
                    color = chipColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = req.status,
                        color = chipColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Requested Quantity: ${req.quantity} ${req.unit}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
            Text(text = "Kitchen Staff: ${req.requestedBy}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Requested: $dateStr", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                
                if (req.status == "Passed (Chef Approved)") {
                    Button(
                        onClick = onIssue,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Issue Supplies", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (req.status == "Issued") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Issued (Stock Deducted)", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                    }
                } else if (req.status == "Pending Chef Approval") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PendingActions, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Awaiting Chef Approval", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (InventoryItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var initialStock by remember { mutableStateOf("") }
    var lowThreshold by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Store Inventory Item", fontWeight = FontWeight.Bold, color = RoyalBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (kg, Ltr, Pcs, Bag)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = initialStock, onValueChange = { initialStock = it }, label = { Text("Initial Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lowThreshold, onValueChange = { lowThreshold = it }, label = { Text("Low Stock Threshold") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode / QR") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                val stock = initialStock.toDoubleOrNull() ?: 0.0
                val threshold = lowThreshold.toDoubleOrNull() ?: 0.0
                onAdd(InventoryItem(name = name, category = category, unit = unit, currentStock = stock, lowStockThreshold = threshold, barcode = barcode)) 
            }, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) { Text("Add Item") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun TransactionDialog(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onSubmit: (String, Double, String) -> Unit
) {
    var type by remember { mutableStateOf("Stock In") }
    var qty by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock Transaction: ${item.name}", fontWeight = FontWeight.Bold, color = RoyalBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (Stock In, Stock Out)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                val q = qty.toDoubleOrNull()
                if (q != null) onSubmit(type, q, remarks)
            }, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
