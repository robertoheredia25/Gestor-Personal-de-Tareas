package com.example.gestorpersonaldetareas.interfaz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestorpersonaldetareas.R
import com.example.gestorpersonaldetareas.modelo.Prioridad
import com.example.gestorpersonaldetareas.modelo.ValidadorTarea

/** Ancho máximo del formulario: en pantallas grandes no se estira de lado a lado. */
private val ANCHO_MAXIMO_FORMULARIO = 600.dp

/**
 * Formulario para crear (idTarea == null) o editar una tarea existente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioTarea(
    idTarea: Int?,
    tareasViewModel: TareasViewModel,
    onVolver: () -> Unit
) {
    // rememberSaveable conserva lo escrito aunque se gire el dispositivo
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var prioridad by rememberSaveable { mutableStateOf<Prioridad?>(null) }
    var datosCargados by rememberSaveable { mutableStateOf(idTarea == null) }
    var errorCarga by rememberSaveable { mutableStateOf<String?>(null) }
    // Los errores se muestran a partir del primer intento de guardar
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }

    val estadoGuardado by tareasViewModel.estadoGuardado.collectAsStateWithLifecycle()
    val guardando = estadoGuardado is EstadoOperacion.EnCurso ||
            estadoGuardado is EstadoOperacion.Completada

    // Al editar, se leen los datos actuales de Room una sola vez (no al girar la pantalla)
    LaunchedEffect(idTarea) {
        if (idTarea != null && !datosCargados) {
            val tarea = tareasViewModel.cargarTarea(idTarea)
            if (tarea != null) {
                titulo = tarea.titulo
                descripcion = tarea.descripcion
                prioridad = tarea.prioridad
            } else {
                errorCarga = "No se ha encontrado la tarea."
            }
            datosCargados = true
        }
    }

    // Cuando el guardado termina bien, se vuelve al listado
    LaunchedEffect(estadoGuardado) {
        if (estadoGuardado is EstadoOperacion.Completada) {
            tareasViewModel.reiniciarEstadoGuardado()
            onVolver()
        }
    }

    val salir = {
        // Mientras se guarda no se sale, para no dejar la operación a medias
        if (!guardando) {
            // Si el guardado falló, se limpia el error para la próxima vez
            if (estadoGuardado is EstadoOperacion.Error) tareasViewModel.reiniciarEstadoGuardado()
            onVolver()
        }
    }
    // El botón "atrás" del sistema equivale a Cancelar
    BackHandler(onBack = salir)

    val errorTitulo = if (mostrarErrores) ValidadorTarea.validarTitulo(titulo) else null
    val errorPrioridad = if (mostrarErrores) ValidadorTarea.validarPrioridad(prioridad) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (idTarea == null) "Nueva tarea" else "Editar tarea") },
                navigationIcon = {
                    IconButton(onClick = salir) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
                    }
                }
            )
        }
    ) { margenes ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(margenes)
                .fillMaxSize()
        ) {
            when {
                !datosCargados -> CircularProgressIndicator(Modifier.padding(32.dp))

                errorCarga != null -> MensajeInformativo(
                    icono = R.drawable.ic_lista_tareas,
                    texto = errorCarga.orEmpty()
                )

                else -> Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .widthIn(max = ANCHO_MAXIMO_FORMULARIO)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título *") },
                        singleLine = true,
                        isError = errorTitulo != null,
                        supportingText = {
                            Text(errorTitulo ?: "Obligatorio, mínimo ${ValidadorTarea.LONGITUD_MINIMA_TITULO} caracteres.")
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción (opcional)") },
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SelectorPrioridad(
                        prioridadSeleccionada = prioridad,
                        error = errorPrioridad,
                        onPrioridadElegida = { prioridad = it }
                    )

                    if (estadoGuardado is EstadoOperacion.Error) {
                        Text(
                            text = (estadoGuardado as EstadoOperacion.Error).mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = salir, enabled = !guardando) { Text("Cancelar") }
                        Button(
                            // Deshabilitado mientras se guarda: evita registros duplicados
                            enabled = !guardando,
                            onClick = {
                                mostrarErrores = true
                                val formularioValido =
                                    ValidadorTarea.validarTitulo(titulo) == null &&
                                            ValidadorTarea.validarPrioridad(prioridad) == null
                                if (formularioValido) {
                                    tareasViewModel.guardarTarea(idTarea, titulo, descripcion, prioridad)
                                }
                            }
                        ) {
                            if (guardando) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text("Guardar")
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Menú desplegable para elegir una de las tres prioridades. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorPrioridad(
    prioridadSeleccionada: Prioridad?,
    error: String?,
    onPrioridadElegida: (Prioridad) -> Unit
) {
    // remember basta aquí: no importa que el menú se cierre al girar la pantalla
    var menuAbierto by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = menuAbierto,
        onExpandedChange = { menuAbierto = it }
    ) {
        OutlinedTextField(
            value = prioridadSeleccionada?.etiqueta ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Prioridad *") },
            placeholder = { Text("Selecciona una prioridad") },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false }
        ) {
            Prioridad.entries.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion.etiqueta) },
                    onClick = {
                        onPrioridadElegida(opcion)
                        menuAbierto = false
                    }
                )
            }
        }
    }
}
