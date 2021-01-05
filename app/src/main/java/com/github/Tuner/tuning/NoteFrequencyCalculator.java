package com.github.Tuner.tuning;

import com.github.Tuner.Note;

import java.util.Arrays;
import java.util.List;
//aceasta clasa ne da freceventele adevarate ale notelor in functie de frecenta de referinta (by default 440Hz)

public class NoteFrequencyCalculator {

    private static List<String> notes =
            Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private float referenceFrequency;

    public NoteFrequencyCalculator(float referenceFrequency) {
        //in acest constructor se seteaza frecventa de referinta data ca parametru
        this.referenceFrequency = referenceFrequency;
    }
//primeste ca parametru o nota (in format nume Nota, octava, semn) si returneaza frevcenta adevarata (reala) a notei
    public double getFrequency(Note note) {
        int semitonesPerOctave = 12;
        int referenceOctave = 4;
        double distance = semitonesPerOctave * (note.getOctave() - referenceOctave); //calculeaza cate octave diferenta sunt intre A4 si nota cantata

        distance += notes.indexOf(note.getName() + note.getSign()) - notes.indexOf("A"); //calculeaza cate semitonuri diferenta

        return referenceFrequency * Math.pow(2, distance / 12); //calcuam frecventa notei bazata pe diferenta in semitonuri cu A4, care este referinta in industria muzicala
    }
}