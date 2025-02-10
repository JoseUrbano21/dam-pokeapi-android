package com.unirfp.myapplication

// Mapeo de la lista de pokémon completa
data class PokemonListResponse(
    val results: List<PokemonResult>
)

// Mapeo de cada elemento de la lista
data class PokemonResult(
    val name: String,
    val url: String
)