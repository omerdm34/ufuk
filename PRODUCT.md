# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Native Android, same stack and versions as the Kamp+ reference app ("Hava", github.com/omerdm34/weather-app), chosen by the user: Kotlin 2.2.20, Jetpack Compose (Material 3), feature-first Clean Architecture in a single `:app` module, Coroutines + StateFlow, Hilt, Retrofit + kotlinx.serialization, Room, Navigation Compose (type-safe), DataStore for preferences. Tooling: Spotless/ktlint, JUnit + Turbine + MockWebServer, GitHub Actions, Git Flow with Conventional Commits.

## Users

Participants and reviewers of the Kamp+ (Turkcell Akademi) mobile bootcamp, and ordinary people in Türkiye checking the weather: a quick glance in the morning before going out, planning the next few hours (umbrella, jacket), checking a city they are travelling to.

## Product Purpose

The same job as the camp's reference app, done better: list cities with their current weather, open a city for its hourly and daily forecast, keep favourite cities, search any city in the world, and handle loading / data / empty / error states. Ufuk adds the user's own location, offline access to the last forecast, air quality, readable forecast summaries, and unit preferences.

Success: a reviewer can trace every technique taught in the camp (layers, DI, OCP bindings, four UI states, tests, Git Flow, CI, release config) and sees a product that feels finished on a real phone.

## Positioning

A forecast you can act on in a glance: Ufuk turns the raw numbers into short plain-language statements ("Rain starts around 15:00", "Tomorrow is 6° warmer") computed by tested domain rules, and still shows the last known forecast when the network is gone.

## Operating Context

- Data: Open-Meteo forecast, geocoding and air-quality APIs; free, no API key, non-commercial use, attribution required (CC BY 4.0).
- Used one-handed, outdoors, often on mobile data or no connection; camp classrooms may sit behind TLS-inspecting networks.
- Evaluated by instructors reading the code and the git history as much as by using the app.

## Capabilities and Constraints

- Featured Turkish cities list with live temperatures; worldwide city search (debounced).
- Forecast detail: current conditions, 24-hour hourly, 10-day daily, UV, wind, humidity, pressure, visibility, sunrise/sunset, air quality.
- Favourites ("My places") persisted in Room, with correct undo on removal; the device location as a place (runtime permission, no Google Play Services dependency).
- Offline-first forecast cache in Room; stale data is labelled with its age, never presented as live.
- Settings: temperature unit (°C/°F), wind unit (km/h, m/s, mph). Turkish default, English translation.
- Undecided: home-screen widget, notifications, GitHub repository visibility (asked at push time).

## Brand Commitments

- Name: Ufuk ("horizon"). Package `com.kampplus.ufuk`.
- Voice: short, calm, specific, second person informal Turkish ("sen"), no alarmism.

## Evidence on Hand

- Reference implementation and its known issues: `C:\Users\Acer\Claude Code\weather-app` (undo restores the wrong city, ineffective HTTP cache, `isDay` unused, hard-coded strings, refresh failure clears data).
- No users, testimonials or metrics exist; do not invent any.

## Product Principles

1. Glance first: the answer to "do I need a jacket or an umbrella?" is visible without scrolling.
2. Never lie about freshness: cached data always says how old it is.
3. Rules live in the domain: every derived statement comes from pure, unit-tested Kotlin.
4. Extend, don't modify: new data sources, conditions and units plug in through DI bindings.
5. Everything the camp taught stays legible in the code and the history.

## Accessibility & Inclusion

TalkBack reads every card as one sentence (city, temperature, condition). Text scales with system font size. Animated sky respects the system "remove animations" setting. Colour is never the only carrier of meaning (AQI and UV bands have labels).
