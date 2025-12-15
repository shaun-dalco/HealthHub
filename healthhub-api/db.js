const Database = require("better-sqlite3");

const db = new Database("healthhub.db");

db.exec(`
  CREATE TABLE IF NOT EXISTS steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day TEXT NOT NULL UNIQUE,
    steps INTEGER NOT NULL
  );
`);

db.exec(`
  CREATE TABLE IF NOT EXISTS calories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day TEXT NOT NULL UNIQUE,
    calories INTEGER NOT NULL,
    protein INTEGER NOT NULL,
    fat INTEGER NOT NULL,
    carbs INTEGER NOT NULL
  );
`);

module.exports = db;
