-- Replace link-only reasoning articles with first-party RailPrep primers.
-- RRB NTPC CEN 05/2024 and CEN 06/2024 list Syllogism, Venn Diagrams,
-- Statement-Conclusion, series, calendar/clock-style reasoning under General
-- Intelligence and Reasoning. These primers keep the learner inside the app.

update public.topics
set content_md = $primer$
# Syllogism - Reasoning Primer

## What this topic tests

Syllogism questions test whether a conclusion must follow from given statements. In RRB NTPC, the answer is not based on real-world truth. It is based only on the statement set.

Typical words:

| Word | Meaning in exam logic |
|------|------------------------|
| All A are B | A is fully inside B |
| No A is B | A and B do not overlap |
| Some A are B | at least one A overlaps B |
| Some A are not B | at least one A is outside B |
| Only A are B | all B are A |

## 40-second method

1. Draw the strict Venn relation for each statement.
2. Test each conclusion separately.
3. A conclusion follows only when it is true in every valid diagram.
4. If a conclusion is only possible, mark it only when the option says possibility.

## Must-follow rules

- From "All trains are vehicles", you can say "Some vehicles are trains" only if trains are known to exist in the question universe. RRB questions normally treat stated classes as non-empty.
- From "Some A are B", you cannot reverse into "All B are A".
- From "No A is B", both "No B is A" and "Some A are not B" are safe if A exists.
- "Some A are B" and "No A is B" are contradictory.

## Common traps

- Treating "some" as "some but not all". In syllogism, some means at least one. It may include all.
- Reading "Only A are B" as "Only B are A". The direction reverses.
- Accepting a conclusion because it looks possible. Syllogism asks must follow unless the option explicitly says possibility.

## Worked example

Statements: All trains are vehicles. Some vehicles are electric.

Conclusion I: Some trains are electric.

Conclusion II: No train is electric.

Neither conclusion is compulsory. The electric vehicles may include trains, or may be completely outside trains. Therefore neither follows.

## Exam takeaway

Do not solve syllogism from memory of facts. Convert statements into circles, then ask: "Is this conclusion forced in every diagram?"
$primer$,
    article_url = null,
    source = 'In-house',
    license = 'ORIGINAL',
    updated_at = now()
where id = 'de69393e-b1f5-4889-aecf-84b492aa1b60';

update public.topics
set content_md = $primer$
# Venn Diagrams - Reasoning Primer

## What this topic tests

Venn diagram questions ask you to represent groups and count exact regions. In RRB NTPC, this appears in two forms: choosing the correct diagram for a relationship, and solving number-based set problems.

## Relationship diagrams

Use these patterns:

| Relationship | Diagram idea |
|--------------|--------------|
| All A are B | A circle fully inside B |
| Some A are B | A and B overlap partly |
| No A is B | A and B separate |
| A, B, C unrelated | three separate circles |
| A and B inside C | two smaller circles inside one bigger circle |

## Counting formula

For three sets A, B, C:

Total in at least one set = A + B + C - (A and B) - (B and C) - (C and A) + (A and B and C)

The plus at the end is important because the middle region gets subtracted three times.

## 40-second method

1. Draw the three circles.
2. Fill the center first: A and B and C.
3. Fill pair-only regions by subtracting the center from each pair count.
4. Fill single-only regions last.
5. Add only the asked region, not the whole diagram.

## Common traps

- Confusing "A and B" with "only A and B". If the question says A and B, it usually includes the center unless "only" is written.
- Forgetting the outside region when the question gives total students.
- Subtracting pair overlaps but not adding the triple overlap back.

## Worked example

In a class, 30 study Math, 25 study Reasoning, 20 study GK, 10 study Math and Reasoning, 8 study Reasoning and GK, 6 study Math and GK, and 4 study all three.

At least one subject = 30 + 25 + 20 - 10 - 8 - 6 + 4 = 55.

Only Math and Reasoning = 10 - 4 = 6.

## Exam takeaway

The word "only" decides the region. Underline it before doing arithmetic.
$primer$,
    article_url = null,
    source = 'In-house',
    license = 'ORIGINAL',
    updated_at = now()
where id = '296b1be4-65fa-4066-8275-836724e9eed8';

update public.topics
set content_md = $primer$
# Clock Problems - Reasoning Primer

## What this topic tests

Clock questions test angles, relative speed, mirror images, and time gained or lost. The highest-value shortcut is knowing that the minute hand moves much faster than the hour hand.

## Core facts

| Hand | Speed |
|------|-------|
| Minute hand | 6 degrees per minute |
| Hour hand | 0.5 degrees per minute |
| Relative speed | 5.5 degrees per minute |

## Angle shortcut

At H hours and M minutes:

Angle = absolute value of (30H - 5.5M)

If the answer is greater than 180 degrees, use 360 - angle for the smaller angle.

## 40-second method

1. Convert the hour into degrees: H x 30.
2. Convert minutes into minute-hand degrees: M x 6.
3. Add hour-hand movement after the hour: M x 0.5.
4. Subtract the two hand positions.
5. Choose the smaller angle if asked.

## Common traps

- Keeping the hour hand fixed at the hour mark. At 3:20, the hour hand has moved 10 degrees past 3.
- Giving reflex angle when the question asks smaller angle.
- Treating 12 as 12 x 30 = 360 instead of 0 degrees on the dial.

## Worked example

Find the smaller angle at 4:20.

Hour hand position = 4 x 30 + 20 x 0.5 = 120 + 10 = 130 degrees.

Minute hand position = 20 x 6 = 120 degrees.

Angle = 130 - 120 = 10 degrees.

## Exam takeaway

For angle questions, write only two numbers: hour-hand position and minute-hand position. That avoids most mistakes.
$primer$,
    article_url = null,
    source = 'In-house',
    license = 'ORIGINAL',
    updated_at = now()
where id = '05cfc37b-acce-4c03-85f9-fe054ce636c8';

update public.topics
set content_md = $primer$
# Calendar Problems - Day of Week Primer

## What this topic tests

Calendar questions ask the day of the week after adding years, months, and days. The core idea is odd days: the remainder left after dividing total days by 7.

## Core facts

| Period | Odd days |
|--------|----------|
| Normal year | 1 |
| Leap year | 2 |
| 100 years | 5 |
| 200 years | 3 |
| 300 years | 1 |
| 400 years | 0 |

A leap year is divisible by 4, except century years must be divisible by 400.

## Month odd days

For a normal year:

| Month | Odd days |
|-------|----------|
| Jan | 3 |
| Feb | 0 |
| Mar | 3 |
| Apr | 2 |
| May | 3 |
| Jun | 2 |
| Jul | 3 |
| Aug | 3 |
| Sep | 2 |
| Oct | 3 |
| Nov | 2 |
| Dec | 3 |

In a leap year, February contributes 1 odd day.

## 40-second method

1. Break the date into completed years, completed months, and remaining days.
2. Convert each part into odd days.
3. Add odd days and divide by 7.
4. Move that many days forward from the known reference day.

## Common traps

- Treating 1900 as a leap year. It is not divisible by 400, so it is not leap.
- Including the target date twice. Count completed days before the date, then add the date number carefully.
- Forgetting that 400-year cycles add 0 odd days.

## Worked example

If 1 Jan is Monday, what day is 1 Mar in a non-leap year?

Completed months before March: January = 3 odd days, February = 0 odd days.

Total shift = 3 days. Monday + 3 = Thursday.

## Exam takeaway

Calendar questions are remainder questions. Do not count full weeks; count only odd days.
$primer$,
    article_url = null,
    source = 'In-house',
    license = 'ORIGINAL',
    updated_at = now()
where id = 'e6d230ef-ee0e-4aa7-b683-48c5634885ee';

update public.topics
set content_md = $primer$
# Number Series - Arithmetic Progression Primer

## What this topic tests

Number series questions test pattern recognition under time pressure. Arithmetic progression is the simplest pattern: the difference between consecutive terms stays constant.

## Core pattern

If the series is:

a, a + d, a + 2d, a + 3d ...

then d is the common difference.

Nth term = a + (n - 1)d

## 40-second method

1. Write differences between consecutive terms.
2. If differences are equal, use arithmetic progression.
3. If differences are not equal, check second differences.
4. If signs alternate, split the series into odd-position and even-position terms.

## Common RRB patterns

| Pattern | What to check |
|---------|---------------|
| Constant difference | +4, +4, +4 |
| Increasing difference | +2, +4, +6, +8 |
| Decreasing difference | +20, +17, +14 |
| Alternating series | positions 1,3,5 follow one rule; 2,4,6 follow another |
| Mixed operation | x2 + 1, x2 + 2, x2 + 3 |

## Common traps

- Forcing one pattern when odd and even positions have separate rules.
- Missing negative differences in descending series.
- Checking only the first two gaps and assuming the rest match.

## Worked example

Find the missing term:

7, 12, 17, 22, ?, 32

Differences are +5, +5, +5. So the missing term is 22 + 5 = 27.

## Exam takeaway

Always write the difference row. For most NTPC number-series questions, the answer is visible after one or two difference rows.
$primer$,
    article_url = null,
    source = 'In-house',
    license = 'ORIGINAL',
    updated_at = now()
where id = '46785a36-6348-45d3-8e5c-7e1e677ad1f3';
