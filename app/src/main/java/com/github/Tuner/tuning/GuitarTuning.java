package com.github.Tuner.tuning;

import com.github.Tuner.Note;
import com.github.Tuner.NoteName;
import com.github.Tuner.Tuning;

import static com.github.Tuner.NoteName.*;

public class GuitarTuning implements Tuning {

    @Override
    public Note[] getNotes() {
        return Pitch.values();
    }

    @Override
    public Note findNote(String name) {
        return Pitch.valueOf(name);
    }

    public enum Pitch implements Note {

        E4(E, 4),
        B3(B, 3),
        G3(G, 3),
        D3(D, 3),
        A2(A, 2),
        E2(E, 2);

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
