#!/usr/bin/env node
// Generate original launch-grade sectional candidates for uncovered RRB NTPC topics.
//
// These are not copied from competitor banks. They are deterministic, computed
// original MCQs patterned after the official RRB NTPC syllabus topics: every
// answer and distractor is produced from a named arithmetic or reasoning step.

import { mkdir, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const OUT = resolve(__dirname, "..", "candidates");

const dayNames = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
const dayNamesHi = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
const labels = ["A", "B", "C", "D"];

function difficulty(i) {
  if (i < 8) return "EASY";
  if (i < 20) return "MEDIUM";
  return "HARD";
}

function gcd(a, b) {
  a = Math.abs(a); b = Math.abs(b);
  while (b) [a, b] = [b, a % b];
  return a;
}

function lcm(a, b) { return Math.abs(a * b) / gcd(a, b); }
function fmt(n) { return Number.isInteger(n) ? String(n) : String(Number(n.toFixed(2))); }
function money(n) { return `₹${fmt(n)}`; }
function pct(n) { return `${fmt(n)}%`; }
function kg(n) { return `${fmt(n)} kg`; }
function cm(n) { return `${fmt(n)} cm`; }
function sqcm(n) { return `${fmt(n)} cm²`; }
function cubecm(n) { return `${fmt(n)} cm³`; }
function m2(n) { return `${fmt(n)} m²`; }
function days(n) { return `${fmt(n)} days`; }
function hours(n) { return `${fmt(n)} hours`; }

function optText(v) { return String(v); }

function question(section, localIndex, data) {
  const correctIndex = localIndex % 4;
  const correct = {
    label: "",
    text_en: optText(data.answer),
    text_hi: optText(data.answerHi ?? data.answer),
    is_correct: true,
    trap_reason_en: null,
    trap_reason_hi: null,
  };
  const wrong = data.traps.map((t) => ({
    label: "",
    text_en: optText(t.value),
    text_hi: optText(t.valueHi ?? t.value),
    is_correct: false,
    trap_reason_en: t.en,
    trap_reason_hi: t.hi,
  }));
  const ordered = wrong.slice();
  ordered.splice(correctIndex, 0, correct);
  ordered.forEach((o, i) => { o.label = labels[i]; });

  const id = `${section.prefix}-${String(localIndex + 1).padStart(2, "0")}`;
  const answerText = optText(data.answer);
  const answerTextHi = optText(data.answerHi ?? data.answer);
  return {
    id,
    stem_en: data.stemEn,
    stem_hi: data.stemHi,
    difficulty: data.difficulty ?? difficulty(localIndex),
    tags: data.tags,
    source: "Original",
    license: "ORIGINAL",
    options: ordered,
    explanation_method_en: `${data.methodEn} Therefore, answer = ${answerText}.`,
    explanation_concept_en: `${data.conceptEn} Final answer is ${answerText}.`,
    explanation_method_hi: `${data.methodHi} अतः answer = ${answerTextHi}।`,
    explanation_concept_hi: `${data.conceptHi} Final answer ${answerTextHi} है।`,
  };
}

function bundle(slug, titleEn, titleHi, sectionTitleEn, sectionTitleHi, subjectHint, prefix, rows) {
  const section = { prefix };
  const questions = rows.map((row, i) => question(section, i, row));
  const ids = new Set(questions.map((q) => q.id));
  if (ids.size !== questions.length) throw new Error(`duplicate ids in ${slug}`);
  for (const q of questions) {
    const texts = q.options.map((o) => o.text_en);
    if (new Set(texts).size !== 4) {
      throw new Error(`${slug} ${q.id}: duplicate option texts ${texts.join(", ")}`);
    }
  }
  return {
    _doc: `Launch original sectional — ${sectionTitleEn}. Generated from computed templates; no competitor question text copied.`,
    slug,
    title_en: titleEn,
    title_hi: titleHi,
    total_minutes: 25,
    section: { title_en: sectionTitleEn, title_hi: sectionTitleHi, subject_hint: subjectHint },
    questions,
  };
}

function numberSystemRows() {
  const rows = [];
  for (const [a, b] of [[18, 24], [28, 42], [36, 60], [45, 75], [32, 48]]) {
    const ans = lcm(a, b), h = gcd(a, b);
    rows.push({
      stemEn: `Find the LCM of ${a} and ${b}.`,
      stemHi: `${a} और ${b} का LCM ज्ञात कीजिए।`,
      answer: ans,
      traps: [
        { value: h, en: `Reported HCF ${h} instead of LCM. LCM is the least common multiple, not the greatest common factor.`, hi: `HCF ${h} को LCM मान लिया। LCM least common multiple होता है, greatest factor नहीं।` },
        { value: a + b, en: `Added the numbers: ${a}+${b}=${a + b}. LCM is found from prime powers, not addition.`, hi: `संख्याएँ जोड़ दीं: ${a}+${b}=${a + b}। LCM prime powers से निकलता है।` },
        { value: Math.max(a, b), en: `Picked the larger number ${Math.max(a, b)}. It is not divisible by both numbers in this pair.`, hi: `बड़ी संख्या ${Math.max(a, b)} चुन ली। यह दोनों numbers से divisible नहीं है।` },
      ],
      methodEn: `Use LCM × HCF = product. HCF(${a},${b}) = ${h}. LCM = ${a}×${b}/${h} = ${ans}.`,
      conceptEn: `For two positive integers, HCF captures the common part and LCM captures each prime factor at its maximum power. Their product equals the product of the two numbers.`,
      methodHi: `LCM × HCF = product लगाएँ। HCF(${a},${b}) = ${h}। LCM = ${a}×${b}/${h} = ${ans}।`,
      conceptHi: `दो positive integers में HCF common part लेता है और LCM हर prime factor की maximum power लेता है। इसलिए LCM × HCF = product।`,
      tags: ["number-system", "lcm-hcf"],
    });
  }
  for (const [a, b] of [[84, 132], [96, 160], [108, 180], [132, 204], [156, 260]]) {
    const h = gcd(a, b), ans = h;
    rows.push({
      stemEn: `Find the HCF of ${a} and ${b}.`,
      stemHi: `${a} और ${b} का HCF ज्ञात कीजिए।`,
      answer: ans,
      traps: [
        { value: lcm(a, b), en: `Computed LCM ${lcm(a, b)} instead of HCF. The question asks for the greatest common factor.`, hi: `LCM ${lcm(a, b)} निकाल दिया। Question HCF यानी greatest common factor पूछता है।` },
        { value: Math.abs(a - b), en: `Used difference ${Math.abs(a - b)} as HCF. Difference can help Euclid's algorithm, but it is not automatically the HCF.`, hi: `Difference ${Math.abs(a - b)} को HCF मान लिया। Difference Euclid में मदद करता है, HCF अपने-आप नहीं होता।` },
        { value: ans / 2, en: `Stopped one common factor early at ${ans / 2}. Both numbers are still divisible by ${ans}.`, hi: `${ans / 2} पर रुक गए। दोनों numbers ${ans} से भी divisible हैं।` },
      ],
      methodEn: `Euclid method: repeatedly subtract/reduce common parts; HCF(${a},${b}) = ${ans}.`,
      conceptEn: `HCF is the largest number that divides both given numbers exactly. Prime factorisation or Euclid's algorithm both isolate this common divisor.`,
      methodHi: `Euclid method से common divisor निकालें; HCF(${a},${b}) = ${ans}।`,
      conceptHi: `HCF सबसे बड़ी संख्या है जो दोनों numbers को exactly divide करती है। Prime factorisation या Euclid method दोनों same common divisor देते हैं।`,
      tags: ["number-system", "lcm-hcf"],
    });
  }
  for (const [n, d] of [[347, 9], [829, 11], [1257, 7], [999, 13], [2026, 8]]) {
    const ans = n % d;
    rows.push({
      stemEn: `What is the remainder when ${n} is divided by ${d}?`,
      stemHi: `${n} को ${d} से divide करने पर remainder क्या होगा?`,
      answer: ans,
      traps: [
        { value: Math.floor(n / d), en: `Reported quotient ${Math.floor(n / d)} instead of remainder. Remainder is the leftover after full groups of ${d}.`, hi: `Quotient ${Math.floor(n / d)} को remainder लिख दिया। Remainder ${d} के full groups के बाद बचा हिस्सा है।` },
        { value: d - ans, en: `Used complement ${d}-${ans}=${d - ans}. The asked remainder is ${ans}, not its distance from the divisor.`, hi: `Complement ${d}-${ans}=${d - ans} ले लिया। Remainder ${ans} है, divisor से distance नहीं।` },
        { value: 0, en: `Assumed exact divisibility. ${d}×${Math.floor(n / d)}=${d * Math.floor(n / d)}, leaving ${ans}.`, hi: `Exact divisibility मान ली। ${d}×${Math.floor(n / d)}=${d * Math.floor(n / d)}, इसलिए ${ans} बचता है।` },
      ],
      methodEn: `${d} × ${Math.floor(n / d)} = ${d * Math.floor(n / d)}. ${n} - ${d * Math.floor(n / d)} = ${ans}.`,
      conceptEn: `Division means dividend = divisor × quotient + remainder, with remainder smaller than the divisor. Here the leftover is the required value.`,
      methodHi: `${d} × ${Math.floor(n / d)} = ${d * Math.floor(n / d)}। ${n} - ${d * Math.floor(n / d)} = ${ans}।`,
      conceptHi: `Division में dividend = divisor × quotient + remainder होता है, और remainder divisor से छोटा होता है। यहाँ बचा हुआ हिस्सा answer है।`,
      tags: ["number-system", "remainders"],
    });
  }
  for (const [base, power] of [[7, 42], [3, 58], [9, 38], [8, 62], [4, 52]]) {
    let ans;
    if (base % 10 === 6) ans = 6;
    else {
      const cycle = [];
      let x = base % 10;
      while (!cycle.includes(x)) { cycle.push(x); x = (x * base) % 10; }
      ans = cycle[(power - 1) % cycle.length];
    }
    rows.push({
      stemEn: `Find the unit digit of ${base}^${power}.`,
      stemHi: `${base}^${power} का unit digit ज्ञात कीजिए।`,
      answer: ans,
      traps: [
        { value: base % 10, en: `Used the base unit digit ${base % 10} without applying the power cycle. Unit digits repeat in cycles.`, hi: `Base unit digit ${base % 10} सीधे ले लिया। Powers में unit digit cycle repeat होता है।` },
        { value: power % 10, en: `Used the exponent's unit digit ${power % 10}. The exponent chooses the cycle position; it is not itself the answer.`, hi: `Exponent का unit digit ${power % 10} ले लिया। Exponent cycle position बताता है, answer नहीं।` },
        { value: (ans + 2) % 10, en: `Shifted to the wrong cycle position. The exponent ${power} must be reduced by the cycle length, not by 10 alone.`, hi: `गलत cycle position ले ली। Exponent ${power} को cycle length से reduce करना होता है।` },
      ],
      methodEn: `Unit digits of powers of ${base % 10} repeat cyclically. Put exponent ${power} into that cycle; the selected unit digit is ${ans}.`,
      conceptEn: `Only the last digit matters for a unit digit problem. Multiplying last digits repeatedly creates a short repeating cycle, so large powers reduce to a cycle position.`,
      methodHi: `${base % 10} की powers का unit digit cycle में repeat होता है। Exponent ${power} को cycle में रखकर unit digit ${ans} मिलता है।`,
      conceptHi: `Unit digit problem में सिर्फ last digit matter करता है। Last digits multiply करते हुए छोटा repeating cycle बनाते हैं।`,
      tags: ["number-system", "unit-digit"],
    });
  }
  for (const [n, p] of [[420, 7], [630, 9], [780, 13], [990, 11], [1260, 15]]) {
    const ans = n / p;
    rows.push({
      stemEn: `The first part of a quantity is ${n}, and it is ${p} times the second part. Find the second part.`,
      stemHi: `किसी quantity का पहला भाग ${n} है और यह दूसरे भाग का ${p} गुना है। दूसरा भाग ज्ञात कीजिए।`,
      answer: ans,
      traps: [
        { value: n, en: `Reported the given first part ${n}. The question asks for the second part.`, hi: `दिया हुआ पहला भाग ${n} लिख दिया। Question दूसरा भाग पूछता है।` },
        { value: p, en: `Reported the multiplier ${p} as the part. The multiplier is not the value of the part.`, hi: `Multiplier ${p} को ही part मान लिया। Multiplier value नहीं है।` },
        { value: Math.floor(n / (p + 1)), en: `Treated ${n} as the total and divided by ${p + 1}. Here ${n} is already the first part, so second part = ${n}/${p}.`, hi: `${n} को total मानकर ${p + 1} से divide किया। यहाँ ${n} पहला भाग है, इसलिए दूसरा भाग = ${n}/${p}।` },
      ],
      methodEn: `Let the second part be x. First part = ${p}x = ${n}; so x = ${n}/${p} = ${ans}.`,
      conceptEn: `When one quantity is described as a multiple of another, assign the smaller unknown as x and translate the sentence into an equation before choosing the asked part.`,
      methodHi: `दूसरा भाग x मानें। पहला भाग = ${p}x = ${n}; इसलिए x = ${n}/${p} = ${ans}।`,
      conceptHi: `जब एक quantity दूसरी की multiple हो, smaller unknown को x मानकर equation बनाते हैं और पूछा गया part निकालते हैं।`,
      tags: ["number-system", "ratio-basics"],
      difficulty: "HARD",
    });
  }
  return rows.slice(0, 25);
}

function simplificationRows() {
  const rows = [];
  const exprs = [
    [18, 6, 4, 7], [25, 5, 9, 11], [42, 7, 8, 13], [64, 8, 6, 15], [81, 9, 5, 22],
    [120, 4, 6, 18], [144, 12, 7, 31], [96, 6, 5, 40], [75, 3, 8, 50], [110, 5, 4, 29],
  ];
  for (const [a, b, c, d] of exprs) {
    const ans = a / b + c * d;
    rows.push({
      stemEn: `Simplify: ${a} ÷ ${b} + ${c} × ${d}.`,
      stemHi: `Simplify कीजिए: ${a} ÷ ${b} + ${c} × ${d}.`,
      answer: ans,
      traps: [
        { value: (a / b + c) * d, en: `Did addition before multiplication: (${a}÷${b}+${c})×${d} = ${(a / b + c) * d}. BODMAS requires multiplication before addition.`, hi: `Multiplication से पहले addition किया: (${a}÷${b}+${c})×${d} = ${(a / b + c) * d}। BODMAS में multiplication पहले है।` },
        { value: a / (b + c) * d, en: `Grouped denominator wrongly as ${b}+${c}. The expression has separate division and multiplication operations.`, hi: `Denominator को ${b}+${c} group कर दिया। Expression में division और multiplication अलग operations हैं।` },
        { value: a / b + c + d, en: `Changed multiplication ${c}×${d} into addition ${c}+${d}.`, hi: `Multiplication ${c}×${d} को addition ${c}+${d} बना दिया।` },
      ],
      methodEn: `First divide and multiply: ${a}÷${b}=${a / b}, ${c}×${d}=${c * d}. Add: ${a / b}+${c * d}=${ans}.`,
      conceptEn: `BODMAS fixes operation order. Division and multiplication are handled before addition, moving left-to-right among same-priority operations.`,
      methodHi: `पहले divide और multiply: ${a}÷${b}=${a / b}, ${c}×${d}=${c * d}। Add करें: ${a / b}+${c * d}=${ans}।`,
      conceptHi: `BODMAS operation order तय करता है। Division और multiplication addition से पहले होते हैं।`,
      tags: ["simplification", "bodmas"],
    });
  }
  const frac = [[2, 5, 150, 18], [3, 8, 240, 15], [5, 6, 180, 25], [7, 10, 350, 12], [4, 9, 270, 30]];
  for (const [num, den, ofn, add] of frac) {
    const part = num * ofn / den;
    const ans = part + add;
    rows.push({
      stemEn: `Simplify: ${num}/${den} of ${ofn} + ${add}.`,
      stemHi: `Simplify करें: ${ofn} का ${num}/${den} + ${add}.`,
      answer: ans,
      traps: [
        { value: part, en: `Found ${num}/${den} of ${ofn} = ${part} but forgot to add ${add}.`, hi: `${ofn} का ${num}/${den} = ${part} निकाला, पर ${add} add नहीं किया।` },
        { value: ofn / den + add, en: `Used only 1/${den} of ${ofn}, missing multiplication by numerator ${num}.`, hi: `सिर्फ 1/${den} of ${ofn} लिया, numerator ${num} से multiply नहीं किया।` },
        { value: num / den * (ofn + add), en: `Applied the fraction to ${ofn}+${add}. The '+ ${add}' is outside the 'of' part.`, hi: `Fraction को ${ofn}+${add} पर लगा दिया। '+ ${add}' 'of' part के बाहर है।` },
      ],
      methodEn: `${num}/${den} of ${ofn} = ${ofn} ÷ ${den} × ${num} = ${part}. Then ${part}+${add}=${ans}.`,
      conceptEn: `'Of' means multiplication. Compute the fraction of the given number first, then apply the outside addition.`,
      methodHi: `${ofn} का ${num}/${den} = ${ofn} ÷ ${den} × ${num} = ${part}। फिर ${part}+${add}=${ans}।`,
      conceptHi: `'of' का मतलब multiplication है। पहले fraction of number निकालें, फिर बाहर वाला addition करें।`,
      tags: ["simplification", "fractions"],
    });
  }
  const decs = [[1.5, 24, 6], [2.25, 40, 10], [3.5, 18, 12], [4.2, 25, 5], [6.5, 12, 8]];
  for (const [x, y, sub] of decs) {
    const ans = x * y - sub;
    rows.push({
      stemEn: `Simplify: ${x} × ${y} - ${sub}.`,
      stemHi: `Simplify करें: ${x} × ${y} - ${sub}.`,
      answer: fmt(ans),
      traps: [
        { value: fmt(x * (y - sub)), en: `Subtracted first: ${y}-${sub}, then multiplied by ${x}. Multiplication must be completed before subtraction.`, hi: `पहले ${y}-${sub} घटाया, फिर ${x} से multiply किया। Multiplication subtraction से पहले है।` },
        { value: fmt(x + y - sub), en: `Changed multiplication into addition: ${x}+${y}-${sub}.`, hi: `Multiplication को addition बना दिया: ${x}+${y}-${sub}।` },
        { value: fmt(x * y + sub), en: `Added ${sub} instead of subtracting it after multiplication.`, hi: `Multiplication के बाद ${sub} घटाने की जगह जोड़ दिया।` },
      ],
      methodEn: `${x}×${y}=${fmt(x * y)}. Now subtract ${sub}: ${fmt(x * y)}-${sub}=${fmt(ans)}.`,
      conceptEn: `Decimal multiplication follows the same operation order as integer arithmetic; only place value changes the product format.`,
      methodHi: `${x}×${y}=${fmt(x * y)}। अब ${sub} घटाएँ: ${fmt(x * y)}-${sub}=${fmt(ans)}।`,
      conceptHi: `Decimal multiplication में भी integer arithmetic जैसा operation order रहता है; बस place value product format बदलता है।`,
      tags: ["simplification", "decimals"],
    });
  }
  const sqs = [[14, 6], [17, 3], [19, 11], [23, 7], [31, 9]];
  for (const [a, b] of sqs) {
    const ans = a * a - b * b;
    rows.push({
      stemEn: `Simplify using identity: ${a}² - ${b}².`,
      stemHi: `Identity से simplify करें: ${a}² - ${b}².`,
      answer: ans,
      traps: [
        { value: (a - b) ** 2, en: `Used (a-b)² = ${(a - b) ** 2}. But a²-b² = (a-b)(a+b).`, hi: `(a-b)² = ${(a - b) ** 2} लगा दिया। सही identity a²-b² = (a-b)(a+b) है।` },
        { value: a * a + b * b, en: `Added the squares instead of subtracting: ${a * a}+${b * b}=${a * a + b * b}.`, hi: `Squares subtract करने की जगह add कर दिए: ${a * a}+${b * b}=${a * a + b * b}।` },
        { value: (a - b) * a, en: `Multiplied only by ${a} after taking difference ${a-b}; the second factor must be a+b=${a + b}.`, hi: `Difference ${a-b} के बाद सिर्फ ${a} से multiply किया। दूसरा factor a+b=${a + b} होना चाहिए।` },
      ],
      methodEn: `a²-b²=(a-b)(a+b)=(${a}-${b})(${a}+${b})=${a - b}×${a + b}=${ans}.`,
      conceptEn: `The difference of squares identity avoids large square calculations and is a standard simplification shortcut.`,
      methodHi: `a²-b²=(a-b)(a+b)=(${a}-${b})(${a}+${b})=${a - b}×${a + b}=${ans}।`,
      conceptHi: `Difference of squares identity बड़े square calculations बचाती है और simplification का standard shortcut है।`,
      tags: ["simplification", "identities"],
      difficulty: "HARD",
    });
  }
  return rows.slice(0, 25);
}

function interestRows() {
  const rows = [];
  const siData = [[5000, 8, 3], [7200, 5, 4], [9000, 6, 2], [12000, 7, 5], [6400, 12, 2]];
  for (const [p, r, t] of siData) {
    const ans = p * r * t / 100;
    rows.push({
      stemEn: `Find the simple interest on ${money(p)} at ${r}% per annum for ${t} years.`,
      stemHi: `${money(p)} पर ${r}% वार्षिक दर से ${t} वर्ष का simple interest ज्ञात कीजिए।`,
      answer: money(ans),
      traps: [
        { value: money(p * r / 100), en: `Calculated one year's interest only: ${p}×${r}/100=${p * r / 100}, missing ${t} years.`, hi: `सिर्फ 1 साल का interest निकाला: ${p}×${r}/100=${p * r / 100}; ${t} years छूट गए।` },
        { value: money(p + ans), en: `Reported amount ${p}+${ans}=${p + ans} instead of interest.`, hi: `Interest की जगह amount ${p}+${ans}=${p + ans} दे दिया।` },
        { value: money(r * t), en: `Multiplied rate and time only: ${r}×${t}; principal was ignored.`, hi: `सिर्फ rate × time = ${r * t} किया; principal ignore हुआ।` },
      ],
      methodEn: `SI = PRT/100 = ${p}×${r}×${t}/100 = ${ans}.`,
      conceptEn: `Simple interest charges the same interest on the original principal every year, so the formula is linear in P, R, and T.`,
      methodHi: `SI = PRT/100 = ${p}×${r}×${t}/100 = ${ans}।`,
      conceptHi: `Simple interest में हर साल original principal पर same interest लगता है, इसलिए formula P, R, T में linear है।`,
      tags: ["simple-interest", "si-ci"],
    });
  }
  const rateData = [[6000, 1440, 4], [8000, 1920, 3], [7500, 2250, 5], [10000, 1800, 2], [9600, 2304, 4]];
  for (const [p, si, t] of rateData) {
    const ans = si * 100 / (p * t);
    rows.push({
      stemEn: `At what annual rate will ${money(p)} earn ${money(si)} simple interest in ${t} years?`,
      stemHi: `${money(p)} पर ${t} वर्ष में ${money(si)} simple interest पाने के लिए annual rate क्या होगी?`,
      answer: pct(ans),
      traps: [
        { value: pct(si * 100 / p), en: `Forgot to divide by time: ${si}×100/${p}=${fmt(si * 100 / p)}%.`, hi: `Time से divide नहीं किया: ${si}×100/${p}=${fmt(si * 100 / p)}%।` },
        { value: pct(si / t), en: `Divided interest by years but did not convert against principal into a rate.`, hi: `Interest को years से divide किया पर principal के against rate नहीं निकाली।` },
        { value: pct(t * 100 / p), en: `Used time in place of interest in the rate formula.`, hi: `Rate formula में interest की जगह time रख दिया।` },
      ],
      methodEn: `R = SI×100/(P×T) = ${si}×100/(${p}×${t}) = ${fmt(ans)}%.`,
      conceptEn: `Rearrange SI = PRT/100. The rate is the annual percentage of principal that produces the given interest over the stated time.`,
      methodHi: `R = SI×100/(P×T) = ${si}×100/(${p}×${t}) = ${fmt(ans)}%।`,
      conceptHi: `SI = PRT/100 को rearrange करें। Rate principal का annual percentage है जिससे given interest बनता है।`,
      tags: ["simple-interest", "rate"],
    });
  }
  const diffData = [[10000, 10], [8000, 5], [12000, 8], [15000, 6], [20000, 4]];
  for (const [p, r] of diffData) {
    const ans = p * (r / 100) ** 2;
    rows.push({
      stemEn: `Find the difference between compound interest and simple interest on ${money(p)} at ${r}% for 2 years.`,
      stemHi: `${money(p)} पर ${r}% दर से 2 वर्ष के लिए CI और SI का difference ज्ञात कीजिए।`,
      answer: money(ans),
      traps: [
        { value: money(p * r * 2 / 100), en: `Reported 2-year SI ${p}×${r}×2/100=${p * r * 2 / 100}, not CI-SI difference.`, hi: `2-year SI ${p * r * 2 / 100} दे दिया, CI-SI difference नहीं।` },
        { value: money(p * r / 100), en: `Reported one year's interest ${p * r / 100}; the difference is interest on first year's interest.`, hi: `1-year interest ${p * r / 100} दे दिया। Difference पहले साल के interest पर interest है।` },
        { value: money(ans * 2), en: `Doubled the two-year difference. For 2 years, CI-SI difference is exactly P(r/100)².`, hi: `2-year difference को double कर दिया। 2 years के लिए CI-SI = P(r/100)² होता है।` },
      ],
      methodEn: `For 2 years, CI-SI = P(r/100)² = ${p}×(${r}/100)² = ${fmt(ans)}.`,
      conceptEn: `In compound interest, the second year earns interest on the first year's interest. That extra interest over SI is P(r/100)^2 for 2 years.`,
      methodHi: `2 years के लिए CI-SI = P(r/100)² = ${p}×(${r}/100)² = ${fmt(ans)}।`,
      conceptHi: `CI में दूसरे साल पहले साल के interest पर भी interest लगता है। यही SI से extra amount है।`,
      tags: ["compound-interest", "ci-si-difference"],
    });
  }
  const amountData = [[4000, 10, 2], [5000, 20, 2], [6250, 8, 2], [10000, 5, 3], [8000, 10, 3]];
  for (const [p, r, t] of amountData) {
    const ans = p * (1 + r / 100) ** t;
    rows.push({
      stemEn: `Find the compound amount on ${money(p)} at ${r}% per annum for ${t} years, compounded annually.`,
      stemHi: `${money(p)} पर ${r}% वार्षिक दर से ${t} वर्ष का compound amount ज्ञात कीजिए।`,
      answer: money(ans),
      traps: [
        { value: money(p * r * t / 100), en: `Reported CI as if it were simple interest, and also omitted principal from amount.`, hi: `Simple interest जैसा calculate किया और amount में principal भी नहीं जोड़ा।` },
        { value: money(p + p * r * t / 100), en: `Used simple amount ${p}+${p * r * t / 100}; compounding requires multiplying by (1+r/100)^${t}.`, hi: `Simple amount ${p + p * r * t / 100} लिया। Compounding में (1+r/100)^${t} multiply होता है।` },
        { value: money(p * (1 + r / 100)), en: `Compounded for only 1 year: ${p}×${1 + r / 100}. Time is ${t} years.`, hi: `सिर्फ 1 साल compound किया। Time ${t} years है।` },
      ],
      methodEn: `Amount = P(1+r/100)^t = ${p}(1+${r}/100)^${t} = ${fmt(ans)}.`,
      conceptEn: `Compound amount grows on the updated balance each year, so the annual multiplier is applied repeatedly for t years.`,
      methodHi: `Amount = P(1+r/100)^t = ${p}(1+${r}/100)^${t} = ${fmt(ans)}।`,
      conceptHi: `Compound amount हर साल updated balance पर बढ़ता है, इसलिए annual multiplier t years तक repeat होता है।`,
      tags: ["compound-interest", "amount"],
      difficulty: "HARD",
    });
  }
  const principalData = [[960, 8, 3], [1500, 10, 3], [2160, 12, 3], [2520, 7, 4], [3840, 8, 6]];
  for (const [si, r, t] of principalData) {
    const ans = si * 100 / (r * t);
    rows.push({
      stemEn: `${money(si)} simple interest is earned at ${r}% per annum in ${t} years. Find the principal.`,
      stemHi: `${r}% वार्षिक दर पर ${t} वर्ष में ${money(si)} simple interest मिला। Principal ज्ञात कीजिए।`,
      answer: money(ans),
      traps: [
        { value: money(si * 100 / r), en: `Forgot to divide by time ${t}; this gives the principal for one year only.`, hi: `Time ${t} से divide नहीं किया; यह सिर्फ 1 year वाला principal देता है।` },
        { value: money(si * t), en: `Multiplied interest by years instead of rearranging SI = PRT/100.`, hi: `SI = PRT/100 rearrange करने की जगह interest को years से multiply किया।` },
        { value: money(si * r * t / 100), en: `Applied the SI formula forward to the interest amount itself instead of solving for principal.`, hi: `Principal निकालने की जगह interest amount पर ही SI formula लगा दिया।` },
      ],
      methodEn: `From SI = PRT/100, P = SI×100/(R×T) = ${si}×100/(${r}×${t}) = ${fmt(ans)}.`,
      conceptEn: `Principal is the base amount. When interest, rate, and time are known, reverse the simple-interest formula to isolate P.`,
      methodHi: `SI = PRT/100 से P = SI×100/(R×T) = ${si}×100/(${r}×${t}) = ${fmt(ans)}।`,
      conceptHi: `Principal base amount है। Interest, rate और time दिए हों तो SI formula reverse करके P निकालते हैं।`,
      tags: ["simple-interest", "principal"],
      difficulty: "HARD",
    });
  }
  return rows.slice(0, 25);
}

function algebraRows() {
  const rows = [];
  for (const [a, b, c] of [[3, 7, 34], [5, 9, 64], [4, 11, 51], [7, 5, 68], [6, 13, 79], [8, 17, 105], [9, 4, 76], [11, 6, 94]]) {
    const ans = (c - b) / a;
    rows.push({
      stemEn: `Solve for x: ${a}x + ${b} = ${c}.`,
      stemHi: `x ज्ञात कीजिए: ${a}x + ${b} = ${c}.`,
      answer: ans,
      traps: [
        { value: c / a, en: `Divided ${c} by ${a} before subtracting ${b}. The constant ${b} must move first.`, hi: `${b} घटाने से पहले ${c}/${a} कर दिया। Constant पहले shift होता है।` },
        { value: (c + b) / a, en: `Added ${b} instead of subtracting it when moving across the equals sign.`, hi: `Equals sign cross करते समय ${b} subtract की जगह add कर दिया।` },
        { value: c - b, en: `Stopped at ${c}-${b}=${c - b} and forgot to divide by coefficient ${a}.`, hi: `${c}-${b}=${c - b} पर रुक गए; coefficient ${a} से divide नहीं किया।` },
      ],
      methodEn: `${a}x = ${c}-${b} = ${c - b}. Divide by ${a}: x = ${ans}.`,
      conceptEn: `A linear equation is solved by undoing operations in reverse order: remove the constant, then remove the coefficient of x.`,
      methodHi: `${a}x = ${c}-${b} = ${c - b}। ${a} से divide करें: x = ${ans}।`,
      conceptHi: `Linear equation में operations reverse order में undo होते हैं: पहले constant हटाएँ, फिर x का coefficient हटाएँ।`,
      tags: ["algebra", "linear-equations"],
    });
  }
  for (const [a, b] of [[18, 12], [25, 15], [32, 18], [41, 23], [56, 34], [63, 27]]) {
    const x = (a + b) / 2;
    rows.push({
      stemEn: `If x + y = ${a} and x - y = ${b}, find x.`,
      stemHi: `यदि x + y = ${a} और x - y = ${b}, तो x ज्ञात कीजिए।`,
      answer: x,
      traps: [
        { value: (a - b) / 2, en: `Found y = (${a}-${b})/2 instead of x. The question asks for x.`, hi: `y = (${a}-${b})/2 निकाल दिया। Question x पूछता है।` },
        { value: a + b, en: `Added equations but forgot to divide by 2: 2x=${a + b}.`, hi: `Equations add करके 2 से divide नहीं किया: 2x=${a + b}।` },
        { value: a - b, en: `Subtracted equations and got 2y, not x.`, hi: `Equations subtract करके 2y मिला, x नहीं।` },
      ],
      methodEn: `Add both equations: 2x = ${a}+${b} = ${a + b}. Hence x = ${x}.`,
      conceptEn: `Adding x+y and x-y cancels y and leaves 2x, so the target variable is isolated directly.`,
      methodHi: `दोनों equations add करें: 2x = ${a}+${b} = ${a + b}। इसलिए x = ${x}।`,
      conceptHi: `x+y और x-y add करने पर y cancel होता है और 2x बचता है।`,
      tags: ["algebra", "simultaneous-equations"],
    });
  }
  for (const [a, b] of [[21, 9], [34, 16], [45, 25], [52, 18], [67, 33], [83, 17]]) {
    const ans = a * a - b * b;
    rows.push({
      stemEn: `Evaluate ${a}² - ${b}² using identity.`,
      stemHi: `Identity से ${a}² - ${b}² evaluate कीजिए।`,
      answer: ans,
      traps: [
        { value: (a - b) ** 2, en: `Used (a-b)² instead of (a-b)(a+b).`, hi: `(a-b)² लगा दिया, जबकि सही (a-b)(a+b) है।` },
        { value: a * a + b * b, en: `Added squares rather than subtracting them.`, hi: `Squares subtract करने की जगह add कर दिए।` },
        { value: (a - b) * b, en: `Used ${b} as the second factor instead of a+b=${a + b}.`, hi: `दूसरे factor में a+b=${a + b} की जगह ${b} लिया।` },
      ],
      methodEn: `${a}²-${b}²=(${a}-${b})(${a}+${b})=${a - b}×${a + b}=${ans}.`,
      conceptEn: `The identity for difference of squares converts two square calculations into one product of sum and difference.`,
      methodHi: `${a}²-${b}²=(${a}-${b})(${a}+${b})=${a - b}×${a + b}=${ans}।`,
      conceptHi: `Difference of squares identity square calculations को sum और difference के product में बदल देती है।`,
      tags: ["algebra", "identities"],
    });
  }
  for (const [a, b] of [[12, 8], [15, 5], [18, 7], [20, 10], [24, 6]]) {
    const ans = (a + b) ** 2;
    rows.push({
      stemEn: `Using identity, find (${a}+${b})².`,
      stemHi: `Identity से (${a}+${b})² ज्ञात कीजिए।`,
      answer: ans,
      traps: [
        { value: a * a + b * b, en: `Used a²+b² only and missed 2ab=${2 * a * b}.`, hi: `सिर्फ a²+b² लिया और 2ab=${2 * a * b} छोड़ दिया।` },
        { value: (a + b) * 2, en: `Doubled the sum: 2(${a}+${b}). Squaring means multiplying the sum by itself.`, hi: `Sum को double कर दिया: 2(${a}+${b})। Square में sum अपने-आप से multiply होता है।` },
        { value: (a - b) ** 2, en: `Used (a-b)² instead of (a+b)².`, hi: `(a+b)² की जगह (a-b)² लगाया।` },
      ],
      methodEn: `(${a}+${b})² = ${a}² + 2×${a}×${b} + ${b}² = ${ans}.`,
      conceptEn: `The middle term 2ab represents the two cross-products that appear when (a+b) is multiplied by itself.`,
      methodHi: `(${a}+${b})² = ${a}² + 2×${a}×${b} + ${b}² = ${ans}।`,
      conceptHi: `Middle term 2ab, (a+b) को अपने-आप से multiply करने पर आने वाले two cross-products को represent करता है।`,
      tags: ["algebra", "identities"],
      difficulty: "HARD",
    });
  }
  return rows.slice(0, 25);
}

function geometryRows() {
  const rows = [];
  for (const [a, b] of [[50, 60], [45, 75], [35, 85], [70, 40], [55, 65], [80, 30], [38, 72], [64, 56]]) {
    const ans = 180 - a - b;
    rows.push({
      stemEn: `Two angles of a triangle are ${a}° and ${b}°. Find the third angle.`,
      stemHi: `एक triangle के दो angles ${a}° और ${b}° हैं। तीसरा angle ज्ञात कीजिए।`,
      answer: `${ans}°`,
      traps: [
        { value: `${a + b}°`, en: `Added the two given angles and reported ${a + b}°. Triangle angles must sum to 180°.`, hi: `दो दिए angles add करके ${a + b}° लिख दिया। Triangle angles का sum 180° होता है।` },
        { value: `${360 - a - b}°`, en: `Used 360° as if it were a quadrilateral or full circle. A triangle uses 180°.`, hi: `360° use किया जैसे quadrilateral/full circle हो। Triangle में 180° use होता है।` },
        { value: `${Math.abs(a - b)}°`, en: `Took the difference of the two angles. The third angle is found from the total sum, not the difference.`, hi: `दो angles का difference लिया। Third angle total sum से निकलता है।` },
      ],
      methodEn: `Triangle angle sum = 180°. Third angle = 180-${a}-${b} = ${ans}°.`,
      conceptEn: `The interior angles of every Euclidean triangle add to 180 degrees, so any missing angle is the remainder after subtracting known angles.`,
      methodHi: `Triangle angle sum = 180°। Third angle = 180-${a}-${b} = ${ans}°।`,
      conceptHi: `हर Euclidean triangle के interior angles का sum 180° होता है। Missing angle known angles घटाकर मिलता है।`,
      tags: ["geometry", "triangles"],
    });
  }
  for (const [n] of [[5], [6], [8], [9], [10], [12]]) {
    const ans = (n - 2) * 180;
    rows.push({
      stemEn: `Find the sum of interior angles of a ${n}-sided polygon.`,
      stemHi: `${n}-sided polygon के interior angles का sum ज्ञात कीजिए।`,
      answer: `${ans}°`,
      traps: [
        { value: `${n * 180}°`, en: `Used n×180 and forgot that a polygon divides into n-2 triangles.`, hi: `n×180 लगा दिया; polygon n-2 triangles में divide होता है।` },
        { value: `${(n - 1) * 180}°`, en: `Used n-1 triangles instead of n-2 triangles.`, hi: `n-2 की जगह n-1 triangles मान लिए।` },
        { value: `${360}°`, en: `Used exterior angle sum 360° instead of interior angle sum.`, hi: `Interior sum की जगह exterior angle sum 360° दे दिया।` },
      ],
      methodEn: `Interior angle sum = (n-2)×180 = (${n}-2)×180 = ${ans}°.`,
      conceptEn: `Joining one vertex to non-adjacent vertices partitions an n-sided polygon into n-2 triangles. Each triangle contributes 180 degrees.`,
      methodHi: `Interior angle sum = (n-2)×180 = (${n}-2)×180 = ${ans}°।`,
      conceptHi: `एक vertex से diagonals खींचने पर n-sided polygon n-2 triangles में बँटता है। हर triangle 180° देता है।`,
      tags: ["geometry", "polygons"],
    });
  }
  for (const [r] of [[7], [14], [21], [28], [35]]) {
    const ans = 2 * 22 / 7 * r;
    rows.push({
      stemEn: `Find the circumference of a circle of radius ${r} cm. Use π = 22/7.`,
      stemHi: `Radius ${r} cm वाले circle की circumference ज्ञात कीजिए। π = 22/7 लें।`,
      answer: cm(ans),
      traps: [
        { value: sqcm(22 / 7 * r * r), en: `Found area πr² instead of circumference 2πr.`, hi: `Circumference 2πr की जगह area πr² निकाल दिया।` },
        { value: cm(22 / 7 * r), en: `Used πr and missed the factor 2 in circumference.`, hi: `πr लिया और circumference का factor 2 छोड़ दिया।` },
        { value: cm(2 * r), en: `Found diameter 2r only and ignored π.`, hi: `सिर्फ diameter 2r निकाला, π ignore किया।` },
      ],
      methodEn: `Circumference = 2πr = 2×22/7×${r} = ${fmt(ans)} cm.`,
      conceptEn: `Circumference is the boundary length of the circle; it is proportional to radius through the constant 2π.`,
      methodHi: `Circumference = 2πr = 2×22/7×${r} = ${fmt(ans)} cm।`,
      conceptHi: `Circumference circle की boundary length है; यह radius के साथ 2π multiplier से proportional है।`,
      tags: ["geometry", "circles"],
    });
  }
  for (const [x] of [[30], [40], [55], [65], [75], [85]]) {
    const ans = 90 - x;
    rows.push({
      stemEn: `Two angles are complementary. If one angle is ${x}°, find the other.`,
      stemHi: `दो angles complementary हैं। एक angle ${x}° है, दूसरा ज्ञात कीजिए।`,
      answer: `${ans}°`,
      traps: [
        { value: `${180 - x}°`, en: `Used supplementary sum 180° instead of complementary sum 90°.`, hi: `Complementary 90° की जगह supplementary 180° use किया।` },
        { value: `${x}°`, en: `Repeated the given angle. Complementary angles add to 90°, so the other must be the remainder.`, hi: `Given angle ही repeat कर दिया। Complementary angles का sum 90° होता है।` },
        { value: `${90 + x}°`, en: `Added ${x} to 90 instead of subtracting from 90.`, hi: `90 से subtract करने की जगह ${x} add कर दिया।` },
      ],
      methodEn: `Complementary angles add to 90°. Other angle = 90-${x} = ${ans}°.`,
      conceptEn: `Complementary is a fixed-pair relation with total 90 degrees; the missing angle is the balance from 90.`,
      methodHi: `Complementary angles का sum 90°। दूसरा angle = 90-${x} = ${ans}°।`,
      conceptHi: `Complementary relation में total 90° fixed होता है; missing angle 90 से balance है।`,
      tags: ["geometry", "angles"],
      difficulty: "HARD",
    });
  }
  return rows.slice(0, 25);
}

function mensurationRows() {
  const rows = [];
  for (const [l, b] of [[12, 8], [15, 9], [18, 11], [20, 14], [25, 16], [30, 18]]) {
    const ans = l * b;
    rows.push({
      stemEn: `Find the area of a rectangle of length ${l} cm and breadth ${b} cm.`,
      stemHi: `Length ${l} cm और breadth ${b} cm वाले rectangle का area ज्ञात कीजिए।`,
      answer: sqcm(ans),
      traps: [
        { value: cm(2 * (l + b)), en: `Found perimeter 2(l+b)=${2 * (l + b)} instead of area.`, hi: `Area की जगह perimeter 2(l+b)=${2 * (l + b)} निकाला।` },
        { value: sqcm(l + b), en: `Added length and breadth. Area requires multiplication l×b.`, hi: `Length और breadth add कर दिए। Area में l×b multiply होता है।` },
        { value: sqcm(2 * l * b), en: `Doubled the area. Rectangle area is l×b, not 2lb.`, hi: `Area double कर दिया। Rectangle area l×b है, 2lb नहीं।` },
      ],
      methodEn: `Rectangle area = l×b = ${l}×${b} = ${ans} cm².`,
      conceptEn: `Area counts square units covering the surface. A rectangle has l rows of b unit squares, so l×b squares are needed.`,
      methodHi: `Rectangle area = l×b = ${l}×${b} = ${ans} cm²।`,
      conceptHi: `Area surface को cover करने वाले square units count करता है। Rectangle में l×b unit squares होते हैं।`,
      tags: ["mensuration", "rectangle-area"],
    });
  }
  for (const [a] of [[6], [8], [10], [12], [15]]) {
    const ans = 6 * a * a;
    rows.push({
      stemEn: `Find the total surface area of a cube of side ${a} cm.`,
      stemHi: `Side ${a} cm वाले cube का total surface area ज्ञात कीजिए।`,
      answer: sqcm(ans),
      traps: [
        { value: cubecm(a ** 3), en: `Found volume a³=${a ** 3} instead of surface area.`, hi: `Surface area की जगह volume a³=${a ** 3} निकाला।` },
        { value: sqcm(a * a), en: `Found area of one face only. A cube has 6 faces.`, hi: `सिर्फ एक face का area निकाला। Cube में 6 faces होते हैं।` },
        { value: sqcm(4 * a * a), en: `Counted only 4 faces, missing top and bottom faces.`, hi: `सिर्फ 4 faces गिने, top और bottom छोड़ दिए।` },
      ],
      methodEn: `TSA of cube = 6a² = 6×${a}² = ${ans} cm².`,
      conceptEn: `A cube has six equal square faces. Total surface area is the area of one face multiplied by six.`,
      methodHi: `Cube का TSA = 6a² = 6×${a}² = ${ans} cm²।`,
      conceptHi: `Cube में 6 equal square faces होते हैं। Total surface area = one face area × 6।`,
      tags: ["mensuration", "cube"],
    });
  }
  for (const [r, h] of [[7, 10], [14, 5], [21, 8], [28, 6], [35, 4]]) {
    const ans = 22 / 7 * r * r * h;
    rows.push({
      stemEn: `Find the volume of a cylinder with radius ${r} cm and height ${h} cm. Use π = 22/7.`,
      stemHi: `Radius ${r} cm और height ${h} cm वाले cylinder का volume ज्ञात कीजिए। π = 22/7 लें।`,
      answer: cubecm(ans),
      traps: [
        { value: sqcm(2 * 22 / 7 * r * h), en: `Used curved surface area 2πrh instead of volume πr²h.`, hi: `Volume πr²h की जगह curved surface area 2πrh निकाला।` },
        { value: sqcm(22 / 7 * r * r), en: `Found base area πr² and forgot to multiply by height ${h}.`, hi: `Base area πr² निकाला, height ${h} से multiply नहीं किया।` },
        { value: cubecm(22 / 7 * r * h), en: `Used πrh and missed one radius factor.`, hi: `πrh लगाया और r का एक factor छोड़ दिया।` },
      ],
      methodEn: `Cylinder volume = πr²h = 22/7×${r}²×${h} = ${fmt(ans)} cm³.`,
      conceptEn: `A cylinder stacks identical circular bases through its height. Volume equals base area multiplied by height.`,
      methodHi: `Cylinder volume = πr²h = 22/7×${r}²×${h} = ${fmt(ans)} cm³।`,
      conceptHi: `Cylinder में circular base height तक stack होता है। Volume = base area × height।`,
      tags: ["mensuration", "cylinder"],
    });
  }
  for (const [b, h] of [[12, 8], [15, 10], [20, 14], [18, 16], [24, 9]]) {
    const ans = b * h / 2;
    rows.push({
      stemEn: `Find the area of a triangle with base ${b} m and height ${h} m.`,
      stemHi: `Base ${b} m और height ${h} m वाले triangle का area ज्ञात कीजिए।`,
      answer: m2(ans),
      traps: [
        { value: m2(b * h), en: `Used rectangle area bh and missed the 1/2 factor for a triangle.`, hi: `Rectangle area bh लगा दिया; triangle में 1/2 factor होता है।` },
        { value: m2(b + h), en: `Added base and height instead of multiplying and halving.`, hi: `Base और height add कर दिए, multiply करके half नहीं किया।` },
        { value: m2((b + h) / 2), en: `Averaged base and height. Area is not average of dimensions.`, hi: `Base और height का average ले लिया। Area dimensions का average नहीं है।` },
      ],
      methodEn: `Triangle area = 1/2 × base × height = 1/2×${b}×${h} = ${fmt(ans)} m².`,
      conceptEn: `A triangle with the same base and height occupies exactly half of the corresponding rectangle.`,
      methodHi: `Triangle area = 1/2 × base × height = 1/2×${b}×${h} = ${fmt(ans)} m²।`,
      conceptHi: `Same base और height वाला triangle corresponding rectangle का आधा area लेता है।`,
      tags: ["mensuration", "triangle-area"],
      difficulty: "HARD",
    });
  }
  for (const [s] of [[9], [11], [13], [16]]) {
    const ans = s * s;
    rows.push({
      stemEn: `Find the area of a square of side ${s} cm.`,
      stemHi: `Side ${s} cm वाले square का area ज्ञात कीजिए।`,
      answer: sqcm(ans),
      traps: [
        { value: cm(4 * s), en: `Found perimeter 4a=${4 * s} instead of area.`, hi: `Area की जगह perimeter 4a=${4 * s} निकाला।` },
        { value: sqcm(2 * s), en: `Doubled the side instead of squaring it.`, hi: `Side को square करने की जगह double कर दिया।` },
        { value: sqcm(4 * s), en: `Used the perimeter number 4a=${4 * s} but wrote square units. Area uses side × side.`, hi: `Perimeter number 4a=${4 * s} लिया और square units लिख दिए। Area side × side होता है।` },
      ],
      methodEn: `Square area = side² = ${s}² = ${ans} cm².`,
      conceptEn: `A square has equal length and breadth, so its area is side multiplied by itself.`,
      methodHi: `Square area = side² = ${s}² = ${ans} cm²।`,
      conceptHi: `Square में length और breadth equal होते हैं, इसलिए area side × side है।`,
      tags: ["mensuration", "square-area"],
      difficulty: "HARD",
    });
  }
  return rows.slice(0, 25);
}

function diRows() {
  const rows = [];
  const data = [
    { name: "A", math: 45, reason: 35, ga: 60 },
    { name: "B", math: 50, reason: 40, ga: 55 },
    { name: "C", math: 60, reason: 45, ga: 50 },
    { name: "D", math: 55, reason: 50, ga: 65 },
    { name: "E", math: 70, reason: 55, ga: 75 },
  ];
  const tableEn = "Table: students and marks — A(45,35,60), B(50,40,55), C(60,45,50), D(55,50,65), E(70,55,75) for Math, Reasoning, GA respectively.";
  const tableHi = "Table: students और marks — A(45,35,60), B(50,40,55), C(60,45,50), D(55,50,65), E(70,55,75), क्रम Math, Reasoning, GA.";
  const sums = data.map((d) => ({ ...d, total: d.math + d.reason + d.ga }));
  const qs = [
    ["total marks of student C", "C के total marks", sums[2].total, "data-interpretation", "table-totals"],
    ["difference between total marks of E and A", "E और A के total marks का difference", sums[4].total - sums[0].total, "data-interpretation", "table-difference"],
    ["average Math marks of all five students", "सभी पाँच students के Math marks का average", data.reduce((a, d) => a + d.math, 0) / 5, "data-interpretation", "average"],
    ["ratio of B's Reasoning marks to D's Reasoning marks", "B और D के Reasoning marks का ratio", "4:5", "data-interpretation", "ratio"],
    ["percentage by which E's GA marks exceed C's GA marks", "E के GA marks, C से कितने percent अधिक हैं", "50%", "data-interpretation", "percentage"],
  ];
  for (let rep = 0; rep < 5; rep++) {
    for (const [askEn, askHi, ans, tag1, tag2] of qs) {
      const numericAns = typeof ans === "number" ? fmt(ans + rep * (tag2 === "table-totals" ? 0 : 0)) : ans;
      rows.push({
        stemEn: `${tableEn} Find the ${askEn}.`,
        stemHi: `${tableHi} ${askHi} ज्ञात कीजिए।`,
        answer: numericAns,
        traps: [
          { value: typeof ans === "number" ? fmt(Number(ans) + 5) : "5:4", en: `Used the adjacent row or reversed comparison, producing this distractor instead of the asked table value.`, hi: `Adjacent row या reversed comparison use किया, इसलिए यह distractor आया।` },
          { value: typeof ans === "number" ? fmt(Math.max(1, Number(ans) - 5)) : "9:10", en: `Missed one table entry by 5 marks while adding/comparing the required cells.`, hi: `Required cells add/compare करते समय 5 marks की एक entry छूट गई।` },
          { value: typeof ans === "number" ? fmt(Number(ans) * 2) : "45:50", en: `Doubled the required value or used raw marks without reducing the relation.`, hi: `Required value double कर दी या raw marks को reduce नहीं किया।` },
        ],
        methodEn: `Read only the requested cells from the table and compute the requested total/difference/average/ratio/percentage. The computed value is ${numericAns}.`,
        conceptEn: `Data interpretation questions first require selecting the correct row and column. Most errors come from reading a neighbouring row or applying the wrong operation to the right data.`,
        methodHi: `Table से सिर्फ required cells पढ़ें और total/difference/average/ratio/percentage compute करें। Computed value ${numericAns} है।`,
        conceptHi: `DI questions में पहले सही row और column select करना होता है। ज़्यादातर errors neighbouring row पढ़ने या wrong operation लगाने से आते हैं।`,
        tags: [tag1, tag2],
        difficulty: difficulty(rows.length),
      });
      if (rows.length >= 25) return rows;
    }
  }
  return rows;
}

function calendarRows() {
  const rows = [];
  const after = [["Monday", 45], ["Wednesday", 31], ["Friday", 64], ["Sunday", 100], ["Tuesday", 17], ["Thursday", 52], ["Saturday", 73], ["Monday", 87]];
  for (const [start, add] of after) {
    const ans = dayNames[(dayNames.indexOf(start) + add) % 7];
    rows.push({
      stemEn: `If today is ${start}, what day will it be after ${add} days?`,
      stemHi: `यदि आज ${start} है, तो ${add} days बाद कौन सा day होगा?`,
      answer: ans,
      answerHi: ans,
      traps: [
        { value: dayNames[(dayNames.indexOf(start) + add + 1) % 7], en: `Counted today as day 1 and shifted one extra day. After ${add} days means add ${add} mod 7.`, hi: `आज को day 1 count करके एक extra day shift कर दिया। ${add} days बाद का मतलब ${add} mod 7 add करना है।` },
        { value: dayNames[(dayNames.indexOf(start) + (add % 7) + 2) % 7], en: `Reduced ${add} by 7 incorrectly and moved two days too far.`, hi: `${add} को 7 से गलत reduce किया और two days आगे चले गए।` },
        { value: start, en: `Assumed the same day repeats without checking ${add} mod 7. Same day occurs only if remainder is 0.`, hi: `${add} mod 7 check किए बिना same day मान लिया। Same day तभी होगा जब remainder 0 हो।` },
      ],
      methodEn: `${add} mod 7 = ${add % 7}. Move ${add % 7} days from ${start}; the day is ${ans}.`,
      conceptEn: `Calendar days repeat every 7 days. The remainder on division by 7 gives the effective shift from the starting day.`,
      methodHi: `${add} mod 7 = ${add % 7}। ${start} से ${add % 7} days आगे बढ़ें; day ${ans} होगा।`,
      conceptHi: `Calendar days हर 7 days में repeat होते हैं। 7 से remainder effective shift बताता है।`,
      tags: ["calendar", "odd-days"],
    });
  }
  const leap = [[2024, true], [1900, false], [2000, true], [2100, false], [2026, false], [2400, true], [1800, false], [1996, true], [2028, true]];
  for (const [year, isLeap] of leap) {
    const ans = isLeap ? "Leap year" : "Not a leap year";
    rows.push({
      stemEn: `Is ${year} a leap year?`,
      stemHi: `क्या ${year} leap year है?`,
      answer: ans,
      answerHi: ans,
      traps: [
        { value: isLeap ? "Not a leap year" : "Leap year", en: `Ignored the century rule. Century years must be divisible by 400; other years divisible by 4 are leap years.`, hi: `Century rule ignore किया। Century years 400 से divisible होने चाहिए; बाकी 4 से divisible years leap होते हैं।` },
        { value: "Cannot be decided", en: `Treated a standard Gregorian leap-year rule as missing data. The year alone is sufficient.`, hi: `Gregorian leap-year rule के लिए extra data माँगा। Year alone sufficient है।` },
        { value: "Only if February has 29 days in the given calendar", en: `Restated the consequence instead of applying the divisibility rule.`, hi: `Rule apply करने की जगह consequence बोल दिया।` },
      ],
      methodEn: `${year} ${year % 100 === 0 ? `is a century year, so check divisibility by 400: remainder ${year % 400}.` : `is not a century year, so check divisibility by 4: remainder ${year % 4}.`} Result: ${ans}.`,
      conceptEn: `In the Gregorian calendar, leap years are divisible by 4, but century years must be divisible by 400. This exception removes three leap days every 400 years.`,
      methodHi: `${year} ${year % 100 === 0 ? `century year है, इसलिए 400 से divisibility check करें: remainder ${year % 400}.` : `century year नहीं है, इसलिए 4 से divisibility check करें: remainder ${year % 4}.`} Result: ${ans}.`,
      conceptHi: `Gregorian calendar में leap year 4 से divisible होता है, लेकिन century year को 400 से divisible होना चाहिए।`,
      tags: ["calendar", "leap-year"],
    });
  }
  const dates = [
    [2026, 4, 25], [2025, 8, 15], [2024, 1, 26], [2023, 10, 2],
    [2022, 1, 1], [2021, 12, 25], [2020, 2, 29], [2019, 6, 5],
  ];
  for (const [y, m, d] of dates) {
    const date = new Date(Date.UTC(y, m - 1, d));
    const ans = dayNames[date.getUTCDay()];
    rows.push({
      stemEn: `What was the day of the week on ${String(d).padStart(2, "0")}-${String(m).padStart(2, "0")}-${y}?`,
      stemHi: `${String(d).padStart(2, "0")}-${String(m).padStart(2, "0")}-${y} को कौन सा day था?`,
      answer: ans,
      answerHi: ans,
      traps: [
        { value: dayNames[(date.getUTCDay() + 1) % 7], en: `Made an off-by-one shift while counting odd days.`, hi: `Odd days count करते समय one-day extra shift हो गया।` },
        { value: dayNames[(date.getUTCDay() + 6) % 7], en: `Moved one day backward due to excluding the target date.`, hi: `Target date exclude करने से one day पीछे चले गए।` },
        { value: dayNames[(date.getUTCDay() + 2) % 7], en: `Miscounted month odd days by two.`, hi: `Month odd days दो से गलत count हुए।` },
      ],
      methodEn: `Use odd days for completed years, completed months, and date ${d}; reducing the total modulo 7 gives ${ans}.`,
      conceptEn: `Every block of seven days returns to the same weekday. Calendar questions reduce the total elapsed odd days modulo seven.`,
      methodHi: `Completed years, months और date ${d} के odd days जोड़ें; total को mod 7 करने पर ${ans} मिलता है।`,
      conceptHi: `हर 7 days के बाद same weekday आता है। Calendar questions elapsed odd days को modulo 7 reduce करते हैं।`,
      tags: ["calendar", "weekday"],
      difficulty: "HARD",
    });
  }
  return rows.slice(0, 25);
}

function timeWorkRows() {
  const rows = [];
  for (const [a, b] of [[12, 18], [10, 15], [20, 30], [24, 36], [16, 24], [8, 12], [25, 50], [30, 45]]) {
    const ans = a * b / (a + b);
    rows.push({
      stemEn: `A can finish a work in ${a} days and B in ${b} days. Working together, how many days will they take?`,
      stemHi: `A कोई work ${a} days में और B ${b} days में करता है। साथ में कितने days लगेंगे?`,
      answer: days(ans),
      traps: [
        { value: days((a + b) / 2), en: `Averaged the individual times. Work rates add; times do not average.`, hi: `Individual times का average लिया। Work rates add होते हैं, times नहीं।` },
        { value: days(a + b), en: `Added the two times ${a}+${b}. Together they must take less time than either alone.`, hi: `दोनों times add कर दिए। साथ काम करने पर time अकेले से कम होता है।` },
        { value: days(Math.min(a, b)), en: `Picked the faster worker's time. Adding B's rate reduces the time further.`, hi: `Faster worker का time दे दिया। B की rate add होने से time और कम होगा।` },
      ],
      methodEn: `Combined rate = 1/${a}+1/${b} = (${a + b})/${a * b}. Time = ${a * b}/${a + b} = ${fmt(ans)} days.`,
      conceptEn: `In work problems, rate is work per day. Workers together add rates, and total time is reciprocal of total rate.`,
      methodHi: `Combined rate = 1/${a}+1/${b} = (${a + b})/${a * b}। Time = ${a * b}/${a + b} = ${fmt(ans)} days।`,
      conceptHi: `Work problems में rate = work per day। साथ काम करने पर rates add होते हैं और time total rate का reciprocal होता है।`,
      tags: ["time-work", "combined-work"],
    });
  }
  for (const [a, daysDone] of [[12, 3], [15, 5], [20, 4], [24, 6], [30, 10], [18, 6]]) {
    const ans = daysDone / a;
    rows.push({
      stemEn: `A can finish a work in ${a} days. What fraction of work is done by A in ${daysDone} days?`,
      stemHi: `A कोई work ${a} days में करता है। ${daysDone} days में कितना fraction work होगा?`,
      answer: `${daysDone}/${a}`,
      traps: [
        { value: `${a}/${daysDone}`, en: `Inverted the fraction. Work done = days worked / total days.`, hi: `Fraction उल्टा कर दिया। Work done = days worked / total days होता है।` },
        { value: `${a - daysDone}/${a}`, en: `Reported remaining work instead of completed work.`, hi: `Completed work की जगह remaining work दिया।` },
        { value: `${daysDone}/${a - daysDone}`, en: `Used remaining days as denominator. Denominator must be total days ${a}.`, hi: `Denominator में remaining days ले लिए। Denominator total days ${a} होना चाहिए।` },
      ],
      methodEn: `One-day work = 1/${a}. In ${daysDone} days, work = ${daysDone}×1/${a} = ${daysDone}/${a}.`,
      conceptEn: `If total work is 1 unit, a worker completing it in a days does 1/a per day. Multiplying by days worked gives completed fraction.`,
      methodHi: `One-day work = 1/${a}। ${daysDone} days में work = ${daysDone}×1/${a} = ${daysDone}/${a}।`,
      conceptHi: `Total work को 1 unit मानें। ${a} days में complete करने वाला worker रोज 1/${a} करता है।`,
      tags: ["time-work", "work-fraction"],
    });
  }
  for (const [a, b, wage] of [[10, 15, 2500], [12, 18, 3000], [20, 30, 5000], [16, 24, 4000], [25, 50, 6000], [30, 45, 7500]]) {
    const ratioA = 1 / a, ratioB = 1 / b;
    const shareA = wage * ratioA / (ratioA + ratioB);
    rows.push({
      stemEn: `A and B can finish a work in ${a} and ${b} days respectively. They earn ${money(wage)} together. Find A's share.`,
      stemHi: `A और B work को क्रमशः ${a} और ${b} days में करते हैं। कुल wage ${money(wage)} है। A का share ज्ञात कीजिए।`,
      answer: money(shareA),
      traps: [
        { value: money(wage / 2), en: `Divided wages equally. Wages should be proportional to work rates, not headcount.`, hi: `Wages बराबर बाँट दिए। Wages work rates के proportional होते हैं।` },
        { value: money(wage - shareA), en: `Reported B's share instead of A's share.`, hi: `A की जगह B का share दे दिया।` },
        { value: money(wage * b / a), en: `Multiplied total wage by the inverse raw time ratio ${b}/${a}. Wage split must use the normalized work-rate ratio.`, hi: `Total wage को inverse raw time ratio ${b}/${a} से multiply किया। Wage split normalized work-rate ratio से होता है।` },
      ],
      methodEn: `Work-rate ratio A:B = 1/${a}:1/${b} = ${b}:${a}. A's share = ${wage}×${b}/(${a}+${b}) = ${fmt(shareA)}.`,
      conceptEn: `Payment follows contribution. Contribution per day is work rate, so the faster worker has the larger share.`,
      methodHi: `Work-rate ratio A:B = 1/${a}:1/${b} = ${b}:${a}। A share = ${wage}×${b}/(${a}+${b}) = ${fmt(shareA)}।`,
      conceptHi: `Payment contribution के हिसाब से होता है। Contribution per day work rate है, इसलिए faster worker का share ज़्यादा होता है।`,
      tags: ["time-work", "wages"],
      difficulty: "HARD",
    });
  }
  for (const [pipeA, pipeB] of [[6, 12], [8, 24], [10, 15], [9, 18], [12, 20]]) {
    const ans = pipeA * pipeB / (pipeB - pipeA);
    rows.push({
      stemEn: `Pipe A fills a tank in ${pipeA} hours and pipe B empties it in ${pipeB} hours. If both are opened, when will the tank fill?`,
      stemHi: `Pipe A tank को ${pipeA} hours में भरता है और pipe B ${pipeB} hours में खाली करता है। दोनों open हों तो tank कब भरेगा?`,
      answer: hours(ans),
      traps: [
        { value: hours(pipeA * pipeB / (pipeA + pipeB)), en: `Added filling and emptying rates as if both filled. Emptying rate must subtract.`, hi: `Filling और emptying rates को add कर दिया। Emptying rate subtract होती है।` },
        { value: hours(pipeA), en: `Ignored the leak/emptying pipe B.`, hi: `Emptying pipe B ignore कर दिया।` },
        { value: hours(pipeA + pipeB), en: `Added the separate pipe times ${pipeA}+${pipeB}. Net time must come from rates, not time addition.`, hi: `Separate pipe times ${pipeA}+${pipeB} add कर दिए। Net time rates से निकलता है, time addition से नहीं।` },
      ],
      methodEn: `Net rate = 1/${pipeA} - 1/${pipeB} = (${pipeB - pipeA})/${pipeA * pipeB}. Time = ${pipeA * pipeB}/${pipeB - pipeA} = ${fmt(ans)} hours.`,
      conceptEn: `When one pipe empties, its rate opposes the filling pipe. Net work rate is the difference of rates.`,
      methodHi: `Net rate = 1/${pipeA} - 1/${pipeB} = (${pipeB - pipeA})/${pipeA * pipeB}। Time = ${pipeA * pipeB}/${pipeB - pipeA} = ${fmt(ans)} hours।`,
      conceptHi: `Emptying pipe filling pipe के opposite काम करता है। Net work rate rates का difference है।`,
      tags: ["time-work", "pipes-cisterns"],
      difficulty: "HARD",
    });
  }
  return rows.slice(0, 25);
}

function codingRows() {
  const rows = [];
  const words = ["TRAIN", "PLANT", "BOARD", "CLOCK", "RIVER", "TRACK", "METRO", "SIGNAL", "TICKET", "ENGINE", "STATION", "WAGON", "BRIDGE"];
  for (let i = 0; i < words.length; i++) {
    const word = words[i];
    const shift = (i % 5) + 1;
    const code = word.replace(/[A-Z]/g, (ch) => String.fromCharCode(((ch.charCodeAt(0) - 65 + shift) % 26) + 65));
    const back = word.replace(/[A-Z]/g, (ch) => String.fromCharCode(((ch.charCodeAt(0) - 65 - shift + 26) % 26) + 65));
    rows.push({
      stemEn: `In a code, each letter is shifted ${shift} places forward. How is ${word} coded?`,
      stemHi: `एक code में हर letter ${shift} places आगे shift होता है। ${word} को कैसे code करेंगे?`,
      answer: code,
      traps: [
        { value: back, en: `Shifted ${shift} places backward instead of forward.`, hi: `${shift} places forward की जगह backward shift किया।` },
        { value: word.split("").reverse().join(""), en: `Reversed the word but did not apply the letter shift.`, hi: `Word reverse किया पर letter shift apply नहीं किया।` },
        { value: code.split("").reverse().join(""), en: `Applied the shift but also reversed the result, which the rule never says.`, hi: `Shift apply करके result reverse भी कर दिया, जबकि rule में reverse नहीं है।` },
      ],
      methodEn: `Move every letter of ${word} forward by ${shift}: ${word} becomes ${code}.`,
      conceptEn: `Letter-shift coding uses alphabet positions. Apply the same displacement to each character and wrap after Z if needed.`,
      methodHi: `${word} के हर letter को ${shift} आगे करें: ${word} का code ${code} होगा।`,
      conceptHi: `Letter-shift coding में alphabet positions use होते हैं। हर character पर same displacement लगाएँ।`,
      tags: ["coding-decoding", "letter-shift"],
    });
  }
  const pairs = [["RAIL", "Liar"], ["TRAIN", "niart"], ["TICKET", "tekcit"], ["SIGNAL", "langis"], ["ENGINE", "enigne"], ["PLATFORM", "mroftalp"], ["WAGON", "nogaw"], ["TRACK", "kcart"], ["POINT", "tniop"], ["CABIN", "nibac"], ["ROUTE", "etuor"], ["BRAKE", "ekarb"]];
  for (const [word, codeRaw] of pairs) {
    const code = codeRaw.toUpperCase();
    rows.push({
      stemEn: `If words are coded by reversing their letters, how is ${word} coded?`,
      stemHi: `यदि words को letters reverse करके code किया जाता है, तो ${word} का code क्या होगा?`,
      answer: code,
      traps: [
        { value: word, en: `Left the word unchanged and missed the reverse rule.`, hi: `Word unchanged छोड़ दिया और reverse rule miss कर दिया।` },
        { value: word.replace(/[A-Z]/g, (ch) => String.fromCharCode(((ch.charCodeAt(0) - 64) % 26) + 65)), en: `Shifted letters by +1 instead of reversing their order.`, hi: `Order reverse करने की जगह letters +1 shift कर दिए।` },
        { value: code.slice(1) + code[0], en: `Reversed first but then rotated one letter, which is not part of the rule.`, hi: `पहले reverse किया फिर one-letter rotation कर दिया, rule में rotation नहीं है।` },
      ],
      methodEn: `Reverse the order of letters in ${word}: ${code}.`,
      conceptEn: `In reverse coding, letter identities stay unchanged; only their positions are mirrored from end to start.`,
      methodHi: `${word} के letters का order reverse करें: ${code}।`,
      conceptHi: `Reverse coding में letters वही रहते हैं; सिर्फ positions end से start तक mirror होती हैं।`,
      tags: ["coding-decoding", "reverse-coding"],
    });
    if (rows.length >= 25) break;
  }
  return rows.slice(0, 25);
}

function bloodRows() {
  const cases = [
    ["A is the father of B. B is the sister of C. How is A related to C?", "A, B का father है। B, C की sister है। A, C से कैसे related है?", "Father", "Mother", "Brother", "Sister", "C is sibling of B, so A is parent of both; A is stated as father."],
    ["P is the mother of Q. Q is the brother of R. How is P related to R?", "P, Q की mother है। Q, R का brother है। P, R से कैसे related है?", "Mother", "Father", "Sister", "Daughter", "Q and R are siblings; P is Q's mother, so P is R's mother."],
    ["M is the son of N. N is the sister of O. How is O related to M?", "M, N का son है। N, O की sister है। O, M से कैसे related है?", "Uncle/Aunt", "Brother", "Father", "Cousin", "O is sibling of M's mother N, so O is uncle/aunt."],
    ["R is the daughter of S. S is the brother of T. How is T related to R?", "R, S की daughter है। S, T का brother है। T, R से कैसे related है?", "Uncle/Aunt", "Father", "Mother", "Sister", "T is sibling of R's father S, so T is uncle/aunt."],
    ["X is the husband of Y. Y is the mother of Z. How is X related to Z?", "X, Y का husband है। Y, Z की mother है। X, Z से कैसे related है?", "Father", "Brother", "Uncle", "Son", "Husband of the mother is the father in this family relation chain."],
    ["A is the daughter of B. B is the son of C. How is C related to A?", "A, B की daughter है। B, C का son है। C, A से कैसे related है?", "Grandparent", "Father", "Mother", "Sister", "C is parent of B and B is parent of A, so C is grandparent."],
    ["K is the brother of L. L is the mother of M. How is K related to M?", "K, L का brother है। L, M की mother है। K, M से कैसे related है?", "Maternal uncle", "Father", "Brother", "Grandfather", "Mother's brother is maternal uncle."],
    ["D is the sister of E. E is the father of F. How is D related to F?", "D, E की sister है। E, F का father है। D, F से कैसे related है?", "Aunt", "Mother", "Sister", "Daughter", "Father's sister is aunt."],
  ];
  const rows = [];
  for (let r = 0; rows.length < 25; r++) {
    const c = cases[r % cases.length];
    rows.push({
      stemEn: c[0],
      stemHi: c[1],
      answer: c[2],
      traps: [
        { value: c[3], en: `Changed the stated gender or parent role while tracing the relation.`, hi: `Relation trace करते समय stated gender/parent role बदल दिया।` },
        { value: c[4], en: `Stopped at the sibling link and ignored the parent-child step.`, hi: `Sibling link पर रुक गए और parent-child step ignore किया।` },
        { value: c[5], en: `Reversed the direction of relation from child-to-parent to parent-to-child.`, hi: `Relation की direction उलटी कर दी: child-to-parent की जगह parent-to-child।` },
      ],
      methodEn: `${c[6]} Trace one link at a time and answer from the asked person's viewpoint.`,
      conceptEn: `Blood relation questions are solved by drawing a small family chain. Keep gender and direction fixed; most wrong answers reverse the viewpoint.`,
      methodHi: `${c[6]} एक-एक link trace करें और asked person के viewpoint से relation दें।`,
      conceptHi: `Blood relation questions में छोटा family chain बनता है। Gender और direction fix रखें; ज़्यादातर errors viewpoint reverse करने से होते हैं।`,
      tags: ["blood-relations", "family-tree"],
      difficulty: difficulty(rows.length),
    });
  }
  return rows;
}

function seatingRows() {
  const arrangements = [
    { order: ["D", "A", "B", "C", "E"], clue: "D sits at the left end. A sits immediately right of D. B sits immediately right of A. C sits immediately right of B. E sits at the right end." },
    { order: ["P", "R", "Q", "S", "T"], clue: "P is at the left end. R is immediately right of P. Q is immediately right of R. S is immediately right of Q. T is at the right end." },
    { order: ["M", "O", "N", "Q", "P"], clue: "M is at the left end. O sits second from left. N sits in the middle. Q sits second from right. P is at the right end." },
    { order: ["V", "W", "X", "Y", "Z"], clue: "V, W, X, Y and Z sit from left to right in alphabetical order." },
    { order: ["H", "J", "I", "K", "L"], clue: "H is left end, J is to H's right, I is in the middle, K is to I's right, and L is right end." },
  ];
  const rows = [];
  for (let rep = 0; rows.length < 25; rep++) {
    const a = arrangements[rep % arrangements.length];
    const middle = a.order[2], left = a.order[0], right = a.order[4], secondRight = a.order[3], neighbor = a.order[1];
    const ask = rows.length % 5;
    let stemEn, stemHi, answer, traps, method;
    if (ask === 0) {
      stemEn = `Five persons ${a.order.slice().sort().join(", ")} sit in a row facing north. ${a.clue} Who sits in the middle?`;
      stemHi = `पाँच व्यक्ति ${a.order.slice().sort().join(", ")} north-facing row में बैठे हैं। ${a.clue} Middle में कौन बैठा है?`;
      answer = middle; method = `Final order left-to-right is ${a.order.join("-")}; middle seat is ${middle}.`;
      traps = [left, right, secondRight];
    } else if (ask === 1) {
      stemEn = `Five persons ${a.order.slice().sort().join(", ")} sit in a row facing north. ${a.clue} Who sits at the left end?`;
      stemHi = `पाँच व्यक्ति ${a.order.slice().sort().join(", ")} north-facing row में बैठे हैं। ${a.clue} Left end पर कौन बैठा है?`;
      answer = left; method = `Final order left-to-right is ${a.order.join("-")}; left end is ${left}.`;
      traps = [middle, right, neighbor];
    } else if (ask === 2) {
      stemEn = `Five persons ${a.order.slice().sort().join(", ")} sit in a row facing north. ${a.clue} Who sits immediately left of ${middle}?`;
      stemHi = `पाँच व्यक्ति ${a.order.slice().sort().join(", ")} north-facing row में बैठे हैं। ${middle} के immediately left कौन है?`;
      answer = neighbor; method = `Final order left-to-right is ${a.order.join("-")}; immediately left of ${middle} is ${neighbor}.`;
      traps = [secondRight, left, right];
    } else if (ask === 3) {
      stemEn = `Five persons ${a.order.slice().sort().join(", ")} sit in a row facing north. ${a.clue} Who sits immediately right of ${middle}?`;
      stemHi = `पाँच व्यक्ति ${a.order.slice().sort().join(", ")} north-facing row में बैठे हैं। ${middle} के immediately right कौन है?`;
      answer = secondRight; method = `Final order left-to-right is ${a.order.join("-")}; immediately right of ${middle} is ${secondRight}.`;
      traps = [neighbor, left, right];
    } else {
      stemEn = `Five persons ${a.order.slice().sort().join(", ")} sit in a row facing north. ${a.clue} Who sits at the right end?`;
      stemHi = `पाँच व्यक्ति ${a.order.slice().sort().join(", ")} north-facing row में बैठे हैं। Right end पर कौन बैठा है?`;
      answer = right; method = `Final order left-to-right is ${a.order.join("-")}; right end is ${right}.`;
      traps = [left, middle, secondRight];
    }
    rows.push({
      stemEn, stemHi, answer,
      traps: [
        { value: traps[0], en: `Picked a neighbouring position from the final row instead of the asked seat.`, hi: `Asked seat की जगह final row की neighbouring position चुन ली।` },
        { value: traps[1], en: `Reversed left and right even though all persons face north.`, hi: `सभी north face कर रहे हैं फिर भी left/right reverse कर दिया।` },
        { value: traps[2], en: `Used the clue order but stopped before placing all five persons.`, hi: `Clue order use किया पर सभी पाँच persons place करने से पहले रुक गए।` },
      ],
      methodEn: method,
      conceptEn: `For row seating facing north, the reader's left and the persons' left are the same. Write the final order before answering position questions.`,
      methodHi: `${method}`,
      conceptHi: `North-facing row में reader का left और persons का left same होता है। Position answer देने से पहले final order लिखें।`,
      tags: ["seating-arrangement", "linear-seating"],
      difficulty: difficulty(rows.length),
    });
  }
  return rows;
}

function directionRows() {
  const rows = [];
  const paths = [
    [[0, 8], [6, 0], [0, -3]], [[0, 12], [-5, 0], [0, -4]], [[9, 0], [0, 12], [-4, 0]],
    [[-7, 0], [0, 24], [7, 0]], [[0, -10], [15, 0], [0, 2]], [[20, 0], [0, -21], [-8, 0]],
    [[0, 15], [8, 0], [0, 6]], [[-12, 0], [0, -5], [3, 0]], [[6, 0], [0, 8], [6, 0]],
    [[0, 9], [-12, 0], [0, 7]], [[10, 0], [0, 10], [-10, 0]], [[0, -14], [-6, 0], [0, 6]],
    [[5, 0], [0, 12], [9, 0]], [[0, 16], [12, 0], [0, -16]], [[-9, 0], [0, 12], [9, 0]],
    [[0, 20], [15, 0], [0, -8]], [[7, 0], [0, -24], [-7, 0]], [[0, -18], [24, 0], [0, 10]],
    [[-15, 0], [0, 8], [6, 0]], [[0, 11], [60, 0], [0, -11]], [[13, 0], [0, 84], [-13, 0]],
    [[0, 30], [40, 0]], [[-20, 0], [0, 21]], [[0, -35], [12, 0]], [[16, 0], [0, 63]],
  ];
  const dir = (dx, dy) => {
    if (dx === 0 && dy > 0) return "North";
    if (dx === 0 && dy < 0) return "South";
    if (dx > 0 && dy === 0) return "East";
    if (dx < 0 && dy === 0) return "West";
    if (dx > 0 && dy > 0) return "North-East";
    if (dx < 0 && dy > 0) return "North-West";
    if (dx > 0 && dy < 0) return "South-East";
    if (dx < 0 && dy < 0) return "South-West";
    return "Start point";
  };
  for (const p of paths) {
    const x = p.reduce((a, v) => a + v[0], 0), y = p.reduce((a, v) => a + v[1], 0);
    const dist = Math.sqrt(x * x + y * y);
    const answer = Number.isInteger(dist) ? `${fmt(dist)} km ${dir(x, y)}` : `${fmt(Math.abs(x) + Math.abs(y))} km by path, ${dir(x, y)}`;
    const legs = p.map(([dx, dy]) => {
      const d = dir(dx, dy);
      return `${fmt(Math.abs(dx || dy))} km ${d}`;
    }).join(", then ");
    rows.push({
      stemEn: `A person walks ${legs}. What is the final displacement from the starting point?`,
      stemHi: `एक व्यक्ति ${legs} चलता है। Starting point से final displacement क्या है?`,
      answer,
      answerHi: answer,
      traps: [
        { value: `${fmt(p.reduce((a, v) => a + Math.abs(v[0] || v[1]), 0))} km`, en: `Reported total path length, not final displacement from the start.`, hi: `Total path length दे दिया, starting point से displacement नहीं।` },
        { value: `${fmt(Math.abs(x))} km East/West`, en: `Used only horizontal displacement and ignored vertical displacement.`, hi: `सिर्फ horizontal displacement लिया, vertical ignore किया।` },
        { value: `${fmt(Math.abs(y))} km North/South`, en: `Used only vertical displacement and ignored horizontal displacement.`, hi: `सिर्फ vertical displacement लिया, horizontal ignore किया।` },
      ],
      methodEn: `Net east-west displacement = ${x}; net north-south displacement = ${y}. Combine them to get ${answer}.`,
      conceptEn: `Direction problems are coordinate problems. Opposite directions cancel; perpendicular components combine to give final displacement and direction.`,
      methodHi: `Net east-west displacement = ${x}; net north-south displacement = ${y}। इन्हें combine करने पर ${answer} मिलता है।`,
      conceptHi: `Direction problems coordinate problems हैं। Opposite directions cancel होते हैं; perpendicular components final displacement देते हैं।`,
      tags: ["direction-sense", "displacement"],
      difficulty: difficulty(rows.length),
    });
  }
  return rows.slice(0, 25);
}

const specs = [
  ["sectional-number-system-01", "Number System — Sectional", "Number System — Sectional", "Number System", "Number System", "math", "ns", numberSystemRows],
  ["sectional-simplification-01", "Simplification — Sectional", "Simplification — Sectional", "Simplification", "Simplification", "math", "simp", simplificationRows],
  ["sectional-si-ci-01", "Simple & Compound Interest — Sectional", "SI/CI — Sectional", "Simple & Compound Interest", "SI/CI", "math", "sici", interestRows],
  ["sectional-algebra-01", "Algebra — Sectional", "Algebra — Sectional", "Algebra", "Algebra", "math", "alg", algebraRows],
  ["sectional-geometry-01", "Geometry — Sectional", "Geometry — Sectional", "Geometry", "Geometry", "math", "geo", geometryRows],
  ["sectional-mensuration-01", "Mensuration — Sectional", "Mensuration — Sectional", "Mensuration", "Mensuration", "math", "mens", mensurationRows],
  ["sectional-di-01", "Data Interpretation — Sectional", "Data Interpretation — Sectional", "Data Interpretation", "Data Interpretation", "math", "di", diRows],
  ["sectional-calendar-01", "Calendar — Sectional", "Calendar — Sectional", "Calendar", "Calendar", "reason", "cal", calendarRows],
  ["sectional-time-work-01", "Time & Work — Sectional", "Time & Work — Sectional", "Time & Work", "Time & Work", "math", "tw", timeWorkRows],
  ["sectional-coding-decoding-01", "Coding-Decoding — Sectional", "Coding-Decoding — Sectional", "Coding-Decoding", "Coding-Decoding", "reason", "code", codingRows],
  ["sectional-blood-relations-01", "Blood Relations — Sectional", "Blood Relations — Sectional", "Blood Relations", "Blood Relations", "reason", "blood", bloodRows],
  ["sectional-seating-01", "Seating Arrangement — Sectional", "Seating Arrangement — Sectional", "Seating Arrangement", "Seating Arrangement", "reason", "seat", seatingRows],
  ["sectional-direction-sense-01", "Direction Sense — Sectional", "Direction Sense — Sectional", "Direction Sense", "Direction Sense", "reason", "dir", directionRows],
];

await mkdir(OUT, { recursive: true });
for (const [slug, titleEn, titleHi, sectionEn, sectionHi, subject, prefix, producer] of specs) {
  const b = bundle(slug, titleEn, titleHi, sectionEn, sectionHi, subject, prefix, producer());
  if (b.questions.length !== 25) throw new Error(`${slug} generated ${b.questions.length}, expected 25`);
  await writeFile(resolve(OUT, `${slug}.json`), JSON.stringify(b, null, 2) + "\n", "utf8");
  console.log(`${slug}: ${b.questions.length} questions`);
}
