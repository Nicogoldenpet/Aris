package com.example.aris

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import com.example.aris.data.DepositoDao

private sealed class ArisDestino(
    val ruta: String,
    val titulo: String
) {
    data object Inicio : ArisDestino("inicio", "Inicio")
    data object Historial : ArisDestino("historial", "Historial")
    data object Ajustes : ArisDestino("ajustes", "Ajustes")
}

@Composable
fun ArisNavigation(
    depositoDao: DepositoDao,
    onAnnouncePayment: (Int) -> Unit,
    modoOscuro: Boolean,
    onCambiarModoOscuro: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val destinos = listOf(
        ArisDestino.Inicio,
        ArisDestino.Historial,
        ArisDestino.Ajustes
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    val esModoOscuro =
        MaterialTheme.colorScheme.background.luminance() < 0.5f

    val colorBarra = if (esModoOscuro) {
        Color(0xFF1B2D3A)
    } else {
        Color(0xFFFFFFFF)
    }

    val colorSeleccionado = if (esModoOscuro) {
        Color(0xFF4DD8C7)
    } else {
        Color(0xFF168F83)
    }

    val colorIndicador = if (esModoOscuro) {
        Color(0xFF244B4A)
    } else {
        Color(0xFFDDF5F1)
    }

    val colorNoSeleccionado = if (esModoOscuro) {
        Color(0xFFA8BBC9)
    } else {
        Color(0xFF607586)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.shadow(
                    elevation = 8.dp
                ),
                containerColor = colorBarra,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 3.dp
            ) {
                destinos.forEach { destino ->

                    NavigationBarItem(
                        selected = rutaActual == destino.ruta,

                        onClick = {
                            navController.navigate(destino.ruta) {
                                launchSingleTop = true
                                restoreState = true

                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },

                        icon = {
                            when (destino) {
                                ArisDestino.Inicio ->
                                    Icon(Icons.Filled.Home, contentDescription = null)

                                ArisDestino.Historial ->
                                    Icon(Icons.Filled.History, contentDescription = null)

                                ArisDestino.Ajustes ->
                                    Icon(Icons.Filled.Settings, contentDescription = null)
                            }
                        },

                        label = {
                            Text(destino.titulo)
                        },

                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorSeleccionado,
                            selectedTextColor = colorSeleccionado,
                            indicatorColor = colorIndicador,
                            unselectedIconColor = colorNoSeleccionado,
                            unselectedTextColor = colorNoSeleccionado
                        )
                    )
                }
            }
        }
    ) { paddingInterno ->
        NavHost(
            navController = navController,
            startDestination = ArisDestino.Inicio.ruta,
            modifier = Modifier.padding(paddingInterno)
        ) {
            composable(ArisDestino.Inicio.ruta) {
                ArisHome(
                    depositoDao = depositoDao,
                    onAnnouncePayment = onAnnouncePayment,
                    onVerHistorial = {
                        navController.navigate(ArisDestino.Historial.ruta) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                )
            }

            composable(ArisDestino.Historial.ruta) {
                PantallaHistorial(
                    depositoDao = depositoDao
                )
            }

            composable(ArisDestino.Ajustes.ruta) {
                PantallaAjustes(
                    modoOscuro = modoOscuro,
                    onCambiarModoOscuro = onCambiarModoOscuro
                )
            }
        }
    }
}
