package com.github.Tuner;

import com.github.Tuner.tuning.NoteFrequencyCalculator;

import java.util.Arrays;

class PitchComparator {

    static PitchDifference retrieveNote(float pitch) {
        Tuning tuning = MainActivity.getCurrentTuning(); //instrumentul selectat
        int referencePitch = MainActivity.getReferencePitch(); //ii 440hz in general, daca user-ul nu a modificat in prealabil

        Note[] tuningNotes = tuning.getNotes(); //notele instrumentuui selectat
        Note[] notes;

        if (MainActivity.isAutoModeEnabled()) {
            notes = tuningNotes; //in array-ul notes punem toate notele isntrumentului selectat
        } else {
            notes = new Note[]{tuningNotes[MainActivity.getReferencePosition()]}; //in array-ul notes punem doar nota selectata (de exemplu doar nota E4)
            //MainActivity.getReferencePosition() returneaza pozitia notei in cadrul instrumentului, adica coarda respectiva
        }
        //NoteFrequencyCalculator calculeaza frecventa notei data prin input in functie de freceventa de referinta data ca parametru constructorului
        NoteFrequencyCalculator noteFrequencyCalculator = new NoteFrequencyCalculator(referencePitch);

        Arrays.sort(notes, (o1, o2) ->
                Double.compare(noteFrequencyCalculator.getFrequency(o1), //sorteaza crescator in functie de freceventa notelor din notes
                        noteFrequencyCalculator.getFrequency(o2)));

        double minCentDifference = Float.POSITIVE_INFINITY;
        Note closest = notes[0];
        for (Note note : notes) {
            double frequency = noteFrequencyCalculator.getFrequency(note);//frecventa adevarata fata de care comparam inputu (de Ex: E4 CURAT)
            double centDifference = 1200d * log2(pitch / frequency);//diferenta in centisunete dintre input(pitch) si frecventa adevarata(frequency)

            //compara care diferenta in centisunete e cea mai mica(compara fata de care nota input-ul e cel mai apropiat)
            if (Math.abs(centDifference) < Math.abs(minCentDifference)) {
                minCentDifference = centDifference; //salvam in centisunete diferenta cea mai mica
                closest = note; //salvam nota(de Ex: E 4)
            }
        }
        //in modul auto array-ul notes va avea toate notele corzilor instrumentului(de ex: 6 note pentru chitara)
        //in modul manual sau auto-off in notes vom avea doar nota selectata prin getReferencePosition()
        return new PitchDifference(closest, minCentDifference); //PitchDifference este o clasa in care salvam nota cea mai apropiata si diferenta in centisunete fata de aceasta
    }

    private static double log2(double number) {
        return Math.log(number) / Math.log(2);
    }
}
