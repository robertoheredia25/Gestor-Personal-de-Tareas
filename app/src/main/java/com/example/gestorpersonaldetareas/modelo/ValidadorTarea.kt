package com.example.gestorpersonaldetareas.modelo

/**
 * Reglas de validación del formulario de tareas.
 * Cada función devuelve el mensaje de error que se mostrará junto al campo, o null si es válido.
 */
object ValidadorTarea {

    const val LONGITUD_MINIMA_TITULO = 3

    /** El título no puede estar vacío ni ser solo espacios, y necesita 3 caracteres sin los espacios exteriores. */
    fun validarTitulo(titulo: String): String? = when {
        titulo.isBlank() -> "El título es obligatorio."
        titulo.trim().length < LONGITUD_MINIMA_TITULO ->
            "El título debe tener al menos $LONGITUD_MINIMA_TITULO caracteres."
        else -> null
    }

    /** La prioridad debe ser una de las tres del enum (null = no se ha elegido ninguna). */
    fun validarPrioridad(prioridad: Prioridad?): String? =
        if (prioridad == null || prioridad !in Prioridad.entries) "Selecciona una prioridad." else null

    // La descripción puede estar vacía, así que no necesita validación.
}
