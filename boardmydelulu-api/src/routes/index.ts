import { Router } from "express";
import trending from "./trending.js";
import search from "./search.js";
import detail from "./detail.js";
import recent from "./recent.js";
import best from "./best.js";
import uploaded from "./uploaded.js";
import favorites from "./favorites.js";

const router = Router();

router.get("/", (_req, res) => {
  res.json({
    status: 200,
    author: "tech-anupam",
    message: "BoardMyDelulu API — https://github.com/tech-anupam/boardmydelulu-api",
    endpoints: [
      "GET /trending?q=<region>",
      "GET /search?q=<query>&page=<n>",
      "GET /detail?id=<soundId>",
      "GET /recent",
      "GET /best?q=<region>",
      "GET /uploaded?username=<user>",
      "GET /favorites?username=<user>",
    ],
  });
});

router.use("/trending", trending);
router.use("/search", search);
router.use("/detail", detail);
router.use("/recent", recent);
router.use("/best", best);
router.use("/uploaded", uploaded);
router.use("/favorites", favorites);

export default router;
