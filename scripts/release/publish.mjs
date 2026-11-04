#!/usr/bin/env node
// リリース成果物を集め、MineSentry のダウンロードページ（Core API）に登録する。
//
//   ./gradlew releaseInfo   … 各プロジェクトの build/release/ に jar と release-info.json を出力
//   node scripts/release/publish.mjs --tag 1.2.0 [--dry-run]
//
// 1. 全プロジェクトの release-info.json を集め、jar を build/release/ にまとめる
// 2. MC バージョン範囲を Mojang の version manifest で具体的なバージョン一覧に展開する
// 3. (対応 MC バージョン, Java バージョン) が同じ jar を 1 エントリにまとめる（Modrinth 方式）
// 4. build/release/manifest.json を書き出し、--dry-run でなければ Core API に upsert する
//
// 環境変数:
//   MINESENTRY_API_URL        例: https://api.minesentry.com
//   MINESENTRY_RELEASE_TOKEN  Core の RELEASE_PUBLISH_TOKEN
//   MINESENTRY_PROJECT_ID     ダウンロードプロジェクト ID（既定: minesentry）
//   GITHUB_REPOSITORY         ダウンロード URL の組み立てに使う（既定: MineSentry/MineSentry）
//   RELEASE_BODY_FILE         changelog にする Release 本文のファイル
//   RELEASE_PUBLISHED_AT      公開日時（ISO 8601）
//   RELEASE_PRERELEASE        "true" なら tag に指定が無くても BETA 扱い

import { copyFileSync, existsSync, mkdirSync, readdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { parseArgs } from "node:util";

const ROOT = resolve(import.meta.dirname, "../..");
const OUT_DIR = join(ROOT, "build", "release");
const MOJANG_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
const SKIP_DIRS = new Set([".deps", ".git", ".gradle", ".idea", ".kotlin", "node_modules", "src"]);
const PLATFORM_LABELS = { fabric: "Fabric", neoforge: "NeoForge", spigot: "Spigot", velocity: "Velocity" };
// 登録順。同じ公開日時のエントリは後に登録したものが上に表示される
const PLATFORM_ORDER = ["fabric", "neoforge", "spigot", "velocity"];

// ---- バージョン比較・範囲 ----

export function compareVersions(a, b) {
  const pa = a.split(/[.-]/).map(Number);
  const pb = b.split(/[.-]/).map(Number);
  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const d = (pa[i] ?? 0) - (pb[i] ?? 0);
    if (d !== 0) return d;
  }
  return 0;
}

/**
 * 範囲を述語の配列にする。対応形式:
 *   ""（非依存）/ "1.21" / "=1.21" / ">=1.21 <=1.21.10"（fabric）/ "[1.21,1.21.10]" "[1.20.6]" "[1.16,)"（maven）
 */
export function parseRange(range) {
  const r = range.trim();
  if (!r) return null;
  const maven = r.match(/^([[(])\s*([^,\])]*)\s*(?:,\s*([^\])]*))?\s*([\])])$/);
  if (maven) {
    const [, open, lo, hi, close] = maven;
    if (hi === undefined) return [(v) => compareVersions(v, lo) === 0];
    const preds = [];
    if (lo) preds.push(open === "[" ? (v) => compareVersions(v, lo) >= 0 : (v) => compareVersions(v, lo) > 0);
    if (hi) preds.push(close === "]" ? (v) => compareVersions(v, hi) <= 0 : (v) => compareVersions(v, hi) < 0);
    return preds;
  }
  return r.split(/\s+/).map((part) => {
    const m = part.match(/^(>=|<=|>|<|=)?(.+)$/);
    const [, op = "=", ver] = m;
    return {
      ">=": (v) => compareVersions(v, ver) >= 0,
      "<=": (v) => compareVersions(v, ver) <= 0,
      ">": (v) => compareVersions(v, ver) > 0,
      "<": (v) => compareVersions(v, ver) < 0,
      "=": (v) => compareVersions(v, ver) === 0,
    }[op];
  });
}

/** 範囲を、リリース済み Minecraft バージョンのうち範囲内のもの（昇順）に展開する。空範囲は []。 */
export function expandRange(range, releases) {
  const preds = parseRange(range);
  if (!preds) return [];
  const list = releases.filter((v) => preds.every((p) => p(v))).sort(compareVersions);
  if (list.length === 0) throw new Error(`MC version range "${range}" matched no released Minecraft version`);
  return list;
}

// ---- エントリの組み立て ----

function channelFor(tag, prerelease) {
  if (/-alpha/i.test(tag)) return "ALPHA";
  if (/-(beta|rc|pre)/i.test(tag) || prerelease) return "BETA";
  return "RELEASE";
}

function labelFor(version, platforms, gameVersions) {
  const names = platforms.map((p) => PLATFORM_LABELS[p] ?? p).join(" / ");
  if (gameVersions.length === 0) return `MineSentry ${version} (${names})`;
  const first = gameVersions[0];
  const last = gameVersions[gameVersions.length - 1];
  const mc = first === last ? first : `${first}–${last}`;
  return `MineSentry ${version} (${names} · MC ${mc})`;
}

/**
 * jar を (gameVersions, javaVersion) でまとめてバージョンエントリにする。
 * versionNumber は `<version>+<代表 MC バージョン>`（MC 非依存なら `+<platform>`）で、Core 側の upsert キーになる。
 */
export function buildVersions(infos, { version, tag, repository, changelog, publishedAt, prerelease, releases }) {
  const groups = new Map();
  for (const info of infos) {
    const gameVersions = expandRange(info.gameVersionRange, releases);
    const key = JSON.stringify([gameVersions, info.javaVersion]);
    if (!groups.has(key)) groups.set(key, { gameVersions, javaVersion: info.javaVersion, files: [] });
    const group = groups.get(key);
    if (group.files.some((f) => f.platform === info.platform)) {
      throw new Error(`Two ${info.platform} jars support the same MC versions: ${info.file}`);
    }
    group.files.push(info);
  }

  const channel = channelFor(tag, prerelease);
  const versions = [...groups.values()].map((g) => {
    g.files.sort((a, b) => PLATFORM_ORDER.indexOf(a.platform) - PLATFORM_ORDER.indexOf(b.platform));
    const platforms = g.files.map((f) => f.platform);
    // fabric-1.21 → 1.21。同じグループの jar は同じ Stonecutter ノード名を持つ
    const suffix = g.gameVersions.length === 0 ? platforms[0] : g.files[0].target.slice(g.files[0].platform.length + 1);
    return {
      label: labelFor(version, platforms, g.gameVersions),
      versionNumber: `${version}+${suffix || platforms[0]}`,
      channel,
      javaVersion: g.javaVersion,
      gameVersions: g.gameVersions,
      changelog,
      publishedAt,
      files: g.files.map((f) => ({
        platform: f.platform,
        downloadUrl: `https://github.com/${repository}/releases/download/${encodeURIComponent(tag)}/${encodeURIComponent(f.file)}`,
        fileName: f.file,
      })),
    };
  });

  const seen = new Set();
  for (const v of versions) {
    if (seen.has(v.versionNumber)) throw new Error(`Duplicate versionNumber ${v.versionNumber}`);
    seen.add(v.versionNumber);
  }

  // 古い MC → 新しい MC → プラグイン → プロキシ の順に登録（表示はその逆順）
  const rank = (v) => {
    if (v.gameVersions.length === 0) return [2, ""];
    return [v.files.every((f) => f.platform === "spigot") ? 1 : 0, v.gameVersions[v.gameVersions.length - 1]];
  };
  return versions.sort((a, b) => {
    const [ra, va] = rank(a);
    const [rb, vb] = rank(b);
    return ra !== rb ? ra - rb : compareVersions(va, vb);
  });
}

// ---- 入出力 ----

function findReleaseInfos(dir, found = []) {
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    if (!entry.isDirectory() || SKIP_DIRS.has(entry.name)) continue;
    const path = join(dir, entry.name);
    const info = join(path, "release", "release-info.json");
    if (entry.name === "build") {
      if (existsSync(info)) found.push(info);
      continue;
    }
    findReleaseInfos(path, found);
  }
  return found;
}

const FETCH_ATTEMPTS = 5;
const FETCH_TIMEOUT_MS = 30_000;

async function fetchReleases() {
  let lastError;
  for (let attempt = 1; attempt <= FETCH_ATTEMPTS; attempt++) {
    try {
      const res = await fetch(MOJANG_MANIFEST, { signal: AbortSignal.timeout(FETCH_TIMEOUT_MS) });
      if (!res.ok) throw new Error(`Failed to fetch Mojang version manifest: ${res.status}`);
      const manifest = await res.json();
      return manifest.versions.filter((v) => v.type === "release").map((v) => v.id);
    } catch (e) {
      lastError = e;
      console.error(`Mojang version manifest attempt ${attempt}/${FETCH_ATTEMPTS} failed: ${describeError(e)}`);
      if (attempt < FETCH_ATTEMPTS) await new Promise((r) => setTimeout(r, 2 ** attempt * 1000));
    }
  }
  throw lastError;
}

/** `fetch failed` のような message だけでは原因が分からないので、cause も辿って連結する。 */
function describeError(e) {
  const parts = [];
  for (let cur = e; cur; cur = cur.cause) parts.push(cur.code ? `${cur.message} (${cur.code})` : String(cur.message ?? cur));
  return parts.join(" <- ");
}

async function main() {
  const { values } = parseArgs({
    options: {
      tag: { type: "string" },
      "dry-run": { type: "boolean", default: false },
    },
  });
  const tag = values.tag ?? process.env.RELEASE_TAG;
  if (!tag) throw new Error("--tag is required");

  const infoFiles = findReleaseInfos(ROOT);
  if (infoFiles.length === 0) throw new Error("No release-info.json found. Run ./gradlew releaseInfo first.");
  const infos = infoFiles.map((path) => {
    const info = JSON.parse(readFileSync(path, "utf8"));
    return { ...info, jar: join(path, "..", info.file) };
  });

  const version = tag.replace(/^v/, "");
  const mismatched = infos.filter((i) => i.version !== version);
  if (mismatched.length > 0) {
    throw new Error(
      `Tag ${tag} does not match project version: ${[...new Set(mismatched.map((i) => i.version))].join(", ")}`,
    );
  }

  rmSync(OUT_DIR, { recursive: true, force: true });
  mkdirSync(OUT_DIR, { recursive: true });
  for (const info of infos) copyFileSync(info.jar, join(OUT_DIR, info.file));

  const bodyFile = process.env.RELEASE_BODY_FILE;
  const versions = buildVersions(infos, {
    version,
    tag,
    repository: process.env.GITHUB_REPOSITORY || "MineSentry/MineSentry",
    changelog: bodyFile && existsSync(bodyFile) ? readFileSync(bodyFile, "utf8").trim() : "",
    publishedAt: process.env.RELEASE_PUBLISHED_AT || new Date().toISOString(),
    prerelease: process.env.RELEASE_PRERELEASE === "true",
    releases: await fetchReleases(),
  });
  const payload = { versions };
  writeFileSync(join(OUT_DIR, "manifest.json"), JSON.stringify(payload, null, 2) + "\n");

  console.log(`Collected ${infos.length} jars into ${relative(ROOT, OUT_DIR)}:`);
  for (const v of versions) {
    console.log(`  ${v.versionNumber.padEnd(22)} ${v.files.map((f) => f.platform).join("+").padEnd(18)} ${v.label}`);
  }

  if (values["dry-run"]) {
    console.log("\n--dry-run: not sending. Payload written to build/release/manifest.json");
    return;
  }

  const apiUrl = process.env.MINESENTRY_API_URL?.replace(/\/+$/, "");
  const token = process.env.MINESENTRY_RELEASE_TOKEN;
  if (!apiUrl || !token) throw new Error("MINESENTRY_API_URL and MINESENTRY_RELEASE_TOKEN are required");
  const projectId = process.env.MINESENTRY_PROJECT_ID || "minesentry";
  const res = await fetch(`${apiUrl}/v1/release/download-projects/${encodeURIComponent(projectId)}/versions`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(`Core API responded ${res.status}: ${await res.text()}`);
  console.log(`\nRegistered ${versions.length} versions to download project "${projectId}".`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main().catch((e) => {
    console.error(describeError(e));
    process.exit(1);
  });
}
