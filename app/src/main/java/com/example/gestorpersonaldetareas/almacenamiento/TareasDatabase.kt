package com.example.gestorpersonaldetareas.almacenamiento

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos Room de la aplicación.
 * Se usa una única instancia (patrón singleton) para toda la app.
 */
@Database(entities = [TareaEntity::class], version = 1, exportSchema = false)
abstract class TareasDatabase : RoomDatabase() {

    abstract fun tareaDao(): TareaDao

    companion object {
        @Volatile
        private var instancia: TareasDatabase? = null

        /** Devuelve la instancia existente o la crea la primera vez. */
        fun obtenerInstancia(context: Context): TareasDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    TareasDatabase::class.java,
                    "tareas.db"
                ).build().also { instancia = it }
            }
    }
}
