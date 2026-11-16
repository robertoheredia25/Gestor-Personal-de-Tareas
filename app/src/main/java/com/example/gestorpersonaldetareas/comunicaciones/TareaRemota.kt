package com.example.gestorpersonaldetareas.comunicaciones

import com.google.gson.annotations.SerializedName

/**
 * Tarea de ejemplo tal y como la devuelve JSONPlaceholder (GET /todos?userId=1).
 * Gson rellena cada propiedad con el campo JSON indicado en @SerializedName.
 * Estas tareas NO se guardan en Room: solo se muestran.
 */
data class TareaRemota(
    @SerializedName("userId") val idUsuario: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("title") val titulo: String,
    @SerializedName("completed") val completada: Boolean
)
