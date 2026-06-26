package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Gold
import com.example.ui.theme.RoyalBlue

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import com.example.ui.DashboardViewModel
import com.example.ui.DashboardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    onNavigateToKitchenWastage: () -> Unit,
    onNavigateToReception: () -> Unit,
    onNavigateToCleaning: () -> Unit,
    onNavigateToRepairs: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToHr: () -> Unit,
    onNavigateToLaundry: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToHub: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    val dashboardState by dashboardViewModel.dashboardState.collectAsStateWithLifecycle()
    val isRefreshing = dashboardState is DashboardState.Loading
    
    Scaffold(
        topBar = {
            // Custom App Bar matching HTML
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        color = RoyalBlue,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AURACORE PREMIUM",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Grand Plaza Ops",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                                .clickable {
                                    onLogout()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                                .clickable { onNavigateToNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                                    .background(Gold, shape = CircleShape)
                                    .border(2.dp, RoyalBlue, CircleShape)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Bottom Navigation from HTML
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(Icons.Default.Dashboard, "Home", isSelected = true, onClick = {})
                    BottomNavItem(Icons.Default.Groups, "Staff", isSelected = false, onClick = onNavigateToStaff)
                    
                    // Center Add Button
                    Box(
                        modifier = Modifier
                            .offset(y = (-24).dp)
                            .size(56.dp)
                            .shadow(8.dp, shape = CircleShape, ambientColor = RoyalBlue, spotColor = RoyalBlue)
                            .background(RoyalBlue, shape = CircleShape)
                            .border(4.dp, Color(0xFFF8FAFC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    
                    BottomNavItem(Icons.Default.Analytics, "Reports", isSelected = false, onClick = onNavigateToReports)
                    BottomNavItem(Icons.Default.Settings, "Profile", isSelected = false, onClick = onNavigateToProfile)
                }
            }
        },
        containerColor = Color(0xFFF8FAFC) // Tailwind bg-slate-50
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { dashboardViewModel.loadDashboardData() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when (val state = dashboardState) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = RoyalBlue)
                }
                is DashboardState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                is DashboardState.Success -> {
                    val data = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Key Metrics Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(modifier = Modifier.weight(1f), title = "Occupancy", value = data.occupancy, subtitle = "Live", icon = Icons.Default.Bed, iconColor = RoyalBlue)
                            StatCard(modifier = Modifier.weight(1f), title = "Revenue", value = data.revenue, subtitle = "Today", icon = Icons.Default.Payments, iconColor = Gold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(modifier = Modifier.weight(1f), title = "Attendance", value = data.attendance, subtitle = "Staff", icon = Icons.Default.Groups, iconColor = Color(0xFF16A34A))
                            StatCard(modifier = Modifier.weight(1f), title = "Tasks", value = data.pendingTasks, subtitle = "Pending", icon = Icons.Default.Build, iconColor = Color(0xFFEA580C))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(modifier = Modifier.weight(1f), title = "Wastage", value = data.kitchenWastage, subtitle = "Kitchen", icon = Icons.Default.Delete, iconColor = Color(0xFFDC2626))
                            StatCard(modifier = Modifier.weight(1f), title = "Alerts", value = data.inventoryAlerts, subtitle = "Inventory", icon = Icons.Default.PriorityHigh, iconColor = Color(0xFF9333EA))
                        }

                        // Operations Quick Access
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "QUICK MODULES",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B), // slate-800
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "View All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "Reception",
                                    icon = Icons.Default.RoomService,
                                    bgColor = Color(0xFFE0E7FF),
                                    iconBgColor = RoyalBlue,
                                    textColor = RoyalBlue,
                                    onClick = onNavigateToReception
                                )
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "Cleaning",
                                    icon = Icons.Default.CleaningServices,
                                    bgColor = Color(0xFFFEF3C7),
                                    iconBgColor = Gold,
                                    textColor = Color(0xFF92400E),
                                    onClick = onNavigateToCleaning
                                )
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "Wastage",
                                    icon = Icons.Default.Delete,
                                    bgColor = Color(0xFFDCFCE7),
                                    iconBgColor = Color(0xFF16A34A), // green-600
                                    textColor = Color(0xFF166534), // green-800
                                    onClick = onNavigateToKitchenWastage
                                )
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "Repairs",
                                    icon = Icons.Default.Build,
                                    bgColor = Color(0xFFFFEDD5),
                                    iconBgColor = Color(0xFFEA580C), // orange-600
                                    textColor = Color(0xFF9A3412), // orange-800
                                    onClick = onNavigateToRepairs
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "Inventory",
                                    icon = Icons.Default.ShoppingCart,
                                    bgColor = Color(0xFFF3E8FF),
                                    iconBgColor = Color(0xFF9333EA), // purple-600
                                    textColor = Color(0xFF6B21A8), // purple-800
                                    onClick = onNavigateToInventory
                                )
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "HR & Atten.",
                                    icon = Icons.Default.Person,
                                    bgColor = Color(0xFFE0F2FE),
                                    iconBgColor = Color(0xFF0284C7), // light blue-600
                                    textColor = Color(0xFF075985), // light blue-800
                                    onClick = onNavigateToHr
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "Security",
                                    icon = Icons.Default.Lock,
                                    bgColor = Color(0xFFF1F5F9),
                                    iconBgColor = Color(0xFF475569), // slate-600
                                    textColor = Color(0xFF1E293B), // slate-800
                                    onClick = onNavigateToSecurity
                                )
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "Core Team",
                                    icon = Icons.Default.Groups,
                                    bgColor = Color(0xFFFCE7F3),
                                    iconBgColor = Color(0xFFDB2777), // pink-600
                                    textColor = Color(0xFF831843), // pink-800
                                    onClick = onNavigateToHub
                                )
                                ModuleCardItem(
                                    modifier = Modifier.weight(1f),
                                    title = "Laundry",
                                    icon = Icons.Default.Done,
                                    bgColor = Color(0xFFFEF9C3),
                                    iconBgColor = Color(0xFFCA8A04), // yellow-600
                                    textColor = Color(0xFF854D0E), // yellow-800
                                    onClick = onNavigateToLaundry
                                )
                            }
                        }

                        // Critical Alerts Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), shape = RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "LIVE ALERTS",
                                            color = Gold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(
                                            text = "${data.complaints} New Complaints reported today.",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Italic,
                                            lineHeight = 22.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(RoyalBlue, shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Text("LVL-1", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        Text("Notifications: ${data.notifications}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("Check notifications log", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) RoyalBlue else Color(0xFF94A3B8) // slate-400
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = color)
        Text(label, color = color, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, subtitle: String, icon: ImageVector, iconColor: Color) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title.uppercase(), color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color(0xFF0F172A), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(subtitle, color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun ModuleCardItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    bgColor: Color,
    iconBgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgColor, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("module_${title.replace(" ", "_").lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(iconBgColor, shape = RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

