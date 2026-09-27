package com.kampplus.ufuk.feature.places.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.kampplus.ufuk.core.model.Coordinates
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

data class PlaceName(
    val name: String,
    val region: String?,
    val country: String?
)

/** Koordinattan yer adı. Bulunamazsa null; ekran bu durumda "Konumum" yazar. */
fun interface ReverseGeocoder {
    suspend fun describe(coordinates: Coordinates): PlaceName?
}

/** Platform Geocoder'ı. Cihazda arka uç yoksa (bazı emülatörler) sessizce null döner. */
class AndroidReverseGeocoder @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ReverseGeocoder {

    override suspend fun describe(coordinates: Coordinates): PlaceName? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    coordinates.latitude,
                    coordinates.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) continuation.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                )
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)?.firstOrNull()
        }
        return address?.toPlaceName()
    }

    private fun Address.toPlaceName(): PlaceName? {
        val name = subAdminArea ?: locality ?: adminArea ?: return null
        return PlaceName(name = name, region = adminArea, country = countryName)
    }
}
