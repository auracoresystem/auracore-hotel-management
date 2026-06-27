package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoyalBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    userRole: String,
    userName: String,
    onBackClick: () -> Unit
) {
    var isCheckedIn by remember { mutableStateOf(false) }
    var checkInTime by remember { mutableStateOf<String?>(null) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var scanAction by remember { mutableStateOf("") } // "checkIn" or "checkOut"
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Dummy tasks based on role
    val initialTasks = remember(userRole) {
        when (userRole) {
            "Housekeeping" -> listOf("Clean Room 101", "Restock Toiletries", "Vacuum Hallway", "Empty Trash Bins")
            "Receptionist" -> listOf("Verify Arriving Guests", "Cash Register Handover", "Reply to pending emails", "Print keycards")
            "Security" -> listOf("Gate Patrol", "Check CCTV Cameras", "Verify Night Staff", "Lock Service Elevator")
            "Kitchen Staff" -> listOf("Prep Breakfast Items", "Check Inventory", "Clean Kitchen Counters", "Dispose Kitchen Wastage")
            else -> listOf("Attend Morning Briefing", "Check Emails", "Organize Workspace", "Submit Daily Report")
        }
    }.map { TaskItem(it, false) }

    var tasks by remember { mutableStateOf(initialTasks) }

    BaseScreen(
        title = "Attendance & Tasks",
        onBackClick = onBackClick
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            // Check-in / Out Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hello, $userName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                    Text(
                        text = "Role: $userRole",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Icon(
                        imageVector = if (isCheckedIn) Icons.Default.HowToReg else Icons.Default.QrCodeScanner,
                        contentDescription = "Status",
                        modifier = Modifier.size(64.dp),
                        tint = if (isCheckedIn) Color(0xFF10B981) else RoyalBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isCheckedIn) "You are Checked In" else "Not Checked In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (checkInTime != null) {
                        Text(
                            text = "Since $checkInTime",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (!isCheckedIn) {
                        Button(
                            onClick = { 
                                scanAction = "checkIn"
                                showScannerDialog = true 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Scan QR & Live Selfie")
                        }
                    } else {
                        Button(
                            onClick = { 
                                scanAction = "checkOut"
                                showScannerDialog = true 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("End Shift & Check Out")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tasks Section
            Text(
                text = "Today's Assigned Tasks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!isCheckedIn && checkInTime == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Please Check-In to view and complete your daily tasks.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(tasks.size) { index ->
                        val task = tasks[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isCheckedIn) {
                                            val newTasks = tasks.toMutableList()
                                            newTasks[index] = task.copy(isCompleted = !task.isCompleted)
                                            tasks = newTasks
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { checked ->
                                        if (isCheckedIn) {
                                            val newTasks = tasks.toMutableList()
                                            newTasks[index] = task.copy(isCompleted = checked)
                                            tasks = newTasks
                                        }
                                    },
                                    enabled = isCheckedIn,
                                    colors = CheckboxDefaults.colors(checkedColor = RoyalBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    fontSize = 16.sp,
                                    color = if (task.isCompleted) Color.Gray else Color.Black,
                                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                            }
                        }
                    }
                    
                    item {
                        val progress = if (tasks.isEmpty()) 0f else tasks.count { it.isCompleted }.toFloat() / tasks.size
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Completion: ${(progress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = if (progress == 1f) Color(0xFF10B981) else RoyalBlue
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (progress == 1f) Color(0xFF10B981) else RoyalBlue,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }

    if (showScannerDialog) {
        AlertDialog(
            onDismissRequest = { if (!isScanning) showScannerDialog = false },
            title = { Text("Verifying Identity...") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.Black, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (isScanning) "Aligning Face with QR..." else "Please position the QR code and your face within the frame.",
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isScanning = true
                        scope.launch {
                            delay(1500) // Simulate processing delay
                            isScanning = false
                            showScannerDialog = false
                            if (scanAction == "checkIn") {
                                isCheckedIn = true
                                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                checkInTime = timeFormat.format(Date())
                            } else {
                                isCheckedIn = false
                            }
                        }
                    },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(if (isScanning) "Scanning..." else "Simulate Scan")
                }
            },
            dismissButton = {
                if (!isScanning) {
                    TextButton(onClick = { showScannerDialog = false }) { Text("Cancel") }
                }
            }
        )
    }
}

data class TaskItem(val title: String, val isCompleted: Boolean)
