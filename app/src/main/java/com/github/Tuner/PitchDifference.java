package com.github.Tuner;

import android.os.Parcel;
import android.os.Parcelable;
//aceasta este o clasa de reprezentare a notei cele mai apropiate si diferenta in centisunete
class PitchDifference implements Parcelable {

    public static final Creator<PitchDifference> CREATOR = new Creator<PitchDifference>() {
        public PitchDifference createFromParcel(Parcel in) {
            return new PitchDifference(in);
        }

        public PitchDifference[] newArray(int size) {
            return new PitchDifference[size];
        }
    };

    final Note closest;
    final double deviation;

    PitchDifference(Note closest, double deviation) {
        this.closest = closest;
        this.deviation = deviation;
    }

    private PitchDifference(Parcel in) {
        Tuning tuning = MainActivity.getCurrentTuning();
        closest = tuning.findNote(in.readString());
        deviation = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(closest.getName().name());
        dest.writeDouble(deviation);
    }

//    public double getDeviation(){
//        return deviation;
//    }
//
//    public String getClosest(){
//        return closest.getName().name();
//    }
}
