package com.github.Tuner;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.github.Tuner.ListenerFragment.TaskCallbacks;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinner.OnItemSelectedListener;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.shawnlin.numberpicker.NumberPicker;
import com.shawnlin.numberpicker.NumberPicker.OnValueChangeListener;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements TaskCallbacks,
        OnItemSelectedListener, OnValueChangeListener, View.OnClickListener {

    public static final int RECORD_AUDIO_PERMISSION = 0;
    public static final String PREFS_FILE = "prefs_file";
    public static final String USE_SCIENTIFIC_NOTATION = "use_scientific_notation";
    public static final String CURRENT_TUNING = "current_tuning";
    public static final String SWITCH_STATE = "switch_state";
    protected static final String REFERENCE_PITCH = "reference_pitch";
    private static final String TAG_LISTENER_FRAGMENT = "listener_fragment";
    private static int tuningPosition = 0;
    private static int referencePitch;
    private static int referencePosition;
    private Switch mySwitch;
    private ImageButton nextButton,previousButton;
    private static ImageView LIndicator, RIndicator;
    private static AnimationDrawable running_left_indicator,running_right_indicator;
    private static boolean isAutoModeEnabled = true;

    public static Tuning getCurrentTuning() {
        return TuningMapper.getTuningFromPosition(tuningPosition);
    }

    public static int getReferencePitch() {
        return referencePitch;
    }

    public static boolean isAutoModeEnabled() {
        return isAutoModeEnabled;
    }

    public static int getReferencePosition() {
        return referencePosition - 1; //ca sa se potriveasca pentru pozitia in modul AUTO
    }

    public static ImageView getleftindicator(){
        return LIndicator;
    }

    public static ImageView getrightindicator(){
        return RIndicator;
    }

    public static AnimationDrawable getLeftAnimation(){
        LIndicator.setImageResource(R.drawable.left_indicator);
        running_left_indicator = (AnimationDrawable) LIndicator.getDrawable();
        return running_left_indicator;
    }
    public static AnimationDrawable getRightAnimation(){
        RIndicator.setImageResource(R.drawable.right_indicator);
        running_right_indicator = (AnimationDrawable) RIndicator.getDrawable();
        return running_right_indicator;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        setContentView(R.layout.activity_main);
        setActivityBackgroundColor(0xFF212121); //setarea culorii fundalului pe negru

        mySwitch = (Switch) findViewById(R.id.simpleSwitch); //mySwitch este gliderul pentru controlul modului de AUTO

        //cand AUTO este OFF cu nextButton si cu previousButton selectam o singura nota pe care vrem sa o acordam din setul de note specific instrumentului selectat
        nextButton = (ImageButton) findViewById(R.id.nextChord);
        previousButton = (ImageButton) findViewById(R.id.previousChord);

        //ImageView-urile pentru aniamtia pendulului spre stanga/dreapta
        LIndicator = (ImageView)findViewById(R.id.indicator);
        RIndicator = (ImageView)findViewById(R.id.indicator1);

        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);

        mySwitch.setChecked(true); //Modul Auto este intodeauna ON la pornirea aplicatiei
        nextButton.setEnabled(false);
        nextButton.setVisibility(View.INVISIBLE); //Asta inseamna ca butoanele pentru selectarea unei note sunt inactive
        previousButton.setEnabled(false);
        previousButton.setVisibility(View.INVISIBLE);

        SwitchState(mySwitch);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestRecordAudioPermission();
        } else {
            startRecording();
        }
        setTuning();
        setReferencePitch();


        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");
        myToolbar.showOverflowMenu();
        myToolbar.setBackgroundColor(0xfff5c71a);
        setSupportActionBar(myToolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //butonul de meniu cu urmatoarele optiuni:
        //set notation
        //set reference pitch
        //about
        switch (item.getItemId()) {
            case R.id.set_notation: {

                final SharedPreferences preferences = getSharedPreferences(PREFS_FILE,
                        MODE_PRIVATE);
                final boolean useScientificNotation =
                        preferences.getBoolean(USE_SCIENTIFIC_NOTATION, true);

                int checkedItem = useScientificNotation ? 0 : 1;

                Builder builder = new Builder(new ContextThemeWrapper(this,
                        R.style.AppTheme));
                builder.setTitle(R.string.choose_notation);
                builder.setSingleChoiceItems(R.array.notations, checkedItem,
                        (dialog, which) -> {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(USE_SCIENTIFIC_NOTATION, which == 0);
                            editor.apply();

                            dialog.dismiss();

                            TunerView tunerView = findViewById(R.id.pitch);
                            tunerView.invalidate();
                        });
                builder.show();

                break;
            }
            case R.id.set_reference_pitch: {
                final SharedPreferences preferences = getSharedPreferences(PREFS_FILE,
                        MODE_PRIVATE);
                int referencePitch = preferences.getInt(REFERENCE_PITCH, 440);

                NumberPickerDialog dialog = new NumberPickerDialog();

                Bundle bundle = new Bundle();
                bundle.putInt("current_value", referencePitch);
                dialog.setArguments(bundle);

                dialog.setValueChangeListener(this);
                dialog.show(getSupportFragmentManager(), "reference_pitch_picker");

                break;
            }
        }

        return false;
    }
    @Override
    public void onClick(View v) {
        //metoda pentru selectarea notei in modul auto off
        String[] displayedValues = getNotes();
        switch (v.getId()){
            case R.id.nextChord:
                if (referencePosition==1){
                    referencePosition=displayedValues.length;
                    break;
                }
                if (referencePosition>1){
                    referencePosition--;
                }
                break;
            case R.id.previousChord:
                if (referencePosition==displayedValues.length){
                    referencePosition=1;
                    break;
                }
                if (referencePosition<displayedValues.length){
                    referencePosition++;
                }
                break;
            default:referencePosition=displayedValues.length;
        }
        TunerView tunerView = this.findViewById(R.id.pitch);
        tunerView.invalidate();
    }

    @Override
    public void onProgressUpdate(PitchDifference pitchDifference) {
        //pitchDifference contine closest si deviation
        //Cum se paseaza pitchDifference de la o clasa la alta?
        //Din ListenerFragment se publica progesul si se transmite average pitchDifference mai departe la MainActivity
        //pitchDifference se trimite in tunerview care il paseaza catre canvas painter pentru a se afisa pe ecran
        TunerView tunerView = this.findViewById(R.id.pitch);

        tunerView.setPitchDifference(pitchDifference);
        tunerView.invalidate();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //cererea de permisiune pentru folosirea microfonului
        if (requestCode == RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording();
                } else {
                    AlertDialog alertDialog = new Builder(MainActivity.this).create();
                    alertDialog.setTitle(R.string.permission_required);
                    alertDialog.setMessage(getString(R.string.microphone_permission_required));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                            (dialog, which) -> {
                                dialog.dismiss();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    finishAffinity();
                                } else {
                                    finish();
                                }
                            });
                    alertDialog.show();
                }
            }
        }
    }

    @Override
    public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
        //selectarea in spinner a instrumentului pentru acordare
        final SharedPreferences preferences = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CURRENT_TUNING, position);
        editor.apply();

        tuningPosition = position;

        recreate();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldValue, int newValue) {
        //schimbarea valorii frecventei de referinta
        String tag = String.valueOf(picker.getTag());
        if ("reference_pitch_picker".equalsIgnoreCase(tag)) {
            final SharedPreferences preferences = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(REFERENCE_PITCH, newValue);
            editor.apply();

            setReferencePitch();

            TunerView tunerView = this.findViewById(R.id.pitch);
            tunerView.invalidate();
        }
    }

    private void startRecording() {
        //inceperea inregistrarii sunetului
        FragmentManager fragmentManager = getSupportFragmentManager(); //fragmentManager este responsabil pentru toate fragmentele (aduagre/sterge/etc)
        ListenerFragment listenerFragment = (ListenerFragment) fragmentManager.findFragmentByTag(TAG_LISTENER_FRAGMENT); //cautam cu findFragmentByTag fragmentul nostru, adica listenerFragment

        if (listenerFragment == null) {
            listenerFragment = new ListenerFragment();
            fragmentManager
                    .beginTransaction() //incepe o serie de operatii de edit pe fragmentele asociate acestui fragmentmanager
                    .add(listenerFragment, TAG_LISTENER_FRAGMENT) //adaugarea listenerFragment in lifecycle-ul de activitate
                    .commit(); //comiterea adaugarii si incepe sa se foloseasca listenerFragment
                               //listenerFragment la onCreate deja incepe sa asculte inputu microfonului si sa calculeze frecventa
        }
    }

    private void setTuning() {
        //Tuning in codul nostru inseamna selectarea instrumentului si a modului de acordare (de ex: Guitar Standard, Guitar Drop D)
        final SharedPreferences preferences = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        //in preferences tinem minte ultimu mod selectat iar la redeschiderea aplicatiei modul va fi cel ramas
        tuningPosition = preferences.getInt(CURRENT_TUNING, 0);
        int textColorDark = getResources().getColor(R.color.colorTextDark);

        MaterialSpinner spinner = findViewById(R.id.tuning);
        MaterialSpinnerAdapter<String> adapter = new MaterialSpinnerAdapter<>(this, Arrays.asList(getResources().getStringArray(R.array.tunings)));

        spinner.setTextColor(textColorDark);
        spinner.setBackgroundColor(0xfff5c71a);
        spinner.setTextColor(textColorDark);
        spinner.setArrowColor(textColorDark);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelectedIndex(tuningPosition);
    }


    private void setReferencePitch() {
        //setarea initiala a freceventei de referinta in functie de salvarile facute in fisierul de preferinte daca exista (PREFS_FILE)
        final SharedPreferences preferences = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        referencePitch = preferences.getInt(REFERENCE_PITCH, 440);
    }

    private void requestRecordAudioPermission() {
        //cererea de permisiune pentru inregistrare a sunetului prin microfon
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION);
    }

    private void SwitchState(Switch mySwitch){
        //setarea Switch-ului pe AUTO ON/OFF

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mySwitch.isChecked()){
                    mySwitch.setChecked(true);
                    isAutoModeEnabled=true;
                    nextButton.setEnabled(false);
                    nextButton.setVisibility(View.INVISIBLE);
                    previousButton.setEnabled(false);
                    previousButton.setVisibility(View.INVISIBLE);
                    referencePosition=0;
                    Toast.makeText(getApplicationContext(),"Auto Mode is enabled",Toast.LENGTH_SHORT).show();
                }else{
                    mySwitch.setChecked(false);
                    isAutoModeEnabled=false;
                    nextButton.setEnabled(true);
                    nextButton.setVisibility(View.VISIBLE);     //daca modific switchu sa se faca vizibile butoanele in cadrul aceluiasi instrument
                    previousButton.setEnabled(true);
                    previousButton.setVisibility(View.VISIBLE);
                    String[] displayedValues = getNotes();
                    referencePosition=displayedValues.length;
                    Toast.makeText(getApplicationContext(),"Auto Mode is disabled",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String[] getNotes(){
        //in fucntie de notatia selectata si de instrument returneaza Array de String-uri cu numele complet al notelor(inclusiv semnul si octava)
        SharedPreferences preferences = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        boolean useScientificNotation = preferences.getBoolean(USE_SCIENTIFIC_NOTATION, true);
        Note[] notes = getCurrentTuning().getNotes();
        String[] displayedValues = new String[notes.length];

        for (int i = 0; i < notes.length; i++) {
            Note note = notes[i];
            NoteName name = note.getName();
            String noteName = name.getScientific();
            int octave = note.getOctave();
            if (!useScientificNotation) {
                noteName = name.getSol();
                if (octave <= 1) {
                    octave = octave - 2;
                }
                octave = octave - 1;
            }
            displayedValues[i] = noteName + note.getSign() + octave;
        }
        return  displayedValues;
    }

    public void setActivityBackgroundColor(int color) {
        //metoda pentru schimbarea culorii de background al unei activitati
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }


}
