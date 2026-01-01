package com.hotmail.shaundalco.healthhub.local.entity

import androidx.room.*

@Entity(
    tableName = "days",
    indices = [
        Index(value = ["day"], unique = true)
    ]
)
data class DayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: String // "YYYY-MM-DD"
)
