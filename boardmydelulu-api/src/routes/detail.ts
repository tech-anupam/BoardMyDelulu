import { Router } from "express";
import { fetchHtml, jsonOk, jsonError, type SoundDetail } from "../scraper.js";

const router = Router();

router.get("/", async (req, res) => {
  const id = req.query.id as string | undefined;
  if (!id) return jsonError(res, "Query parameter 'id' is required, example: ?id=akh-26815", 400);
  try {
    const $ = await fetchHtml(`https://www.myinstants.com/en/instant/${encodeURIComponent(id)}`);
    const web = "https://www.myinstants.com";
    const title = $("h1#instant-page-title").text().trim();
    const soundUrl = $("button#instant-page-button-element").attr("data-url") ?? "";
    const description = $("div#instant-page-description p").first().text().trim();
    const tags: string[] = [];
    $("div#instant-page-tags a").each((_, el) => { tags.push($(el).text().trim()); });
    const favoritesText = $("div#instant-page-likes b").first().text().trim();
    const favorites = favoritesText.replace(" users", "");
    const authorDiv = $("div#instant-page-likes").nextAll("div").eq(1);
    const uploaderLink = authorDiv.find("a").first();
    const username = uploaderLink.text().trim();
    const uploaderUrl = web + (uploaderLink.attr("href") ?? "");
    const authorText = authorDiv.text().trim();
    const views = authorText.replace("views", "").replace(`Uploaded by ${username} - `, "").trim();
    const detail: SoundDetail = {
      id,
      url: `https://www.myinstants.com/en/instant/${id}`,
      title,
      mp3: web + soundUrl,
      description,
      tags,
      favorites,
      views,
      uploader: { username, url: uploaderUrl },
    };
    jsonOk(res, detail);
  } catch {
    jsonError(res, "Sound not found");
  }
});

export default router;
