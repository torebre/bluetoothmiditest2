package com.kjipo.bluetoothmidi.midi


interface EarTrainer {

    fun getCurrentSequence(): List<MidiPlayCommand>

    fun userInputSequence(listOf: List<MidiMessage>)

}