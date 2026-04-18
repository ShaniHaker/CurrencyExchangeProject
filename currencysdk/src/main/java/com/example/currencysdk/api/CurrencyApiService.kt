package com.example.currencysdk.api

import com.example.currencysdk.models.AddFavoriteRequest
import com.example.currencysdk.models.ConversionResponse
import com.example.currencysdk.models.CurrenciesResponse
import com.example.currencysdk.models.FavoritesResponse
import com.example.currencysdk.models.HistoryResponse
import com.example.currencysdk.models.MessageResponse
import com.example.currencysdk.models.RateResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * CurrencyApiService defines every HTTP call to the Flask backend.
 *
 * Retrofit reads these annotations at runtime and generates the actual
 * networking code — no URL-building or JSON parsing by hand.
 *
 * Every function is "suspend" so it can be called from a coroutine
 * without blocking the main thread.
 */
interface CurrencyApiService {

    // ── READ endpoints ────────────────────────────────────────────────────────

    /**
     * GET /currencies
     * Returns the list of currencies the API supports.
     */
    @GET("currencies")
    suspend fun getCurrencies(): CurrenciesResponse

    /**
     * GET /rates?from=USD&to=ILS
     * Returns the exchange rate between two currencies.
     */
    @GET("rates")
    suspend fun getRates(
        @Query("from") from: String,
        @Query("to") to: String
    ): RateResponse

    /**
     * GET /convert?from=USD&to=ILS&amount=100
     * Converts an amount and (on the backend) saves a history entry.
     */
    @GET("convert")
    suspend fun convert(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double
    ): ConversionResponse

    /**
     * GET /favorites
     * Returns all saved favorite currency pairs from MongoDB.
     */
    @GET("favorites")
    suspend fun getFavorites(): FavoritesResponse

    /**
     * GET /history
     * Returns all past conversions stored in MongoDB.
     */
    @GET("history")
    suspend fun getHistory(): HistoryResponse

    // ── WRITE endpoints ───────────────────────────────────────────────────────

    /**
     * POST /favorites
     * Saves a new favorite currency pair to MongoDB.
     *
     * @Body tells Retrofit to serialize the [AddFavoriteRequest] object
     * as a JSON body: { "from": "USD", "to": "ILS" }
     */
    @POST("favorites")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): MessageResponse

    /**
     * DELETE /favorites/{id}
     * Deletes a favorite by its MongoDB document ID.
     *
     * @Path("id") inserts the id value into the URL, e.g. /favorites/507f1f77bcf86cd799439011
     */
    @DELETE("favorites/{id}")
    suspend fun deleteFavorite(@Path("id") id: String): MessageResponse
}
