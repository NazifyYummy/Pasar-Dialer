package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_later_numbers")
data class CallLaterNumber(
    @PrimaryKey val phoneNumber: String,
    val addedAt: Long = System.currentTimeMillis()
)
