package com.example.manascan.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models mirroring the subset of the Scryfall card object we need.
 * Full schema: https://scryfall.com/docs/api/cards
 */

@JsonClass(generateAdapter = true)
data class ScryfallCard(
    val id: String,
    val name: String,
    @Json(name = "mana_cost") val manaCost: String? = null,
    val cmc: Double? = null,
    @Json(name = "type_line") val typeLine: String? = null,
    @Json(name = "oracle_text") val oracleText: String? = null,
    @Json(name = "flavor_text") val flavorText: String? = null,
    val power: String? = null,
    val toughness: String? = null,
    val loyalty: String? = null,
    val colors: List<String>? = null,
    @Json(name = "color_identity") val colorIdentity: List<String>? = null,
    val set: String? = null,
    @Json(name = "set_name") val setName: String? = null,
    val rarity: String? = null,
    val artist: String? = null,
    @Json(name = "collector_number") val collectorNumber: String? = null,
    val legalities: Map<String, String>? = null,
    @Json(name = "image_uris") val imageUris: ImageUris? = null,
    @Json(name = "card_faces") val cardFaces: List<CardFace>? = null,
    val prices: Prices? = null,
    @Json(name = "scryfall_uri") val scryfallUri: String? = null
) {
    /** Best available display image, falling back to the first card face for MDFCs/split cards. */
    val displayImageUrl: String?
        get() = imageUris?.normal
            ?: imageUris?.large
            ?: cardFaces?.firstOrNull()?.imageUris?.normal

    val displayTypeLine: String?
        get() = typeLine ?: cardFaces?.firstOrNull()?.typeLine

    val displayOracleText: String?
        get() = oracleText ?: cardFaces?.joinToString("\n\n") { face ->
            "${face.name}\n${face.oracleText.orEmpty()}"
        }

    val displayManaCost: String?
        get() = manaCost?.takeIf { it.isNotBlank() }
            ?: cardFaces?.mapNotNull { it.manaCost }?.joinToString(" // ")
}

@JsonClass(generateAdapter = true)
data class CardFace(
    val name: String,
    @Json(name = "mana_cost") val manaCost: String? = null,
    @Json(name = "type_line") val typeLine: String? = null,
    @Json(name = "oracle_text") val oracleText: String? = null,
    val power: String? = null,
    val toughness: String? = null,
    @Json(name = "image_uris") val imageUris: ImageUris? = null
)

@JsonClass(generateAdapter = true)
data class ImageUris(
    val small: String? = null,
    val normal: String? = null,
    val large: String? = null,
    val png: String? = null,
    @Json(name = "art_crop") val artCrop: String? = null,
    @Json(name = "border_crop") val borderCrop: String? = null
)

@JsonClass(generateAdapter = true)
data class Prices(
    val usd: String? = null,
    @Json(name = "usd_foil") val usdFoil: String? = null,
    @Json(name = "usd_etched") val usdEtched: String? = null,
    val eur: String? = null,
    val tix: String? = null
)

@JsonClass(generateAdapter = true)
data class ScryfallSearchResponse(
    val data: List<ScryfallCard> = emptyList(),
    @Json(name = "total_cards") val totalCards: Int = 0,
    @Json(name = "has_more") val hasMore: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ScryfallAutocompleteResponse(
    val data: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ScryfallError(
    val status: Int? = null,
    val code: String? = null,
    val details: String? = null
)
