package com.kjipo.bluetoothmidi.session

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
data class SessionWithMessages(
    @Embedded val session: Session,
    @Relation(
        parentColumn = "uid",
        entityColumn = "sessionid"
    )
    val sessionMidiMessages: List<SessionMidiMessage>
) {

    fun encodeToJson(): String {
        return Json.encodeToString(this)
    }

}