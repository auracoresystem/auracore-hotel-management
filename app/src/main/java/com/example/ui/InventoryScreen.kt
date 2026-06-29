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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.MoneyOff
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
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val requirements by viewModel.requirements.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val userRole = when (val state = authState) {
        is AuthState.Authenticated -> state.role
        else -> ""
    }
    
    val currentUser = authViewModel.registeredUsers.collectAsStateWithLifecycle().value.find { 
        (authState as? AuthState.Authenticated)?.name == it.name 
    }
    val isCoreTeam = currentUser?.isCoreTeam ?: false
    val hasApprovalAuthority = currentUser?.hasApprovalAuthority ?: false

    var selectedTab by remember { mutableStateOf(if (userRole == "Kitchen Staff") 1 else 0) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddRequisitionDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var showAddVendorDialog by remember { mutableStateOf(false) }
    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }

    BaseScreen(
        title = "Store Dashboard",
        onBackClick = onBackClick,
        fabAction = {
            if (selectedTab == 0 && userRole != "Kitchen Staff") {
                showAddItemDialog = true
            } else if (selectedTab == 1 && userRole == "Kitchen Staff") {
                showAddRequisitionDialog = true
            } else if (selectedTab == 2) {
                showAddVendorDialog = true
            } else if (selectedTab == 3) {
                showAddPurchaseDialog = true
            } else if (selectedTab == 4) {
                showAddExpenseDialog = true
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

            if (userRole != "Kitchen Staff") {
                val totalStockValue = items.sumOf { it.currentStock * it.unitPrice }
                val totalPurchases = purchases.sumOf { it.totalAmount }
                val totalExpenses = expenses.sumOf { it.amount }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Store Dashboard Overview", fontWeight = FontWeight.Bold, color = RoyalBlue, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StoreMetricItem(title = "Stock Value", value = "₹${totalStockValue}", icon = Icons.Default.Inventory2, color = Color(0xFF4CAF50))
                            StoreMetricItem(title = "Total Purchases", value = "₹${totalPurchases}", icon = Icons.Default.ShoppingCart, color = RoyalBlue)
                            StoreMetricItem(title = "Total Expenses", value = "₹${totalExpenses}", icon = Icons.Default.MoneyOff, color = Color(0xFFF44336))
                        }
                    }
                }
            }

            // Tabs for Stock & Kitchen Requests
            if (userRole != "Kitchen Staff") {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = RoyalBlue,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = RoyalBlue
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalMall, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stock", fontWeight = FontWeight.SemiBold)
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
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vendors", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Purchases", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MoneyOff, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Expenses", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Stock list
                    if (items.isEmpty() && !isLoading) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No inventory items found.", color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(items) { item ->
                                InventoryCard(item = item, onClick = {
                                    selectedItem = item
                                    showTransactionDialog = true
                                })
                            }
                        }
                    }
                }
                1 -> {
                    // Kitchen Requisitions list
                    if (requirements.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No kitchen requirements found.", color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(requirements) { req ->
                                StoreRequisitionCard(
                                    req = req,
                                    userRole = userRole,
                                    hasApprovalAuthority = hasApprovalAuthority,
                                    onIssue = { viewModel.issueRequirement(req.id) },
                                    onApprove = { viewModel.passRequirement(req.id) },
                                    onReject = { viewModel.rejectRequirement(req.id) }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Vendors List
                    if (vendors.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No vendors added.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(vendors) { vendor ->
                                VendorCard(vendor)
                            }
                        }
                    }
                }
                3 -> {
                    // Purchases List
                    if (purchases.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No purchases found.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(purchases) { purchase ->
                                PurchaseCard(purchase)
                            }
                        }
                    }
                }
                4 -> {
                    // Expenses List
                    if (expenses.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No expenses recorded.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(expenses) { expense ->
                                ExpenseCard(expense)
                            }
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

    if (showAddRequisitionDialog) {
        AddRequisitionDialog(
            onDismiss = { showAddRequisitionDialog = false },
            onAdd = { name, qty, unit ->
                val userName = (authState as? AuthState.Authenticated)?.name ?: "Kitchen Staff"
                viewModel.addRequirement(name, qty, unit, userName)
                showAddRequisitionDialog = false
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

    if (showAddVendorDialog) {
        AddVendorDialog(
            onDismiss = { showAddVendorDialog = false },
            onAdd = { name, contact, category ->
                viewModel.addVendor(name, contact, category)
                showAddVendorDialog = false
            }
        )
    }

    if (showAddPurchaseDialog) {
        AddPurchaseDialog(
            onDismiss = { showAddPurchaseDialog = false },
            onAdd = { vName, iName, qty, unit, rate ->
                viewModel.addPurchase(vName, iName, qty, unit, rate)
                showAddPurchaseDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { desc, amount, category ->
                viewModel.addExpense(desc, amount, category)
                showAddExpenseDialog = false
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
                    Text("${item.currentStock} ${item.unit} @ ₹${item.unitPrice}/${item.unit} • ${item.category}", fontSize = 13.sp, color = Color(0xFF64748B))
                    Text("Total Value: ₹${item.currentStock * item.unitPrice}", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.SemiBold)
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
    userRole: String,
    hasApprovalAuthority: Boolean,
    onIssue: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
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
                    if (userRole == "Store" || userRole == "Owner" || userRole == "General Manager") {
                        Button(
                            onClick = onIssue,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Issue Supplies", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chef Approved", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else if (req.status == "Issued") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Issued (Stock Deducted)", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                    }
                } else if (req.status == "Pending Chef Approval") {
                    if (hasApprovalAuthority) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onReject,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PendingActions, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Awaiting Chef Approval", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else if (req.status == "Rejected") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rejected", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddRequisitionDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Supplies", fontWeight = FontWeight.Bold, color = RoyalBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (kg, Ltr, Pcs)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                val q = qty.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && q > 0) {
                    onAdd(name, q, unit)
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) { Text("Request") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
    var unitPrice by remember { mutableStateOf("") }
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
                OutlinedTextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text("Unit Price (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lowThreshold, onValueChange = { lowThreshold = it }, label = { Text("Low Stock Threshold") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode / QR") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                val stock = initialStock.toDoubleOrNull() ?: 0.0
                val threshold = lowThreshold.toDoubleOrNull() ?: 0.0
                val price = unitPrice.toDoubleOrNull() ?: 0.0
                onAdd(InventoryItem(name = name, category = category, unit = unit, currentStock = stock, lowStockThreshold = threshold, unitPrice = price, barcode = barcode)) 
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

@Composable
fun VendorCard(vendor: Vendor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Category: ${vendor.category}", color = Color.Gray, fontSize = 13.sp)
            Text("Contact: ${vendor.contact}", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
fun PurchaseCard(purchase: StorePurchase) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(purchase.date))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(purchase.itemName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("₹${purchase.totalAmount}", fontWeight = FontWeight.Bold, color = RoyalBlue)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Vendor: ${purchase.vendorName}", color = Color.Gray, fontSize = 13.sp)
            Text("${purchase.quantity} ${purchase.unit} @ ₹${purchase.rate}/${purchase.unit}", color = Color.Gray, fontSize = 13.sp)
            Text("Date: $dateStr", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ExpenseCard(expense: StoreExpense) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.date))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Category: ${expense.category}", color = Color.Gray, fontSize = 13.sp)
                Text(dateStr, color = Color.Gray, fontSize = 12.sp)
            }
            Text("₹${expense.amount}", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 16.sp)
        }
    }
}

@Composable
fun AddVendorDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vendor", fontWeight = FontWeight.Bold, color = RoyalBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vendor Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Dairy, Meat)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotBlank()) onAdd(name, contact, category)
            }, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) { Text("Add Vendor") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddPurchaseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, String, Double) -> Unit
) {
    var vendorName by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Purchase", fontWeight = FontWeight.Bold, color = RoyalBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = vendorName, onValueChange = { vendorName = it }, label = { Text("Vendor Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Rate (Per Unit)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                val q = quantity.toDoubleOrNull() ?: 0.0
                val r = rate.toDoubleOrNull() ?: 0.0
                if (itemName.isNotBlank() && vendorName.isNotBlank() && q > 0 && r >= 0) {
                    onAdd(vendorName, itemName, q, unit, r)
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) { Text("Record Purchase") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String) -> Unit
) {
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Expense", fontWeight = FontWeight.Bold, color = RoyalBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Transport, Labor)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (desc.isNotBlank() && amt > 0) {
                    onAdd(desc, amt, category)
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) { Text("Record Expense") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun StoreMetricItem(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        Text(title, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
    }
}
