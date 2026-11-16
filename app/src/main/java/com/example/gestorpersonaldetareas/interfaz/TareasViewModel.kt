package com.example.gestorpersonaldetareas.interfaz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorpersonaldetareas.almacenamiento.TareasDatabase
import com.example.gestorpersonaldetareas.almacenamiento.TareasRepositorio
import com.example.gestorpersonaldetareas.modelo.Prioridad
import com.example.gestorpersonaldetareas.modelo.Tarea
import com.example.gestorpersonaldetareas.modelo.ValidadorTarea
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.gestorpersonaldetareas.preferencias.PreferenciasRepositorio
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de las tareas personales.
 * Sobrevive a los cambios de orientación y lanza las operaciones de Room en coroutines
 * (viewModelScope), de modo que la interfaz nunca se bloquea.
 */
class TareasViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = TareasRepositorio(
        TareasDatabase.obtenerInstancia(application).tareaDao()
    )

    private val preferencias = PreferenciasRepositorio(application)

    /**
     * Valor del Switch "Mostrar solo pendientes" leído de DataStore.
     * Es null mientras todavía no se ha leído el valor guardado.
     */
    val mostrarSoloPendientes: StateFlow<Boolean?> = preferencias.mostrarSoloPendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Estado del listado. Combina las tareas de Room con la preferencia de DataStore
     * y se recalcula automáticamente cuando cambia cualquiera de las dos.
     */
    val estadoLista: StateFlow<EstadoListaTareas> =
        combine(repositorio.tareas, preferencias.mostrarSoloPendientes) { tareas, soloPendientes ->
            val tareasVisibles = if (soloPendientes) tareas.filter { !it.completada } else tareas
            if (tareasVisibles.isEmpty()) {
                EstadoListaTareas.Vacia(porFiltro = tareas.isNotEmpty())
            } else {
                EstadoListaTareas.ConDatos(tareasVisibles)
            }
        }
            .catch<EstadoListaTareas> {
                emit(EstadoListaTareas.Error("No se han podido leer las tareas guardadas."))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoListaTareas.Cargando)

    /** Guarda en DataStore el nuevo valor del filtro. */
    fun cambiarMostrarSoloPendientes(valor: Boolean) {
        viewModelScope.launch { preferencias.guardarMostrarSoloPendientes(valor) }
    }

    private val _estadoGuardado = MutableStateFlow<EstadoOperacion>(EstadoOperacion.Inactiva)

    /** Estado de la operación de guardar (inactiva, en curso, completada o error). */
    val estadoGuardado: StateFlow<EstadoOperacion> = _estadoGuardado.asStateFlow()

    // Canal de mensajes breves para mostrar en un Snackbar (cada mensaje se muestra una sola vez)
    private val _mensajes = Channel<String>(Channel.BUFFERED)
    val mensajes: Flow<String> = _mensajes.receiveAsFlow()

    /** Lee una tarea de la base de datos para rellenar el formulario de edición. */
    suspend fun cargarTarea(id: Int): Tarea? = repositorio.obtenerTarea(id)

    /**
     * Inserta (idTarea == null) o actualiza una tarea.
     * Si ya hay un guardado en curso o recién completado se ignora la llamada:
     * así varias pulsaciones seguidas de "Guardar" no crean registros duplicados.
     */
    fun guardarTarea(idTarea: Int?, titulo: String, descripcion: String, prioridad: Prioridad?) {
        val estadoActual = _estadoGuardado.value
        if (estadoActual is EstadoOperacion.EnCurso || estadoActual is EstadoOperacion.Completada) return

        // Segunda comprobación de las reglas (la interfaz ya las valida antes de llamar)
        if (ValidadorTarea.validarTitulo(titulo) != null ||
            ValidadorTarea.validarPrioridad(prioridad) != null || prioridad == null
        ) return

        _estadoGuardado.value = EstadoOperacion.EnCurso
        viewModelScope.launch {
            try {
                if (idTarea == null) {
                    repositorio.insertar(
                        Tarea(
                            titulo = titulo.trim(),
                            descripcion = descripcion.trim(),
                            prioridad = prioridad
                        )
                    )
                    _mensajes.send("Tarea creada")
                } else {
                    // Se parte de la tarea guardada para conservar su estado (completada o no)
                    val tareaGuardada = repositorio.obtenerTarea(idTarea)
                        ?: throw IllegalStateException("La tarea ya no existe")
                    repositorio.actualizar(
                        tareaGuardada.copy(
                            titulo = titulo.trim(),
                            descripcion = descripcion.trim(),
                            prioridad = prioridad
                        )
                    )
                    _mensajes.send("Tarea actualizada")
                }
                _estadoGuardado.value = EstadoOperacion.Completada
            } catch (error: Exception) {
                _estadoGuardado.value = EstadoOperacion.Error("No se ha podido guardar la tarea.")
            }
        }
    }

    /** Marca una tarea como completada o pendiente y lo guarda en Room. */
    fun cambiarCompletada(tarea: Tarea, completada: Boolean) {
        viewModelScope.launch {
            try {
                repositorio.cambiarCompletada(tarea, completada)
            } catch (error: Exception) {
                _mensajes.send("No se ha podido actualizar la tarea.")
            }
        }
    }

    /** Elimina la tarea de la base de datos (solo se llama tras confirmar en el diálogo). */
    fun eliminarTarea(tarea: Tarea) {
        viewModelScope.launch {
            try {
                repositorio.eliminar(tarea)
                _mensajes.send("Tarea eliminada")
            } catch (error: Exception) {
                _mensajes.send("No se ha podido eliminar la tarea.")
            }
        }
    }

    /** Vuelve a dejar el guardado en reposo (se llama al salir del formulario). */
    fun reiniciarEstadoGuardado() {
        _estadoGuardado.value = EstadoOperacion.Inactiva
    }
}
