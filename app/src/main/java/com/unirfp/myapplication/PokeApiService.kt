package com.unirfp.myapplication

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

// Interfaz de la API para definir los métodos para acceder a los datos de la API (endpoints)
interface PokeApiService {

    // Anotación GET para hacer la llamada http al endpoint "pokemon" que se añadirá a la URL base
    @GET("pokemon?limit=100000&offset=0")
    suspend fun getPokemon(): Response<PokemonListResponse>

    // Para obtener detalles no añadimos un endpoint fijo, sino que se pasará por parámetro
    @GET
    suspend fun getPokemonDetails(@Url url: String): Response<Pokemon>
}