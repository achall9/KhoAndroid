package com.kholabs.khoand.Dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.kholabs.khoand.R;

/**
 * Created by Aladar-PC2 on 2/10/2018.
 */

@SuppressLint("ValidFragment")
public class AddFieldDialog extends DialogFragment {
    private EditText etField;
    private Button btnCancel, btnAdd;

    public interface AddFieldDialogCompliant {
        public void addToPopup(String field);
    }

    private  AddFieldDialogCompliant caller;

    @SuppressLint("ValidFragment")
    public AddFieldDialog(AddFieldDialogCompliant caller){
        super();
        this.caller = caller;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // R.layout.my_layout - that's the layout where your textview is placed
        getDialog().setTitle(R.string.alert_title_add_custom);

        View view = inflater.inflate(R.layout.dialog_add_field, container, false);
        etField = (EditText) view.findViewById(R.id.custom_field);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnAdd = (Button) view.findViewById(R.id.btn_add);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strField = etField.getText().toString();
                caller.addToPopup(strField);
                getDialog().dismiss();
            }
        });
        return view;
    }
}
