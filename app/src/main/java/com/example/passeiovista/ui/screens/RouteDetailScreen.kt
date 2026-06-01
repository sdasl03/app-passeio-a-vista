package com.example.passeiovista.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.passeiovista.data.entity.Route
import com.example.passeiovista.data.model.RoutePoiWithPoi
import com.example.passeiovista.data.repositories.RouteRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

sealed interface RouteDetailUiState {
    data object Loading : RouteDetailUiState
    data class Data(val route: Route?) : RouteDetailUiState
    data class Error(val message: String) : RouteDetailUiState
}

@Composable
fun RouteDetailScreen(
    routeRepository: RouteRepository,
    routeId: Long,
    userId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val routeUiState by produceState<RouteDetailUiState>(
        initialValue = RouteDetailUiState.Loading,
        key1 = routeRepository,
        key2 = routeId
    ) {
        value = try {
            RouteDetailUiState.Data(routeRepository.getRoute(routeId))
        } catch (t: Throwable) {
            RouteDetailUiState.Error(t.message ?: "Erro ao carregar roteiro")
        }
    }

    val routePois by routeRepository.getRouteWithPois(routeId).collectAsState(initial = emptyList())

    Column(modifier = modifier) {
        ListItem(
            headlineContent = {
                Text(
                    text = when (routeUiState) {
                        is RouteDetailUiState.Data -> (routeUiState as RouteDetailUiState.Data).route?.name
                            ?: "Roteiro"

                        is RouteDetailUiState.Error -> "Roteiro"
                        RouteDetailUiState.Loading -> "Roteiro"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            },
            leadingContent = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                }
            },
            trailingContent = {
                IconButton(
                    onClick = {
                        scope.launch {
                            routeRepository.deleteRoute(routeId = routeId, userId = userId)
                            onBack()
                        }
                    }
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Apagar")
                }
            }
        )
        HorizontalDivider()

        when (routeUiState) {
            RouteDetailUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("A carregar…")
                }
            }

            is RouteDetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ocorreu um erro", style = MaterialTheme.typography.titleMedium)
                    Text((routeUiState as RouteDetailUiState.Error).message)
                    Spacer(Modifier.width(0.dp))
                    Button(onClick = onBack) { Text("Voltar") }
                }
            }

            is RouteDetailUiState.Data -> {
                val route = (routeUiState as RouteDetailUiState.Data).route
                if (route == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Roteiro não encontrado", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = onBack) { Text("Voltar") }
                    }
                } else {
                    RouteMeta(route = route)
                    HorizontalDivider()
                    RoutePoisList(routePois = routePois)
                }
            }
        }
    }
}

@Composable
private fun RouteMeta(
    route: Route,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "${route.totalEstimatedMinutes} min • ${formatKm(route.totalDistanceMeters)}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Criado: ${formatDate(route.createdAt)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RoutePoisList(
    routePois: List<RoutePoiWithPoi>,
    modifier: Modifier = Modifier
) {
    if (routePois.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Sem POIs neste roteiro.", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = routePois,
            key = { "${it.routePoi.routeId}_${it.routePoi.poiId}" }
        ) { item ->
            ListItem(
                headlineContent = { Text(item.name) },
                supportingContent = {
                    Text(
                        text = "Paragem: ${item.routePoi.estimatedStopTime} min • ${item.latitude}, ${item.longitude}"
                    )
                }
            )
            HorizontalDivider()
        }
    }
}

private fun formatKm(distanceMeters: Double): String {
    val km = distanceMeters / 1000.0
    return "${"%.1f".format(km)} km"
}

private fun formatDate(value: LocalDateTime): String {
    return value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}
