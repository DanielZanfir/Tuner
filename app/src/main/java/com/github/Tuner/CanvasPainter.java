package com.github.Tuner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Locale;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static com.github.Tuner.MainActivity.*;

//CanvasPainter este o clasa infasuratoare pentru clasa Canvas si e folosita pentru desenarea elementelor grafice
class CanvasPainter {

    private static final double TOLERANCE = 2D; //Toleranta este marja de eroare care ne spune daca nota este sau nu corect acordata
    private static final int MAX_DEVIATION = 60;//MAX_DEVIATION este intervalul devierii pentru fiecare nota
    // Exemplu: daca avem nota G3 care are valoarea de 100 de centisunte. Aplicatia face ca nota G3 sa aibe intevalul (40,160)
    private static final int NUMBER_OF_MARKS_PER_SIDE = 6; //NUMBER_OF_MARKS_PER_SIDE este folosit pentru pozitionarea elementelor pe ecran
    private final Context context;


    private Canvas canvas;

    private TextPaint textPaint = new TextPaint(ANTI_ALIAS_FLAG);
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

    //se apeleaza din TunerView in scopul setarii canvas-ului pentru TunerView
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

        useScientificNotation = preferences.getBoolean(USE_SCIENTIFIC_NOTATION, true);

        referencePitch = preferences.getInt(REFERENCE_PITCH, 440);

        this.canvas = canvas;

        redBackground = R.color.red_dark;
        greenBackground = R.color.green_dark;
        textColor = R.color.colorYellowText;


        gaugeWidth = 0.45F * canvas.getWidth();
        x = canvas.getWidth() / 2F;
        y = canvas.getHeight() / 2F;

        textPaint.setColor(context.getResources().getColor(textColor));
        int textSize = context.getResources().getDimensionPixelSize(R.dimen.noteTextSize);
        textPaint.setTextSize(textSize);
        drawSymbols();
        displayReferencePitch();
        drawImage(R.drawable.blank_button);

        if (!isAutoModeEnabled()) {
            //daca modul auto este OFF, sa afisam notele instrumentului selectat pe ecran
            Note[] tuningNotes = getCurrentTuning().getNotes();
            Note note = tuningNotes[getReferencePosition()];
            drawText(x, canvas.getHeight() / 3F, note, symbolPaint,true);
        }

        if (pitchDifference != null) {
            //daca aplicatia a detectat o nota
            int abs = Math.abs(getNearestDeviation()); //luam deviatia notei
            boolean shouldDraw = abs <= MAX_DEVIATION || (abs <= MAX_DEVIATION * 50 && !isAutoModeEnabled());
            if (shouldDraw) {
                setBackground();
                drawSymbols();
                displayReferencePitch();

                drawDeviation();  //desenam deviatia notei
                float x = canvas.getWidth() / 2F;

                if(isAutoModeEnabled()){
                    drawText(x, canvas.getHeight() / 3F, pitchDifference.closest, textPaint,true); //deseneaza nota pe care o detecteaza aplicatia
                }

            }
        }
    }

    private void drawDeviation() {
        //metoda pentru a desena:deviatia(Numarul de centisunete),un mesaj care ne spune cum este nota (Flat, Sharp), pendulul si animatia pentru pendul
        long rounded = Math.round(pitchDifference.deviation);
        String deviation;
        if (rounded>0){
            deviation = "+"+rounded;
        }
        else{
            deviation = String.valueOf(rounded);
        }
        String flat = "Too Flat";
        String sharp = "Too Sharp";
        float offset = textPaint.measureText(flat) / 2F;
        ImageView left = MainActivity.getleftindicator();
        ImageView right = MainActivity.getrightindicator();
        AnimationDrawable leftI = MainActivity.getLeftAnimation();
        AnimationDrawable rightI = MainActivity.getRightAnimation();

        Rect bounds = new Rect();
        symbolPaint.getTextBounds(deviation, 0, deviation.length(), bounds);
        float spaceWidth = gaugeWidth / NUMBER_OF_MARKS_PER_SIDE;

        float xPosForSharpValues = x + NUMBER_OF_MARKS_PER_SIDE * spaceWidth - symbolPaint.measureText(deviation) / 2F-100;
        float xPosForFlatValues = x - NUMBER_OF_MARKS_PER_SIDE * spaceWidth - symbolPaint.measureText(deviation) / 2F+100;
        float yPos = canvas.getHeight() / 1.3F;
        if (rounded < 0){
            //Setarea vizivilitatii animatilor
            left.setVisibility(View.VISIBLE);
            right.setVisibility(View.INVISIBLE);
            canvas.drawText(deviation, xPosForFlatValues, yPos, symbolPaint);
            if (Math.abs(getNearestDeviation()) > TOLERANCE) {
                canvas.drawText(flat,x-offset,canvas.getHeight() / 4F,textPaint);
                //Pornirea animatiei
                leftI.start();
            }
        }
        else if (rounded>0){
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.VISIBLE);
            canvas.drawText(deviation, xPosForSharpValues, yPos, symbolPaint);
            if (Math.abs(getNearestDeviation()) > TOLERANCE) {
                canvas.drawText(sharp,x-offset,canvas.getHeight() / 4F,textPaint);
                rightI.start();
            }
        }
    }


    private void displayReferencePitch() {
        //metoda pentru a afisa frecventa de referinta
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
        paint.setColor(context.getResources().getColor(textColor));
        int size = (int) (textPaint.getTextSize() / 2);
        paint.setTextSize(size);

        float offset = paint.measureText(getNote(note.getName()) + getOctave(4)) * 0.75f;

        //pozitia x pe ecran difera pentru frecventa de referinta in functie de notatie
        if(useScientificNotation){
            drawText(x + NUMBER_OF_MARKS_PER_SIDE * offset - 45, y, note, paint,false);
            canvas.drawText(String.format(Locale.ENGLISH, "= %d Hz", referencePitch),
                    x + NUMBER_OF_MARKS_PER_SIDE * offset, y, paint);
        }
        else{
            drawText(x + NUMBER_OF_MARKS_PER_SIDE * offset-150, y, note, paint,false);
            canvas.drawText(String.format(Locale.ENGLISH, "= %d Hz", referencePitch),
                    x + NUMBER_OF_MARKS_PER_SIDE * offset-100, y, paint);
        }

    }


    private void drawSymbols() {
        //metoda pentru a afisa simbolurile pentru Flat, Sharp
        float spaceWidth = gaugeWidth / NUMBER_OF_MARKS_PER_SIDE;
        String sharp = "♯";
        String flat = "♭";

        int symbolsTextSize = context.getResources().getDimensionPixelSize(R.dimen.symbolsTextSize);
        symbolPaint.setTextSize(symbolsTextSize);
        symbolPaint.setColor(Color.WHITE);

        float yPos = canvas.getHeight() / 1.5F;
        canvas.drawText(sharp,
                x + NUMBER_OF_MARKS_PER_SIDE * spaceWidth - symbolPaint.measureText(sharp) / 2F, yPos, symbolPaint);

        canvas.drawText(flat,
                x - NUMBER_OF_MARKS_PER_SIDE * spaceWidth - symbolPaint.measureText(flat) / 2F, yPos, symbolPaint);
    }



    private void drawImage(int cirlceID) {
        //metoda pentru a creea cercul de la baza pendulului
        Bitmap bitBtn = decodeSampledBitmapFromResource(context.getResources(), cirlceID, 100, 100);
        canvas.drawBitmap(bitBtn,canvas.getWidth()/2.5F,canvas.getHeight() / 1.2F,gaugePaint);
    }

    private void drawText(float x, float y, Note note, Paint textPaint, boolean textType) {
        //drawText afiseaza pe ecran nota, octava si semnul acesteia
        String noteText = getNote(note.getName());
        TextPaint noteTextPaint = new TextPaint(ANTI_ALIAS_FLAG);
        noteTextPaint.setColor(context.getResources().getColor(textColor));
        //cu ajutorul metodei drawText afisam toate textele de pe ecran
        //motivul acestui if este pentru ca dorim ca unele texte sa aibe dimensiuni mai mari
        //De ex: Nota din centra vrem sa fie mai mare, iar nota de la frecventeda de referinta mai mica
        if (textType){
            noteTextPaint.setTextSize(140);
        }
        else{
            noteTextPaint.setTextSize(textPaint.getTextSize());
        }
        float offset = noteTextPaint.measureText(noteText) / 2F;
        String sign = note.getSign();
        String octave = String.valueOf(getOctave(note.getOctave()));

        TextPaint paint = new TextPaint(ANTI_ALIAS_FLAG);
        paint.setColor(context.getResources().getColor(textColor));
        int textSize = (int) (noteTextPaint.getTextSize() / 2);
        paint.setTextSize(textSize);

        float factor;
        float distance;
        if (useScientificNotation) {
            factor = 1.5f;
            distance=1.25f;
        }
        else{
            factor = 0.75f;
            distance=1.0f;
        }

        canvas.drawText(sign, x + offset * distance, y - offset * factor, paint);
        canvas.drawText(octave, x + offset * distance, y + offset * 0.5f , paint);
        canvas.drawText(noteText, x - offset-distance, y, noteTextPaint);

    }



    private int getOctave(int octave) {
        //aici se ia octava notei
        if (useScientificNotation) {
            return octave;
        }
        if (octave <= 1) {
            return octave - 2;
        }

        return octave - 1;
    }

    private String getNote(NoteName name) {
        //aici se ia numele notei in functie de notatia selectata (solfegiu sau stintific)
        if (useScientificNotation) {
            return name.getScientific();
        }

        return name.getSol();
    }

    private void setBackground() {
        //metoda prin care schimbam culoarea background-ului(ROSU daca nota nu este corect acordata sau VERDE daca este perfect acordata) si a cercului de la baza pendulului
        symbolPaint.setColor(context.getResources().getColor(textColor));
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
        if(useScientificNotation){
            canvas.drawText(text,
                    x + sgaugeWidth - symbolPaint.measureText(text) ,
                    canvas.getHeight() / 3F, symbolPaint);
        }else{
            canvas.drawText(text,
                    x + sgaugeWidth,
                    canvas.getHeight() / 3F, symbolPaint);
        }

    }

    private int getNearestDeviation() {
        //metoda pentru a lua deviatia notei receptionate de aplicatie
        float deviation = (float) pitchDifference.deviation;
        Log.i("DeviationCanvasPainter",String.valueOf(pitchDifference.deviation));
        int rounded = Math.round(deviation);

        return Math.round(rounded / 10f) * 10;
    }


    //metode ajutatoare pentru redimensioarea PNG-urilor
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private  Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                    int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

}
