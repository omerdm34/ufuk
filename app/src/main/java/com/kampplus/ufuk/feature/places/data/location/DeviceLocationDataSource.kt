package com.kampplus.ufuk.feature.places.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.kampplus.ufuk.core.model.Coordinates
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

interface DeviceLocationDataSource {
    fun hasPermission(): Boolean

    /** Cihazın konumu; bulunamazsa null. İzin yoksa SecurityException fırlatır. */
    suspend fun currentCoordinates(): Coordinates?
}

/**
 * Yalnızca platformun LocationManager'ını kullanır: Google Play Services bağımlılığı yoktur,
 * servisleri olmayan cihazlarda da çalışır. Önce taze bir konum istenir; 10 saniyede gelmezse
 * son bilinen konumların en yenisi kullanılır. Kaba konum (şehir düzeyi) hava için yeterlidir.
 */
class AndroidDeviceLocationDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DeviceLocationDataSource {

    private val manager: LocationManager? = context.getSystemService(LocationManager::class.java)

    override fun hasPermission(): Boolean = PERMISSIONS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    override suspend fun currentCoordinates(): Coordinates? {
        if (!hasPermission()) throw SecurityException("Location permission not granted")
        val manager = manager ?: return null
        val providers = manager.getProviders(true).filter { it in PREFERRED_PROVIDERS }
        val fresh = providers.firstOrNull()?.let { provider ->
            withTimeoutOrNull(FRESH_FIX_TIMEOUT_MS) { currentLocation(manager, provider) }
        }
        val location = fresh ?: manager.getProviders(true)
            .mapNotNull { manager.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
        return location?.let { Coordinates(latitude = it.latitude, longitude = it.longitude) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(manager, provider, signal, ContextCompat.getMainExecutor(context)) { location ->
                if (continuation.isActive) continuation.resume(location)
            }
        }

    private companion object {
        const val FRESH_FIX_TIMEOUT_MS = 10_000L

        /** LocationManager.FUSED_PROVIDER (API 31) ile aynı değer; eski sürümlerde yalnızca listede bulunmaz. */
        const val FUSED_PROVIDER = "fused"
        val PERMISSIONS = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

        /** Ağ konumu hızlı ve iç mekânda da çalışır; GPS yedektir. */
        val PREFERRED_PROVIDERS = listOf(LocationManager.NETWORK_PROVIDER, FUSED_PROVIDER, LocationManager.GPS_PROVIDER)
    }
}
