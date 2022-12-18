package com.kjipo.bluetoothmidi.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true) var uid: Int = 0,
    @ColumnInfo(name = "start") val start: Instant,
    @ColumnInfo(name = "stop") var sessionEnd: Instant? = null
)