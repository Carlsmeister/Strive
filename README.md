# Strive
Take control of your fitness with STRIVE – the modern training app that helps you reach your goals, step by step. Explore hundreds of exercises, create custom workouts, and run them with smart timers and guides. Track your runs with real-time GPS and follow your progress through clear charts and statistics.

## Quick start
- No setup required to run the app.
- Build and run; the app will seed its database from `app/src/main/assets/seed_exercises.json` if no API key is provided.
- Adding a RapidAPI key is optional and only enables fetching a larger, live catalog from ExerciseDB.

## Remote API setup (optional)
STRIVE can fetch exercises from ExerciseDB via RapidAPI. To enable remote data:

1. Create a RapidAPI account and subscribe to ExerciseDB.
2. Copy your API key from RapidAPI.
3. In the project root, create or edit `local.properties` and add:

```
RAPIDAPI_KEY=your_rapidapi_key_here
```

Notes:
- The API key is injected at build time via `BuildConfig.RAPIDAPI_KEY` and sent using required headers:
  - `x-rapidapi-key`
  - `x-rapidapi-host: exercisedb.p.rapidapi.com`
- Never commit your real key. The file `rapidapi_key_example.txt` shows the expected format.

## Offline/local fallback
If the API key is missing, invalid, or the network is unavailable, the app will automatically seed the local database using the bundled file at `app/src/main/assets/seed_exercises.json`. This guarantees that the Explore screen shows a starter set of exercises even without network access.

## Why "optional"?
Only the API setup above is optional. You can skip it and the app will work offline using the bundled seed data. The README itself is not optional—keep it for instructions and project info.
