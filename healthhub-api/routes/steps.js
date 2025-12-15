const express = require("express");
const router = express.Router();
const db = require("../db");

// GET all steps
router.get("/", (req, res) => {
  const rows = db.prepare(
    "SELECT day, steps FROM steps ORDER BY day ASC"
  ).all();
  res.json(rows);
});

// BULK replace
router.post("/bulk", (req, res) => {
  const rows = req.body;
  if (!Array.isArray(rows)) return res.status(400).json({ error: "Expected array" });

  const insert = db.prepare("INSERT INTO steps (day, steps) VALUES (?, ?)");
  const clear = db.prepare("DELETE FROM steps");

  db.transaction(() => {
    clear.run();
    for (const r of rows) {
      if (!r.day || typeof r.steps !== "number") continue;
      insert.run(r.day, r.steps);
    }
  })();

  res.json({ ok: true });
});

module.exports = router;
