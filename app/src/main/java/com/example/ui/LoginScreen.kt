package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToDashboard: (String) -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val hotels by viewModel.hotels.collectAsStateWithLifecycle()
    var isLoginMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("owner@auracore.com") }
    var password by remember { mutableStateOf("admin123") }
    var selectedRole by remember { mutableStateOf("Owner") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var hotelNameInput by remember { mutableStateOf("") }
    var selectedHotelId by remember { mutableStateOf("hotel_1") }
    var securityKey by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var joinCodeInput by remember { mutableStateOf("") }
    var localErrorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onNavigateToDashboard((authState as AuthState.Authenticated).role)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AuraCore",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hotel Management System",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Modern Tab-like Selector for Login vs Sign Up
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { isLoginMode = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isLoginMode) RoyalBlue else Color.Gray
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Login", fontSize = 16.sp, fontWeight = if (isLoginMode) FontWeight.Bold else FontWeight.Normal)
                            if (isLoginMode) {
                                Box(modifier = Modifier.width(40.dp).height(3.dp).background(RoyalBlue, RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    TextButton(
                        onClick = { isLoginMode = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (!isLoginMode) RoyalBlue else Color.Gray
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Register", fontSize = 16.sp, fontWeight = if (!isLoginMode) FontWeight.Bold else FontWeight.Normal)
                            if (!isLoginMode) {
                                Box(modifier = Modifier.width(55.dp).height(3.dp).background(RoyalBlue, RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                }

                val rawMessage = localErrorMsg ?: (if (authState is AuthState.Error) (authState as AuthState.Error).message else null)
                if (rawMessage != null) {
                    if (rawMessage.startsWith("REGISTRATION_PENDING_APPROVAL: ")) {
                        val cleanMsg = rawMessage.substringAfter("REGISTRATION_PENDING_APPROVAL: ")
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)), // Light emerald
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFF10B981), shape = CircleShape)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Success! Approval Pending",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = cleanMsg,
                                        fontSize = 11.sp,
                                        color = Color(0xFF065F46),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = rawMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                if (!isLoginMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Mobile Number (Mandatory)") },
                        placeholder = { Text("e.g. +91 9876543210") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-Tenant Hotel Context Selector / Creator
                if (isLoginMode) {
                    if (selectedRole != "AuraSuprime") {
                        Text(
                            text = "Select Hotel to Login:",
                            fontSize = 12.sp,
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            hotels.forEach { hotel ->
                                val isSelected = selectedHotelId == hotel.id
                                val isSuspended = hotel.status == "Suspended"
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { if (!isSuspended) selectedHotelId = hotel.id },
                                    color = if (isSelected) RoyalBlue.copy(alpha = 0.08f) else Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) RoyalBlue else if (isSuspended) Color.Red.copy(alpha = 0.5f) else Color.LightGray
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = hotel.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isSuspended) Color.Gray else Color.Black
                                                )
                                                if (isSuspended) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = Color.Red.copy(alpha = 0.10f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text("SUSPENDED", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Text("Plan: ${hotel.subscriptionPlan} • Owner: ${hotel.ownerName}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { if (!isSuspended) selectedHotelId = hotel.id },
                                            colors = RadioButtonDefaults.colors(selectedColor = RoyalBlue),
                                            enabled = !isSuspended
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    // Registration Flow
                    // 1. Role Selection Grid
                    Text(
                        text = "Select Your Role",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "AuraSuprime", "Owner", "General Manager", "Department Head",
                            "Receptionist", "Housekeeping", "Security",
                            "Kitchen Staff", "Maintenance"
                        ).chunked(2).forEach { rowRoles ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowRoles.forEach { role ->
                                    val isSelected = selectedRole == role
                                    OutlinedButton(
                                        onClick = { 
                                            selectedRole = role
                                            localErrorMsg = null // clear error when switching
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) RoyalBlue.copy(alpha = 0.1f) else Color.Transparent,
                                            contentColor = if (isSelected) RoyalBlue else Color.Gray
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) RoyalBlue else Color.LightGray
                                        )
                                    ) {
                                        Text(role, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Role Info Explanation Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RoyalBlue.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (selectedRole == "AuraSuprime") Icons.Default.Lock else Icons.Default.Info,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Selected Role: $selectedRole",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (selectedRole) {
                                        "AuraSuprime" -> "👑 Superuser (AuraCore Admin): Full system control across all hotel clients. Requires Master Security Key."
                                        "Owner" -> "🏨 Hotel Owner (Tenant): Registers a new Hotel organization. Manages subscription, billing, rooms, and hotel staff."
                                        "General Manager" -> "💼 General Manager (GM): Oversees all hotel staff, housekeeping, check-ins, tasks, and hotel analytics."
                                        "Department Head" -> "👔 Department Head: Manages housekeeping supervisors, kitchen supervisors, and delegates daily tasks."
                                        else -> "👥 Hotel Staff (${selectedRole}): Performs designated operational tasks (check-ins, cleanups, safety, food prep, repairs)."
                                    },
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Conditional Inputs based on Selected Role
                    if (selectedRole == "AuraSuprime") {
                        OutlinedTextField(
                            value = securityKey,
                            onValueChange = { 
                                securityKey = it
                                localErrorMsg = null
                            },
                            label = { Text("Superuser Master Security Key") },
                            placeholder = { Text("Enter security key to register") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    } else if (selectedRole == "Owner") {
                        OutlinedTextField(
                            value = hotelNameInput,
                            onValueChange = { 
                                hotelNameInput = it
                                localErrorMsg = null
                            },
                            label = { Text("New Hotel Name") },
                            placeholder = { Text("e.g. Grand Palace Resort") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        // Staff Approval Warning
                        Surface(
                            color = Color(0xFFFEF3C7), // Amber-100 light warning
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Notice: Staff accounts require activation/approval from the Hotel Owner before logging in.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { 
                                joinCodeInput = it
                                localErrorMsg = null
                            },
                            label = { Text("Hotel Join Code (Mandatory)") },
                            placeholder = { Text("Ask your Owner/GM for the code (e.g. AURA123)") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoginMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Text("Remember Me")
                        }
                        TextButton(onClick = { viewModel.resetPassword(email) }) {
                            Text("Forgot Password?")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = { 
                        if (isLoginMode) {
                            viewModel.login(email, password, rememberMe, selectedHotelId)
                        } else {
                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                localErrorMsg = "All fields are required."
                            } else if (phoneInput.trim().isBlank()) {
                                localErrorMsg = "Mobile Number is mandatory for registration."
                            } else if (selectedRole == "AuraSuprime") {
                                if (securityKey.trim() != "SUPREME123" && securityKey.trim() != "AURASUPREME2026") {
                                    localErrorMsg = "Access Denied: Invalid AuraCore Master Security Key."
                                } else {
                                    localErrorMsg = null
                                    viewModel.signUp(name, email, password, phoneInput, selectedRole, null, null)
                                }
                            } else if (selectedRole == "Owner") {
                                if (hotelNameInput.isBlank()) {
                                    localErrorMsg = "Please enter your New Hotel Name."
                                } else {
                                    localErrorMsg = null
                                    viewModel.signUp(name, email, password, phoneInput, selectedRole, null, hotelNameInput)
                                }
                            } else {
                                // Staff / GM joining a hotel
                                if (joinCodeInput.trim().isBlank()) {
                                    localErrorMsg = "Hotel Join Code is mandatory."
                                } else {
                                    val matchedHotel = hotels.find { it.joinCode.trim().equals(joinCodeInput.trim(), ignoreCase = true) }
                                    if (matchedHotel == null) {
                                        localErrorMsg = "Invalid Hotel Join Code. Please ask your Hotel Owner or GM for the correct registration code."
                                    } else {
                                        localErrorMsg = null
                                        viewModel.signUp(name, email, password, phoneInput, selectedRole, matchedHotel.id, null)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(8.dp),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isLoginMode) "Login" else "Register & Create Account", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(
                        text = " OR BYPASS FOR DEMO ",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No login/signup required. Click below to try any role:",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Row 0: AuraSuprime Superuser
                Text("SUPERUSER ROLE (FULL CONTROLS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { viewModel.loginAsDemo("AuraSuprime") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)), // Gold/Amber-700
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("👑 Log In as AuraSuprime (Superuser)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Row 1: Executive Team
                Text("EXECUTIVE ROLES (NO DELETE ACCESS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.loginAsDemo("Owner", selectedHotelId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), // Red
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Owner", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.loginAsDemo("General Manager", selectedHotelId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)), // Blue
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("GM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.loginAsDemo("Department Head", selectedHotelId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Green
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Dept Head", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2 & 3: Department Staff Roles
                Text("DEPARTMENT STAFF ROLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.loginAsDemo("Receptionist", selectedHotelId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)), // Indigo
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Receptionist", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.loginAsDemo("Housekeeping", selectedHotelId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)), // Amber
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Housekeeping", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.loginAsDemo("Security", selectedHotelId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)), // Slate
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Security", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.loginAsDemo("Kitchen Staff", selectedHotelId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)), // Pink
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Kitchen", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.loginAsDemo("Maintenance", selectedHotelId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6)), // Teal
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Repairs", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
        }
    }
}
