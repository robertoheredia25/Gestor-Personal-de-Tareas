package com.example.gestorpersonaldetareas.almacenamiento

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gestorpersonaldetareas.modelo.Prioridad
import com.example.gestorpersonaldetareas.modelo.Tarea

/**
 * Entidad de Room: cada objeto es una fila de la tabla "tareas".
 * La prioridad se guarda como texto (BAJA, MEDIA, ALTA) para no necesitar un TypeConverter.
 */
@Entity(tableName = "tareas")
data class TareaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "titulo") val titulo: String,
    @ColumnInfo(name = "descripcion") val descripcion: String,
    @ColumnInfo(name = "prioridad") val prioridad: String,
    @ColumnInfo(name = "completada") val completada: Boolean
)

/** Convierte una fila de la base de datos en el modelo usado por la interfaz. */
fun TareaEntity.aModelo(): Tarea = Tarea(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    prioridad = Prioridad.desdeTexto(prioridad),
    completada = completada
)

/** Convierte el modelo en una entidad lista para guardarse en Room. */
fun Tarea.aEntidad(): TareaEntity = TareaEntity(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    prioridad = prioridad.name,
    completada = completada
)
