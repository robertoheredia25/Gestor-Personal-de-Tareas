package com.example.gestorpersonaldetareas.interfaz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Switch
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gestorpersonaldetareas.R
import com.example.gestorpersonaldetareas.interfaz.tema.ColorPrioridadAlta
import com.example.gestorpersonaldetareas.interfaz.tema.ColorPrioridadBaja
import com.example.gestorpersonaldetareas.interfaz.tema.ColorPrioridadMedia
import com.example.gestorpersonaldetareas.modelo.Prioridad
import com.example.gestorpersonaldetareas.modelo.Tarea

/**
 * Pantalla principal: barra superior, botón "Nueva tarea" y listado de tareas personales.
 * En pantallas anchas se añade una segunda columna con las tareas de ejemplo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    tareasViewModel: TareasViewModel,
    estadoSnackbar: SnackbarHostState,
    onNuevaTarea: () -> Unit,
    onEditarTarea: (Tarea) -> Unit,
    onAbrirTareasEjemplo: () -> Unit,
    // Contenido de la segunda columna en pantallas anchas (null en pantallas estrechas)
    panelTareasEjemplo: (@Composable () -> Unit)? = null
) {
    // collectAsStateWithLifecycle convierte el StateFlow en estado de Compose
    val estadoLista by tareasViewModel.estadoLista.collectAsStateWithLifecycle()
    val mostrarSoloPendientes by tareasViewModel.mostrarSoloPendientes.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    // Id de la tarea pendiente de confirmar su eliminación (null = no hay diálogo abierto).
    // Con rememberSaveable el diálogo sigue abierto si se gira el dispositivo.
    var idTareaAEliminar by rememberSaveable { mutableStateOf<Int?>(null) }
    val tareaAEliminar = (estadoLista as? EstadoListaTareas.ConDatos)
        ?.tareas?.firstOrNull { it.id == idTareaAEliminar }

    if (tareaAEliminar != null) {
        DialogoConfirmarEliminacion(
            tarea = tareaAEliminar,
            onConfirmar = {
                tareasViewModel.eliminarTarea(tareaAEliminar)
                idTareaAEliminar = null
            },
            // Cancelar solo cierra el diálogo: la tarea no se modifica
            onCancelar = { idTareaAEliminar = null }
        )
    }

    // Pantalla ancha = hay panel lateral para las tareas de ejemplo
    val pantallaAncha = panelTareasEjemplo != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pantallaAncha) "Gestor personal de tareas" else "Mis tareas") },
                actions = {
                    // En pantallas estrechas, botón para abrir el apartado "Tareas de ejemplo"
                    if (!pantallaAncha) {
                        TextButton(onClick = onAbrirTareasEjemplo) {
                            Icon(painterResource(R.drawable.ic_descargar), contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Ejemplos")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaTarea,
                icon = { Icon(painterResource(R.drawable.ic_anadir), contentDescription = null) },
                text = { Text("Nueva tarea") }
            )
        },
        // En dos columnas el botón va a la izquierda, sobre la lista de tareas personales
        floatingActionButtonPosition = if (pantallaAncha) FabPosition.Start else FabPosition.End,
        snackbarHost = { SnackbarHost(estadoSnackbar) },
        modifier = Modifier.fillMaxSize()
    ) { margenes ->
        Row(
            Modifier
                .padding(margenes)
                .padding(horizontal = 16.dp)
        ) {
            // Primera columna: tareas personales (Room)
            Column(Modifier.weight(1f)) {
                if (pantallaAncha) TituloSeccion("Mis tareas (guardadas en el dispositivo)")
                FiltroSoloPendientes(
                    activado = mostrarSoloPendientes,
                    onCambiar = tareasViewModel::cambiarMostrarSoloPendientes
                )
                ListadoTareas(
                    estado = estadoLista,
                    onCambiarCompletada = tareasViewModel::cambiarCompletada,
                    onEditarTarea = onEditarTarea,
                    onCompartirTarea = { tarea ->
                        val selectorAbierto = compartirTarea(contexto, tarea)
                        // Solo se avisa si no se pudo abrir el selector; nunca se dice "enviado"
                        if (!selectorAbierto) {
                            alcance.launch {
                                estadoSnackbar.showSnackbar("No hay ninguna aplicación instalada para compartir texto.")
                            }
                        }
                    },
                    onEliminarTarea = { tarea -> idTareaAEliminar = tarea.id },
                    modifier = Modifier.weight(1f)
                )
            }
            // Segunda columna (solo en pantallas anchas): tareas de ejemplo del servidor
            if (panelTareasEjemplo != null) {
                VerticalDivider(Modifier.padding(horizontal = 16.dp))
                Column(Modifier.weight(1f)) {
                    TituloSeccion("Tareas de ejemplo (servidor)")
                    panelTareasEjemplo()
                }
            }
        }
    }
}

/** Título de cada sección cuando se muestran en dos columnas. */
@Composable
fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

/**
 * Switch "Mostrar solo pendientes". Su valor viene de DataStore;
 * mientras no se ha leído (null) aparece deshabilitado.
 */
@Composable
fun FiltroSoloPendientes(
    activado: Boolean?,
    onCambiar: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Mostrar solo pendientes",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = activado == true,
            onCheckedChange = onCambiar,
            enabled = activado != null
        )
    }
}

/**
 * Listado de tareas personales según su estado (cargando, vacío, con datos o error).
 */
@Composable
fun ListadoTareas(
    estado: EstadoListaTareas,
    onCambiarCompletada: (Tarea, Boolean) -> Unit,
    onEditarTarea: (Tarea) -> Unit,
    onCompartirTarea: (Tarea) -> Unit,
    onEliminarTarea: (Tarea) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (estado) {
            EstadoListaTareas.Cargando ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is EstadoListaTareas.Vacia -> MensajeInformativo(
                icono = R.drawable.ic_lista_tareas,
                texto = if (estado.porFiltro) {
                    "No tienes tareas pendientes.\nDesactiva «Mostrar solo pendientes» para ver las completadas."
                } else {
                    "Todavía no tienes tareas.\nPulsa «Nueva tarea» para crear la primera."
                },
                modifier = Modifier.align(Alignment.Center)
            )

            is EstadoListaTareas.Error -> MensajeInformativo(
                icono = R.drawable.ic_sin_conexion,
                texto = estado.mensaje,
                modifier = Modifier.align(Alignment.Center)
            )

            is EstadoListaTareas.ConDatos -> LazyColumn(
                contentPadding = PaddingValues(bottom = 88.dp), // hueco para el botón flotante
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // La clave (id) permite a Compose identificar cada tarea cuando cambia la lista
                items(estado.tareas, key = { it.id }) { tarea ->
                    TarjetaTarea(
                        tarea = tarea,
                        onCambiarCompletada = { completada -> onCambiarCompletada(tarea, completada) },
                        onEditar = { onEditarTarea(tarea) },
                        onCompartir = { onCompartirTarea(tarea) },
                        onEliminar = { onEliminarTarea(tarea) }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta con los datos de una tarea (título, descripción, prioridad y estado)
 * y sus acciones: completar (Checkbox), editar y eliminar.
 */
@Composable
fun TarjetaTarea(
    tarea: Tarea,
    onCambiarCompletada: (Boolean) -> Unit,
    onEditar: () -> Unit,
    onCompartir: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Checkbox: cambia entre pendiente y completada
                Checkbox(
                    checked = tarea.completada,
                    onCheckedChange = onCambiarCompletada
                )
                Column(
                    Modifier
                        .weight(1f)
                        .padding(top = 12.dp, end = 12.dp)
                ) {
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        // Las tareas completadas aparecen tachadas
                        textDecoration = if (tarea.completada) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tarea.descripcion.isNotBlank()) {
                        Text(
                            text = tarea.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EtiquetaPrioridad(tarea.prioridad)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (tarea.completada) "Completada" else "Pendiente",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (tarea.completada) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Acciones sobre la tarea, alineadas a la derecha
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEditar) {
                    Icon(painterResource(R.drawable.ic_editar), contentDescription = "Editar tarea")
                }
                IconButton(onClick = onCompartir) {
                    Icon(painterResource(R.drawable.ic_compartir), contentDescription = "Compartir tarea")
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        painterResource(R.drawable.ic_eliminar),
                        contentDescription = "Eliminar tarea",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** Diálogo de confirmación antes de eliminar. Si se cancela, la tarea no cambia. */
@Composable
fun DialogoConfirmarEliminacion(
    tarea: Tarea,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(painterResource(R.drawable.ic_eliminar), contentDescription = null) },
        title = { Text("¿Eliminar tarea?") },
        text = { Text("Se eliminará «${tarea.titulo}» de forma permanente.") },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

/** Pequeña etiqueta de color con la prioridad de la tarea. */
@Composable
fun EtiquetaPrioridad(prioridad: Prioridad, modifier: Modifier = Modifier) {
    val color: Color = when (prioridad) {
        Prioridad.ALTA -> ColorPrioridadAlta
        Prioridad.MEDIA -> ColorPrioridadMedia
        Prioridad.BAJA -> ColorPrioridadBaja
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = "Prioridad ${prioridad.etiqueta.lowercase()}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** Mensaje centrado con un icono local; se usa para la lista vacía y para los errores. */
@Composable
fun MensajeInformativo(icono: Int, texto: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(24.dp)
    ) {
        Icon(
            painter = painterResource(icono),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
