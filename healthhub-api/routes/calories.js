const express = require("express");
const router = express.Router();
const db = require("../db");

// GET all calories
router.get("/", (req, res) => {
  const rows = db.prepare(`
    SELECT day, calories, protein, fat, carbs
    FROM calories
    ORDER BY day ASC
  `).all();

  res.json(rows);
});

// BULK replace
router.post("/bulk", (req, res) => {
  const rows = req.body;
  if (!Array.isArray(rows)) return res.status(400).json({ error: "Expected array" });

  const insert = db.prepare(`
    INSERT INTO calories (day, calories, protein, fat, carbs)
    VALUES (?, ?, ?, ?, ?)
  `);

  const clear = db.prepare("DELETE FROM calories");

  db.transaction(() => {
    clear.run();
    for (const r of rows) {
      if (!r.day) continue;
      insert.run(
        r.day,
        r.calories ?? 0,
        r.protein ?? 0,
        r.fat ?? 0,
        r.carbs ?? 0
      );
    }
  })();

  res.json({ ok: true });
});

module.exports = router;
