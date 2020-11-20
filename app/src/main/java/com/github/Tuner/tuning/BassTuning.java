package com.github.Tuner.tuning;

import com.github.Tuner.Note;
import com.github.Tuner.NoteName;
import com.github.Tuner.Tuning;

import static com.github.Tuner.NoteName.*;

public class BassTuning implements Tuning {

    @Override
    public Note[] getNotes() {
        return Pitch.values();
    }

    @Override
    public Note findNote(String name) {
        return Pitch.valueOf(name);
    }

    private enum Pitch implements Note {

        E1(E, 1),
        A1(A, 1),
        D2(D, 2),
        G2(G, 2);

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
