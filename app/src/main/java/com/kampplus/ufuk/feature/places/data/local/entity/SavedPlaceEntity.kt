package com.kampplus.ufuk.feature.places.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Kayıtlı yer. Cihaz konumu id = -1 ile tutulur ve sıralamaya katılmaz (position = -1). */
@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val region: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val position: Int,
    @ColumnInfo(name = "added_at") val addedAtEpochMillis: Long
)
