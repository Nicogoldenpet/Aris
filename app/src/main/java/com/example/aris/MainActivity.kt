
package com.example.aris

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.example.aris.data.ArisDatabase
import com.example.aris.data.Deposito
import com.example.aris.data.DepositoDao
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var textToSpeech: TextToSpeech? = null
    private var voiceReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val depositoDao = ArisDatabase
            .obtenerInstancia(applicationContext)
            .depositoDao()

        textToSpeech = TextToSpeech(this) { status ->
            voiceReady = status == TextToSpeech.SUCCESS

            if (voiceReady) {
                textToSpeech?.language = Locale("es", "CO")
            }
        }

        setContent {
            MaterialTheme {
                ArisNavigation(
                    depositoDao = depositoDao,
                    onAnnouncePayment = { amount ->
                        if (voiceReady) {
                            val cantidadEnPalabras = amount.toWords()

                            val complementoPesos = when {
                                amount == 1 -> "peso"
                                amount >= 1_000_000 && amount % 1_000_000 == 0 -> "de pesos"
                                else -> "pesos"
                            }

                            textToSpeech?.speak(
                                "Has recibido $cantidadEnPalabras $complementoPesos",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "pago-${System.currentTimeMillis()}"
                            )
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroy()
    }
}



@Composable
fun ArisHome(
    depositoDao: DepositoDao,
    onAnnouncePayment: (Int) -> Unit,
    onVerHistorial: () -> Unit
) {
    val depositos by depositoDao
        .obtenerTodos()
        .collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    var importeTexto by rememberSaveable { mutableStateOf("") }

    val importeValido = importeTexto.toIntOrNull()?.let { it > 0 } == true
    val ahora = Calendar.getInstance()

    val totalHoy = depositos
        .filter {
            val cal = Calendar.getInstance().apply {
                timeInMillis = it.fechaHora
            }
            cal.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == ahora.get(Calendar.DAY_OF_YEAR)
        }
        .sumOf { it.importe }

    val totalMes = depositos
        .filter {
            val cal = Calendar.getInstance().apply {
                timeInMillis = it.fechaHora
            }
            cal.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == ahora.get(Calendar.MONTH)
        }
        .sumOf { it.importe }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aris",
                fontSize = 32.sp,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Tu asistente de pagos")
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total recibido hoy")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatPesos(totalHoy),
                        fontSize = 28.sp,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total recibido este mes")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatPesos(totalMes),
                        fontSize = 28.sp,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = importeTexto,
                onValueChange = { nuevoValor ->
                    if (nuevoValor.all { it.isDigit() } &&
                        nuevoValor.length <= 10
                    ) {
                        importeTexto = nuevoValor
                    }
                },
                label = { Text("Importe del depósito (COP)") },
                placeholder = { Text("Ej. 25000") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val importe = importeTexto.toIntOrNull()

                    if (importe != null && importe > 0) {
                        val deposito = Deposito(importe = importe)

                        coroutineScope.launch {
                            depositoDao.insertar(deposito)
                            onAnnouncePayment(deposito.importe)
                            importeTexto = ""
                        }
                    }
                },
                enabled = importeValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Registrar depósito", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Modo de prueba: no se ha recibido dinero real.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos depósitos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onVerHistorial) {
                    Text("Ver todo →")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (depositos.isEmpty()) {
                Text(
                    text = "Todavía no hay depósitos registrados.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                depositos.take(5).forEach { deposito ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Depósito recibido",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatFecha(deposito.fechaHora),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "+ ${formatPesos(deposito.importe)}",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}


fun formatPesos(amount: Int): String {
    return NumberFormat
        .getCurrencyInstance(Locale("es", "CO"))
        .format(amount)
}

fun formatFecha(timestamp: Long): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy - HH:mm",
        Locale("es", "CO")
    )

    return formatter.format(Date(timestamp))
}

fun Int.toWords(): String {
    if (this == 0) return "cero"

    val unidades = arrayOf(
        "", "uno", "dos", "tres", "cuatro",
        "cinco", "seis", "siete", "ocho", "nueve",
        "diez", "once", "doce", "trece", "catorce",
        "quince", "dieciséis", "diecisiete",
        "dieciocho", "diecinueve", "veinte",
        "veintiuno", "veintidós", "veintitrés",
        "veinticuatro", "veinticinco", "veintiséis",
        "veintisiete", "veintiocho", "veintinueve"
    )

    val decenas = arrayOf(
        "", "", "", "treinta", "cuarenta",
        "cincuenta", "sesenta", "setenta",
        "ochenta", "noventa"
    )

    val centenas = arrayOf(
        "", "ciento", "doscientos", "trescientos",
        "cuatrocientos", "quinientos", "seiscientos",
        "setecientos", "ochocientos", "novecientos"
    )

    fun grupo(n: Int): String {
        if (n == 0) return ""
        if (n <= 29) return unidades[n]

        if (n < 100) {
            val d = n / 10
            val u = n % 10
            return decenas[d] + if (u > 0) " y ${unidades[u]}" else ""
        }

        if (n == 100) return "cien"

        return centenas[n / 100] +
                if (n % 100 > 0) " ${grupo(n % 100)}" else ""
    }

    fun convertir(n: Int): String {
        if (n < 1000) return grupo(n)

        if (n < 1_000_000) {
            val miles = n / 1000
            val resto = n % 1000

            val textoMiles =
                if (miles == 1) "mil"
                else "${grupo(miles).replace("veintiuno", "veintiún").replace("uno", "un")} mil"

            return textoMiles +
                    if (resto > 0) " ${grupo(resto)}" else ""
        }

        val millones = n / 1_000_000
        val resto = n % 1_000_000

        val textoMillones =
            if (millones == 1) "un millón"
            else "${convertir(millones).replace("veintiuno", "veintiún").replace("uno", "un")} millones"

        return textoMillones +
                if (resto > 0) " ${convertir(resto)}" else ""
    }

    return convertir(this).let { texto ->
        when {
            texto.endsWith("veintiuno") ->
                texto.removeSuffix("veintiuno") + "veintiún"

            texto.endsWith(" y uno") ->
                texto.removeSuffix("uno") + "un"

            texto.endsWith("uno") ->
                texto.removeSuffix("uno") + "un"

            else -> texto
        }.trim()
    }
}
