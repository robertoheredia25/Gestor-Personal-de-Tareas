package com.example.gestorpersonaldetareas.interfaz

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestorpersonaldetareas.R
import com.example.gestorpersonaldetareas.comunicaciones.TareaRemota

/**
 * Pantalla completa "Tareas de ejemplo" (se usa en pantallas estrechas).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareasEjemplo(
    tareasEjemploViewModel: TareasEjemploViewModel,
    onVolver: () -> Unit
) {
    BackHandler(onBack = onVolver)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas de ejemplo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
                    }
                }
            )
        }
    ) { margenes ->
        PanelTareasEjemplo(
            tareasEjemploViewModel = tareasEjemploViewModel,
            modifier = Modifier
                .padding(margenes)
                .padding(horizontal = 16.dp)
        )
    }
}

/**
 * Contenido del apartado "Tareas de ejemplo": botón de carga y resultado según el estado.
 */
@Composable
fun PanelTareasEjemplo(
    tareasEjemploViewModel: TareasEjemploViewModel,
    modifier: Modifier = Modifier
) {
    val estado by tareasEjemploViewModel.estado.collectAsStateWithLifecycle()
    val cargando = estado is EstadoTareasEjemplo.Cargando

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Datos de ejemplo obtenidos de JSONPlaceholder. " +
                    "Son de solo lectura y no se guardan en «Mis tareas».",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(
            onClick = tareasEjemploViewModel::cargarTareas,
            // Deshabilitado durante la petición para evitar solicitudes simultáneas
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(painterResource(R.drawable.ic_descargar), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (cargando) "Cargando..." else "Cargar tareas de ejemplo")
        }
        Spacer(Modifier.size(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val estadoActual = estado) {
                EstadoTareasEjemplo.SinConsultar -> MensajeInformativo(
                    icono = R.drawable.ic_descargar,
                    texto = "Pulsa el botón para consultar las tareas del servidor.",
                    modifier = Modifier.align(Alignment.Center)
                )

                EstadoTareasEjemplo.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                EstadoTareasEjemplo.Vacia -> MensajeInformativo(
                    icono = R.drawable.ic_lista_tareas,
                    texto = "El servidor no ha devuelto ninguna tarea.",
                    modifier = Modifier.align(Alignment.Center)
                )

                is EstadoTareasEjemplo.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    MensajeInformativo(icono = R.drawable.ic_sin_conexion, texto = estadoActual.mensaje)
                    Button(onClick = tareasEjemploViewModel::cargarTareas) {
                        Icon(painterResource(R.drawable.ic_reintentar), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reintentar")
                    }
                }

                is EstadoTareasEjemplo.ConDatos -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "${estadoActual.tareas.size} tareas recibidas del servidor",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    items(estadoActual.tareas, key = { it.id }) { tarea ->
                        TarjetaTareaEjemplo(tarea)
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de una tarea remota. Usa un OutlinedCard (con borde) y una etiqueta "Ejemplo"
 * para distinguirla de las tareas personales, y no tiene acciones porque es de solo lectura.
 */
@Composable
fun TarjetaTareaEjemplo(tarea: TareaRemota, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ejemplo #${tarea.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (tarea.completada) "Completada" else "Pendiente",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (tarea.completada) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = tarea.titulo, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
