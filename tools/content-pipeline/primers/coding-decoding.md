# Coding-Decoding — Reasoning Primer

## What this topic tests

A *code* is a rule that transforms a word, number, or symbol string into another. Coding-decoding problems give you one or more (input → coded form) pairs and ask you either to encode a new input or decode a new code. The pattern is hidden in the relationship.

Most exam codes fall into one of four families:

1. **Letter-shift codes** — each letter advances or retreats by a fixed number of positions in the alphabet.
2. **Position-based codes** — each letter is replaced by its alphabet position (A=1, ..., Z=26) or some derivative.
3. **Reversal codes** — the word's letters are reversed, and the result may also be shifted.
4. **Symbol substitution / arithmetic-operator codes** — `+` means `×`, `-` means `+`, etc.

Recognising which family the code belongs to is the entire game. Once you know the family, applying the rule is mechanical.

## Family 1: Letter-shift

Example pair: `CAT → DBU`. Compare letter by letter:
- C → D (shift +1)
- A → B (shift +1)
- T → U (shift +1)

The shift is +1 for every position. So `DOG → EPH` (D→E, O→P, G→H).

**Tricky variant:** the shift can vary by position. `CAT → DCW` has shifts +1, +2, +3 (incremental). Always check at least two letters before assuming the shift is constant.

**Cyclic wrap-around:** Z + 1 → A. Track this when shifts push past Z.

## Family 2: Position-based

Letters become numbers via A=1 … Z=26 (forward) or A=26 … Z=1 (reverse).

Example: `BAD → 2 1 4`. Each letter replaced by its forward position.

Variant: sum the positions. `BAD → 7` (because 2+1+4 = 7).

Variant: take only odd or even positions, or every Nth letter. Always check whether the count of digits in the code matches the count of letters in the word — mismatch tells you a sum or compression is involved.

## Family 3: Reversal

Example: `CAT → TAC`. The letters are reversed. Combine with shift: `CAT → UBD` is reversal (TAC) followed by +1 shift (UBD), or +1 shift first (DBU) then reversed (UBD). Both yield the same result here, but for asymmetric strings they differ.

**Telltale sign of reversal:** the first letter of the input maps to the last letter of the code (often with an additional shift). Compare positions of unique letters across pairs to spot it.

## Family 4: Symbol / operator substitution

You're told `+` means `×`, `−` means `÷`, `×` means `+`, `÷` means `−`. Then a numeric expression like `12 + 6 − 3 × 2 ÷ 4` is given.

**Rule:** Substitute each symbol with its meaning EXACTLY as given, then evaluate using normal BODMAS:

- Substituted: `12 × 6 ÷ 3 + 2 − 4`
- BODMAS: `12 × 6 = 72`, `72 ÷ 3 = 24`, then `24 + 2 − 4 = 22`.

**Most common slip:** evaluating with the original symbols (not the substituted ones) or doing left-to-right evaluation instead of BODMAS. Always substitute first, then do BODMAS.

## Worked example

> *"In a certain code, `TRIPLE` is written as `SQHOKD`. How will `EXOTIC` be written?"*

Step 1: Compare `TRIPLE` to `SQHOKD` letter by letter.

| Position | Original | Code | Shift |
|----------|----------|------|-------|
| 1 | T | S | −1 |
| 2 | R | Q | −1 |
| 3 | I | H | −1 |
| 4 | P | O | −1 |
| 5 | L | K | −1 |
| 6 | E | D | −1 |

Step 2: Every letter shifted by **−1**. Apply to `EXOTIC`:

- E → D, X → W, O → N, T → S, I → H, C → B

Answer: **`DWNSHB`**.

## How to recognise the family quickly

| Symptom | Likely family |
|---------|---------------|
| Code has same number of letters; first letter shifted constant amount | Letter-shift |
| Code is a string of digits | Position-based |
| Code letters spell input backwards | Reversal |
| Code has same number of letters; shift varies by position | Letter-shift (variable) |
| You're given new meanings for `+ − × ÷` | Operator substitution |

## Final check

Always verify by encoding the example forward (or decoding back) AFTER you've found the rule. If `TRIPLE → SQHOKD` via −1 shift, check that `S → T` (S +1) gives back `T`. If forward and reverse both work, the rule is right.

Don't forget cyclic wrap on letter-shift problems: A − 1 → Z, not some negative position.

---

# Coding-Decoding — Reasoning Primer (हिन्दी)

## यह topic क्या test करता है

*Code* एक rule है जो word, number, या symbol string को किसी दूसरी form में बदल देता है। Coding-decoding problems में एक या ज़्यादा (input → coded form) pairs दिए जाते हैं और पूछा जाता है कि नया input encode करें या नया code decode करें।

Exam में चार families मुख्य हैं:

1. **Letter-shift codes** — हर letter alphabet में fixed positions आगे या पीछे जाता है।
2. **Position-based codes** — हर letter उसकी alphabet position से replace होता है (A=1, ..., Z=26)।
3. **Reversal codes** — word के letters reverse हो जाते हैं, साथ में shift भी हो सकता है।
4. **Symbol substitution** — `+` का मतलब `×` है, `−` का मतलब `+` है, etc.

Family पहचानना ही पूरा game है। Family पता चलते ही rule mechanically apply होता है।

## Family 1: Letter-shift

Example: `CAT → DBU`। C→D (+1), A→B (+1), T→U (+1)। Shift = +1। तो `DOG → EPH`।

**Variant:** position के अनुसार shift बदले — `CAT → DCW` में +1, +2, +3। हमेशा 2+ letters check करें।

**Cyclic wrap:** Z + 1 → A।

## Family 2: Position-based

A=1, B=2, ..., Z=26। Example: `BAD → 2 1 4`।

Variant: positions का sum लें। `BAD → 7` (2+1+4)।

## Family 3: Reversal

Example: `CAT → TAC`। Reversal + shift combine हो सकता है।

**पहचान:** input का first letter, code के last letter से (अक्सर shift के साथ) match करता है।

## Family 4: Symbol substitution

`+ का मतलब ×, − का मतलब ÷, × का मतलब +, ÷ का मतलब −`। फिर `12 + 6 − 3 × 2 ÷ 4`।

**Rule:** पहले symbols substitute करें, फिर BODMAS से evaluate करें।

- Substituted: `12 × 6 ÷ 3 + 2 − 4`
- BODMAS: `12 × 6 = 72`, `72 ÷ 3 = 24`, फिर `24 + 2 − 4 = 22`

**Common slip:** original symbols से evaluate करना, या BODMAS नहीं लगाना। पहले substitute, फिर BODMAS।

## Worked example

> *"`TRIPLE` का code `SQHOKD` है। `EXOTIC` का code क्या होगा?"*

Step 1: Letter-by-letter compare:

| Pos | Original | Code | Shift |
|-----|----------|------|-------|
| 1 | T | S | −1 |
| 2 | R | Q | −1 |
| 3 | I | H | −1 |
| 4 | P | O | −1 |
| 5 | L | K | −1 |
| 6 | E | D | −1 |

Step 2: हर letter −1। `EXOTIC`:
E→D, X→W, O→N, T→S, I→H, C→B

Answer: **`DWNSHB`**

## Family पहचानने की shortcut table

| लक्षण | Family |
|-------|--------|
| Same letter count, first letter constant shift | Letter-shift |
| Code digits हैं | Position-based |
| Code, input को backward spell करता है | Reversal |
| Same letter count, shift position से बदले | Letter-shift (variable) |
| `+ − × ÷` के नए meanings दिए गए | Operator substitution |

## Final check

Rule मिलने के बाद **forward encode (या reverse decode) से verify करें**। `TRIPLE → SQHOKD` में S → T (S +1) से वापस `T` मिलना चाहिए। दोनों directions काम करें तो rule सही।

Letter-shift में cyclic wrap मत भूलें: A − 1 → Z।
