
package com.example.aris

import com.example.aris.ui.theme.ArisTheme
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
            ArisTheme {
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
    val depositos by depositoDao.obtenerTodos().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var importeTexto by rememberSaveable { mutableStateOf("") }
    val importeValido = importeTexto.toIntOrNull()?.let { it > 0 } == true

    val ahora = Calendar.getInstance()

    val totalHoy = depositos.filter {
        val fecha = Calendar.getInstance().apply { timeInMillis = it.fechaHora }
        fecha.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) &&
                fecha.get(Calendar.DAY_OF_YEAR) == ahora.get(Calendar.DAY_OF_YEAR)
    }.sumOf { it.importe }

    val totalMes = depositos.filter {
        val fecha = Calendar.getInstance().apply { timeInMillis = it.fechaHora }
        fecha.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) &&
                fecha.get(Calendar.MONTH) == ahora.get(Calendar.MONTH)
    }.sumOf { it.importe }


    val modoOscuro = androidx.compose.foundation.isSystemInDarkTheme()

    val azulAris = if (modoOscuro) Color(0xFF142B40) else Color(0xFF163A5F)
    val azulInput = if (modoOscuro) Color(0xFF163A5F) else Color.White
    val verdeAgua = Color(0xFF35C6B0)
    val verdeSuave = if (modoOscuro) Color(0xFF25483F) else Color(0xFFE3F7F3)
    val grisSuave = if (modoOscuro) Color(0xFF35C6B0) else Color(0xFFE3F7F3)
    val grisTexto = if (modoOscuro) Color(0xFFB7C6D1) else Color(0xFF647582)
    val grisTextoDos = if (modoOscuro) Color(0xFF1B2B3A) else Color(0xFF647582)
    val grisCard = if (modoOscuro) Color(0xFF647582) else Color(0xFFB7C6D1)

    val fondoAris = if (modoOscuro) Color(0xFF101C28) else Color(0xFFF4F8FA)
    val tarjetaMes = if (modoOscuro) Color(0xFF1B2B3A) else Color.White
    val textoPrincipal = if (modoOscuro) Color(0xFFEAF2F7) else Color(0xFF203344)
    val textoOscuro = Color(0xFF203344)


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = fondoAris
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = verdeAgua,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "A",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = azulAris
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Aris",
                        fontSize = 29.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textoPrincipal
                    )
                    Text(
                        text = "Tu asistente de pagos",
                        fontSize = 14.sp,
                        color = grisTexto
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Total recibido hoy
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = azulAris)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "TOTAL RECIBIDO HOY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD6E8F5)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = formatPesos(totalHoy),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingresos registrados durante el día",
                        fontSize = 13.sp,
                        color = verdeAgua
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total del mes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = tarjetaMes),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total recibido este mes",
                            fontSize = 14.sp,
                            color = grisTexto
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = formatPesos(totalMes),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textoPrincipal
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = verdeAgua,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "COP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textoOscuro
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Formulario de registro
            Text(
                text = "Registrar un ingreso",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = textoPrincipal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Introduce el importe del depósito que recibiste.",
                fontSize = 14.sp,
                color = grisTexto
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = importeTexto,
                onValueChange = { nuevoValor ->
                    if (nuevoValor.all { it.isDigit() }) {
                        importeTexto = nuevoValor
                    }
                },
                label = {
                    Text(
                        text = "Importe en pesos",
                        color = Color(0xFF647582)
                    )
                },
                placeholder = {
                    Text(
                        text = "Ej. 25000",
                        color = Color(0xFF84939E)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (modoOscuro) Color(0xFFEAF2F7) else Color(0xFF203344),
                    unfocusedTextColor = if (modoOscuro) Color(0xFFEAF2F7) else Color(0xFF203344),
                    focusedContainerColor = azulInput,
                    unfocusedContainerColor = azulInput,
                    focusedBorderColor = verdeAgua,
                    unfocusedBorderColor = if (modoOscuro) Color(0xFF1E507C) else Color(0xFFB8C5CE),
                    cursorColor = verdeAgua,
                    focusedLabelColor = verdeAgua,
                    unfocusedLabelColor = grisTexto
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val importe = importeTexto.toIntOrNull()

                    if (importe != null && importe > 0) {
                        coroutineScope.launch {
                            depositoDao.insertar(Deposito(importe = importe))
                            onAnnouncePayment(importe)
                            importeTexto = ""
                        }
                    }
                },
                enabled = importeValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = verdeAgua,
                    contentColor = azulAris,
                    disabledContainerColor = if (modoOscuro) Color(0xFF354653) else Color(0xFFDDE5E9),
                    disabledContentColor = if (modoOscuro) Color(0xFFEAF2F7) else Color(0xFF526B72)
                )
            ) {
                Text(
                    text = "Registrar depósito",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Modo de prueba: este registro no confirma un pago real.",
                fontSize = 12.sp,
                color = grisTexto,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Últimos depósitos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos depósitos",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = textoPrincipal
                )

                TextButton(onClick = onVerHistorial) {
                    Text(
                        text = "Ver todo →",
                        color = textoPrincipal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (depositos.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aún no hay depósitos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = azulAris
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Cuando registres un ingreso, aparecerá aquí.",
                            fontSize = 13.sp,
                            color = grisTexto,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = tarjetaMes)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        depositos.take(5).forEachIndexed { indice, deposito ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = grisSuave,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "+",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = azulAris
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Depósito recibido",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textoPrincipal
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = formatFecha(deposito.fechaHora),
                                        fontSize = 12.sp,
                                        color = grisTexto
                                    )
                                }

                                Text(
                                    text = "+ ${formatPesos(deposito.importe)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = verdeAgua
                                )
                            }

                            if (indice < depositos.take(5).lastIndex) {
                                HorizontalDivider(color = Color(0xFFE8EEF2))
                            }
                        }
                    }
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
