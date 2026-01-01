package com.hotmail.shaundalco.healthhub.local.db

import androidx.room.TypeConverter
import com.hotmail.shaundalco.healthhub.local.entity.MealType

class Converters {
    @TypeConverter fun mealTypeToString(v: MealType): String = v.name
    @TypeConverter fun stringToMealType(v: String): MealType = MealType.valueOf(v)
}
