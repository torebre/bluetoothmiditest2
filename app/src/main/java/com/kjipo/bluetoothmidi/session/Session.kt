package com.kjipo.bluetoothmidi.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Entity
@Serializable
data class Session(
    @PrimaryKey(autoGenerate = true) var uid: Long = 0,
    @Serializable(with = InstantToLongSerializer::class)
    @ColumnInfo(name = "start", defaultValue = "CURRENT_TIMESTAMP") val start: Instant,
    @Serializable(with = InstantToLongSerializer::class)
    @ColumnInfo(name = "stop") var sessionEnd: Instant? = null
) {

    fun encodeToJson(): String {
        return Json.encodeToString(this)
    }



}