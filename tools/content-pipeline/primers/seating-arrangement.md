# Seating Arrangement — Reasoning Primer

## What this topic tests

A seating arrangement question gives you a set of people (usually 5-8) and a list of constraints about where each sits relative to the others. The goal is to reconstruct the seating, then answer one or two questions about it. The constraints typically specify directions ("immediately to the left of", "second from the right end", "facing", "between"), so you must keep BOTH the position AND the facing direction straight in your head.

There are three sub-types in the exam:

1. **Linear** — everyone sits on a single bench or row, all facing the same direction.
2. **Circular (single-direction)** — everyone sits around a circular table, all facing centre OR all facing outward.
3. **Circular (mixed-direction)** — some face centre, some face outward. The trickiest variant.

The single biggest source of errors is **flipping left and right**. Pick a frame of reference at the start of the problem and stick to it throughout.

## The cardinal rule for left/right

Imagine you are sitting in your own chair, looking forward. Your left and right are YOUR left and right. When solving:

- **All facing the same direction:** everyone's left/right matches the observer's. Easy.
- **Some facing opposite:** for those people, their left is the observer's right and vice versa. Mark them with an arrow showing facing direction.

For a CIRCULAR table where everyone faces the centre, "left" and "right" depend on each person's orientation. Person A's right neighbour is the one ON A's right hand, NOT the next person clockwise from the observer's bird's-eye view.

A simple heuristic: when everyone faces centre, the person on YOUR right is your COUNTERCLOCKWISE neighbour (when viewed from above). When everyone faces outward, the person on YOUR right is your CLOCKWISE neighbour.

## Drawing the diagram

For a linear arrangement of 6 seats, draw 6 underscores: `_ _ _ _ _ _`. Number them 1 through 6 from the LEFT (i.e., the observer's left = the people's left if all face the observer).

For a circular arrangement, draw a circle with N tick marks around it. Place people one at a time, marking each with `→` (facing centre) or `←` (facing outward) if mixed.

## Linear worked example

> *"Five friends — P, Q, R, S, T — sit in a row, all facing north. Q is to the immediate left of P. R is at the rightmost position. S is two seats to the right of T. Find the position of each."*

Step 1: Draw 5 seats from left to right (from the observer's view, since everyone faces north away from us, "their left" matches the observer's left).

```
   _    _    _    _    _
  L1   L2   L3   L4   L5
```

Step 2: Apply constraints.

- **R is at the rightmost position.** R is at L5.
  ```
   _    _    _    _    R
  L1   L2   L3   L4   L5
  ```

- **Q is to the immediate left of P.** Q at position k, P at position k+1. So Q-P fills two adjacent slots.

- **S is two seats to the right of T.** T at position j, S at position j+2.

We need to fit Q-P (two consecutive) and T-?-S (T then one slot then S) into L1-L4 alongside any leftover.

Try T-?-S spanning L1-L3: T at L1, ? at L2, S at L3. Then Q-P must occupy L4 and somewhere — but L4 is the only slot left in {L4} and L5 is taken. Doesn't fit Q-P (needs two slots).

Try T at L2, S at L4: leaves L1, L3 for Q-P. Q-P needs adjacent slots, but L1 and L3 aren't adjacent.

Try T at L1, S at L3: same as the first attempt.

Try T at L2, S at L4: L1 and L3 remain. Not adjacent → Q-P fails.

This means we need Q-P inside the T-?-S structure. What if Q-P is inside T-?-S? T at L1, Q at L2, P at L3 — but S must be at L1+2 = L3, conflicting with P. Or T at L2, Q at L3, P at L4 → S at L4 = P. Conflict.

Re-examine: Q-P (two consecutive) and T-?-S (positions j and j+2). They share a middle slot if j+1 between Q and P. So Q at j, P at j+2 = S — conflict. So they cannot overlap.

We need Q-P + T-?-S in 4 distinct slots from {L1, L2, L3, L4}. T-?-S needs 3 slots in a row pattern (with a gap). Q-P needs 2 adjacent slots. Total 5 slots used — but we only have 4! So one of T, S, Q, or P MUST be at L5 — contradicting R at L5.

The puzzle as stated is over-constrained for 5 people. (This is for primer illustration — real exam puzzles always have a consistent solution.) The takeaway: when a contradiction emerges, recheck constraint reading. Often the slip is misreading "two seats to the right" as "second from the right" or similar.

## Circular worked example

> *"Six people — A, B, C, D, E, F — sit around a circular table facing the centre. A is opposite to D. B is on A's immediate right. C is between D and E."*

Step 1: Six seats around a circle. With "A opposite D", A and D are 3 apart on the table.

```
       A
   F       B
   E       C
       D
```

Step 2: "B on A's immediate right". When A faces centre, A's right is the counterclockwise neighbour (from observer's bird's-eye view). Place B accordingly.

Step 3: "C is between D and E." C is adjacent to both D and E. Given D is opposite A, and B is one seat to A's right (counterclockwise), the layout is:

```
        A
   B         F
   C         E
        D
```

Wait — re-examine: if B is on A's right (counterclockwise from above), then going clockwise from A: F, E, D, C, B, back to A. So C must be between B and D. That puts C adjacent to D and B, not D and E. Contradiction with "C between D and E".

So we re-place: maybe B is on A's left (clockwise from above) — but the problem says right. With facing-centre, right = counterclockwise from observer. Stick with that. The configuration must have C between D and E with D opposite A.

Final: counterclockwise from A = B, then ?, then D (opposite A), then E, then ?, then back to A. Slot 3 (between B and D) and slot 5 (between D + 2 and A): 6 seats total = A, B, ?, D, E, F. So ? is C, giving A, B, C, D, E, F counterclockwise. C is between B and D? But spec said between D and E. We need to swap the direction. With everyone facing centre and B on A's right (= A's clockwise neighbour from observer), the order from A clockwise is: A, B, C, D, E, F, back to A. Then C is between B and D, E is between D and F. "C between D and E" doesn't match. Re-read the problem.

Lesson: such puzzles have a unique solution only when the constraints are read precisely. When they don't seem to fit, **re-derive your direction convention** before changing the constraints.

## Mixed-direction circular: the survival rule

When some face centre and others face outward, draw arrows on each person. To find someone's right neighbour:

1. Identify the facing direction (arrow).
2. Their right is the seat that's clockwise from their face direction.

Equivalently: rotate yourself to match their orientation, then "right" is your right.

This is the most error-prone variant in the exam. Always draw the arrows.

## Final check

After placing everyone, **re-read each constraint and verify it holds in the diagram**. If even one constraint fails, your placement is wrong — go back and try the alternative branch. Most puzzles have at most 2-3 branches; brute force is fine.

---

# Seating Arrangement — Reasoning Primer (हिन्दी)

## यह topic क्या test करता है

Seating arrangement question में 5-8 लोग दिए जाते हैं और constraints होते हैं कि कौन कहाँ बैठा है ("immediately to the left of", "between", "facing", etc.)। Goal: seating reconstruct करना और 1-2 questions answer करना। Position AND facing direction दोनों track करने पड़ते हैं।

तीन sub-types:

1. **Linear** — एक bench पर सभी same direction में।
2. **Circular (single-direction)** — circular table, सब centre या सब outward।
3. **Circular (mixed-direction)** — कुछ centre, कुछ outward। सबसे tricky।

सबसे बड़ी गलती **left-right flip** करना है। शुरू में एक frame of reference तय करें और उसी पर रहें।

## Left/right का cardinal rule

खुद को chair में बैठा कल्पना करें, सामने देख रहे हों। आपका left/right आपके लिए है।

- **सब same direction में:** सबका left/right observer से match करता है।
- **कुछ opposite face करते हैं:** उनके लिए "left" observer का "right" है। Arrow से facing mark करें।

Circular table पर **सब centre face करें**: आपके right वाला observer के bird's-eye view से counterclockwise neighbour होगा। **सब outward face करें**: right वाला clockwise।

## Diagram बनाना

Linear (6 seats): `_ _ _ _ _ _`। 1 से 6 left-to-right number करें।

Circular: एक circle, N tick marks। हर person को `→` (centre face) या `←` (outward face) से mark करें (mixed में)।

## Linear worked example

> *"पाँच दोस्त — P, Q, R, S, T — एक row में बैठे हैं, सब उत्तर की ओर। Q, P के immediate left पर है। R rightmost पर है। S, T से दो seat right पर है। हर एक की position?"*

Step 1: 5 seats बनाएँ। सब उत्तर की ओर ⇒ "उनका left" observer के left से match (अगर हम पीछे से देख रहे हों — convention check करें)।

Step 2: **R rightmost पर** ⇒ R at L5।

Step 3: **Q-P adjacent**, Q पहले। **T-?-S** (T पहले, फिर एक gap, फिर S)।

Step 4: Try करें: T at L1, ? at L2, S at L3 ⇒ Q-P के लिए L4 बचा (single slot — fail)।

T at L2, S at L4 ⇒ L1, L3 बचे (non-adjacent — Q-P fail)।

[Original puzzle over-constrained — primer में पता चलने पर constraint reading दोबारा check करें।]

## Circular worked example

> *"छह लोग — A, B, C, D, E, F — circular table पर centre face करते बैठे हैं। A, D के opposite है। B, A के immediate right पर है। C, D और E के बीच है।"*

Step 1: 6 seats। A-D opposite ⇒ 3 अपार्ट।

Step 2: **B, A के right पर**: A centre face करता है ⇒ A's right = observer से counterclockwise neighbour।

Step 3: C, D और E के बीच ⇒ C दोनों के adjacent।

[Detailed seating placement pattern से derive — chain by chain।]

## Mixed-direction circular: survival rule

जब कुछ centre, कुछ outward face करें, हर person पर arrow बनाएँ। उसका right निकालने के लिए:

1. उसकी facing direction पहचानें।
2. उसका right = उस direction से clockwise।

Equivalent: खुद को उसकी orientation पर rotate करें, फिर "right" = आपका right।

यह सबसे error-prone variant है। हमेशा arrows बनाएँ।

## Final check

सब place करने के बाद **हर constraint को re-read करके diagram में verify करें**। एक भी constraint fail तो placement गलत — alternative branch try करें। Most puzzles में 2-3 branches होते हैं; brute force fine।
