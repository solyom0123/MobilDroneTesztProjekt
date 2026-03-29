package com.example.mobildrontesztprojekt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobildrontesztprojekt.data.database.AppDatabase
import com.example.mobildrontesztprojekt.data.entity.*
import kotlinx.coroutines.launch

class ManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val warehouses  = db.warehouseDao().getAll()
    val companies   = db.companyDao().getAll()
    val drones      = db.droneDao().getAll()
    val users       = db.userDao().getAll()
    val jobs        = db.jobDao().getAll()
    val allItems    = db.inventoryItemDao().getAll()
    val corridors   = db.corridorDao().getAll()
    val allShelfRows = db.shelfRowDao().getAll()
    val allShelves  = db.shelfLocationDao().getAll()

    fun getCorridorsByWarehouse(wId: Long) = db.corridorDao().getByWarehouse(wId)
    fun getShelfRowsByCorridor(cId: Long)  = db.shelfRowDao().getByCorridor(cId)
    fun getShelfLocations(wId: Long)       = db.shelfLocationDao().getByWarehouse(wId)
    fun getInventoryItems(wId: Long)       = db.inventoryItemDao().getByWarehouse(wId)
    fun getJobItems(jobId: Long)           = db.jobDao().getItemsByJob(jobId)

    // Warehouse
    fun insertWarehouse(w: Warehouse) = viewModelScope.launch { db.warehouseDao().insert(w) }
    fun updateWarehouse(w: Warehouse) = viewModelScope.launch { db.warehouseDao().update(w) }
    fun deleteWarehouse(w: Warehouse) = viewModelScope.launch { db.warehouseDao().delete(w) }

    // Corridor
    fun insertCorridor(c: Corridor) = viewModelScope.launch { db.corridorDao().insert(c) }
    fun updateCorridor(c: Corridor) = viewModelScope.launch { db.corridorDao().update(c) }
    fun deleteCorridor(c: Corridor) = viewModelScope.launch { db.corridorDao().delete(c) }

    // ShelfRow
    fun insertShelfRow(r: ShelfRow) = viewModelScope.launch { db.shelfRowDao().insert(r) }
    fun updateShelfRow(r: ShelfRow) = viewModelScope.launch { db.shelfRowDao().update(r) }
    fun deleteShelfRow(r: ShelfRow) = viewModelScope.launch { db.shelfRowDao().delete(r) }

    // ShelfLocation
    fun insertShelf(s: ShelfLocation) = viewModelScope.launch { db.shelfLocationDao().insert(s) }
    fun updateShelf(s: ShelfLocation) = viewModelScope.launch { db.shelfLocationDao().update(s) }
    fun deleteShelf(s: ShelfLocation) = viewModelScope.launch { db.shelfLocationDao().delete(s) }

    // InventoryItem
    fun insertItem(i: InventoryItem) = viewModelScope.launch { db.inventoryItemDao().insert(i) }
    fun updateItem(i: InventoryItem) = viewModelScope.launch { db.inventoryItemDao().update(i) }
    fun deleteItem(i: InventoryItem) = viewModelScope.launch { db.inventoryItemDao().delete(i) }

    // Job
    fun insertJob(j: Job)       = viewModelScope.launch { db.jobDao().insertJob(j) }
    fun updateJob(j: Job)       = viewModelScope.launch { db.jobDao().updateJob(j) }
    fun deleteJob(j: Job)       = viewModelScope.launch { db.jobDao().deleteJob(j) }
    fun insertJobItem(ji: JobItem) = viewModelScope.launch { db.jobDao().insertJobItem(ji) }
    fun deleteJobItem(ji: JobItem) = viewModelScope.launch { db.jobDao().deleteJobItem(ji) }
}
