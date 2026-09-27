package com.kampplus.ufuk.core.common.di

import com.kampplus.ufuk.core.common.dispatcher.DefaultDispatcher
import com.kampplus.ufuk.core.common.dispatcher.IoDispatcher
import com.kampplus.ufuk.core.common.error.ErrorMapper
import com.kampplus.ufuk.core.common.time.ClockTicker
import com.kampplus.ufuk.core.common.time.Ticker
import com.kampplus.ufuk.core.network.error.NetworkErrorMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {
    @Binds
    abstract fun bindErrorMapper(impl: NetworkErrorMapper): ErrorMapper

    @Binds
    abstract fun bindTicker(impl: ClockTicker): Ticker

    companion object {
        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @DefaultDispatcher
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
