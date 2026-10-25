package com.example.aris

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aris.data.DepositoDao
import java.util.Calendar

@Composable
fun PantallaHistorial(
    depositoDao: DepositoDao
) {
    var periodo by rememberSaveable { mutableStateOf("Mes") }
    var mesSeleccionado by rememberSaveable {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH))
    }
    var anioSeleccionado by rememberSaveable {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR))
    }
    var antiguosPrimero by rememberSaveable { mutableStateOf(false) }
    var cantidadVisible by rememberSaveable { mutableIntStateOf(5) }
    var menuMesAbierto by remember { mutableStateOf(false) }

    val inicioPeriodo = remember(periodo, mesSeleccionado, anioSeleccionado) {
        Calendar.getInstance().apply {
            when (periodo) {
                "Día" -> {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                "Mes" -> {
                    set(Calendar.YEAR, anioSeleccionado)
                    set(Calendar.MONTH, mesSeleccionado)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                "Año" -> {
                    set(Calendar.YEAR, anioSeleccionado)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
        }.timeInMillis
    }

    val finPeriodo = remember(periodo, inicioPeriodo) {
        Calendar.getInstance().apply {
            timeInMillis = inicioPeriodo
            when (periodo) {
                "Día" -> add(Calendar.DAY_OF_MONTH, 1)
                "Mes" -> add(Calendar.MONTH, 1)
                "Año" -> add(Calendar.YEAR, 1)
            }
        }.timeInMillis
    }

    val depositos by depositoDao.obtenerFiltrados(
        inicio = inicioPeriodo,
        fin = finPeriodo,
        antiguosPrimero = if (antiguosPrimero) 1 else 0,
        limite = cantidadVisible,
        desplazamiento = 0
    ).collectAsState(initial = emptyList())

    val nombresMeses = listOf(
        "Enero", "Febrero", "Marzo", "Abril",
        "Mayo", "Junio", "Julio", "Agosto",
        "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Historial de depósitos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Consulta tus ingresos por fecha.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Día", "Mes", "Año").forEach { opcion ->
                FilterChip(
                    selected = periodo == opcion,
                    onClick = {
                        periodo = opcion
                        cantidadVisible = 5
                    },
                    label = { Text(opcion) }
                )
            }
        }

        if (periodo != "Día") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (periodo == "Mes") {
                    Box {
                        TextButton(
                            onClick = { menuMesAbierto = true }
                        ) {
                            Text(nombresMeses[mesSeleccionado])
                        }

                        DropdownMenu(
                            expanded = menuMesAbierto,
                            onDismissRequest = { menuMesAbierto = false }
                        ) {
                            nombresMeses.forEachIndexed { indice, nombre ->
                                DropdownMenuItem(
                                    text = { Text(nombre) },
                                    onClick = {
                                        mesSeleccionado = indice
                                        menuMesAbierto = false
                                        cantidadVisible = 5
                                    }
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            anioSeleccionado--
                            cantidadVisible = 5
                        }
                    ) {
                        Text("−")
                    }

                    Text(anioSeleccionado.toString())

                    TextButton(
                        onClick = {
                            anioSeleccionado++
                            cantidadVisible = 5
                        }
                    ) {
                        Text("+")
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                antiguosPrimero = !antiguosPrimero
                cantidadVisible = 5
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (antiguosPrimero) {
                    "Orden: más antiguos primero"
                } else {
                    "Orden: más recientes primero"
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (depositos.isEmpty()) {
            Text(
                text = "No hay depósitos en el periodo seleccionado.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
        } else {
            depositos.forEach { deposito ->
                ListItem(
                    headlineContent = {
                        Text("Depósito recibido")
                    },
                    supportingContent = {
                        Text(formatFecha(deposito.fechaHora))
                    },
                    trailingContent = {
                        Text(
                            "+ ${formatPesos(deposito.importe)}",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                )

                HorizontalDivider()
            }

            if (depositos.size == cantidadVisible) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { cantidadVisible += 5 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cargar 5 más")
                }
            }
        }
    }
}
