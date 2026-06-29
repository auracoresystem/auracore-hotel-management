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
import androidx.lifecycle.viewmodel.compose.viewModel

data class RevenueEntry(
    val id: String = "",
    val source: String = "",
    val description: String = "",
    val amountWithoutGst: Double = 0.0,
    val gstAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val date: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(
    onBackClick: () -> Unit,
    viewModel: RevenueViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val revenueEntries by viewModel.revenueEntries.collectAsState()
    
    val items by viewModel.items.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    val filteredEntries = when(selectedTab) {
        0 -> revenueEntries
        1 -> revenueEntries.filter { it.source == "Room" }
        2 -> revenueEntries.filter { it.source == "Restaurant" }
        3 -> revenueEntries.filter { it.source == "Event" }
        else -> revenueEntries
    }

    val totalSale = filteredEntries.sumOf { it.totalAmount }
    val totalWithoutGst = filteredEntries.sumOf { it.amountWithoutGst }
    val totalGst = filteredEntries.sumOf { it.gstAmount }

    val totalStockValue = items.sumOf { it.currentStock * it.unitPrice }
    val totalPurchases = purchases.sumOf { it.totalAmount }
    val totalExpenses = expenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financials & Analytics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All Revenue", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Rooms", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Restaurant", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Events", fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Store Analytics", fontWeight = FontWeight.SemiBold) })
            }

            if (selectedTab == 4) {
                // Store Analytics View
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Store Financial Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard(title = "Total Purchases", amount = "₹${totalPurchases}", modifier = Modifier.weight(1f), isPrimary = true)
                        MetricCard(title = "Total Expenses", amount = "₹${totalExpenses}", modifier = Modifier.weight(1f), isPrimary = false)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricCard(title = "Remaining Stock Value", amount = "₹${totalStockValue}", modifier = Modifier.fillMaxWidth(), isPrimary = true)
                }
            } else {
                // Revenue View
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
