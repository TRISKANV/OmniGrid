package com.omnigrid.payload.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.omnigrid.payload.data.local.dao.PayloadDao
import com.omnigrid.payload.data.local.dao.PayloadSessionDao
import com.omnigrid.payload.data.local.entity.PayloadEntity
import com.omnigrid.payload.data.local.entity.PayloadSessionEntity

@Database(
    entities = [
        PayloadEntity::class,
        PayloadSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PayloadDatabase : RoomDatabase() {
    abstract fun payloadDao(): PayloadDao
    abstract fun sessionDao(): PayloadSessionDao

    companion object {
        const val DATABASE_NAME = "omnigrid_payload_db"

        @Volatile
        private var INSTANCE: PayloadDatabase? = null

        fun getDatabase(context: Context): PayloadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PayloadDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
