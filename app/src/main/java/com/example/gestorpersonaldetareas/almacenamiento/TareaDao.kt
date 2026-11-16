package com.example.gestorpersonaldetareas.almacenamiento

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): operaciones de consulta, inserción, actualización y borrado.
 * Las funciones "suspend" se ejecutan en segundo plano; el Flow avisa de cada cambio en la tabla.
 */
@Dao
interface TareaDao {

    /** Todas las tareas; el Flow emite una lista nueva cada vez que cambia la tabla. */
    @Query("SELECT * FROM tareas ORDER BY id DESC")
    fun observarTodas(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): TareaEntity?

    /** Inserta una tarea y devuelve el id generado. */
    @Insert
    suspend fun insertar(tarea: TareaEntity): Long

    @Update
    suspend fun actualizar(tarea: TareaEntity)

    /** Cambia solo el estado completada/pendiente de una tarea. */
    @Query("UPDATE tareas SET completada = :completada WHERE id = :id")
    suspend fun cambiarCompletada(id: Int, completada: Boolean)

    @Delete
    suspend fun eliminar(tarea: TareaEntity)
}
