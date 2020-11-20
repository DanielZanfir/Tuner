package com.github.Tuner.tuning;

import com.github.Tuner.Note;
import com.github.Tuner.NoteName;
import com.github.Tuner.Tuning;

import static com.github.Tuner.NoteName.*;

public class ViolinTuning implements Tuning {

    @Override
    public Note[] getNotes() {
        return Pitch.values();
    }

    @Override
    public Note findNote(String name) {
        return Pitch.valueOf(name);
    }

    private enum Pitch implements Note {

        G3(G, 3),
        D4(D, 4),
        A4(A, 4),
        E5(E, 5);

        private final String sign;
        private final int octave;
        private NoteName name;

        Pitch(NoteName name, int octave) {
            this.name = name;
            this.octave = octave;
            this.sign = "";
        }

        public NoteName getName() {
            return name;
        }

        @Override
        public int getOctave() {
            return octave;
        }

        @Override
        public String getSign() {
            return sign;
        }
    }
}
