# Currency Exchange — Android Project

An Android project that demonstrates a **Currency Exchange SDK** and an **example app** that consumes it.

[![JitPack](https://jitpack.io/v/ShaniHaker/CurrencyExchangeProject.svg)](https://jitpack.io/#ShaniHaker/CurrencyExchangeProject)

**Documentation:** [ShaniHaker.github.io/CurrencyExchangeProject](https://ShaniHaker.github.io/CurrencyExchangeProject)  
**Backend API:** [web-production-4ba4.up.railway.app](https://web-production-4ba4.up.railway.app)  
**Backend Repo:** [ShaniHaker/currency-exchange-api](https://github.com/ShaniHaker/currency-exchange-api)

---

## Project Structure

```
CurrencyExchangeProject/
├── currencysdk/          # Android library — published to JitPack
│   ├── api/              # Retrofit service interface + client
│   ├── models/           # Data classes that map to API responses
│   └── CurrencySdk.kt    # Main SDK entry point
│
└── app/                  # Example application that uses the SDK
    └── src/
        ├── MainActivity.kt
        ├── CurrencyViewModel.kt
        ├── CurrencyConverterScreen.kt
        └── FavoritesHistoryScreen.kt
```

---

## Architecture

```
┌──────────────────────────────────────┐
│          Example App (:app)          │
│                                      │
│  CurrencyConverterScreen  (Compose)  │
│  FavoritesHistoryScreen   (Compose)  │
│  CurrencyViewModel        (MVVM)     │
└──────────────┬───────────────────────┘
               │ uses
┌──────────────▼───────────────────────┐
│        Currency SDK (:currencysdk)   │
│                                      │
│  CurrencySdk         (public API)    │
│  CurrencyApiService  (Retrofit)      │
│  RetrofitClient      (OkHttp)        │
│  Models              (data classes)  │
└──────────────┬───────────────────────┘
               │ HTTP
┌──────────────▼───────────────────────┐
│     Flask API (Railway)              │
│  + MongoDB Atlas                     │
└──────────────────────────────────────┘
```

---

## SDK — Quick Start

### 1. Add JitPack to `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add the dependency to your app's `build.gradle.kts`

```kotlin
dependencies {
    implementation("com.github.ShaniHaker:currencysdk:1.0.1")
}
```

### 3. Use the SDK

```kotlin
import com.example.currencysdk.CurrencySdk

val sdk = CurrencySdk()

// Inside a coroutine or viewModelScope:
viewModelScope.launch {

    // Get supported currencies
    val currencies = sdk.getSupportedCurrencies()

    // Get live exchange rate
    val rate = sdk.getExchangeRate("USD", "ILS")
    println("Rate: ${rate.rate}")

    // Convert an amount
    val result = sdk.convertAmount("USD", "ILS", 100.0)
    println("${result.amount} USD = ${result.convertedAmount} ILS")

    // Save a favorite
    sdk.addFavorite("USD", "ILS")

    // Get all favorites
    val favorites = sdk.getFavorites()

    // Delete a favorite
    sdk.deleteFavorite(favorites.favorites[0].id)

    // Get conversion history
    val history = sdk.getHistory()
}
```

---

## SDK Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `getSupportedCurrencies()` | List all supported currencies | `CurrenciesResponse` |
| `getExchangeRate(from, to)` | Live exchange rate | `RateResponse` |
| `convertAmount(from, to, amount)` | Convert and save to history | `ConversionResponse` |
| `getFavorites()` | All saved favorites | `FavoritesResponse` |
| `addFavorite(from, to)` | Save a currency pair | `MessageResponse` |
| `deleteFavorite(id)` | Remove a favorite | `MessageResponse` |
| `getHistory()` | All past conversions | `HistoryResponse` |

---

## Pointing the SDK at the Live API

By default `RetrofitClient.kt` points to `http://127.0.0.1:5000/` for local development.  
To use the deployed Railway API, change `BASE_URL` in `currencysdk/src/main/java/com/example/currencysdk/api/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "https://web-production-4ba4.up.railway.app/"
```

---

## Example App Features

| Screen | Features |
|--------|----------|
| Converter | Select currencies, enter amount, see live conversion result |
| Favorites & History | View saved pairs, add/delete favorites, browse past conversions |

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Networking | Retrofit 3 + OkHttp 5 + Gson |
| Async | Kotlin Coroutines |
| Distribution | JitPack |

---

## License

MIT — see [LICENSE](LICENSE)
