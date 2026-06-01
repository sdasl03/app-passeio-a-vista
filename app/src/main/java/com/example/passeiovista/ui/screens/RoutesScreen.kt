package com.example.passeiovista.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.entity.Route
import com.example.passeiovista.data.repositories.RouteRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

sealed interface RoutesUiState {
    data object Loading : RoutesUiState
    data class Data(val routes: List<Route>) : RoutesUiState
    data class Error(val message: String) : RoutesUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    routeRepository: RouteRepository,
    allPois: List<Poi>,
    userId: String,
    onOpenRoute: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var showCreate by remember { mutableStateOf(false) }

    val routesUiState by produceState<RoutesUiState>(
        initialValue = RoutesUiState.Loading,
        key1 = routeRepository,
        key2 = userId
    ) {
        try {
            routeRepository.getRoutesByUser(userId).collect { routes ->
                value = RoutesUiState.Data(routes)
            }
        } catch (t: Throwable) {
            value = RoutesUiState.Error(t.message ?: "Erro ao carregar roteiros")
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "Novo roteiro")
            }
        }
    ) { padding ->
        when (routesUiState) {
            RoutesUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("A carregar…")
                }
            }

            is RoutesUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ocorreu um erro",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text((routesUiState as RoutesUiState.Error).message)
                }
            }

            is RoutesUiState.Data -> {
                val routes = (routesUiState as RoutesUiState.Data).routes
                if (routes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ainda não tens roteiros.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Cria um roteiro com os POIs que queres visitar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = routes,
                            key = { it.id }
                        ) { route ->
                            RouteRow(
                                route = route,
                                onClick = { onOpenRoute(route.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateRouteSheet(
            allPois = allPois,
            onDismissRequest = { showCreate = false },
            onCreate = { name, selected ->
                scope.launch {
                    val routeId = routeRepository.createRoute(
                        name = name,
                        userId = userId,
                        selectedPois = selected,
                        startLat = 41.1496,
                        startLon = -8.6109
                    )
                    showCreate = false
                    onOpenRoute(routeId)
                }
            }
        )
    }
}

@Composable
private fun RouteRow(
    route: Route,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = { Text(route.name) },
        supportingContent = {
            Text(
                text = "${route.totalEstimatedMinutes} min • ${formatKm(route.totalDistanceMeters)} • ${formatDate(route.createdAt)}"
            )
        },
        trailingContent = {
            IconButton(onClick = onClick) {
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Abrir")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRouteSheet(
    allPois: List<Poi>,
    onDismissRequest: () -> Unit,
    onCreate: (String, List<Poi>) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ListItem(
                headlineContent = { Text("Novo roteiro") },
                trailingContent = {
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Outlined.Close, contentDescription = "Fechar")
                    }
                }
            )
            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Nome do roteiro") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Seleciona POIs (${selected.size})",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(
                    items = allPois,
                    key = { it.id }
                ) { poi ->
                    val checked = selected.contains(poi.id)
                    ListItem(
                        headlineContent = { Text(poi.name) },
                        leadingContent = {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selected = if (isChecked) selected + poi.id else selected - poi.id
                                }
                            )
                        }
                    )
                }
            }

            error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (busy) return@Button
                        val trimmed = name.trim()
                        if (trimmed.isBlank()) {
                            error = "Indica um nome para o roteiro."
                            return@Button
                        }
                        val selectedPois = allPois.filter { selected.contains(it.id) }
                        if (selectedPois.isEmpty()) {
                            error = "Seleciona pelo menos um POI."
                            return@Button
                        }
                        busy = true
                        error = null
                        onCreate(trimmed, selectedPois)
                    }
                ) {
                    Text("Validar e criar")
                    if (busy) {
                        Spacer(Modifier.width(12.dp))
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    }
                }
            }
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
