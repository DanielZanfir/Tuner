package com.github.Tuner;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.IntStream;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity implements TaskCallbacks,
        OnItemSelectedListener, OnValueChangeListener, View.OnClickListener {

    public static final int RECORD_AUDIO_PERMISSION = 0;
    public static final String PREFS_FILE = "prefs_file";
    public static final String USE_SCIENTIFIC_NOTATION = "use_scientific_notation";
    public static final String CURRENT_TUNING = "current_tuning";
    public static final String SWITCH_STATE = "switch_state";
    public static final String TEXT_TO_SPEAK_KEY = "text_to_speak";
    protected static final String REFERENCE_PITCH = "reference_pitch";
    private static final String TAG_LISTENER_FRAGMENT = "listener_fragment";
    private static final String QUEUE_KEY = "speech_queue";
    private static int tuningPosition = 0;
    private static int referencePitch;
    private static int referencePosition;
    private Switch mySwitch;
    private ImageButton nextButton,previousButton;
    private Button noteChange;
    private static ImageView LIndicator, RIndicator;
    private static AnimationDrawable running_left_indicator,running_right_indicator;
    private static boolean isAutoModeEnabled = true;

    public static TextToSpeech textToSpeech;
    private Queue<String> textQueue;
    ListenerFragment listenerFragment;
    private SpeechRecognizer speechRecognizer;
    Intent speechIntent = null;

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

        setContentView(R.layout.activity_main);
        setActivityBackgroundColor(0xFF212121); //setarea culorii fundalului pe negru

        mySwitch = (Switch) findViewById(R.id.simpleSwitch); //mySwitch este gliderul pentru controlul modului de AUTO

        //cand AUTO este OFF cu nextButton si cu previousButton selectam o singura nota pe care vrem sa o acordam din setul de note specific instrumentului selectat
        nextButton = (ImageButton) findViewById(R.id.nextChord);
        previousButton = (ImageButton) findViewById(R.id.previousChord);
        noteChange =(Button) findViewById(R.id.noteChange) ;

        //ImageView-urile pentru aniamtia pendulului spre stanga/dreapta
        LIndicator = (ImageView)findViewById(R.id.indicator);
        RIndicator = (ImageView)findViewById(R.id.indicator1);

        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        noteChange.setOnClickListener(this);

        mySwitch.setChecked(true); //Modul Auto este intodeauna ON la pornirea aplicatiei
        nextButton.setEnabled(false);
        nextButton.setVisibility(View.INVISIBLE); //Asta inseamna ca butoanele pentru selectarea unei note sunt inactive
        previousButton.setEnabled(false);
        previousButton.setVisibility(View.INVISIBLE);
        noteChange.setEnabled(false);
        noteChange.setVisibility(View.INVISIBLE);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechInitialize();
        textToSpeech = new TextToSpeech(this, status -> {
            verifyTextToSpeechStatus(status);
            setActivityStartPopUp();
        });
        textQueue = new LinkedList<>();

        SwitchState(mySwitch);

        setTuning();
        setReferencePitch();


        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");
        myToolbar.showOverflowMenu();
        myToolbar.setBackgroundColor(0xffffad1d);
        setSupportActionBar(myToolbar);


    }


    private void setActivityStartPopUp() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            addToQueue("Welcome to Tuning audio permission! In order to use the application please grant access to use the microphone");
            processTextQueue();
            requestRecordAudioPermission();

        } else {
            addToQueue(getWelcomeMessage());
            processTextQueue();
            startSpeechRecognition();
        }

    }

    private void verifyTextToSpeechStatus(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language not supported, handle accordingly
            } else {
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        // Utterance started, handle accordingly
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        // Utterance completed, handle accordingly
                        processTextQueue();
                        startSpeechRecognition();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        // Error occurred, handle accordingly
                    }
                });
            }
        } else {
            // Initialization failed, handle accordingly
        }
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
            case R.id.about_us:{
                Intent myIntent = new Intent(this, AboutActivity.class);
                startActivity(myIntent);
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
                if (referencePosition==displayedValues.length){
                    referencePosition=1;
                    break;
                }
                if (referencePosition>=1){
                    referencePosition++;
                }
                break;
            case R.id.previousChord:
                if (referencePosition==1){
                    referencePosition=displayedValues.length;
                    break;
                }
                if (referencePosition<=displayedValues.length){
                    referencePosition--;
                }
                break;
            case R.id.noteChange:
                final SharedPreferences preferences = getSharedPreferences(PREFS_FILE,
                        MODE_PRIVATE);
                NotePickerDialog dialog = new NotePickerDialog();

                Bundle bundle = new Bundle();
                bundle.putBoolean("use_scientific_notation", preferences.getBoolean(
                        MainActivity.USE_SCIENTIFIC_NOTATION, true));
                bundle.putInt("current_value", referencePosition);
                dialog.setArguments(bundle);

                dialog.setValueChangeListener(this);
                dialog.show(getSupportFragmentManager(), "note_picker");
                break;

            //default:referencePosition=displayedValues.length;
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

        if (pitchDifference != null) {
            int deviation = (int) pitchDifference.deviation;
            Log.i("PitchDifMainAcc", pitchDifference.closest.getName().name() + pitchDifference.closest.getSign() + pitchDifference.closest.getOctave());
            Log.i("PitchDifDeviation", ""+pitchDifference.deviation);
            addToQueue("Note played is " + pitchDifference.closest.getName().name() + pitchDifference.closest.getSign() + pitchDifference.closest.getOctave() +
                    "and the deviation is " + deviation);
            processTextQueue();
        }
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
                    addToQueue("Permission granted! Welcome to Tuning App. You can start to tune or adjust some settings. Do you want to know what you can change and the vocal commands to do it?");
                    processTextQueue();
                    startSpeechRecognition();

                } else {
                    addToQueue("Permission denied! In order to use the application please reopen the application and grant access to use the microphone");
                    processTextQueue();
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
        MaterialSpinnerAdapter<String> adapter = new MaterialSpinnerAdapter<>(this, asList(getResources().getStringArray(R.array.tunings)));
        tuningPositionSet(position, preferences, adapter);
    }

    private void tuningPositionSet(int position, SharedPreferences preferences, MaterialSpinnerAdapter<String> adapter) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CURRENT_TUNING, position);
        editor.apply();

        addToQueue("Current tuning mode is " + adapter.get(position));
        Log.i("tuningPosition", adapter.get(position));
        processTextQueue();
        tuningPosition = position;

        TunerView tunerView = this.findViewById(R.id.pitch);
        tunerView.invalidate();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldValue, int newValue) {
        //schimbarea valorii frecventei de referinta
        String tag = String.valueOf(picker.getTag());
        if ("reference_pitch_picker".equalsIgnoreCase(tag)) {
            addReferencePitch(newValue);
        } else if ("note_picker".equalsIgnoreCase(tag)) {
            referencePosition = newValue;

            speakSelectedNote(newValue);

            TunerView tunerView = this.findViewById(R.id.pitch);
            tunerView.invalidate();
        }
    }

    private void speakSelectedNote(int newValue) {
        String[] displayedValues = getNotes();
        addToQueue("Current note is " + noteToBeSpoken(newValue, displayedValues));
        processTextQueue();
    }

    private void addReferencePitch(int newValue) {
        final SharedPreferences preferences = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(REFERENCE_PITCH, newValue);
        editor.apply();

        setReferencePitch();
        addToQueue("Reference set to " + newValue);
        processTextQueue();


        TunerView tunerView = this.findViewById(R.id.pitch);
        tunerView.invalidate();
    }

    private String noteToBeSpoken(int newValue, String[] displayedValues) {
        if (displayedValues[newValue-1].contains("-")){
            String[] parts = displayedValues[newValue-1].split("-");
            String toBeSpoken = parts[0] + " -" + parts[1];
            Log.i("NoteDisplayedWithSplit", toBeSpoken);
            return toBeSpoken;

        }else{
            return displayedValues[newValue-1];
        }
    }

    private void startRecording() {
        //inceperea inregistrarii sunetului
        FragmentManager fragmentManager = getSupportFragmentManager(); //fragmentManager este responsabil pentru toate fragmentele (aduagre/sterge/etc)
        listenerFragment = (ListenerFragment) fragmentManager.findFragmentByTag(TAG_LISTENER_FRAGMENT); //cautam cu findFragmentByTag fragmentul nostru, adica listenerFragment

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
        int textColorDark = getResources().getColor(R.color.colorWhiteText);

        MaterialSpinner spinner = findViewById(R.id.tuning);
        MaterialSpinnerAdapter<String> adapter = new MaterialSpinnerAdapter<>(this, asList(getResources().getStringArray(R.array.tunings)));

        spinner.setTextColor(textColorDark);
        spinner.setBackgroundColor(0xffffad1d);
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
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mySwitch.setChecked(true);
                    isAutoModeEnabled=true;
                    nextButton.setEnabled(false);
                    nextButton.setVisibility(View.INVISIBLE);
                    previousButton.setEnabled(false);
                    previousButton.setVisibility(View.INVISIBLE);
                    noteChange.setEnabled(false);
                    noteChange.setVisibility(View.INVISIBLE);
                    referencePosition=0;
                    Toast.makeText(getApplicationContext(),"Auto Mode is enabled",Toast.LENGTH_SHORT).show();
                    Log.i("Switch", "e on");
                    addToQueue("Auto mode is enabled");
                    processTextQueue();
                    TunerView tunerView = findViewById(R.id.pitch);
                    tunerView.invalidate();
                }else{
                    mySwitch.setChecked(false);
                    isAutoModeEnabled=false;
                    nextButton.setEnabled(true);
                    nextButton.setVisibility(View.VISIBLE);     //daca modific switchu sa se faca vizibile butoanele in cadrul aceluiasi instrument
                    previousButton.setEnabled(true);
                    previousButton.setVisibility(View.VISIBLE);
                    noteChange.setEnabled(true);
                    noteChange.setVisibility(View.VISIBLE);
                    String[] displayedValues = getNotes();
                    referencePosition=displayedValues.length;
                    Toast.makeText(getApplicationContext(),"Auto Mode is disabled",Toast.LENGTH_SHORT).show();
                    Log.i("Switch", "e off");
                    addToQueue("Auto mode is disabled. Current note is "+ noteToBeSpoken(displayedValues.length, displayedValues));
                    processTextQueue();
                    Handler handler =new Handler();
                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(),"Tap the note for easier selection",Toast.LENGTH_SHORT).show();
                        }
                    },3000);
                    TunerView tunerView = findViewById(R.id.pitch);
                    tunerView.invalidate();
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


    private void speak(String text) {
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            if (listenerFragment != null){
                listenerFragment.stopRecording();
            }
            if (speechRecognizer != null){
                speechRecognizer.destroy();
            }
            textToSpeech.speak(text, QUEUE_FLUSH, null);
            startSpeechRecognition();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        speechRecognizer.destroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(QUEUE_KEY, new ArrayList<>(textQueue));
    }

    private void addToQueue(String text) {
        textQueue.add(text);
    }

    private void processTextQueue() {
        if (!textQueue.isEmpty()) {
            String text = textQueue.poll();
            speak(text);
        }
    }

    private String getWelcomeMessage() {
        String switchState = isAutoModeEnabled() ? "enabled" : "disabled";
        String[] tuningMode = getCurrentTuning().getClass().getSimpleName().split("(?<!^)(?=[A-Z])");
        StringBuilder refinedTuningMode = new StringBuilder();
        for (int i = 0; i < tuningMode.length; i++){
            if (i!= tuningMode.length-1){
                refinedTuningMode.append(tuningMode[i]);
                refinedTuningMode.append(" ");
            }
        }
        Log.i("tuningModeName", refinedTuningMode.toString());
        return "Auto mode is " + switchState +". The reference pitch is " + getReferencePitch()+ ". The mode is" + refinedTuningMode.toString() + ".";
    }

    private void speechInitialize() {
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

    }

    private void startSpeechRecognition() {
            speechRecognizer.startListening(speechIntent);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    // Called when the speech recognition is ready to start
                }

                @Override
                public void onBeginningOfSpeech() {
                    // Called when the user starts speaking
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Called when the input sound level changes
                }

                @Override
                public void onBufferReceived(byte[] bytes) {

                }

                @Override
                public void onEndOfSpeech() {
                    // Called when the user stops speaking
                }

                @Override
                public void onError(int error) {
                    Log.e("speechRecognition", String.valueOf(error));
                    if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                        startSpeechRecognition();
                    }
                    if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT){
                        startSpeechRecognition();
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onResults(Bundle results) {
                    // Called when speech recognition results are available
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    Log.i("NoCommand","nu am comanda");
                        String command = matches.get(0);
                        Log.i("voiceCommand", command);
                        actionsFromVoiceInput(command);

                    startSpeechRecognition();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Called when partial recognition results are available
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Called when a speech recognition event occurs
                }
            });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void actionsFromVoiceInput(String command) {
        String commandToLowerCase = command.toLowerCase();

        if (commandToLowerCase.startsWith("set note ")){
            if (mySwitch.isChecked()){
                addToQueue("Can't select a specific note if auto mode is enabled");
                processTextQueue();
            } else {
                String[] splited = commandToLowerCase.split("\\s+");
                String note = splited[splited.length-1];
                List<String> displayedValues = asList(getNotes());

                referencePosition = IntStream.range(0, displayedValues.size())
                        .filter(i -> Objects.equals(displayedValues.get(i).toLowerCase(), note.toLowerCase()))
                        .findFirst()
                        .orElse(-1);
                referencePosition++;
                speakSelectedNote(referencePosition);

                TunerView tunerView = this.findViewById(R.id.pitch);
                tunerView.invalidate();
            }
        }

        if (commandToLowerCase.startsWith("set reference ")){
            String[] splited = commandToLowerCase.split("\\s+");
            int referenceToBeSet = Integer.parseInt(splited[splited.length-1]);
            if (referenceToBeSet >= 400 && referenceToBeSet <= 500) {
                addReferencePitch(referenceToBeSet);
            }else {
                addToQueue("Invalid value. The reference needs to be between 400 and 500");
                processTextQueue();
            }
        }

        if (commandToLowerCase.startsWith("set tuning ")){
            String[] splited = commandToLowerCase.split("\\s+");
            StringBuilder tuningMode = new StringBuilder();
            for (int i = 3; i <= splited.length-1; i ++){
                tuningMode.append(splited[i]);
                tuningMode.append(" ");
            }
            tuningMode.deleteCharAt(tuningMode.length() - 1);
            final SharedPreferences preferences = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
            MaterialSpinnerAdapter<String> adapterForVoice = new MaterialSpinnerAdapter<>(this, asList(getResources().getStringArray(R.array.tuningsVoiceInput)));
            MaterialSpinnerAdapter<String> adapter = new MaterialSpinnerAdapter<>(this, asList(getResources().getStringArray(R.array.tunings)));


            List<String> tuningModes = adapterForVoice.getItems();
            Log.i("TuningModes", tuningModes.toString());
            Log.i("tunningFromCommnad", tuningMode.toString());
            int position = IntStream.range(0, tuningModes.size())
                    .filter(i -> Objects.equals(tuningModes.get(i).toLowerCase(), tuningMode.toString().toLowerCase()))
                    .findFirst()
                    .orElse(-1);
            Log.i("Position", String.valueOf(position));

            if (position == -1){
                addToQueue("Invalid tuning mode");
                processTextQueue();
            } else {
                Log.i("positionExists", "intr aici");
                tuningPositionSet(position,preferences,adapter);
                MaterialSpinner spinner = findViewById(R.id.tuning);
                spinner.setAdapter(adapter);
                spinner.setSelectedIndex(position);
            }
        }

        switch (commandToLowerCase) {
            case "start":
            case "continue":
            case "again":
                if (listenerFragment != null){
                    listenerFragment.restartRecording();
                } else {
                    startRecording();
                }
             break;

            case "yes":
            case "help":
                addToQueue(helpCommands());
                processTextQueue();
             break;

            case "no":
                addToQueue("You can begin tuning. The settings are " + getWelcomeMessage());
                processTextQueue();
             break;

            case "enable":
                mySwitch.setChecked(true);
             break;

            case "disable":
                mySwitch.setChecked(false);
             break;
        }
    }

    private String helpCommands(){
        return "There are 14 tuning modes. The list of them is: Chromatic, Guitar standard, Guitar Drop D, Guitar Drop C, Guitar Drop C#, Guitar Open G, Bass standard, Bass Drop C, " +
                "Ukulele standard, Ukulele D tuning, Violin, Cello, Viola, Banjo. To select one of them say: select tuning to and the name of it." +
                "You can set the reference pitch by saying set reference to and the number you want." +
                "Tuning app has auto mode that automatically detects the chord or note you played. You can disable it by saying disable and you can enable it by saying enable." +
                "Before playing any note or chord say one of the following: start, again or continue." +
                "If you forgot any command just say help and this message will be played again." +
                "You can begin tuning. The settings are " + getWelcomeMessage();
    }

}

