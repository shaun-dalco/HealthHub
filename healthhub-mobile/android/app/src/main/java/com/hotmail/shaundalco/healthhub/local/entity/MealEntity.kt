package com.hotmail.shaundalco.healthhub.local.entity

import androidx.room.*

@Entity(
    tableName = "meals",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["id"],
            childColumns = ["day_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["day_id"]),                 // idx_meals_day
        Index(value = ["day_id", "meal_type"], unique = true) // UNIQUE(day_id, meal_type)
    ]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "day_id") val dayId: Long,

    @ColumnInfo(name = "meal_type") val mealType: MealType
)
