#!/usr/bin/env node
// Generate launch-grade General Awareness sectionals plus exam-shape CBT-1 mocks.
//
// Research basis: official RRB NTPC pattern is GA 40 / Math 30 / Reasoning 30
// for CBT-1, and the syllabus spans static GA, science, history, geography,
// polity, economy, computers, rail transport, math, and reasoning. Competitor
// pages were used only to benchmark topic coverage and weightage; no competitor
// question text is copied here. All rows are original MCQs over stable syllabus
// facts or previously audited original sectionals.

import { mkdir, readFile, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const OUT = resolve(__dirname, "..", "candidates");
const labels = ["A", "B", "C", "D"];

function difficulty(i) {
  if (i < 8) return "EASY";
  if (i < 20) return "MEDIUM";
  return "HARD";
}

function optionText(value) {
  return String(value);
}

function factQuestion(prefix, localIndex, data) {
  const correctIndex = localIndex % 4;
  const correct = {
    label: "",
    text_en: optionText(data.answer),
    text_hi: optionText(data.answerHi ?? data.answer),
    is_correct: true,
    trap_reason_en: null,
    trap_reason_hi: null,
  };
  const wrong = data.distractors.map((d) => ({
    label: "",
    text_en: optionText(d.en),
    text_hi: optionText(d.hi ?? d.en),
    is_correct: false,
    trap_reason_en: `Confuses ${d.en} with ${data.answer}; the clue in this question points specifically to ${data.answer}.`,
    trap_reason_hi: `${d.hi ?? d.en} को ${data.answerHi ?? data.answer} समझ लिया। इस question की clue specifically ${data.answerHi ?? data.answer} की ओर जाती है।`,
  }));
  const ordered = wrong.slice();
  ordered.splice(correctIndex, 0, correct);
  ordered.forEach((o, i) => { o.label = labels[i]; });

  const answerText = optionText(data.answer);
  const answerTextHi = optionText(data.answerHi ?? data.answer);
  return {
    id: `${prefix}-${String(localIndex + 1).padStart(2, "0")}`,
    stem_en: data.stemEn,
    stem_hi: data.stemHi,
    difficulty: data.difficulty ?? difficulty(localIndex),
    tags: data.tags,
    source: "Original",
    license: "ORIGINAL",
    options: ordered,
    explanation_method_en: `Recall the keyword: ${data.factEn} Therefore, answer = ${answerText}.`,
    explanation_concept_en: `${data.conceptEn ?? data.factEn} In NTPC GA, link the exact clue in the stem to the fixed fact before eliminating distractors. Final answer is ${answerText}.`,
    explanation_method_hi: `Keyword याद रखें: ${data.factHi ?? data.factEn} अतः answer = ${answerTextHi}।`,
    explanation_concept_hi: `${data.conceptHi ?? data.factHi ?? data.factEn} NTPC GA में stem की exact clue को fixed fact से link करके distractors हटाते हैं। Final answer ${answerTextHi} है।`,
  };
}

function sectional(slug, titleEn, titleHi, subjectHint, prefix, rows) {
  const questions = rows.map((row, i) => factQuestion(prefix, i, row));
  for (const q of questions) {
    const texts = q.options.map((o) => o.text_en);
    if (new Set(texts).size !== 4) {
      throw new Error(`${slug} ${q.id}: duplicate options ${texts.join(", ")}`);
    }
  }
  return {
    _doc: `Launch original GA sectional — ${titleEn}. Stable syllabus facts, no competitor question text copied.`,
    slug,
    title_en: `${titleEn} — Sectional`,
    title_hi: `${titleHi} — Sectional`,
    total_minutes: 25,
    section: { title_en: titleEn, title_hi: titleHi, subject_hint: subjectHint },
    questions,
  };
}

function row(stemEn, stemHi, answer, distractors, factEn, factHi, tags, extra = {}) {
  return {
    stemEn,
    stemHi,
    answer,
    answerHi: extra.answerHi,
    distractors: distractors.map((d) => Array.isArray(d) ? { en: d[0], hi: d[1] } : { en: d }),
    factEn,
    factHi,
    tags,
    ...extra,
  };
}

const physicsRows = [
  row("What is the SI unit of force?", "Force की SI unit क्या है?", "newton", ["joule", "watt", "pascal"], "Force is measured in newton.", "Force की unit newton होती है।", ["general-science", "physics", "si-units"]),
  row("What is the SI unit of work?", "Work की SI unit क्या है?", "joule", ["newton", "watt", "volt"], "Work and energy are measured in joule.", "Work और energy की unit joule होती है।", ["general-science", "physics", "si-units"]),
  row("What is the SI unit of power?", "Power की SI unit क्या है?", "watt", ["joule", "ohm", "ampere"], "Power is measured in watt.", "Power की unit watt होती है।", ["general-science", "physics", "si-units"]),
  row("Electric current is measured in which SI unit?", "Electric current किस SI unit में measured होता है?", "ampere", ["volt", "ohm", "hertz"], "Electric current is measured in ampere.", "Electric current की unit ampere है।", ["general-science", "electricity", "si-units"]),
  row("Potential difference is measured in which unit?", "Potential difference किस unit में measured होता है?", "volt", ["ampere", "ohm", "tesla"], "Potential difference is measured in volt.", "Potential difference की unit volt होती है।", ["general-science", "electricity", "si-units"]),
  row("Electrical resistance is measured in which unit?", "Electrical resistance की unit क्या है?", "ohm", ["volt", "ampere", "coulomb"], "Resistance is measured in ohm.", "Resistance की unit ohm है।", ["general-science", "electricity", "si-units"]),
  row("Frequency is measured in which SI unit?", "Frequency किस SI unit में measured होती है?", "hertz", ["watt", "newton", "kelvin"], "Frequency is measured in hertz.", "Frequency की unit hertz होती है।", ["general-science", "physics", "si-units"]),
  row("The approximate speed of sound in air at room temperature is closest to:", "Room temperature पर air में sound की approximate speed किसके closest है?", "340 m/s", ["3 x 10^8 m/s", "30 m/s", "1500 m/s"], "Sound in air travels at about 340 m/s.", "Air में sound लगभग 340 m/s चलती है।", ["general-science", "sound"]),
  row("A plane mirror forms which type of image?", "Plane mirror किस type की image बनाता है?", "virtual and same size", ["real and inverted", "magnified and real", "diminished and real"], "A plane mirror forms a virtual image of the same size.", "Plane mirror same-size virtual image बनाता है।", ["general-science", "light"]),
  row("A concave lens is also called a:", "Concave lens को क्या कहा जाता है?", "diverging lens", ["converging lens", "plane mirror", "prism"], "A concave lens diverges light rays.", "Concave lens light rays को diverge करता है।", ["general-science", "light"]),
  row("A convex lens is also called a:", "Convex lens को क्या कहा जाता है?", "converging lens", ["diverging lens", "concave mirror", "flat lens"], "A convex lens converges light rays.", "Convex lens light rays को converge करता है।", ["general-science", "light"]),
  row("The value of acceleration due to gravity near Earth is approximately:", "Earth के पास acceleration due to gravity लगभग कितना है?", "9.8 m/s^2", ["1.6 m/s^2", "340 m/s", "3 x 10^8 m/s"], "Earth's gravitational acceleration is about 9.8 m/s^2.", "Earth पर g लगभग 9.8 m/s^2 होता है।", ["general-science", "physics"]),
  row("Density is defined as:", "Density किसके बराबर होती है?", "mass/volume", ["force/area", "work/time", "distance/time"], "Density equals mass divided by volume.", "Density = mass/volume होती है।", ["general-science", "physics"]),
  row("Pressure is defined as:", "Pressure किसके बराबर होता है?", "force/area", ["mass/volume", "work/time", "charge/time"], "Pressure equals force per unit area.", "Pressure = force/area होता है।", ["general-science", "physics"]),
  row("A transformer works on the principle of:", "Transformer किस principle पर काम करता है?", "electromagnetic induction", ["diffusion", "evaporation", "neutralization"], "A transformer uses electromagnetic induction.", "Transformer electromagnetic induction पर work करता है।", ["general-science", "electricity"]),
  row("Fuse wire is chosen mainly because it has:", "Fuse wire mainly किस property के कारण चुनी जाती है?", "low melting point", ["very high melting point", "zero resistance", "magnetic poles"], "Fuse wire melts quickly when excess current flows.", "Excess current पर fuse wire जल्दी melt होती है।", ["general-science", "electricity"]),
  row("An ammeter is connected in:", "Ammeter circuit में कैसे connect होता है?", "series", ["parallel", "open circuit", "earth only"], "An ammeter is connected in series to measure current.", "Current measure करने के लिए ammeter series में लगता है।", ["general-science", "electricity"]),
  row("A voltmeter is connected in:", "Voltmeter circuit में कैसे connect होता है?", "parallel", ["series", "short circuit", "earth only"], "A voltmeter is connected in parallel across a component.", "Potential difference measure करने के लिए voltmeter parallel लगता है।", ["general-science", "electricity"]),
  row("The SI unit of temperature is:", "Temperature की SI unit क्या है?", "kelvin", ["celsius", "fahrenheit", "calorie"], "The SI base unit of temperature is kelvin.", "Temperature की SI base unit kelvin है।", ["general-science", "physics", "si-units"]),
  row("Which one is a scalar quantity?", "इनमें scalar quantity कौन-सी है?", "speed", ["velocity", "force", "acceleration"], "Speed has magnitude only, so it is scalar.", "Speed में सिर्फ magnitude होता है, इसलिए scalar है।", ["general-science", "physics"]),
  row("Which one is a vector quantity?", "इनमें vector quantity कौन-सी है?", "velocity", ["speed", "distance", "mass"], "Velocity has magnitude and direction.", "Velocity में magnitude और direction दोनों होते हैं।", ["general-science", "physics"]),
  row("Sound cannot travel through:", "Sound किसमें travel नहीं कर सकती?", "vacuum", ["air", "water", "steel"], "Sound needs a material medium and cannot travel in vacuum.", "Sound को material medium चाहिए, इसलिए vacuum में travel नहीं करती।", ["general-science", "sound"]),
  row("Friction generally acts:", "Friction generally किस direction में act करता है?", "opposite to motion", ["same as motion", "only upward", "only downward"], "Friction opposes relative motion.", "Friction relative motion को oppose करता है।", ["general-science", "physics"]),
  row("The device that converts mechanical energy into electrical energy is:", "Mechanical energy को electrical energy में convert करने वाला device कौन-सा है?", "generator", ["motor", "transformer", "fuse"], "A generator converts mechanical energy into electrical energy.", "Generator mechanical energy को electrical energy में convert करता है।", ["general-science", "electricity"]),
  row("The device that converts electrical energy into mechanical energy is:", "Electrical energy को mechanical energy में convert करने वाला device कौन-सा है?", "motor", ["generator", "voltmeter", "resistor"], "An electric motor converts electrical energy into mechanical energy.", "Motor electrical energy को mechanical energy में convert करता है।", ["general-science", "electricity"]),
];

const chemBioRows = [
  row("A solution with pH less than 7 is:", "pH 7 से कम वाला solution कैसा होता है?", "acidic", ["neutral", "basic", "saline only"], "pH below 7 indicates an acidic solution.", "pH 7 से कम acidic solution बताता है।", ["general-science", "chemistry", "acids-bases"]),
  row("A neutral solution has pH:", "Neutral solution का pH कितना होता है?", "7", ["0", "14", "1"], "A neutral solution has pH 7.", "Neutral solution का pH 7 होता है।", ["general-science", "chemistry", "acids-bases"]),
  row("The chemical name of common salt is:", "Common salt का chemical name क्या है?", "sodium chloride", ["sodium bicarbonate", "calcium carbonate", "potassium nitrate"], "Common salt is sodium chloride.", "Common salt sodium chloride होता है।", ["general-science", "chemistry"]),
  row("The chemical formula of baking soda is:", "Baking soda का chemical formula क्या है?", "NaHCO3", ["NaCl", "Na2CO3", "CaCO3"], "Baking soda is sodium bicarbonate, NaHCO3.", "Baking soda sodium bicarbonate यानी NaHCO3 है।", ["general-science", "chemistry"]),
  row("Washing soda is chemically:", "Washing soda chemically क्या है?", "sodium carbonate decahydrate", ["sodium chloride", "calcium sulphate", "sodium bicarbonate"], "Washing soda is sodium carbonate decahydrate.", "Washing soda sodium carbonate decahydrate है।", ["general-science", "chemistry"]),
  row("An acid turns blue litmus:", "Acid blue litmus को क्या करता है?", "red", ["blue", "green", "colourless"], "Acids turn blue litmus red.", "Acid blue litmus को red करता है।", ["general-science", "chemistry", "acids-bases"]),
  row("A base turns red litmus:", "Base red litmus को क्या करता है?", "blue", ["red", "yellow", "colourless"], "Bases turn red litmus blue.", "Base red litmus को blue करता है।", ["general-science", "chemistry", "acids-bases"]),
  row("The chemical symbol of gold is:", "Gold का chemical symbol क्या है?", "Au", ["Ag", "Fe", "Pb"], "Gold has the symbol Au.", "Gold का symbol Au है।", ["general-science", "chemistry", "elements"]),
  row("The chemical symbol of iron is:", "Iron का chemical symbol क्या है?", "Fe", ["Ir", "I", "In"], "Iron has the symbol Fe.", "Iron का symbol Fe है।", ["general-science", "chemistry", "elements"]),
  row("The atomic number of oxygen is:", "Oxygen का atomic number क्या है?", "8", ["6", "7", "16"], "Oxygen has atomic number 8.", "Oxygen का atomic number 8 है।", ["general-science", "chemistry", "elements"]),
  row("Red blood cells mainly help in carrying:", "Red blood cells mainly क्या carry करती हैं?", "oxygen", ["bile", "insulin", "urine"], "RBCs carry oxygen with the help of haemoglobin.", "RBC haemoglobin की मदद से oxygen carry करती हैं।", ["general-science", "biology", "human-body"]),
  row("Haemoglobin contains which metal?", "Haemoglobin में कौन-सा metal होता है?", "iron", ["copper", "zinc", "sodium"], "Haemoglobin contains iron.", "Haemoglobin में iron होता है।", ["general-science", "biology", "human-body"]),
  row("Most nutrient absorption in humans occurs in the:", "Humans में nutrients का major absorption कहाँ होता है?", "small intestine", ["stomach", "large intestine", "mouth"], "The small intestine is the main site of nutrient absorption.", "Small intestine nutrients absorption की main site है।", ["general-science", "biology", "human-body"]),
  row("Bile is secreted by the:", "Bile किस organ द्वारा secreted होता है?", "liver", ["pancreas", "kidney", "heart"], "The liver secretes bile.", "Liver bile secret करता है।", ["general-science", "biology", "human-body"]),
  row("Insulin is secreted by the:", "Insulin किस organ द्वारा secreted होता है?", "pancreas", ["liver", "thyroid", "kidney"], "The pancreas secretes insulin.", "Pancreas insulin secret करता है।", ["general-science", "biology", "human-body"]),
  row("Deficiency of vitamin C causes:", "Vitamin C की deficiency से क्या होता है?", "scurvy", ["rickets", "goitre", "night blindness"], "Vitamin C deficiency causes scurvy.", "Vitamin C deficiency से scurvy होता है।", ["general-science", "biology", "vitamins"]),
  row("Deficiency of vitamin D causes:", "Vitamin D की deficiency से क्या होता है?", "rickets", ["scurvy", "anaemia", "beriberi"], "Vitamin D deficiency causes rickets.", "Vitamin D deficiency से rickets होता है।", ["general-science", "biology", "vitamins"]),
  row("Deficiency of iodine commonly causes:", "Iodine deficiency commonly क्या cause करती है?", "goitre", ["scurvy", "rickets", "malaria"], "Iodine deficiency can cause goitre.", "Iodine deficiency से goitre हो सकता है।", ["general-science", "biology", "deficiency-diseases"]),
  row("The basic unit of heredity is:", "Heredity की basic unit क्या है?", "gene", ["neuron", "nephron", "alveolus"], "A gene is the basic unit of heredity.", "Gene heredity की basic unit है।", ["general-science", "biology", "genetics"]),
  row("Photosynthesis releases which gas?", "Photosynthesis में कौन-सी gas release होती है?", "oxygen", ["nitrogen", "carbon monoxide", "hydrogen"], "Photosynthesis releases oxygen.", "Photosynthesis oxygen release करता है।", ["general-science", "biology", "plants"]),
  row("Gas exchange in leaves mainly occurs through:", "Leaves में gas exchange mainly किससे होता है?", "stomata", ["xylem", "root hair", "petals"], "Stomata regulate gas exchange in leaves.", "Leaves में stomata gas exchange regulate करते हैं।", ["general-science", "biology", "plants"]),
  row("Malaria is caused by:", "Malaria किससे caused होता है?", "Plasmodium", ["HIV", "Vibrio cholerae", "Rhizobium"], "Malaria is caused by Plasmodium.", "Malaria Plasmodium से caused होता है।", ["general-science", "biology", "diseases"]),
  row("Tuberculosis is caused by a:", "Tuberculosis किस type के pathogen से caused होती है?", "bacterium", ["virus", "protozoan", "fungus only"], "Tuberculosis is caused by bacteria.", "Tuberculosis bacteria से caused होती है।", ["general-science", "biology", "diseases"]),
  row("AIDS is caused by:", "AIDS किससे caused होता है?", "HIV", ["HBV", "Plasmodium", "Salmonella"], "AIDS is caused by HIV.", "AIDS HIV से caused होता है।", ["general-science", "biology", "diseases"]),
  row("The functional unit of the kidney is:", "Kidney की functional unit क्या है?", "nephron", ["neuron", "alveolus", "villus"], "A nephron is the functional unit of the kidney.", "Nephron kidney की functional unit है।", ["general-science", "biology", "human-body"]),
];

const historyRows = [
  row("Which is the oldest Veda?", "सबसे पुराना Veda कौन-सा है?", "Rigveda", ["Samaveda", "Yajurveda", "Atharvaveda"], "Rigveda is regarded as the oldest Veda.", "Rigveda को सबसे पुराना Veda माना जाता है।", ["history", "ancient-india"]),
  row("Gautama Buddha attained enlightenment at:", "Gautama Buddha को enlightenment कहाँ मिला?", "Bodh Gaya", ["Sarnath", "Kushinagar", "Lumbini"], "Buddha attained enlightenment at Bodh Gaya.", "Buddha को Bodh Gaya में enlightenment मिला।", ["history", "ancient-india"]),
  row("Mahavira is associated with which religion?", "Mahavira किस religion से associated हैं?", "Jainism", ["Buddhism", "Sikhism", "Zoroastrianism"], "Mahavira is the 24th Tirthankara of Jainism.", "Mahavira Jainism के 24th Tirthankara हैं।", ["history", "ancient-india"]),
  row("Ashoka belonged to which dynasty?", "Ashoka किस dynasty से belong करते थे?", "Maurya", ["Gupta", "Mughal", "Chola"], "Ashoka was a Mauryan emperor.", "Ashoka Maurya emperor थे।", ["history", "ancient-india"]),
  row("Which war deeply changed Ashoka's policy?", "किस war ने Ashoka की policy को deeply change किया?", "Kalinga War", ["Battle of Plassey", "Battle of Buxar", "Panipat War"], "The Kalinga War transformed Ashoka's policy.", "Kalinga War ने Ashoka की policy बदल दी।", ["history", "ancient-india"]),
  row("The Gupta period is often called the:", "Gupta period को अक्सर क्या कहा जाता है?", "Golden Age", ["Iron Age", "Company Rule", "Sultanate Age"], "The Gupta period is often called the Golden Age.", "Gupta period को Golden Age कहा जाता है।", ["history", "ancient-india"]),
  row("Hiuen Tsang visited India during the reign of:", "Hiuen Tsang किसके reign में India आए?", "Harsha", ["Akbar", "Ashoka", "Shivaji"], "Hiuen Tsang visited during Harsha's time.", "Hiuen Tsang Harsha के समय आए।", ["history", "ancient-india"]),
  row("Qutub Minar was begun by:", "Qutub Minar की शुरुआत किसने की?", "Qutb-ud-din Aibak", ["Iltutmish", "Akbar", "Shah Jahan"], "Qutb-ud-din Aibak began Qutub Minar.", "Qutb-ud-din Aibak ने Qutub Minar शुरू कराया।", ["history", "medieval-india"]),
  row("The First Battle of Panipat was fought in:", "First Battle of Panipat किस year में हुई?", "1526", ["1556", "1761", "1757"], "The First Battle of Panipat was fought in 1526.", "First Battle of Panipat 1526 में हुई।", ["history", "medieval-india"]),
  row("Akbar's revenue minister was:", "Akbar के revenue minister कौन थे?", "Todar Mal", ["Birbal", "Tansen", "Raja Man Singh"], "Todar Mal is associated with Akbar's revenue system.", "Todar Mal Akbar के revenue system से associated हैं।", ["history", "medieval-india"]),
  row("Shivaji's coronation took place in:", "Shivaji का coronation किस year में हुआ?", "1674", ["1600", "1707", "1764"], "Shivaji was coronated in 1674.", "Shivaji का coronation 1674 में हुआ।", ["history", "medieval-india"]),
  row("The Battle of Plassey was fought in:", "Battle of Plassey किस year में हुई?", "1757", ["1764", "1857", "1526"], "The Battle of Plassey was fought in 1757.", "Battle of Plassey 1757 में हुई।", ["history", "modern-india"]),
  row("The Battle of Buxar was fought in:", "Battle of Buxar किस year में हुई?", "1764", ["1757", "1857", "1942"], "The Battle of Buxar was fought in 1764.", "Battle of Buxar 1764 में हुई।", ["history", "modern-india"]),
  row("The Revolt of 1857 began at:", "1857 का revolt कहाँ से शुरू हुआ?", "Meerut", ["Delhi", "Kanpur", "Jhansi"], "The Revolt of 1857 began at Meerut.", "1857 revolt Meerut से शुरू हुआ।", ["history", "freedom-struggle"]),
  row("The Indian National Congress was founded in:", "Indian National Congress किस year में founded हुई?", "1885", ["1905", "1919", "1942"], "The INC was founded in 1885.", "INC 1885 में founded हुई।", ["history", "freedom-struggle"]),
  row("The Swadeshi Movement began after the partition of:", "Swadeshi Movement किस partition के बाद शुरू हुआ?", "Bengal", ["Punjab", "Bombay", "Madras"], "The Swadeshi Movement followed the 1905 Bengal partition.", "Swadeshi Movement 1905 Bengal partition के बाद शुरू हुआ।", ["history", "freedom-struggle"]),
  row("Mahatma Gandhi returned to India from South Africa in:", "Mahatma Gandhi South Africa से India कब लौटे?", "1915", ["1905", "1919", "1930"], "Gandhi returned to India in 1915.", "Gandhi 1915 में India लौटे।", ["history", "freedom-struggle"]),
  row("The Jallianwala Bagh massacre occurred in:", "Jallianwala Bagh massacre किस year में हुआ?", "1919", ["1905", "1920", "1930"], "Jallianwala Bagh happened in 1919.", "Jallianwala Bagh 1919 में हुआ।", ["history", "freedom-struggle"]),
  row("The Non-Cooperation Movement began in:", "Non-Cooperation Movement किस year में शुरू हुआ?", "1920", ["1915", "1930", "1942"], "The Non-Cooperation Movement began in 1920.", "Non-Cooperation Movement 1920 में शुरू हुआ।", ["history", "freedom-struggle"]),
  row("The Dandi March took place in:", "Dandi March किस year में हुआ?", "1930", ["1920", "1942", "1946"], "The Dandi March took place in 1930.", "Dandi March 1930 में हुआ।", ["history", "freedom-struggle"]),
  row("The Quit India Movement was launched in:", "Quit India Movement किस year में launch हुआ?", "1942", ["1930", "1946", "1950"], "Quit India was launched in 1942.", "Quit India 1942 में launch हुआ।", ["history", "freedom-struggle"]),
  row("The Cabinet Mission came to India in:", "Cabinet Mission India कब आया?", "1946", ["1942", "1947", "1950"], "The Cabinet Mission came in 1946.", "Cabinet Mission 1946 में आया।", ["history", "freedom-struggle"]),
  row("The Constituent Assembly first met in:", "Constituent Assembly पहली बार कब मिली?", "1946", ["1947", "1949", "1950"], "The Constituent Assembly first met in 1946.", "Constituent Assembly पहली बार 1946 में मिली।", ["history", "constitution"]),
  row("India became independent on:", "India कब independent हुआ?", "15 August 1947", ["26 January 1950", "2 October 1947", "14 November 1947"], "India became independent on 15 August 1947.", "India 15 August 1947 को independent हुआ।", ["history", "freedom-struggle"]),
  row("The slogan 'Do or Die' is linked with:", "'Do or Die' slogan किस movement से linked है?", "Quit India Movement", ["Swadeshi Movement", "Dandi March", "Non-Cooperation Movement"], "Do or Die was the call of Quit India Movement.", "Do or Die Quit India Movement का call था।", ["history", "freedom-struggle"]),
];

const geographyRows = [
  row("Which important latitude passes through India?", "India से कौन-सी important latitude गुजरती है?", "Tropic of Cancer", ["Equator", "Tropic of Capricorn", "Arctic Circle"], "The Tropic of Cancer passes through India.", "Tropic of Cancer India से गुजरती है।", ["geography", "india-geography"]),
  row("India's Standard Meridian is:", "India का Standard Meridian कौन-सा है?", "82.5°E", ["68.7°E", "77.0°E", "90.0°E"], "India uses 82.5°E as the Standard Meridian.", "India का Standard Meridian 82.5°E है।", ["geography", "india-geography"]),
  row("The largest Indian state by area is:", "Area के आधार पर India का largest state कौन-सा है?", "Rajasthan", ["Madhya Pradesh", "Maharashtra", "Uttar Pradesh"], "Rajasthan is the largest state by area.", "Rajasthan area से largest state है।", ["geography", "india-geography"]),
  row("The smallest Indian state by area is:", "Area के आधार पर India का smallest state कौन-सा है?", "Goa", ["Sikkim", "Tripura", "Nagaland"], "Goa is the smallest state by area.", "Goa area से smallest state है।", ["geography", "india-geography"]),
  row("The longest river within India is commonly identified as:", "India के भीतर longest river commonly किसे माना जाता है?", "Ganga", ["Narmada", "Tapi", "Mahi"], "Ganga is commonly identified as the longest river within India.", "India के भीतर Ganga को longest river माना जाता है।", ["geography", "rivers"]),
  row("The southernmost point of mainland India is:", "Mainland India का southernmost point कौन-सा है?", "Kanyakumari", ["Indira Point", "Kochi", "Rameswaram"], "Kanyakumari is the southern tip of mainland India.", "Mainland India का southern tip Kanyakumari है।", ["geography", "india-geography"]),
  row("The Western Ghats run roughly parallel to the:", "Western Ghats roughly किस coast के parallel हैं?", "western coast", ["eastern coast", "northern plains", "Thar Desert"], "Western Ghats run along the western coast.", "Western Ghats western coast के साथ चलते हैं।", ["geography", "physical-geography"]),
  row("Black soil is especially suitable for:", "Black soil खास तौर पर किस crop के लिए suitable है?", "cotton", ["tea", "jute", "rubber"], "Black soil is associated with cotton cultivation.", "Black soil cotton cultivation से associated है।", ["geography", "soils"]),
  row("Alluvial soil is dominant in the:", "Alluvial soil mainly कहाँ पाई जाती है?", "northern plains", ["Thar Desert", "Deccan traps only", "Andaman islands"], "Northern plains have extensive alluvial soil.", "Northern plains में alluvial soil extensive है।", ["geography", "soils"]),
  row("The Indian monsoon is mainly marked by:", "Indian monsoon mainly किससे marked होता है?", "seasonal reversal of winds", ["permanent west winds", "no rainfall", "polar night"], "Monsoon involves seasonal reversal of winds.", "Monsoon में winds का seasonal reversal होता है।", ["geography", "climate"]),
  row("The Himalayas are examples of:", "Himalayas किसका example हैं?", "young fold mountains", ["block mountains", "volcanic islands", "old plateaus"], "The Himalayas are young fold mountains.", "Himalayas young fold mountains हैं।", ["geography", "physical-geography"]),
  row("The Deccan Plateau is part of:", "Deccan Plateau किसका part है?", "Peninsular India", ["Northern Plains", "Himalayan arc", "Ganga delta"], "The Deccan Plateau is in Peninsular India.", "Deccan Plateau Peninsular India में है।", ["geography", "physical-geography"]),
  row("Chilika Lake is located in:", "Chilika Lake किस state में है?", "Odisha", ["Gujarat", "Kerala", "Punjab"], "Chilika Lake is in Odisha.", "Chilika Lake Odisha में है।", ["geography", "lakes"]),
  row("The Sundarbans are associated with the delta of:", "Sundarbans किस delta से associated हैं?", "Ganga-Brahmaputra", ["Narmada-Tapi", "Godavari-Krishna only", "Indus only"], "Sundarbans lie in the Ganga-Brahmaputra delta region.", "Sundarbans Ganga-Brahmaputra delta region में हैं।", ["geography", "rivers"]),
  row("The Thar Desert is mainly in:", "Thar Desert mainly कहाँ है?", "Rajasthan", ["Assam", "Kerala", "Bihar"], "The Thar Desert is mainly in Rajasthan.", "Thar Desert mainly Rajasthan में है।", ["geography", "physical-geography"]),
  row("The Narmada river flows into the:", "Narmada river किसमें गिरती है?", "Arabian Sea", ["Bay of Bengal", "Chilika Lake", "Indian Ocean at Kanyakumari"], "Narmada is a west-flowing river draining into the Arabian Sea.", "Narmada west-flowing river है और Arabian Sea में गिरती है।", ["geography", "rivers"]),
  row("Kaziranga National Park is in:", "Kaziranga National Park किस state में है?", "Assam", ["Gujarat", "Uttarakhand", "Madhya Pradesh"], "Kaziranga National Park is in Assam.", "Kaziranga National Park Assam में है।", ["geography", "environment"]),
  row("Gir National Park is famous for:", "Gir National Park किसके लिए famous है?", "Asiatic lion", ["one-horned rhinoceros", "snow leopard", "red panda"], "Gir is famous for the Asiatic lion.", "Gir Asiatic lion के लिए famous है।", ["geography", "environment"]),
  row("The Sundarbans are a major example of:", "Sundarbans किसका major example हैं?", "mangrove forest", ["tropical desert", "alpine meadow", "coral island"], "Sundarbans are mangrove forests.", "Sundarbans mangrove forests हैं।", ["geography", "environment"]),
  row("Jim Corbett National Park is in:", "Jim Corbett National Park किस state में है?", "Uttarakhand", ["Assam", "Gujarat", "Odisha"], "Jim Corbett National Park is in Uttarakhand.", "Jim Corbett National Park Uttarakhand में है।", ["geography", "environment"]),
  row("Andaman and Nicobar Islands are located in the:", "Andaman and Nicobar Islands किसमें located हैं?", "Bay of Bengal", ["Arabian Sea", "Palk Strait only", "Gulf of Kutch"], "Andaman and Nicobar lie in the Bay of Bengal.", "Andaman and Nicobar Bay of Bengal में हैं।", ["geography", "india-geography"]),
  row("Lakshadweep is located in the:", "Lakshadweep किसमें located है?", "Arabian Sea", ["Bay of Bengal", "Indian Ocean near Sri Lanka", "Gulf of Mannar"], "Lakshadweep lies in the Arabian Sea.", "Lakshadweep Arabian Sea में है।", ["geography", "india-geography"]),
  row("Indian Standard Time is:", "Indian Standard Time क्या है?", "UTC+5:30", ["UTC", "UTC+4:30", "UTC+6:30"], "Indian Standard Time is UTC+5:30.", "Indian Standard Time UTC+5:30 है।", ["geography", "india-geography"]),
  row("The Mahanadi delta is mainly associated with:", "Mahanadi delta mainly किस state से associated है?", "Odisha", ["Punjab", "Gujarat", "Himachal Pradesh"], "The Mahanadi delta lies along Odisha's coast.", "Mahanadi delta Odisha coast से associated है।", ["geography", "rivers"]),
  row("The Konkan coast lies along the:", "Konkan coast किस sea के along है?", "Arabian Sea", ["Bay of Bengal", "Red Sea", "Caspian Sea"], "Konkan is a west-coast belt along the Arabian Sea.", "Konkan Arabian Sea के along west-coast belt है।", ["geography", "india-geography"]),
];

const polityRows = [
  row("The Constitution of India was adopted on:", "India का Constitution कब adopted हुआ?", "26 November 1949", ["26 January 1950", "15 August 1947", "2 October 1949"], "The Constitution was adopted on 26 November 1949.", "Constitution 26 November 1949 को adopted हुआ।", ["polity", "constitution"]),
  row("The Constitution of India came into force on:", "India का Constitution कब enforce हुआ?", "26 January 1950", ["26 November 1949", "15 August 1947", "30 January 1950"], "The Constitution came into force on 26 January 1950.", "Constitution 26 January 1950 को enforce हुआ।", ["polity", "constitution"]),
  row("The Preamble begins with:", "Preamble किस phrase से शुरू होता है?", "We, the People of India", ["Satyameva Jayate", "In Parliament We Trust", "Union of States only"], "The Preamble starts with We, the People of India.", "Preamble 'We, the People of India' से शुरू होता है।", ["polity", "constitution"]),
  row("Article 14 mainly deals with:", "Article 14 mainly किससे deal करता है?", "equality before law", ["right to life", "constitutional remedies", "fundamental duties"], "Article 14 provides equality before law.", "Article 14 equality before law देता है।", ["polity", "fundamental-rights"]),
  row("Article 19 is linked with:", "Article 19 किससे linked है?", "freedoms", ["emergency", "GST", "CAG"], "Article 19 protects certain freedoms.", "Article 19 certain freedoms protect करता है।", ["polity", "fundamental-rights"]),
  row("Article 21 protects:", "Article 21 क्या protect करता है?", "life and personal liberty", ["right to property as fundamental right", "panchayats", "money bill"], "Article 21 protects life and personal liberty.", "Article 21 life और personal liberty protect करता है।", ["polity", "fundamental-rights"]),
  row("Article 32 is associated with:", "Article 32 किससे associated है?", "constitutional remedies", ["state finance", "official language only", "trade unions"], "Article 32 gives the right to constitutional remedies.", "Article 32 constitutional remedies का right देता है।", ["polity", "fundamental-rights"]),
  row("Fundamental Duties are in:", "Fundamental Duties कहाँ हैं?", "Article 51A", ["Article 14", "Article 21", "Article 324"], "Fundamental Duties are listed in Article 51A.", "Fundamental Duties Article 51A में हैं।", ["polity", "constitution"]),
  row("Directive Principles of State Policy are in:", "Directive Principles of State Policy कहाँ हैं?", "Part IV", ["Part III", "Part IVA", "Part XVIII"], "DPSP are placed in Part IV.", "DPSP Part IV में हैं।", ["polity", "constitution"]),
  row("The nominal head of the Union executive is the:", "Union executive का nominal head कौन है?", "President", ["Prime Minister", "Speaker", "Chief Justice"], "The President is the nominal head of the Union executive.", "President Union executive के nominal head हैं।", ["polity", "union-executive"]),
  row("The real head of the Council of Ministers is the:", "Council of Ministers का real head कौन है?", "Prime Minister", ["President", "Governor", "Vice President"], "The Prime Minister heads the Council of Ministers.", "Prime Minister Council of Ministers को head करते हैं।", ["polity", "union-executive"]),
  row("A Money Bill can be introduced only in the:", "Money Bill केवल कहाँ introduce किया जा सकता है?", "Lok Sabha", ["Rajya Sabha", "Supreme Court", "NITI Aayog"], "A Money Bill is introduced only in Lok Sabha.", "Money Bill केवल Lok Sabha में introduce होता है।", ["polity", "parliament"]),
  row("The Rajya Sabha is a:", "Rajya Sabha कैसी house है?", "permanent house", ["dissolved every 5 years", "state court", "money bill committee"], "Rajya Sabha is not dissolved; it is permanent.", "Rajya Sabha dissolve नहीं होती; यह permanent house है।", ["polity", "parliament"]),
  row("The highest court in India is the:", "India की highest court कौन-सी है?", "Supreme Court", ["High Court", "District Court", "Tribunal"], "The Supreme Court is India's highest court.", "Supreme Court India की highest court है।", ["polity", "judiciary"]),
  row("The Election Commission of India is mentioned in:", "Election Commission of India किस Article में mentioned है?", "Article 324", ["Article 14", "Article 51A", "Article 76"], "Article 324 deals with Election Commission.", "Article 324 Election Commission से deal करता है।", ["polity", "constitutional-bodies"]),
  row("The CAG of India is mentioned in:", "India के CAG का Article कौन-सा है?", "Article 148", ["Article 280", "Article 324", "Article 21"], "Article 148 deals with the CAG.", "Article 148 CAG से deal करता है।", ["polity", "constitutional-bodies"]),
  row("Panchayats were constitutionalised by the:", "Panchayats को constitutional status किस amendment से मिला?", "73rd Amendment", ["74th Amendment", "42nd Amendment", "101st Amendment"], "The 73rd Amendment concerns Panchayats.", "73rd Amendment Panchayats से related है।", ["polity", "local-government"]),
  row("Municipalities were constitutionalised by the:", "Municipalities को constitutional status किस amendment से मिला?", "74th Amendment", ["73rd Amendment", "44th Amendment", "86th Amendment"], "The 74th Amendment concerns Municipalities.", "74th Amendment Municipalities से related है।", ["polity", "local-government"]),
  row("GST was introduced through the:", "GST किस constitutional amendment से introduced हुआ?", "101st Amendment", ["73rd Amendment", "42nd Amendment", "61st Amendment"], "GST is linked to the 101st Amendment.", "GST 101st Amendment से linked है।", ["polity", "constitution", "economy"]),
  row("Emergency provisions are mainly in:", "Emergency provisions mainly कहाँ हैं?", "Part XVIII", ["Part III", "Part IV", "Part IX"], "Emergency provisions are in Part XVIII.", "Emergency provisions Part XVIII में हैं।", ["polity", "constitution"]),
  row("A republic means the head of state is:", "Republic का मतलब head of state कैसा होता है?", "elected", ["hereditary monarch", "appointed by UN", "selected by court"], "In a republic, the head of state is elected.", "Republic में head of state elected होता है।", ["polity", "constitution"]),
  row("The Vice President of India is ex officio Chairman of the:", "Vice President India किसके ex officio Chairman हैं?", "Rajya Sabha", ["Lok Sabha", "NITI Aayog", "Finance Commission"], "The Vice President chairs the Rajya Sabha ex officio.", "Vice President Rajya Sabha के ex officio Chairman हैं।", ["polity", "parliament"]),
  row("A State Governor is appointed by the:", "State Governor किसके द्वारा appointed होता है?", "President", ["Prime Minister alone", "Chief Minister", "Speaker"], "A Governor is appointed by the President.", "Governor President द्वारा appointed होता है।", ["polity", "state-executive"]),
  row("The Attorney General of India is mentioned in:", "Attorney General of India किस Article में mentioned है?", "Article 76", ["Article 32", "Article 148", "Article 280"], "Article 76 deals with the Attorney General.", "Article 76 Attorney General से deal करता है।", ["polity", "constitutional-posts"]),
  row("India is described in Article 1 as a:", "Article 1 में India को क्या describe किया गया है?", "Union of States", ["Confederation only", "Unitary kingdom", "Municipal federation"], "Article 1 calls India a Union of States.", "Article 1 India को Union of States कहता है।", ["polity", "constitution"]),
];

const economyRows = [
  row("GDP stands for:", "GDP का full form क्या है?", "Gross Domestic Product", ["General Domestic Price", "Gross Debt Product", "Government Deposit Plan"], "GDP means Gross Domestic Product.", "GDP का अर्थ Gross Domestic Product है।", ["economy", "basic-economy"]),
  row("India's central bank is the:", "India का central bank कौन-सा है?", "Reserve Bank of India", ["State Bank of India", "NABARD", "SEBI"], "RBI is India's central bank.", "RBI India का central bank है।", ["economy", "banking"]),
  row("A fiscal deficit broadly means:", "Fiscal deficit broadly किसे कहते हैं?", "government expenditure exceeds receipts excluding borrowings", ["exports exceed imports", "prices fall continuously", "bank deposits double"], "Fiscal deficit is the gap funded by borrowings.", "Fiscal deficit वह gap है जिसे borrowing से fund किया जाता है।", ["economy", "public-finance"]),
  row("Repo rate is the rate at which:", "Repo rate वह rate है जिस पर:", "RBI lends to banks", ["banks lend to RBI only", "SEBI lists shares", "government pays salary"], "Repo rate is RBI's short-term lending rate to banks.", "Repo rate RBI का banks को lending rate है।", ["economy", "banking"]),
  row("CRR stands for:", "CRR का full form क्या है?", "Cash Reserve Ratio", ["Current Repo Rate", "Credit Relief Rule", "Capital Receipt Register"], "CRR means Cash Reserve Ratio.", "CRR का अर्थ Cash Reserve Ratio है।", ["economy", "banking"]),
  row("Inflation means:", "Inflation का मतलब क्या है?", "rise in general price level", ["fall in population", "fixed bank rate", "increase in rainfall"], "Inflation is a sustained rise in general prices.", "Inflation general price level में rise है।", ["economy", "inflation"]),
  row("MGNREGA is mainly linked with:", "MGNREGA mainly किससे linked है?", "rural wage employment", ["space research", "urban metro only", "stock trading"], "MGNREGA provides rural wage employment guarantee.", "MGNREGA rural wage employment guarantee से linked है।", ["economy", "schemes"]),
  row("Pradhan Mantri Jan Dhan Yojana focuses on:", "PM Jan Dhan Yojana का focus क्या है?", "financial inclusion", ["rocket launch", "soil testing only", "rail electrification"], "PMJDY focuses on financial inclusion through bank accounts.", "PMJDY bank accounts के through financial inclusion पर focus करती है।", ["economy", "schemes"]),
  row("Pradhan Mantri Ujjwala Yojana is linked with:", "PM Ujjwala Yojana किससे linked है?", "LPG connections", ["crop insurance", "digital payments only", "metro rail"], "Ujjwala is linked with LPG connections.", "Ujjwala LPG connections से linked है।", ["economy", "schemes"]),
  row("Ayushman Bharat is mainly a:", "Ayushman Bharat mainly क्या है?", "health protection scheme", ["rail freight policy", "space mission", "defence exercise"], "Ayushman Bharat is a health protection programme.", "Ayushman Bharat health protection programme है।", ["economy", "schemes"]),
  row("PM-KISAN provides:", "PM-KISAN क्या provide करता है?", "income support to farmers", ["free rail tickets", "urban housing tax", "defence pension only"], "PM-KISAN provides income support to eligible farmers.", "PM-KISAN eligible farmers को income support देता है।", ["economy", "schemes"]),
  row("GST is a:", "GST किस type का tax है?", "indirect tax", ["direct tax", "wealth title", "bank deposit"], "GST is an indirect tax on supply of goods and services.", "GST goods/services supply पर indirect tax है।", ["economy", "taxation"]),
  row("Income tax is a:", "Income tax किस type का tax है?", "direct tax", ["indirect tax", "customs duty only", "cess on goods only"], "Income tax is paid directly on income.", "Income tax income पर direct tax है।", ["economy", "taxation"]),
  row("NITI Aayog replaced the:", "NITI Aayog ने किस institution को replace किया?", "Planning Commission", ["Election Commission", "Finance Commission", "Railway Board"], "NITI Aayog replaced the Planning Commission.", "NITI Aayog ने Planning Commission को replace किया।", ["economy", "institutions"]),
  row("NABARD is mainly associated with:", "NABARD mainly किससे associated है?", "agriculture and rural development", ["stock exchanges", "space missions", "supreme court"], "NABARD supports agriculture and rural development finance.", "NABARD agriculture और rural development finance से linked है।", ["economy", "institutions"]),
  row("SEBI regulates the:", "SEBI किस market को regulate करता है?", "securities market", ["weather department", "railway recruitment", "school boards"], "SEBI regulates India's securities market.", "SEBI securities market regulate करता है।", ["economy", "institutions"]),
  row("Balance of Payments records:", "Balance of Payments क्या record करता है?", "transactions with the rest of the world", ["only railway tickets", "only rainfall", "only school results"], "BoP records economic transactions with the rest of the world.", "BoP rest of the world के साथ economic transactions record करता है।", ["economy", "external-sector"]),
  row("MSP stands for:", "MSP का full form क्या है?", "Minimum Support Price", ["Maximum Selling Price", "Monthly Salary Plan", "Market Share Policy"], "MSP means Minimum Support Price.", "MSP का अर्थ Minimum Support Price है।", ["economy", "agriculture"]),
  row("The Green Revolution is linked mainly with:", "Green Revolution mainly किससे linked है?", "food grains", ["milk", "fish", "oil and gas"], "The Green Revolution raised food grain production.", "Green Revolution food grain production से linked है।", ["economy", "agriculture"]),
  row("The White Revolution is linked mainly with:", "White Revolution mainly किससे linked है?", "milk", ["coal", "cotton", "jute"], "White Revolution is linked to milk production.", "White Revolution milk production से linked है।", ["economy", "agriculture"]),
  row("The Blue Revolution is linked mainly with:", "Blue Revolution mainly किससे linked है?", "fish production", ["wheat production", "steel production", "textiles"], "Blue Revolution relates to fisheries.", "Blue Revolution fisheries से related है।", ["economy", "agriculture"]),
  row("HDI is published by:", "HDI किस organization द्वारा publish होता है?", "UNDP", ["RBI", "SEBI", "ISRO"], "The Human Development Index is associated with UNDP.", "Human Development Index UNDP से associated है।", ["economy", "human-development"]),
  row("CPI is commonly used to track:", "CPI commonly किसे track करता है?", "retail inflation", ["rainfall", "rail speed", "forest cover only"], "Consumer Price Index tracks retail inflation.", "Consumer Price Index retail inflation track करता है।", ["economy", "inflation"]),
  row("FDI stands for:", "FDI का full form क्या है?", "Foreign Direct Investment", ["Fiscal Debt Index", "Food Distribution Input", "Federal Deposit Insurance"], "FDI means Foreign Direct Investment.", "FDI का अर्थ Foreign Direct Investment है।", ["economy", "external-sector"]),
  row("Make in India mainly promotes:", "Make in India mainly किसे promote करता है?", "manufacturing", ["desert tourism only", "court procedure", "monsoon forecast"], "Make in India promotes manufacturing and investment.", "Make in India manufacturing और investment promote करता है।", ["economy", "schemes"]),
];

const computerRailRows = [
  row("CPU is often called the:", "CPU को अक्सर क्या कहा जाता है?", "brain of the computer", ["printer of the computer", "screen of the computer", "battery only"], "CPU performs central processing and control.", "CPU central processing और control करता है।", ["computers", "basic-computers"]),
  row("RAM is a type of:", "RAM किस type की memory है?", "volatile memory", ["non-volatile memory", "optical disc", "printer"], "RAM is volatile memory.", "RAM volatile memory होती है।", ["computers", "memory"]),
  row("ROM is generally:", "ROM generally कैसी memory है?", "non-volatile memory", ["volatile memory", "temporary cache only", "input device"], "ROM is non-volatile memory.", "ROM non-volatile memory होती है।", ["computers", "memory"]),
  row("URL stands for:", "URL का full form क्या है?", "Uniform Resource Locator", ["Universal Record Link", "User Readable Language", "Unit Register List"], "URL means Uniform Resource Locator.", "URL का अर्थ Uniform Resource Locator है।", ["computers", "internet"]),
  row("An operating system mainly:", "Operating system mainly क्या करता है?", "manages hardware and software resources", ["prints only photos", "stores only passwords", "cools the CPU physically"], "An OS manages hardware and software resources.", "OS hardware और software resources manage करता है।", ["computers", "operating-system"]),
  row("Spreadsheet software is used mainly for:", "Spreadsheet software mainly किसके लिए use होता है?", "tabular calculations", ["image capture only", "virus removal only", "speaker control"], "Spreadsheets organise rows, columns and calculations.", "Spreadsheet rows, columns और calculations organize करता है।", ["computers", "applications"]),
  row("PDF stands for:", "PDF का full form क्या है?", "Portable Document Format", ["Printed Data File", "Public Device Folder", "Program Design Flow"], "PDF means Portable Document Format.", "PDF का अर्थ Portable Document Format है।", ["computers", "file-formats"]),
  row("LAN stands for:", "LAN का full form क्या है?", "Local Area Network", ["Large Access Number", "Linked Archive Node", "Long Audio Network"], "LAN means Local Area Network.", "LAN का अर्थ Local Area Network है।", ["computers", "networking"]),
  row("An IP address is used to:", "IP address किसके लिए use होता है?", "identify a device on a network", ["increase battery capacity", "print colour pages", "measure rainfall"], "IP address identifies a networked device.", "IP address networked device identify करता है।", ["computers", "networking"]),
  row("Malware means:", "Malware का मतलब क्या है?", "malicious software", ["manual hardware", "mail server", "memory address only"], "Malware is malicious software.", "Malware malicious software होता है।", ["computers", "cybersecurity"]),
  row("A firewall is used for:", "Firewall किसके लिए use होता है?", "network security", ["water cooling", "screen brightness", "typing speed"], "A firewall helps protect network traffic.", "Firewall network traffic protect करने में help करता है।", ["computers", "cybersecurity"]),
  row("Phishing is a type of:", "Phishing किस type की activity है?", "online fraud", ["file compression", "screen display", "valid backup"], "Phishing tricks users through fake communication.", "Phishing fake communication से users को trick करता है।", ["computers", "cybersecurity"]),
  row("Ctrl+C is commonly used for:", "Ctrl+C commonly किसके लिए use होता है?", "copy", ["paste", "save", "print"], "Ctrl+C is the copy shortcut.", "Ctrl+C copy shortcut है।", ["computers", "shortcuts"]),
  row("Ctrl+V is commonly used for:", "Ctrl+V commonly किसके लिए use होता है?", "paste", ["copy", "undo", "select all"], "Ctrl+V is the paste shortcut.", "Ctrl+V paste shortcut है।", ["computers", "shortcuts"]),
  row("The binary number system uses:", "Binary number system किन digits का use करता है?", "0 and 1", ["1 and 2", "0 to 9", "A to F only"], "Binary uses only 0 and 1.", "Binary केवल 0 और 1 use करता है।", ["computers", "number-systems"]),
  row("India's first passenger train ran in 1853 between:", "India की first passenger train 1853 में कहाँ चली?", "Mumbai and Thane", ["Delhi and Agra", "Kolkata and Patna", "Chennai and Bengaluru"], "India's first passenger train ran between Mumbai and Thane.", "India की first passenger train Mumbai और Thane के बीच चली।", ["railways", "transport-systems"]),
  row("The Railway Board was established in:", "Railway Board किस year में established हुआ?", "1905", ["1853", "1947", "1950"], "The Railway Board was established in 1905.", "Railway Board 1905 में established हुआ।", ["railways", "transport-systems"]),
  row("Rail Bhavan, associated with Indian Railways, is in:", "Indian Railways से associated Rail Bhavan कहाँ है?", "New Delhi", ["Mumbai", "Kolkata", "Chennai"], "Rail Bhavan is in New Delhi.", "Rail Bhavan New Delhi में है।", ["railways", "transport-systems"]),
  row("Konkan Railway is associated with the:", "Konkan Railway किस region से associated है?", "western coastal belt", ["Thar Desert", "Ganga plain only", "Himalayan glacier belt"], "Konkan Railway serves the western coastal belt.", "Konkan Railway western coastal belt से associated है।", ["railways", "transport-systems"]),
  row("Dedicated Freight Corridor is built mainly for:", "Dedicated Freight Corridor mainly किसके लिए built है?", "freight movement", ["tourist buses", "air traffic", "postal stamps"], "Dedicated Freight Corridors prioritise freight movement.", "Dedicated Freight Corridor freight movement के लिए है।", ["railways", "transport-systems"]),
  row("IRCTC is mainly associated with:", "IRCTC mainly किससे associated है?", "rail ticketing and catering services", ["space launch", "bank regulation", "weather forecast"], "IRCTC is linked with rail ticketing, catering and tourism services.", "IRCTC rail ticketing, catering और tourism services से linked है।", ["railways", "transport-systems"]),
  row("Vande Bharat Express is best described as a:", "Vande Bharat Express को best कैसे describe करेंगे?", "semi-high-speed trainset", ["steam locomotive only", "freight wagon only", "metro station"], "Vande Bharat is a semi-high-speed trainset.", "Vande Bharat semi-high-speed trainset है।", ["railways", "transport-systems"]),
  row("Metro rail is mainly a form of:", "Metro rail mainly किसका form है?", "urban mass transit", ["deep sea transport", "rural irrigation", "space transport"], "Metro rail serves urban mass transit.", "Metro rail urban mass transit serve करती है।", ["railways", "transport-systems"]),
  row("Rail gauge means the:", "Rail gauge का मतलब क्या है?", "distance between two rails", ["length of platform", "height of signal", "weight of coach"], "Gauge is the distance between the two rails.", "Gauge दो rails के बीच की distance है।", ["railways", "transport-systems"]),
  row("A loco pilot is responsible for:", "Loco pilot किसके लिए responsible होता है?", "driving the train", ["selling tickets", "maintaining court records", "printing timetables only"], "A loco pilot drives the train.", "Loco pilot train drive करता है।", ["railways", "transport-systems"]),
];

const staticEnvironmentRows = [
  row("The United Nations was founded in:", "United Nations किस year में founded हुआ?", "1945", ["1919", "1950", "1965"], "The UN was founded in 1945.", "UN 1945 में founded हुआ।", ["static-gk", "world-organizations"]),
  row("WHO headquarters is in:", "WHO headquarters कहाँ है?", "Geneva", ["Paris", "New York", "Rome"], "WHO is headquartered in Geneva.", "WHO headquarters Geneva में है।", ["static-gk", "world-organizations"]),
  row("UNESCO headquarters is in:", "UNESCO headquarters कहाँ है?", "Paris", ["Geneva", "Washington DC", "Kathmandu"], "UNESCO is headquartered in Paris.", "UNESCO headquarters Paris में है।", ["static-gk", "world-organizations"]),
  row("SAARC headquarters is in:", "SAARC headquarters कहाँ है?", "Kathmandu", ["New Delhi", "Dhaka", "Colombo"], "SAARC headquarters is in Kathmandu.", "SAARC headquarters Kathmandu में है।", ["static-gk", "world-organizations"]),
  row("World Environment Day is observed on:", "World Environment Day कब observe होता है?", "5 June", ["22 April", "16 September", "10 December"], "World Environment Day is on 5 June.", "World Environment Day 5 June को होता है।", ["environment", "important-days"]),
  row("World Ozone Day is observed on:", "World Ozone Day कब observe होता है?", "16 September", ["5 June", "22 April", "2 October"], "World Ozone Day is on 16 September.", "World Ozone Day 16 September को होता है।", ["environment", "important-days"]),
  row("Earth Day is observed on:", "Earth Day कब observe होता है?", "22 April", ["5 June", "16 September", "1 May"], "Earth Day is on 22 April.", "Earth Day 22 April को होता है।", ["environment", "important-days"]),
  row("International Day for Biological Diversity is observed on:", "International Day for Biological Diversity कब observe होता है?", "22 May", ["22 April", "5 June", "16 September"], "Biodiversity Day is on 22 May.", "Biodiversity Day 22 May को होता है।", ["environment", "important-days"]),
  row("The Ramsar Convention is related to:", "Ramsar Convention किससे related है?", "wetlands", ["deserts", "nuclear weapons", "postal services"], "Ramsar Convention concerns wetlands.", "Ramsar Convention wetlands से related है।", ["environment", "conventions"]),
  row("A major greenhouse gas is:", "Major greenhouse gas कौन-सी है?", "carbon dioxide", ["argon", "helium", "neon"], "Carbon dioxide is a major greenhouse gas.", "Carbon dioxide major greenhouse gas है।", ["environment", "climate"]),
  row("India's national animal is the:", "India का national animal कौन-सा है?", "tiger", ["lion", "elephant", "deer"], "The tiger is India's national animal.", "Tiger India का national animal है।", ["static-gk", "national-symbols"]),
  row("India's national bird is the:", "India का national bird कौन-सा है?", "peacock", ["sparrow", "eagle", "swan"], "The peacock is India's national bird.", "Peacock India का national bird है।", ["static-gk", "national-symbols"]),
  row("India's national aquatic animal is the:", "India का national aquatic animal कौन-सा है?", "Ganges river dolphin", ["blue whale", "olive ridley turtle", "mahseer"], "The Ganges river dolphin is India's national aquatic animal.", "Ganges river dolphin India का national aquatic animal है।", ["static-gk", "national-symbols"]),
  row("India's national song is:", "India का national song कौन-सा है?", "Vande Mataram", ["Jana Gana Mana", "Saare Jahan Se Achha", "Mile Sur Mera Tumhara"], "Vande Mataram is India's national song.", "Vande Mataram India का national song है।", ["static-gk", "national-symbols"]),
  row("India's national anthem is:", "India का national anthem कौन-सा है?", "Jana Gana Mana", ["Vande Mataram", "Ae Mere Watan Ke Logon", "Sare Jahan Se Achha"], "Jana Gana Mana is India's national anthem.", "Jana Gana Mana India का national anthem है।", ["static-gk", "national-symbols"]),
  row("The Father of the Indian Constitution is commonly called:", "Indian Constitution के Father commonly किसे कहा जाता है?", "B. R. Ambedkar", ["Mahatma Gandhi", "Jawaharlal Nehru", "Sardar Patel"], "B. R. Ambedkar is called the Father of the Indian Constitution.", "B. R. Ambedkar को Indian Constitution का Father कहा जाता है।", ["static-gk", "famous-personalities"]),
  row("The Iron Man of India is:", "Iron Man of India कौन हैं?", "Sardar Vallabhbhai Patel", ["Subhas Chandra Bose", "B. R. Ambedkar", "C. V. Raman"], "Sardar Patel is called the Iron Man of India.", "Sardar Patel को Iron Man of India कहा जाता है।", ["static-gk", "famous-personalities"]),
  row("The Missile Man of India is:", "Missile Man of India कौन हैं?", "A. P. J. Abdul Kalam", ["Homi Bhabha", "Vikram Sarabhai", "C. V. Raman"], "A. P. J. Abdul Kalam is called the Missile Man of India.", "A. P. J. Abdul Kalam को Missile Man of India कहा जाता है।", ["static-gk", "famous-personalities"]),
  row("The first Indian Nobel laureate in Literature was:", "Literature में first Indian Nobel laureate कौन थे?", "Rabindranath Tagore", ["C. V. Raman", "Amartya Sen", "Mother Teresa"], "Rabindranath Tagore won the Nobel Prize in Literature.", "Rabindranath Tagore ने Literature में Nobel Prize जीता।", ["static-gk", "famous-personalities"]),
  row("India's highest civilian award is:", "India का highest civilian award कौन-सा है?", "Bharat Ratna", ["Padma Shri", "Padma Bhushan", "Dronacharya Award"], "Bharat Ratna is India's highest civilian award.", "Bharat Ratna India का highest civilian award है।", ["static-gk", "awards"]),
  row("Raj Ghat is associated with:", "Raj Ghat किससे associated है?", "Mahatma Gandhi", ["Jawaharlal Nehru", "Indira Gandhi", "Sardar Patel"], "Raj Ghat is Mahatma Gandhi's memorial.", "Raj Ghat Mahatma Gandhi का memorial है।", ["static-gk", "places"]),
  row("Gateway of India is located in:", "Gateway of India कहाँ located है?", "Mumbai", ["Delhi", "Kolkata", "Hyderabad"], "Gateway of India is in Mumbai.", "Gateway of India Mumbai में है।", ["static-gk", "monuments"]),
  row("Charminar is located in:", "Charminar कहाँ located है?", "Hyderabad", ["Mumbai", "Delhi", "Jaipur"], "Charminar is in Hyderabad.", "Charminar Hyderabad में है।", ["static-gk", "monuments"]),
  row("India Gate is located in:", "India Gate कहाँ located है?", "New Delhi", ["Mumbai", "Kolkata", "Chennai"], "India Gate is in New Delhi.", "India Gate New Delhi में है।", ["static-gk", "monuments"]),
  row("Ajanta Caves are located in:", "Ajanta Caves किस state में हैं?", "Maharashtra", ["Odisha", "Punjab", "Assam"], "Ajanta Caves are in Maharashtra.", "Ajanta Caves Maharashtra में हैं।", ["static-gk", "monuments"]),
];

const gaSpecs = [
  ["sectional-general-science-physics-01", "General Science: Physics", "General Science: Physics", "ga", "gaphy", physicsRows],
  ["sectional-chemistry-biology-01", "Chemistry & Biology", "Chemistry & Biology", "ga", "gacb", chemBioRows],
  ["sectional-indian-history-01", "Indian History", "Indian History", "ga", "gahis", historyRows],
  ["sectional-indian-geography-01", "Indian Geography", "Indian Geography", "ga", "gageo", geographyRows],
  ["sectional-indian-polity-01", "Indian Polity", "Indian Polity", "ga", "gapol", polityRows],
  ["sectional-economy-schemes-01", "Economy & Schemes", "Economy & Schemes", "ga", "gaeco", economyRows],
  ["sectional-computers-railways-01", "Computers & Railways", "Computers & Railways", "ga", "gacomp", computerRailRows],
  ["sectional-static-gk-environment-01", "Static GK & Environment", "Static GK & Environment", "ga", "gastat", staticEnvironmentRows],
];

const mathCandidateSlugs = [
  "sectional-percentage",
  "sectional-profit-loss",
  "sectional-ratio",
  "sectional-averages",
  "sectional-tsd",
  "sectional-number-system-01",
  "sectional-simplification-01",
  "sectional-si-ci-01",
  "sectional-algebra-01",
  "sectional-geometry-01",
  "sectional-mensuration-01",
  "sectional-di-01",
  "sectional-time-work-01",
];

const reasonCandidateSlugs = [
  "sectional-syllogism",
  "sectional-calendar-01",
  "sectional-coding-decoding-01",
  "sectional-blood-relations-01",
  "sectional-seating-01",
  "sectional-direction-sense-01",
];

async function loadPool(slugs) {
  const out = [];
  for (const slug of slugs) {
    const bundle = JSON.parse(await readFile(resolve(OUT, `${slug}.json`), "utf8"));
    for (const q of bundle.questions) out.push({ ...q, _from: slug });
  }
  return out;
}

function cloneQuestion(q, id, extraTags) {
  return {
    ...q,
    id,
    tags: Array.from(new Set([...(q.tags ?? []), ...extraTags])),
    source: "Original",
    license: "ORIGINAL",
    options: q.options.map((o) => ({ ...o })),
  };
}

function mockBundle(index, gaPool, mathPool, reasonPool) {
  const mockNo = String(index + 1).padStart(2, "0");
  const ga = gaPool.slice(index * 40, index * 40 + 40).map((q, i) => cloneQuestion(q, `cbt1m${mockNo}-ga-${String(i + 1).padStart(2, "0")}`, ["cbt1-full-mock", "general-awareness"]));
  const math = mathPool.slice(index * 30, index * 30 + 30).map((q, i) => cloneQuestion(q, `cbt1m${mockNo}-math-${String(i + 1).padStart(2, "0")}`, ["cbt1-full-mock", "mathematics"]));
  const reason = reasonPool.slice(index * 30, index * 30 + 30).map((q, i) => cloneQuestion(q, `cbt1m${mockNo}-reason-${String(i + 1).padStart(2, "0")}`, ["cbt1-full-mock", "reasoning"]));
  if (ga.length !== 40 || math.length !== 30 || reason.length !== 30) {
    throw new Error(`mock ${mockNo}: expected 40/30/30, got ${ga.length}/${math.length}/${reason.length}`);
  }
  const sections = [
    { title_en: "General Awareness", title_hi: "General Awareness", subject_hint: "ga", questions: ga },
    { title_en: "Mathematics", title_hi: "Mathematics", subject_hint: "math", questions: math },
    { title_en: "General Intelligence & Reasoning", title_hi: "General Intelligence & Reasoning", subject_hint: "reason", questions: reason },
  ];
  return {
    _doc: "Exam-shape CBT-1 mock assembled from audited original RailPrep sectional items. Pattern: GA 40 / Math 30 / Reasoning 30.",
    slug: `ntpc-cbt1-full-mock-${mockNo}`,
    title_en: `NTPC CBT-1 Full Mock ${mockNo}`,
    title_hi: `NTPC CBT-1 Full Mock ${mockNo}`,
    kind: "CBT1_FULL",
    exam_target: "NTPC_CBT1",
    total_minutes: 90,
    sections,
    questions: sections.flatMap((s) => s.questions),
  };
}

await mkdir(OUT, { recursive: true });
const generatedGa = [];
for (const [slug, titleEn, titleHi, subject, prefix, rows] of gaSpecs) {
  if (rows.length !== 25) throw new Error(`${slug}: expected 25 rows, got ${rows.length}`);
  const bundle = sectional(slug, titleEn, titleHi, subject, prefix, rows);
  generatedGa.push(bundle);
  await writeFile(resolve(OUT, `${slug}.json`), JSON.stringify(bundle, null, 2) + "\n", "utf8");
  console.log(`${slug}: ${bundle.questions.length} questions`);
}

const gaPool = generatedGa.flatMap((b) => b.questions);
const mathPool = await loadPool(mathCandidateSlugs);
const reasonPool = await loadPool(reasonCandidateSlugs);

for (let i = 0; i < 5; i++) {
  const bundle = mockBundle(i, gaPool, mathPool, reasonPool);
  await writeFile(resolve(OUT, `${bundle.slug}.json`), JSON.stringify(bundle, null, 2) + "\n", "utf8");
  console.log(`${bundle.slug}: ${bundle.questions.length} questions`);
}
