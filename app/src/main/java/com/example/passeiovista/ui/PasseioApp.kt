package com.example.passeiovista.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.example.passeiovista.data.model.FavoriteWithLocation
import com.example.passeiovista.data.repositories.FavoriteRepository
import com.example.passeiovista.data.repositories.PoiRepository
import com.example.passeiovista.data.repositories.RouteRepository
import com.example.passeiovista.ui.screens.FavoritesSheet
import com.example.passeiovista.ui.screens.MapScreen
import com.example.passeiovista.ui.screens.RouteDetailScreen
import com.example.passeiovista.ui.screens.RoutesScreen
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private object Destinations {
    const val Map = "map"
    const val Routes = "routes"
    const val RouteDetail = "route/{routeId}"

    fun routeDetail(routeId: Long): String = "route/$routeId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasseioApp(
    poiRepository: PoiRepository,
    favoriteRepository: FavoriteRepository,
    routeRepository: RouteRepository,
    userId: String,
    isOffline: StateFlow<Boolean>,
    forcedOffline: StateFlow<Boolean>,
    onToggleForcedOffline: () -> Unit,
    pendingSyncCount: Flow<Int>,
    modifier: Modifier = Modifier
) {
    val pois by poiRepository.getAllPois().collectAsState(initial = emptyList())
    val offline by isOffline.collectAsState()
    val forced by forcedOffline.collectAsState()
    val pendingCount by pendingSyncCount.collectAsState(initial = 0)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    var showFavorites by remember { mutableStateOf(false) }

    val favoritesUiState by produceState<FavoritesUiState>(
        initialValue = FavoritesUiState.Loading,
        key1 = favoriteRepository,
        key2 = userId
    ) {
        try {
            favoriteRepository.getFavoritesWithLocation(userId).collect { favorites ->
                value = FavoritesUiState.Data(favorites)
            }
        } catch (t: Throwable) {
            value = FavoritesUiState.Error(t.message ?: "Erro ao carregar favoritos")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            Destinations.Routes -> "Roteiros"
                            Destinations.RouteDetail -> "Roteiro"
                            else -> "Mapa"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (pendingCount > 0) {
                                Badge { Text(pendingCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onToggleForcedOffline) {
                            Icon(
                                imageVector = if (forced) Icons.Outlined.CloudOff else Icons.Outlined.CloudQueue,
                                contentDescription = "Modo offline"
                            )
                        }
                    }
                    if (currentRoute == Destinations.Map || currentRoute == null) {
                        IconButton(onClick = { showFavorites = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Favorite,
                                contentDescription = "Favoritos"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Destinations.Map || currentRoute == null,
                    onClick = {
                        navController.navigate(Destinations.Map) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Outlined.Map, contentDescription = null) },
                    label = { Text("Mapa") }
                )

                NavigationBarItem(
                    selected = currentRoute == Destinations.Routes || currentRoute == Destinations.RouteDetail,
                    onClick = {
                        navController.navigate(Destinations.Routes) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Outlined.Route, contentDescription = null) },
                    label = { Text("Roteiros") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (offline) {
                OfflineBanner(forcedOffline = forced, pendingCount = pendingCount)
            }

            NavHost(
                navController = navController,
                startDestination = Destinations.Map,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Destinations.Map) {
                    MapScreen(
                        pois = pois,
                        favoriteRepository = favoriteRepository,
                        userId = userId,
                        onUserLocationUpdated = {
                            // TODO: Guardar as coordenadas reais do GPS para usar no Roteiro
                        }
                    )
                }

                composable(Destinations.Routes) {
                    RoutesScreen(
                        routeRepository = routeRepository,
                        allPois = pois,
                        userId = userId,
                        onOpenRoute = { routeId ->
                            navController.navigate(Destinations.routeDetail(routeId))
                        }
                    )
                }

                composable(
                    Destinations.RouteDetail,
                    arguments = listOf(navArgument("routeId") { type = NavType.LongType })
                ) { entry ->
                    val routeId = entry.arguments?.getLong("routeId") ?: 0L
                    RouteDetailScreen(
                        routeRepository = routeRepository,
                        routeId = routeId,
                        userId = userId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    if (showFavorites) {
        FavoritesSheet(
            state = favoritesUiState,
            onDismissRequest = { showFavorites = false },
            onCreateRouteClick = { customName, selected ->
                scope.launch {
                    if (selected.isNotEmpty()) {
                        val finalName = customName.ifBlank {
                            val timeSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                            "Roteiro de $timeSuffix"
                        }

                        val poisList = pois.filter { dbPoi ->
                            selected.any { fav -> fav.poiId == dbPoi.id }
                        }

                        if (poisList.isNotEmpty()) {
                            routeRepository.createRoute(
                                name = finalName,
                                userId = userId,
                                selectedPois = poisList,
                                startLat = poisList[0].latitude,
                                startLon = poisList[0].longitude
                            )
                            showFavorites = false
                            navController.navigate(Destinations.Routes)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun OfflineBanner(
    forcedOffline: Boolean,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = if (forcedOffline) {
                if (pendingCount > 0) {
                    "Modo offline ativado. Favoritos e roteiros continuam disponíveis localmente. Pendentes de sincronização: $pendingCount."
                } else {
                    "Modo offline ativado. Favoritos e roteiros continuam disponíveis localmente."
                }
            } else {
                if (pendingCount > 0) {
                    "Sem ligação à Internet. Favoritos e roteiros continuam disponíveis localmente. Pendentes de sincronização: $pendingCount."
                } else {
                    "Sem ligação à Internet. Favoritos e roteiros continuam disponíveis localmente."
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
