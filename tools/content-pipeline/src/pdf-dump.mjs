#!/usr/bin/env node
// Dump arbitrary slices of extracted PDF text.
// Usage: node src/pdf-dump.mjs <path.pdf> <start> <end>

import fs from "node:fs/promises";
import { PDFParse } from "pdf-parse";

const [, , path, startStr, endStr] = process.argv;
const start = Number(startStr ?? 0);
const end = Number(endStr ?? 5000);

const buf = await fs.readFile(path);
const parser = new PDFParse({ data: buf });
const r = await parser.getText();
const t = r.text ?? "";
console.log(`TOTAL chars: ${t.length}, pages: ${r.numpages ?? "?"}`);
console.log(`--- chars ${start}..${end} ---`);
console.log(t.slice(start, end));
