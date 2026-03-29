package com.example.mobildrontesztprojekt.ui.screen

import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobildrontesztprojekt.data.entity.*
import com.example.mobildrontesztprojekt.ui.theme.*
import com.example.mobildrontesztprojekt.viewmodel.DroneControlViewModel
import com.example.mobildrontesztprojekt.viewmodel.DroneConnectionState
import com.example.mobildrontesztprojekt.viewmodel.SdkRegisterState
import com.example.mobildrontesztprojekt.viewmodel.TechnicalViewModel
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalScreen(onBack: () -> Unit) {
    val vm: TechnicalViewModel = viewModel()
    val controlVm: DroneControlViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Polc méretek", "Termék méretek", "Dronok", "Csomópontok", "Vezérlés")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Technikai panel", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Vissza", tint = DroneSecondary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DroneSurface)
            )
        },
        containerColor = DroneBackground
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = DroneSurface, contentColor = DroneSecondary, edgePadding = 0.dp) {
                tabs.forEachIndexed { i, t -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t) }) }
            }
            when (selectedTab) {
                0 -> ShelfDimensionsTab(vm)
                1 -> ProductSizesTab(vm)
                2 -> DronesTab(vm)
                3 -> UWBNodesTab(vm)
                4 -> DroneControlTab(controlVm, vm)
            }
        }
    }
}

// ─── POLC MÉRETEK ─────────────────────────────────────────────────────────────

@Composable
private fun ShelfDimensionsTab(vm: TechnicalViewModel) {
    val shelves   by vm.shelfLocations.collectAsStateWithLifecycle(emptyList())
    val allRows   by vm.allShelfRows.collectAsStateWithLifecycle(emptyList())
    val uwbNodes  by vm.uwbNodes.collectAsStateWithLifecycle(emptyList())
    var editTarget by remember { mutableStateOf<ShelfLocation?>(null) }

    editTarget?.let { shelf ->
        ShelfEditDialog(shelf, allRows, uwbNodes,
            onDismiss = { editTarget = null },
            onSave = { vm.updateShelf(it); editTarget = null })
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(shelves, key = { it.id }) { shelf ->
            val rowCode  = allRows.find { it.id == shelf.shelfRowId }?.code
            val anchorCode = uwbNodes.find { it.id == shelf.uwbAnchorId }?.code
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DroneSurface)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ViewInAr, null, tint = DroneAccent, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(shelf.code, fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                        Text("X:${shelf.posX}m  Y:${shelf.posY}m  Z:${shelf.posZ}m", style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.6f))
                        Text("${shelf.widthCm}×${shelf.heightCm}×${shelf.depthCm} cm  |  max ${shelf.maxWeightKg} kg", style = MaterialTheme.typography.labelSmall, color = DroneSecondary)
                        if (rowCode != null || anchorCode != null) {
                            Text("Sor: ${rowCode ?: "—"}  |  UWB: ${anchorCode ?: "—"}", style = MaterialTheme.typography.labelSmall, color = DroneAccent)
                        }
                    }
                    IconButton(onClick = { editTarget = shelf }) { Icon(Icons.Filled.Edit, null, tint = DroneSecondary) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfEditDialog(
    shelf: ShelfLocation,
    allRows: List<ShelfRow>,
    uwbNodes: List<UWBNode>,
    onDismiss: () -> Unit,
    onSave: (ShelfLocation) -> Unit
) {
    var posX      by remember { mutableStateOf(shelf.posX.toString()) }
    var posY      by remember { mutableStateOf(shelf.posY.toString()) }
    var posZ      by remember { mutableStateOf(shelf.posZ.toString()) }
    var width     by remember { mutableStateOf(shelf.widthCm.toString()) }
    var height    by remember { mutableStateOf(shelf.heightCm.toString()) }
    var depth     by remember { mutableStateOf(shelf.depthCm.toString()) }
    var maxWeight by remember { mutableStateOf(shelf.maxWeightKg.toString()) }
    var rowId     by remember { mutableStateOf(shelf.shelfRowId) }
    var anchorId  by remember { mutableStateOf(shelf.uwbAnchorId) }
    var rowExp    by remember { mutableStateOf(false) }
    var anchorExp by remember { mutableStateOf(false) }

    // Only show ANCHOR type nodes for anchor selection
    val anchors = uwbNodes.filter { it.nodeType == NodeType.ANCHOR }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Polchely: ${shelf.code}") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("UWB koordináták (méterben)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TNumField("X", posX, Modifier.weight(1f)) { posX = it }
                    TNumField("Y", posY, Modifier.weight(1f)) { posY = it }
                    TNumField("Z", posZ, Modifier.weight(1f)) { posZ = it }
                }
                Text("Fizikai méretek (cm)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TNumField("Szél.", width, Modifier.weight(1f)) { width = it }
                    TNumField("Mag.", height, Modifier.weight(1f)) { height = it }
                    TNumField("Mél.", depth, Modifier.weight(1f)) { depth = it }
                }
                TNumField("Max. terhelés (kg)", maxWeight) { maxWeight = it }

                Text("Kapcsolatok", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                TDropdownField("Polcsor", allRows.find { it.id == rowId }?.code ?: "— nincs —", rowExp, { rowExp = it }) {
                    DropdownMenuItem(text = { Text("— nincs —") }, onClick = { rowId = null; rowExp = false })
                    allRows.forEach { r -> DropdownMenuItem(text = { Text("${r.code} (${if (r.side == ShelfSide.LEFT) "bal" else "jobb"})") }, onClick = { rowId = r.id; rowExp = false }) }
                }
                TDropdownField("UWB Horgony", anchors.find { it.id == anchorId }?.let { "${it.code} (${it.posX},${it.posY},${it.posZ})" } ?: "— nincs —", anchorExp, { anchorExp = it }) {
                    DropdownMenuItem(text = { Text("— nincs —") }, onClick = { anchorId = null; anchorExp = false })
                    anchors.forEach { n -> DropdownMenuItem(text = { Text("${n.code}  X:${n.posX} Y:${n.posY} Z:${n.posZ}") }, onClick = { anchorId = n.id; anchorExp = false }) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(shelf.copy(
                posX = posX.f, posY = posY.f, posZ = posZ.f,
                widthCm = width.f, heightCm = height.f, depthCm = depth.f,
                maxWeightKg = maxWeight.f, shelfRowId = rowId, uwbAnchorId = anchorId)) }) { Text("Mentés", color = DroneSecondary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── TERMÉK MÉRETEK ───────────────────────────────────────────────────────────

@Composable
private fun ProductSizesTab(vm: TechnicalViewModel) {
    val items by vm.inventoryItems.collectAsStateWithLifecycle(emptyList())
    var editTarget by remember { mutableStateOf<InventoryItem?>(null) }

    editTarget?.let { item ->
        ProductSizeDialog(item, onDismiss = { editTarget = null }, onSave = { vm.updateItem(it); editTarget = null })
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.id }) { item ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DroneSurface)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Inventory2, null, tint = DroneWarning, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                        Text("SKU: ${item.sku}  |  Db: ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.6f))
                        Text("${item.widthCm}×${item.heightCm}×${item.depthCm} cm  |  ${item.weightKg} kg", style = MaterialTheme.typography.labelSmall, color = DroneSecondary)
                    }
                    IconButton(onClick = { editTarget = item }) { Icon(Icons.Filled.Edit, null, tint = DroneSecondary) }
                }
            }
        }
    }
}

@Composable
private fun ProductSizeDialog(item: InventoryItem, onDismiss: () -> Unit, onSave: (InventoryItem) -> Unit) {
    var width  by remember { mutableStateOf(item.widthCm.toString()) }
    var height by remember { mutableStateOf(item.heightCm.toString()) }
    var depth  by remember { mutableStateOf(item.depthCm.toString()) }
    var weight by remember { mutableStateOf(item.weightKg.toString()) }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Termék: ${item.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fizikai méretek (cm)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TNumField("Szél.", width, Modifier.weight(1f)) { width = it }
                    TNumField("Mag.", height, Modifier.weight(1f)) { height = it }
                    TNumField("Mél.", depth, Modifier.weight(1f)) { depth = it }
                }
                TNumField("Súly (kg)", weight) { weight = it }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(item.copy(widthCm = width.f, heightCm = height.f, depthCm = depth.f, weightKg = weight.f)) }) { Text("Mentés", color = DroneSecondary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── DRONOK ───────────────────────────────────────────────────────────────────

@Composable
private fun DronesTab(vm: TechnicalViewModel) {
    val drones    by vm.drones.collectAsStateWithLifecycle(emptyList())
    val companies by vm.companies.collectAsStateWithLifecycle(emptyList())
    val uwbNodes  by vm.uwbNodes.collectAsStateWithLifecycle(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget    by remember { mutableStateOf<Drone?>(null) }
    var deleteTarget  by remember { mutableStateOf<Drone?>(null) }

    if (showAddDialog || editTarget != null) {
        DroneDialog(editTarget, companies, uwbNodes,
            onDismiss = { showAddDialog = false; editTarget = null },
            onSave = { d -> if (editTarget == null) vm.insertDrone(d) else vm.updateDrone(d); showAddDialog = false; editTarget = null })
    }
    deleteTarget?.let { ConfirmDeleteDialog(it.name, { vm.deleteDrone(it); deleteTarget = null }, { deleteTarget = null }) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(drones, key = { it.id }) { drone ->
                val (statusLabel, statusColor) = when (drone.status) {
                    DroneStatus.IDLE        -> "SZABAD" to DroneSuccess
                    DroneStatus.ACTIVE      -> "AKTÍV" to DroneSecondary
                    DroneStatus.CHARGING    -> "TÖLTÉS" to DroneWarning
                    DroneStatus.MAINTENANCE -> "KARBANTARTÁS" to DroneError
                }
                val company  = companies.find { it.id == drone.companyId }
                val homeBase = uwbNodes.find { it.id == drone.homeBaseId }
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DroneSurface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Flight, null, tint = statusColor, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(drone.name, fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                            Text("${drone.model} | SN: ${drone.serialNumber}", style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.6f))
                            company?.let { Text(it.name, style = MaterialTheme.typography.labelSmall, color = DroneAccent) }
                            homeBase?.let { Text("Home: ${it.name} (${it.code})", style = MaterialTheme.typography.labelSmall, color = DroneSuccess) }
                        }
                        TStatusBadge(statusLabel, statusColor)
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { editTarget = drone }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Edit, null, tint = DroneSecondary, modifier = Modifier.size(16.dp)) }
                        IconButton(onClick = { deleteTarget = drone }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, null, tint = DroneError, modifier = Modifier.size(16.dp)) }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { editTarget = null; showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), containerColor = DronePrimary) {
            Icon(Icons.Filled.Add, null, tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DroneDialog(initial: Drone?, companies: List<Company>, uwbNodes: List<UWBNode>, onDismiss: () -> Unit, onSave: (Drone) -> Unit) {
    var name         by remember { mutableStateOf(initial?.name ?: "") }
    var model        by remember { mutableStateOf(initial?.model ?: "") }
    var serialNumber by remember { mutableStateOf(initial?.serialNumber ?: "") }
    var companyId    by remember { mutableStateOf(initial?.companyId ?: companies.firstOrNull()?.id ?: 0L) }
    var status       by remember { mutableStateOf(initial?.status ?: DroneStatus.IDLE) }
    var homeBaseId   by remember { mutableStateOf(initial?.homeBaseId) }
    var companyExp   by remember { mutableStateOf(false) }
    var statusExp    by remember { mutableStateOf(false) }
    var baseExp      by remember { mutableStateOf(false) }

    val droneBases = uwbNodes.filter { it.nodeType == NodeType.DRONE_BASE }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Dron hozzáadása" else "Dron szerkesztése") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TField("Dron neve", name) { name = it }
                TField("Modell", model) { model = it }
                TField("Sorozatszám", serialNumber) { serialNumber = it }
                TDropdownField("Cég", companies.find { it.id == companyId }?.name ?: "—", companyExp, { companyExp = it }) {
                    companies.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { companyId = c.id; companyExp = false }) }
                }
                TDropdownField("Státusz", status.name, statusExp, { statusExp = it }) {
                    DroneStatus.entries.forEach { s -> DropdownMenuItem(text = { Text(s.name) }, onClick = { status = s; statusExp = false }) }
                }
                TDropdownField("Home Base (UWB)", droneBases.find { it.id == homeBaseId }?.let { "${it.name} (${it.code})" } ?: "— nincs —", baseExp, { baseExp = it }) {
                    DropdownMenuItem(text = { Text("— nincs —") }, onClick = { homeBaseId = null; baseExp = false })
                    droneBases.forEach { b -> DropdownMenuItem(
                        text = { Column { Text(b.name, fontWeight = FontWeight.SemiBold); Text("${b.code}  X:${b.posX} Y:${b.posY}", style = MaterialTheme.typography.bodySmall) } },
                        onClick = { homeBaseId = b.id; baseExp = false }) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(Drone(initial?.id ?: 0, companyId, name, model, serialNumber, status, homeBaseId)) }) { Text("Mentés", color = DroneSecondary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── UWB CSOMÓPONTOK ──────────────────────────────────────────────────────────

@Composable
private fun UWBNodesTab(vm: TechnicalViewModel) {
    val nodes      by vm.uwbNodes.collectAsStateWithLifecycle(emptyList())
    val warehouses by vm.warehouses.collectAsStateWithLifecycle(emptyList())
    val corridors  by vm.corridors.collectAsStateWithLifecycle(emptyList())
    var showDialog  by remember { mutableStateOf(false) }
    var editTarget  by remember { mutableStateOf<UWBNode?>(null) }
    var deleteTarget by remember { mutableStateOf<UWBNode?>(null) }

    if (showDialog || editTarget != null) {
        UWBNodeDialog(editTarget, warehouses, corridors,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { n -> if (editTarget == null) vm.insertUWBNode(n) else vm.updateUWBNode(n); showDialog = false; editTarget = null })
    }
    deleteTarget?.let { ConfirmDeleteDialog(it.name, { vm.deleteUWBNode(it); deleteTarget = null }, { deleteTarget = null }) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(nodes, key = { it.id }) { node ->
                val (typeLabel, typeColor, typeIcon) = when (node.nodeType) {
                    NodeType.ANCHOR     -> Triple("HORGONY",   DroneSecondary, Icons.Filled.MyLocation)
                    NodeType.DROP_POINT -> Triple("LERAKÓHELY", DroneWarning,   Icons.Filled.MoveToInbox)
                    NodeType.DRONE_BASE -> Triple("DRON BASE",  DroneSuccess,    Icons.Filled.Home)
                }
                val corridor = corridors.find { it.id == node.corridorId }
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DroneSurface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(node.name, fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                            Text("${node.code}  |  X:${node.posX}m Y:${node.posY}m Z:${node.posZ}m", style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.6f))
                            corridor?.let { Text("Folyosó: ${it.name}", style = MaterialTheme.typography.labelSmall, color = DroneAccent) }
                        }
                        TStatusBadge(typeLabel, typeColor)
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { editTarget = node }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Edit, null, tint = DroneSecondary, modifier = Modifier.size(16.dp)) }
                        IconButton(onClick = { deleteTarget = node }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, null, tint = DroneError, modifier = Modifier.size(16.dp)) }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { editTarget = null; showDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), containerColor = DronePrimary) {
            Icon(Icons.Filled.Add, null, tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UWBNodeDialog(initial: UWBNode?, warehouses: List<Warehouse>, corridors: List<Corridor>, onDismiss: () -> Unit, onSave: (UWBNode) -> Unit) {
    var name        by remember { mutableStateOf(initial?.name ?: "") }
    var code        by remember { mutableStateOf(initial?.code ?: "") }
    var posX        by remember { mutableStateOf(initial?.posX?.toString() ?: "0.0") }
    var posY        by remember { mutableStateOf(initial?.posY?.toString() ?: "0.0") }
    var posZ        by remember { mutableStateOf(initial?.posZ?.toString() ?: "4.5") }
    var warehouseId by remember { mutableStateOf(initial?.warehouseId ?: warehouses.firstOrNull()?.id ?: 0L) }
    var corridorId  by remember { mutableStateOf(initial?.corridorId) }
    var nodeType    by remember { mutableStateOf(initial?.nodeType ?: NodeType.ANCHOR) }
    var whExp       by remember { mutableStateOf(false) }
    var corrExp     by remember { mutableStateOf(false) }
    var typeExp     by remember { mutableStateOf(false) }

    val filteredCorridors = corridors.filter { it.warehouseId == warehouseId }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Csomópont hozzáadása" else "Csomópont szerkesztése") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TField("Név", name) { name = it }
                TField("Kód", code) { code = it }
                Text("UWB pozíció (méterben)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TNumField("X", posX, Modifier.weight(1f)) { posX = it }
                    TNumField("Y", posY, Modifier.weight(1f)) { posY = it }
                    TNumField("Z", posZ, Modifier.weight(1f)) { posZ = it }
                }
                TDropdownField("Típus",
                    when (nodeType) { NodeType.ANCHOR -> "Horgony (ANCHOR)"; NodeType.DROP_POINT -> "Lerakóhely"; NodeType.DRONE_BASE -> "Dron Base" },
                    typeExp, { typeExp = it }) {
                    DropdownMenuItem(text = { Text("Horgony (ANCHOR)") }, onClick = { nodeType = NodeType.ANCHOR; typeExp = false })
                    DropdownMenuItem(text = { Text("Lerakóhely (DROP_POINT)") }, onClick = { nodeType = NodeType.DROP_POINT; typeExp = false })
                    DropdownMenuItem(text = { Text("Dron Base (DRONE_BASE)") }, onClick = { nodeType = NodeType.DRONE_BASE; typeExp = false })
                }
                TDropdownField("Raktár", warehouses.find { it.id == warehouseId }?.name ?: "—", whExp, { whExp = it }) {
                    warehouses.forEach { w -> DropdownMenuItem(text = { Text(w.name) }, onClick = { warehouseId = w.id; corridorId = null; whExp = false }) }
                }
                TDropdownField("Folyosó (opcionális)", filteredCorridors.find { it.id == corridorId }?.name ?: "— nincs —", corrExp, { corrExp = it }) {
                    DropdownMenuItem(text = { Text("— nincs —") }, onClick = { corridorId = null; corrExp = false })
                    filteredCorridors.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { corridorId = c.id; corrExp = false }) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(UWBNode(initial?.id ?: 0, warehouseId, corridorId, name, code, posX.f, posY.f, posZ.f, nodeType)) }) { Text("Mentés", color = DroneSecondary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── SHARED HELPERS ──────────────────────────────────────────────────────────

@Composable
private fun TStatusBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
        Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun TField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = fieldColors())
}

@Composable
private fun TNumField(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier.fillMaxWidth(), singleLine = true, colors = fieldColors())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TDropdownField(label: String, value: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(), colors = fieldColors())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, content = content)
    }
}

// ─── DRÓN VEZÉRLÉS TAB ────────────────────────────────────────────────────────

@Composable
fun DroneControlTab(controlVm: DroneControlViewModel, techVm: TechnicalViewModel) {
    val savedKey     by controlVm.savedAppKey.collectAsStateWithLifecycle(null)
    val sdkState     by controlVm.sdkState.collectAsStateWithLifecycle()
    val sdkError     by controlVm.sdkError.collectAsStateWithLifecycle()
    val connState    by controlVm.connectionState.collectAsStateWithLifecycle()
    val drones       by techVm.drones.collectAsStateWithLifecycle(emptyList())

    var selectedDrone by remember { mutableStateOf<Drone?>(null) }

    val droneAvailable = connState == DroneConnectionState.CONNECTED
    val sdkReady       = sdkState == SdkRegisterState.SUCCESS

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── 1. DJI App Key kártya ────────────────────────────────────────────
        AppKeyCard(
            savedKey   = savedKey?.djiAppKey,
            sdkState   = sdkState,
            sdkError   = sdkError,
            onSave     = { controlVm.saveAndRegisterKey(it) },
            onClear    = { controlVm.clearAppKey() }
        )

        // ── 2. Drón kiválasztás ──────────────────────────────────────────────
        DronePickerCard(
            drones        = drones,
            selected      = selectedDrone,
            sdkReady      = sdkReady,
            droneAvailable = droneAvailable,
            onSelect      = { selectedDrone = it }
        )

        // ── 3. Vezérlő + kamera panel (csak ha drón és SDK kész) ────────────
        if (selectedDrone != null && sdkReady) {
            if (droneAvailable) {
                DroneFlightPanel(controlVm = controlVm)
            } else {
                DroneOfflineCard(droneName = selectedDrone!!.name)
            }
        }
    }
}

// ─── App Key kártya ──────────────────────────────────────────────────────────

@Composable
private fun AppKeyCard(
    savedKey : String?,
    sdkState : SdkRegisterState,
    sdkError : String?,
    onSave   : (String) -> Unit,
    onClear  : () -> Unit
) {
    var inputKey    by remember { mutableStateOf(savedKey ?: "") }
    var showKey     by remember { mutableStateOf(false) }

    LaunchedEffect(savedKey) { if (savedKey != null) inputKey = savedKey }

    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DroneSurface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Key, null, tint = DroneAccent)
                Spacer(Modifier.width(8.dp))
                Text("DJI App Key", fontWeight = FontWeight.Bold, color = DroneOnSurface)
                Spacer(Modifier.weight(1f))
                // SDK állapot badge
                val (label, color) = when (sdkState) {
                    SdkRegisterState.IDLE        -> "Nincs regisztrálva" to DroneOnSurface.copy(0.4f)
                    SdkRegisterState.REGISTERING -> "Regisztrálás…" to DroneWarning
                    SdkRegisterState.SUCCESS     -> "SDK kész ✓" to DroneSuccess
                    SdkRegisterState.FAILED      -> "Hiba ✗" to DroneError
                }
                TStatusBadge(label, color)
            }

            OutlinedTextField(
                value         = inputKey,
                onValueChange = { inputKey = it },
                label         = { Text("App Key") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon  = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            null, tint = DroneSecondary
                        )
                    }
                },
                colors = fieldColors()
            )

            if (sdkError != null) {
                Text(
                    "Hiba: $sdkError",
                    color = DroneError,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = { onSave(inputKey) },
                    enabled  = inputKey.isNotBlank() && sdkState != SdkRegisterState.REGISTERING,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(containerColor = DronePrimary)
                ) {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Mentés & Aktiválás")
                }
                if (savedKey != null) {
                    OutlinedButton(onClick = onClear) {
                        Text("Törlés", color = DroneError)
                    }
                }
            }

            if (sdkState == SdkRegisterState.IDLE && savedKey == null) {
                Text(
                    "⚠ DJI App Key nélkül a drónvezérlés nem elérhető.\nRegisztrálj a developer.dji.com oldalon.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DroneWarning
                )
            }
        }
    }
}

// ─── Drón kiválasztó kártya ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DronePickerCard(
    drones         : List<Drone>,
    selected       : Drone?,
    sdkReady       : Boolean,
    droneAvailable : Boolean,
    onSelect       : (Drone) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DroneSurface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Flight, null, tint = DroneSecondary)
                Spacer(Modifier.width(8.dp))
                Text("Drón kiválasztása", fontWeight = FontWeight.Bold, color = DroneOnSurface)
                Spacer(Modifier.weight(1f))
                if (droneAvailable && selected != null) {
                    TStatusBadge("ELÉRHETŐ", DroneSuccess)
                } else if (selected != null) {
                    TStatusBadge("NEM ELÉRHETŐ", DroneError)
                }
            }

            ExposedDropdownMenuBox(
                expanded          = expanded && sdkReady,
                onExpandedChange  = { if (sdkReady) expanded = it }
            ) {
                OutlinedTextField(
                    value         = selected?.let { "${it.name}  (${it.model})" } ?: "Válassz drónт…",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Drón") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    enabled       = sdkReady,
                    modifier      = Modifier.menuAnchor().fillMaxWidth(),
                    colors        = fieldColors()
                )
                ExposedDropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    drones.forEach { d ->
                        val (statusLabel, statusColor) = when (d.status) {
                            DroneStatus.IDLE        -> "SZABAD"        to DroneSuccess
                            DroneStatus.ACTIVE      -> "AKTÍV"         to DroneSecondary
                            DroneStatus.CHARGING    -> "TÖLTÉS"        to DroneWarning
                            DroneStatus.MAINTENANCE -> "KARBANTARTÁS"  to DroneError
                        }
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Flight, null, tint = statusColor, modifier = Modifier.size(16.dp))
                                    Column {
                                        Text(d.name, fontWeight = FontWeight.SemiBold)
                                        Text(d.model, style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(0.6f))
                                    }
                                    Spacer(Modifier.weight(1f))
                                    TStatusBadge(statusLabel, statusColor)
                                }
                            },
                            onClick = { onSelect(d); expanded = false }
                        )
                    }
                }
            }

            if (!sdkReady) {
                Text(
                    "⚠ Add meg és aktiváld a DJI App Key-t a drón kiválasztásához.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DroneWarning
                )
            }
        }
    }
}

// ─── Drón offline kártya ──────────────────────────────────────────────────────

@Composable
private fun DroneOfflineCard(droneName: String) {
    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DroneSurface)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.FlightOff, null, tint = DroneError, modifier = Modifier.size(48.dp))
            Text(
                "$droneName",
                fontWeight = FontWeight.Bold,
                color = DroneOnSurface,
                fontSize = 18.sp
            )
            Text(
                "A drón jelenleg nem elérhető.\nEllenőrizd a DJI RC kapcsolatot és a drón bekapcsolt állapotát.",
                style = MaterialTheme.typography.bodyMedium,
                color = DroneOnSurface.copy(0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Drón repülési panel ──────────────────────────────────────────────────────

@Composable
private fun DroneFlightPanel(controlVm: DroneControlViewModel) {
    val vsEnabled  by controlVm.virtualStickEnabled.collectAsStateWithLifecycle()
    val flightData by controlVm.flightData.collectAsStateWithLifecycle()
    val camAvail   by controlVm.cameraAvailable.collectAsStateWithLifecycle()

    // ── Telemetria kártya ────────────────────────────────────────────────────
    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DroneSurface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Analytics, null, tint = DroneAccent)
                Spacer(Modifier.width(8.dp))
                Text("Telemetria", fontWeight = FontWeight.Bold, color = DroneOnSurface)
                Spacer(Modifier.weight(1f))
                TStatusBadge(if (flightData.isFlying) "REPÜL" else "FÖLDÖN",
                    if (flightData.isFlying) DroneSuccess else DroneSecondary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TelemetryItem("Magasság", "%.1f m".format(flightData.altitudeM), Icons.Filled.Height)
                TelemetryItem("Sebesség", "%.1f m/s".format(flightData.speedMs),   Icons.Filled.Speed)
                TelemetryItem("Akksi",    "${flightData.batteryPercent}%",          Icons.Filled.BatteryFull)
            }
        }
    }

    // ── Kamera nézet ─────────────────────────────────────────────────────────
    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            if (camAvail) {
                // TextureView-t AndroidView segítségével integráljuk
                val textureViewRef = remember { mutableStateOf<TextureView?>(null) }
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).also { tv ->
                            textureViewRef.value = tv
                            controlVm.startCameraStream(tv)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { tv -> controlVm.stopCameraStream(tv) }
                )
                // Kamera overlay – élő felirat
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("● ÉLŐKÉP", color = DroneError, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.VideocamOff, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Kamera nem elérhető", color = Color.Gray)
                }
            }
        }
    }

    // ── Virtual Stick kapcsoló ────────────────────────────────────────────────
    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DroneSurface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.SportsEsports, null, tint = DroneSecondary)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Manuális vezérlés", fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                Text(
                    if (vsEnabled) "Virtual Stick aktív – joystickkel vezérelheted a drónт" else "Engedélyezd a manuális vezérlés előtt",
                    style = MaterialTheme.typography.bodySmall,
                    color = DroneOnSurface.copy(0.6f)
                )
            }
            Switch(
                checked  = vsEnabled,
                onCheckedChange = { if (it) controlVm.enableVirtualStick() else controlVm.disableVirtualStick() },
                colors   = SwitchDefaults.colors(checkedThumbColor = DroneSuccess, checkedTrackColor = DroneSuccess.copy(0.4f))
            )
        }
    }

    // ── Joystick vezérlők ─────────────────────────────────────────────────────
    if (vsEnabled) {
        Card(
            shape  = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DroneSurface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Joystick",
                    fontWeight = FontWeight.Bold,
                    color = DroneOnSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    Modifier.fillMaxWidth().height(180.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bal stick – Throttle (fel/le) + Yaw (forgás)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gáz / Forgás", style = MaterialTheme.typography.labelSmall, color = DroneOnSurface.copy(0.6f))
                        Spacer(Modifier.height(4.dp))
                        JoystickWidget(
                            onStickMove = { dx, dy -> controlVm.updateLeftStick(-dy, dx) },
                            onRelease   = { controlVm.centerSticks() }
                        )
                    }
                    // Jobb stick – Pitch (előre/hátra) + Roll (oldalt)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Előre / Oldalt", style = MaterialTheme.typography.labelSmall, color = DroneOnSurface.copy(0.6f))
                        Spacer(Modifier.height(4.dp))
                        JoystickWidget(
                            onStickMove = { dx, dy -> controlVm.updateRightStick(-dy, dx) },
                            onRelease   = { controlVm.centerSticks() }
                        )
                    }
                }
                Text(
                    "Bal: gáz (fel/le) + forgás  |  Jobb: előre/hátra + oldalsó mozgás",
                    style = MaterialTheme.typography.labelSmall,
                    color = DroneOnSurface.copy(0.45f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

// ─── Joystick Widget ──────────────────────────────────────────────────────────

@Composable
private fun JoystickWidget(
    onStickMove : (dx: Float, dy: Float) -> Unit,
    onRelease   : () -> Unit,
    size        : Float = 140f
) {
    val halfSize = size / 2f
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius  = halfSize * 0.65f

    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(DroneBackground)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {},
                    onDragEnd   = {
                        knobOffset = Offset.Zero
                        onRelease()
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onRelease()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        knobOffset = if (dist <= maxRadius) newOffset
                        else newOffset / dist * maxRadius
                        onStickMove(
                            knobOffset.x / maxRadius,
                            knobOffset.y / maxRadius
                        )
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Háttér körök
        Box(
            Modifier
                .size((size * 0.6f).dp)
                .clip(CircleShape)
                .background(DroneSurface)
        )
        // Keresztvonal
        Divider(
            Modifier
                .width(size.dp)
                .align(Alignment.Center),
            color = DroneOnSurface.copy(0.1f), thickness = 1.dp
        )
        // Knob
        Box(
            Modifier
                .offset(
                    x = knobOffset.x.dp,
                    y = knobOffset.y.dp
                )
                .size(44.dp)
                .clip(CircleShape)
                .background(DroneSecondary)
        )
    }
}

// ─── Telemetria elem ──────────────────────────────────────────────────────────

@Composable
private fun TelemetryItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = DroneAccent, modifier = Modifier.size(20.dp))
        Text(value, fontWeight = FontWeight.Bold, color = DroneOnSurface, fontSize = 15.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = DroneOnSurface.copy(0.55f))
    }
}

// ─── ConfirmDeleteDialog (meglévő kódban is használt) ────────────────────────

@Composable
private fun ConfirmDeleteDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Törlés megerősítése") },
        text  = { Text("Biztosan törlöd: $name?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Törlés", color = DroneError) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = DroneSecondary,
    unfocusedBorderColor = DroneOnSurface.copy(alpha = 0.3f),
    focusedLabelColor    = DroneSecondary,
    cursorColor          = DroneSecondary
)

private val String.f: Float get() = toFloatOrNull() ?: 0f