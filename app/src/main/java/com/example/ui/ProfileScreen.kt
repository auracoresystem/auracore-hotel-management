package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val registeredUsers by authViewModel.registeredUsers.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    // Find the currently logged in RegisteredUser to retrieve their correct password and staffId
    val matchedUser = remember(registeredUsers, profile) {
        registeredUsers.find { 
            it.email.trim().equals(profile?.email?.trim(), ignoreCase = true) ||
            it.staffId.trim().equals(profile?.employeeId?.trim(), ignoreCase = true)
        }
    }

    BaseScreen(
        title = "Profile & Settings",
        onBackClick = onBackClick
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (profile != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = RoyalBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(profile!!.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(profile!!.role, color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Email", color = Color.Gray, fontSize = 12.sp)
                                Text(profile!!.email, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Department", color = Color.Gray, fontSize = 12.sp)
                                Text(profile!!.department.ifEmpty { "Not set" }, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Phone", color = Color.Gray, fontSize = 12.sp)
                                Text(profile!!.phone.ifEmpty { "Not set" }, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Unique Staff ID", color = Color.Gray, fontSize = 12.sp)
                                Text(matchedUser?.staffId?.ifEmpty { "N/A" } ?: profile!!.employeeId.ifEmpty { "N/A" }, fontWeight = FontWeight.SemiBold, color = RoyalBlue)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showEditDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    ListItem(headlineContent = { Text("Dark Mode") }, trailingContent = { Switch(checked = false, onCheckedChange = {}) })
                    HorizontalDivider()
                    ListItem(headlineContent = { Text("Language") }, trailingContent = { Text("English", color = Color.Gray) })
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Change Password (पासवर्ड बदलें)") }, 
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalBlue) },
                        modifier = Modifier.clickable { showPasswordDialog = true }
                    )
                    HorizontalDivider()
                    ListItem(headlineContent = { Text("Notification Settings") }, leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) })
                }
            }
        }
    }

    if (showEditDialog && profile != null) {
        var name by remember { mutableStateOf(profile!!.name) }
        var phone by remember { mutableStateOf(profile!!.phone) }
        var dept by remember { mutableStateOf(profile!!.department) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProfile(name, phone, dept, onSuccess = { showEditDialog = false })
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showPasswordDialog && profile != null && matchedUser != null) {
        var currentPasswordInput by remember { mutableStateOf("") }
        var newPasswordInput by remember { mutableStateOf("") }
        var confirmPasswordInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Security Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Unique ID: ${matchedUser.staffId}", fontWeight = FontWeight.Bold, color = RoyalBlue, fontSize = 14.sp)
                    
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = { currentPasswordInput = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("New Password (Min 4 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (currentPasswordInput != matchedUser.password) {
                        Toast.makeText(context, "Current password is incorrect!", Toast.LENGTH_SHORT).show()
                    } else if (newPasswordInput.length < 4) {
                        Toast.makeText(context, "New password must be at least 4 characters!", Toast.LENGTH_SHORT).show()
                    } else if (newPasswordInput != confirmPasswordInput) {
                        Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                    } else {
                        val success = authViewModel.changeUserPassword(matchedUser.id, newPasswordInput)
                        if (success) {
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            showPasswordDialog = false
                        } else {
                            Toast.makeText(context, "Failed to update password!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("Change Password") }
            },
            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") } }
        )
    }
}
