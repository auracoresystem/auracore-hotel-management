package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrScreen(
    viewModel: HrViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val currentHotel by authViewModel.currentHotel.collectAsStateWithLifecycle()
    val registeredUsers by authViewModel.registeredUsers.collectAsStateWithLifecycle()
    val leaveRequests by viewModel.leaveRequests.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("database") } // "database" or "leave"
    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("All") }
    var showCheckInDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var revealedPasswords by remember { mutableStateOf(setOf<String>()) }

    // Filter approved staff members for the current hotel
    val approvedStaff = remember(registeredUsers, currentHotel, searchQuery, selectedDepartment) {
        registeredUsers.filter { user ->
            val matchesHotel = currentHotel == null || user.hotelId == currentHotel?.id
            val isApproved = user.status == "Approved"
            val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) || 
                                user.staffId.contains(searchQuery, ignoreCase = true) || 
                                user.phone.contains(searchQuery, ignoreCase = true)
            
            val dept = when (user.role) {
                "Receptionist" -> "Front Office"
                "Housekeeping" -> "Housekeeping"
                "Security" -> "Security"
                "Kitchen Staff" -> "Kitchen"
                "Maintenance" -> "Maintenance"
                "General Manager", "Department Head" -> "Management"
                else -> "Other"
            }
            val matchesDept = selectedDepartment == "All" || dept == selectedDepartment

            matchesHotel && isApproved && matchesSearch && matchesDept
        }
    }

    BaseScreen(
        title = "HR & Operations Suite",
        onBackClick = onBackClick
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF1F5F9)) // Smooth light background
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RoyalBlue)
            }

            // Tab Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0)),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = "database" }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = if (activeTab == "database") RoyalBlue else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Staff ID Database",
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "database") RoyalBlue else Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    if (activeTab == "database") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.6f)
                                .height(3.dp)
                                .background(RoyalBlue, shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = "leave" }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = if (activeTab == "leave") RoyalBlue else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leave & Attendance",
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "leave") RoyalBlue else Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    if (activeTab == "leave") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.6f)
                                .height(3.dp)
                                .background(RoyalBlue, shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                }
            }

            if (activeTab == "database") {
                // --- STAFF DATABASE SPREADSHEET SCREEN ---
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Card with Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Excel ID Directory",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "${approvedStaff.size} Approved Staff Members",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        // Export Button
                        Button(
                            onClick = {
                                if (approvedStaff.isEmpty()) {
                                    android.widget.Toast.makeText(context, "No approved staff to export!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    val csvBuilder = StringBuilder()
                                    csvBuilder.append("Sr No.,Staff ID,Name,Role,Department,Mobile,Email,Password,Status\n")
                                    approvedStaff.forEachIndexed { idx, staff ->
                                        val dept = when (staff.role) {
                                            "Receptionist" -> "Front Office"
                                            "Housekeeping" -> "Housekeeping"
                                            "Security" -> "Security"
                                            "Kitchen Staff" -> "Kitchen"
                                            "Maintenance" -> "Maintenance"
                                            "General Manager", "Department Head" -> "Management"
                                            else -> "Other"
                                        }
                                        csvBuilder.append("${idx + 1},${staff.staffId},${staff.name},${staff.role},$dept,${staff.phone},${staff.email},${staff.password},Approved\n")
                                    }
                                    
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Staff ID Database - Excel Format")
                                        putExtra(android.content.Intent.EXTRA_TEXT, csvBuilder.toString())
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Staff Sheet via"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Excel Green
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export to Excel", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search and Filter Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, ID or phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Department Horizontal Chips Filter
                    val departments = listOf("All", "Front Office", "Housekeeping", "Security", "Kitchen", "Maintenance", "Management")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        departments.forEach { dept ->
                            val isSelected = selectedDepartment == dept
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDepartment = dept },
                                label = { Text(dept, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color.DarkGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // SPREADSHEET CARD GRID
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        if (approvedStaff.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No matching approved staff found.", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Scrollable Excel Sheet Table container
                            Box(modifier = Modifier.fillMaxSize()) {
                                val horizontalScrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .horizontalScroll(horizontalScrollState)
                                ) {
                                    // Spreadsheet Header Row
                                    Row(
                                        modifier = Modifier
                                            .background(Color(0xFFE2E8F0))
                                            .padding(vertical = 12.dp)
                                    ) {
                                        TableHeaderCell("Sr No.", width = 60.dp)
                                        TableHeaderCell("Unique ID", width = 140.dp)
                                        TableHeaderCell("Staff Name", width = 160.dp)
                                        TableHeaderCell("Role / Position", width = 140.dp)
                                        TableHeaderCell("Department", width = 120.dp)
                                        TableHeaderCell("Mobile Number", width = 130.dp)
                                        TableHeaderCell("Email ID", width = 180.dp)
                                        TableHeaderCell("Password", width = 110.dp)
                                        TableHeaderCell("Actions", width = 120.dp)
                                    }

                                    // Spreadsheet Data Rows
                                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                        itemsIndexed(approvedStaff) { index, staff ->
                                            val isEven = index % 2 == 0
                                            val rowBg = if (isEven) Color(0xFFF8FAFC) else Color.White
                                            val dept = when (staff.role) {
                                                "Receptionist" -> "Front Office"
                                                "Housekeeping" -> "Housekeeping"
                                                "Security" -> "Security"
                                                "Kitchen Staff" -> "Kitchen"
                                                "Maintenance" -> "Maintenance"
                                                "General Manager", "Department Head" -> "Management"
                                                else -> "Other"
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .background(rowBg)
                                                    .border(0.5.dp, Color(0xFFE2E8F0))
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TableCell("${index + 1}", width = 60.dp, isCenter = true)
                                                
                                                // ID Badge Cell
                                                Box(
                                                    modifier = Modifier
                                                        .width(140.dp)
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Surface(
                                                        color = RoyalBlue.copy(alpha = 0.1f),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f)),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = staff.staffId,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = RoyalBlue,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }

                                                TableCell(staff.name, width = 160.dp, isBold = true)
                                                TableCell(staff.role, width = 140.dp)
                                                TableCell(dept, width = 120.dp)
                                                TableCell(staff.phone, width = 130.dp)
                                                TableCell(staff.email, width = 180.dp)
                                                
                                                // Password Cell with Toggle eye
                                                val isRevealed = revealedPasswords.contains(staff.id)
                                                Row(
                                                    modifier = Modifier.width(110.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = if (isRevealed) staff.password else "••••",
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 12.sp,
                                                        modifier = Modifier.padding(start = 12.dp)
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            revealedPasswords = if (isRevealed) {
                                                                revealedPasswords - staff.id
                                                            } else {
                                                                revealedPasswords + staff.id
                                                            }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp),
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                }

                                                // Copy Action Cell
                                                Row(
                                                    modifier = Modifier.width(120.dp),
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            val details = "Hotel: ${currentHotel?.name ?: "Hotel"}\nStaff: ${staff.name}\nRole: ${staff.role}\nDepartment: $dept\nStaff ID: ${staff.staffId}\nPassword: ${staff.password}"
                                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                            val clip = android.content.ClipData.newPlainText("Staff Credentials", details)
                                                            clipboard.setPrimaryClip(clip)
                                                            android.widget.Toast.makeText(context, "Details copied for ${staff.name}!", android.widget.Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ContentCopy,
                                                            contentDescription = "Copy details",
                                                            tint = RoyalBlue,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // --- LEAVE & ATTENDANCE ACTIONS SCREEN ---
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showCheckInDialog = true }, 
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("Check In Attendance")
                        }
                        Button(
                            onClick = { showLeaveDialog = true }, 
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("Request Leave")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Recent Leave Requests", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(leaveRequests) { request ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(request.employeeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Reason: ${request.reason}", color = Color.Gray, fontSize = 12.sp)
                                        Text("Emp ID: ${request.employeeId}", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text(
                                        request.status,
                                        color = if (request.status == "Approved") Color(0xFF10B981) else if (request.status == "Rejected") Color(0xFFEF4444) else Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
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
                    label = { Text("Employee ID / Unique Staff ID") },
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = empId, onValueChange = { empId = it }, label = { Text("Unique Staff ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Staff Name") }, modifier = Modifier.fillMaxWidth())
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

@Composable
fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 12.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = Color(0xFF475569)
        )
    }
}

@Composable
fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, isBold: Boolean = false, isCenter: Boolean = false) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = if (isCenter) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            color = Color(0xFF334155),
            maxLines = 1
        )
    }
}
