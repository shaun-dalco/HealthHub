package com.hotmail.shaundalco.healthhub.local.model

data class FoodRow(
    val dayId: Long,
    val day: String,
    val mealId: Long,
    val mealType: String,

    val foodId: Long,
    val name: String,

    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val sugar: Double,
    val saturatedFat: Double,
    val sodium: Double,

    val sortOrder: Int
)