package com.example.gestorpersonaldetareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gestorpersonaldetareas.interfaz.AppGestorTareas
import com.example.gestorpersonaldetareas.interfaz.tema.GestorPersonalDeTareasTheme

/**
 * Única Activity de la aplicación. Todas las pantallas son funciones @Composable
 * y se cambia entre ellas con una navegación sencilla basada en estados.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestorPersonalDeTareasTheme {
                AppGestorTareas()
            }
        }
    }
}
