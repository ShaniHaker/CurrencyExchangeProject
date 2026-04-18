package com.example.currencysdk

import com.example.currencysdk.api.RetrofitClient
import com.example.currencysdk.models.AddFavoriteRequest
import com.example.currencysdk.models.ConversionResponse
import com.example.currencysdk.models.CurrenciesResponse
import com.example.currencysdk.models.FavoritesResponse
import com.example.currencysdk.models.HistoryResponse
import com.example.currencysdk.models.MessageResponse
import com.example.currencysdk.models.RateResponse

/**
 * CurrencySdk is the main entry point for the Currency Exchange SDK.
 *
 * Create one instance and call its suspend functions from a coroutine.
 *
 * Example:
 * ```
 * val sdk = CurrencySdk()
 * viewModelScope.launch {
 *     val rate   = sdk.getExchangeRate("USD", "ILS")
 *     val result = sdk.convertAmount("USD", "ILS", 100.0)
 *     sdk.addFavorite("USD", "ILS")
 * }
 * ```
 */
class CurrencySdk {

    private val api = RetrofitClient.apiService

    // ── Read operations ───────────────────────────────────────────────────────

    /** Returns all currencies supported by the Flask API. */
    suspend fun getSupportedCurrencies(): CurrenciesResponse {
        return api.getCurrencies()
    }

    /** Returns the current exchange rate between [from] and [to]. */
    suspend fun getExchangeRate(from: String, to: String): RateResponse {
        return api.getRates(from = from, to = to)
    }

    /**
     * Converts [amount] from [from] to [to].
     * The backend automatically saves a history entry when this is called.
     */
    suspend fun convertAmount(from: String, to: String, amount: Double): ConversionResponse {
        return api.convert(from = from, to = to, amount = amount)
    }

    /** Returns all saved favorite currency pairs from MongoDB. */
    suspend fun getFavorites(): FavoritesResponse {
        return api.getFavorites()
    }

    /** Returns all past conversions stored in MongoDB. */
    suspend fun getHistory(): HistoryResponse {
        return api.getHistory()
    }

    // ── Write operations ──────────────────────────────────────────────────────

    /**
     * Saves a new favorite currency pair ([from] → [to]) to MongoDB.
     * Returns a message confirming the operation.
     */
    suspend fun addFavorite(from: String, to: String): MessageResponse {
        // Field names in AddFavoriteRequest must match what the backend expects
        return api.addFavorite(AddFavoriteRequest(fromCurrency = from, toCurrency = to))
    }

    /**
     * Deletes the favorite with the given MongoDB document [id].
     * Returns a message confirming the operation.
     */
    suspend fun deleteFavorite(id: String): MessageResponse {
        return api.deleteFavorite(id = id)
    }
}
