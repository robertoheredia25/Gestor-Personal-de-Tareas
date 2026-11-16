package com.example.gestorpersonaldetareas.interfaz

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** Pantallas de la aplicación (navegación sencilla basada en estados, sin librería). */
enum class Pantalla { LISTA, FORMULARIO, EJEMPLOS }

/** A partir de este ancho se considera pantalla grande y se usan dos columnas. */
private val ANCHO_MINIMO_DOS_COLUMNAS = 600.dp

/**
 * Composable raíz: decide qué pantalla se muestra según el estado [pantallaActual].
 * Los ViewModel pertenecen a la Activity, así que conservan sus datos al cambiar de pantalla
 * y al girar el dispositivo.
 */
@Composable
fun AppGestorTareas(
    tareasViewModel: TareasViewModel = viewModel(),
    tareasEjemploViewModel: TareasEjemploViewModel = viewModel()
) {
    // rememberSaveable: tras girar el dispositivo se sigue en la misma pantalla
    var pantallaActual by rememberSaveable { mutableStateOf(Pantalla.LISTA) }
    // Id de la tarea que se edita (null = se está creando una nueva)
    var idTareaEnEdicion by rememberSaveable { mutableStateOf<Int?>(null) }

    val estadoSnackbar = remember { SnackbarHostState() }

    // Mensajes breves del ViewModel ("Tarea creada", ...) mostrados en un Snackbar
    LaunchedEffect(Unit) {
        tareasViewModel.mensajes.collect { mensaje -> estadoSnackbar.showSnackbar(mensaje) }
    }

    // BoxWithConstraints indica el ancho disponible para adaptar la interfaz
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val pantallaAncha = maxWidth >= ANCHO_MINIMO_DOS_COLUMNAS

        // En pantalla ancha las tareas de ejemplo ya se ven junto al listado
        LaunchedEffect(pantallaAncha, pantallaActual) {
            if (pantallaAncha && pantallaActual == Pantalla.EJEMPLOS) pantallaActual = Pantalla.LISTA
        }

        when (pantallaActual) {
            Pantalla.LISTA -> PantallaPrincipal(
                tareasViewModel = tareasViewModel,
                estadoSnackbar = estadoSnackbar,
                onNuevaTarea = {
                    idTareaEnEdicion = null
                    pantallaActual = Pantalla.FORMULARIO
                },
                onEditarTarea = { tarea ->
                    idTareaEnEdicion = tarea.id
                    pantallaActual = Pantalla.FORMULARIO
                },
                onAbrirTareasEjemplo = { pantallaActual = Pantalla.EJEMPLOS },
                panelTareasEjemplo = if (pantallaAncha) {
                    { PanelTareasEjemplo(tareasEjemploViewModel) }
                } else {
                    null
                }
            )

            Pantalla.FORMULARIO -> PantallaFormularioTarea(
                idTarea = idTareaEnEdicion,
                tareasViewModel = tareasViewModel,
                onVolver = { pantallaActual = Pantalla.LISTA }
            )

            Pantalla.EJEMPLOS -> PantallaTareasEjemplo(
                tareasEjemploViewModel = tareasEjemploViewModel,
                onVolver = { pantallaActual = Pantalla.LISTA }
            )
        }
    }
}
