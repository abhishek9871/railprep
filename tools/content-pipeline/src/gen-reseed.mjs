#!/usr/bin/env node
// Phase 2.5 comprehensive reseed — built from verified NCERT page-1 content.
// Each book's real identity was confirmed by extracting first-page text (see
// docs/PHASE_25_REPORT.md audit). This script wires every NCERT chapter we have
// to the catalog chapter that matches its real content, not its filename guess.

import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '..');

// Catalog chapter IDs (stable since Phase 2 schema).
const ch = {
  math_number: '24553445-02f2-4ac1-9bc9-2d2cf4b4ac39',
  math_pnl:    'e1749650-7e7d-4af5-b42e-f65c56da507d',
  math_geom:   'c639a5ff-f6c3-4181-9806-f4a7aed4e2d1',
  ga_polity:   '89201ed8-408a-4209-8484-747a6b41a768',
  ga_history:  'df5e8579-bbce-4487-9a11-6589fae2b233',
  ga_economy:  '71f49244-c588-46f8-948d-d24339206242',
  // ga_geography will be created by the migration (new row). The migration
  // inserts it with a fixed UUID so inserts here can reference it directly.
  ga_geography:'b2a4f8c1-0000-4000-8000-000000000001',
  sci_physics: '29ceae82-b170-46fe-bf7d-e5e97710f184',
  sci_chem:    'ef0c85b2-9bf3-48f9-af33-3b9a0089aba1',
  sci_bio:     '0ef3d9a6-97ed-49f2-b385-0d4ed0aaf8a5',
  eng_grammar: 'a1779db8-40d7-4ff7-b3ad-9848f9472e2d',
  eng_vocab:   '3282ba47-949a-4b61-9095-38320ed30e12',
  eng_compr:   'aeb2635a-97af-40af-be02-254fecfb12c8',
};

// Verified NCERT chapter titles — sourced from NCERT TOCs for each book.
// Classes whose books have been renamed in 2024-25 (iemh = Ganita Manjari,
// hemh = Ganita Prakash) get generic "Class X Math — Chapter N" titles because
// the in-book chapter names differ from older editions and aren't stable yet.
//
// Shape: [fname, catalog_chapter_id, title_en, title_hi]
const seed = [
  // ========== Math — Number System & Simplification ==========
  ['iemh101.pdf', ch.math_number, 'Class 9 Mathematics — Chapter 1 (NCERT)',                                   'कक्षा 9 गणित — अध्याय 1 (NCERT)'],
  ['iemh102.pdf', ch.math_number, 'Class 9 Mathematics — Chapter 2 (NCERT)',                                   'कक्षा 9 गणित — अध्याय 2 (NCERT)'],
  ['jemh101.pdf', ch.math_number, 'Real Numbers (NCERT Class 10 Ch 1)',                                        'वास्तविक संख्याएँ (NCERT कक्षा 10 अध्याय 1)'],
  ['jemh102.pdf', ch.math_number, 'Polynomials (NCERT Class 10 Ch 2)',                                         'बहुपद (NCERT कक्षा 10 अध्याय 2)'],
  ['hemh101.pdf', ch.math_number, 'Rational Numbers (NCERT Class 8 Ch 1)',                                     'परिमेय संख्याएँ (NCERT कक्षा 8 अध्याय 1)'],
  ['hemh102.pdf', ch.math_number, 'Linear Equations in One Variable (NCERT Class 8 Ch 2)',                     'एक चर में रैखिक समीकरण (NCERT कक्षा 8 अध्याय 2)'],

  // ========== Math — Percentage, Profit & Loss ==========
  ['hemh107.pdf', ch.math_pnl,    'Comparing Quantities (NCERT Class 8 Ch 7)',                                 'राशियों की तुलना (NCERT कक्षा 8 अध्याय 7)'],
  ['hemh108.pdf', ch.math_pnl,    'Algebraic Expressions (NCERT Class 8 Ch 8)',                                'बीजीय व्यंजक (NCERT कक्षा 8 अध्याय 8)'],
  ['jemh103.pdf', ch.math_pnl,    'Pair of Linear Equations in Two Variables (NCERT Class 10 Ch 3)',           'दो चर वाले रैखिक समीकरण युग्म (NCERT कक्षा 10 अध्याय 3)'],
  ['jemh104.pdf', ch.math_pnl,    'Quadratic Equations (NCERT Class 10 Ch 4)',                                 'द्विघात समीकरण (NCERT कक्षा 10 अध्याय 4)'],
  ['jemh105.pdf', ch.math_pnl,    'Arithmetic Progressions (NCERT Class 10 Ch 5)',                             'समांतर श्रेढ़ी (NCERT कक्षा 10 अध्याय 5)'],

  // ========== Math — Geometry & Mensuration ==========
  ['iemh107.pdf', ch.math_geom,   'Class 9 Mathematics — Chapter 7 (NCERT)',                                   'कक्षा 9 गणित — अध्याय 7 (NCERT)'],
  ['iemh108.pdf', ch.math_geom,   'Class 9 Mathematics — Chapter 8 (NCERT)',                                   'कक्षा 9 गणित — अध्याय 8 (NCERT)'],
  ['jemh106.pdf', ch.math_geom,   'Triangles (NCERT Class 10 Ch 6)',                                           'त्रिभुज (NCERT कक्षा 10 अध्याय 6)'],
  ['jemh107.pdf', ch.math_geom,   'Coordinate Geometry (NCERT Class 10 Ch 7)',                                 'निर्देशांक ज्यामिति (NCERT कक्षा 10 अध्याय 7)'],
  ['jemh108.pdf', ch.math_geom,   'Introduction to Trigonometry (NCERT Class 10 Ch 8)',                        'त्रिकोणमिति का परिचय (NCERT कक्षा 10 अध्याय 8)'],
  ['jemh109.pdf', ch.math_geom,   'Some Applications of Trigonometry (NCERT Class 10 Ch 9)',                   'त्रिकोणमिति के कुछ अनुप्रयोग (NCERT कक्षा 10 अध्याय 9)'],
  ['jemh110.pdf', ch.math_geom,   'Circles (NCERT Class 10 Ch 10)',                                            'वृत्त (NCERT कक्षा 10 अध्याय 10)'],
  ['jemh111.pdf', ch.math_geom,   'Areas Related to Circles (NCERT Class 10 Ch 11)',                           'वृत्तों से सम्बंधित क्षेत्रफल (NCERT कक्षा 10 अध्याय 11)'],
  ['jemh112.pdf', ch.math_geom,   'Surface Areas and Volumes (NCERT Class 10 Ch 12)',                          'पृष्ठीय क्षेत्रफल एवं आयतन (NCERT कक्षा 10 अध्याय 12)'],

  // ========== GA — Indian Polity ==========
  // Class 9 Democratic Politics I (iess4 — verified content: "What is democracy? What are its features?")
  ['iess401.pdf', ch.ga_polity,   'What is Democracy? Why Democracy? (NCERT Class 9 Polity Ch 1)',             'लोकतंत्र क्या है? लोकतंत्र क्यों? (NCERT कक्षा 9 राजनीति अध्याय 1)'],
  ['iess402.pdf', ch.ga_polity,   'Constitutional Design (NCERT Class 9 Polity Ch 2)',                         'संविधान निर्माण (NCERT कक्षा 9 राजनीति अध्याय 2)'],
  ['iess403.pdf', ch.ga_polity,   'Electoral Politics (NCERT Class 9 Polity Ch 3)',                            'चुनावी राजनीति (NCERT कक्षा 9 राजनीति अध्याय 3)'],
  ['iess404.pdf', ch.ga_polity,   'Working of Institutions (NCERT Class 9 Polity Ch 4)',                       'संस्थाओं का कामकाज (NCERT कक्षा 9 राजनीति अध्याय 4)'],
  ['iess405.pdf', ch.ga_polity,   'Democratic Rights (NCERT Class 9 Polity Ch 5)',                             'लोकतांत्रिक अधिकार (NCERT कक्षा 9 राजनीति अध्याय 5)'],
  // Class 10 Democratic Politics II (jess4 — verified content: "Power-sharing")
  ['jess401.pdf', ch.ga_polity,   'Power Sharing (NCERT Class 10 Polity Ch 1)',                                'सत्ता की साझेदारी (NCERT कक्षा 10 राजनीति अध्याय 1)'],
  ['jess402.pdf', ch.ga_polity,   'Federalism (NCERT Class 10 Polity Ch 2)',                                   'संघवाद (NCERT कक्षा 10 राजनीति अध्याय 2)'],
  ['jess403.pdf', ch.ga_polity,   'Gender, Religion and Caste (NCERT Class 10 Polity Ch 3)',                   'जाति, धर्म और लिंग (NCERT कक्षा 10 राजनीति अध्याय 3)'],
  ['jess404.pdf', ch.ga_polity,   'Political Parties (NCERT Class 10 Polity Ch 4)',                            'राजनीतिक दल (NCERT कक्षा 10 राजनीति अध्याय 4)'],
  ['jess405.pdf', ch.ga_polity,   'Outcomes of Democracy (NCERT Class 10 Polity Ch 5)',                        'लोकतंत्र के परिणाम (NCERT कक्षा 10 राजनीति अध्याय 5)'],
  // Class 8 Social and Political Life III (hess3)
  ['hess301.pdf', ch.ga_polity,   'The Indian Constitution (NCERT Class 8 SPL III Ch 1)',                      'भारतीय संविधान (NCERT कक्षा 8 अध्याय 1)'],
  ['hess302.pdf', ch.ga_polity,   'Understanding Secularism (NCERT Class 8 SPL III Ch 2)',                     'धर्मनिरपेक्षता की समझ (NCERT कक्षा 8 अध्याय 2)'],
  ['hess303.pdf', ch.ga_polity,   'Parliament and the Making of Laws (NCERT Class 8 SPL III Ch 3)',            'संसद एवं क़ानून बनना (NCERT कक्षा 8 अध्याय 3)'],
  ['hess304.pdf', ch.ga_polity,   'Judiciary (NCERT Class 8 SPL III Ch 4)',                                    'न्यायपालिका (NCERT कक्षा 8 अध्याय 4)'],
  ['hess305.pdf', ch.ga_polity,   'Understanding Our Criminal Justice System (NCERT Class 8 SPL III Ch 5)',    'हमारी आपराधिक न्याय प्रणाली (NCERT कक्षा 8 अध्याय 5)'],

  // ========== GA — History ==========
  // Class 9 India and the Contemporary World I (iess3)
  ['iess301.pdf', ch.ga_history,  'The French Revolution (NCERT Class 9 History Ch 1)',                        'फ्रांसीसी क्रांति (NCERT कक्षा 9 इतिहास अध्याय 1)'],
  ['iess302.pdf', ch.ga_history,  'Socialism in Europe and the Russian Revolution (NCERT Class 9 History Ch 2)','यूरोप में समाजवाद एवं रूसी क्रांति (NCERT कक्षा 9 इतिहास अध्याय 2)'],
  ['iess303.pdf', ch.ga_history,  'Nazism and the Rise of Hitler (NCERT Class 9 History Ch 3)',                'नात्सीवाद और हिटलर का उदय (NCERT कक्षा 9 इतिहास अध्याय 3)'],
  ['iess304.pdf', ch.ga_history,  'Forest Society and Colonialism (NCERT Class 9 History Ch 4)',               'वन्य समाज एवं उपनिवेशवाद (NCERT कक्षा 9 इतिहास अध्याय 4)'],
  ['iess305.pdf', ch.ga_history,  'Pastoralists in the Modern World (NCERT Class 9 History Ch 5)',             'आधुनिक विश्व में चरवाहे (NCERT कक्षा 9 इतिहास अध्याय 5)'],
  // Class 10 India and the Contemporary World II (jess3)
  ['jess301.pdf', ch.ga_history,  'The Rise of Nationalism in Europe (NCERT Class 10 History Ch 1)',           'यूरोप में राष्ट्रवाद का उदय (NCERT कक्षा 10 इतिहास अध्याय 1)'],
  ['jess302.pdf', ch.ga_history,  'Nationalism in India (NCERT Class 10 History Ch 2)',                        'भारत में राष्ट्रवाद (NCERT कक्षा 10 इतिहास अध्याय 2)'],
  ['jess303.pdf', ch.ga_history,  'The Making of a Global World (NCERT Class 10 History Ch 3)',                'भूमंडलीकृत विश्व का बनना (NCERT कक्षा 10 इतिहास अध्याय 3)'],
  ['jess304.pdf', ch.ga_history,  'The Age of Industrialisation (NCERT Class 10 History Ch 4)',                'औद्योगीकरण का युग (NCERT कक्षा 10 इतिहास अध्याय 4)'],
  ['jess305.pdf', ch.ga_history,  'Print Culture and the Modern World (NCERT Class 10 History Ch 5)',          'मुद्रण संस्कृति और आधुनिक विश्व (NCERT कक्षा 10 इतिहास अध्याय 5)'],
  // Class 8 Our Pasts III (hess2 — colonial Indian history)
  ['hess201.pdf', ch.ga_history,  'How, When and Where (NCERT Class 8 History Ch 1)',                          'कैसे, कब और कहाँ (NCERT कक्षा 8 इतिहास अध्याय 1)'],
  ['hess202.pdf', ch.ga_history,  'From Trade to Territory: The Company Establishes Power (NCERT Class 8 Ch 2)','व्यापार से साम्राज्य तक (NCERT कक्षा 8 अध्याय 2)'],
  ['hess203.pdf', ch.ga_history,  'Ruling the Countryside (NCERT Class 8 History Ch 3)',                       'ग्रामीण क्षेत्र पर शासन (NCERT कक्षा 8 इतिहास अध्याय 3)'],
  ['hess204.pdf', ch.ga_history,  'Tribals, Dikus and the Vision of a Golden Age (NCERT Class 8 Ch 4)',        'आदिवासी, दीकू और एक स्वर्ण युग की कल्पना (NCERT कक्षा 8 अध्याय 4)'],
  ['hess205.pdf', ch.ga_history,  'When People Rebel — 1857 and After (NCERT Class 8 History Ch 5)',           'जब जनता बग़ावत करती है — 1857 के बाद (NCERT कक्षा 8 अध्याय 5)'],
  // Class 11 Themes in World History (kehs — kept, explicitly labeled World History)
  ['kehs101.pdf', ch.ga_history,  'Writing and City Life — Mesopotamia (NCERT Class 11 World History Ch 1)',   'लेखन और शहरी जीवन — मेसोपोटामिया (NCERT कक्षा 11 विश्व इतिहास अध्याय 1)'],
  ['kehs102.pdf', ch.ga_history,  'An Empire Across Three Continents — Rome (NCERT Class 11 World History Ch 2)', 'तीन महाद्वीपों में फैला साम्राज्य — रोम (NCERT कक्षा 11 विश्व इतिहास अध्याय 2)'],
  ['kehs103.pdf', ch.ga_history,  'Nomadic Empires — The Mongols (NCERT Class 11 World History Ch 3)',         'खानाबदोश साम्राज्य — मंगोल (NCERT कक्षा 11 विश्व इतिहास अध्याय 3)'],
  ['kehs104.pdf', ch.ga_history,  'The Three Orders — European Feudalism (NCERT Class 11 World History Ch 4)', 'तीन वर्ग — यूरोपीय सामंतवाद (NCERT कक्षा 11 विश्व इतिहास अध्याय 4)'],
  ['kehs105.pdf', ch.ga_history,  'Changing Cultural Traditions — The Renaissance (NCERT Class 11 World History Ch 5)', 'बदलती सांस्कृतिक परंपराएँ — पुनर्जागरण (NCERT कक्षा 11 विश्व इतिहास अध्याय 5)'],

  // ========== GA — Indian Economy ==========
  // Class 9 Economics (iess2 — verified: "The Story of Village Palampur")
  ['iess201.pdf', ch.ga_economy,  'The Story of Village Palampur (NCERT Class 9 Economics Ch 1)',              'पालमपुर गाँव की कहानी (NCERT कक्षा 9 अर्थशास्त्र अध्याय 1)'],
  ['iess202.pdf', ch.ga_economy,  'People as Resource (NCERT Class 9 Economics Ch 2)',                         'संसाधन के रूप में लोग (NCERT कक्षा 9 अर्थशास्त्र अध्याय 2)'],
  ['iess203.pdf', ch.ga_economy,  'Poverty as a Challenge (NCERT Class 9 Economics Ch 3)',                     'ग़रीबी एक चुनौती (NCERT कक्षा 9 अर्थशास्त्र अध्याय 3)'],
  ['iess204.pdf', ch.ga_economy,  'Food Security in India (NCERT Class 9 Economics Ch 4)',                     'भारत में खाद्य सुरक्षा (NCERT कक्षा 9 अर्थशास्त्र अध्याय 4)'],
  // Class 10 Understanding Economic Development (jess2)
  ['jess201.pdf', ch.ga_economy,  'Development (NCERT Class 10 Economics Ch 1)',                               'विकास (NCERT कक्षा 10 अर्थशास्त्र अध्याय 1)'],
  ['jess202.pdf', ch.ga_economy,  'Sectors of the Indian Economy (NCERT Class 10 Economics Ch 2)',             'भारतीय अर्थव्यवस्था के क्षेत्रक (NCERT कक्षा 10 अर्थशास्त्र अध्याय 2)'],
  ['jess203.pdf', ch.ga_economy,  'Money and Credit (NCERT Class 10 Economics Ch 3)',                          'मुद्रा और साख (NCERT कक्षा 10 अर्थशास्त्र अध्याय 3)'],
  ['jess204.pdf', ch.ga_economy,  'Globalisation and the Indian Economy (NCERT Class 10 Economics Ch 4)',      'वैश्वीकरण और भारतीय अर्थव्यवस्था (NCERT कक्षा 10 अर्थशास्त्र अध्याय 4)'],
  ['jess205.pdf', ch.ga_economy,  'Consumer Rights (NCERT Class 10 Economics Ch 5)',                           'उपभोक्ता अधिकार (NCERT कक्षा 10 अर्थशास्त्र अध्याय 5)'],
  // Class 11 Indian Economic Development (keec)
  ['keec101.pdf', ch.ga_economy,  'Indian Economy on the Eve of Independence (NCERT Class 11 Ch 1)',           'स्वतंत्रता की पूर्व संध्या पर भारतीय अर्थव्यवस्था (NCERT कक्षा 11 अध्याय 1)'],
  ['keec102.pdf', ch.ga_economy,  'Indian Economy 1950-1990 (NCERT Class 11 Ch 2)',                            'भारतीय अर्थव्यवस्था 1950-1990 (NCERT कक्षा 11 अध्याय 2)'],
  ['keec103.pdf', ch.ga_economy,  'Liberalisation, Privatisation and Globalisation (NCERT Class 11 Ch 3)',     'उदारीकरण, निजीकरण और वैश्वीकरण (NCERT कक्षा 11 अध्याय 3)'],
  ['keec104.pdf', ch.ga_economy,  'Poverty (NCERT Class 11 Economics Ch 4)',                                   'निर्धनता (NCERT कक्षा 11 अर्थशास्त्र अध्याय 4)'],
  ['keec105.pdf', ch.ga_economy,  'Human Capital Formation in India (NCERT Class 11 Ch 5)',                    'मानव पूंजी निर्माण (NCERT कक्षा 11 अध्याय 5)'],
  ['keec106.pdf', ch.ga_economy,  'Rural Development (NCERT Class 11 Economics Ch 6)',                         'ग्रामीण विकास (NCERT कक्षा 11 अर्थशास्त्र अध्याय 6)'],
  ['keec107.pdf', ch.ga_economy,  'Employment — Growth, Informalisation (NCERT Class 11 Ch 7)',                'रोज़गार (NCERT कक्षा 11 अध्याय 7)'],
  ['keec108.pdf', ch.ga_economy,  'Infrastructure (NCERT Class 11 Economics Ch 8)',                            'अवसंरचना (NCERT कक्षा 11 अध्याय 8)'],

  // ========== GA — Indian Geography (NEW chapter) ==========
  // Class 9 Contemporary India I (iess1 — verified: "Tropic of Cancer divides India...")
  ['iess101.pdf', ch.ga_geography,'India — Size and Location (NCERT Class 9 Geography Ch 1)',                  'भारत — आकार और स्थिति (NCERT कक्षा 9 भूगोल अध्याय 1)'],
  ['iess102.pdf', ch.ga_geography,'Physical Features of India (NCERT Class 9 Geography Ch 2)',                 'भारत की भौतिक आकृतियाँ (NCERT कक्षा 9 भूगोल अध्याय 2)'],
  ['iess103.pdf', ch.ga_geography,'Drainage — Rivers of India (NCERT Class 9 Geography Ch 3)',                 'अपवाह — भारत की नदियाँ (NCERT कक्षा 9 भूगोल अध्याय 3)'],
  ['iess104.pdf', ch.ga_geography,'Climate (NCERT Class 9 Geography Ch 4)',                                    'जलवायु (NCERT कक्षा 9 भूगोल अध्याय 4)'],
  ['iess105.pdf', ch.ga_geography,'Natural Vegetation and Wildlife (NCERT Class 9 Geography Ch 5)',            'प्राकृतिक वनस्पति और वन्य जीवन (NCERT कक्षा 9 भूगोल अध्याय 5)'],
  ['iess106.pdf', ch.ga_geography,'Population (NCERT Class 9 Geography Ch 6)',                                 'जनसंख्या (NCERT कक्षा 9 भूगोल अध्याय 6)'],
  // Class 10 Contemporary India II (jess1)
  ['jess101.pdf', ch.ga_geography,'Resources and Development (NCERT Class 10 Geography Ch 1)',                 'संसाधन एवं विकास (NCERT कक्षा 10 भूगोल अध्याय 1)'],
  ['jess102.pdf', ch.ga_geography,'Forest and Wildlife Resources (NCERT Class 10 Geography Ch 2)',             'वन एवं वन्य जीव संसाधन (NCERT कक्षा 10 भूगोल अध्याय 2)'],
  ['jess103.pdf', ch.ga_geography,'Water Resources (NCERT Class 10 Geography Ch 3)',                           'जल संसाधन (NCERT कक्षा 10 भूगोल अध्याय 3)'],
  ['jess104.pdf', ch.ga_geography,'Agriculture (NCERT Class 10 Geography Ch 4)',                               'कृषि (NCERT कक्षा 10 भूगोल अध्याय 4)'],
  ['jess105.pdf', ch.ga_geography,'Minerals and Energy Resources (NCERT Class 10 Geography Ch 5)',             'खनिज एवं ऊर्जा संसाधन (NCERT कक्षा 10 भूगोल अध्याय 5)'],
  ['jess106.pdf', ch.ga_geography,'Manufacturing Industries (NCERT Class 10 Geography Ch 6)',                  'विनिर्माण उद्योग (NCERT कक्षा 10 भूगोल अध्याय 6)'],
  ['jess107.pdf', ch.ga_geography,'Lifelines of National Economy (NCERT Class 10 Geography Ch 7)',             'राष्ट्रीय अर्थव्यवस्था की जीवन रेखाएँ (NCERT कक्षा 10 भूगोल अध्याय 7)'],
  // Class 8 Resources and Development (hess4)
  ['hess401.pdf', ch.ga_geography,'Resources (NCERT Class 8 Geography Ch 1)',                                  'संसाधन (NCERT कक्षा 8 भूगोल अध्याय 1)'],
  ['hess402.pdf', ch.ga_geography,'Land, Soil, Water, Natural Vegetation and Wildlife (NCERT Class 8 Ch 2)',   'भूमि, मिट्टी, जल, प्राकृतिक वनस्पति एवं वन्य जीवन (NCERT कक्षा 8 अध्याय 2)'],
  ['hess403.pdf', ch.ga_geography,'Mineral and Power Resources (NCERT Class 8 Geography Ch 3)',                'खनिज एवं शक्ति संसाधन (NCERT कक्षा 8 भूगोल अध्याय 3)'],
  ['hess404.pdf', ch.ga_geography,'Agriculture (NCERT Class 8 Geography Ch 4)',                                'कृषि (NCERT कक्षा 8 भूगोल अध्याय 4)'],
  ['hess405.pdf', ch.ga_geography,'Industries (NCERT Class 8 Geography Ch 5)',                                 'उद्योग (NCERT कक्षा 8 भूगोल अध्याय 5)'],

  // ========== Science — Physics (Class 10 only, NTPC-appropriate level) ==========
  ['jesc109.pdf', ch.sci_physics, 'Light — Reflection and Refraction (NCERT Class 10 Ch 9)',                   'प्रकाश — परावर्तन और अपवर्तन (NCERT कक्षा 10 अध्याय 9)'],
  ['jesc110.pdf', ch.sci_physics, 'The Human Eye and the Colourful World (NCERT Class 10 Ch 10)',              'मानव नेत्र तथा रंगीन संसार (NCERT कक्षा 10 अध्याय 10)'],
  ['jesc111.pdf', ch.sci_physics, 'Electricity (NCERT Class 10 Ch 11)',                                        'विद्युत (NCERT कक्षा 10 अध्याय 11)'],
  ['jesc112.pdf', ch.sci_physics, 'Magnetic Effects of Electric Current (NCERT Class 10 Ch 12)',               'विद्युत धारा के चुंबकीय प्रभाव (NCERT कक्षा 10 अध्याय 12)'],

  // ========== Science — Chemistry (Class 10) ==========
  ['jesc101.pdf', ch.sci_chem,    'Chemical Reactions and Equations (NCERT Class 10 Ch 1)',                    'रासायनिक अभिक्रियाएँ एवं समीकरण (NCERT कक्षा 10 अध्याय 1)'],
  ['jesc102.pdf', ch.sci_chem,    'Acids, Bases and Salts (NCERT Class 10 Ch 2)',                              'अम्ल, क्षारक एवं लवण (NCERT कक्षा 10 अध्याय 2)'],
  ['jesc103.pdf', ch.sci_chem,    'Metals and Non-metals (NCERT Class 10 Ch 3)',                               'धातु एवं अधातु (NCERT कक्षा 10 अध्याय 3)'],
  ['jesc104.pdf', ch.sci_chem,    'Carbon and its Compounds (NCERT Class 10 Ch 4)',                            'कार्बन एवं उसके यौगिक (NCERT कक्षा 10 अध्याय 4)'],

  // ========== Science — Biology (Class 10) ==========
  ['jesc105.pdf', ch.sci_bio,     'Life Processes (NCERT Class 10 Ch 5)',                                      'जैव प्रक्रम (NCERT कक्षा 10 अध्याय 5)'],
  ['jesc106.pdf', ch.sci_bio,     'Control and Coordination (NCERT Class 10 Ch 6)',                            'नियंत्रण एवं समन्वय (NCERT कक्षा 10 अध्याय 6)'],
  ['jesc107.pdf', ch.sci_bio,     'How Do Organisms Reproduce? (NCERT Class 10 Ch 7)',                         'जीव जनन कैसे करते हैं? (NCERT कक्षा 10 अध्याय 7)'],
  ['jesc108.pdf', ch.sci_bio,     'Heredity and Evolution (NCERT Class 10 Ch 8)',                              'आनुवंशिकता (NCERT कक्षा 10 अध्याय 8)'],
  ['jesc113.pdf', ch.sci_bio,     'Our Environment (NCERT Class 10 Ch 13)',                                    'हमारा पर्यावरण (NCERT कक्षा 10 अध्याय 13)'],

  // ========== English — Comprehension (literature passages for reading practice) ==========
  // Beehive (iebe) — main literature reader. Note: iebe chapter 1 is "The Fun They Had" per
  // NCERT TOC; the Phase 2.5 first-page audit showed pre-reading questions, which is how the
  // 2024-25 edition starts each chapter. Chapter label matches NCERT TOC.
  ['iebe101.pdf', ch.eng_compr,   'Beehive — The Fun They Had (NCERT Class 9 English Ch 1)',                   'Beehive — The Fun They Had (NCERT कक्षा 9 अंग्रेज़ी अध्याय 1)'],
  ['iebe102.pdf', ch.eng_compr,   'Beehive — The Sound of Music (NCERT Class 9 English Ch 2)',                 'Beehive — The Sound of Music (NCERT कक्षा 9 अंग्रेज़ी अध्याय 2)'],
  ['iebe103.pdf', ch.eng_compr,   'Beehive — The Little Girl (NCERT Class 9 English Ch 3)',                    'Beehive — The Little Girl (NCERT कक्षा 9 अंग्रेज़ी अध्याय 3)'],
  ['iebe104.pdf', ch.eng_compr,   'Beehive — A Truly Beautiful Mind (NCERT Class 9 English Ch 4)',             'Beehive — A Truly Beautiful Mind (NCERT कक्षा 9 अंग्रेज़ी अध्याय 4)'],
  ['iebe105.pdf', ch.eng_compr,   'Beehive — The Snake and the Mirror (NCERT Class 9 English Ch 5)',           'Beehive — The Snake and the Mirror (NCERT कक्षा 9 अंग्रेज़ी अध्याय 5)'],
  ['iebe106.pdf', ch.eng_compr,   'Beehive — My Childhood (NCERT Class 9 English Ch 6)',                       'Beehive — My Childhood (NCERT कक्षा 9 अंग्रेज़ी अध्याय 6)'],
  ['iebe107.pdf', ch.eng_compr,   'Beehive — Reach for the Top (NCERT Class 9 English Ch 7)',                  'Beehive — Reach for the Top (NCERT कक्षा 9 अंग्रेज़ी अध्याय 7)'],
  ['iebe108.pdf', ch.eng_compr,   'Beehive — Kathmandu (NCERT Class 9 English Ch 8)',                          'Beehive — Kathmandu (NCERT कक्षा 9 अंग्रेज़ी अध्याय 8)'],
  // Moments (iemo) — supplementary short stories
  ['iemo101.pdf', ch.eng_compr,   'Moments — The Lost Child (NCERT Class 9 English Ch 1)',                     'Moments — The Lost Child (NCERT कक्षा 9 अंग्रेज़ी अध्याय 1)'],
  ['iemo102.pdf', ch.eng_compr,   'Moments — The Adventures of Toto (NCERT Class 9 English Ch 2)',             'Moments — The Adventures of Toto (NCERT कक्षा 9 अंग्रेज़ी अध्याय 2)'],
  ['iemo103.pdf', ch.eng_compr,   'Moments — Iswaran the Storyteller (NCERT Class 9 English Ch 3)',            'Moments — Iswaran the Storyteller (NCERT कक्षा 9 अंग्रेज़ी अध्याय 3)'],
  ['iemo104.pdf', ch.eng_compr,   'Moments — In the Kingdom of Fools (NCERT Class 9 English Ch 4)',            'Moments — In the Kingdom of Fools (NCERT कक्षा 9 अंग्रेज़ी अध्याय 4)'],
  ['iemo105.pdf', ch.eng_compr,   'Moments — The Happy Prince (NCERT Class 9 English Ch 5)',                   'Moments — The Happy Prince (NCERT कक्षा 9 अंग्रेज़ी अध्याय 5)'],
  ['iemo106.pdf', ch.eng_compr,   'Moments — Weathering the Storm in Ersama (NCERT Class 9 English Ch 6)',     'Moments — Weathering the Storm in Ersama (NCERT कक्षा 9 अंग्रेज़ी अध्याय 6)'],
  ['iemo107.pdf', ch.eng_compr,   'Moments — The Last Leaf (NCERT Class 9 English Ch 7)',                      'Moments — The Last Leaf (NCERT कक्षा 9 अंग्रेज़ी अध्याय 7)'],
];

// ---------- SQL generation ----------
function esc(s) { return `'${String(s).replaceAll("'", "''")}'`; }

const lines = [];
lines.push('-- Phase 2.5 comprehensive reseed — all NCERT book identities verified by page-1 content audit.');
lines.push('');

// 1) Add the new Geography chapter to the GA subject.
lines.push('-- 1. Add "Indian Geography" chapter under GA (new since Phase 2). Fixed UUID so inserts below can reference it.');
lines.push(`insert into public.chapters (id, subject_id, slug, title_en, title_hi, display_order)`);
lines.push(`select '${ch.ga_geography}'::uuid, s.id, 'geography', 'Indian Geography', 'भारतीय भूगोल', 4`);
lines.push(`  from public.subjects s where s.slug='ga'`);
lines.push(`  on conflict (id) do nothing;`);
lines.push('');

// 2) Mark every existing PDF topic stale so the correct ones below supersede them.
lines.push('-- 2. Retire all Phase 2 / early Phase 2.5 PDF rows. Correct replacements are inserted below.');
lines.push(`update public.topics set status='stale' where content_type='PDF_URL';`);
lines.push('');

// 3) Bulk insert via a single multi-row values() — compact enough to pass through one MCP migration.
lines.push('-- 3. Insert verified topics with correct chapter mappings.');
lines.push(`insert into public.topics (chapter_id, title_en, title_hi, content_type, external_pdf_url, source, license, display_order, status, last_verified_at) values`);
const rows = seed.map(([fname, chapterId, en, hi], i) => {
  const url = `https://ncert.nic.in/textbook/pdf/${fname}`;
  return `  ('${chapterId}', ${esc(en)}, ${esc(hi)}, 'PDF_URL', ${esc(url)}, 'NCERT', 'NCERT_LINKED', ${10 + i}, 'active', now())`;
});
lines.push(rows.join(',\n') + ';');

const sql = lines.join('\n') + '\n';
mkdirSync(resolve(ROOT, 'sql'), { recursive: true });
writeFileSync(resolve(ROOT, 'sql', 'phase25_reseed_v2.sql'), sql);
console.log(`wrote sql/phase25_reseed_v2.sql (${seed.length} topics)`);
