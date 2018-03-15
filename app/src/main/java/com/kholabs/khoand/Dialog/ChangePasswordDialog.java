package com.kholabs.khoand.Dialog;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.R;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("ValidFragment")
public class ChangePasswordDialog extends DialogFragment{

    private EditText etNewPassword, etConfirmPassword;
    private Button btnCancel, btnChange;
    private String strNew, strConfirm;
    private PasswordChangeListener caller;

    public interface PasswordChangeListener {
        public void onPasswordChanged();
    }

    @SuppressLint("ValidFragment")
    public ChangePasswordDialog(PasswordChangeListener _caller){
        super();
        this.caller = _caller;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // R.layout.my_layout - that's the layout where your textview is placed
        getDialog().setTitle(R.string.alert_title_change_password);

        View view = inflater.inflate(R.layout.dialog_change_password, container, false);
        etNewPassword = (EditText) view.findViewById(R.id.et_new_password);
        etConfirmPassword = (EditText) view.findViewById(R.id.et_confirm_password);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnChange = (Button) view.findViewById(R.id.btn_change);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
                strNew = etNewPassword.getText().toString();;
                strConfirm = etConfirmPassword.getText().toString();
                if (strNew.equals("") || strConfirm.equals(""))
                    MyApp.getInstance().alertDisplayer("Error", "Missing fields info");
                else if (!strNew.equals(strConfirm))
                {
                    MyApp.getInstance().alertDisplayer("Error", "Password do not match");
                }
                else
                {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    Map<String, String> requestParams = new HashMap<String, String>();
                    requestParams.put("username", currentUser.getUsername());
                    requestParams.put("newPassword", strNew);

                    ParseCloud.callFunctionInBackground("changeUserPassword", requestParams, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object object, ParseException e) {
                            if (e != null) {
                                MyApp.getInstance().alertDisplayer("Error", e.getLocalizedMessage());
                                return;
                            }

                            caller.onPasswordChanged();

                        }
                    });
                }
            }
        });
        return view;
    }
}
