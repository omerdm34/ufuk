package com.kampplus.ufuk.core.common.error

/** Uygulama genelindeki hata sözlüğü. UI metinleri presentation katmanında üretilir. */
sealed interface AppError {
    data object Network : AppError

    data class Server(
        val code: Int
    ) : AppError

    data object NotFound : AppError

    data object Parse : AppError

    /** Konum izni var ama cihaz bir konum veremedi (GPS kapalı, iç mekân, zaman aşımı). */
    data object LocationUnavailable : AppError

    data object LocationPermissionDenied : AppError

    data class Unknown(
        val cause: Throwable
    ) : AppError
}
