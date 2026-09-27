package com.kampplus.ufuk.core.common.error

import javax.inject.Inject

/** Teknik hatayı ([Throwable]) alan hatasına ([AppError]) çevirir. */
fun interface ErrorMapper {
    fun map(throwable: Throwable): AppError
}

/** Veri kaynağı yerel olduğu sürece yeterli olan varsayılan eşleyici. */
class DefaultErrorMapper @Inject constructor() : ErrorMapper {
    override fun map(throwable: Throwable): AppError = when (throwable) {
        is NoSuchElementException -> AppError.NotFound
        else -> AppError.Unknown(throwable)
    }
}
