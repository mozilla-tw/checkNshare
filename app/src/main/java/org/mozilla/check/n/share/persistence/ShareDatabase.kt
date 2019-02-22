package org.mozilla.check.n.share.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ShareEntity::class], version = 1)
abstract class ShareDatabase : RoomDatabase() {
    abstract fun shareDao(): ShareDao
}