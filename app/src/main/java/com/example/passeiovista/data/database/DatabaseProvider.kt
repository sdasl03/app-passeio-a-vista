package com.example.passeiovista.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `pending_sync_operations` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `action` TEXT NOT NULL,
                    `poiId` TEXT,
                    `routeId` INTEGER,
                    `createdAt` INTEGER NOT NULL,
                    `state` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_pending_sync_operations_state` ON `pending_sync_operations` (`state`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_pending_sync_operations_createdAt` ON `pending_sync_operations` (`createdAt`)"
            )
        }
    }

    fun get(context: Context): AppDatabase {
        val existing = instance
        if (existing != null) return existing

        return synchronized(this) {
            val again = instance
            if (again != null) return again

            val created = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "passeio_a_vista.db"
            ).addMigrations(MIGRATION_1_2).build()

            instance = created
            created
        }
    }
}
