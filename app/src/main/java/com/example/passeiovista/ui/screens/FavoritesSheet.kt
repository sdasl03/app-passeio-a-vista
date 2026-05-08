package com.example.passeiovista.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
) {
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
                    Text(
                        text = "Ocorreu um erro",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                        Text(
                            text = "Ainda não tens favoritos.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Adiciona POIs aos favoritos para acesso rápido.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = state.favorites,
                            key = { it.id }
                        ) { favorite ->
                            FavoriteRow(favorite = favorite)
                            HorizontalDivider()
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
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(favorite.name) },
        supportingContent = {
            Text(
                text = "Adicionado: ${favorite.createdAt?.let { formatDateTime(it) } ?: "—"}"
            )
        }
    )
}

private fun formatDateTime(epochMillis: Long): String {
    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
    return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}
