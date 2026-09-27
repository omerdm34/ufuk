package com.kampplus.ufuk.feature.places.domain.repository

import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place

interface LocationRepository {
    fun hasPermission(): Boolean

    /**
     * Cihazın şu anki konumu, adıyla birlikte. Ad bulunamazsa boş kalır ve ekran "Konumum" yazar.
     * İzin yoksa [com.kampplus.ufuk.core.common.error.AppError.LocationPermissionDenied] döner.
     */
    suspend fun locateDevice(): AppResult<Place>
}
