package com.kjipo.bluetoothmidi.midi

sealed class MidiPlayCommand {

}

class NoteOn(val pitch: Int, val velocity: Int) : MidiPlayCommand()

class NoteOff(val pitch: Int, val velocity: Int) : MidiPlayCommand()

class Sleep(val sleepInMilliseconds: Long) : MidiPlayCommand()
