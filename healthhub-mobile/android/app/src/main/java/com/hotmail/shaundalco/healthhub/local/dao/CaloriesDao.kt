package com.hotmail.shaundalco.healthhub.local.dao

import androidx.room.*
import com.hotmail.shaundalco.healthhub.local.entity.DayEntity
import com.hotmail.shaundalco.healthhub.local.entity.FoodEntity
import com.hotmail.shaundalco.healthhub.local.entity.MealEntity
import com.hotmail.shaundalco.healthhub.local.entity.MealType
import com.hotmail.shaundalco.healthhub.local.model.FoodRow
import kotlinx.coroutines.flow.Flow

@Dao
interface CaloriesDao {

    // ----- Days -----
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDay(day: DayEntity): Long

    @Query("SELECT id FROM days WHERE day = :day LIMIT 1")
    suspend fun getDayId(day: String): Long?

    suspend fun getOrCreateDayId(day: String): Long {
        val existing = getDayId(day)
        if (existing != null) return existing
        val inserted = insertDay(DayEntity(day = day))
        if (inserted != -1L) return inserted
        // race: someone else inserted between calls
        return getDayId(day) ?: error("Failed to create day row for $day")
    }

    // ----- Meals -----
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Query("""
        SELECT id FROM meals
        WHERE day_id = :dayId AND meal_type = :mealType
        LIMIT 1
    """)
    suspend fun getMealId(dayId: Long, mealType: MealType): Long?

    suspend fun getOrCreateMealId(dayId: Long, mealType: MealType): Long {
        val existing = getMealId(dayId, mealType)
        if (existing != null) return existing
        val inserted = insertMeal(MealEntity(dayId = dayId, mealType = mealType))
        if (inserted != -1L) return inserted
        return getMealId(dayId, mealType) ?: error("Failed to create meal row")
    }

    // ----- Foods -----

    @Query("""
        SELECT
          d.id  AS dayId,
          d.day AS day,

          m.id AS mealId,
          m.meal_type AS mealType,

          f.id AS foodId,
          f.name AS name,

          f.calories AS calories,
          f.protein AS protein,
          f.carbs AS carbs,
          f.fats AS fats,
          f.sugar AS sugar,
          f.saturated_fat AS saturatedFat,
          f.sodium AS sodium,

          f.sort_order AS sortOrder
        FROM foods f
        JOIN meals m ON m.id = f.meal_id
        JOIN days  d ON d.id = m.day_id
        ORDER BY d.day DESC, m.meal_type ASC, f.sort_order ASC, f.id ASC
    """)
    fun observeAllFoodRows(): Flow<List<FoodRow>>

    @Insert
    suspend fun insertFood(food: FoodEntity): Long

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Query("DELETE FROM foods WHERE id = :foodId")
    suspend fun deleteFood(foodId: Long)

    @Query("SELECT * FROM foods WHERE meal_id = :mealId ORDER BY sort_order, id")
    suspend fun listFoodsForMeal(mealId: Long): List<FoodEntity>

    @Query("DELETE FROM foods")
    suspend fun deleteAllFoods()

    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()

    @Query("DELETE FROM days")
    suspend fun deleteAllDays()

    suspend fun nukeAllCaloriesData() {
        // order matters if FK constraints are enabled
        deleteAllFoods()
        deleteAllMeals()
        deleteAllDays()
    }

    // Convenience: add food by day + meal_type
    @Transaction
    suspend fun addFood(
        day: String,
        mealType: MealType,
        food: FoodEntity
    ): Long {
        val dayId = getOrCreateDayId(day)
        val mealId = getOrCreateMealId(dayId, mealType)
        return insertFood(food.copy(mealId = mealId))
    }
}
