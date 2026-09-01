import { Router } from "express";
import { fetchHtml, parseSounds, jsonOk, jsonError } from "../scraper.js";

const router = Router();

router.get("/", async (req, res) => {
  const page = req.query.page as string | undefined;
  try {
    const pageParam = page && Number(page) > 1 ? `?page=${encodeURIComponent(page)}` : "";
    const $ = await fetchHtml(`https://www.myinstants.com/en/recent/${pageParam}`);
    jsonOk(res, parseSounds($));
  } catch {
    jsonError(res, "Failed to fetch recent sounds");
  }
});

export default router;
