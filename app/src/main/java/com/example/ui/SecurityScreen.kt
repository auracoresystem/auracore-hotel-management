package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
fun SecurityScreen(
    viewModel: SecurityViewModel,
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val visitors by viewModel.visitors.collectAsStateWithLifecycle()
    val incidents by viewModel.incidents.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showVisitorDialog by remember { mutableStateOf(false) }
    var showIncidentDialog by remember { mutableStateOf(false) }

    BaseScreen(
        title = "Security & Visitors",
        onBackClick = onBackClick
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showVisitorDialog = true }, modifier = Modifier.weight(1f)) {
                    Text("Add Visitor")
                }
                Button(onClick = { showIncidentDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), modifier = Modifier.weight(1f)) {
                    Text("Report Incident")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Visitors", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visitors) { visitor ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(visitor.name, fontWeight = FontWeight.Bold)
                                    Text(visitor.purpose, color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                            if (visitor.status == "Checked In") {
                                Button(onClick = { viewModel.checkOutVisitor(visitor.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))) {
                                    Text("Check Out")
                                }
                            } else {
                                Text("Checked Out", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVisitorDialog) {
        var name by remember { mutableStateOf("") }
        var purpose by remember { mutableStateOf("") }
        var vehicle by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showVisitorDialog = false },
            title = { Text("New Visitor Entry") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Visitor Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Purpose") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = vehicle, onValueChange = { vehicle = it }, label = { Text("Vehicle No. (Optional)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addVisitor(name, purpose, vehicle, null, onSuccess = { 
                            com.example.util.SoundUtils.playSuccessSound(context)
                            showVisitorDialog = false 
                        })
                    }
                }) { Text("Entry") }
            },
            dismissButton = { TextButton(onClick = { showVisitorDialog = false }) { Text("Cancel") } }
        )
    }

    if (showIncidentDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("General") }
        
        AlertDialog(
            onDismissRequest = { showIncidentDialog = false },
            title = { Text("Report Incident") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (General/Emergency)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.reportIncident(title, desc, type, onSuccess = { 
                            com.example.util.SoundUtils.playSuccessSound(context)
                            showIncidentDialog = false 
                        })
                    }
                }) { Text("Report") }
            },
            dismissButton = { TextButton(onClick = { showIncidentDialog = false }) { Text("Cancel") } }
        )
    }
}
