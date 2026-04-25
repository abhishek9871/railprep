# Direction Sense — Reasoning Primer

## What this topic tests

A person walks a sequence of segments — *"5 km north, then 3 km east, then 2 km south"* — and the question asks for their final position relative to the start, or the total displacement, or the angle of the final bearing. You have to chain the segments on a map, account for direction changes, and read off the result.

The standard cardinal directions: **N (north), E (east), S (south), W (west)**, and the four diagonals NE, NW, SE, SW. Bearings are typically given relative to the previous walking direction ("turns 90° left", "turns right and walks 4 km") or to absolute north ("walks east", "walks south-west").

The single biggest error pattern is **applying turns from a fixed frame instead of the walker's frame**. When the walker "turns left", they turn relative to their CURRENT direction of travel, not the map's left.

## The relative turn rule

Track two things at every step:

1. **Position** (x, y) — typically with east as +x and north as +y.
2. **Heading** — the direction you're currently facing, given as one of N, E, S, W (or finer angles).

At a turn, update the heading per this table (assuming you start facing one of the four cardinal directions):

| Current heading | Left turn | Right turn |
|-----------------|-----------|------------|
| N | W | E |
| E | N | S |
| S | E | W |
| W | S | N |

Each "left" turn is +90° counterclockwise from the current heading; each "right" is −90°. After ANY left/right turn, look up the new heading in the table; do not try to compute it relative to the original direction.

For more exotic angles (45°, 135°, etc.), draw a compass on each step and rotate it.

## Cardinal worked example

> *"Sumit starts from his home, walks 5 km north, turns right, walks 3 km, turns right again, walks 5 km, then turns left and walks 4 km. How far is he from his home and in which direction?"*

Step 1: Set start at origin (0, 0), facing N.

Step 2: Walk 5 km north. Position (0, 5). Heading N.

Step 3: Turn right ⇒ heading E. Walk 3 km east. Position (3, 5). Heading E.

Step 4: Turn right ⇒ heading S. Walk 5 km south. Position (3, 0). Heading S.

Step 5: Turn left ⇒ heading E. Walk 4 km east. Position (7, 0). Heading E.

**Final position: (7, 0)** — 7 km east of home, exactly on the east-west line through home.

**Distance from home:** √(7² + 0²) = 7 km.

**Direction from home:** Due east.

## Pythagoras for the diagonal case

If the final position has both nonzero x and y components, distance = √(x² + y²). Direction is read from the signs:

- (+x, +y) → northeast quadrant
- (−x, +y) → northwest
- (−x, −y) → southwest
- (+x, −y) → southeast

To get the exact angle:

- If |x| = |y|, the bearing is exactly NE / NW / SE / SW (45° from N).
- Otherwise, the bearing is "north Y° east" or similar, where Y = arctan(|x| / |y|) for a north-anchored bearing, or arctan(|y| / |x|) for an east-anchored one. Exam questions usually pick clean ratios where Y is 30°, 45°, or 60°.

## Worked example with diagonal turn

> *"Ravi walks 4 km east, turns 45° to his left, and walks 5√2 km. How far is he from the start, and in what direction?"*

Step 1: From (0, 0), walk 4 km east → (4, 0), heading E.

Step 2: Turn 45° left from heading E. From the table: heading E + 90° left = N, so 45° left of E is NE (halfway between).

Step 3: Walk 5√2 km along NE direction. Each step in NE moves √2 metres in x and √2 metres in y per metre of NE motion (because cos 45° = sin 45° = 1/√2). So 5√2 km along NE = 5 km east AND 5 km north.

Step 4: New position = (4 + 5, 0 + 5) = (9, 5).

**Distance from home:** √(9² + 5²) = √(81 + 25) = √106 ≈ 10.30 km.

**Direction:** North-east of home (both x and y positive). Specifically, the angle east of north is arctan(9/5) ≈ 60.95° — so roughly "north 61° east" or, in exam parlance, "between north and east, closer to east".

## The shadow trick

If a problem mentions "shadow at sunrise" or "shadow at sunset", remember:

- At sunrise, the sun is in the east, so shadows fall WEST.
- At sunset, the sun is in the west, so shadows fall EAST.

A person whose shadow falls to their left at sunrise is facing NORTH (because their left is west, and west = sunrise shadow direction). Similarly, shadow to the right at sunrise → facing south. Shadow to left at sunset → facing south.

These tricks let you anchor a starting heading even when the problem doesn't state it explicitly.

## Common slip patterns

**Slip 1: Turning relative to start instead of current heading.** "He turns right" always means right of his CURRENT direction. If he was heading west, "right" is north (not east).

**Slip 2: Mixing up east-west axis.** Always set east = +x and north = +y. If you flip these, every distance is right but every direction is wrong.

**Slip 3: Forgetting to update heading at each turn.** Mechanically write the new heading after every turn — don't carry the old one mentally.

**Slip 4: Confusing "walks right" with "turns right and walks".** "Walks 5 km to his right" usually means turns right then walks. "Walks 5 km right of the road" usually means a sideways translation. Read carefully.

## Final check

Plot all positions on graph paper or a sketch. Verify each segment matches the heading at that step. If your final answer says "north" but your final position has x > 0 and y = 0, recheck — that's east, not north.

When the answer involves a diagonal, the distance √(x² + y²) and direction (arctan ratio) should both round to clean exam values. If they don't, you probably mis-tallied a turn.

---

# Direction Sense — Reasoning Primer (हिन्दी)

## यह topic क्या test करता है

एक व्यक्ति कई segments चलता है — *"5 km उत्तर, फिर 3 km पूर्व, फिर 2 km दक्षिण"* — और पूछा जाता है: starting point से final position की दूरी, displacement, या final bearing। Segments को map पर chain करना, direction changes track करना, और answer पढ़ना है।

Standard cardinal directions: **N (उत्तर), E (पूर्व), S (दक्षिण), W (पश्चिम)**, और चार diagonals NE, NW, SE, SW।

सबसे बड़ी error: **turns को fixed frame से apply करना — walker's frame से नहीं**। जब walker "left turn" करे, वह CURRENT direction से turn करता है, map के left से नहीं।

## Relative turn rule

हर step पर दो चीज़ें track करें:

1. **Position** (x, y) — east = +x, north = +y।
2. **Heading** — current facing direction।

Turn पर update table:

| Current heading | Left turn | Right turn |
|-----------------|-----------|------------|
| N | W | E |
| E | N | S |
| S | E | W |
| W | S | N |

Left turn = +90° counterclockwise, right turn = −90°। Turn के बाद TABLE से नया heading लें — original direction से calculate मत करें।

45°, 135° जैसे angles के लिए हर step पर compass draw करें।

## Cardinal worked example

> *"सुमित घर से 5 km उत्तर चला, right turn, 3 km, फिर right turn, 5 km, फिर left turn, 4 km। घर से कितनी दूर और किस direction में है?"*

Step 1: Start (0, 0), heading N।

Step 2: 5 km उत्तर ⇒ (0, 5)। Heading N।

Step 3: Right turn ⇒ heading E। 3 km पूर्व ⇒ (3, 5)।

Step 4: Right turn ⇒ heading S। 5 km दक्षिण ⇒ (3, 0)।

Step 5: Left turn ⇒ heading E। 4 km पूर्व ⇒ (7, 0)।

**Final: (7, 0)** — 7 km east of home।

**Distance:** √(7² + 0²) = 7 km।

**Direction:** ठीक पूर्व (due east)।

## Diagonal के लिए Pythagoras

Final position में दोनों x और y nonzero हों तो distance = √(x² + y²)। Direction signs से:

- (+x, +y) → northeast
- (−x, +y) → northwest
- (−x, −y) → southwest
- (+x, −y) → southeast

|x| = |y| हो तो bearing exactly NE/NW/SE/SW (45°)। Exam में अक्सर 30°, 45°, 60° clean ratios होते हैं।

## Diagonal turn worked example

> *"रवि 4 km east चला, फिर 45° left turn, फिर 5√2 km। घर से कितनी दूर?"*

Step 1: (0, 0) से 4 km east ⇒ (4, 0)।

Step 2: Heading E से 45° left = NE।

Step 3: NE में 5√2 km = 5 km east + 5 km north (cos 45° = sin 45° = 1/√2)।

Step 4: New position = (4 + 5, 0 + 5) = (9, 5)।

**Distance:** √(81 + 25) = √106 ≈ 10.30 km।

**Direction:** Northeast (दोनों x, y positive)।

## Shadow trick

Sunrise पर sun east में ⇒ shadow WEST में पड़ती है। Sunset पर sun west ⇒ shadow EAST।

Sunrise पर shadow left पर पड़े ⇒ person NORTH face कर रहा है (left = west, sunrise shadow = west)।

## Common slips

- **Start direction से turn**: "right turn" current heading से होता है, original से नहीं।
- **East-west axis flip**: हमेशा east = +x, north = +y।
- **हर turn पर heading update**: mechanically लिखें — mind में मत carry करें।
- **"Walks right" vs "Turns right and walks"**: ध्यान से पढ़ें।

## Final check

सभी positions plot करें। हर segment heading से match करना चाहिए। अगर answer "उत्तर" आता है पर final position में x > 0, y = 0 है — recheck (वह east है, उत्तर नहीं)।

Distance √(x²+y²) और direction (arctan ratio) clean values आनी चाहिए। न आएँ तो turn somewhere mis-tally हुई।
