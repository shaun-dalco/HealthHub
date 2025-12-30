const express = require("express");
const router = express.Router();
const db = require("../db");

/**
 * Helpers
 */
function getOrCreateDayId(day) {
  const row = db.prepare("SELECT id FROM days WHERE day = ?").get(day);
  if (row) return row.id;

  const info = db.prepare("INSERT INTO days (day) VALUES (?)").run(day);
  return info.lastInsertRowid;
}

function getOrCreateMealId(dayId, mealType) {
  const row = db
    .prepare("SELECT id FROM meals WHERE day_id = ? AND meal_type = ?")
    .get(dayId, mealType);

  if (row) return row.id;

  const info = db
    .prepare("INSERT INTO meals (day_id, meal_type) VALUES (?, ?)")
    .run(dayId, mealType);

  return info.lastInsertRowid;
}

/**
 * GET /api/calories
 * Daily totals (for charts / list)
 */
router.get("/", (req, res) => {
  const rows = db
    .prepare(
      `
      SELECT
        d.day AS day,
        COALESCE(SUM(f.calories), 0)        AS calories,
        COALESCE(SUM(f.protein), 0)         AS protein,
        COALESCE(SUM(f.carbs), 0)           AS carbs,
        COALESCE(SUM(f.fats), 0)            AS fats,
        COALESCE(SUM(f.sugar), 0)           AS sugar,
        COALESCE(SUM(f.saturated_fat), 0)   AS saturated_fat,
        COALESCE(SUM(f.sodium), 0)          AS sodium
      FROM days d
      LEFT JOIN meals m ON m.day_id = d.id
      LEFT JOIN foods f ON f.meal_id = m.id
      GROUP BY d.id
      ORDER BY d.day ASC
    `
    )
    .all();

  res.json(rows);
});

/**
 * GET /api/calories/day/:day
 * One day, nested (meals -> foods)
 */
router.get("/day/:day", (req, res) => {
  const { day } = req.params;

  const dayRow = db.prepare("SELECT id, day FROM days WHERE day = ?").get(day);
  if (!dayRow) {
    // Return an empty structure so UI can render immediately
    return res.json({
      day,
      meals: { breakfast: [], lunch: [], dinner: [], snacks: [] },
    });
  }

  const foods = db
    .prepare(
      `
      SELECT
        m.meal_type,
        f.id,
        f.name,
        f.calories,
        f.protein,
        f.carbs,
        f.fats,
        f.sugar,
        f.saturated_fat,
        f.sodium,
        f.sort_order
      FROM meals m
      LEFT JOIN foods f ON f.meal_id = m.id
      WHERE m.day_id = ?
      ORDER BY m.meal_type ASC, f.sort_order ASC, f.id ASC
    `
    )
    .all(dayRow.id);

  const meals = { breakfast: [], lunch: [], dinner: [], snacks: [] };

  for (const r of foods) {
    if (!meals[r.meal_type]) meals[r.meal_type] = [];
    // If there are meal rows but no foods, f.id will be null because of LEFT JOIN
    if (r.id == null) continue;

    meals[r.meal_type].push({
      id: r.id,
      name: r.name,
      calories: r.calories,
      protein: r.protein,
      carbs: r.carbs,
      fats: r.fats,
      sugar: r.sugar,
      saturated_fat: r.saturated_fat,
      sodium: r.sodium,
      sort_order: r.sort_order,
    });
  }

  res.json({ day: dayRow.day, meals });
});

/**
 * POST /api/calories/day/:day/food
 * Add a single food item to a meal
 *
 * Body:
 * {
 *   "mealType": "breakfast",
 *   "name": "Oats",
 *   "calories": 250,
 *   "protein": 9,
 *   "carbs": 45,
 *   "fats": 4,
 *   "sugar": 1,
 *   "saturated_fat": 0.5,
 *   "sodium": 5
 * }
 */
router.post("/day/:day/food", (req, res) => {
  const { day } = req.params;
  const {
    mealType,
    name,
    calories = 0,
    protein = 0,
    carbs = 0,
    fats = 0,
    sugar = 0,
    saturated_fat = 0,
    sodium = 0,
  } = req.body || {};

  const allowed = new Set(["breakfast", "lunch", "dinner", "snacks"]);
  if (!allowed.has(mealType)) {
    return res.status(400).json({ error: "mealType must be breakfast|lunch|dinner|snacks" });
  }
  if (!name || typeof name !== "string") {
    return res.status(400).json({ error: "name is required" });
  }

  const insert = db.transaction(() => {
    const dayId = getOrCreateDayId(day);
    const mealId = getOrCreateMealId(dayId, mealType);

    const max = db
      .prepare("SELECT COALESCE(MAX(sort_order), -1) AS maxSort FROM foods WHERE meal_id = ?")
      .get(mealId).maxSort;

    const info = db
      .prepare(
        `
        INSERT INTO foods (
          meal_id, name,
          calories, protein, carbs, fats,
          sugar, saturated_fat, sodium,
          sort_order
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `
      )
      .run(
        mealId,
        name.trim(),
        calories,
        protein,
        carbs,
        fats,
        sugar,
        saturated_fat,
        sodium,
        max + 1
      );

    return info.lastInsertRowid;
  });

  try {
    const id = insert();
    res.json({ ok: true, id });
  } catch (e) {
    res.status(500).json({ error: "Failed to add food", details: String(e) });
  }
});

/**
 * DELETE /api/calories/food/:id
 * Remove a single food item
 */
router.delete("/food/:id", (req, res) => {
  const id = Number(req.params.id);
  if (!Number.isFinite(id)) return res.status(400).json({ error: "Invalid id" });

  const info = db.prepare("DELETE FROM foods WHERE id = ?").run(id);

  if (info.changes === 0) {
    return res.status(404).json({ error: "Food not found" });
  }

  res.json({ ok: true });
});

module.exports = router;
