package com.kampplus.ufuk.feature.places.data.catalog

import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place
import javax.inject.Inject

/** Keşfet ekranında öne çıkan yerler. Farklı bir liste (ör. Avrupa başkentleri) = yeni implementasyon. */
fun interface FeaturedPlaceCatalog {
    fun places(): List<Place>
}

/**
 * Türkiye'nin büyük şehirleri. Kimlikler ve koordinatlar Open-Meteo geocoding sonuçlarıyla aynıdır;
 * Keşfet'ten kaydedilen şehir aramada da "kayıtlı" görünür.
 */
class TurkishCityCatalog @Inject constructor() : FeaturedPlaceCatalog {
    override fun places(): List<Place> = CITIES

    private companion object {
        val CITIES = listOf(
            city(745044, "İstanbul", 41.0138, 28.9497),
            city(323786, "Ankara", 39.9199, 32.8543),
            city(311046, "İzmir", 38.4127, 27.1384),
            city(750269, "Bursa", 40.1956, 29.0601),
            city(323777, "Antalya", 36.9081, 30.6956),
            city(325363, "Adana", 36.9862, 35.3253),
            city(306571, "Konya", 37.8713, 32.4846),
            city(314830, "Gaziantep", 37.0594, 37.3825),
            city(298333, "Şanlıurfa", 37.1671, 38.7939),
            city(745028, "İzmit", 40.7650, 29.9293, region = "Kocaeli"),
            city(304531, "Mersin", 36.8120, 34.6389),
            city(316541, "Diyarbakır", 37.9136, 40.2172),
            city(308464, "Kayseri", 38.7322, 35.4853),
            city(315202, "Eskişehir", 39.7767, 30.5206),
            city(740264, "Samsun", 41.2798, 36.3361),
            city(738648, "Trabzon", 41.0050, 39.7269),
            city(315368, "Erzurum", 39.9086, 41.2769),
            city(298117, "Van", 38.4946, 43.3832),
            city(304922, "Malatya", 38.3502, 38.3167),
            city(317109, "Denizli", 37.7742, 29.0875)
        )

        fun city(id: Long, name: String, latitude: Double, longitude: Double, region: String = name) = Place(
            id = id,
            name = name,
            region = region,
            country = "Türkiye",
            coordinates = Coordinates(latitude = latitude, longitude = longitude)
        )
    }
}
