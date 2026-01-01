package com.hotmail.shaundalco.healthhub.local.entity

import androidx.room.*

@Entity(
    tableName = "foods",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["meal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["meal_id"]) // idx_foods_meal
    ]
)
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "meal_id") val mealId: Long,

    val name: String,

    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val sugar: Double = 0.0,

    @ColumnInfo(name = "saturated_fat") val saturatedFat: Double = 0.0,
    val sodium: Double = 0.0,

    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
