package com.github.Tuner.tuning;

import com.github.Tuner.Note;
import com.github.Tuner.NoteName;
import com.github.Tuner.Tuning;

import static com.github.Tuner.NoteName.*;
//aici se afla notele pentru acordarea instrumentului chitara
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

        E2(E, 2),
        A2(A, 2),
        D3(D, 3),
        G3(G, 3),
        B3(B, 3),
        E4(E, 4); //E este de fapt un valoare E("E", "Mi") a Enumului NoteName


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
