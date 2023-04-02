package com.kjipo.bluetoothmidi.midi

import timber.log.Timber


class EarTrainerImpl : EarTrainer {

    override fun getCurrentSequence(): List<MidiPlayCommand> {
        // TODO Generate new sequence based on previous inputs
        return listOf(
            NoteOn(60, 127),
            Sleep(3000),
            NoteOff(60, 127),
            NoteOn(64, 127),
            Sleep(3000),
            NoteOff(64, 127),
            NoteOn(70, 127),
            Sleep(3000),
            NoteOff(70, 127)
        )
    }

    override fun userInputSequence(receivedMidiMessages: List<MidiMessage>) {
        Timber.tag("EarTrainer").d("Received MIDI messages")
        // TODO See how well user was able to reproduce sequence
        receivedMidiMessages.forEach {
            Timber.tag("EarTrainer").d("MIDI message: $it")
        }
    }

}