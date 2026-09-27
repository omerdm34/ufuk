package com.kampplus.ufuk.feature.places.data.remote.api

import com.kampplus.ufuk.feature.places.data.remote.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** https://geocoding-api.open-meteo.com/v1/ — ada göre konum arama. */
interface OpenMeteoGeocodingApi {
    @GET("search")
    suspend fun search(
        @Query("name") name: String,
        @Query("language") language: String,
        @Query("count") count: Int = 20,
        @Query("format") format: String = "json"
    ): GeocodingResponseDto
}
