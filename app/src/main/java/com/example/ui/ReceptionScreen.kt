package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionScreen(
    viewModel: ReceptionViewModel,
    onBackClick: () -> Unit
) {
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val guests by viewModel.guests.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var newRoomNumber by remember { mutableStateOf("") }
    
    var showCheckInDialog by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    
    var showChangeRoomDialog by remember { mutableStateOf(false) }
    var selectedGuestForChange by remember { mutableStateOf<Guest?>(null) }
    var selectedOldRoom by remember { mutableStateOf<Room?>(null) }
    var targetRoomNumber by remember { mutableStateOf("") }
    
    var searchQuery by remember { mutableStateOf("") }

    BaseScreen(
        title = "Reception",
        onBackClick = onBackClick,
        fabAction = { showAddRoomDialog = true }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Rooms or Guests") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val filteredRooms = rooms.filter {
                    it.roomNumber.contains(searchQuery, ignoreCase = true) ||
                    (it.currentGuestId != null && guests.find { g -> g.id == it.currentGuestId }?.name?.contains(searchQuery, ignoreCase = true) == true)
                }

                items(filteredRooms) { room ->
                    val guest = guests.find { it.id == room.currentGuestId }
                    RoomCard(
                        room = room,
                        guest = guest,
                        onCheckInClick = {
                            selectedRoom = room
                            showCheckInDialog = true
                        },
                        onCheckOutClick = {
                            viewModel.checkOutGuest(room.id, guest!!.id)
                        },
                        onChangeRoomClick = {
                            selectedOldRoom = room
                            selectedGuestForChange = guest
                            showChangeRoomDialog = true
                        }
                    )
                }
                
                if (filteredRooms.isEmpty() && !isLoading) {
                    item {
                        Text(
                            text = "No rooms available. Tap + to add a room.",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    if (showAddRoomDialog) {
        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("Add Room") },
            text = {
                OutlinedTextField(
                    value = newRoomNumber,
                    onValueChange = { newRoomNumber = it },
                    label = { Text("Room Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newRoomNumber.isNotBlank()) {
                        viewModel.addRoom(newRoomNumber)
                        newRoomNumber = ""
                        showAddRoomDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddRoomDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCheckInDialog && selectedRoom != null) {
        CheckInDialog(
            room = selectedRoom!!,
            onDismiss = { showCheckInDialog = false },
            onConfirm = { name, notes, paymentStatus, days, photoUri ->
                val expectedCheckOut = System.currentTimeMillis() + (days * 24L * 60L * 60L * 1000L)
                viewModel.checkInGuest(
                    roomId = selectedRoom!!.id,
                    name = name,
                    notes = notes,
                    paymentStatus = paymentStatus,
                    expectedCheckOutDate = expectedCheckOut,
                    idPhotoUriString = photoUri?.toString(),
                    onSuccess = { showCheckInDialog = false },
                    onError = { /* show error toast */ }
                )
            }
        )
    }

    if (showChangeRoomDialog && selectedGuestForChange != null && selectedOldRoom != null) {
        AlertDialog(
            onDismissRequest = { showChangeRoomDialog = false },
            title = { Text("Change Room") },
            text = {
                Column {
                    Text("Move ${selectedGuestForChange!!.name} from Room ${selectedOldRoom!!.roomNumber} to:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetRoomNumber,
                        onValueChange = { targetRoomNumber = it },
                        label = { Text("New Room Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val targetRoom = rooms.find { it.roomNumber == targetRoomNumber && it.status == "Available" }
                    if (targetRoom != null) {
                        viewModel.changeRoom(selectedGuestForChange!!.id, selectedOldRoom!!.id, targetRoom.id)
                        showChangeRoomDialog = false
                        targetRoomNumber = ""
                    }
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showChangeRoomDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCard(
    room: Room,
    guest: Guest?,
    onCheckInClick: () -> Unit,
    onCheckOutClick: () -> Unit,
    onChangeRoomClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = if (room.status == "Available") Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Room ${room.roomNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = room.status,
                            color = if (room.status == "Available") Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                    }
                }
                
                if (room.status == "Available") {
                    Button(onClick = onCheckInClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                        Text("Check In")
                    }
                } else if (room.status == "Occupied" && guest != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onChangeRoomClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))) {
                            Text("Change Room")
                        }
                        Button(onClick = onCheckOutClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                            Text("Check Out")
                        }
                    }
                }
            }

            if (guest != null && room.status == "Occupied") {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (guest.idPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(guest.idPhotoUrl).crossfade(true).build(),
                            contentDescription = "Guest ID",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Guest: ${guest.name}", fontWeight = FontWeight.SemiBold)
                        Text("ID: ${guest.guestId}", fontSize = 12.sp, color = Color.Gray)
                        Text("Payment: ${guest.paymentStatus}", fontSize = 12.sp, color = if (guest.paymentStatus == "Paid") Color(0xFF10B981) else Color(0xFFF59E0B))
                    }
                }
            }
        }
    }
}

@Composable
fun CheckInDialog(
    room: Room,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Uri?) -> Unit
) {
    var guestName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf("Pending") }
    var expectedDays by remember { mutableStateOf("1") }
    var idPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> idPhotoUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Check In - Room ${room.roomNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (idPhotoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(idPhotoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "ID Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Text("Upload Guest ID")
                        }
                    }
                }

                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("Guest Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = expectedDays,
                    onValueChange = { expectedDays = it },
                    label = { Text("Stay Duration (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = paymentStatus,
                    onValueChange = { paymentStatus = it },
                    label = { Text("Payment Status (e.g. Paid, Pending)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (guestName.isNotBlank() && expectedDays.toIntOrNull() != null) {
                    onConfirm(guestName, notes, paymentStatus, expectedDays.toInt(), idPhotoUri)
                }
            }) { Text("Confirm Check In") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
