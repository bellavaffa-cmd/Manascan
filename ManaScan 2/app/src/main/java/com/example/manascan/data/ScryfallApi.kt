package com.example.manascan.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Thin wrapper around the public Scryfall REST API (https://scryfall.com/docs/api).
 * No API key is required, but Scryfall asks integrations to send an identifying
 * User-Agent and Accept header, which is added in [NetworkModule].
 */
interface ScryfallApi {

    /**
     * Fuzzy name lookup - tolerant of typos and partial names (e.g. OCR output).
     * Returns a single best-match card, or HTTP 404 if nothing is close enough.
     */
    @GET("cards/named")
    suspend fun getCardByFuzzyName(@Query("fuzzy") name: String): ScryfallCard

    /** Exact name lookup, used when the user picks a suggestion verbatim. */
    @GET("cards/named")
    suspend fun getCardByExactName(@Query("exact") name: String): ScryfallCard

    /** Returns up to 20 card name completions for a partial query, for manual search. */
    @GET("cards/autocomplete")
    suspend fun autocomplete(@Query("q") query: String): ScryfallAutocompleteResponse

    /** Full-text search, used as a fallback when fuzzy name lookup fails outright. */
    @GET("cards/search")
    suspend fun search(@Query("q") query: String): ScryfallSearchResponse
}
