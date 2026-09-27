package com.kampplus.ufuk.core.navigation

import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place
import kotlinx.serialization.Serializable

/** Type-safe navigasyon hedefleri. Argümanlar derleme zamanında denetlenir. */
@Serializable
data object MyPlacesDestination

@Serializable
data object ExploreDestination

@Serializable
data object SettingsDestination

/**
 * Tahmin ekranı. Yerin tamamı argüman olarak taşınır; böylece detay ekranı ek bir
 * "yeri getir" isteğine ihtiyaç duymaz ve cihaz konumu da aynı yoldan açılır.
 */
@Serializable
data class ForecastDestination(
    val placeId: Long,
    val name: String,
    val region: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double
) {
    fun toPlace() = Place(
        id = placeId,
        name = name,
        region = region,
        country = country,
        coordinates = Coordinates(latitude = latitude, longitude = longitude)
    )

    companion object {
        // SavedStateHandle anahtarları; property adlarıyla aynı olmalıdır.
        const val ARG_PLACE_ID = "placeId"
        const val ARG_NAME = "name"
        const val ARG_REGION = "region"
        const val ARG_COUNTRY = "country"
        const val ARG_LATITUDE = "latitude"
        const val ARG_LONGITUDE = "longitude"
    }
}

fun Place.toForecastDestination() = ForecastDestination(
    placeId = id,
    name = name,
    region = region,
    country = country,
    latitude = coordinates.latitude,
    longitude = coordinates.longitude
)
