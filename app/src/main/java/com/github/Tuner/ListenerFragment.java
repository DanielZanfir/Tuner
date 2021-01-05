package com.github.Tuner;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
//Un fragment este o portiune din UI-ul aplicatiei independenta, cu propriul life cycle
//ListenerFragment este un wrapper cu metode de lifecycle pentru PitchListener
//PitchListener este o clasa care executa metode asincron
public class ListenerFragment extends Fragment {

    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 1024 * 4;
    private static final int OVERLAP = 768 * 4;
    private static final int MIN_ITEMS_COUNT = 15;
    static boolean IS_RECORDING;
    private static List<PitchDifference> pitchDifferences = new ArrayList<>();
    private static TaskCallbacks taskCallbacks;
    private PitchListener pitchListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        taskCallbacks = (TaskCallbacks) context;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            taskCallbacks = (TaskCallbacks) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //la crearea lui ListenerFragment imediat se creaza o instanta de PitchListener si se executa
        //adica incepe imediat sa asculte inputu de la microfon si sa calculeze frecventa
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        pitchListener = new PitchListener();
        pitchListener.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        taskCallbacks = null;
        pitchListener.cancel(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        pitchListener.cancel(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (pitchListener.isCancelled()) {
            pitchListener = new PitchListener();
            pitchListener.execute();
        }
    }

    interface TaskCallbacks {
        //clasa care implementeaza TaskCallbacks permite sa primeasca date asincron de la o clasa asincrona
        void onProgressUpdate(PitchDifference percent);
    }

    private static class PitchListener extends AsyncTask<Void, PitchDifference, Void> {

        private AudioDispatcher audioDispatcher;

        //doInBackground pe scurt, captureaza sunetul si calculeaza frecventa
        @Override
        protected Void doInBackground(Void... params) {
            //PitchDetectionHandler se ocupa de primirea frecventei rezultate din PitchProcessor si de calcularile ulterioare ale mediei
            PitchDetectionHandler pitchDetectionHandler = (pitchDetectionResult, audioEvent) -> {

                if (isCancelled()) {
                    stopAudioDispatcher();
                    return;
                }

                if (!IS_RECORDING) {
                    IS_RECORDING = true;
                    publishProgress(); //aceasta metoda publica update-uri catre thread-ul UI-ului in timp ce doInBackground inca ruleaza
                    //orice apel la aceasta metoda va declansa executia metodei onProgressUpdate(PitchDifference... pitchDifference)
                }

                float pitch = pitchDetectionResult.getPitch(); //aceasta este freceventa inputului calculata

                if (pitch != -1) {
                    PitchDifference pitchDifference = PitchComparator.retrieveNote(pitch);
                    //PitchDifference este un o clasa wrapper pentru 2 chestii: closest si deviation(minCentDiff)
                    pitchDifferences.add(pitchDifference);
                    Log.i("PitchDiferenceNote",pitchDifference.closest.getName().toString()+pitchDifference.closest.getSign()+pitchDifference.closest.getOctave()+" "+pitchDifference.deviation);
                    if (pitchDifferences.size() >= MIN_ITEMS_COUNT) {
                        PitchDifference average = Sampler.calculateAverageDifference(pitchDifferences);
                                //Sampler calculeaza average pentru un nr dat de deviatii
                                //in cazul nostru un average pentru 15 deviatii
                        publishProgress(average);//average este de fapt average deviation
                        Log.i("AveragepitchDif",String.valueOf(average.deviation));
                        pitchDifferences.clear();
                    }
                }
            };
            //PitchProcessor este o clasa de tarsos lib care se foloseste pe o anumita imprimare a sunetului doar in scopul calcularii frecventei
            PitchProcessor pitchProcessor = new PitchProcessor(PitchEstimationAlgorithm.FFT_YIN,
                    SAMPLE_RATE,
                    BUFFER_SIZE, pitchDetectionHandler);

            //audioDispatcher este o clasa din tarsos lib prin care se selecteaza pe care sunet se aplica procesarea si ce fel de procesare
            //pe care sunet -> sunetul din DefaultMicrophone
            //ce fel de procesare -> pitchProcessor
            audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE,
                    BUFFER_SIZE, OVERLAP);

            audioDispatcher.addAudioProcessor(pitchProcessor);

            audioDispatcher.run();

            return null;
        }

        @Override
        protected void onCancelled(Void result) {
            stopAudioDispatcher();
        }

        @Override
        protected void onProgressUpdate(PitchDifference... pitchDifference) {
            if (taskCallbacks != null) {
                if (pitchDifference.length > 0) {
                    taskCallbacks.onProgressUpdate(pitchDifference[0]);
                } else {
                    taskCallbacks.onProgressUpdate(null);
                }
            }
        }

        private void stopAudioDispatcher() {
            if (audioDispatcher != null && !audioDispatcher.isStopped()) {
                audioDispatcher.stop();
                IS_RECORDING = false;
            }
        }
    }
}
