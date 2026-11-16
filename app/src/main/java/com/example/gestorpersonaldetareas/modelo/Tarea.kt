package com.example.gestorpersonaldetareas.modelo

/**
 * Tarea personal tal y como la usa la interfaz.
 * Es independiente de Room: la conversión se hace en el paquete de almacenamiento.
 *
 * @property id identificador único (0 = tarea nueva aún no guardada; Room genera el valor).
 */
data class Tarea(
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val prioridad: Prioridad,
    val completada: Boolean = false
)
