package com.example.mobildrontesztprojekt.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobildrontesztprojekt.data.dao.*
import com.example.mobildrontesztprojekt.data.entity.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase as SQLiteDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        User::class, Company::class, Warehouse::class,
        Corridor::class, ShelfRow::class,
        ShelfLocation::class, InventoryItem::class,
        UWBNode::class, Drone::class,
        Job::class, JobItem::class,
        AppKey::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun companyDao(): CompanyDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun corridorDao(): CorridorDao
    abstract fun shelfRowDao(): ShelfRowDao
    abstract fun shelfLocationDao(): ShelfLocationDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun uwbNodeDao(): UWBNodeDao
    abstract fun droneDao(): DroneDao
    abstract fun jobDao(): JobDao
    abstract fun appKeyDao(): AppKeyDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "drontech.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SQLiteDB) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `app_key` (`id` INTEGER NOT NULL, `djiAppKey` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { seed(it) }
            }
        }

        private suspend fun seed(db: AppDatabase) {
            // ── Cégek ──────────────────────────────────────────────────────────
            val co1 = db.companyDao().insert(Company(name = "DronTech Kft.", address = "Budapest, Váci út 1.", phone = "+36 1 234 5678"))
            val co2 = db.companyDao().insert(Company(name = "LogiDron Zrt.", address = "Győr, Ipari park 12.", phone = "+36 96 555 100"))

            // ── Felhasználók (1 per szerepkör) ─────────────────────────────────
            db.userDao().insert(User(firstName = "Adminisztrátor", lastName = "Rendszer", email = "admin@drontech.hu",   passwordHash = hashPassword("admin123"),   role = UserRole.ADMIN,     companyId = co1))
            db.userDao().insert(User(firstName = "Béla",           lastName = "Kovács",   email = "manager@drontech.hu", passwordHash = hashPassword("manager123"), role = UserRole.MANAGER,   companyId = co1))
            val techId = db.userDao().insert(User(firstName = "Péter", lastName = "Nagy", email = "tech@drontech.hu",    passwordHash = hashPassword("tech123"),    role = UserRole.TECHNICAL, companyId = co1))
            db.userDao().insert(User(firstName = "Gábor",          lastName = "Tóth",     email = "tech2@logidron.hu",   passwordHash = hashPassword("tech123"),    role = UserRole.TECHNICAL, companyId = co2))

            // ── Raktárak ───────────────────────────────────────────────────────
            val wh1 = db.warehouseDao().insert(Warehouse(companyId = co1, name = "Budapest Központi Raktár", address = "Budapest, Ipari út 5."))
            val wh2 = db.warehouseDao().insert(Warehouse(companyId = co1, name = "Debrecen Raktár",          address = "Debrecen, Freeport 3."))
            val wh3 = db.warehouseDao().insert(Warehouse(companyId = co2, name = "Győr Logisztikai Centrum", address = "Győr, Ipari park 12."))

            // ── Folyosók ───────────────────────────────────────────────────────
            val corrA = db.corridorDao().insert(Corridor(warehouseId = wh1, name = "A folyosó", code = "COR-A",
                startX = 0f, startY = 2f, endX = 20f, endY = 2f,
                widthCm = 300f, heightCm = 500f))
            val corrB = db.corridorDao().insert(Corridor(warehouseId = wh1, name = "B folyosó", code = "COR-B",
                startX = 0f, startY = 6f, endX = 20f, endY = 6f,
                widthCm = 300f, heightCm = 500f))
            val corrC = db.corridorDao().insert(Corridor(warehouseId = wh2, name = "Főfolyosó", code = "COR-M",
                startX = 0f, startY = 3f, endX = 15f, endY = 3f,
                widthCm = 400f, heightCm = 600f))

            // ── Polcsorok ──────────────────────────────────────────────────────
            val rowA_L = db.shelfRowDao().insert(ShelfRow(corridorId = corrA, code = "A-BAL",   side = ShelfSide.LEFT,  description = "A folyosó bal oldali polcsora"))
            val rowA_R = db.shelfRowDao().insert(ShelfRow(corridorId = corrA, code = "A-JOBB",  side = ShelfSide.RIGHT, description = "A folyosó jobb oldali polcsora"))
            val rowB_L = db.shelfRowDao().insert(ShelfRow(corridorId = corrB, code = "B-BAL",   side = ShelfSide.LEFT,  description = "B folyosó bal oldali polcsora"))
            val rowC_L = db.shelfRowDao().insert(ShelfRow(corridorId = corrC, code = "M-BAL",   side = ShelfSide.LEFT,  description = "Főfolyosó bal oldal"))

            // ── UWB Csomópontok ────────────────────────────────────────────────
            val anchorA1 = db.uwbNodeDao().insert(UWBNode(warehouseId = wh1, corridorId = corrA, name = "A-Horgony-1", code = "UWB-A01",
                posX = 5.0f, posY = 2.0f, posZ = 4.5f, nodeType = NodeType.ANCHOR))
            val anchorA2 = db.uwbNodeDao().insert(UWBNode(warehouseId = wh1, corridorId = corrA, name = "A-Horgony-2", code = "UWB-A02",
                posX = 15.0f, posY = 2.0f, posZ = 4.5f, nodeType = NodeType.ANCHOR))
            val dropA   = db.uwbNodeDao().insert(UWBNode(warehouseId = wh1, corridorId = corrA, name = "A-Lerakó",    code = "UWB-A-DROP",
                posX = 10.0f, posY = 2.0f, posZ = 0.5f, nodeType = NodeType.DROP_POINT))
            val base1   = db.uwbNodeDao().insert(UWBNode(warehouseId = wh1, corridorId = null,  name = "Home Base 1", code = "UWB-BASE1",
                posX = 0.5f,  posY = 0.5f, posZ = 0.1f, nodeType = NodeType.DRONE_BASE))
            val base2   = db.uwbNodeDao().insert(UWBNode(warehouseId = wh1, corridorId = null,  name = "Home Base 2", code = "UWB-BASE2",
                posX = 1.5f,  posY = 0.5f, posZ = 0.1f, nodeType = NodeType.DRONE_BASE))
            val anchorB1 = db.uwbNodeDao().insert(UWBNode(warehouseId = wh1, corridorId = corrB, name = "B-Horgony-1", code = "UWB-B01",
                posX = 5.0f, posY = 6.0f, posZ = 4.5f, nodeType = NodeType.ANCHOR))
            db.uwbNodeDao().insert(UWBNode(warehouseId = wh2, corridorId = corrC, name = "M-Horgony-1", code = "UWB-M01",
                posX = 7.5f, posY = 3.0f, posZ = 4.5f, nodeType = NodeType.ANCHOR))

            // ── Polchelyek (UWB-kalibrálva) ────────────────────────────────────
            val shelf_A01 = db.shelfLocationDao().insert(ShelfLocation(warehouseId = wh1, shelfRowId = rowA_L, uwbAnchorId = anchorA1,
                code = "A-01-01", posX = 1.0f, posY = 0.5f, posZ = 0.5f, widthCm = 120f, heightCm = 40f, depthCm = 80f, maxWeightKg = 150f))
            val shelf_A02 = db.shelfLocationDao().insert(ShelfLocation(warehouseId = wh1, shelfRowId = rowA_L, uwbAnchorId = anchorA1,
                code = "A-01-02", posX = 1.0f, posY = 0.5f, posZ = 1.5f, widthCm = 120f, heightCm = 40f, depthCm = 80f, maxWeightKg = 150f))
            val shelf_A03 = db.shelfLocationDao().insert(ShelfLocation(warehouseId = wh1, shelfRowId = rowA_R, uwbAnchorId = anchorA1,
                code = "A-02-01", posX = 1.0f, posY = 3.5f, posZ = 0.5f, widthCm = 120f, heightCm = 40f, depthCm = 80f, maxWeightKg = 150f))
            val shelf_B01 = db.shelfLocationDao().insert(ShelfLocation(warehouseId = wh1, shelfRowId = rowB_L, uwbAnchorId = anchorB1,
                code = "B-01-01", posX = 3.5f, posY = 4.5f, posZ = 0.5f, widthCm = 100f, heightCm = 50f, depthCm = 60f, maxWeightKg = 200f))
            val shelf_B02 = db.shelfLocationDao().insert(ShelfLocation(warehouseId = wh1, shelfRowId = rowB_L, uwbAnchorId = anchorB1,
                code = "B-01-02", posX = 3.5f, posY = 4.5f, posZ = 1.5f, widthCm = 100f, heightCm = 50f, depthCm = 60f, maxWeightKg = 200f))
            db.shelfLocationDao().insert(ShelfLocation(warehouseId = wh2, shelfRowId = rowC_L, uwbAnchorId = null,
                code = "C-01-01", posX = 2.0f, posY = 1.0f, posZ = 0.8f, widthCm = 150f, heightCm = 45f, depthCm = 90f, maxWeightKg = 300f))

            // ── Készlet ────────────────────────────────────────────────────────
            val item1 = db.inventoryItemDao().insert(InventoryItem(warehouseId = wh1, shelfLocationId = shelf_A01, name = "Ipari szenzor X200",    sku = "SNS-X200", widthCm = 15f, heightCm = 10f, depthCm = 8f,  weightKg = 0.5f, quantity = 24))
            val item2 = db.inventoryItemDao().insert(InventoryItem(warehouseId = wh1, shelfLocationId = shelf_A02, name = "Akkumulátor csomag 48V", sku = "BAT-48V",  widthCm = 30f, heightCm = 20f, depthCm = 15f, weightKg = 4.2f, quantity = 8))
            val item3 = db.inventoryItemDao().insert(InventoryItem(warehouseId = wh1, shelfLocationId = shelf_B01, name = "Vezérlő modul DM-5",     sku = "CTL-DM5",  widthCm = 20f, heightCm = 15f, depthCm = 10f, weightKg = 1.1f, quantity = 12))
            val item4 = db.inventoryItemDao().insert(InventoryItem(warehouseId = wh1, shelfLocationId = shelf_B02, name = "Kamera modul 4K",        sku = "CAM-4K",   widthCm = 8f,  heightCm = 6f,  depthCm = 5f,  weightKg = 0.3f, quantity = 6))
            db.inventoryItemDao().insert(InventoryItem(warehouseId = wh2, shelfLocationId = null, name = "Propeller készlet", sku = "PROP-SET", widthCm = 40f, heightCm = 5f, depthCm = 40f, weightKg = 0.8f, quantity = 30))

            // ── Dronok (homeBase rendelve) ─────────────────────────────────────
            val drone1 = db.droneDao().insert(Drone(companyId = co1, name = "Dron-001", model = "DJI Matrice 300",  serialNumber = "DJI-M300-001", status = DroneStatus.IDLE,        homeBaseId = base1))
            val drone2 = db.droneDao().insert(Drone(companyId = co1, name = "Dron-002", model = "DJI Matrice 300",  serialNumber = "DJI-M300-002", status = DroneStatus.CHARGING,     homeBaseId = base2))
            val drone3 = db.droneDao().insert(Drone(companyId = co1, name = "Dron-003", model = "DJI Matrice 350",  serialNumber = "DJI-M350-001", status = DroneStatus.MAINTENANCE,  homeBaseId = null))
            val drone4 = db.droneDao().insert(Drone(companyId = co2, name = "LogiDron-01", model = "Autel EVO II",  serialNumber = "AUT-EVO-001",  status = DroneStatus.ACTIVE,       homeBaseId = null))

            // ── Munkák ─────────────────────────────────────────────────────────
            val job1 = db.jobDao().insertJob(Job(droneId = drone1, assignedUserId = techId, status = JobStatus.PENDING,     notes = "Raktár felmérés – Budapest"))
            db.jobDao().insertJobItem(JobItem(jobId = job1, inventoryItemId = item1, quantity = 5))
            db.jobDao().insertJobItem(JobItem(jobId = job1, inventoryItemId = item3, quantity = 2))

            val job2 = db.jobDao().insertJob(Job(droneId = drone4, status = JobStatus.IN_PROGRESS, notes = "Leltározás – Győr"))
            db.jobDao().insertJobItem(JobItem(jobId = job2, inventoryItemId = item2, quantity = 3))
            db.jobDao().insertJobItem(JobItem(jobId = job2, inventoryItemId = item4, quantity = 1))

            db.jobDao().insertJob(Job(droneId = drone2, status = JobStatus.COMPLETED, notes = "Kiszállítás teljesítve"))
        }
    }
}