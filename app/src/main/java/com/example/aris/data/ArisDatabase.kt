package com.example.aris.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Deposito::class],
    version = 1,
    exportSchema = false
)
abstract class ArisDatabase : RoomDatabase() {

    abstract fun depositoDao(): DepositoDao

    companion object {

        @Volatile
        private var INSTANCE: ArisDatabase? = null

        fun obtenerInstancia(context: Context): ArisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    ArisDatabase::class.java,
                    "aris_database"
                ).build()

                INSTANCE = instancia
                instancia
            }
        }
    }
}