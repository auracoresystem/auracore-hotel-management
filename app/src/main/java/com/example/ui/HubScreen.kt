package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
    viewModel: HubViewModel,
    userRole: String = "Owner",
    onBackClick: () -> Unit
) {
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    val isCoreTeam = userRole == "Owner" || userRole == "General Manager" || userRole == "Department Head"

    // Filter announcements: HOD/Core Team sees all, regular staff ONLY sees "All Staff"
    val filteredAnnouncements = remember(announcements, isCoreTeam) {
        if (isCoreTeam) {
            announcements
        } else {
            announcements.filter { it.targetAudience == "All Staff" }
        }
    }

    BaseScreen(
        title = "AuraCore Hub",
        onBackClick = onBackClick,
        fabAction = if (isCoreTeam) { { showCreateDialog = true } } else null
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notice Board", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                Text(
                    text = if (isCoreTeam) "HOD Posting Enabled" else "Read-Only Access",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCoreTeam) RoyalBlue else Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAnnouncements.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No announcements available for your role.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredAnnouncements) { announcement ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Campaign, contentDescription = null, tint = RoyalBlue)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(announcement.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Audience chip
                                        val isCoreOnly = announcement.targetAudience == "Core Team"
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(announcement.targetAudience, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (isCoreOnly) Color(0xFFFCE7F3) else Color(0xFFF1F5F9),
                                                labelColor = if (isCoreOnly) Color(0xFF9D174D) else Color(0xFF475569)
                                            ),
                                            border = null,
                                            modifier = Modifier.height(24.dp)
                                        )
                                        
                                        // Delete only for Core Team
                                        if (isCoreTeam) {
                                            IconButton(onClick = { viewModel.deleteAnnouncement(announcement.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(announcement.content, fontSize = 14.sp, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Posted by: ${announcement.authorName}",
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(announcement.createdAt)),
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var targetAudience by remember { mutableStateOf("All Staff") } // Options: "All Staff", "Core Team"
        
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Post Broadcast Announcement") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Announcement Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    // Audience Target Selector
                    Column {
                        Text("Target Audience", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All Staff", "Core Team").forEach { aud ->
                                val isSel = targetAudience == aud
                                FilterChip(
                                    selected = isSel,
                                    onClick = { targetAudience = aud },
                                    label = { Text(aud) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            viewModel.createAnnouncement(title, content, targetAudience, onSuccess = { showCreateDialog = false })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Post Broadcast")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
