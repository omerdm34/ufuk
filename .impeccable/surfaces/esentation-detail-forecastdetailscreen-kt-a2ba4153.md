---
version: 1
slug: "esentation-detail-forecastdetailscreen-kt-a2ba4153"
primary_target: "app/src/main/java/com/kampplus/ufuk/feature/forecast/presentation/detail/ForecastDetailScreen.kt"
related_targets: []
---

Scope: whole Ufuk Android app (forecast detail is the lead surface; places, explore, settings inherit). Mode: Operate.
Audience/job: people in Türkiye glancing at the weather before going out; camp reviewers reading code. Task: know now / next hours / next days in one glance; manage places; search.
Constraints: Material 3 structure (navigation bar, top app bars, snackbars, system Back), Open-Meteo attribution, four UI states, stale data labelled.

## Direction contract

THESIS: Every place is read like an instrument on a desk weather station: a dial whose steel needle sits on the current value and whose brass set-hand marks the same hour yesterday, so change is visible before it is read. Refuses the category default of a full-screen sky gradient with frosted glass cards.

OWN-WORLD: Black lacquer dial ground (#121417 / #1E2126) in dark, silvered dial (#E7E6E1) in light; engraved scales in bone (#E9E4D8) or ink; gilt (#C9A45C) for engraving accents and the brass set-hand; one live accent, vermilion needle (#C8412D), used only for the current reading. Blued steel (#3E6FA8) for rain. Round instrument faces, hairline tick scales, engraved small labels, tabular numerals. No gradients, no glass.

STORY: The visitor sees the value, sees which way it moved, reads one plain sentence, then goes deeper (hours, days, instruments) only if needed.

FIRST VIEWPORT: Detail: top app bar with city; a full-width dial (~300dp) whose arc spans today's min–max, needle at now, set-hand at the same hour yesterday, huge thin temperature numeral in the lower half with condition word under it (the needle never crosses the readout); beneath, one sentence of forecast summary and a row of three small readings (feels like, rain chance, wind). Hourly strip starts right under the fold.

FORM: Desk weather station (barometer + thermometer + hygrometer plaque), position 3 of 7 on the ordered list, seed key d8fd1428. Raises: live accent only on the current reading (nixie); stale data dims like decayed glow and states its age (cathode gauze); list hierarchy by size, not boxes (festival lineup).

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
