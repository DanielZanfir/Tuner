package com.github.Tuner;

import android.util.Log;

import com.github.Tuner.tuning.BanjoTuning;
import com.github.Tuner.tuning.DropCBassTuning;
import com.github.Tuner.tuning.BassTuning;
import com.github.Tuner.tuning.CelloTuning;
import com.github.Tuner.tuning.ChromaticTuning;
import com.github.Tuner.tuning.DropCGuitarTuning;
import com.github.Tuner.tuning.DropCSharpGuitarTuning;
import com.github.Tuner.tuning.DropDGuitarTuning;
import com.github.Tuner.tuning.GuitarTuning;
import com.github.Tuner.tuning.OpenGGuitarTuning;
import com.github.Tuner.tuning.OudStdTurkishTuning;
import com.github.Tuner.tuning.UkuleleDTuning;
import com.github.Tuner.tuning.UkuleleTuning;
import com.github.Tuner.tuning.ViolinTuning;
import com.github.Tuner.tuning.ViolaTuning;

class TuningMapper {

    private static final int CHROMATIC_TUNING_POSITION = 0;
    private static final int GUITAR_TUNING_POSITION = 1;
    private static final int DROP_D_TUNING_POSITION = 2;
    private static final int DROP_C_TUNING_POSITION = 3;
    private static final int DROP_C_SHARP_TUNING_POSITION = 4;
    private static final int OPEN_G_TUNING = 5;
    private static final int BASS_TUNING_POSITION = 6;
    private static final int BASS_DROP_C_TUNING_POSITION = 7;
    private static final int UKULELE_TUNING_POSITION = 8;
    private static final int D_TUNING_POSITION = 9;
    private static final int VIOLIN_TUNING_POSITION = 10;
    private static final int CELLO_TUNING_POSITION = 11;
    private static final int VIOLA_TUNING_POSITION = 12;
    private static final int OUDSTDTR_TUNING_POSITION = 13;
    private static final int BANJO_TUNING_POSITION = 14;

    static Tuning getTuningFromPosition(int position) {
        switch (position) {
            case CHROMATIC_TUNING_POSITION:
                return new ChromaticTuning();
            case GUITAR_TUNING_POSITION:
                return new GuitarTuning();
            case DROP_D_TUNING_POSITION:
                return new DropDGuitarTuning();
            case DROP_C_TUNING_POSITION:
                return new DropCGuitarTuning();
            case DROP_C_SHARP_TUNING_POSITION:
                return new DropCSharpGuitarTuning();
            case OPEN_G_TUNING:
                return new OpenGGuitarTuning();
            case BASS_TUNING_POSITION:
                return new BassTuning();
            case BASS_DROP_C_TUNING_POSITION:
                return new DropCBassTuning();
            case UKULELE_TUNING_POSITION:
                return new UkuleleTuning();
            case D_TUNING_POSITION:
                return new UkuleleDTuning();
            case VIOLIN_TUNING_POSITION:
                return new ViolinTuning();
            case CELLO_TUNING_POSITION:
                return new CelloTuning();
            case VIOLA_TUNING_POSITION:
                return new ViolaTuning();
            case OUDSTDTR_TUNING_POSITION:
                return new OudStdTurkishTuning();
            case BANJO_TUNING_POSITION:
                return new BanjoTuning();
            default:
                Log.w("com.github.cythara", "Unknown position for tuning dropdown list");
                return new ChromaticTuning();
        }
    }
}
