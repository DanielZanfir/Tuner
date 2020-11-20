package com.github.Tuner.tuning;

import com.github.Tuner.Note;
import com.github.Tuner.NoteName;
import com.github.Tuner.Tuning;

import static com.github.Tuner.NoteName.*;

public class BanjoTuning implements Tuning {

    @Override
    public Note[] getNotes() {
        return Pitch.values();
    }

    @Override
    public Note findNote(String name) {
        return Pitch.valueOf(name);
    }

    private enum Pitch implements Note {

        G4(G, 4),
        D3(D, 3),
        G3(G, 3),
        B3(B, 3),
        D4(D, 4);

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
