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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel,
    onBackClick: () -> Unit
) {
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedTicket by remember { mutableStateOf<MaintenanceTicket?>(null) }

    BaseScreen(
        title = "Maintenance",
        onBackClick = onBackClick,
        fabAction = { showCreateDialog = true }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tickets) { ticket ->
                    TicketCard(
                        ticket = ticket,
                        onClick = {
                            selectedTicket = ticket
                            showUpdateDialog = true
                        }
                    )
                }
                if (tickets.isEmpty() && !isLoading) {
                    item { Text("No maintenance tickets.", color = Color.Gray) }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTicketDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { room, cat, prio, uri, notes ->
                viewModel.createTicket(room, cat, prio, uri?.toString(), notes, onSuccess = { showCreateDialog = false })
            }
        )
    }

    if (showUpdateDialog && selectedTicket != null) {
        UpdateTicketDialog(
            ticket = selectedTicket!!,
            onDismiss = { showUpdateDialog = false },
            onUpdate = { status, afterUri, notes ->
                viewModel.updateTicket(selectedTicket!!.id, status, afterUri?.toString(), notes, onSuccess = { showUpdateDialog = false })
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCard(ticket: MaintenanceTicket, onClick: () -> Unit) {
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
                    Icons.Default.Build, 
                    contentDescription = null, 
                    tint = if (ticket.status == "Completed") Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("${ticket.ticketNumber} - Room ${ticket.roomNumber}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${ticket.category} • ${ticket.priority}", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Text(
                text = ticket.status,
                color = if (ticket.status == "Completed") Color(0xFF10B981) else Color(0xFFF59E0B),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Uri?, String) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Low") }
    var notes by remember { mutableStateOf("") }
    var beforePhotoUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> beforePhotoUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Maintenance Ticket") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = roomNumber, onValueChange = { roomNumber = it }, label = { Text("Room/Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Plumbing)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = priority, onValueChange = { priority = it }, label = { Text("Priority (Low, Medium, High, Emergency)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).clickable {
                            picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (beforePhotoUri != null) {
                        AsyncImage(model = beforePhotoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(roomNumber, category, priority, beforePhotoUri, notes) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun UpdateTicketDialog(
    ticket: MaintenanceTicket,
    onDismiss: () -> Unit,
    onUpdate: (String, Uri?, String) -> Unit
) {
    var status by remember { mutableStateOf(ticket.status) }
    var notes by remember { mutableStateOf(ticket.repairNotes) }
    var afterPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> afterPhotoUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Ticket - ${ticket.ticketNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status (Pending, In Progress, Completed)") }, modifier = Modifier.fillMaxWidth())
                
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).clickable {
                            picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (afterPhotoUri != null || ticket.afterPhotoUrl.isNotEmpty()) {
                        AsyncImage(model = afterPhotoUri ?: ticket.afterPhotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Repair Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onUpdate(status, afterPhotoUri, notes) }) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
