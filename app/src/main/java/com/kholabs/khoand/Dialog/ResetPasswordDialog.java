package com.kholabs.khoand.Dialog;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kholabs.khoand.Activity.AthleteLoginActivity;
import com.kholabs.khoand.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

@SuppressLint("ValidFragment")
public class ResetPasswordDialog extends DialogFragment{

    private EditText emailField;
    private Button btnCancel, btnReset;
    private String resetEmail;

    public interface ResetPasswordDialogCompliant {
        public void doScanEmail(String email);
    }

    private  ResetPasswordDialogCompliant caller;

    @SuppressLint("ValidFragment")
    public ResetPasswordDialog(ResetPasswordDialogCompliant caller){
        super();
        this.caller = caller;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // R.layout.my_layout - that's the layout where your textview is placed
        getDialog().setTitle(R.string.alert_title_reset_password);

        View view = inflater.inflate(R.layout.dialog_reset_password, container, false);
        emailField = (EditText) view.findViewById(R.id.reset_email_field);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnReset = (Button) view.findViewById(R.id.btn_reset);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetEmail = emailField.getText().toString();
                caller.doScanEmail(resetEmail);
                getDialog().dismiss();
            }
        });
        return view;
    }
}
