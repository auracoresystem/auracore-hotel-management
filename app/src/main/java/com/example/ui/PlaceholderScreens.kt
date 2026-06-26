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

@Composable
fun ReceptionScreen(onBackClick: () -> Unit) {
    BaseScreen(title = "Reception", onBackClick = onBackClick, fabAction = {}) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(listOf("John Doe - Room 101", "Jane Smith - Room 102", "Alice Brown - Room 105")) { guest ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    ListItem(
                        headlineContent = { Text(guest, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Checked In") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null, tint = RoyalBlue) }
                    )
                }
            }
        }
    }
}

@Composable
fun CleaningScreen(onBackClick: () -> Unit) {
    BaseScreen(title = "Cleaning", onBackClick = onBackClick) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(listOf(Pair("Room 101", true), Pair("Room 102", false), Pair("Room 103", false))) { room ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    ListItem(
                        headlineContent = { Text(room.first, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(if (room.second) "Cleaned" else "Needs Cleaning") },
                        leadingContent = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = if (room.second) Color(0xFF10B981) else Color(0xFFEF4444)) }
                    )
                }
            }
        }
    }
}

@Composable
fun RepairsScreen(onBackClick: () -> Unit) {
    BaseScreen(title = "Repairs & Maintenance", onBackClick = onBackClick, fabAction = {}) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(listOf("AC not working - Room 201", "Leaking Tap - Room 105")) { issue ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    ListItem(
                        headlineContent = { Text(issue, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Pending") },
                        leadingContent = { Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFF59E0B)) }
                    )
                }
            }
        }
    }
}

@Composable
fun StaffScreen(onBackClick: () -> Unit) {
    BaseScreen(title = "Staff Management", onBackClick = onBackClick, fabAction = {}) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(listOf("Michael - Chef", "Sarah - Housekeeping", "David - Reception")) { staff ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    ListItem(
                        headlineContent = { Text(staff, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("On Duty") },
                        leadingContent = { Icon(Icons.Default.Badge, contentDescription = null, tint = RoyalBlue) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(onBackClick: () -> Unit) {
    BaseScreen(title = "Reports & Analytics", onBackClick = onBackClick) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today's Revenue", color = Color.Gray)
                    Text("$1,240.00", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Occupancy Rate", color = Color.Gray)
                    Text("85%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                }
            }
        }
    }
}

@Composable
fun SetupScreen(onBackClick: () -> Unit) {
    BaseScreen(title = "System Setup", onBackClick = onBackClick) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                ListItem(headlineContent = { Text("Hotel Profile") }, leadingContent = { Icon(Icons.Default.Business, contentDescription = null, tint = RoyalBlue) })
            }
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                ListItem(headlineContent = { Text("User Permissions") }, leadingContent = { Icon(Icons.Default.Security, contentDescription = null, tint = RoyalBlue) })
            }
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                ListItem(headlineContent = { Text("Notification Settings") }, leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = RoyalBlue) })
            }
        }
    }
}
