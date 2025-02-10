package com.unirfp.myapplication

import com.google.gson.annotations.SerializedName
import java.io.Serial

// Mapeo de los detalles de pokémon
data class Pokemon(
    val sprites: Sprites,
    val name: String,
    val types: List<Types>,
    val stats: List<Stats>
)

data class Sprites(
    val other: Other
)

data class Other(
    @SerializedName("official-artwork") val officialArtwork: Artwork
)

data class Artwork(
    @SerializedName("front_default") val frontDefault: String
)

data class Types(
    val type: Type
)

data class Type(
    val name: String
)

data class Stats(
    @SerializedName("base_stat") val baseStat: Int,
    val stat: Stat
)

data class Stat(
    val name: String
)