package com.example.passeiovista

import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.model.FavoriteWithLocation
import com.example.passeiovista.data.repositories.FavoriteRepository
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FavoriteRepositoryTest {
    @Test
    fun toggleFavorite_addThenRemove_updatesFlow() = runTest {
        val dao = FakeFavoriteDao()
        val repo = FavoriteRepository(dao)

        val userId = "u1"
        val poiId = "p1"

        assertThat(repo.getFavoritePoiIds(userId).first()).doesNotContain(poiId)

        val afterAdd = repo.toggleFavorite(userId = userId, poiId = poiId)
        assertThat(afterAdd).isTrue()
        assertThat(repo.getFavoritePoiIds(userId).first()).contains(poiId)

        val afterRemove = repo.toggleFavorite(userId = userId, poiId = poiId)
        assertThat(afterRemove).isFalse()
        assertThat(repo.getFavoritePoiIds(userId).first()).doesNotContain(poiId)
    }
}

private class FakeFavoriteDao : FavoriteDao {
    private val items = MutableStateFlow<List<Favorite>>(emptyList())

    override suspend fun insertFavorite(favorite: Favorite): Long {
        items.value = items.value.filterNot { it.id == favorite.id } + favorite
        return 1L
    }

    override suspend fun deleteFavorite(favorite: Favorite): Int {
        val before = items.value.size
        items.value = items.value.filterNot { it.id == favorite.id }
        return before - items.value.size
    }

    override suspend fun deleteFavorite(userId: String, poiId: String): Int {
        val before = items.value.size
        items.value = items.value.filterNot { it.userId == userId && it.poiId == poiId }
        return before - items.value.size
    }

    override suspend fun deleteAllFavorites(userId: String) {
        items.value = items.value.filterNot { it.userId == userId }
    }

    override fun getFavoritesByUser(userId: String): Flow<List<Favorite>> =
        items.map { list ->
            list.filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
        }

    override suspend fun getFavorite(userId: String, poiId: String): Favorite? {
        return items.value.firstOrNull { it.userId == userId && it.poiId == poiId }
    }

    override fun getFavoritesWithLocation(userId: String): Flow<List<FavoriteWithLocation>> {
        return items.map { emptyList() }
    }
}
