package com.kjipo.bluetoothmidi.midi


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

}