package com.kampplus.ufuk.feature.places.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kampplus.ufuk.feature.places.data.local.entity.SavedPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places ORDER BY CASE WHEN id = -1 THEN 0 ELSE 1 END, position")
    fun observeAll(): Flow<List<SavedPlaceEntity>>

    @Query("SELECT * FROM saved_places WHERE id = :id")
    suspend fun get(id: Long): SavedPlaceEntity?

    @Query("SELECT COALESCE(MAX(position), -1) FROM saved_places WHERE id != -1")
    suspend fun maxPosition(): Int

    @Upsert
    suspend fun upsert(entity: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE saved_places SET position = position + 1 WHERE position >= :position AND id != -1")
    suspend fun openGap(position: Int)

    @Query("UPDATE saved_places SET position = position - 1 WHERE position > :position AND id != -1")
    suspend fun closeGap(position: Int)

    /** Kayıtlı değilse sona ekler; kayıtlıysa sırasını korur. */
    @Transaction
    suspend fun append(entity: SavedPlaceEntity) {
        if (get(entity.id) != null) return
        upsert(entity.copy(position = maxPosition() + 1))
    }

    @Transaction
    suspend fun insertAt(entity: SavedPlaceEntity) {
        if (entity.id != DEVICE_LOCATION_ID) openGap(entity.position)
        upsert(entity)
    }

    @Transaction
    suspend fun remove(id: Long): SavedPlaceEntity? {
        val entity = get(id) ?: return null
        delete(id)
        if (id != DEVICE_LOCATION_ID) closeGap(entity.position)
        return entity
    }

    private companion object {
        const val DEVICE_LOCATION_ID = -1L
    }
}
