package com.github.Tuner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

//TunerView este un View personalizat pentru a putea afisa pe ecran informatiile despre nota cantata (Nume nota, octava, semn, diferenta in centisunete, etc)
//TunerView se foloseste de un CanvasPainter pentru a arata pe ecran acele informatii
public class TunerView extends View {

    private CanvasPainter canvasPainter;
    private PitchDifference pitchDifference;

    public TunerView(Context context) {
        super(context);
        canvasPainter = CanvasPainter.with(getContext());
    }

    public TunerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        canvasPainter = CanvasPainter.with(getContext());
    }

    //cea mai importanta metoda pentru a putea desena pe ecran
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvasPainter.paint(pitchDifference).on(canvas);
    }

    //metoda pentru setarea/updatarea atributului pitchDifference
    public void setPitchDifference(PitchDifference pitchDifference) {
        this.pitchDifference = pitchDifference;
    }
}