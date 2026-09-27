package com.kampplus.ufuk.feature.forecast.domain.repository

import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.domain.model.ForecastSnapshot
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import kotlinx.coroutines.flow.Flow

/**
 * Hava verisinin tek giriş noktası. Tahmin "önce önbellek" çalışır: ekran her zaman önbelleği
 * izler, yenileme yalnızca önbelleği günceller. Ağ düşse de son tahmin ekranda kalır.
 */
interface ForecastRepository {
    /** Yerin önbellekteki tahmini; hiç alınmamışsa null. Yenilendikçe yeni değer yayınlar. */
    fun observeForecast(place: Place): Flow<ForecastSnapshot?>

    /** Tahmini ağdan alıp önbelleğe yazar. Başarısızlıkta önbelleğe dokunmaz. */
    suspend fun refreshForecast(place: Place): AppResult<Unit>

    /**
     * Yerlerin anlık durumu, tek istekte. Ağ yoksa önbellekte tahmini olan yerler için son bilinen
     * durum döner ([PlaceConditions.isFromCache]); hiçbirinin önbelleği yoksa hata döner.
     */
    suspend fun getConditions(places: List<Place>): AppResult<List<PlaceConditions>>
}
