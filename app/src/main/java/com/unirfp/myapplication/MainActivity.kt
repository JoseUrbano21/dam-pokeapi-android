package com.unirfp.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.unirfp.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    // Variable para implementar la interfaz en la app
    private val pokeApiService = RetrofitClient.getPokeApiService()

    // Definimos variables dinámicas y las inicializamos
    private var pokemonList by mutableStateOf(emptyList<PokemonResult>())
    private var sprites by mutableStateOf<Sprites?>(null)
    private var name by mutableStateOf("")
    private var types by mutableStateOf(emptyList<Types>())
    private var stats by mutableStateOf(emptyList<Stats>())

    // Variable para guardar el texto de búsqueda para filtrar la lista
    private var searchQuery by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fetchPokemonList()
        setContent {
            MyApplicationTheme {
                if (name.isNotEmpty()) {
                    MostrarDetallesPokemon(
                        name, sprites, types, stats,
                        onBack = { name = "" }
                    )

                } else {
                    MostrarListaPokemon(
                        pokemonList = pokemonList,
                        onPokemonClick = { url -> fetchPokemonDetails(url) },
                        searchQuery = searchQuery,
                        onSearchQueryChanged = { newQuery -> searchQuery = newQuery }
                    )
                }
            }
        }
    }

    // Obtener la lista de pokémon de la API
    private fun fetchPokemonList() {
        // Ejecutamos un hilo en segundo plano para no bloquear el hilo principal
        CoroutineScope(Dispatchers.IO).launch {
            // Guardamos la respuesta de la API en una variable
            val response = pokeApiService.getPokemon()
            // Si obtenemos respuesta de la API, guardamos la lista obtenida en una variable
            if (response.isSuccessful) {
                val resultado = response.body()?.results ?: emptyList()
                // Cambiamos al entorno principal y guardamos la lista en la variable declarada al principio
                withContext(Dispatchers.Main) {
                    pokemonList = resultado
                }
            } else {
                // Si da error la API, guardamos el mensaje del error en una variable
                val error = response.errorBody()?.string() ?: "Error desconocido"
                // Mostramos el error, así como el código
                Log.e("Error API", "${response.code()}: $error")
            }
        }
    }

    // Obtener los detalles de pokémon de la API
    private fun fetchPokemonDetails(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = pokeApiService.getPokemonDetails(url)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val resultSprites = body.sprites
                    val resultName = body.name
                    val resultTypes = body.types
                    val resultStats = body.stats
                    withContext(Dispatchers.Main) {
                        sprites = resultSprites
                        name = resultName
                        types = resultTypes
                        stats = resultStats
                    }
                }
            } else {
                val error = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("Error API", "${response.code()}: $error")
            }
        }

    }

    // *************** FUNCIONES JETPACK COMPOSE (UI) ***************
    @Composable
    fun MostrarListaPokemon(
        // Pasamos como parámetros la lista de pokémon y una función para hacerlos clicables
        pokemonList: List<PokemonResult>,
        onPokemonClick: (String) -> Unit,
        searchQuery: String,
        onSearchQueryChanged: (String) -> Unit
    ) {
        Column {
            // Barra de búsqueda
            TextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    onSearchQueryChanged(newQuery)
                },
                label = { Text("Buscar Pokémon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
            )

            // Filtrar la lista de Pokémon
            val filteredList =
                pokemonList.filter { it.name.contains(searchQuery, ignoreCase = true) }

            // Componente de JPC para mostrar la lista
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Recorremos la lista, mostramos cada elemento y hacemos que sean clickables
                items(filteredList) { pokemon ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pokemon.name.uppercase(),
                            modifier = Modifier.clickable { onPokemonClick(pokemon.url) },
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }
        }
    }

    @Composable
    fun MostrarDetallesPokemon(
        name: String,
        sprites: Sprites?,
        types: List<Types>,
        stats: List<Stats>,
        onBack: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = name.uppercase())
                sprites?.other?.officialArtwork?.frontDefault?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = "",
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Fit
                    )
                } ?: Text(text = "Imagen no disponible")

                Text(text = "Types: ${types.joinToString { it.type.name.capitalize() }}")
                Text(text = "Stats:")
                stats.forEach { stat ->
                    Text(text = "${stat.stat.name.capitalize()}: ${stat.baseStat}")
                }

                Button(onClick = { onBack() }) {
                    Text("Volver")
                }
            }
        }
    }
}