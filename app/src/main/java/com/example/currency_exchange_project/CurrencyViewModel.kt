package com.example.currency_exchange_project

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencysdk.CurrencySdk
import com.example.currencysdk.models.ConversionResponse
import com.example.currencysdk.models.Currency
import com.example.currencysdk.models.FavoriteItem
import com.example.currencysdk.models.HistoryItem
import com.example.currencysdk.models.RateResponse
import kotlinx.coroutines.launch

private const val TAG = "CurrencyViewModel"

/**
 * CurrencyViewModel holds all state for both screens.
 *
 * Because it is a ViewModel it survives screen rotations, and because
 * both composables share the same Activity they both get the same
 * ViewModel instance — so navigation state is always in sync.
 */
class CurrencyViewModel : ViewModel() {

    private val sdk = CurrencySdk()

    // ── Navigation ────────────────────────────────────────────────────────────
    // Simple flag: false = Converter screen, true = Favorites & History screen.
    var showFavoritesAndHistory by mutableStateOf(false)
        private set

    fun navigateToFavoritesHistory() {
        showFavoritesAndHistory = true
        // Refresh data every time the user opens the second screen
        loadFavoritesAndHistory()
    }

    fun navigateBack() {
        showFavoritesAndHistory = false
    }

    // ── Currencies list (loaded once on startup) ──────────────────────────────

    var currencyList by mutableStateOf<List<Currency>>(emptyList())
        private set

    var isLoadingCurrencies by mutableStateOf(false)
        private set

    // ── User selections on the Converter screen ───────────────────────────────

    var fromCurrency by mutableStateOf("USD")
        private set

    var toCurrency by mutableStateOf("ILS")
        private set

    /** The amount typed by the user, kept as a String so partial input like "10." works. */
    var amount by mutableStateOf("")
        private set

    // ── Conversion results ────────────────────────────────────────────────────

    var exchangeRate by mutableStateOf<RateResponse?>(null)
        private set

    var conversionResult by mutableStateOf<ConversionResponse?>(null)
        private set

    var isConverting by mutableStateOf(false)
        private set

    /** Error shown on the Converter screen. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // ── Favorites ─────────────────────────────────────────────────────────────

    var favoritesList by mutableStateOf<List<FavoriteItem>>(emptyList())
        private set

    var isSavingFavorite by mutableStateOf(false)
        private set

    // ── History ───────────────────────────────────────────────────────────────

    var historyList by mutableStateOf<List<HistoryItem>>(emptyList())
        private set

    /** True while favorites or history are being fetched. */
    var isLoadingFavoritesHistory by mutableStateOf(false)
        private set

    // ── Startup ───────────────────────────────────────────────────────────────

    init {
        loadCurrencies()
    }

    // ── Converter screen actions ──────────────────────────────────────────────

    fun onFromCurrencySelected(code: String) {
        fromCurrency = code
        // Clear stale results when the pair changes
        exchangeRate = null
        conversionResult = null
    }

    fun onToCurrencySelected(code: String) {
        toCurrency = code
        exchangeRate = null
        conversionResult = null
    }

    fun onAmountChanged(value: String) {
        amount = value
    }

    fun loadCurrencies() {
        viewModelScope.launch {
            isLoadingCurrencies = true
            errorMessage = null
            try {
                val response = sdk.getSupportedCurrencies()
                currencyList = response.currencies
                Log.d(TAG, "Currencies loaded: ${currencyList.map { it.code }}")
            } catch (e: Exception) {
                errorMessage = "Failed to load currencies: ${e.message}"
                Log.e(TAG, "loadCurrencies failed", e)
            } finally {
                isLoadingCurrencies = false
            }
        }
    }

    /**
     * Calls /rates then /convert with the currently selected pair and amount.
     * The backend automatically saves a history entry when /convert is called.
     */
    fun convert() {
        val amountDouble = amount.toDoubleOrNull()
        if (amountDouble == null) {
            errorMessage = "Please enter a valid number"
            return
        }
        viewModelScope.launch {
            isConverting = true
            errorMessage = null
            try {
                exchangeRate = sdk.getExchangeRate(fromCurrency, toCurrency)
                Log.d(TAG, "Rate: 1 $fromCurrency = ${exchangeRate?.rate} $toCurrency")

                conversionResult = sdk.convertAmount(fromCurrency, toCurrency, amountDouble)
                Log.d(TAG, "Converted: $amountDouble $fromCurrency = ${conversionResult?.convertedAmount} $toCurrency")
            } catch (e: Exception) {
                errorMessage = "Conversion failed: ${e.message}"
                Log.e(TAG, "convert() failed", e)
            } finally {
                isConverting = false
            }
        }
    }

    /**
     * Saves the current currency pair (fromCurrency → toCurrency) as a favorite.
     * A failure here is logged but never shown as a crash or blocking error —
     * the convert flow must keep working even if MongoDB is unavailable.
     */
    fun saveFavorite() {
        viewModelScope.launch {
            isSavingFavorite = true
            try {
                sdk.addFavorite(fromCurrency, toCurrency)
                Log.d(TAG, "Saved favorite: $fromCurrency → $toCurrency")
            } catch (e: Exception) {
                // Silent fail: favorites are a bonus feature, not critical path
                Log.e(TAG, "saveFavorite failed", e)
            } finally {
                isSavingFavorite = false
            }
        }
    }

    // ── Favorites & History screen actions ────────────────────────────────────

    /**
     * Loads favorites and history from the backend.
     * Each call is in its own try-catch so one failure doesn't block the other.
     */
    fun loadFavoritesAndHistory() {
        viewModelScope.launch {
            isLoadingFavoritesHistory = true

            // Favorites
            try {
                val response = sdk.getFavorites()
                favoritesList = response.favorites
                Log.d(TAG, "Favorites loaded: ${favoritesList.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "loadFavorites failed", e)
                // Keep whatever list we had before; don't crash
            }

            // History
            try {
                val response = sdk.getHistory()
                historyList = response.history
                Log.d(TAG, "History loaded: ${historyList.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "loadHistory failed", e)
            }

            isLoadingFavoritesHistory = false
        }
    }

    /**
     * Deletes a favorite by its MongoDB ID.
     * Uses an optimistic update: removes from the local list immediately
     * so the UI feels instant, even before the server confirms.
     */
    fun deleteFavorite(id: String) {
        // Optimistic update — remove from UI right away
        favoritesList = favoritesList.filter { it.id != id }
        viewModelScope.launch {
            try {
                sdk.deleteFavorite(id)
                Log.d(TAG, "Deleted favorite: $id")
            } catch (e: Exception) {
                // If the delete failed, reload so the list is accurate again
                Log.e(TAG, "deleteFavorite failed, reloading", e)
                loadFavoritesAndHistory()
            }
        }
    }

    /**
     * Applies a saved favorite to the converter and navigates back.
     * Called when the user taps "Use" on a favorite row.
     */
    fun useFavorite(favorite: FavoriteItem) {
        fromCurrency = favorite.fromCurrency
        toCurrency = favorite.toCurrency
        // Clear stale results so the UI doesn't show old data for the new pair
        exchangeRate = null
        conversionResult = null
        showFavoritesAndHistory = false
    }
}
