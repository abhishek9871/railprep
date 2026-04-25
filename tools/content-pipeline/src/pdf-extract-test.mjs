#!/usr/bin/env node
// One-shot: read a PDF, report first-N-chars, total length, extractability verdict.
// Usage: node src/pdf-extract-test.mjs <path.pdf>

import fs from "node:fs/promises";
import { PDFParse } from "pdf-parse";

const path = process.argv[2];
if (!path) { console.error("usage: pdf-extract-test.mjs <path>"); process.exit(2); }

const buf = await fs.readFile(path);
const parser = new PDFParse({ data: buf });
const r = await parser.getText();
const text = r.text ?? "";
const total = text.length;
const firstPageRough = text.slice(0, 4000);
const cleanRatio = (firstPageRough.match(/[A-Za-z0-9ऀ-ॿ]/g) || []).length / Math.max(firstPageRough.length, 1);
const verdict = total > 5000 && cleanRatio > 0.3 ? "text-extractable" : total > 500 ? "partial" : "scan-only";

console.log(`path=${path}`);
console.log(`pages=${r.numpages ?? r.pages?.length ?? "?"}`);
console.log(`chars=${total}`);
console.log(`clean-ratio(page1)=${cleanRatio.toFixed(2)}`);
console.log(`verdict=${verdict}`);
console.log(`--- first 1200 chars ---`);
console.log(firstPageRough.slice(0, 1200));
