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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionScreen(hotelViewModel: HotelViewModel, onBackClick: () -> Unit) {
    val rooms by hotelViewModel.rooms.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var newRoomNumber by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf<com.example.data.RoomStatus?>(null) }
    var guestName by remember { mutableStateOf("") }
    var checkInDialog by remember { mutableStateOf(false) }

    BaseScreen(title = "Reception", onBackClick = onBackClick, fabAction = { showDialog = true }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(rooms) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    onClick = {
                        if (room.guestName.isNullOrEmpty()) {
                            selectedRoom = room
                            guestName = ""
                            checkInDialog = true
                        } else {
                            hotelViewModel.checkOutGuest(room)
                        }
                    }
                ) {
                    ListItem(
                        headlineContent = { Text("Room ${room.roomNumber}", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(if (room.guestName.isNullOrEmpty()) "Available" else "Occupied by ${room.guestName}") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null, tint = if (room.guestName.isNullOrEmpty()) Color.Gray else RoyalBlue) },
                        trailingContent = {
                            if (!room.guestName.isNullOrEmpty()) {
                                Button(onClick = { hotelViewModel.checkOutGuest(room) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                                    Text("Check Out")
                                }
                            } else {
                                Button(onClick = {
                                    selectedRoom = room
                                    guestName = ""
                                    checkInDialog = true
                                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                                    Text("Check In")
                                }
                            }
                        }
                    )
                }
            }
            if (rooms.isEmpty()) {
                item { Text("No rooms available. Tap + to add rooms.") }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Room") },
            text = {
                OutlinedTextField(
                    value = newRoomNumber,
                    onValueChange = { newRoomNumber = it },
                    label = { Text("Room Number") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newRoomNumber.isNotBlank()) {
                        hotelViewModel.addRoom(newRoomNumber)
                        newRoomNumber = ""
                        showDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }

    if (checkInDialog && selectedRoom != null) {
         AlertDialog(
            onDismissRequest = { checkInDialog = false },
            title = { Text("Check In - Room ${selectedRoom?.roomNumber}") },
            text = {
                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("Guest Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (guestName.isNotBlank()) {
                        hotelViewModel.checkInGuest(selectedRoom!!, guestName)
                        checkInDialog = false
                    }
                }) { Text("Check In") }
            },
            dismissButton = { TextButton(onClick = { checkInDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun CleaningScreen(hotelViewModel: HotelViewModel, onBackClick: () -> Unit) {
    val rooms by hotelViewModel.rooms.collectAsStateWithLifecycle()

    BaseScreen(title = "Cleaning", onBackClick = onBackClick) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(rooms) { room ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    ListItem(
                        headlineContent = { Text("Room ${room.roomNumber}", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(if (room.isCleaned) "Cleaned" else "Needs Cleaning") },
                        leadingContent = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = if (room.isCleaned) Color(0xFF10B981) else Color(0xFFEF4444)) },
                        trailingContent = {
                            Switch(
                                checked = room.isCleaned,
                                onCheckedChange = { isCleaned -> hotelViewModel.updateRoomStatus(room, isCleaned) }
                            )
                        }
                    )
                }
            }
             if (rooms.isEmpty()) {
                item { Text("No rooms exist yet. Add rooms from Reception.") }
            }
        }
    }
}

@Composable
fun RepairsScreen(hotelViewModel: HotelViewModel, onBackClick: () -> Unit) {
    val repairs by hotelViewModel.repairs.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var issueDesc by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    BaseScreen(title = "Repairs & Maintenance", onBackClick = onBackClick, fabAction = { showDialog = true }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(repairs) { repair ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    ListItem(
                        headlineContent = { Text(repair.description, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${repair.location} - " + if (repair.isFixed) "Fixed" else "Pending") },
                        leadingContent = { Icon(Icons.Default.Build, contentDescription = null, tint = if (repair.isFixed) Color(0xFF10B981) else Color(0xFFF59E0B)) },
                        trailingContent = {
                            Checkbox(checked = repair.isFixed, onCheckedChange = { hotelViewModel.toggleRepairStatus(repair) })
                        }
                    )
                }
            }
             if (repairs.isEmpty()) {
                item { Text("No repair requests.") }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New Repair Request") },
            text = {
                Column {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (e.g. Room 101)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = issueDesc,
                        onValueChange = { issueDesc = it },
                        label = { Text("Issue Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (location.isNotBlank() && issueDesc.isNotBlank()) {
                        hotelViewModel.addRepair(issueDesc, location)
                        location = ""
                        issueDesc = ""
                        showDialog = false
                    }
                }) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

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
