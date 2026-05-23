package com.example.passeiovista.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.passeiovista.data.model.FavoriteWithLocation
import com.example.passeiovista.ui.FavoritesUiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesSheet(
    state: FavoritesUiState,
    onDismissRequest: () -> Unit,
    onCreateRouteClick: (String, List<FavoriteWithLocation>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPoiIds by remember { mutableStateOf(setOf<String>()) }
    var routeName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Favoritos",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            leadingContent = {
                Icon(Icons.Outlined.Favorite, contentDescription = null)
            },
            trailingContent = {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Outlined.Close, contentDescription = "Fechar")
                }
            }
        )
        HorizontalDivider()

        when (state) {
            FavoritesUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("A carregar…")
                }
            }

            is FavoritesUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Ocorreu um erro", style = MaterialTheme.typography.titleMedium)
                    Text(text = state.message, style = MaterialTheme.typography.bodyMedium)
                }
            }

            is FavoritesUiState.Data -> {
                if (state.favorites.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Ainda não tens favoritos.", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Adiciona POIs aos favoritos para acesso rápido.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(1f, fill = false)
                    ) {
                        OutlinedTextField(
                            value = routeName,
                            onValueChange = { routeName = it },
                            label = { Text("Nome do Roteiro (Opcional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = state.favorites,
                                key = { it.id }
                            ) { favorite ->
                                val isSelected = selectedPoiIds.contains(favorite.id)

                                FavoriteRow(
                                    favorite = favorite,
                                    isSelected = isSelected,
                                    onToggle = {
                                        selectedPoiIds = if (isSelected) selectedPoiIds - favorite.id else selectedPoiIds + favorite.id
                                    }
                                )
                                HorizontalDivider()
                            }
                        }

                        val canCreate = selectedPoiIds.size >= 2
                        Button(
                            onClick = {
                                onCreateRouteClick(
                                    routeName.trim(),
                                    state.favorites.filter { selectedPoiIds.contains(it.id) }
                                )
                            },
                            enabled = canCreate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            Icon(Icons.Filled.DirectionsWalk, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (canCreate) "Criar Roteiro (${selectedPoiIds.size} locais)" else "Seleciona 2+ locais")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: FavoriteWithLocation,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable { onToggle() },
        headlineContent = { Text(favorite.name) },
        supportingContent = {
            Text(text = "Adicionado: ${favorite.createdAt?.let { formatDateTime(it) } ?: "—"}")
        },
        leadingContent = {
            Checkbox(checked = isSelected, onCheckedChange = null)
        }
    )
}

private fun formatDateTime(epochMillis: Long): String {
    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
    return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}