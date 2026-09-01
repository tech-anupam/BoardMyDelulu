import { Router } from "express";
import { fetchHtml, parseSounds, jsonOk, jsonError } from "../scraper.js";

const router = Router();

router.get("/", async (req, res) => {
  const q = req.query.q as string | undefined;
  const page = req.query.page as string | undefined;
  const region = !q || q === "global" || q === "all" ? "us" : q;
  try {
    const pageParam = page && Number(page) > 1 ? `?page=${encodeURIComponent(page)}` : "";
    const url = `https://www.myinstants.com/en/best_of_all_time/${encodeURIComponent(region)}/${pageParam}`;
    const $ = await fetchHtml(url);
    jsonOk(res, parseSounds($));
  } catch {
    jsonError(res, "Failed to fetch best sounds");
  }
});

export default router;
