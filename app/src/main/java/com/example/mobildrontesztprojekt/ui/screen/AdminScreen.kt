package com.example.mobildrontesztprojekt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobildrontesztprojekt.data.entity.Company
import com.example.mobildrontesztprojekt.data.entity.User
import com.example.mobildrontesztprojekt.data.entity.UserRole
import com.example.mobildrontesztprojekt.ui.theme.*
import com.example.mobildrontesztprojekt.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit) {
    val vm: AdminViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin panel", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Vissza", tint = DroneSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DroneSurface)
            )
        },
        containerColor = DroneBackground
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DroneSurface,
                contentColor = DroneSecondary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Felhasználók") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Cégek") })
            }
            when (selectedTab) {
                0 -> UsersTab(vm)
                1 -> CompaniesTab(vm)
            }
        }
    }
}

// ─── USERS TAB ───────────────────────────────────────────────────────────────

@Composable
private fun UsersTab(vm: AdminViewModel) {
    val users by vm.users.collectAsStateWithLifecycle(emptyList())
    val companies by vm.companies.collectAsStateWithLifecycle(emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<User?>(null) }
    var deleteTarget by remember { mutableStateOf<User?>(null) }

    if (showDialog) {
        UserDialog(
            initial = editTarget,
            companies = companies,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { user ->
                if (editTarget == null) vm.insertUser(user) else vm.updateUser(user)
                showDialog = false; editTarget = null
            },
            hashPassword = vm::hashPassword
        )
    }
    deleteTarget?.let { u ->
        ConfirmDeleteDialog(name = "${u.lastName} ${u.firstName}",
            onConfirm = { vm.deleteUser(u); deleteTarget = null },
            onDismiss = { deleteTarget = null })
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(users, key = { it.id }) { user ->
                UserCard(user, companies,
                    onEdit = { editTarget = it; showDialog = true },
                    onDelete = { deleteTarget = it })
            }
        }
        FloatingActionButton(
            onClick = { editTarget = null; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = DronePrimary
        ) { Icon(Icons.Filled.PersonAdd, null, tint = Color.White) }
    }
}

@Composable
private fun UserCard(
    user: User,
    companies: List<Company>,
    onEdit: (User) -> Unit,
    onDelete: (User) -> Unit
) {
    val company = companies.find { it.id == user.companyId }
    val (roleLabel, roleColor) = when (user.role) {
        UserRole.ADMIN -> "ADMIN" to DroneError
        UserRole.MANAGER -> "MANAGER" to DroneWarning
        UserRole.TECHNICAL -> "TECH" to DroneSuccess
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DroneSurface)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AccountCircle, null, tint = DroneSecondary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${user.lastName} ${user.firstName}", fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.6f))
                company?.let {
                    Text(it.name, style = MaterialTheme.typography.labelSmall, color = DroneAccent)
                }
            }
            Surface(shape = RoundedCornerShape(6.dp), color = roleColor.copy(alpha = 0.15f)) {
                Text(roleLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(color = roleColor, fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onEdit(user) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Edit, null, tint = DroneSecondary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { onDelete(user) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Delete, null, tint = DroneError, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDialog(
    initial: User?,
    companies: List<Company>,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit,
    hashPassword: (String) -> String
) {
    var firstName   by remember { mutableStateOf(initial?.firstName ?: "") }
    var lastName    by remember { mutableStateOf(initial?.lastName ?: "") }
    var email       by remember { mutableStateOf(initial?.email ?: "") }
    var password    by remember { mutableStateOf("") }
    var pwVisible   by remember { mutableStateOf(false) }
    var role        by remember { mutableStateOf(initial?.role ?: UserRole.TECHNICAL) }
    var companyId   by remember { mutableStateOf(initial?.companyId) }
    var roleExpanded    by remember { mutableStateOf(false) }
    var companyExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Felhasználó hozzáadása" else "Felhasználó szerkesztése") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DField("Vezetéknév", lastName) { lastName = it }
                DField("Keresztnév", firstName) { firstName = it }
                DField("Email", email) { email = it }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (initial == null) "Jelszó" else "Új jelszó (üresen hagyható)") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { pwVisible = !pwVisible }) {
                            Icon(if (pwVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                        }
                    },
                    colors = fieldColors()
                )
                // Role dropdown
                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(
                        value = role.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jogosultság") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = fieldColors()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        UserRole.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.name) },
                                onClick = { role = r; roleExpanded = false }
                            )
                        }
                    }
                }
                // Company dropdown
                ExposedDropdownMenuBox(expanded = companyExpanded, onExpandedChange = { companyExpanded = it }) {
                    OutlinedTextField(
                        value = companies.find { it.id == companyId }?.name ?: "— nincs cég —",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cég") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = companyExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = fieldColors()
                    )
                    ExposedDropdownMenu(expanded = companyExpanded, onDismissRequest = { companyExpanded = false }) {
                        DropdownMenuItem(text = { Text("— nincs cég —") }, onClick = { companyId = null; companyExpanded = false })
                        companies.forEach { c ->
                            DropdownMenuItem(text = { Text(c.name) }, onClick = { companyId = c.id; companyExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val pwHash = if (password.isNotBlank()) hashPassword(password) else initial?.passwordHash ?: hashPassword("changeme")
                onSave(User(
                    id = initial?.id ?: 0,
                    firstName = firstName, lastName = lastName,
                    email = email, passwordHash = pwHash,
                    role = role, companyId = companyId
                ))
            }) { Text("Mentés", color = DroneSecondary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } }
    )
}

// ─── COMPANIES TAB ────────────────────────────────────────────────────────────

@Composable
private fun CompaniesTab(vm: AdminViewModel) {
    val companies by vm.companies.collectAsStateWithLifecycle(emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Company?>(null) }
    var deleteTarget by remember { mutableStateOf<Company?>(null) }

    if (showDialog) {
        CompanyDialog(
            initial = editTarget,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { c ->
                if (editTarget == null) vm.insertCompany(c) else vm.updateCompany(c)
                showDialog = false; editTarget = null
            }
        )
    }
    deleteTarget?.let { c ->
        ConfirmDeleteDialog(name = c.name,
            onConfirm = { vm.deleteCompany(c); deleteTarget = null },
            onDismiss = { deleteTarget = null })
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(companies, key = { it.id }) { company ->
                CompanyCard(company,
                    onEdit = { editTarget = it; showDialog = true },
                    onDelete = { deleteTarget = it })
            }
        }
        FloatingActionButton(
            onClick = { editTarget = null; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = DronePrimary
        ) { Icon(Icons.Filled.AddBusiness, null, tint = Color.White) }
    }
}

@Composable
private fun CompanyCard(company: Company, onEdit: (Company) -> Unit, onDelete: (Company) -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DroneSurface)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Business, null, tint = DroneAccent, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(company.name, fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                Text(company.address, style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.6f))
                Text(company.phone, style = MaterialTheme.typography.labelSmall, color = DroneSecondary)
            }
            IconButton(onClick = { onEdit(company) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Edit, null, tint = DroneSecondary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { onDelete(company) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Delete, null, tint = DroneError, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun CompanyDialog(initial: Company?, onDismiss: () -> Unit, onSave: (Company) -> Unit) {
    var name    by remember { mutableStateOf(initial?.name ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var phone   by remember { mutableStateOf(initial?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Cég hozzáadása" else "Cég szerkesztése") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DField("Cégnév", name) { name = it }
                DField("Cím", address) { address = it }
                DField("Telefonszám", phone) { phone = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(Company(id = initial?.id ?: 0, name = name, address = address, phone = phone))
            }) { Text("Mentés", color = DroneSecondary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } }
    )
}

// ─── SHARED HELPERS ──────────────────────────────────────────────────────────

@Composable
internal fun ConfirmDeleteDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Törlés megerősítése") },
        text = { Text("Biztosan törli: $name?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Törlés", color = DroneError) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } }
    )
}

@Composable
internal fun DField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = fieldColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DroneSecondary,
    focusedLabelColor = DroneSecondary
)
