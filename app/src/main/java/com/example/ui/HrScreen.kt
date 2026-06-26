package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrScreen(
    viewModel: HrViewModel,
    onBackClick: () -> Unit
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val leaveRequests by viewModel.leaveRequests.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showCheckInDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    BaseScreen(
        title = "HR & Attendance",
        onBackClick = onBackClick
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showCheckInDialog = true }, modifier = Modifier.weight(1f)) {
                    Text("Check In")
                }
                Button(onClick = { showLeaveDialog = true }, modifier = Modifier.weight(1f)) {
                    Text("Leave Request")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Leave Requests", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(leaveRequests) { request ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(request.employeeName, fontWeight = FontWeight.Bold)
                                Text(request.reason, color = Color.Gray, fontSize = 14.sp)
                            }
                            Text(
                                request.status,
                                color = if (request.status == "Approved") Color(0xFF10B981) else if (request.status == "Rejected") Color(0xFFEF4444) else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCheckInDialog) {
        var empId by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = { Text("Quick Check-In") },
            text = {
                OutlinedTextField(
                    value = empId,
                    onValueChange = { empId = it },
                    label = { Text("Employee ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (empId.isNotBlank()) {
                        viewModel.checkIn(empId, onSuccess = { showCheckInDialog = false })
                    }
                }) { Text("Check In") }
            },
            dismissButton = { TextButton(onClick = { showCheckInDialog = false }) { Text("Cancel") } }
        )
    }
    
    if (showLeaveDialog) {
        var empId by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Request Leave") },
            text = {
                Column {
                    OutlinedTextField(value = empId, onValueChange = { empId = it }, label = { Text("Employee ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (empId.isNotBlank() && reason.isNotBlank()) {
                        viewModel.requestLeave(empId, name, reason, onSuccess = { showLeaveDialog = false })
                    }
                }) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel") } }
        )
    }
}
