package com.example.interesmap

import android.location.Location
import com.example.reto9gps.Poi
import com.example.reto9gps.PoiCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.cos

class NominatimRepository {

    private val cache = mutableMapOf<String, List<Poi>>()

    suspend fun nearby(
        lat: Double,
        lon: Double,
        radiusMeters: Int,
        categories: Set<PoiCategory>
    ): List<Poi> = withContext(Dispatchers.IO) {
        if (categories.isEmpty()) return@withContext emptyList()

        val key = "%.3f,%.3f,%d,%s".format(Locale.US, lat, lon, radiusMeters, categories.map { it.name }.sorted())
        cache[key]?.let { return@withContext it }

        // Rectángulo que contiene el círculo: izquierda-arriba y derecha-abajo (lon,lat)
        val dLat = radiusMeters / 111_320.0
        val dLon = radiusMeters / (111_320.0 * cos(Math.toRadians(lat)))
        val viewbox = String.format(
            Locale.US, "%.5f,%.5f,%.5f,%.5f",
            lon - dLon, lat + dLat, lon + dLon, lat - dLat
        )

        val found = LinkedHashMap<String, Poi>()
        val errors = mutableListOf<String>()
        var first = true

        for (cat in categories) {
            for (term in cat.searchTerms) {
                if (!first) delay(1_100)          // Nominatim permite máximo 1 petición por segundo
                first = false

                val url = "https://nominatim.openstreetmap.org/search" +
                        "?format=jsonv2&limit=40&bounded=1&viewbox=$viewbox" +
                        "&q=" + URLEncoder.encode(term, "UTF-8")
                try {
                    val (code, body) = get(url)
                    if (code != 200) {
                        errors += "HTTP $code ($term)"
                        continue
                    }
                    val arr = JSONArray(body)
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val name = o.optString("name").ifBlank { continue }
                        val pLat = o.getString("lat").toDouble()
                        val pLon = o.getString("lon").toDouble()

                        val d = FloatArray(1)
                        Location.distanceBetween(lat, lon, pLat, pLon, d)
                        if (d[0] > radiusMeters) continue   // fuera del círculo

                        val id = o.getString("osm_type") + o.getLong("osm_id")
                        found.putIfAbsent(id, Poi(id, name, pLat, pLon, cat))
                    }
                } catch (e: IOException) {
                    errors += "${e.javaClass.simpleName} ($term)"
                }
            }
        }

        if (found.isEmpty() && errors.isNotEmpty()) {
            error("Nominatim no respondió → " + errors.joinToString(" | "))
        }
        val result = found.values.toList()
        if (errors.isEmpty()) cache[key] = result   // no guardar resultados incompletos
        result
    }

    private fun get(url: String): Pair<Int, String> {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 20_000
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept-Language", "es")
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            return code to stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        } finally {
            conn.disconnect()
        }
    }

    private companion object {
        const val USER_AGENT = "Reto9GPS/1.0 (curso Moviles UNAL; amoralesch@unal.edu.co)"
    }
}