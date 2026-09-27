package com.kampplus.ufuk.core.model

import java.util.Locale

/**
 * Özellikler arasında paylaşılan çekirdek model (shared kernel). Yerler özelliği yerleri yönetir,
 * tahmin özelliği yalnızca koordinatları kullanır; ikisi de bu tipi tanır, birbirini tanımaz.
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Önbellek anahtarı. İki ondalık basamak ~1 km'lik bir hücredir: aynı şehrin arama sonucu ile
     * cihaz konumu aynı tahmini paylaşır, konumdaki küçük oynamalar önbelleği bölmez.
     */
    val cacheKey: String get() = String.format(Locale.ROOT, "%.2f,%.2f", latitude, longitude)
}

/**
 * Hava durumuna bakılabilen bir yer. [id] geocoding servisinin kimliğidir; cihaz konumu
 * [DEVICE_LOCATION_ID] ile temsil edilir ve adı ters coğrafi kodlamadan gelir (bulunamazsa boş).
 */
data class Place(
    val id: Long,
    val name: String,
    val region: String?,
    val country: String?,
    val coordinates: Coordinates
) {
    val isDeviceLocation: Boolean get() = id == DEVICE_LOCATION_ID

    companion object {
        const val DEVICE_LOCATION_ID = -1L
    }
}
