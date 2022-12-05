package com.kjipo.bluetoothmidi.session

interface MidiSessionRepository {

    fun addMessageToSession()

    fun closeSession()

}