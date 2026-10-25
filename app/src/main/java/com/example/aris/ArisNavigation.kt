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
    onAnnouncePayment: (Int) -> Unit
) {
    val navController = rememberNavController()
    val destinos = listOf(
        ArisDestino.Inicio,
        ArisDestino.Historial,
        ArisDestino.Ajustes
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
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
                        label = { Text(destino.titulo) }
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
                PantallaAjustesPendiente()
            }
        }
    }
}

@Composable
private fun PantallaHistorialPendiente() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Aquí estará el historial de depósitos.")
    }
}

@Composable
private fun PantallaAjustesPendiente() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Aquí estarán los ajustes de Aris.")
    }
}
