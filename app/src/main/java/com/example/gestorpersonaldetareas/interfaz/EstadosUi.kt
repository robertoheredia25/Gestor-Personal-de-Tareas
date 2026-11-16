package com.example.gestorpersonaldetareas.interfaz

import com.example.gestorpersonaldetareas.comunicaciones.TareaRemota
import com.example.gestorpersonaldetareas.modelo.Tarea

/**
 * Estados de la consulta de "Tareas de ejemplo" a la API REST.
 */
sealed interface EstadoTareasEjemplo {
    /** Todavía no se ha pedido nada al servidor. */
    data object SinConsultar : EstadoTareasEjemplo

    /** Petición en curso: se muestra el indicador de carga y el botón queda deshabilitado. */
    data object Cargando : EstadoTareasEjemplo

    /** Respuesta recibida con datos. */
    data class ConDatos(val tareas: List<TareaRemota>) : EstadoTareasEjemplo

    /** El servidor respondió, pero sin tareas. */
    data object Vacia : EstadoTareasEjemplo

    /** No se pudo completar la petición (sin conexión, error del servidor...). */
    data class Error(val mensaje: String) : EstadoTareasEjemplo
}

/**
 * Estados posibles del listado de tareas personales.
 * Cada estado se dibuja de forma distinta en la pantalla principal.
 */
sealed interface EstadoListaTareas {
    /** Operación en curso: todavía se está leyendo la base de datos. */
    data object Cargando : EstadoListaTareas

    /**
     * Lista vacía: no hay tareas que mostrar.
     * @property porFiltro true si hay tareas, pero el filtro "solo pendientes" las oculta todas.
     */
    data class Vacia(val porFiltro: Boolean) : EstadoListaTareas

    /** Datos disponibles. */
    data class ConDatos(val tareas: List<Tarea>) : EstadoListaTareas

    /** Error al leer la base de datos. */
    data class Error(val mensaje: String) : EstadoListaTareas
}

/**
 * Estado de una operación puntual, como guardar una tarea.
 */
sealed interface EstadoOperacion {
    /** No se está haciendo nada. */
    data object Inactiva : EstadoOperacion

    /** Operación en curso: se deshabilita el botón para evitar pulsaciones repetidas. */
    data object EnCurso : EstadoOperacion

    /** Operación completada correctamente. */
    data object Completada : EstadoOperacion

    /** La operación ha fallado. */
    data class Error(val mensaje: String) : EstadoOperacion
}
