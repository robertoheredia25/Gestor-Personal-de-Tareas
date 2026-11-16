package com.example.gestorpersonaldetareas.interfaz

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.gestorpersonaldetareas.modelo.Tarea

/** Texto que se comparte: título, descripción y prioridad de la tarea. */
fun crearTextoParaCompartir(tarea: Tarea): String = buildString {
    appendLine("Tarea: ${tarea.titulo}")
    appendLine("Descripción: ${tarea.descripcion.ifBlank { "(sin descripción)" }}")
    append("Prioridad: ${tarea.prioridad.etiqueta}")
}

/**
 * Abre el selector de aplicaciones para compartir la tarea mediante un Intent implícito ACTION_SEND.
 *
 * @return true si se ha abierto el selector; false si no hay ninguna aplicación compatible.
 * Abrir el selector NO significa que el usuario haya llegado a enviar nada,
 * por eso la interfaz nunca muestra un mensaje de "enviado".
 */
fun compartirTarea(context: Context, tarea: Tarea): Boolean {
    val intentEnviar = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, tarea.titulo)
        putExtra(Intent.EXTRA_TEXT, crearTextoParaCompartir(tarea))
    }

    // Se comprueba si alguna app instalada puede recibir texto
    // (requiere el bloque <queries> del AndroidManifest en Android 11 o superior)
    val aplicacionesCompatibles = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.queryIntentActivities(intentEnviar, PackageManager.ResolveInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(intentEnviar, 0)
    }
    if (aplicacionesCompatibles.isEmpty()) return false

    return try {
        // createChooser muestra siempre el selector para que el usuario elija la aplicación
        context.startActivity(Intent.createChooser(intentEnviar, "Compartir tarea con"))
        true
    } catch (error: ActivityNotFoundException) {
        false
    }
}
