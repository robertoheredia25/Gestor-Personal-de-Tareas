package com.example.gestorpersonaldetareas.comunicaciones

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Peticiones HTTP al servicio JSONPlaceholder.
 * Retrofit genera la implementación a partir de las anotaciones.
 */
interface JsonPlaceholderApi {

    /** GET /todos?userId={idUsuario}. Es "suspend": se ejecuta sin bloquear el hilo principal. */
    @GET("todos")
    suspend fun obtenerTareas(@Query("userId") idUsuario: Int): List<TareaRemota>
}
