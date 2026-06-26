package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoyalBlue

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreen(title: String, onBackClick: () -> Unit, fabAction: (() -> Unit)? = null, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalBlue)
            )
        },
        floatingActionButton = {
            if (fabAction != null) {
                FloatingActionButton(onClick = fabAction, containerColor = RoyalBlue) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        content(padding)
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionScreen(hotelViewModel: HotelViewModel, onBackClick: () -> Unit) {
    // ... replaced ...
}
*/

// BaseScreen and other screens...

@Composable
fun StaffScreen(hotelViewModel: HotelViewModel, onBackClick: () -> Unit) {
    val staff by hotelViewModel.staff.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var staffName by remember { mutableStateOf("") }
    var staffRole by remember { mutableStateOf("") }

    BaseScreen(title = "Staff Management", onBackClick = onBackClick, fabAction = { showDialog = true }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(staff) { member ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    ListItem(
                        headlineContent = { Text(member.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${member.role} - " + if (member.isOnDuty) "On Duty" else "Off Duty") },
                        leadingContent = { Icon(Icons.Default.Badge, contentDescription = null, tint = RoyalBlue) },
                        trailingContent = {
                            Switch(checked = member.isOnDuty, onCheckedChange = { hotelViewModel.toggleStaffDuty(member) })
                        }
                    )
                }
            }
             if (staff.isEmpty()) {
                item { Text("No staff members. Tap + to add.") }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Staff Member") },
            text = {
                Column {
                    OutlinedTextField(
                        value = staffName,
                        onValueChange = { staffName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = staffRole,
                        onValueChange = { staffRole = it },
                        label = { Text("Role (e.g. Receptionist)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (staffName.isNotBlank() && staffRole.isNotBlank()) {
                        hotelViewModel.addStaff(staffName, staffRole)
                        staffName = ""
                        staffRole = ""
                        showDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}


