package com.ahmad.girum.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DownloadItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class GirumDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
