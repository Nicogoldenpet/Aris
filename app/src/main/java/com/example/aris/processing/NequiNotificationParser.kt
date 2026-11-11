package com.example.aris.processing

object NequiNotificationParser {

    // Reconocemos las dos formas de anunciar transferencias recibidas.
    private val patronMonto = Regex(
        """(?:te envió|te enviaron)\s+\$?\s*([0-9]+(?:[.,][0-9]{3})*)""",
        RegexOption.IGNORE_CASE
    )

    fun obtenerMonto(titulo: String, mensaje: String): Int? {

        // Buscamos el importe dentro del mensaje.
        val coincidencia = patronMonto.find(mensaje)
            ?: return null

        val cantidadTexto = coincidencia.groupValues[1]

        // Nequi utiliza importes enteros en pesos colombianos.
        // Eliminamos los separadores de miles.
        val cantidadLimpia = cantidadTexto
            .replace(".", "")
            .replace(",", "")

        return cantidadLimpia.toIntOrNull()
    }
}
