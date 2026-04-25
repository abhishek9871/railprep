#!/usr/bin/env node
// Generate additional coverage sectionals for the official RRB NTPC syllabus gaps:
// current/science-tech GA, art/culture/literature, sports/awards/orgs, and
// reasoning families not covered by the first launch sectional batch.

import { mkdir, writeFile } from "node:fs/promises";
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

function fmt(n) {
  return Number.isInteger(n) ? String(n) : String(Number(n.toFixed(2)));
}

function qrow(stemEn, stemHi, answer, traps, methodEn, conceptEn, methodHi, conceptHi, tags, extra = {}) {
  return { stemEn, stemHi, answer, answerHi: extra.answerHi, traps, methodEn, conceptEn, methodHi, conceptHi, tags, difficulty: extra.difficulty };
}

function fact(stemEn, stemHi, answer, distractors, factEn, factHi, tags, extra = {}) {
  return qrow(
    stemEn,
    stemHi,
    answer,
    distractors.map((d) => ({
      value: Array.isArray(d) ? d[0] : d,
      valueHi: Array.isArray(d) ? d[1] : undefined,
      en: `Confuses ${Array.isArray(d) ? d[0] : d} with ${answer}; the clue in the question points specifically to ${answer}.`,
      hi: `${Array.isArray(d) ? d[1] : d} को ${extra.answerHi ?? answer} समझ लिया। Stem की clue specifically ${extra.answerHi ?? answer} की ओर जाती है।`,
    })),
    `Recall the fixed fact: ${factEn}`,
    `${factEn} This is a stable RRB NTPC General Awareness fact; match the keyword in the stem before eliminating distractors.`,
    `Fixed fact याद रखें: ${factHi}`,
    `${factHi} यह stable RRB NTPC General Awareness fact है; distractors हटाने से पहले stem keyword match करें।`,
    tags,
    extra,
  );
}

function buildQuestion(prefix, i, data) {
  const correctIndex = i % 4;
  const correct = {
    label: "",
    text_en: String(data.answer),
    text_hi: String(data.answerHi ?? data.answer),
    is_correct: true,
    trap_reason_en: null,
    trap_reason_hi: null,
  };
  const wrong = data.traps.map((t) => ({
    label: "",
    text_en: String(t.value),
    text_hi: String(t.valueHi ?? t.value),
    is_correct: false,
    trap_reason_en: t.en,
    trap_reason_hi: t.hi,
  }));
  const ordered = wrong.slice();
  ordered.splice(correctIndex, 0, correct);
  ordered.forEach((o, idx) => { o.label = labels[idx]; });
  return {
    id: `${prefix}-${String(i + 1).padStart(2, "0")}`,
    stem_en: data.stemEn,
    stem_hi: data.stemHi,
    difficulty: data.difficulty ?? difficulty(i),
    tags: data.tags,
    source: "Original",
    license: "ORIGINAL",
    options: ordered,
    explanation_method_en: `${data.methodEn} Therefore, answer = ${data.answer}.`,
    explanation_concept_en: `${data.conceptEn} Final answer is ${data.answer}.`,
    explanation_method_hi: `${data.methodHi} अतः answer = ${data.answerHi ?? data.answer}।`,
    explanation_concept_hi: `${data.conceptHi} Final answer ${data.answerHi ?? data.answer} है।`,
  };
}

function bundle(slug, titleEn, titleHi, subject, prefix, rows) {
  if (rows.length !== 25) throw new Error(`${slug}: expected 25 rows, got ${rows.length}`);
  const questions = rows.map((r, i) => buildQuestion(prefix, i, r));
  for (const q of questions) {
    const texts = q.options.map((o) => o.text_en);
    if (new Set(texts).size !== 4) throw new Error(`${slug} ${q.id}: duplicate option ${texts.join(", ")}`);
  }
  return {
    _doc: `Coverage sectional — ${titleEn}. Original rows for official RRB NTPC syllabus coverage.`,
    slug,
    title_en: `${titleEn} — Sectional`,
    title_hi: `${titleHi} — Sectional`,
    total_minutes: 25,
    section: { title_en: titleEn, title_hi: titleHi, subject_hint: subject },
    questions,
  };
}

const currentRows = [
  fact("Chandrayaan-3 made India's soft landing near the Moon's south polar region in which year?", "Chandrayaan-3 ने Moon के south polar region के पास soft landing किस year में की?", "2023", ["2019", "2022", "2024"], "Chandrayaan-3 soft-landed in 2023.", "Chandrayaan-3 ने 2023 में soft landing की।", ["current-affairs", "science-tech", "space"]),
  fact("Aditya-L1 is India's mission to study the:", "Aditya-L1 India का किसे study करने वाला mission है?", "Sun", ["Moon", "Mars", "Venus"], "Aditya-L1 is a solar observation mission.", "Aditya-L1 solar observation mission है।", ["current-affairs", "science-tech", "space"]),
  fact("Aditya-L1 was launched by which launch vehicle?", "Aditya-L1 किस launch vehicle से launch हुआ?", "PSLV-C57", ["GSLV Mk III", "SSLV-D1", "ASLV"], "Aditya-L1 was launched by PSLV-C57.", "Aditya-L1 PSLV-C57 से launch हुआ।", ["current-affairs", "science-tech", "space"]),
  fact("XPoSat is associated with studies of:", "XPoSat किस study से associated है?", "X-ray polarization", ["soil moisture", "ocean salinity", "solar eclipse photography"], "XPoSat studies X-ray polarization.", "XPoSat X-ray polarization study करता है।", ["current-affairs", "science-tech", "space"]),
  fact("The 2023 G20 Summit hosted by India was held in:", "India द्वारा hosted 2023 G20 Summit कहाँ हुआ?", "New Delhi", ["Mumbai", "Bengaluru", "Hyderabad"], "India hosted the 2023 G20 Summit in New Delhi.", "India ने 2023 G20 Summit New Delhi में host किया।", ["current-affairs", "summits"]),
  fact("India's 2023 G20 theme was:", "India की 2023 G20 theme क्या थी?", "One Earth, One Family, One Future", ["Make in India", "Digital First, Global Next", "Green India Mission"], "The 2023 G20 theme was One Earth, One Family, One Future.", "2023 G20 theme One Earth, One Family, One Future थी।", ["current-affairs", "summits"]),
  fact("The ICC Men's T20 World Cup 2024 was won by:", "ICC Men's T20 World Cup 2024 किसने जीता?", "India", ["Australia", "South Africa", "England"], "India won the 2024 ICC Men's T20 World Cup.", "India ने 2024 ICC Men's T20 World Cup जीता।", ["current-affairs", "sports"]),
  fact("The Paris Olympics were held in:", "Paris Olympics किस year में हुए?", "2024", ["2020", "2022", "2026"], "The Paris Olympics were held in 2024.", "Paris Olympics 2024 में हुए।", ["current-affairs", "sports"]),
  fact("Neeraj Chopra won which medal in men's javelin at Paris 2024?", "Paris 2024 men's javelin में Neeraj Chopra ने कौन-सा medal जीता?", "silver", ["gold", "bronze", "no medal"], "Neeraj Chopra won silver at Paris 2024.", "Neeraj Chopra ने Paris 2024 में silver जीता।", ["current-affairs", "sports"]),
  fact("The 2024 Nobel Peace Prize was awarded to:", "2024 Nobel Peace Prize किसे दिया गया?", "Nihon Hidankyo", ["World Food Programme", "Narges Mohammadi", "ICAN"], "Nihon Hidankyo received the 2024 Nobel Peace Prize.", "Nihon Hidankyo को 2024 Nobel Peace Prize मिला।", ["current-affairs", "awards"]),
  fact("COP29 was hosted by:", "COP29 किसने host किया?", "Azerbaijan", ["Brazil", "India", "UAE"], "COP29 was hosted in Baku, Azerbaijan.", "COP29 Baku, Azerbaijan में host हुआ।", ["current-affairs", "environment"]),
  fact("COP30 was scheduled/hosted in:", "COP30 किस country में scheduled/hosted था?", "Brazil", ["Azerbaijan", "Egypt", "India"], "COP30 is associated with Brazil.", "COP30 Brazil से associated है।", ["current-affairs", "environment"]),
  fact("The Nari Shakti Vandan Adhiniyam is linked with:", "Nari Shakti Vandan Adhiniyam किससे linked है?", "women's reservation", ["GST", "forest rights", "railway safety"], "Nari Shakti Vandan Adhiniyam is the women's reservation law.", "Nari Shakti Vandan Adhiniyam women's reservation से linked है।", ["current-affairs", "polity"]),
  fact("PM Surya Ghar: Muft Bijli Yojana is linked with:", "PM Surya Ghar: Muft Bijli Yojana किससे linked है?", "rooftop solar", ["rail freight", "river linking", "crop MSP"], "PM Surya Ghar is a rooftop solar scheme.", "PM Surya Ghar rooftop solar scheme है।", ["current-affairs", "schemes"]),
  fact("Bharat Ratna 2024 recipient M. S. Swaminathan is associated with:", "Bharat Ratna 2024 recipient M. S. Swaminathan किससे associated हैं?", "Green Revolution", ["White Revolution", "Missile programme", "Indian cinema"], "M. S. Swaminathan is associated with the Green Revolution.", "M. S. Swaminathan Green Revolution से associated हैं।", ["current-affairs", "awards"]),
  fact("Bharat Ratna 2024 recipient Karpoori Thakur was a former Chief Minister of:", "Bharat Ratna 2024 recipient Karpoori Thakur किस state के former Chief Minister थे?", "Bihar", ["Odisha", "Kerala", "Punjab"], "Karpoori Thakur was a former Chief Minister of Bihar.", "Karpoori Thakur Bihar के former Chief Minister थे।", ["current-affairs", "awards"]),
  fact("The 2023 ICC Men's ODI World Cup was won by:", "2023 ICC Men's ODI World Cup किसने जीता?", "Australia", ["India", "England", "New Zealand"], "Australia won the 2023 ICC Men's ODI World Cup.", "Australia ने 2023 ICC Men's ODI World Cup जीता।", ["current-affairs", "sports"]),
  fact("India crossed 100 medals at the Hangzhou Asian Games with a total of:", "Hangzhou Asian Games में India ने कितने medals जीते?", "107", ["97", "117", "87"], "India won 107 medals at the Hangzhou Asian Games.", "India ने Hangzhou Asian Games में 107 medals जीते।", ["current-affairs", "sports"]),
  fact("At the 2024 Chess Olympiad, India won gold in:", "2024 Chess Olympiad में India ने gold किसमें जीता?", "both open and women's sections", ["open section only", "women's section only", "neither section"], "India won both open and women's team golds at the 2024 Chess Olympiad.", "India ने 2024 Chess Olympiad में open और women's दोनों team gold जीते।", ["current-affairs", "sports"]),
  fact("The 2024 Nobel Prize in Physics was associated with work foundational to:", "2024 Nobel Prize in Physics किस work से associated था?", "machine learning", ["green revolution", "DNA sequencing only", "black hole imaging only"], "The 2024 Physics Nobel honoured foundational machine-learning work.", "2024 Physics Nobel foundational machine-learning work से associated था।", ["current-affairs", "awards", "science-tech"]),
  fact("The 2024 Nobel Prize in Chemistry included work on:", "2024 Nobel Prize in Chemistry किस work से associated था?", "protein structure and design", ["ozone hole discovery", "graphene only", "radioactivity discovery"], "The 2024 Chemistry Nobel was linked with protein structure and design.", "2024 Chemistry Nobel protein structure और design से linked था।", ["current-affairs", "awards", "science-tech"]),
  fact("India's new Parliament building was inaugurated in:", "India का new Parliament building किस year में inaugurated हुआ?", "2023", ["2019", "2021", "2025"], "The new Parliament building was inaugurated in 2023.", "New Parliament building 2023 में inaugurated हुआ।", ["current-affairs", "polity"]),
  fact("The Chandrayaan-3 mission was led by:", "Chandrayaan-3 mission किस organization ने lead किया?", "ISRO", ["DRDO", "BARC", "CSIR"], "Chandrayaan-3 was an ISRO mission.", "Chandrayaan-3 ISRO mission था।", ["current-affairs", "science-tech", "space"]),
  fact("The 2024 T20 World Cup final was played against:", "2024 T20 World Cup final India ने किसके against खेला?", "South Africa", ["Australia", "England", "Pakistan"], "India defeated South Africa in the 2024 T20 World Cup final.", "India ने 2024 T20 World Cup final में South Africa को हराया।", ["current-affairs", "sports"]),
  fact("Arshad Nadeem's Paris 2024 gold medal was in:", "Arshad Nadeem का Paris 2024 gold medal किस event में था?", "javelin throw", ["long jump", "shot put", "discus throw"], "Arshad Nadeem won javelin throw gold at Paris 2024.", "Arshad Nadeem ने Paris 2024 में javelin throw gold जीता।", ["current-affairs", "sports"]),
];

const cultureRows = [
  fact("Bharatanatyam is a classical dance form of:", "Bharatanatyam किस state का classical dance है?", "Tamil Nadu", ["Kerala", "Assam", "Odisha"], "Bharatanatyam is associated with Tamil Nadu.", "Bharatanatyam Tamil Nadu से associated है।", ["art-culture", "dance"]),
  fact("Kathakali is a classical dance-drama from:", "Kathakali किस state से है?", "Kerala", ["Punjab", "Rajasthan", "Bihar"], "Kathakali is associated with Kerala.", "Kathakali Kerala से associated है।", ["art-culture", "dance"]),
  fact("Sattriya dance is associated with:", "Sattriya dance किस state से associated है?", "Assam", ["Gujarat", "Tamil Nadu", "Maharashtra"], "Sattriya is associated with Assam.", "Sattriya Assam से associated है।", ["art-culture", "dance"]),
  fact("Odissi dance is associated with:", "Odissi dance किस state से associated है?", "Odisha", ["Kerala", "Punjab", "Goa"], "Odissi is associated with Odisha.", "Odissi Odisha से associated है।", ["art-culture", "dance"]),
  fact("Kuchipudi is associated with:", "Kuchipudi किस state से associated है?", "Andhra Pradesh", ["Assam", "Haryana", "Jharkhand"], "Kuchipudi is associated with Andhra Pradesh.", "Kuchipudi Andhra Pradesh से associated है।", ["art-culture", "dance"]),
  fact("Bihu is a major folk festival of:", "Bihu किस state का major folk festival है?", "Assam", ["Kerala", "Punjab", "Tamil Nadu"], "Bihu is associated with Assam.", "Bihu Assam से associated है।", ["art-culture", "festivals"]),
  fact("Pongal is mainly celebrated in:", "Pongal mainly कहाँ celebrate होता है?", "Tamil Nadu", ["Punjab", "Bihar", "Rajasthan"], "Pongal is a major Tamil Nadu harvest festival.", "Pongal Tamil Nadu का harvest festival है।", ["art-culture", "festivals"]),
  fact("Onam is mainly celebrated in:", "Onam mainly कहाँ celebrate होता है?", "Kerala", ["Assam", "Himachal Pradesh", "Madhya Pradesh"], "Onam is associated with Kerala.", "Onam Kerala से associated है।", ["art-culture", "festivals"]),
  fact("Lohri is mainly associated with:", "Lohri mainly किस region/state से associated है?", "Punjab", ["Kerala", "Odisha", "Manipur"], "Lohri is strongly associated with Punjab.", "Lohri Punjab से strongly associated है।", ["art-culture", "festivals"]),
  fact("Madhubani painting is associated with:", "Madhubani painting किस state से associated है?", "Bihar", ["Goa", "Nagaland", "Haryana"], "Madhubani painting is associated with Bihar.", "Madhubani painting Bihar से associated है।", ["art-culture", "painting"]),
  fact("Warli painting is associated with:", "Warli painting किस state से associated है?", "Maharashtra", ["Punjab", "Assam", "Kerala"], "Warli painting is associated with Maharashtra.", "Warli painting Maharashtra से associated है।", ["art-culture", "painting"]),
  fact("Pattachitra painting is associated with:", "Pattachitra painting किस state से associated है?", "Odisha", ["Gujarat", "Uttarakhand", "Bihar"], "Pattachitra is associated with Odisha.", "Pattachitra Odisha से associated है।", ["art-culture", "painting"]),
  fact("The author of Gitanjali was:", "Gitanjali के author कौन थे?", "Rabindranath Tagore", ["Premchand", "R. K. Narayan", "Bankim Chandra Chatterjee"], "Gitanjali was written by Rabindranath Tagore.", "Gitanjali Rabindranath Tagore ने लिखी।", ["literature", "indian-literature"]),
  fact("Godan was written by:", "Godan किसने लिखी?", "Premchand", ["Mahadevi Verma", "Harivansh Rai Bachchan", "Mulk Raj Anand"], "Godan is a novel by Premchand.", "Godan Premchand का novel है।", ["literature", "indian-literature"]),
  fact("Anandamath was written by:", "Anandamath किसने लिखी?", "Bankim Chandra Chatterjee", ["Rabindranath Tagore", "Sarojini Naidu", "Premchand"], "Anandamath was written by Bankim Chandra Chatterjee.", "Anandamath Bankim Chandra Chatterjee ने लिखी।", ["literature", "indian-literature"]),
  fact("The Discovery of India was written by:", "The Discovery of India किसने लिखी?", "Jawaharlal Nehru", ["Mahatma Gandhi", "B. R. Ambedkar", "Sardar Patel"], "The Discovery of India was written by Jawaharlal Nehru.", "The Discovery of India Jawaharlal Nehru ने लिखी।", ["literature", "indian-literature"]),
  fact("Wings of Fire is associated with:", "Wings of Fire किससे associated है?", "A. P. J. Abdul Kalam", ["C. V. Raman", "Vikram Sarabhai", "Homi Bhabha"], "Wings of Fire is associated with A. P. J. Abdul Kalam.", "Wings of Fire A. P. J. Abdul Kalam से associated है।", ["literature", "indian-literature"]),
  fact("The Sanchi Stupa is located in:", "Sanchi Stupa किस state में है?", "Madhya Pradesh", ["Bihar", "Odisha", "Gujarat"], "Sanchi Stupa is in Madhya Pradesh.", "Sanchi Stupa Madhya Pradesh में है।", ["art-culture", "monuments"]),
  fact("The Sun Temple at Konark is in:", "Konark Sun Temple किस state में है?", "Odisha", ["Gujarat", "Tamil Nadu", "Rajasthan"], "Konark Sun Temple is in Odisha.", "Konark Sun Temple Odisha में है।", ["art-culture", "monuments"]),
  fact("Hampi is located in:", "Hampi किस state में है?", "Karnataka", ["Kerala", "Punjab", "Bihar"], "Hampi is in Karnataka.", "Hampi Karnataka में है।", ["art-culture", "monuments"]),
  fact("Khajuraho temples are in:", "Khajuraho temples किस state में हैं?", "Madhya Pradesh", ["Rajasthan", "Maharashtra", "Assam"], "Khajuraho temples are in Madhya Pradesh.", "Khajuraho temples Madhya Pradesh में हैं।", ["art-culture", "monuments"]),
  fact("The Ajanta caves are famous mainly for:", "Ajanta caves mainly किसके लिए famous हैं?", "paintings", ["modern glass towers", "rail bridges", "desert forts only"], "Ajanta caves are famous for ancient paintings.", "Ajanta caves ancient paintings के लिए famous हैं।", ["art-culture", "monuments"]),
  fact("The language of the earliest Buddhist texts is mainly:", "Earliest Buddhist texts की main language कौन-सी है?", "Pali", ["Persian", "Tamil only", "English"], "Early Buddhist texts are mainly in Pali.", "Early Buddhist texts mainly Pali में हैं।", ["art-culture", "literature"]),
  fact("The Sangam literature is associated with:", "Sangam literature किस language से associated है?", "Tamil", ["Pali", "Persian", "Urdu only"], "Sangam literature is associated with Tamil.", "Sangam literature Tamil से associated है।", ["art-culture", "literature"]),
  fact("The Ellora caves are located in:", "Ellora caves कहाँ located हैं?", "Maharashtra", ["Bihar", "Kerala", "Punjab"], "Ellora caves are in Maharashtra.", "Ellora caves Maharashtra में हैं।", ["art-culture", "monuments"]),
];

const sportsOrgRows = [
  fact("The headquarters of the United Nations is in:", "United Nations headquarters कहाँ है?", "New York", ["Geneva", "Paris", "Rome"], "UN headquarters is in New York.", "UN headquarters New York में है।", ["static-gk", "world-organizations"]),
  fact("The headquarters of the International Court of Justice is in:", "International Court of Justice headquarters कहाँ है?", "The Hague", ["Geneva", "Paris", "London"], "ICJ is headquartered at The Hague.", "ICJ headquarters The Hague में है।", ["static-gk", "world-organizations"]),
  fact("The headquarters of FAO is in:", "FAO headquarters कहाँ है?", "Rome", ["Geneva", "New York", "Vienna"], "FAO is headquartered in Rome.", "FAO headquarters Rome में है।", ["static-gk", "world-organizations"]),
  fact("The headquarters of IMF is in:", "IMF headquarters कहाँ है?", "Washington DC", ["Paris", "Geneva", "Kathmandu"], "IMF headquarters is in Washington DC.", "IMF headquarters Washington DC में है।", ["static-gk", "world-organizations"]),
  fact("The headquarters of World Bank is in:", "World Bank headquarters कहाँ है?", "Washington DC", ["Geneva", "Rome", "Brussels"], "World Bank headquarters is in Washington DC.", "World Bank headquarters Washington DC में है।", ["static-gk", "world-organizations"]),
  fact("Olympic Games are held every:", "Olympic Games कितने years में होते हैं?", "4 years", ["2 years", "3 years", "5 years"], "Summer Olympics are held every 4 years.", "Summer Olympics 4 years में होते हैं।", ["sports", "static-gk"]),
  fact("The term 'Grand Slam' is associated with:", "Grand Slam term किस sport से associated है?", "tennis", ["hockey", "kabaddi", "chess"], "Grand Slam is a tennis term.", "Grand Slam tennis term है।", ["sports", "static-gk"]),
  fact("Thomas Cup is associated with:", "Thomas Cup किस sport से associated है?", "badminton", ["football", "cricket", "boxing"], "Thomas Cup is associated with badminton.", "Thomas Cup badminton से associated है।", ["sports", "static-gk"]),
  fact("Uber Cup is associated with:", "Uber Cup किस sport से associated है?", "badminton", ["tennis", "hockey", "athletics"], "Uber Cup is associated with badminton.", "Uber Cup badminton से associated है।", ["sports", "static-gk"]),
  fact("Davis Cup is associated with:", "Davis Cup किस sport से associated है?", "tennis", ["cricket", "football", "badminton"], "Davis Cup is associated with tennis.", "Davis Cup tennis से associated है।", ["sports", "static-gk"]),
  fact("The Dronacharya Award is given for:", "Dronacharya Award किसके लिए दिया जाता है?", "coaching in sports", ["literature", "civil service", "film direction"], "Dronacharya Award recognises sports coaching.", "Dronacharya Award sports coaching के लिए है।", ["awards", "sports"]),
  fact("The Arjuna Award is related to:", "Arjuna Award किससे related है?", "sports performance", ["science research", "literature", "public administration"], "Arjuna Award recognises sports performance.", "Arjuna Award sports performance को recognise करता है।", ["awards", "sports"]),
  fact("The Jnanpith Award is given for:", "Jnanpith Award किस field के लिए है?", "literature", ["sports", "science only", "cinema music only"], "Jnanpith is a major literary award.", "Jnanpith major literary award है।", ["awards", "literature"]),
  fact("The Dadasaheb Phalke Award is linked with:", "Dadasaheb Phalke Award किससे linked है?", "cinema", ["sports", "agriculture", "rail safety"], "Dadasaheb Phalke Award is India's highest cinema award.", "Dadasaheb Phalke Award cinema से linked है।", ["awards", "cinema"]),
  fact("The Shanti Swarup Bhatnagar Prize is given for:", "Shanti Swarup Bhatnagar Prize किसके लिए दिया जाता है?", "science and technology", ["sports", "classical dance only", "railway service"], "The prize recognises science and technology work.", "यह prize science और technology work के लिए है।", ["awards", "science-tech"]),
  fact("Hockey uses how many players per side on the field?", "Hockey में field पर each side में कितने players होते हैं?", "11", ["7", "9", "12"], "Field hockey uses 11 players per side.", "Field hockey में each side 11 players होते हैं।", ["sports", "static-gk"]),
  fact("Football uses how many players per side on the field?", "Football में each side कितने players होते हैं?", "11", ["9", "10", "12"], "Football has 11 players per side.", "Football में each side 11 players होते हैं।", ["sports", "static-gk"]),
  fact("Kabaddi has how many players on court per side?", "Kabaddi में court पर each side कितने players होते हैं?", "7", ["5", "9", "11"], "Kabaddi has 7 players per side on court.", "Kabaddi में court पर each side 7 players होते हैं।", ["sports", "static-gk"]),
  fact("The Ranji Trophy is associated with:", "Ranji Trophy किस sport से associated है?", "cricket", ["football", "hockey", "tennis"], "Ranji Trophy is a domestic cricket tournament.", "Ranji Trophy domestic cricket tournament है।", ["sports", "cricket"]),
  fact("The Santosh Trophy is associated with:", "Santosh Trophy किस sport से associated है?", "football", ["cricket", "badminton", "tennis"], "Santosh Trophy is associated with football.", "Santosh Trophy football से associated है।", ["sports", "football"]),
  fact("The Sultan Azlan Shah Cup is associated with:", "Sultan Azlan Shah Cup किस sport से associated है?", "hockey", ["cricket", "tennis", "wrestling"], "Sultan Azlan Shah Cup is a hockey tournament.", "Sultan Azlan Shah Cup hockey tournament है।", ["sports", "hockey"]),
  fact("The Durand Cup is associated with:", "Durand Cup किस sport से associated है?", "football", ["cricket", "hockey", "chess"], "Durand Cup is associated with football.", "Durand Cup football से associated है।", ["sports", "football"]),
  fact("The term 'checkmate' is associated with:", "Checkmate term किस game से associated है?", "chess", ["kabaddi", "hockey", "badminton"], "Checkmate is a chess term.", "Checkmate chess term है।", ["sports", "chess"]),
  fact("The term 'deuce' is used in:", "Deuce term किस sport में used होता है?", "tennis", ["football", "hockey", "cricket"], "Deuce is a tennis scoring term.", "Deuce tennis scoring term है।", ["sports", "tennis"]),
  fact("The term 'LBW' is associated with:", "LBW term किस sport से associated है?", "cricket", ["football", "chess", "badminton"], "LBW is a cricket dismissal term.", "LBW cricket dismissal term है।", ["sports", "cricket"]),
];

function seriesAnalogyRows() {
  const rows = [];
  const series = [
    [3, 7, 13, 21, 31], [5, 11, 19, 29, 41], [2, 6, 12, 20, 30], [4, 9, 16, 25, 36], [10, 20, 35, 55, 80],
    [1, 4, 9, 16, 25], [2, 8, 18, 32, 50], [8, 15, 24, 35, 48], [11, 22, 36, 53, 73], [6, 13, 22, 33, 46],
  ];
  for (const seq of series) {
    const lastDiff = seq.at(-1) - seq.at(-2);
    const prevDiff = seq.at(-2) - seq.at(-3);
    const inc = lastDiff - prevDiff;
    const next = seq.at(-1) + lastDiff + inc;
    rows.push(qrow(
      `Find the next term: ${seq.join(", ")}, ?`,
      `Next term ज्ञात कीजिए: ${seq.join(", ")}, ?`,
      next,
      [
        { value: seq.at(-1) + lastDiff, en: `Repeated the previous difference ${lastDiff} and missed the change in differences.`, hi: `Previous difference ${lastDiff} repeat कर दिया और differences का change miss किया।` },
        { value: seq.at(-1) + prevDiff, en: `Used the older difference ${prevDiff} instead of the next expected difference.`, hi: `Next expected difference की जगह older difference ${prevDiff} use किया।` },
        { value: seq.at(-1) + inc, en: `Added only the increment in differences ${inc}, not the full next difference.`, hi: `सिर्फ difference increment ${inc} add किया, full next difference नहीं।` },
      ],
      `Differences are ${seq.slice(1).map((v, i) => v - seq[i]).join(", ")}; next difference = ${lastDiff + inc}.`,
      `Number series questions are solved by checking first differences, ratios, or square/cube patterns before guessing a term.`,
      `Differences ${seq.slice(1).map((v, i) => v - seq[i]).join(", ")} हैं; next difference = ${lastDiff + inc}।`,
      `Number series में first differences, ratios या square/cube pattern check करके next term चुनते हैं।`,
      ["reasoning", "series"],
    ));
  }
  const pairs = [
    ["Doctor", "Hospital", "Teacher", "School"], ["Pen", "Write", "Knife", "Cut"], ["Bird", "Nest", "Bee", "Hive"],
    ["Author", "Book", "Painter", "Painting"], ["Clock", "Time", "Thermometer", "Temperature"],
    ["Seed", "Plant", "Egg", "Bird"], ["Carpenter", "Wood", "Mason", "Brick"], ["Eye", "See", "Ear", "Hear"],
    ["Oxygen", "Breathing", "Food", "Eating"], ["Library", "Books", "Museum", "Artifacts"],
    ["Compass", "Direction", "Scale", "Length"], ["Farmer", "Field", "Fisherman", "Sea"], ["Pilot", "Aircraft", "Driver", "Bus"],
    ["Tailor", "Cloth", "Cobbler", "Leather"], ["Judge", "Court", "Doctor", "Clinic"],
  ];
  for (const [a, b, c, d] of pairs) {
    rows.push(qrow(
      `${a} is related to ${b} in the same way as ${c} is related to:`,
      `${a}, ${b} से related है उसी तरह ${c} किससे related है?`,
      d,
      [
        { value: b, en: `Copied the first pair's second term instead of applying the same relation to ${c}.`, hi: `First pair का second term copy कर दिया, ${c} पर same relation apply नहीं किया।` },
        { value: a, en: `Picked the first pair's first term and broke the analogy relation.`, hi: `First pair का first term चुन लिया और analogy relation टूट गया।` },
        { value: c, en: `Repeated the given third term instead of finding its related object/action/place.`, hi: `Given third term repeat कर दिया, उसका related object/action/place नहीं निकाला।` },
      ],
      `Identify the relation ${a} -> ${b}; applying the same relation gives ${c} -> ${d}.`,
      `Analogy questions test relation transfer. The word pair relation must remain identical in the second pair.`,
      `${a} -> ${b} relation पहचानें; same relation लगाने पर ${c} -> ${d} मिलता है।`,
      `Analogy questions relation transfer test करते हैं। Second pair में relation same रहना चाहिए।`,
      ["reasoning", "analogy"],
    ));
  }
  return rows.slice(0, 25);
}

function statementRows() {
  const samples = [
    ["All trains are vehicles", "Some vehicles are electric", "Some trains may be electric", "No train is electric", "Only possibility follows"],
    ["All roses are flowers", "Some flowers fade quickly", "Some roses may fade quickly", "All flowers are roses", "Only possibility follows"],
    ["No pens are pencils", "All pencils are stationery", "Some stationery are not pens", "All stationery are pens", "Only I follows"],
    ["All rivers are water bodies", "No water body is a desert", "No river is a desert", "Some deserts are rivers", "Only I follows"],
    ["Some books are novels", "All novels are stories", "Some books are stories", "All stories are books", "Only I follows"],
    ["All doctors are graduates", "Some graduates are teachers", "Some doctors may be teachers", "All teachers are doctors", "Only possibility follows"],
    ["No metals are gases", "Some gases are elements", "Some elements are not metals", "All elements are metals", "Only I follows"],
    ["All squares are rectangles", "All rectangles are quadrilaterals", "All squares are quadrilaterals", "All quadrilaterals are squares", "Only I follows"],
    ["Some apps are tools", "All tools are useful", "Some apps are useful", "All useful things are apps", "Only I follows"],
    ["No birds are mammals", "All bats are mammals", "No bats are birds", "Some bats are birds", "Only I follows"],
  ];
  const rows = [];
  for (const [s1, s2, c1, c2, ans] of samples) {
    rows.push(qrow(
      `Statements: ${s1}. ${s2}. Conclusions: I. ${c1}. II. ${c2}. Which follows?`,
      `Statements: ${s1}. ${s2}. Conclusions: I. ${c1}. II. ${c2}. कौन follow करता है?`,
      ans,
      [
        { value: "Only II follows", en: `Accepted conclusion II even though it reverses or overstates the given class relation.`, hi: `Conclusion II accept कर लिया जबकि यह given class relation को reverse/overstate करता है।` },
        { value: "Both I and II follow", en: `Treated the invalid reversed conclusion as valid along with the valid one.`, hi: `Invalid reversed conclusion को valid conclusion के साथ सही मान लिया।` },
        { value: "Neither follows", en: `Rejected the direct Venn implication from the two statements.`, hi: `दो statements से निकले direct Venn implication को reject कर दिया।` },
      ],
      `Draw the Venn relation from the two statements; ${ans}.`,
      `In statement-conclusion reasoning, do not reverse an all/some relation unless the statement permits it. Possibility conclusions are valid only when not contradicted.`,
      `दो statements से Venn relation बनाइए; ${ans}।`,
      `Statement-conclusion में all/some relation को reverse नहीं करते जब तक statement allow न करे। Possibility conclusion contradiction न हो तभी valid है।`,
      ["reasoning", "statement-conclusion", "venn-diagram"],
    ));
  }
  const venn = [
    ["Students", "Readers", "Singers"], ["Engineers", "Graduates", "Artists"], ["Farmers", "Workers", "Players"],
    ["Teachers", "Employees", "Writers"], ["Drivers", "Adults", "Cyclists"], ["Doctors", "Professionals", "Poets"],
    ["Athletes", "Fit persons", "Musicians"], ["Dancers", "Artists", "Teachers"], ["Coders", "Employees", "Gamers"],
    ["Voters", "Adults", "Taxpayers"], ["Parents", "Adults", "Doctors"], ["Painters", "Artists", "Students"],
    ["Swimmers", "Sportspersons", "Readers"], ["Pilots", "Employees", "Authors"], ["Nurses", "Professionals", "Singers"],
  ];
  for (const [a, b, c] of venn) {
    rows.push(qrow(
      `In a Venn diagram, ${a} is fully inside ${b}, and ${c} partly overlaps ${b}. Which statement is definitely true?`,
      `Venn diagram में ${a}, ${b} के अंदर है और ${c}, ${b} को partly overlap करता है। Definitely true statement कौन-सा है?`,
      `All ${a} are ${b}`,
      [
        { value: `All ${c} are ${b}`, en: `Converted a partial overlap between ${c} and ${b} into full inclusion.`, hi: `${c} और ${b} के partial overlap को full inclusion मान लिया।` },
        { value: `No ${a} are ${c}`, en: `Assumed separation between ${a} and ${c}; the diagram does not guarantee that.`, hi: `${a} और ${c} को separate मान लिया; diagram यह guarantee नहीं करता।` },
        { value: `All ${b} are ${a}`, en: `Reversed the inclusion. ${a} inside ${b} means all ${a} are ${b}, not all ${b} are ${a}.`, hi: `Inclusion reverse कर दिया। ${a}, ${b} के अंदर है मतलब all ${a} are ${b}, all ${b} are ${a} नहीं।` },
      ],
      `${a} lies completely within ${b}, so every ${a} is ${b}.`,
      `A Venn diagram only gives guaranteed relations shown by full containment, separation, or overlap. Do not infer more than the diagram states.`,
      `${a} पूरी तरह ${b} के अंदर है, इसलिए every ${a}, ${b} है।`,
      `Venn diagram सिर्फ वही guaranteed relation देता है जो full containment, separation या overlap से दिखता है। Extra inference न करें।`,
      ["reasoning", "venn-diagram"],
    ));
  }
  return rows.slice(0, 25);
}

function operationsDataRows() {
  const rows = [];
  for (const [a, b, c] of [[4, 5, 3], [6, 2, 7], [8, 3, 5], [9, 4, 2], [7, 6, 3], [5, 8, 4], [12, 3, 6], [10, 7, 2], [11, 2, 9], [13, 5, 4], [14, 6, 3], [15, 2, 5], [16, 4, 7]]) {
    const ans = a * b + c;
    rows.push(qrow(
      `If A # B means A x B + ${c}, find ${a} # ${b}.`,
      `यदि A # B का अर्थ A x B + ${c} है, तो ${a} # ${b} ज्ञात कीजिए।`,
      ans,
      [
        { value: a + b + c, en: `Added A and B first: ${a}+${b}+${c}; # requires multiplication before adding ${c}.`, hi: `A और B add कर दिए: ${a}+${b}+${c}; # में पहले multiplication फिर ${c} add होता है।` },
        { value: a * (b + c), en: `Added ${c} to B before multiplication: ${a} x (${b}+${c}).`, hi: `${c} को B में add करके multiplication किया: ${a} x (${b}+${c})।` },
        { value: a * b - c, en: `Subtracted ${c} after multiplication instead of adding it.`, hi: `Multiplication के बाद ${c} add करने की जगह subtract कर दिया।` },
      ],
      `${a} # ${b} = ${a} x ${b} + ${c} = ${ans}.`,
      `For coded mathematical operations, replace the symbol with its defined operation exactly and follow the stated order.`,
      `${a} # ${b} = ${a} x ${b} + ${c} = ${ans}।`,
      `Coded mathematical operations में symbol को exact defined operation से replace करें और stated order follow करें।`,
      ["reasoning", "mathematical-operations"],
    ));
  }
  const ds = [
    [12, 8], [15, 9], [20, 13], [18, 11], [25, 17], [30, 19], [24, 14], [16, 7], [28, 21], [35, 26], [40, 31], [22, 15],
  ];
  for (const [sum, x] of ds) {
    const y = sum - x;
    rows.push(qrow(
      `Data Sufficiency: Find y. Statement I: x + y = ${sum}. Statement II: x = ${x}.`,
      `Data Sufficiency: y ज्ञात करें। Statement I: x + y = ${sum}. Statement II: x = ${x}.`,
      "Both statements together are sufficient",
      [
        { value: "Statement I alone is sufficient", en: `Statement I gives only x+y=${sum}; y cannot be fixed without x.`, hi: `Statement I सिर्फ x+y=${sum} देता है; x के बिना y fixed नहीं होता।` },
        { value: "Statement II alone is sufficient", en: `Statement II gives x=${x} but says nothing about y by itself.`, hi: `Statement II x=${x} देता है पर y के बारे में अकेले कुछ नहीं बताता।` },
        { value: "Neither statement is sufficient", en: `Missed that combining x+y=${sum} with x=${x} gives y=${y}.`, hi: `यह miss किया कि x+y=${sum} और x=${x} combine करने पर y=${y} मिलता है।` },
      ],
      `Together: y = ${sum} - ${x} = ${y}.`,
      `Data sufficiency asks whether the data can uniquely answer the question, not whether the final answer is immediately written in one statement.`,
      `Together: y = ${sum} - ${x} = ${y}।`,
      `Data sufficiency में पूछा जाता है कि data uniquely answer दे सकता है या नहीं, final answer statement में directly लिखा है या नहीं।`,
      ["reasoning", "data-sufficiency"],
    ));
  }
  return rows.slice(0, 25);
}

function rankingClockRows() {
  const rows = [];
  for (const [n, left] of [[40, 12], [55, 18], [63, 21], [72, 25], [50, 9], [48, 16], [81, 30], [36, 11], [90, 42], [67, 28], [75, 34], [44, 13]]) {
    const right = n - left + 1;
    rows.push(qrow(
      `In a row of ${n} candidates, Ravi is ${left}th from the left. What is his rank from the right?`,
      `${n} candidates की row में Ravi left से ${left}th है। Right से उसका rank क्या है?`,
      right,
      [
        { value: n - left, en: `Forgot to add 1 in opposite rank: right rank = total - left rank + 1.`, hi: `Opposite rank में +1 भूल गए: right rank = total - left rank + 1।` },
        { value: left, en: `Copied the left rank as right rank without converting sides.`, hi: `Left rank को right rank में convert किए बिना copy कर दिया।` },
        { value: n + left, en: `Added total and rank, which gives a count beyond the row size.`, hi: `Total और rank add कर दिए, जिससे row size से बाहर count आता है।` },
      ],
      `Right rank = ${n} - ${left} + 1 = ${right}.`,
      `For rank from the opposite side, the person's position is counted in both directions, so add 1 after subtracting from total.`,
      `Right rank = ${n} - ${left} + 1 = ${right}।`,
      `Opposite-side rank में person दोनों sides की counting में शामिल होता है, इसलिए total से subtract करके +1 करते हैं।`,
      ["reasoning", "ranking"],
    ));
  }
  for (const [h, m] of [[3, 10], [6, 20], [2, 30], [4, 30], [5, 20], [7, 20], [8, 40], [10, 10], [9, 15], [1, 50], [11, 5], [12, 25], [3, 40]]) {
    const hourAngle = (h % 12) * 30 + m * 0.5;
    const minuteAngle = m * 6;
    let ans = Math.abs(hourAngle - minuteAngle);
    if (ans > 180) ans = 360 - ans;
    const answerValue = `${fmt(ans)}°`;
    const trapCandidates = [
      { value: `${fmt(Math.abs((h % 12) * 30 - minuteAngle))}°`, en: `Ignored the hour hand's ${m} minutes movement after the hour.`, hi: `Hour hand की ${m} minutes वाली movement ignore कर दी।` },
      { value: `${fmt(360 - ans)}°`, en: `Used the larger reflex angle instead of the smaller angle.`, hi: `Smaller angle की जगह larger reflex angle ले लिया।` },
      { value: `${fmt(minuteAngle)}°`, en: `Reported the minute hand's angle from 12, not the angle between both hands.`, hi: `Minute hand का 12 से angle दे दिया, दोनों hands के बीच angle नहीं।` },
      { value: `${fmt(hourAngle)}°`, en: `Reported the hour hand's angle from 12, not the angle between both hands.`, hi: `Hour hand का 12 से angle दे दिया, दोनों hands के बीच angle नहीं।` },
      { value: `${fmt((ans + 30) % 360)}°`, en: `Shifted by one hour mark after finding the hand difference.`, hi: `Hand difference निकालने के बाद one-hour mark shift कर दिया।` },
    ];
    const usedTrapValues = new Set([answerValue]);
    const traps = [];
    for (const candidate of trapCandidates) {
      if (!usedTrapValues.has(candidate.value)) {
        traps.push(candidate);
        usedTrapValues.add(candidate.value);
      }
      if (traps.length === 3) break;
    }
    if (traps.length !== 3) throw new Error(`clock row ${h}:${m} did not produce 3 traps`);
    rows.push(qrow(
      `Find the smaller angle between clock hands at ${h}:${String(m).padStart(2, "0")}.`,
      `${h}:${String(m).padStart(2, "0")} पर clock hands के बीच smaller angle ज्ञात करें।`,
      answerValue,
      traps,
      `Hour hand = ${fmt(hourAngle)}°, minute hand = ${fmt(minuteAngle)}°, smaller difference = ${fmt(ans)}°.`,
      `Clock-angle questions require both hands' positions. The hour hand moves 0.5° per minute, so it is not fixed at the hour mark after minutes pass.`,
      `Hour hand = ${fmt(hourAngle)}°, minute hand = ${fmt(minuteAngle)}°, smaller difference = ${fmt(ans)}°।`,
      `Clock-angle में दोनों hands की position चाहिए। Hour hand 0.5° per minute चलता है, इसलिए minutes pass होने पर hour mark पर fixed नहीं रहता।`,
      ["reasoning", "clock"],
    ));
  }
  return rows.slice(0, 25);
}

function puzzleMapRows() {
  const rows = [];
  const groups = [
    ["A", "B", "C", "D", "E"], ["P", "Q", "R", "S", "T"], ["L", "M", "N", "O", "P"], ["V", "W", "X", "Y", "Z"],
    ["Ravi", "Sita", "Mohan", "Geeta", "Kabir"], ["Anu", "Bina", "Chirag", "Dev", "Esha"], ["Farah", "Gopal", "Hina", "Irfan", "Jaya"],
    ["K", "L", "M", "N", "O"], ["U", "V", "W", "X", "Y"], ["Amit", "Bala", "Charu", "Deep", "Eina"],
  ];
  for (const g of groups) {
    const order = [g[1], g[3], g[0], g[4], g[2]];
    rows.push(qrow(
      `${order[2]} sits in the middle. ${order[0]} is at the left end. ${order[1]} sits between ${order[0]} and ${order[2]}. ${order[3]} sits to the right of ${order[2]}. Who sits at the right end?`,
      `${order[2]} middle में बैठा है। ${order[0]} left end पर है। ${order[1]}, ${order[0]} और ${order[2]} के बीच है। ${order[3]}, ${order[2]} के right में है। Right end पर कौन है?`,
      order[4],
      [
        { value: order[3], en: `Stopped at the person immediately right of the middle, not the right end.`, hi: `Middle के immediate right वाले person पर रुक गए, right end नहीं देखा।` },
        { value: order[0], en: `Picked the left end even though the question asks for the right end.`, hi: `Question right end पूछता है लेकिन left end चुन लिया।` },
        { value: order[2], en: `Picked the middle person instead of completing the row.`, hi: `Row complete करने की जगह middle person चुन लिया।` },
      ],
      `Final order is ${order.join(" - ")}; right end is ${order[4]}.`,
      `Linear puzzle clues should be converted into a final row before answering. End positions and immediate-neighbour clues are common traps.`,
      `Final order ${order.join(" - ")} है; right end ${order[4]} है।`,
      `Linear puzzle clues को final row में convert करें। End positions और immediate-neighbour clues common traps हैं।`,
      ["reasoning", "puzzle", "seating-arrangement"],
    ));
  }
  const paths = [
    [[6, 0], [0, 8]], [[8, 0], [0, 15]], [[5, 0], [0, 12]], [[12, 0], [0, 16]], [[9, 0], [0, 12]],
    [[7, 0], [0, 24]], [[15, 0], [0, 20]], [[10, 0], [0, 24]], [[20, 0], [0, 21]], [[16, 0], [0, 30]],
    [[24, 0], [0, 32]], [[18, 0], [0, 24]], [[21, 0], [0, 28]], [[30, 0], [0, 40]], [[12, 0], [0, 35]],
  ];
  for (const p of paths) {
    const east = p[0][0], north = p[1][1];
    const dist = Math.sqrt(east * east + north * north);
    rows.push(qrow(
      `A map shows a point ${east} km east and ${north} km north from the start. What is the shortest distance from the start?`,
      `Map में एक point start से ${east} km east और ${north} km north है। Start से shortest distance क्या है?`,
      `${fmt(dist)} km`,
      [
        { value: `${east + north} km`, en: `Added path components and reported route length, not straight-line distance.`, hi: `Components add करके route length दे दी, straight-line distance नहीं।` },
        { value: `${east} km`, en: `Used only east displacement and ignored north displacement.`, hi: `सिर्फ east displacement लिया, north ignore किया।` },
        { value: `${north} km`, en: `Used only north displacement and ignored east displacement.`, hi: `सिर्फ north displacement लिया, east ignore किया।` },
      ],
      `Shortest distance = sqrt(${east}^2 + ${north}^2) = ${fmt(dist)} km.`,
      `Map distance between perpendicular directions is a Pythagoras problem. The direct distance is the hypotenuse, not the sum of the legs.`,
      `Shortest distance = sqrt(${east}^2 + ${north}^2) = ${fmt(dist)} km।`,
      `Perpendicular directions का map distance Pythagoras problem है। Direct distance hypotenuse होता है, legs का sum नहीं।`,
      ["reasoning", "maps", "direction-sense"],
    ));
  }
  return rows.slice(0, 25);
}

const specs = [
  ["sectional-current-affairs-science-tech-01", "Current Affairs & Science Tech", "Current Affairs & Science Tech", "ga", "gacurr", currentRows],
  ["sectional-art-culture-literature-01", "Art, Culture & Literature", "Art, Culture & Literature", "ga", "gaculture", cultureRows],
  ["sectional-sports-awards-organizations-01", "Sports, Awards & Organizations", "Sports, Awards & Organizations", "ga", "gasports", sportsOrgRows],
  ["sectional-reasoning-series-analogy-01", "Series & Analogy", "Series & Analogy", "reason", "rseries", seriesAnalogyRows()],
  ["sectional-reasoning-venn-statements-01", "Venn & Statement Conclusions", "Venn & Statement Conclusions", "reason", "rvenn", statementRows()],
  ["sectional-reasoning-operations-data-sufficiency-01", "Operations & Data Sufficiency", "Operations & Data Sufficiency", "reason", "rops", operationsDataRows()],
  ["sectional-reasoning-ranking-clocks-01", "Ranking & Clocks", "Ranking & Clocks", "reason", "rrank", rankingClockRows()],
  ["sectional-reasoning-puzzles-maps-01", "Puzzles & Maps", "Puzzles & Maps", "reason", "rmap", puzzleMapRows()],
];

await mkdir(OUT, { recursive: true });
for (const [slug, titleEn, titleHi, subject, prefix, rows] of specs) {
  const b = bundle(slug, titleEn, titleHi, subject, prefix, rows);
  await writeFile(resolve(OUT, `${slug}.json`), JSON.stringify(b, null, 2) + "\n", "utf8");
  console.log(`${slug}: ${b.questions.length} questions`);
}
