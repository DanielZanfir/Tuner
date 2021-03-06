package com.github.Tuner;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import com.shawnlin.numberpicker.NumberPicker;

import androidx.appcompat.app.AlertDialog.Builder;
import androidx.fragment.app.DialogFragment;

//dialogul/fereastra pentru alegerea freceventei de referinta
public class NumberPickerDialog extends DialogFragment {

    private NumberPicker.OnValueChangeListener valueChangeListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final NumberPicker numberPicker = new NumberPicker(getActivity());
        numberPicker.setTag("reference_pitch_picker");

        Bundle arguments = getArguments();
        int currentValue = arguments.getInt("current_value", 440);

        numberPicker.setMinValue(400);
        numberPicker.setMaxValue(500);
        numberPicker.setValue(currentValue);

        int textColorDark = getResources().getColor(R.color.colorWhiteText);
        numberPicker.setTextColor(textColorDark);
        numberPicker.setDividerColor(textColorDark);
        numberPicker.setSelectedTextColor(textColorDark);



        Builder builder = new Builder(new ContextThemeWrapper(getActivity(),
                R.style.AppTheme));
        builder.setMessage(R.string.choose_a_frequency);

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
