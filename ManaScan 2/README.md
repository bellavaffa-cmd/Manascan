# ManaScan

An Android app that scans a Magic: The Gathering card with the camera, reads the
card's title with on-device OCR, and looks up full card details (mana cost, rules
text, set, rarity, price) from the [Scryfall API](https://scryfall.com/docs/api).

## How it works

1. **Camera** (CameraX) streams frames from the back camera into a live preview.
2. **OCR** (ML Kit Text Recognition, on-device, no network) reads text on each
   frame and picks the topmost plausible line of text — MTG cards always print
   the name in a title bar along the top edge.
3. **Lookup** (Retrofit + Scryfall's `/cards/named?fuzzy=` endpoint) resolves the
   noisy OCR text to a real card, tolerating typos and partial reads.
4. **Detail screen** shows the card image, mana cost, type line, oracle text,
   power/toughness or loyalty, set, rarity, and USD price.

A manual search box with autocomplete is always available as a fallback, since
OCR against small, stylized fonts and glare on glossy cards won't be 100%
reliable.

## Project structure

```
app/src/main/java/com/example/manascan/
  MainActivity.kt              Nav host wiring the two screens together
  camera/
    CameraPreview.kt           CameraX preview + analysis use case, Compose-wrapped
    CardTextAnalyzer.kt        ML Kit OCR analyzer + "which line is the title" heuristic
  data/
    ScryfallApi.kt             Retrofit interface for Scryfall
    ScryfallModels.kt          Card/ImageUris/Prices data classes (Moshi)
    NetworkModule.kt           OkHttp/Retrofit/Moshi wiring
    CardRepository.kt          OCR-text cleanup + fuzzy/exact lookup + autocomplete
  ui/
    ScannerViewModel.kt        Debounces OCR reads, drives lookups, holds UI state
    ScanScreen.kt              Camera view, scan-frame overlay, search panel, permission UI
    CardDetailScreen.kt        Card detail display
    theme/                     Material3 theme (colors/typography)
```

## Requirements

- Android Studio (Koala or newer recommended)
- JDK 17
- An Android device or emulator running **API 24+** with a camera (a physical
  device is strongly recommended — camera OCR on an emulator is unreliable)

## Building

1. Open the `ManaScan/` folder in Android Studio as an existing project.
2. Let Gradle sync. This project doesn't ship a `gradlew` binary wrapper jar
   (binary files can't be generated in this environment) — Android Studio will
   prompt to create the Gradle wrapper automatically on first open. If it
   doesn't, run once from a terminal in the project root:
   ```
   gradle wrapper --gradle-version 8.7
   ```
   (requires a local Gradle install; afterwards `./gradlew` works normally).
3. Run the `app` configuration on a connected device or emulator.

No API keys are required — Scryfall's API is free and open, and this app
identifies itself via a `User-Agent` header per Scryfall's API etiquette
guidelines.

## Notes / things you may want to change

- **OCR heuristic**: `CardTextAnalyzer` assumes the topmost legible line in
  frame is the card name. This works well for modern-frame cards held
  right-side up; foil glare, sleeves, or unusual card frames (like older
  border styles) can trip it up. The manual search box is there for exactly
  that reason.
- **Rate limiting**: Scryfall asks clients to stay under ~10 requests/second
  and to cache where reasonable. The 600ms debounce on OCR reads keeps this
  app well under that, but a production app scanning many cards back-to-back
  should add local caching.
- **Double-faced cards**: basic support is included (falls back to the front
  face's image/text), but there's no "flip" button — add one if you plan to
  scan a lot of transform/modal cards.
- **No persistence**: scanned cards aren't saved anywhere; this is a pure
  scan-and-view app. Wiring in Room for a collection tracker would be the
  natural next step.
- **Attribution**: card data/images come from Scryfall, which in turn sources
  them from Wizards of the Coast. Keep the in-app attribution line if you
  publish this.
