package com.example.gestorpersonaldetareas.modelo

/**
 * Las tres prioridades permitidas para una tarea.
 * Al ser un enum, es imposible asignar un valor distinto de estos tres.
 */
enum class Prioridad(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    companion object {
        /** Convierte el texto guardado en la base de datos en una prioridad (MEDIA si no es válido). */
        fun desdeTexto(texto: String): Prioridad =
            entries.firstOrNull { it.name == texto } ?: MEDIA
    }
}
