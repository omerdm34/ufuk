package com.kampplus.ufuk.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kampplus.ufuk.feature.places.data.local.dao.SavedPlaceDao
import com.kampplus.ufuk.feature.places.data.local.entity.SavedPlaceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedPlaceDaoTest {

    @get:Rule
    val migrationHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), UfukDatabase::class.java)

    private lateinit var database: UfukDatabase
    private lateinit var dao: SavedPlaceDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UfukDatabase::class.java).allowMainThreadQueries().build()
        dao = database.savedPlaceDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deviceLocationIsFirstAndOthersKeepTheirOrder() = runTest {
        dao.append(place(1, "İstanbul"))
        dao.append(place(2, "Ankara"))
        dao.upsert(place(-1, "Çankaya").copy(position = -1))

        assertEquals(listOf("Çankaya", "İstanbul", "Ankara"), dao.observeAll().first().map { it.name })
    }

    @Test
    fun undoPutsThePlaceBackWhereItWas() = runTest {
        listOf("İstanbul", "Ankara", "İzmir").forEachIndexed { index, name -> dao.append(place(index + 1L, name)) }

        val removed = dao.remove(1)!!
        dao.remove(3)
        dao.insertAt(removed)

        assertEquals(listOf("İstanbul", "Ankara"), dao.observeAll().first().map { it.name })
    }

    @Test
    fun appendingTwiceKeepsOneRow() = runTest {
        dao.append(place(1, "İstanbul"))
        dao.append(place(1, "İstanbul"))

        assertEquals(1, dao.observeAll().first().size)
    }

    @Test
    fun migrationFromVersionOneKeepsTheForecastCache() {
        migrationHelper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO forecast_cache (cache_key, payload_json, fetched_at) VALUES ('39.92,32.85', '{}', 1000)")
            close()
        }

        val migrated = migrationHelper.runMigrationsAndValidate(TEST_DB, 2, true)

        migrated.query("SELECT COUNT(*) FROM forecast_cache").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
    }

    private fun place(id: Long, name: String) = SavedPlaceEntity(
        id = id,
        name = name,
        region = null,
        country = "Türkiye",
        latitude = 39.9,
        longitude = 32.8,
        position = 0,
        addedAtEpochMillis = 0
    )

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
