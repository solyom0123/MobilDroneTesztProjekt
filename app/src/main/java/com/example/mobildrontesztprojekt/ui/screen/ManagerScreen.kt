package com.example.mobildrontesztprojekt.ui.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobildrontesztprojekt.data.entity.*
import com.example.mobildrontesztprojekt.ui.theme.*
import com.example.mobildrontesztprojekt.viewmodel.ManagerViewModel
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerScreen(onBack: () -> Unit) {
    val vm: ManagerViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Raktárak", "Folyosók", "Polcsorok", "Polchelyek", "Készlet", "Munkák")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manager panel", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Vissza", tint = DroneSecondary) }
                },
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
                0 -> WarehousesTab(vm)
                1 -> CorridorsTab(vm)
                2 -> ShelfRowsTab(vm)
                3 -> ShelvesTab(vm)
                4 -> InventoryTab(vm)
                5 -> JobsTab(vm)
            }
        }
    }
}

// ─── WAREHOUSES ──────────────────────────────────────────────────────────────

@Composable
private fun WarehousesTab(vm: ManagerViewModel) {
    val warehouses by vm.warehouses.collectAsStateWithLifecycle(emptyList())
    val companies  by vm.companies.collectAsStateWithLifecycle(emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Warehouse?>(null) }
    var deleteTarget by remember { mutableStateOf<Warehouse?>(null) }

    if (showDialog) {
        WarehouseDialog(editTarget, companies,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { w -> if (editTarget == null) vm.insertWarehouse(w) else vm.updateWarehouse(w); showDialog = false; editTarget = null })
    }
    deleteTarget?.let { ConfirmDeleteDialog(it.name, { vm.deleteWarehouse(it); deleteTarget = null }, { deleteTarget = null }) }

    CrudList(
        items = warehouses, keyFn = { it.id },
        fab = { editTarget = null; showDialog = true }
    ) { w ->
        val cn = companies.find { it.id == w.companyId }?.name ?: "?"
        MgmtCard(Icons.Filled.Warehouse, DroneWarning, w.name, w.address, cn,
            { editTarget = w; showDialog = true }, { deleteTarget = w })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WarehouseDialog(initial: Warehouse?, companies: List<Company>, onDismiss: () -> Unit, onSave: (Warehouse) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var companyId by remember { mutableStateOf(initial?.companyId ?: companies.firstOrNull()?.id ?: 0L) }
    var exp by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Raktár hozzáadása" else "Raktár szerkesztése") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DField("Raktár neve", name) { name = it }
                DField("Cím", address) { address = it }
                DropdownField("Cég", companies.find { it.id == companyId }?.name ?: "—", exp, { exp = it }) {
                    companies.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { companyId = c.id; exp = false }) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(Warehouse(initial?.id ?: 0, companyId, name, address)) }) { Text("Mentés", color = DroneSecondary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── CORRIDORS ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CorridorsTab(vm: ManagerViewModel) {
    val warehouses by vm.warehouses.collectAsStateWithLifecycle(emptyList())
    var selectedWh by remember { mutableStateOf<Warehouse?>(null) }
    var whExp by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Corridor?>(null) }
    var deleteTarget by remember { mutableStateOf<Corridor?>(null) }

    val corridors by remember(selectedWh) {
        selectedWh?.let { vm.getCorridorsByWarehouse(it.id) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    if (showDialog && selectedWh != null) {
        CorridorDialog(editTarget, selectedWh!!.id,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { c -> if (editTarget == null) vm.insertCorridor(c) else vm.updateCorridor(c); showDialog = false; editTarget = null })
    }
    deleteTarget?.let { ConfirmDeleteDialog(it.name, { vm.deleteCorridor(it); deleteTarget = null }, { deleteTarget = null }) }

    Column(Modifier.fillMaxSize()) {
        WarehousePicker(warehouses, selectedWh, whExp, { whExp = it }) { selectedWh = it }
        Box(Modifier.weight(1f)) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(corridors, key = { it.id }) { c ->
                    MgmtCard(Icons.Filled.Route, DroneSecondary, c.name,
                        "Start(${c.startX},${c.startY}) → End(${c.endX},${c.endY})",
                        "Szél: ${c.widthCm}cm  Mag: ${c.heightCm}cm  |  ${c.code}",
                        { editTarget = c; showDialog = true }, { deleteTarget = c })
                }
            }
            if (selectedWh != null) FAB(Modifier.align(Alignment.BottomEnd)) { editTarget = null; showDialog = true }
        }
    }
}

@Composable
private fun CorridorDialog(initial: Corridor?, warehouseId: Long, onDismiss: () -> Unit, onSave: (Corridor) -> Unit) {
    var name    by remember { mutableStateOf(initial?.name ?: "") }
    var code    by remember { mutableStateOf(initial?.code ?: "") }
    var startX  by remember { mutableStateOf(initial?.startX?.toString() ?: "0") }
    var startY  by remember { mutableStateOf(initial?.startY?.toString() ?: "0") }
    var endX    by remember { mutableStateOf(initial?.endX?.toString() ?: "0") }
    var endY    by remember { mutableStateOf(initial?.endY?.toString() ?: "0") }
    var width   by remember { mutableStateOf(initial?.widthCm?.toString() ?: "300") }
    var height  by remember { mutableStateOf(initial?.heightCm?.toString() ?: "500") }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Folyosó hozzáadása" else "Folyosó szerkesztése") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DField("Név", name) { name = it }
                DField("Kód", code) { code = it }
                Text("Kezdőpont (m)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("X", startX, Modifier.weight(1f)) { startX = it }
                    NumField("Y", startY, Modifier.weight(1f)) { startY = it }
                }
                Text("Végpont (m)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("X", endX, Modifier.weight(1f)) { endX = it }
                    NumField("Y", endY, Modifier.weight(1f)) { endY = it }
                }
                Text("Fizikai méretek", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("Szél. (cm)", width, Modifier.weight(1f)) { width = it }
                    NumField("Mag. (cm)", height, Modifier.weight(1f)) { height = it }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(Corridor(initial?.id ?: 0, warehouseId, name, code,
                startX.f, startY.f, endX.f, endY.f, width.f, height.f)) }) { Text("Mentés", color = DroneSecondary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── SHELF ROWS ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfRowsTab(vm: ManagerViewModel) {
    val warehouses by vm.warehouses.collectAsStateWithLifecycle(emptyList())
    var selectedWh by remember { mutableStateOf<Warehouse?>(null) }
    var whExp by remember { mutableStateOf(false) }
    var selectedCorridor by remember { mutableStateOf<Corridor?>(null) }
    var corrExp by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<ShelfRow?>(null) }
    var deleteTarget by remember { mutableStateOf<ShelfRow?>(null) }

    val corridors by remember(selectedWh) {
        selectedWh?.let { vm.getCorridorsByWarehouse(it.id) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    val rows by remember(selectedCorridor) {
        selectedCorridor?.let { vm.getShelfRowsByCorridor(it.id) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    if (showDialog && selectedCorridor != null) {
        ShelfRowDialog(editTarget, selectedCorridor!!.id,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { r -> if (editTarget == null) vm.insertShelfRow(r) else vm.updateShelfRow(r); showDialog = false; editTarget = null })
    }
    deleteTarget?.let { ConfirmDeleteDialog(it.code, { vm.deleteShelfRow(it); deleteTarget = null }, { deleteTarget = null }) }

    Column(Modifier.fillMaxSize()) {
        WarehousePicker(warehouses, selectedWh, whExp, { whExp = it }) { selectedWh = it; selectedCorridor = null }
        Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            DropdownField("Folyosó", selectedCorridor?.name ?: "Válassz folyosót…", corrExp, { corrExp = it }) {
                corridors.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { selectedCorridor = c; corrExp = false }) }
            }
        }
        Box(Modifier.weight(1f)) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(rows, key = { it.id }) { row ->
                    val sideLabel = if (row.side == ShelfSide.LEFT) "BAL oldal" else "JOBB oldal"
                    MgmtCard(Icons.Filled.TableRows, DroneAccent, row.code, row.description, sideLabel,
                        { editTarget = row; showDialog = true }, { deleteTarget = row })
                }
            }
            if (selectedCorridor != null) FAB(Modifier.align(Alignment.BottomEnd)) { editTarget = null; showDialog = true }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfRowDialog(initial: ShelfRow?, corridorId: Long, onDismiss: () -> Unit, onSave: (ShelfRow) -> Unit) {
    var code  by remember { mutableStateOf(initial?.code ?: "") }
    var desc  by remember { mutableStateOf(initial?.description ?: "") }
    var side  by remember { mutableStateOf(initial?.side ?: ShelfSide.LEFT) }
    var sideExp by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Polcsor hozzáadása" else "Polcsor szerkesztése") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DField("Kód", code) { code = it }
                DField("Leírás", desc) { desc = it }
                DropdownField("Oldal", if (side == ShelfSide.LEFT) "BAL oldal" else "JOBB oldal", sideExp, { sideExp = it }) {
                    DropdownMenuItem(text = { Text("BAL oldal") }, onClick = { side = ShelfSide.LEFT; sideExp = false })
                    DropdownMenuItem(text = { Text("JOBB oldal") }, onClick = { side = ShelfSide.RIGHT; sideExp = false })
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(ShelfRow(initial?.id ?: 0, corridorId, code, side, desc)) }) { Text("Mentés", color = DroneSecondary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── SHELVES ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelvesTab(vm: ManagerViewModel) {
    val warehouses  by vm.warehouses.collectAsStateWithLifecycle(emptyList())
    val allRows     by vm.allShelfRows.collectAsStateWithLifecycle(emptyList())
    var selectedWh  by remember { mutableStateOf<Warehouse?>(null) }
    var whExp by remember { mutableStateOf(false) }
    var showDialog  by remember { mutableStateOf(false) }
    var editTarget  by remember { mutableStateOf<ShelfLocation?>(null) }
    var deleteTarget by remember { mutableStateOf<ShelfLocation?>(null) }

    val shelves by remember(selectedWh) {
        selectedWh?.let { vm.getShelfLocations(it.id) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    if (showDialog && selectedWh != null) {
        ShelfDialog(editTarget, selectedWh!!.id, allRows,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { s -> if (editTarget == null) vm.insertShelf(s) else vm.updateShelf(s); showDialog = false; editTarget = null })
    }
    deleteTarget?.let { ConfirmDeleteDialog(it.code, { vm.deleteShelf(it); deleteTarget = null }, { deleteTarget = null }) }

    Column(Modifier.fillMaxSize()) {
        WarehousePicker(warehouses, selectedWh, whExp, { whExp = it }) { selectedWh = it }
        Box(Modifier.weight(1f)) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(shelves, key = { it.id }) { shelf ->
                    val rowCode = allRows.find { it.id == shelf.shelfRowId }?.code
                    MgmtCard(Icons.Filled.ViewInAr, DroneAccent, shelf.code,
                        "X:${shelf.posX}m Y:${shelf.posY}m Z:${shelf.posZ}m",
                        "${shelf.widthCm}×${shelf.heightCm}×${shelf.depthCm}cm  |  max ${shelf.maxWeightKg}kg${rowCode?.let { "  |  $it" } ?: ""}",
                        { editTarget = shelf; showDialog = true }, { deleteTarget = shelf })
                }
            }
            if (selectedWh != null) FAB(Modifier.align(Alignment.BottomEnd)) { editTarget = null; showDialog = true }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfDialog(initial: ShelfLocation?, warehouseId: Long, allRows: List<ShelfRow>, onDismiss: () -> Unit, onSave: (ShelfLocation) -> Unit) {
    var code      by remember { mutableStateOf(initial?.code ?: "") }
    var posX      by remember { mutableStateOf(initial?.posX?.toString() ?: "0.0") }
    var posY      by remember { mutableStateOf(initial?.posY?.toString() ?: "0.0") }
    var posZ      by remember { mutableStateOf(initial?.posZ?.toString() ?: "0.0") }
    var width     by remember { mutableStateOf(initial?.widthCm?.toString() ?: "120") }
    var height    by remember { mutableStateOf(initial?.heightCm?.toString() ?: "40") }
    var depth     by remember { mutableStateOf(initial?.depthCm?.toString() ?: "80") }
    var maxWeight by remember { mutableStateOf(initial?.maxWeightKg?.toString() ?: "150") }
    var rowId     by remember { mutableStateOf(initial?.shelfRowId) }
    var rowExp    by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Polchely hozzáadása" else "Polchely szerkesztése") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DField("Kód (pl. A-01-01)", code) { code = it }
                Text("UWB koordináták (méterben)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("X", posX, Modifier.weight(1f)) { posX = it }
                    NumField("Y", posY, Modifier.weight(1f)) { posY = it }
                    NumField("Z", posZ, Modifier.weight(1f)) { posZ = it }
                }
                Text("Fizikai méretek (cm)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("Szél.", width, Modifier.weight(1f)) { width = it }
                    NumField("Mag.", height, Modifier.weight(1f)) { height = it }
                    NumField("Mél.", depth, Modifier.weight(1f)) { depth = it }
                }
                NumField("Max. terhelés (kg)", maxWeight) { maxWeight = it }
                DropdownField("Polcsor", allRows.find { it.id == rowId }?.code ?: "— nincs —", rowExp, { rowExp = it }) {
                    DropdownMenuItem(text = { Text("— nincs —") }, onClick = { rowId = null; rowExp = false })
                    allRows.forEach { r -> DropdownMenuItem(text = { Text("${r.code} (${if (r.side == ShelfSide.LEFT) "bal" else "jobb"})") }, onClick = { rowId = r.id; rowExp = false }) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(ShelfLocation(initial?.id ?: 0, warehouseId, rowId, initial?.uwbAnchorId,
                code, posX.f, posY.f, posZ.f, width.f, height.f, depth.f, maxWeight.f)) }) { Text("Mentés", color = DroneSecondary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── INVENTORY ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryTab(vm: ManagerViewModel) {
    val warehouses by vm.warehouses.collectAsStateWithLifecycle(emptyList())
    var selectedWh by remember { mutableStateOf<Warehouse?>(null) }
    var whExp by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<InventoryItem?>(null) }
    var deleteTarget by remember { mutableStateOf<InventoryItem?>(null) }

    val items by remember(selectedWh) {
        selectedWh?.let { vm.getInventoryItems(it.id) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    if (showDialog && selectedWh != null) {
        InventoryDialog(editTarget, selectedWh!!.id,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { i -> if (editTarget == null) vm.insertItem(i) else vm.updateItem(i); showDialog = false; editTarget = null })
    }
    deleteTarget?.let { ConfirmDeleteDialog(it.name, { vm.deleteItem(it); deleteTarget = null }, { deleteTarget = null }) }

    Column(Modifier.fillMaxSize()) {
        WarehousePicker(warehouses, selectedWh, whExp, { whExp = it }) { selectedWh = it }
        Box(Modifier.weight(1f)) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.id }) { item ->
                    MgmtCard(Icons.Filled.Inventory2, DroneAccent, item.name,
                        "${item.sku} | ${item.widthCm}×${item.heightCm}×${item.depthCm}cm | ${item.weightKg}kg",
                        "Db: ${item.quantity}",
                        { editTarget = item; showDialog = true }, { deleteTarget = item })
                }
            }
            if (selectedWh != null) FAB(Modifier.align(Alignment.BottomEnd)) { editTarget = null; showDialog = true }
        }
    }
}

@Composable
private fun InventoryDialog(initial: InventoryItem?, warehouseId: Long, onDismiss: () -> Unit, onSave: (InventoryItem) -> Unit) {
    var name     by remember { mutableStateOf(initial?.name ?: "") }
    var sku      by remember { mutableStateOf(initial?.sku ?: "") }
    var width    by remember { mutableStateOf(initial?.widthCm?.toString() ?: "") }
    var height   by remember { mutableStateOf(initial?.heightCm?.toString() ?: "") }
    var depth    by remember { mutableStateOf(initial?.depthCm?.toString() ?: "") }
    var weight   by remember { mutableStateOf(initial?.weightKg?.toString() ?: "") }
    var quantity by remember { mutableStateOf(initial?.quantity?.toString() ?: "1") }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Termék hozzáadása" else "Termék szerkesztése") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DField("Termék neve", name) { name = it }
                DField("SKU / vonalkód", sku) { sku = it }
                Text("Méretek (cm) & Súly (kg)", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("Szél.", width, Modifier.weight(1f)) { width = it }
                    NumField("Mag.", height, Modifier.weight(1f)) { height = it }
                    NumField("Mél.", depth, Modifier.weight(1f)) { depth = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("Súly (kg)", weight, Modifier.weight(1f)) { weight = it }
                    NumField("Darabszám", quantity, Modifier.weight(1f)) { quantity = it }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(InventoryItem(initial?.id ?: 0, warehouseId, null, name, sku, width.f, height.f, depth.f, weight.f, quantity.toIntOrNull() ?: 0)) }) { Text("Mentés", color = DroneSecondary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── JOBS ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobsTab(vm: ManagerViewModel) {
    val jobs     by vm.jobs.collectAsStateWithLifecycle(emptyList())
    val drones   by vm.drones.collectAsStateWithLifecycle(emptyList())
    val users    by vm.users.collectAsStateWithLifecycle(emptyList())
    val allItems by vm.allItems.collectAsStateWithLifecycle(emptyList())
    var showDialog   by remember { mutableStateOf(false) }
    var editTarget   by remember { mutableStateOf<Job?>(null) }
    var deleteTarget by remember { mutableStateOf<Job?>(null) }
    var expandedJobId by remember { mutableStateOf<Long?>(null) }

    if (showDialog) {
        JobDialog(editTarget, drones, users,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { j -> if (editTarget == null) vm.insertJob(j) else vm.updateJob(j); showDialog = false; editTarget = null })
    }
    deleteTarget?.let { ConfirmDeleteDialog("Munka #${it.id}", { vm.deleteJob(it); deleteTarget = null }, { deleteTarget = null }) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(jobs, key = { it.id }) { job ->
                val drone = drones.find { it.id == job.droneId }
                val user  = users.find { it.id == job.assignedUserId }
                val isExp = expandedJobId == job.id
                val jobItems by vm.getJobItems(job.id).collectAsStateWithLifecycle(emptyList())
                JobCard(job, drone, user, isExp, jobItems, allItems,
                    { expandedJobId = if (isExp) null else job.id },
                    { editTarget = job; showDialog = true }, { deleteTarget = job },
                    { iId, qty -> vm.insertJobItem(JobItem(jobId = job.id, inventoryItemId = iId, quantity = qty)) },
                    { vm.deleteJobItem(it) })
            }
        }
        FAB(Modifier.align(Alignment.BottomEnd)) { editTarget = null; showDialog = true }
    }
}

@Composable
private fun JobCard(
    job: Job, drone: Drone?, user: User?, isExpanded: Boolean,
    jobItems: List<JobItem>, allItems: List<InventoryItem>,
    onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit,
    onAddItem: (Long, Int) -> Unit, onDeleteItem: (JobItem) -> Unit
) {
    val (statusLabel, statusColor) = when (job.status) {
        JobStatus.PENDING     -> "VÁRAKOZIK" to DroneWarning
        JobStatus.IN_PROGRESS -> "FOLYAMATBAN" to DroneSecondary
        JobStatus.COMPLETED   -> "KÉSZ" to DroneSuccess
        JobStatus.CANCELLED   -> "TÖRÖLVE" to DroneError
    }
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(job.createdAt))
    var showItemDialog by remember { mutableStateOf(false) }

    if (showItemDialog) {
        AddJobItemDialog(allItems, { showItemDialog = false }) { iId, qty -> onAddItem(iId, qty); showItemDialog = false }
    }

    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DroneSurface)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Assignment, null, tint = DroneSuccess, modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Munka #${job.id} — ${drone?.name ?: "?"}", fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                    Text("Hozzárendelve: ${user?.let { "${it.lastName} ${it.firstName}" } ?: "—"}", style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.6f))
                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = DroneOnSurface.copy(alpha = 0.4f))
                }
                StatusBadge(statusLabel, statusColor)
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) { Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = DroneSecondary) }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Edit, null, tint = DroneSecondary, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, null, tint = DroneError, modifier = Modifier.size(16.dp)) }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(Modifier.padding(top = 10.dp)) {
                    if (job.notes.isNotBlank()) { Text("Megjegyzés: ${job.notes}", style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.7f)); Spacer(Modifier.height(6.dp)) }
                    Text("Termékek:", style = MaterialTheme.typography.labelMedium, color = DroneSecondary)
                    jobItems.forEach { ji ->
                        val item = allItems.find { it.id == ji.inventoryItemId }
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• ${item?.name ?: "#${ji.inventoryItemId}"}", style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.8f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${ji.quantity} db", style = MaterialTheme.typography.labelSmall, color = DroneSecondary)
                                IconButton(onClick = { onDeleteItem(ji) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Filled.Close, null, tint = DroneError, modifier = Modifier.size(12.dp)) }
                            }
                        }
                    }
                    TextButton(onClick = { showItemDialog = true }) { Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Termék hozzáadása") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobDialog(initial: Job?, drones: List<Drone>, users: List<User>, onDismiss: () -> Unit, onSave: (Job) -> Unit) {
    var droneId        by remember { mutableStateOf(initial?.droneId ?: drones.firstOrNull()?.id ?: 0L) }
    var assignedUserId by remember { mutableStateOf(initial?.assignedUserId) }
    var status         by remember { mutableStateOf(initial?.status ?: JobStatus.PENDING) }
    var notes          by remember { mutableStateOf(initial?.notes ?: "") }
    var droneExp by remember { mutableStateOf(false) }
    var userExp  by remember { mutableStateOf(false) }
    var statExp  by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Munka hozzáadása" else "Munka szerkesztése") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownField("Dron", drones.find { it.id == droneId }?.name ?: "—", droneExp, { droneExp = it }) {
                    drones.forEach { d -> DropdownMenuItem(text = { Text("${d.name} (${d.status})") }, onClick = { droneId = d.id; droneExp = false }) }
                }
                DropdownField("Technikus", users.find { it.id == assignedUserId }?.let { "${it.lastName} ${it.firstName}" } ?: "— nincs —", userExp, { userExp = it }) {
                    DropdownMenuItem(text = { Text("— nincs —") }, onClick = { assignedUserId = null; userExp = false })
                    users.forEach { u -> DropdownMenuItem(text = { Text("${u.lastName} ${u.firstName} (${u.role})") }, onClick = { assignedUserId = u.id; userExp = false }) }
                }
                DropdownField("Státusz", status.name, statExp, { statExp = it }) {
                    JobStatus.entries.forEach { s -> DropdownMenuItem(text = { Text(s.name) }, onClick = { status = s; statExp = false }) }
                }
                DField("Megjegyzés", notes) { notes = it }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(Job(initial?.id ?: 0, droneId, assignedUserId, status, initial?.createdAt ?: System.currentTimeMillis(), notes)) }) { Text("Mentés", color = DroneSecondary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddJobItemDialog(availableItems: List<InventoryItem>, onDismiss: () -> Unit, onAdd: (Long, Int) -> Unit) {
    var selectedId by remember { mutableStateOf(availableItems.firstOrNull()?.id ?: 0L) }
    var quantity   by remember { mutableStateOf("1") }
    var exp        by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Termék hozzáadása") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownField("Termék", availableItems.find { it.id == selectedId }?.name ?: "—", exp, { exp = it }) {
                    availableItems.forEach { item -> DropdownMenuItem(text = { Text("${item.name} (${item.quantity} db)") }, onClick = { selectedId = item.id; exp = false }) }
                }
                NumField("Darabszám", quantity) { quantity = it }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(selectedId, quantity.toIntOrNull() ?: 1) }) { Text("Hozzáad", color = DroneSecondary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégsem") } })
}

// ─── SHARED HELPERS ──────────────────────────────────────────────────────────

@Composable
private fun MgmtCard(icon: ImageVector, iconColor: Color, title: String, subtitle: String, badge: String? = null, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DroneSurface)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = DroneOnSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DroneOnSurface.copy(alpha = 0.6f))
                badge?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = DroneSecondary) }
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Edit, null, tint = DroneSecondary, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, null, tint = DroneError, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
        Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun FAB(modifier: Modifier, onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick, modifier = modifier.padding(16.dp), containerColor = DronePrimary) {
        Icon(Icons.Filled.Add, null, tint = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(label: String, value: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(), colors = fieldColors())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WarehousePicker(warehouses: List<Warehouse>, selected: Warehouse?, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, onSelect: (Warehouse) -> Unit) {
    Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        DropdownField("Raktár", selected?.name ?: "Válassz raktárat…", expanded, onExpandedChange) {
            warehouses.forEach { w -> DropdownMenuItem(text = { Text(w.name) }, onClick = { onSelect(w); onExpandedChange(false) }) }
        }
    }
}

private fun <T> CrudList(items: List<T>, keyFn: (T) -> Any, fab: () -> Unit, itemContent: @Composable (T) -> Unit) {}

@Composable
private fun <T> CrudList(
    items: List<T>,
    keyFn: (T) -> Any,
    fab: () -> Unit,
    content: @Composable (T) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = keyFn) { content(it) }
        }
        FAB(Modifier.align(Alignment.BottomEnd)) { fab() }
    }
}

// Extension for concise float parsing
private val String.f: Float get() = toFloatOrNull() ?: 0f
