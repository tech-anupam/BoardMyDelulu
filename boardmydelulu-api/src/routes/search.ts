import { Router } from "express";
import { fetchHtml, parseSounds, jsonOk, jsonError } from "../scraper.js";

const router = Router();

router.get("/", async (req, res) => {
  const q = req.query.q as string | undefined;
  const page = req.query.page as string | undefined;
  if (!q) return jsonError(res, "Query parameter 'q' is required, example: ?q=laugh", 400);
  try {
    const pageParam = page && Number(page) > 1 ? `&page=${encodeURIComponent(page)}` : "";
    const $ = await fetchHtml(`https://www.myinstants.com/en/search/?name=${encodeURIComponent(q)}${pageParam}`);
    jsonOk(res, parseSounds($));
  } catch {
    jsonError(res, "Failed to search sounds");
  }
});

export default router;
