package com.kampplus.ufuk.testing

import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place

fun place(
    id: Long = 323786,
    name: String = "Ankara",
    region: String? = "Ankara",
    country: String? = "Türkiye",
    latitude: Double = 39.92,
    longitude: Double = 32.85
) = Place(id = id, name = name, region = region, country = country, coordinates = Coordinates(latitude, longitude))

val ankara = place()
val istanbul = place(id = 745044, name = "İstanbul", region = "İstanbul", latitude = 41.01, longitude = 28.95)
val izmir = place(id = 311046, name = "İzmir", region = "İzmir", latitude = 38.41, longitude = 27.14)
