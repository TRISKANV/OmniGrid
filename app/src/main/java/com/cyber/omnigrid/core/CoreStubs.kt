package com.cyber.omnigrid.core

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

// 1. Entidad mínima para que Room no crashee por lista de entidades vacía
@Entity(tableName = "dummy_table")
data class DummyEntity(
    @PrimaryKey val id: Int = 0
)

// 2. DAO válido (interfaz) para que no tire error con 'Any' u 'Object'
@Dao
interface DummyDao {
    // Vacío por ahora, suficiente para que compile
}

// 3. Base de datos con una entidad y exportSchema = false para limpiar el warning
@Database(entities = [DummyEntity::class], version = 1, exportSchema = false)
abstract class OmniGridDatabase : RoomDatabase() {
    abstract fun payloadDao(): DummyDao 
}

class OfflinePayloadRepository
