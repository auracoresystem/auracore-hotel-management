package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefCabinScreen(
    onBackClick: () -> Unit
) {
    var showCalculatorDialog by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chef Cabin", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Food Costing Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChefStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Today's Food Cost",
                        value = "28.5%",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconColor = Color(0xFF10B981)
                    )
                    ChefStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Wastage Cost",
                        value = "₹1,250",
                        icon = Icons.Default.AttachMoney,
                        iconColor = Color(0xFFEF4444)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recipe Cost Calculator", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                        Text("Calculate menu item pricing based on raw ingredient costs from the store.", fontSize = 13.sp, color = Color.Gray)
                        
                        Button(
                            onClick = { showCalculatorDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("Open Calculator")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fastfood, contentDescription = null, tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menu Engineering", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                        Text("Analyze which dishes are highly profitable (Stars) and which ones are underperforming (Dogs).", fontSize = 13.sp, color = Color.Gray)
                        
                        Button(
                            onClick = { showMenuDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text("Analyze Menu")
                        }
                    }
                }
            }
        }

        if (showCalculatorDialog) {
            RecipeCalculatorDialog(onDismiss = { showCalculatorDialog = false })
        }

        if (showMenuDialog) {
            MenuEngineeringDialog(onDismiss = { showMenuDialog = false })
        }
    }
}

@Composable
fun ChefStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(text = title, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun RecipeCalculatorDialog(onDismiss: () -> Unit) {
    var recipeName by remember { mutableStateOf("") }
    var ingredientCost by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recipe Cost Calculator (Demo)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = { recipeName = it },
                    label = { Text("Recipe Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredientCost,
                    onValueChange = { ingredientCost = it },
                    label = { Text("Total Ingredient Cost (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                val cost = ingredientCost.toDoubleOrNull() ?: 0.0
                val price = sellingPrice.toDoubleOrNull() ?: 0.0
                
                if (cost > 0 && price > 0) {
                    val foodCostPercentage = (cost / price) * 100
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Food Cost: %.1f%%".format(foodCostPercentage),
                        fontWeight = FontWeight.Bold,
                        color = if (foodCostPercentage > 30) Color.Red else Color(0xFF10B981)
                    )
                    if (foodCostPercentage > 30) {
                        Text("Target should be below 30%", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun MenuEngineeringDialog(onDismiss: () -> Unit) {
    val sampleMenu = listOf(
        Pair("Butter Chicken", "Star (High Profit, High Popularity)"),
        Pair("Truffle Pasta", "Puzzle (High Profit, Low Popularity)"),
        Pair("French Fries", "Plowhorse (Low Profit, High Popularity)"),
        Pair("Exotic Salad", "Dog (Low Profit, Low Popularity)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menu Engineering (Demo)") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sampleMenu) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.first, fontWeight = FontWeight.Bold)
                            Text(item.second, fontSize = 13.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

