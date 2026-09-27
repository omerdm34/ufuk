package com.kampplus.ufuk.feature.places.data.repository

import com.kampplus.ufuk.core.common.dispatcher.IoDispatcher
import com.kampplus.ufuk.core.common.error.ErrorMapper
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.common.result.runCatchingApp
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.data.catalog.FeaturedPlaceCatalog
import com.kampplus.ufuk.feature.places.data.remote.PlaceRemoteDataSource
import com.kampplus.ufuk.feature.places.domain.repository.PlaceSearchRepository
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PlaceSearchRepositoryImpl @Inject constructor(
    private val remoteDataSource: PlaceRemoteDataSource,
    private val catalog: FeaturedPlaceCatalog,
    private val errorMapper: ErrorMapper,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlaceSearchRepository {

    /** Sonuç adları cihaz dilinde istenir (Türkçede "İstanbul", İngilizcede "Istanbul"). */
    override suspend fun search(query: String): AppResult<List<Place>> = withContext(ioDispatcher) {
        errorMapper.runCatchingApp { remoteDataSource.search(query, Locale.getDefault().language) }
    }

    override fun featured(): List<Place> = catalog.places()
}
