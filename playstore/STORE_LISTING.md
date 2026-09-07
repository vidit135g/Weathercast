# Weathercast — Google Play Store listing

**Package:** `com.tac.Weathercast`
**Developer:** The Absolute Corporation
**Category:** Weather
**Content rating:** Everyone
**versionCode:** 5 · **versionName:** 1.1

---

## App name (30 chars max)
```
Weathercast
```

## Short description (80 chars max)
```
A weather app that reads the sky — full-screen conditions, briefings and trends.
```

## Full description (4000 chars max)
```
Weathercast turns the forecast into something you actually want to look at.

The whole screen becomes the sky. A clear afternoon is bright blue, an overcast
morning is soft grey, dusk glows orange, and a clear night is deep navy — the
gradient shifts with the weather and the time of day, and rain, snow, drifting
clouds and sun move gently behind the numbers.

— TODAY —
The temperature floats on the sky with the condition, a matching illustration,
and quick "Feels / Wind / UV" chips. Below it, an "At a glance" panel writes the
day in plain English and pulls out the two things that matter most right now —
whether pressure is falling, when rain arrives, how big the temperature swing is.
Tap any card to open its full detail screen.

— BRIEFING —
A computed read on the day ahead: today versus tomorrow side by side, an
hour-by-hour timeline, the standout insights, and a Sun & Moon panel with the
real lunar phase, daylight length and sun times — all in the shown city's own
local time.

— TRENDS —
Five days of temperature, rainfall, pressure, wind and humidity. Every card
opens a detailed screen with a full chart, highs and lows, a plain-English
reading, and an hour-by-hour list.

— RADAR —
A dark, full-screen precipitation map with a location pin and a clean legend,
one tap from the Today screen.

— HOME-SCREEN WIDGETS —
Three widgets — compact, clock, and detailed — each drawn in the same sky-gradient
style as the app, so your home screen matches the weather too.

— MORE —
• Search any city with live autocomplete as you type
• Light, dark (AMOLED) or automatic theme
• Metric or imperial units, your choice of wind and pressure units
• Physics-based UV index that tracks the real daily curve — 0 at night
• Powered by OpenWeatherMap

No account. No ads. Made with care in India.
```

## What's new (release notes, 500 chars max)
```
A ground-up redesign:
• Apple-style full-screen sky that follows the weather and time of day
• New "Briefing" tab — today vs tomorrow, day-ahead timeline, standout insights, Sun & Moon
• Trends cards now open detailed per-metric screens
• Radar rebuilt with a clean dark map
• Home-screen widgets redrawn to match the app
• Fixed UV readings, city-local times, and a new app icon
• Motion throughout — charts draw themselves on, cards ease in, the sky cross-fades
```

## Graphic assets (in this folder)
| Asset | File | Spec |
|---|---|---|
| App icon (hi-res) | `icon-512.png` | 512×512 PNG |
| Feature graphic | `feature-graphic.png` | 1024×500 PNG |
| Phone screenshots | `screenshots/*.png` | 1080×2400 PNG (min 2, max 8) |
| Adaptive launcher icon | shipped in the APK/AAB | `@mipmap/ic_launcher` (vector fg + gradient bg + monochrome) |

## Data safety (suggested answers)
- Location: collected, used for app functionality (current-location weather), not shared, optional (city search works without it).
- No other personal or financial data collected.
- Weather requests go to OpenWeatherMap.
