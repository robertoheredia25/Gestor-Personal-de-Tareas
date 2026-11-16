package com.example.gestorpersonaldetareas.preferencias

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Único DataStore de la app (fichero "preferencias_usuario"), creado una sola vez por proceso. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias_usuario")

/**
 * Lectura y escritura de preferencias con Preferences DataStore.
 * Solo se guarda la opción "Mostrar solo pendientes"; las tareas siguen en Room.
 */
class PreferenciasRepositorio(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    private object Claves {
        val MOSTRAR_SOLO_PENDIENTES = booleanPreferencesKey("mostrar_solo_pendientes")
    }

    /** Valor guardado del filtro. Si nunca se ha guardado, es false (mostrar todas). */
    val mostrarSoloPendientes: Flow<Boolean> = dataStore.data
        .catch { error ->
            // Si el fichero no se puede leer, se usan los valores por defecto
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferencias -> preferencias[Claves.MOSTRAR_SOLO_PENDIENTES] ?: false }

    /** Guarda el nuevo valor del filtro (se ejecuta en segundo plano). */
    suspend fun guardarMostrarSoloPendientes(valor: Boolean) {
        dataStore.edit { preferencias ->
            preferencias[Claves.MOSTRAR_SOLO_PENDIENTES] = valor
        }
    }
}
