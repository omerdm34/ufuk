package com.kampplus.ufuk.feature.forecast.data.remote

import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastPayload
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastResponseDto

/**
 * Uzak hava verisi sözleşmesi. Önbelleğe yazılacağı için tahmin, domain modeli yerine
 * data katmanına ait [ForecastPayload] olarak döner; domain'e çeviri mapper'da yapılır.
 */
interface ForecastRemoteDataSource {
    suspend fun fetchForecast(coordinates: Coordinates): ForecastPayload

    /** Konum sırasıyla, her konum için anlık durum ve bugünün en düşük/en yüksek değeri. */
    suspend fun fetchConditions(coordinates: List<Coordinates>): List<ForecastResponseDto>
}
