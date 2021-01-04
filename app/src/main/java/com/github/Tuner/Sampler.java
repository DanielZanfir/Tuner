package com.github.Tuner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Sampler {

    //Sampler calculeaza average dintr-o lista cu un nr dat de deviatii
    //din 15 deviatii inregistrate calculeaza media doar pentru cea mai frecventa nota apropiata din lista cu 15 deviatii
    static PitchDifference calculateAverageDifference(List<PitchDifference> samples) {
        Note mostFrequentNote = extractMostFrequentNote(samples); //se extrage din samples cea mai frecventa nota apropiata
        List<PitchDifference> filteredSamples = filterByNote(samples, mostFrequentNote); //lista deviatilor doar pentru cea mai frecventa nota apropiata

        double deviationSum = 0;
        int sameNoteCount = 0;
        for (PitchDifference pitchDifference : filteredSamples) {
            deviationSum += pitchDifference.deviation;
            sameNoteCount++;
        }

        if (sameNoteCount > 0) {
            double averageDeviation = deviationSum / sameNoteCount;

            return new PitchDifference(mostFrequentNote, averageDeviation);
        }

        return null;
    }

    //metode ajutoatoare pentru determinarea celei mai frecevente note apropiate din lista de esantioane (samples)
    static Note extractMostFrequentNote(List<PitchDifference> samples) {
        Map<Note, Integer> noteFrequencies = new HashMap<>();

        for (PitchDifference pitchDifference : samples) {
            Note closest = pitchDifference.closest;
            if (noteFrequencies.containsKey(closest)) {
                Integer count = noteFrequencies.get(closest);
                noteFrequencies.put(closest, count + 1);
            } else {
                noteFrequencies.put(closest, 1);
            }
        }

        Note mostFrequentNote = null;
        int mostOccurrences = 0;
        for (Note note : noteFrequencies.keySet()) {
            Integer occurrences = noteFrequencies.get(note);
            if (occurrences > mostOccurrences) {
                mostFrequentNote = note;
                mostOccurrences = occurrences;
            }
        }

        return mostFrequentNote;
    }

    //filtrarea listei de esantioane (samples) astfel incat ea sa contina doar esantioane ale notei apropiate cele mai frecvente
    static List<PitchDifference> filterByNote(List<PitchDifference> samples, Note note) {
        List<PitchDifference> filteredSamples = new ArrayList<>();

        for (PitchDifference sample : samples) {
            if (sample.closest == note) {
                filteredSamples.add(sample);
            }
        }

        return filteredSamples;
    }


}
