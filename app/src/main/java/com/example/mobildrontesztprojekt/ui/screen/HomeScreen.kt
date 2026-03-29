package com.example.mobildrontesztprojekt.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobildrontesztprojekt.data.entity.User
import com.example.mobildrontesztprojekt.data.entity.UserRole
import com.example.mobildrontesztprojekt.ui.theme.*
import com.example.mobildrontesztprojekt.viewmodel.AuthViewModel

private data class DashboardCard(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val user = currentUser ?: return

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Kijelentkezés") },
            text = { Text("Biztosan ki szeretne jelentkezni?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(); onLogout() }) {
                    Text("Igen", color = DroneSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Mégsem") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "DRONTECH",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black, letterSpacing = 2.sp
                            )
                        )
                        Text(
                            "${user.lastName} ${user.firstName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = DroneSecondary)
                        )
                    }
                },
                actions = {
                    RoleBadge(user.role)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Filled.Logout, "Kijelentkezés", tint = DroneSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DroneSurface)
            )
        },
        containerColor = DroneBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            WelcomeBanner(user)
            Spacer(Modifier.height(20.dp))
            Text(
                "Funkciók",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.sp
                )
            )
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cardsForRole(user.role)) { card ->
                    DashboardCardItem(card, onNavigate)
                }
            }
        }
    }
}

@Composable
private fun WelcomeBanner(user: User) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(DronePrimary, Color(0xFF1A237E))))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Filled.Flight, null, tint = DroneAccent, modifier = Modifier.size(40.dp))
                Column {
                    Text("Üdvözöljük!", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)))
                    Text("${user.lastName} ${user.firstName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    val (label, color) = when (role) {
        UserRole.ADMIN     -> "ADMIN" to DroneError
        UserRole.MANAGER   -> "MANAGER" to DroneWarning
        UserRole.TECHNICAL -> "TECH" to DroneSuccess
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.2f)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
            )
        )
    }
}

@Composable
private fun DashboardCardItem(card: DashboardCard, onNavigate: (String) -> Unit) {
    Card(
        onClick = { card.route?.let { onNavigate(it) } },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DroneSurface),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        enabled = card.route != null
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(card.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(card.icon, null, tint = card.color, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(card.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (card.route != null) DroneOnSurface else DroneOnSurface.copy(alpha = 0.4f)
                    ))
                Text(card.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = DroneOnSurface.copy(alpha = 0.5f)))
            }
        }
    }
}

private fun cardsForRole(role: UserRole): List<DashboardCard> = when (role) {
    UserRole.ADMIN -> listOf(
        DashboardCard("Felhasználók", "CRUD + Céghez rendelés", Icons.Filled.People, DroneSecondary, "admin"),
        DashboardCard("Cégek", "Szervezetek kezelése", Icons.Filled.Business, DroneAccent, "admin"),
        DashboardCard("Raktárak", "Áttekintés", Icons.Filled.Warehouse, DroneWarning, "manager"),
        DashboardCard("Dronok", "Flotta", Icons.Filled.Flight, DronePrimary, "technical"),
        DashboardCard("Munkák", "Feladatok", Icons.Filled.Assignment, DroneSuccess, "manager"),
        DashboardCard("Beállítások", "Hamarosan", Icons.Filled.Settings, Color(0xFF9E9E9E), null)
    )
    UserRole.MANAGER -> listOf(
        DashboardCard("Raktárak", "Kezelés", Icons.Filled.Warehouse, DroneWarning, "manager"),
        DashboardCard("Polchelyek", "Koordináták", Icons.Filled.ViewInAr, DroneAccent, "manager"),
        DashboardCard("Készlet", "Termékek", Icons.Filled.Inventory, DroneSecondary, "manager"),
        DashboardCard("Munkák", "Dron feladatok", Icons.Filled.Assignment, DroneSuccess, "manager")
    )
    UserRole.TECHNICAL -> listOf(
        DashboardCard("Polc méretek", "Frissítés", Icons.Filled.ViewInAr, DroneAccent, "technical"),
        DashboardCard("Termék méretek", "Frissítés", Icons.Filled.Inventory2, DroneWarning, "technical"),
        DashboardCard("Dronok", "Kezelés + Felvétel", Icons.Filled.Flight, DronePrimary, "technical")
    )
}
