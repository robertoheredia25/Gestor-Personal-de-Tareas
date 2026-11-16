package com.example.gestorpersonaldetareas.almacenamiento

import com.example.gestorpersonaldetareas.modelo.Tarea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Comunica la interfaz con Room: traduce entre el modelo [Tarea] y [TareaEntity].
 * Así la interfaz no depende directamente de las clases de la base de datos.
 */
class TareasRepositorio(private val tareaDao: TareaDao) {

    /** Lista de tareas que se actualiza sola cuando cambia la base de datos. */
    val tareas: Flow<List<Tarea>> =
        tareaDao.observarTodas().map { filas -> filas.map { it.aModelo() } }

    suspend fun obtenerTarea(id: Int): Tarea? = tareaDao.obtenerPorId(id)?.aModelo()

    suspend fun insertar(tarea: Tarea): Long = tareaDao.insertar(tarea.aEntidad())

    suspend fun actualizar(tarea: Tarea) = tareaDao.actualizar(tarea.aEntidad())

    suspend fun cambiarCompletada(tarea: Tarea, completada: Boolean) =
        tareaDao.cambiarCompletada(tarea.id, completada)

    suspend fun eliminar(tarea: Tarea) = tareaDao.eliminar(tarea.aEntidad())
}
