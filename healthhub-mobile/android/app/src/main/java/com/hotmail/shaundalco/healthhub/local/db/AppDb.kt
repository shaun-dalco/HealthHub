package com.hotmail.shaundalco.healthhub.local.db

import androidx.room.*
import com.hotmail.shaundalco.healthhub.local.dao.CaloriesDao
import com.hotmail.shaundalco.healthhub.local.entity.DayEntity
import com.hotmail.shaundalco.healthhub.local.entity.FoodEntity
import com.hotmail.shaundalco.healthhub.local.entity.MealEntity

@Database(
    entities = [DayEntity::class, MealEntity::class, FoodEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun caloriesDao(): CaloriesDao
}
