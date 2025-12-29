const express = require("express");
const cors = require("cors");

const stepsRoutes = require("./routes/steps");
const caloriesRoutes = require("./routes/calories");

const app = express();
app.use(cors());
app.use(express.json());

app.use("/api/steps", stepsRoutes);
app.use("/api/calories", caloriesRoutes);

app.use(express.json({ limit: "1mb" }));

app.listen(3001, () =>
  console.log("API running on http://localhost:3001")
);
