const Database = require("better-sqlite3");

const db = new Database("healthhub.db");

db.pragma("foreign_keys = ON");

/* ---------------- STEPS ---------------- */
db.exec(`
  CREATE TABLE IF NOT EXISTS steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day TEXT NOT NULL UNIQUE,
    steps INTEGER NOT NULL
  );
`);

/* ---------------- HEART RATE ---------------- */
db.exec(`
  CREATE TABLE IF NOT EXISTS hr (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day TEXT NOT NULL UNIQUE,
    hr INTEGER NOT NULL
  );
`);

/* ---------------- CALORIES ---------------- */

/* Days */
db.exec(`
  CREATE TABLE IF NOT EXISTS days (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day TEXT NOT NULL UNIQUE
  );
`);

/* Meals */
db.exec(`
  CREATE TABLE IF NOT EXISTS meals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day_id INTEGER NOT NULL,
    meal_type TEXT NOT NULL, -- breakfast | lunch | dinner | snacks
    UNIQUE(day_id, meal_type),
    FOREIGN KEY(day_id) REFERENCES days(id) ON DELETE CASCADE
  );
`);

/* Foods */
db.exec(`
  CREATE TABLE IF NOT EXISTS foods (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    meal_id INTEGER NOT NULL,
    name TEXT NOT NULL,

    calories REAL NOT NULL DEFAULT 0,
    protein REAL NOT NULL DEFAULT 0,
    carbs REAL NOT NULL DEFAULT 0,
    fats REAL NOT NULL DEFAULT 0,
    sugar REAL NOT NULL DEFAULT 0,
    saturated_fat REAL NOT NULL DEFAULT 0,
    sodium REAL NOT NULL DEFAULT 0,

    sort_order INTEGER NOT NULL DEFAULT 0,

    FOREIGN KEY(meal_id) REFERENCES meals(id) ON DELETE CASCADE
  );
`);

/* Indexes */
db.exec(`
  CREATE INDEX IF NOT EXISTS idx_meals_day ON meals(day_id);
  CREATE INDEX IF NOT EXISTS idx_foods_meal ON foods(meal_id);
`);

module.exports = db;
