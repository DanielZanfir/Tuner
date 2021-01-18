package com.github.Tuner;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.shawnlin.numberpicker.NumberPicker;

public class NotePickerDialog extends DialogFragment {

    private NumberPicker.OnValueChangeListener valueChangeListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final NumberPicker numberPicker = new NumberPicker(getActivity());
        numberPicker.setTag("note_picker");

        Bundle arguments = getArguments();
        boolean useScientificNotation = arguments.getBoolean("use_scientific_notation", true);
        int currentValue = arguments.getInt("current_value", 0);

        Note[] notes = MainActivity.getCurrentTuning().getNotes();

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(notes.length);
        if (currentValue < notes.length) {
            numberPicker.setValue(currentValue);
        } else {
            numberPicker.setValue(0);
        }

        String[] displayedValues = new String[notes.length];

        for (int i = 0; i < notes.length; i++) {
            Note note = notes[i];
            NoteName name = note.getName();
            String noteName = name.getScientific();
            int octave = note.getOctave();
            if (!useScientificNotation) {
                noteName = name.getSol();

                //TODO Extract method
                if (octave <= 1) {
                    octave = octave - 2;
                }

                octave = octave - 1;
            }
            displayedValues[i] = noteName + note.getSign() + octave;
        }

        numberPicker.setDisplayedValues(displayedValues);

        int textColorDark = getResources().getColor(R.color.colorWhiteText);
        numberPicker.setTextColor(textColorDark);
        numberPicker.setDividerColor(textColorDark);
        numberPicker.setSelectedTextColor(textColorDark);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
                R.style.AppTheme));
        builder.setMessage("Choose a note");

        builder.setPositiveButton("OK",
                (dialog, which) -> valueChangeListener.onValueChange(numberPicker,
                        numberPicker.getValue(), numberPicker.getValue()));

        builder.setNegativeButton("CANCEL", (dialog, which) -> {
        });

        builder.setView(numberPicker);
        return builder.create();
    }

    @Override
    public void onPause() {
        super.onPause();

        this.dismiss();
    }

    void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }
}
