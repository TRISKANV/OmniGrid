package com.cyber.omnigrid.core.database
import androidx.room.RoomDatabase
import androidx.room.Database

@Database(entities = [], version = 1)
abstract class OmniGridDatabase : RoomDatabase() {
    abstract fun payloadDao(): Any 
}

package com.cyber.omnigrid.core.designsystem.theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TrueBlack = Color(0xFF000000)
@Composable fun OmniGridTheme(content: @Composable () -> Unit) { content() }
