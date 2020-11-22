package com.github.Tuner;

import android.util.Log;

import com.github.Tuner.tuning.BassTuning;
import com.github.Tuner.tuning.ChromaticTuning;
import com.github.Tuner.tuning.GuitarTuning;
import com.github.Tuner.tuning.ViolinTuning;

class TuningMapper {

    private static final int CHROMATIC_TUNING_POSITION = 0;
    private static final int GUITAR_TUNING_POSITION = 1;
    private static final int BASS_TUNING_POSITION = 2;
    private static final int VIOLIN_TUNING_POSITION = 3;

    static Tuning getTuningFromPosition(int position) {
        switch (position) {
            case CHROMATIC_TUNING_POSITION:
                return new ChromaticTuning();
            case GUITAR_TUNING_POSITION:
                return new GuitarTuning();
            case BASS_TUNING_POSITION:
                return new BassTuning();
            case VIOLIN_TUNING_POSITION:
                return new ViolinTuning();
            default:
                Log.w("com.github.Tuner", "Unknown position for tuning dropdown list");
                return new ChromaticTuning();
        }
    }
}
