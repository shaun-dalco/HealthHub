package com.hotmail.shaundalco.healthhub.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.hotmail.shaundalco.healthhub.R
import com.hotmail.shaundalco.healthhub.local.db.AppDb
import com.hotmail.shaundalco.healthhub.local.entity.FoodEntity
import com.hotmail.shaundalco.healthhub.local.entity.MealType
import com.hotmail.shaundalco.healthhub.local.model.FoodRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CaloriesDbFragment : Fragment(R.layout.fragment_calories_db) {

    private lateinit var db: AppDb
    private val adapter = FoodRowAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If you already have a singleton DB, use that instead of creating here.
        db = Room.databaseBuilder(requireContext(), AppDb::class.java, "health.db").build()

        val etDay = view.findViewById<EditText>(R.id.etDay)
        val spMealType = view.findViewById<Spinner>(R.id.spMealType)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etCalories = view.findViewById<EditText>(R.id.etCalories)
        val etProtein = view.findViewById<EditText>(R.id.etProtein)
        val etCarbs = view.findViewById<EditText>(R.id.etCarbs)
        val etFats = view.findViewById<EditText>(R.id.etFats)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnClear = view.findViewById<Button>(R.id.btnClear)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        // Spinner options
        val mealTypes = MealType.values().map { it.name }
        spMealType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            mealTypes
        )

        // Recycler
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Observe everything
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                db.caloriesDao().observeAllFoodRows().collect { rows ->
                    adapter.submitList(rows)
                    tvStatus.text = "Rows: ${rows.size}"
                }
            }
        }

        btnAdd.setOnClickListener {
            val day = etDay.text?.toString()?.trim().orEmpty()
            val mealTypeStr = spMealType.selectedItem?.toString() ?: "breakfast"
            val name = etName.text?.toString()?.trim().orEmpty()

            if (day.isBlank()) {
                tvStatus.text = "Day is required (YYYY-MM-DD)."
                return@setOnClickListener
            }
            if (name.isBlank()) {
                tvStatus.text = "Food name is required."
                return@setOnClickListener
            }

            val calories = etCalories.text.toString().toDoubleOrNull() ?: 0.0
            val protein = etProtein.text.toString().toDoubleOrNull() ?: 0.0
            val carbs = etCarbs.text.toString().toDoubleOrNull() ?: 0.0
            val fats = etFats.text.toString().toDoubleOrNull() ?: 0.0

            val mealType = runCatching { MealType.valueOf(mealTypeStr) }.getOrElse { MealType.breakfast }

            tvStatus.text = "Adding..."
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        db.caloriesDao().addFood(
                            day = day,
                            mealType = mealType,
                            food = FoodEntity(
                                mealId = 0, // overwritten in addFood()
                                name = name,
                                calories = calories,
                                protein = protein,
                                carbs = carbs,
                                fats = fats
                            )
                        )
                    }
                    // Clear only the food fields (keep day/meal for fast entry)
                    etName.setText("")
                    etCalories.setText("")
                    etProtein.setText("")
                    etCarbs.setText("")
                    etFats.setText("")
                    tvStatus.text = "Added."
                } catch (e: Exception) {
                    tvStatus.text = "Add failed: ${e.message ?: "unknown error"}"
                }
            }
        }

        btnClear.setOnClickListener {
            tvStatus.text = "Clearing..."
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        db.caloriesDao().nukeAllCaloriesData()
                    }
                    tvStatus.text = "Cleared."
                } catch (e: Exception) {
                    tvStatus.text = "Clear failed: ${e.message ?: "unknown error"}"
                }
            }
        }
    }
}

/** RecyclerView adapter showing joined rows */
private class FoodRowAdapter :
    ListAdapter<FoodRow, FoodRowVH>(Diff) {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FoodRowVH {
        val tv = TextView(parent.context).apply {
            setPadding(12, 10, 12, 10)
        }
        return FoodRowVH(tv)
    }

    override fun onBindViewHolder(holder: FoodRowVH, position: Int) {
        holder.bind(getItem(position))
    }

    private object Diff : DiffUtil.ItemCallback<FoodRow>() {
        override fun areItemsTheSame(oldItem: FoodRow, newItem: FoodRow): Boolean =
            oldItem.foodId == newItem.foodId

        override fun areContentsTheSame(oldItem: FoodRow, newItem: FoodRow): Boolean =
            oldItem == newItem
    }
}

private class FoodRowVH(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
    fun bind(r: FoodRow) {
        tv.text =
            "${r.day} • ${r.mealType}\n" +
                    "${r.name}\n" +
                    "cal ${fmt(r.calories)} | p ${fmt(r.protein)} | c ${fmt(r.carbs)} | f ${fmt(r.fats)}"
    }

    private fun fmt(v: Double): String =
        if (v % 1.0 == 0.0) v.toInt().toString() else String.format("%.1f", v)
}
