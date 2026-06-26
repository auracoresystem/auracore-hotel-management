package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.Gold
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousekeepingScreen(
    viewModel: HousekeepingViewModel,
    onBackClick: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedRoomId by remember { mutableStateOf("") }
    var staffName by remember { mutableStateOf("") }
    
    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<CleaningTask?>(null) }

    BaseScreen(
        title = "Housekeeping",
        onBackClick = onBackClick,
        fabAction = { showAssignDialog = true }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = {
                            selectedTask = task
                            showUpdateDialog = true
                        }
                    )
                }
                if (tasks.isEmpty() && !isLoading) {
                    item { Text("No cleaning tasks assigned.", color = Color.Gray) }
                }
            }
        }
    }

    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign Room Cleaning") },
            text = {
                Column {
                    OutlinedTextField(
                        value = selectedRoomId,
                        onValueChange = { selectedRoomId = it },
                        label = { Text("Room Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = staffName,
                        onValueChange = { staffName = it },
                        label = { Text("Staff Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val room = rooms.find { it.roomNumber == selectedRoomId }
                    if (room != null && staffName.isNotBlank()) {
                        viewModel.assignTask(room.id, room.roomNumber, "STAFF-ID-TEMP", staffName)
                        showAssignDialog = false
                    }
                }) { Text("Assign") }
            },
            dismissButton = { TextButton(onClick = { showAssignDialog = false }) { Text("Cancel") } }
        )
    }

    if (showUpdateDialog && selectedTask != null) {
        UpdateTaskDialog(
            task = selectedTask!!,
            onDismiss = { showUpdateDialog = false },
            onUpdate = { status, beforePhoto, afterPhoto, notes ->
                viewModel.updateTaskStatus(selectedTask!!.id, status, beforePhoto?.toString(), afterPhoto?.toString(), notes)
                showUpdateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(task: CleaningTask, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CleaningServices, 
                    contentDescription = null, 
                    tint = if (task.status == "Ready") Color(0xFF10B981) else if (task.status == "Dirty") Color(0xFFEF4444) else Color(0xFFF59E0B),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Room ${task.roomNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Assigned to: ${task.assignedStaffName}", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Text(
                text = task.status,
                color = if (task.status == "Ready") Color(0xFF10B981) else if (task.status == "Dirty") Color(0xFFEF4444) else Color(0xFFF59E0B),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UpdateTaskDialog(
    task: CleaningTask,
    onDismiss: () -> Unit,
    onUpdate: (String, Uri?, Uri?, String) -> Unit
) {
    var status by remember { mutableStateOf(task.status) }
    var notes by remember { mutableStateOf("") }
    var beforePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var afterPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val beforePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> beforePhotoUri = uri }
    val afterPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> afterPhotoUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Status - Room ${task.roomNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Simple dropdown simulation with text field for now
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status (Dirty, Cleaning, Inspection, Ready)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant).clickable {
                                beforePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (beforePhotoUri != null || task.beforePhotoUrl.isNotEmpty()) {
                            AsyncImage(model = beforePhotoUri ?: task.beforePhotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant).clickable {
                                afterPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (afterPhotoUri != null || task.afterPhotoUrl.isNotEmpty()) {
                            AsyncImage(model = afterPhotoUri ?: task.afterPhotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Inspection Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onUpdate(status, beforePhotoUri, afterPhotoUri, notes) }) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
