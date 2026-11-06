package com.example.aris.processing

object NequiNotificationParser {

    fun obtenerMonto(titulo: String, mensaje: String): Int? {

        // Por ahora solo identificamos transferencias recibidas.
        val esTransferenciaRecibida =
            mensaje.contains("te envió", ignoreCase = true)

        if (!esTransferenciaRecibida) {
            return null
        }

        // Busca la cantidad que aparece después de "te envió".
        val patron = Regex(
            """te envió\s+\$?\s*([\d.,]+)""",
            RegexOption.IGNORE_CASE
        )

        val coincidencia = patron.find(mensaje) ?: return null

        val cantidadTexto = coincidencia.groupValues[1]

        // Nequi muestra los importes en pesos enteros.
        // Quitamos puntos y comas usados como separadores.
        val cantidadLimpia = cantidadTexto.replace(".", "").replace(",", "")

        return cantidadLimpia.toIntOrNull()
    }
}