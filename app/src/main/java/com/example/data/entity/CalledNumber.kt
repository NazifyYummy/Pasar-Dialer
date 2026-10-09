package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "called_numbers")
data class CalledNumber(
    @PrimaryKey val phoneNumber: String,
    val calledAt: Long = System.currentTimeMillis()
)
