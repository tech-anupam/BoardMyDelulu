import { load, type CheerioAPI } from "cheerio";
import type { Response } from "express";

const BASE_URL = "https://www.myinstants.com";
const USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

export interface Sound {
  id: string;
  title: string;
  url: string;
  mp3: string;
}

export interface Uploader {
  username: string;
  url: string;
}

export interface SoundDetail {
  id: string;
  url: string;
  title: string;
  mp3: string;
  description: string;
  tags: string[];
  favorites: string;
  views: string;
  uploader: Uploader;
}

const AUTHOR = "tech-anupam";

export async function fetchHtml(url: string): Promise<CheerioAPI> {
  const response = await fetch(url, {
    headers: { "User-Agent": USER_AGENT },
    redirect: "follow",
  });
  if (!response.ok) {
    throw new Error(`Fetch failed: HTTP ${response.status}`);
  }
  const html = await response.text();
  return load(html);
}

export function parseSounds($: CheerioAPI): Sound[] {
  const sounds: Sound[] = [];
  $("div.instant").each((_, el) => {
    const link = $(el).find("a.instant-link").first();
    if (!link.length) return;
    const title = link.text().trim();
    const href = link.attr("href") ?? "";
    const url = BASE_URL + href;
    const id = href.replace("/en/instant/", "").replace(/\/$/, "");
    const btn = $(el).find("button.small-button").first();
    const onclick = btn.attr("onclick") ?? "";
    const match = onclick.match(/play\('(.*?)'/);
    if (match) {
      sounds.push({ id, title, url, mp3: BASE_URL + match[1] });
    }
  });
  return sounds;
}

export function jsonOk<T>(res: Response, data: T): void {
  res.status(200).json({ status: 200, author: AUTHOR, data });
}

export function jsonError(res: Response, message: string, status = 404): void {
  res.status(status).json({ status, author: AUTHOR, message });
}
