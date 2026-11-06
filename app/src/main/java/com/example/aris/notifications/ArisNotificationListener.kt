
package com.example.aris.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.aris.data.ArisDatabase
import com.example.aris.data.Deposito
import com.example.aris.processing.NequiNotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ArisNotificationListener : NotificationListenerService() {

    private lateinit var depositoDao: com.example.aris.data.DepositoDao

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var textToSpeech: TextToSpeech? = null
    private var voiceReady = false

    override fun onCreate() {
        super.onCreate()

        // Conectamos el servicio con la base de datos.
        depositoDao = ArisDatabase
            .obtenerInstancia(applicationContext)
            .depositoDao()

        // Inicializamos el motor de voz del servicio.
        textToSpeech = TextToSpeech(this) { status ->

            voiceReady = status == TextToSpeech.SUCCESS

            if (voiceReady) {
                textToSpeech?.language = Locale("es", "CO")
                Log.d("ArisListener", "Motor de voz preparado")
            } else {
                Log.e("ArisListener", "No se pudo inicializar el motor de voz")
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.d("ArisListener", "Aris ya está escuchando notificaciones")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // Solo procesamos notificaciones de Nequi.
        if (sbn.packageName != "com.nequi.MobileApp") {
            return
        }

        val extras = sbn.notification.extras

        val titulo = extras.getCharSequence(Notification.EXTRA_TITLE)
            ?.toString() ?: ""

        val texto = extras.getCharSequence(Notification.EXTRA_TEXT)
            ?.toString() ?: ""

        val textoAmpliado = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?.toString() ?: ""

        // Preferimos el texto normal; usamos el ampliado como alternativa.
        val mensaje = texto.ifBlank { textoAmpliado }

        Log.d("ArisListener", "Notificación de Nequi detectada")

        val monto = NequiNotificationParser.obtenerMonto(titulo, mensaje)

        if (monto == null) {
            Log.d("ArisListener", "Notificación no reconocida como transferencia")
            return
        }

        Log.d("ArisListener", "Posible pago detectado: $monto pesos")

        // Guardamos el depósito en segundo plano.
        serviceScope.launch {
            try {
                depositoDao.insertar(
                    Deposito(importe = monto)
                )

                Log.d("ArisListener", "Depósito guardado correctamente")

                // Anunciamos el pago después de guardarlo.
                withContext(Dispatchers.Main) {
                    if (voiceReady) {
                        textToSpeech?.speak(
                            "Has recibido $monto pesos",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "pago-${System.currentTimeMillis()}"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("ArisListener", "Error al guardar el depósito", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        Log.d("ArisListener", "Servicio de Aris detenido")
    }
}
