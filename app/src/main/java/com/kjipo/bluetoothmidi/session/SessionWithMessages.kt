package com.kjipo.bluetoothmidi.session

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithMessages(
    @Embedded val session: Session,
    @Relation(
        parentColumn = "id",
        entityColumn = "session_id"
    )
    val sessionMidiMessages: List<SessionMidiMessage>
)