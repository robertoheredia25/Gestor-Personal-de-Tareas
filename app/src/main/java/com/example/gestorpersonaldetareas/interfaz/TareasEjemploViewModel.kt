package com.example.gestorpersonaldetareas.interfaz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorpersonaldetareas.comunicaciones.ClienteRetrofit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel del apartado "Tareas de ejemplo".
 * Hace la petición con Retrofit dentro de una coroutine y expone su estado.
 * Los resultados se guardan solo en memoria (no en Room).
 */
class TareasEjemploViewModel : ViewModel() {

    private val _estado = MutableStateFlow<EstadoTareasEjemplo>(EstadoTareasEjemplo.SinConsultar)
    val estado: StateFlow<EstadoTareasEjemplo> = _estado.asStateFlow()

    /** Pide las tareas al servidor. Se usa tanto para la primera carga como para reintentar. */
    fun cargarTareas() {
        // Evita peticiones simultáneas si ya hay una en curso
        if (_estado.value is EstadoTareasEjemplo.Cargando) return

        _estado.value = EstadoTareasEjemplo.Cargando
        viewModelScope.launch {
            _estado.value = try {
                val tareas = ClienteRetrofit.api.obtenerTareas(ClienteRetrofit.ID_USUARIO_EJEMPLO)
                if (tareas.isEmpty()) EstadoTareasEjemplo.Vacia
                else EstadoTareasEjemplo.ConDatos(tareas)
            } catch (error: IOException) {
                // Sin Internet, servidor inalcanzable o tiempo de espera agotado
                EstadoTareasEjemplo.Error(
                    "No se ha podido conectar con el servidor.\nComprueba tu conexión a Internet."
                )
            } catch (error: HttpException) {
                EstadoTareasEjemplo.Error("El servidor ha respondido con un error (código ${error.code()}).")
            } catch (error: CancellationException) {
                // La cancelación de la coroutine no es un error: se deja propagar
                throw error
            } catch (error: Exception) {
                EstadoTareasEjemplo.Error("No se han podido leer las tareas recibidas.")
            }
        }
    }
}
