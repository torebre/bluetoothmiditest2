package com.kjipo.bluetoothmidi.session

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MidiMessage {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}