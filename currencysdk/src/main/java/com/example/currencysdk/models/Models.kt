package com.example.currencysdk.models

// ---------------------------------------------------------------------------
// Each data class below maps directly to one piece of JSON that the Flask API
// sends back. Gson reads the JSON and fills in the fields automatically.
// @SerializedName("...") tells Gson which JSON key to look for when the JSON
// key name differs from the Kotlin property name.
// ---------------------------------------------------------------------------

/**
 * A single supported currency.
 * Example JSON: { "code": "USD", "name": "US Dollar" }
 */
data class Currency(
    val code: String,
    val name: String
)

/**
 * The full response from GET /currencies.
 * Example JSON: { "currencies": [ { "code": "USD", "name": "US Dollar" }, ... ] }
 */
data class CurrenciesResponse(
    val currencies: List<Currency>
)

/**
 * The exchange rate between two currencies.
 * Example JSON: { "from": "USD", "to": "ILS", "rate": 3.7 }
 */
data class RateResponse(
    val from: String,
    val to: String,
    val rate: Double
)

/**
 * The result of converting an amount from one currency to another.
 * Example JSON: { "from": "USD", "to": "ILS", "amount": 100, "convertedAmount": 370.0, "rate": 3.7, "timestamp": "..." }
 */
data class ConversionResponse(
    val from: String,
    val to: String,
    val amount: Double,
    val convertedAmount: Double,
    val rate: Double,
    val timestamp: String
)

/**
 * A single saved favorite currency pair.
 *
 * Field names match exactly what the Flask backend returns.
 * All fields have empty-string defaults so Gson never leaves them null
 * even if a field is missing from the JSON — this prevents crashes in
 * Compose Text() calls.
 *
 * Example JSON:
 * { "id": "abc123", "fromCurrency": "USD", "toCurrency": "ILS",
 *   "nickname": "My pair", "createdAt": "2024-01-01T12:00:00" }
 */
data class FavoriteItem(
    val id: String = "",              // MongoDB document ID — needed for deletion
    val fromCurrency: String = "",    // source currency code, e.g. "USD"
    val toCurrency: String = "",      // target currency code, e.g. "ILS"
    val nickname: String? = null,     // optional label; null when not set
    val createdAt: String = ""        // creation timestamp from the backend
)

/**
 * The full response from GET /favorites.
 * Example JSON: { "favorites": [ { "_id": "...", "from": "USD", "to": "ILS" }, ... ] }
 */
data class FavoritesResponse(
    val favorites: List<FavoriteItem>
)

/**
 * A single entry in the conversion history.
 * The backend saves one automatically every time /convert is called.
 *
 * Field names match exactly what the Flask backend returns.
 * Numeric fields default to 0.0 and strings to "" so Gson never
 * produces nulls that would crash Compose Text() calls.
 *
 * Example JSON:
 * { "id": "abc123", "fromCurrency": "USD", "toCurrency": "ILS",
 *   "amount": 100, "rate": 3.7, "convertedAmount": 370.0,
 *   "createdAt": "2024-01-01T12:00:00" }
 */
data class HistoryItem(
    val id: String = "",
    val fromCurrency: String = "",
    val toCurrency: String = "",
    val amount: Double = 0.0,
    val rate: Double = 0.0,
    val convertedAmount: Double = 0.0,
    val createdAt: String = ""
)

/**
 * The full response from GET /history.
 * Example JSON: { "history": [ { ... }, ... ] }
 */
data class HistoryResponse(
    val history: List<HistoryItem>
)

// ---------------------------------------------------------------------------
// Types used for writing data (POST / DELETE)
// ---------------------------------------------------------------------------

/**
 * The JSON body sent when saving a favorite.
 * Retrofit + Gson serializes this into: { "fromCurrency": "USD", "toCurrency": "ILS" }
 * Field names must match what the Flask backend expects.
 */
data class AddFavoriteRequest(
    val fromCurrency: String,
    val toCurrency: String
)

/**
 * Generic response returned by the backend for write operations
 * (add favorite, delete favorite).
 * Example JSON: { "message": "Favorite added successfully" }
 */
data class MessageResponse(
    val message: String = ""
)
