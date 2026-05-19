package com.cyber.omnigrid.core

import androidx.room.RoomDatabase
import androidx.room.Database

@Database(entities = [], version = 1)
abstract class OmniGridDatabase : RoomDatabase() {
    abstract fun payloadDao(): Any 
}

class OfflinePayloadRepository
