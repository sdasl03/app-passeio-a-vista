package com.example.passeiovista.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

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
            ).fallbackToDestructiveMigration().build()

            instance = created
            created
        }
    }
}
