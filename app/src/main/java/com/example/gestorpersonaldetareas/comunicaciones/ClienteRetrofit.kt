package com.example.gestorpersonaldetareas.comunicaciones

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit único para toda la app.
 * "by lazy" hace que se cree la primera vez que se usa.
 */
object ClienteRetrofit {

    private const val URL_BASE = "https://jsonplaceholder.typicode.com/"

    /** Usuario cuyas tareas de ejemplo se consultan (GET /todos?userId=1). */
    const val ID_USUARIO_EJEMPLO = 1

    val api: JsonPlaceholderApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            // Convierte la respuesta JSON en objetos Kotlin con Gson
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JsonPlaceholderApi::class.java)
    }
}
