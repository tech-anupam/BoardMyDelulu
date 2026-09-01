import express from "express";
import cors from "cors";
import routes from "./routes/index.js";

const app = express();
const PORT = parseInt(process.env.PORT || "3000");

app.use(cors());
app.use((_, res, next) => {
  res.setHeader("Cache-Control", "s-maxage=3600, stale-while-revalidate");
  next();
});

app.use("/", routes);

app.use((_req, res) => {
  res.status(404).json({ status: 404, author: "tech-anupam", message: "Endpoint not found" });
});

app.listen(PORT, () => {
  console.log(`BoardMyDelulu API running on http://localhost:${PORT}`);
});
