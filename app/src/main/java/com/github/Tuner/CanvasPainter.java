package com.github.Tuner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.text.TextPaint;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Objects;

import androidx.core.content.ContextCompat;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static com.github.Tuner.MainActivity.*;

class CanvasPainter {

    private static final double TOLERANCE = 2D;
    private static final int MAX_DEVIATION = 60;
    private static final int NUMBER_OF_MARKS_PER_SIDE = 6;
    private final Context context;

    private Canvas canvas;

    private TextPaint textPaint = new TextPaint(ANTI_ALIAS_FLAG);
    private TextPaint numbersPaint = new TextPaint(ANTI_ALIAS_FLAG);
    private Paint gaugePaint = new Paint(ANTI_ALIAS_FLAG);
    private Paint symbolPaint = new TextPaint(ANTI_ALIAS_FLAG);

    private int redBackground;
    private int greenBackground;
    private int textColor;

    private PitchDifference pitchDifference;

    private float gaugeWidth;
    private float x;
    private float y;
    private boolean useScientificNotation;
    private int referencePitch;


    private CanvasPainter(Context context) {
        this.context = context;
    }

    static CanvasPainter with(Context context) {
        return new CanvasPainter(context);
    }

    CanvasPainter paint(PitchDifference pitchDifference) {
        this.pitchDifference = pitchDifference;

        return this;
    }

    void on(Canvas canvas) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFS_FILE, Context.MODE_PRIVATE);

        useScientificNotation = preferences.getBoolean(
                USE_SCIENTIFIC_NOTATION, true);

        referencePitch = preferences.getInt(
                REFERENCE_PITCH, 440);

        this.canvas = canvas;

       // int color = context.getResources().getColor(R.color.colorPrimaryDark);
       // this.canvas.drawColor(color);

        redBackground = R.color.red_dark;
        greenBackground = R.color.green_dark;
        textColor = Color.WHITE;


        gaugeWidth = 0.45F * canvas.getWidth();
        x = canvas.getWidth() / 2F;
        y = canvas.getHeight() / 2F;

        textPaint.setColor(textColor);
        int textSize = context.getResources().getDimensionPixelSize(R.dimen.noteTextSize);
        textPaint.setTextSize(textSize);
        drawGauge();
        drawImage(R.drawable.blank_button);

        if (!isAutoModeEnabled()) {
            Note[] tuningNotes = getCurrentTuning().getNotes();
            Note note = tuningNotes[getReferencePosition()];
            drawText(x, canvas.getHeight() / 3F, note, symbolPaint);
        }

        if (pitchDifference != null) {
            int abs = Math.abs(getNearestDeviation());
            boolean shouldDraw = abs <= MAX_DEVIATION ||
                    (abs <= MAX_DEVIATION * 50 && !isAutoModeEnabled());
            if (shouldDraw) {
                setBackground();
                drawGauge();

                drawDeviation();  //desenez cu cat ii mai proasta nota
                float x = canvas.getWidth() / 2F;

                if(isAutoModeEnabled()){
                    drawText(x, canvas.getHeight() / 3F, pitchDifference.closest, textPaint); //aici desenez nota cea mai apropiata cu octava si # la nota sus acolo
                }

            }
        }
    }

    private void drawDeviation() {
        long rounded = Math.round(pitchDifference.deviation);
        String text = String.valueOf(rounded);
        String flat = "To Flat";
        String sharp = "To Sharp";
        float offset = textPaint.measureText(flat) / 2F;
        ImageView left = MainActivity.getleftindicator();
        ImageView right = MainActivity.getrightindicator();
        AnimationDrawable leftI = MainActivity.getLeftAnimation();
        AnimationDrawable rightI = MainActivity.getRightAnimation();

        Rect bounds = new Rect();
        symbolPaint.getTextBounds(text, 0, text.length(), bounds);
        float spaceWidth = gaugeWidth / NUMBER_OF_MARKS_PER_SIDE;

        float xPosForSharpValues = x + NUMBER_OF_MARKS_PER_SIDE * spaceWidth - symbolPaint.measureText(text) / 2F-100;
        float xPosForFlatValues = x - NUMBER_OF_MARKS_PER_SIDE * spaceWidth - symbolPaint.measureText(text) / 2F+100;
        float yPos = canvas.getHeight() / 1.3F;
        if (rounded < 0){
            left.setVisibility(View.VISIBLE);
            right.setVisibility(View.INVISIBLE);
            canvas.drawText(text, xPosForFlatValues, yPos, symbolPaint);
            if (Math.abs(getNearestDeviation()) > TOLERANCE) {
                canvas.drawText(flat,x-offset,canvas.getHeight() / 4F,textPaint);
                leftI.start();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (rounded>0){
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.VISIBLE);
            canvas.drawText(text, xPosForSharpValues, yPos, symbolPaint);
            if (Math.abs(getNearestDeviation()) > TOLERANCE) {
                canvas.drawText(sharp,x-offset,canvas.getHeight() / 4F,textPaint);
                rightI.start();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void drawGauge() {

        gaugePaint.setColor(Color.RED);

        int gaugeSize = context.getResources().getDimensionPixelSize(R.dimen.gaugeSize);
        gaugePaint.setStrokeWidth(gaugeSize);

        int textSize = context.getResources().getDimensionPixelSize(R.dimen.numbersTextSize);
        numbersPaint.setTextSize(textSize);
        numbersPaint.setColor(textColor);


        float spaceWidth = gaugeWidth / NUMBER_OF_MARKS_PER_SIDE;
        drawSymbols(spaceWidth);

        displayReferencePitch();
    }

    private void displayReferencePitch() {
        float y = canvas.getHeight() / 6.5F;

        Note note = new Note() {
            @Override
            public NoteName getName() {
                return NoteName.A;
            }

            @Override
            public int getOctave() {
                return 4;
            }

            @Override
            public String getSign() {
                return "";
            }
        };

        TextPaint paint = new TextPaint(ANTI_ALIAS_FLAG);
        paint.setColor(textColor);
        int size = (int) (textPaint.getTextSize() / 2);
        paint.setTextSize(size);

        float offset = paint.measureText(getNote(note.getName()) + getOctave(4)) * 0.75f;

        drawText(x + NUMBER_OF_MARKS_PER_SIDE * offset - 45, y, note, paint);
        canvas.drawText(String.format(Locale.ENGLISH, "= %d Hz", referencePitch),
                x + NUMBER_OF_MARKS_PER_SIDE * offset, y, paint);
    }


    private void drawSymbols(float spaceWidth) {
        String sharp = "♯";
        String flat = "♭";

        int symbolsTextSize = context.getResources().getDimensionPixelSize(R.dimen.symbolsTextSize);
        symbolPaint.setTextSize(symbolsTextSize);
        symbolPaint.setColor(textColor);

        float yPos = canvas.getHeight() / 1.5F;
        canvas.drawText(sharp,
                x + NUMBER_OF_MARKS_PER_SIDE * spaceWidth - symbolPaint.measureText(sharp) / 2F,
                yPos, symbolPaint);

        canvas.drawText(flat,
                x - NUMBER_OF_MARKS_PER_SIDE * spaceWidth - symbolPaint.measureText(flat) / 2F,
                yPos,
                symbolPaint);
    }

    private void drawImage(int cirlceID) {
        Bitmap bitBtn = decodeSampledBitmapFromResource(context.getResources(), cirlceID, 100, 100);
        canvas.drawBitmap(bitBtn,canvas.getWidth()/2.5F,canvas.getHeight() / 1.2F,gaugePaint);
    }

    private void drawText(float x, float y, Note note, Paint textPaint) {
        String noteText = getNote(note.getName());
        float offset = textPaint.measureText(noteText) / 2F;

        String sign = note.getSign();
        String octave = String.valueOf(getOctave(note.getOctave()));

        TextPaint paint = new TextPaint(ANTI_ALIAS_FLAG);
        paint.setColor(textColor);
        int textSize = (int) (textPaint.getTextSize() / 2);
        paint.setTextSize(textSize);

        float factor = 0.75f;
        if (useScientificNotation) {
            factor = 1.5f;
        }

        canvas.drawText(sign, x + offset * 1.25f, y - offset * factor, paint);
        canvas.drawText(octave, x + offset * 1.25f, y + offset * 0.5f , paint);

        canvas.drawText(noteText, x - offset, y, textPaint);
    }

    private int getOctave(int octave) {
        if (useScientificNotation) {
            return octave;
        }
        if (octave <= 1) {
            return octave - 2;
        }

        return octave - 1;
    }

    private String getNote(NoteName name) {
        if (useScientificNotation) {
            return name.getScientific();
        }

        return name.getSol();
    }

    private void setBackground() {
        String perfect = "Perfect";
        float offset = textPaint.measureText(perfect) / 2F;
        float sgaugeWidth = 0.15F * canvas.getWidth();
        drawImage(R.drawable.red_button);
        int color = redBackground;
        String text = "✗";
        if (Math.abs(getNearestDeviation()) <= TOLERANCE) {
            drawImage(R.drawable.green_button);
            canvas.drawText(perfect,x-offset,canvas.getHeight() / 4F,textPaint);
            color = greenBackground;
            text = "✓";
        }


        canvas.drawColor(context.getResources().getColor(color));
        canvas.drawText(text,
                x + sgaugeWidth - symbolPaint.measureText(text) ,
                canvas.getHeight() / 3F, symbolPaint);
    }

    private int getNearestDeviation() {
        float deviation = (float) pitchDifference.deviation;
        int rounded = Math.round(deviation);

        return Math.round(rounded / 10f) * 10;
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private  Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                    int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

}
