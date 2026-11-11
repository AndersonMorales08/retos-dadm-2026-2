package com.example.interesmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reto9gps.Poi
import com.example.reto9gps.PoiCategory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import kotlin.time.Duration.Companion.milliseconds

data class UiState(
    val userLocation: GeoPoint? = null,
    val pois: List<Poi> = emptyList(),
    val categories: Set<PoiCategory> = setOf(PoiCategory.HEALTH, PoiCategory.TOURISM, PoiCategory.FOOD),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MapViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsRepository(app)
    private val overpass = NominatimRepository()

    val radiusKm: StateFlow<Float> = settings.radiusKm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2f)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var trackingJob: Job? = null
    private var lastSearchCenter: GeoPoint? = null

    // ---------------- Seguimiento del GPS ----------------
    fun startTracking() {
        if (trackingJob?.isActive == true) return
        _state.update { it.copy(error = null) }
        trackingJob = viewModelScope.launch {
            LocationProvider.updates(getApplication())
                .catch { e ->
                    _state.update { it.copy(error = "No se pudo obtener tu ubicación: ${e.message}") }
                }
                .collect { loc -> onNewLocation(GeoPoint(loc.latitude, loc.longitude)) }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    fun restartTracking() {
        stopTracking()
        lastSearchCenter = null   // fuerza una búsqueda con la próxima posición
        startTracking()
    }

    private suspend fun onNewLocation(point: GeoPoint) {
        _state.update { it.copy(userLocation = point) }

        // Nunca lanzar otra búsqueda mientras hay una en curso
        if (searchJob?.isActive == true) return

        val last = lastSearchCenter
        val thresholdM = maxOf(200.0, settings.radiusKm.first() * 1000.0 * 0.25)
        if (last == null || last.distanceToAsDouble(point) > thresholdM) search()
    }

    // ---------------- Búsqueda de lugares ----------------
    fun search() {
        val center = _state.value.userLocation ?: return
        lastSearchCenter = center
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(600.milliseconds)   // debounce: varios cambios seguidos producen una sola consulta
            _state.update { it.copy(isLoading = true, error = null) }
            val km = settings.radiusKm.first()
            try {
                val pois = overpass.nearby(
                    center.latitude, center.longitude,
                    (km * 1000).toInt(), _state.value.categories
                )
                _state.update { it.copy(pois = pois, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = "Error buscando lugares: ${e.message}")
                }
            }
        }
    }

    fun saveRadius(km: Float) {
        viewModelScope.launch {
            settings.setRadiusKm(km)
            search()
        }
    }

    fun toggleCategory(category: PoiCategory) {
        _state.update {
            val new = if (category in it.categories) it.categories - category else it.categories + category
            it.copy(categories = new)
        }
        search()
    }
}