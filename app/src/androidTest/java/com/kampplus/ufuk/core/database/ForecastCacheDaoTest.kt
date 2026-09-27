package com.kampplus.ufuk.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kampplus.ufuk.feature.forecast.data.local.dao.ForecastCacheDao
import com.kampplus.ufuk.feature.forecast.data.local.entity.ForecastCacheEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForecastCacheDaoTest {

    private lateinit var database: UfukDatabase
    private lateinit var dao: ForecastCacheDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UfukDatabase::class.java).allowMainThreadQueries().build()
        dao = database.forecastCacheDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertReplacesTheEntryForTheSameKey() = runTest {
        dao.upsert(ForecastCacheEntity("39.92,32.85", "{\"a\":1}", 1_000))
        dao.upsert(ForecastCacheEntity("39.92,32.85", "{\"a\":2}", 2_000))

        val entry = dao.observe("39.92,32.85").first()!!
        assertEquals("{\"a\":2}", entry.payloadJson)
        assertEquals(2_000, entry.fetchedAtEpochMillis)
    }

    @Test
    fun oldEntriesArePruned() = runTest {
        dao.upsert(ForecastCacheEntity("old", "{}", 1_000))
        dao.upsert(ForecastCacheEntity("new", "{}", 9_000))

        dao.deleteOlderThan(5_000)

        assertNull(dao.get("old"))
        assertEquals(9_000, dao.get("new")!!.fetchedAtEpochMillis)
    }
}
