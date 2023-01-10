package com.kjipo.bluetoothmidi.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true) var uid: Long = 0,
    @ColumnInfo(name = "start", defaultValue = "CURRENT_TIMESTAMP") val start: Instant,
    @ColumnInfo(name = "stop") var sessionEnd: Instant? = null
)