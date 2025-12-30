const express = require("express");
const router = express.Router();
const db = require("../db");

// GET all hr
router.get("/", (req, res) => {
  const rows = db.prepare(
    "SELECT day, hr FROM hr ORDER BY day ASC"
  ).all();
  res.json(rows);
});

// BULK replace
router.post("/bulk", (req, res) => {
  const rows = req.body;
  if (!Array.isArray(rows)) return res.status(400).json({ error: "Expected array" });

  const insert = db.prepare("INSERT INTO hr (day, hr) VALUES (?, ?)");
  const clear = db.prepare("DELETE FROM hr");

  db.transaction(() => {
    clear.run();
    for (const r of rows) {
      if (!r.day || typeof r.hr !== "number") continue;
      insert.run(r.day, r.hr);
    }
  })();

  res.json({ ok: true });
});

router.post("/addbulk", (req, res) => {
  try {
    const items = req.body?.items;

    if (!Array.isArray(items)) {
      return res.status(400).json({ error: "Expected { items: [...] }" });
    }

    // Basic validation + normalization
    const cleaned = [];
    for (const it of items) {
      const day = typeof it.day === "string" ? it.day.trim() : "";
      const hr = Number.isFinite(it.hr) ? it.hr : parseInt(it.hr, 10);

      if (!/^\d{4}-\d{2}-\d{2}$/.test(day)) continue;
      if (!Number.isFinite(hr) || hr < 0) continue;

      cleaned.push({ day, hr: Math.floor(hr) });
    }

    if (cleaned.length === 0) {
      return res.status(400).json({ error: "No valid items to insert." });
    }

    const upsert = db.prepare(`
      INSERT INTO hr (day, hr)
      VALUES (?, ?)
      ON CONFLICT(day) DO UPDATE SET hr = excluded.hr
    `);

    const tx = db.transaction((rows) => {
      for (const r of rows) {
        upsert.run(r.day, r.hr);
      }
      return rows.length;
    });

    const upserted = tx(cleaned);

    return res.json({
      ok: true,
      received: items.length,
      valid: cleaned.length,
      upserted
    });
  } catch (err) {
    console.error("POST /api/heartrate/addbulk failed:", err);
    return res.status(500).json({ error: String(err?.message || err) });
  }
});


module.exports = router;
