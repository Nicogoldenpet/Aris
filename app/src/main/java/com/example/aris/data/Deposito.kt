package com.example.aris.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "depositos")
data class Deposito(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val importe: Int,

    val fechaHora: Long = System.currentTimeMillis()
)