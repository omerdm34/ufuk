package com.kampplus.ufuk.core.common.time

import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * "Şimdi"yi düzenli aralıklarla yayınlar; "5 dk önce güncellendi" gibi metinler ekran açıkken
 * de doğru kalır. Testlerde sabit bir akışla değiştirilir.
 */
fun interface Ticker {
    fun ticks(): Flow<Instant>
}

class ClockTicker @Inject constructor(
    private val clock: Clock
) : Ticker {
    override fun ticks(): Flow<Instant> = flow {
        while (true) {
            emit(clock.instant())
            delay(PERIOD.toMillis())
        }
    }

    private companion object {
        val PERIOD: Duration = Duration.ofSeconds(30)
    }
}
