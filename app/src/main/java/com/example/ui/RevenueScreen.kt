package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoyalBlue
import java.text.SimpleDateFormat
import java.util.*

data class RevenueEntry(
    val id: String,
    val source: String, // Room, Restaurant, Event, etc.
    val description: String,
    val amountWithoutGst: Double,
    val gstAmount: Double,
    val totalAmount: Double,
    val date: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Simulated dummy data
    val dummyEntries = remember {
        listOf(
            RevenueEntry("1", "Room", "Booking #1042", 5000.0, 600.0, 5600.0, System.currentTimeMillis()),
            RevenueEntry("2", "Restaurant", "Table #4", 1500.0, 75.0, 1575.0, System.currentTimeMillis() - 86400000),
            RevenueEntry("3", "Event", "Marriage Hall Advance", 50000.0, 9000.0, 59000.0, System.currentTimeMillis() - (86400000 * 2)),
            RevenueEntry("4", "Room", "Booking #1043", 3000.0, 360.0, 3360.0, System.currentTimeMillis() - (86400000 * 3)),
            RevenueEntry("5", "Restaurant", "Table #2", 800.0, 40.0, 840.0, System.currentTimeMillis() - (86400000 * 4))
        )
    }

    val filteredEntries = when(selectedTab) {
        0 -> dummyEntries
        1 -> dummyEntries.filter { it.source == "Room" }
        2 -> dummyEntries.filter { it.source == "Restaurant" }
        3 -> dummyEntries.filter { it.source == "Event" }
        else -> dummyEntries
    }

    val totalSale = filteredEntries.sumOf { it.totalAmount }
    val totalWithoutGst = filteredEntries.sumOf { it.amountWithoutGst }
    val totalGst = filteredEntries.sumOf { it.gstAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revenue & Sales", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { ToastManager.showToast("Exporting to Excel... ✅") }) {
                        Icon(Icons.Default.Download, contentDescription = "Export to Excel", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalBlue)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = RoyalBlue,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = RoyalBlue
                        )
                    }
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Total", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Rooms", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Restaurant", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Events", fontWeight = FontWeight.SemiBold) })
            }

            // Summary Cards
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(title = "Total (Inc. GST)", amount = "₹${totalSale}", modifier = Modifier.weight(1f), isPrimary = true)
                MetricCard(title = "Total (Ex. GST)", amount = "₹${totalWithoutGst}", modifier = Modifier.weight(1f), isPrimary = false)
            }
            
            Text(
                "Transaction Breakdown", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp, 
                color = RoyalBlue,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredEntries) { entry ->
                    RevenueEntryCard(entry)
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, amount: String, modifier: Modifier = Modifier, isPrimary: Boolean) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (isPrimary) RoyalBlue else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = if (isPrimary) Color.White.copy(alpha = 0.8f) else Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, color = if (isPrimary) Color.White else RoyalBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RevenueEntryCard(entry: RevenueEntry) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(entry.date))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("₹${entry.totalAmount}", fontWeight = FontWeight.Bold, color = RoyalBlue)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.source, color = Color.Gray, fontSize = 13.sp)
                Text(dateStr, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Amt: ₹${entry.amountWithoutGst}", color = Color.Gray, fontSize = 12.sp)
                Text("GST: ₹${entry.gstAmount}", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
