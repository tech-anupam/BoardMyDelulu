import { Router } from "express";
import { fetchHtml, parseSounds, jsonOk, jsonError } from "../scraper.js";

const router = Router();

router.get("/", async (req, res) => {
  const username = req.query.username as string | undefined;
  if (!username) return jsonError(res, "Query parameter 'username' is required, example: ?username=hellmouz", 400);
  try {
    const $ = await fetchHtml(`https://www.myinstants.com/en/profile/${encodeURIComponent(username)}`);
    jsonOk(res, parseSounds($));
  } catch {
    jsonError(res, "Failed to fetch favorite sounds");
  }
});

export default router;
