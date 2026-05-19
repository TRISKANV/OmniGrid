package com.cyber.omnigrid.core.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

@Entity(tableName = "dummy_table")
data class DummyEntity(@PrimaryKey val id: Int = 0)

@Dao
interface DummyDao

@Database(entities = [DummyEntity::class], version = 1, exportSchema = false)
abstract class OmniGridDatabase : RoomDatabase() {
    abstract fun payloadDao(): DummyDao 
}
