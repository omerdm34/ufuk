package com.kampplus.ufuk.feature.places.data.remote

import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.data.remote.api.OpenMeteoGeocodingApi
import javax.inject.Inject

interface PlaceRemoteDataSource {
    suspend fun search(query: String, language: String): List<Place>
}

/**
 * Open-Meteo geocoding. Yalnızca yerleşim yerleri (GeoNames PPL*) döner: "Ankara" araması
 * havalimanını, "Uludağ" araması dağ zirvesini şehir gibi listelemez.
 */
class OpenMeteoPlaceRemoteDataSource @Inject constructor(
    private val api: OpenMeteoGeocodingApi
) : PlaceRemoteDataSource {
    override suspend fun search(query: String, language: String): List<Place> = api.search(name = query, language = language)
        .results
        .filter { it.featureCode == null || it.featureCode.startsWith(POPULATED_PLACE) }
        .take(MAX_RESULTS)
        .map { dto ->
            Place(
                id = dto.id,
                name = dto.name,
                region = dto.region,
                country = dto.country,
                coordinates = Coordinates(latitude = dto.latitude, longitude = dto.longitude)
            )
        }

    private companion object {
        const val POPULATED_PLACE = "PPL"
        const val MAX_RESULTS = 10
    }
}
