package com.example.passeiovista.data.repositories

import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.entity.Poi
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TourismPoiRemoteRepository(
    private val poiDao: PoiDao
) {
    suspend fun refreshTourismPoisInBounds(
        south: Double,
        north: Double,
        west: Double,
        east: Double
    ): Int = withContext(Dispatchers.IO) {
        val query = buildOverpassQuery(
            south = south,
            west = west,
            north = north,
            east = east
        )

        val connection = (URL(OVERPASS_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            setRequestProperty("User-Agent", "passeio-a-vista/1.0")
        }

        val body = "data=" + java.net.URLEncoder.encode(query, "UTF-8")
        connection.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }

        val response = connection.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        val json = JSONObject(response)
        val elements = json.getJSONArray("elements")

        val now = LocalDateTime.now()
        val pois = mutableListOf<Poi>()

        for (i in 0 until elements.length()) {
            val el = elements.getJSONObject(i)
            val type = el.optString("type")
            val id = el.optLong("id")
            val lat = el.optDouble("lat", Double.NaN)
            val lon = el.optDouble("lon", Double.NaN)
            if (lat.isNaN() || lon.isNaN()) continue

            val tags = el.optJSONObject("tags") ?: JSONObject()
            val name = tags.optString("name").trim()
            val tourism = tags.optString("tourism").trim()
            val historic = tags.optString("historic").trim()

            val finalName = when {
                name.isNotBlank() -> name
                tourism.isNotBlank() -> tourism.replaceFirstChar { it.uppercase() }
                historic.isNotBlank() -> historic.replaceFirstChar { it.uppercase() }
                else -> "Ponto turístico"
            }

            val osmId = "osm_${type}_$id"
            val address = buildAddress(tags)
            val description = tags.optString("description").ifBlank { "" }

            pois.add(
                Poi(
                    id = osmId,
                    name = finalName,
                    latitude = lat,
                    longitude = lon,
                    description = description,
                    address = address,
                    accessibility = "unknown",
                    categoryId = TOURISM_CATEGORY_ID,
                    isOpenNow = false,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        poiDao.insertPois(pois)
        pois.size
    }

    private fun buildAddress(tags: JSONObject): String? {
        val city = tags.optString("addr:city").ifBlank { tags.optString("addr:locality") }
        val road = tags.optString("addr:street")
        val number = tags.optString("addr:housenumber")

        val line = listOf(road, number)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" ")

        return listOf(line, city)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { null }
    }

    private fun buildOverpassQuery(
        south: Double,
        west: Double,
        north: Double,
        east: Double
    ): String {
        return """
            [out:json][timeout:25];
            (
              node["tourism"~"attraction|museum|viewpoint|artwork|gallery|theme_park|zoo"]($south,$west,$north,$east);
              node["historic"~"monument|memorial|castle|archaeological_site"]($south,$west,$north,$east);
            );
            out body 120;
        """.trimIndent()
    }

    companion object {
        const val TOURISM_CATEGORY_ID = "cat_tourism"
        private const val OVERPASS_URL = "https://overpass-api.de/api/interpreter"
    }
}
