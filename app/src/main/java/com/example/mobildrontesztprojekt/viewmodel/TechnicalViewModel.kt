package com.example.mobildrontesztprojekt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobildrontesztprojekt.data.database.AppDatabase
import com.example.mobildrontesztprojekt.data.entity.*
import kotlinx.coroutines.launch

class TechnicalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val shelfLocations = db.shelfLocationDao().getAll()
    val inventoryItems = db.inventoryItemDao().getAll()
    val drones         = db.droneDao().getAll()
    val companies      = db.companyDao().getAll()
    val uwbNodes       = db.uwbNodeDao().getAll()
    val allShelfRows   = db.shelfRowDao().getAll()
    val corridors      = db.corridorDao().getAll()
    val warehouses     = db.warehouseDao().getAll()

    fun getDroneBases()     = db.uwbNodeDao().getByType(NodeType.DRONE_BASE)
    fun getDropPoints()     = db.uwbNodeDao().getByType(NodeType.DROP_POINT)
    fun getAnchors()        = db.uwbNodeDao().getByType(NodeType.ANCHOR)
    fun getNodesByWarehouse(wId: Long) = db.uwbNodeDao().getByWarehouse(wId)

    // ShelfLocation (méretek + sor + horgony frissítése)
    fun updateShelf(s: ShelfLocation) = viewModelScope.launch { db.shelfLocationDao().update(s) }

    // InventoryItem (méret frissítése)
    fun updateItem(i: InventoryItem)  = viewModelScope.launch { db.inventoryItemDao().update(i) }

    // Drones
    fun insertDrone(d: Drone) = viewModelScope.launch { db.droneDao().insert(d) }
    fun updateDrone(d: Drone) = viewModelScope.launch { db.droneDao().update(d) }
    fun deleteDrone(d: Drone) = viewModelScope.launch { db.droneDao().delete(d) }

    // UWB Nodes
    fun insertUWBNode(n: UWBNode) = viewModelScope.launch { db.uwbNodeDao().insert(n) }
    fun updateUWBNode(n: UWBNode) = viewModelScope.launch { db.uwbNodeDao().update(n) }
    fun deleteUWBNode(n: UWBNode) = viewModelScope.launch { db.uwbNodeDao().delete(n) }
}
