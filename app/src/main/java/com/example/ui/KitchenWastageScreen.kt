package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.wastage.WasteRecord
import com.example.wastage.WastageViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenWastageScreen(
    viewModel: WastageViewModel,
    inventoryViewModel: InventoryViewModel,
    userRole: String = "Kitchen Staff",
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val records by viewModel.uiState.collectAsStateWithLifecycle()
    val requirements by inventoryViewModel.requirements.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddReqDialog by remember { mutableStateOf(false) }

    // Chef/HOD access check to "pass" requirements
    val isChefOrHOD = userRole == "Kitchen Staff" || 
                      userRole == "Owner" || 
                      userRole == "General Manager" || 
                      userRole == "Department Head" || 
                      userRole == "AuraSuprime"

    BaseScreen(
        title = "Kitchen Management",
        onBackClick = onBackClick,
        fabAction = {
            if (selectedTab == 0) {
                onAddClick()
            } else {
                showAddReqDialog = true
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Material 3 Custom TabRow
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
                            Icon(Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kitchen Wastage", fontWeight = FontWeight.SemiBold)
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
                            Text("Store Requisitions", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Kitchen Wastage view
                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No wastage records found.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(records) { record ->
                            WasteRecordItem(
                                record = record,
                                onApprove = { viewModel.updateStatus(record.id, "Approved") },
                                onReject = { viewModel.updateStatus(record.id, "Rejected") }
                            )
                        }
                    }
                }
            } else {
                // Store Requisitions view
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
                            Text("No store requisitions found.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requirements) { req ->
                            RequirementItem(
                                req = req,
                                isChefOrHOD = isChefOrHOD,
                                onPass = { inventoryViewModel.passRequirement(req.id) },
                                onReject = { inventoryViewModel.rejectRequirement(req.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Create Requisition
    if (showAddReqDialog) {
        var itemName by remember { mutableStateOf("") }
        var quantityStr by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("kg") }
        var chefName by remember { mutableStateOf("Kitchen Crew") }

        AlertDialog(
            onDismissRequest = { showAddReqDialog = false },
            title = { Text("Request Store Supplies", fontWeight = FontWeight.Bold, color = RoyalBlue) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name (e.g. Rice, Salt, Oil)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Select Unit
                    Column {
                        Text("Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("kg", "Ltr", "Pcs", "Bag").forEach { u ->
                                FilterChip(
                                    selected = unit == u,
                                    onClick = { unit = u },
                                    label = { Text(u) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = chefName,
                        onValueChange = { chefName = it },
                        label = { Text("Requested By") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityStr.toDoubleOrNull() ?: 0.0
                        if (itemName.isNotBlank() && qty > 0.0) {
                            inventoryViewModel.addRequirement(itemName, qty, unit, chefName)
                            showAddReqDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Submit Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReqDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WasteRecordItem(record: WasteRecord, onApprove: () -> Unit, onReject: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(record.timestamp))

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
                Text(
                    text = record.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                StatusChip(status = record.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Category: ${record.category}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
            Text(text = "Quantity: ${record.quantity} ${record.unit}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
            Text(text = "Reason: ${record.reason}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "By: ${record.staffName}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                Text(text = dateString, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
            }

            if (record.status == "Pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Reject", color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Approve", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RequirementItem(
    req: KitchenRequirement,
    isChefOrHOD: Boolean,
    onPass: () -> Unit,
    onReject: () -> Unit
) {
    val dateString = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(req.date))

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
                StatusChip(status = req.status)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Requested Quantity: ${req.quantity} ${req.unit}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
            Text(text = "Indent Staff: ${req.requestedBy}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Req ID: ${req.id.take(8)}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                Text(text = dateString, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
            }

            // Chef action to pass/reject requirement
            if (req.status == "Pending Chef Approval" && isChefOrHOD) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onPass,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pass (Chef Approved)", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "Approved", "Passed (Chef Approved)", "Issued" -> Color(0xFF10B981)
        "Rejected" -> Color.Red
        else -> Color(0xFF3B82F6) // Pending is Blue
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
