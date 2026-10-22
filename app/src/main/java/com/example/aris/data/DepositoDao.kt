package com.example.aris.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositoDao {

    @Insert
    suspend fun insertar(deposito: Deposito)

    @Query("SELECT * FROM depositos ORDER BY fechaHora DESC")
    fun obtenerTodos(): Flow<List<Deposito>>

    @Query("SELECT SUM(importe) FROM depositos WHERE fechaHora >= :inicio AND fechaHora < :fin")
    fun obtenerTotalEntreFechas(
        inicio: Long,
        fin: Long
    ): Flow<Long?>

    @Query("""
    SELECT * FROM depositos
    WHERE fechaHora >= :inicio AND fechaHora < :fin
    ORDER BY
        CASE WHEN :antiguosPrimero = 1 THEN fechaHora END ASC,
        CASE WHEN :antiguosPrimero = 0 THEN fechaHora END DESC,
        id DESC
    LIMIT :limite OFFSET :desplazamiento
""")
    fun obtenerFiltrados(
        inicio: Long,
        fin: Long,
        antiguosPrimero: Int,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<Deposito>>
}