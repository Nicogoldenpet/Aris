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
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape

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

    // Detectamos el tema actual.
    val esModoOscuro =
        MaterialTheme.colorScheme.background.luminance() < 0.5f

// Colores propios de Aris.
    val colorAcento = if (esModoOscuro) {
        Color(0xFF4DD8C7)
    } else {
        Color(0xFF168F83)
    }

    val colorAcentoSuave = if (esModoOscuro) {
        Color(0xFF244B4A)
    } else {
        Color(0xFFDDF5F1)
    }

    val colorTarjeta = if (esModoOscuro) {
        Color(0xFF1B2D3A)
    } else {
        Color(0xFFFFFFFF)
    }

    val colorTextoSecundario = if (esModoOscuro) {
        Color(0xFFA8BBC9)
    } else {
        Color(0xFF526779)
    }

    val colorBorde = if (esModoOscuro) {
        Color(0xFF344A59)
    } else {
        Color(0xFFD8E2E8)
    }

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
                    label = {
                        Text(opcion)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorAcentoSuave,
                        selectedLabelColor = colorAcento,
                        containerColor = colorTarjeta,
                        labelColor = colorTextoSecundario
                    )
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
                            onDismissRequest = { menuMesAbierto = false },
                            containerColor = colorTarjeta,
                            tonalElevation = 4.dp
                        ) {
                            nombresMeses.forEachIndexed { indice, nombre ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = nombre,
                                            color = if (mesSeleccionado == indice) {
                                                colorAcento
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (mesSeleccionado == indice) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )
                                    },
                                    onClick = {
                                        mesSeleccionado = indice
                                        menuMesAbierto = false
                                        cantidadVisible = 5
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface
                                    )
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
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorAcento
                        )
                    ) {
                        Text("−")
                    }

                    Text(anioSeleccionado.toString())

                    TextButton(
                        onClick = {
                            anioSeleccionado++
                            cantidadVisible = 5
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorAcento
                        )
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorTarjeta
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Depósito recibido",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text(
                                text = formatFecha(deposito.fechaHora),
                                color = colorTextoSecundario
                            )
                        },
                        trailingContent = {
                            Text(
                                text = "+ ${formatPesos(deposito.importe)}",
                                color = colorAcento,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (depositos.size == cantidadVisible) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { cantidadVisible += 5 },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorAcento
                    ),
                    border = BorderStroke(1.dp, colorBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cargar 5 más")
                }
            }
        }
    }
}
