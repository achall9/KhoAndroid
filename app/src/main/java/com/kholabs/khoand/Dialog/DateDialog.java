package com.kholabs.khoand.Dialog;

/**
 * Created by Aladar-PC2 on 1/8/2018.
 */

import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.kholabs.khoand.R;

@SuppressLint("ValidFragment")
public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    EditText txtdate;
    public DateDialog(View view){
        txtdate=(EditText)view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {


// Use the current date as the default date in the dialog
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Dialog, this, year, month, day);
        pickerDialog.getDatePicker().setCalendarViewShown(false);
        pickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        pickerDialog.show();
        return pickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        //show to the selected date in the text box
        String date=day+"/"+(month+1)+"/"+year;
        txtdate.setText(date);
        txtdate.clearFocus();
    }



}