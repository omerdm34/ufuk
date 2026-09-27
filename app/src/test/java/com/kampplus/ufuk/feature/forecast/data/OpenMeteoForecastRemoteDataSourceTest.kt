package com.kampplus.ufuk.feature.forecast.data

import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.network.di.NetworkModule
import com.kampplus.ufuk.feature.forecast.data.remote.OpenMeteoForecastRemoteDataSource
import com.kampplus.ufuk.feature.forecast.data.remote.api.OpenMeteoAirQualityApi
import com.kampplus.ufuk.feature.forecast.data.remote.api.OpenMeteoForecastApi
import com.kampplus.ufuk.testing.readResource
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class OpenMeteoForecastRemoteDataSourceTest {

    private val server = MockWebServer()
    private lateinit var dataSource: OpenMeteoForecastRemoteDataSource

    private var forecastBody = readResource("forecast_full.json")
    private var airQualityResponse = MockResponse().setBody(readResource("air_quality.json"))

    private val ankara = Coordinates(39.92, 32.85)
    private val istanbul = Coordinates(41.01, 28.95)

    @Before
    fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.requestUrl?.encodedPath) {
                "/v1/forecast" -> MockResponse().setBody(forecastBody)
                "/v1/air-quality" -> airQualityResponse
                else -> MockResponse().setResponseCode(404)
            }
        }
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/v1/"))
            .addConverterFactory(NetworkModule.provideConverterFactory(NetworkModule.provideJson()))
            .build()
        dataSource = OpenMeteoForecastRemoteDataSource(
            forecastApi = retrofit.create(OpenMeteoForecastApi::class.java),
            airQualityApi = retrofit.create(OpenMeteoAirQualityApi::class.java)
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `forecast asks for yesterday, ten days and local time zone`() = runTest {
        dataSource.fetchForecast(ankara)

        val forecastRequest = requests().single { it.encodedPath == "/v1/forecast" }
        assertEquals("1", forecastRequest.queryParameter("past_days"))
        assertEquals("10", forecastRequest.queryParameter("forecast_days"))
        assertEquals("auto", forecastRequest.queryParameter("timezone"))
        assertEquals("39.92", forecastRequest.queryParameter("latitude"))
    }

    @Test
    fun `forecast and air quality arrive together`() = runTest {
        val payload = dataSource.fetchForecast(ankara)

        assertEquals(22.4, payload.forecast.current!!.temperature, 0.0)
        assertEquals(38, payload.airQuality?.current?.europeanAqi)
    }

    @Test
    fun `air quality failure does not fail the forecast`() = runTest {
        airQualityResponse = MockResponse().setResponseCode(503)

        val payload = dataSource.fetchForecast(ankara)

        assertEquals(22.4, payload.forecast.current!!.temperature, 0.0)
        assertNull(payload.airQuality)
    }

    @Test
    fun `several places are fetched in one request and keep their order`() = runTest {
        forecastBody = readResource("conditions_multi.json")

        val responses = dataSource.fetchConditions(listOf(istanbul, ankara))

        val request = requests().single()
        assertEquals("41.01,39.92", request.queryParameter("latitude"))
        assertEquals("1", request.queryParameter("forecast_days"))
        assertEquals(listOf(19.6, 22.4), responses.map { it.current!!.temperature })
    }

    @Test
    fun `a single place uses the object response`() = runTest {
        val responses = dataSource.fetchConditions(listOf(ankara))

        assertEquals(1, responses.size)
        assertEquals(22.4, responses.single().current!!.temperature, 0.0)
    }

    private fun requests() = List(server.requestCount) { server.takeRequest().requestUrl!! }
}
