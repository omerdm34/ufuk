package com.kampplus.ufuk.core.network.error

import com.kampplus.ufuk.core.common.error.AppError
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkErrorMapperTest {

    private val mapper = NetworkErrorMapper()

    @Test
    fun `io failure means no connection`() {
        assertEquals(AppError.Network, mapper.map(IOException("timeout")))
    }

    @Test
    fun `http status is kept for server errors and 404 means not found`() {
        assertEquals(AppError.Server(503), mapper.map(httpError(503)))
        assertEquals(AppError.NotFound, mapper.map(httpError(404)))
    }

    @Test
    fun `unreadable body or date is a parse error`() {
        assertEquals(AppError.Parse, mapper.map(SerializationException("bad")))
        val dateError = runCatching { LocalDateTime.parse("27.09.2026") }.exceptionOrNull() as DateTimeParseException
        assertEquals(AppError.Parse, mapper.map(dateError))
    }

    @Test
    fun `anything else is unknown`() {
        assertTrue(mapper.map(IllegalStateException()) is AppError.Unknown)
    }

    private fun httpError(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody()))
}
