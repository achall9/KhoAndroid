package com.kholabs.khoand.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityBase;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.Helper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;

public class TherapistSignupActivity extends ActivityBase implements ConstValues, View.OnClickListener{

    private EditText nameField, emailField, passwordField, confirmField;
    private CheckBox termCheck;
    private TextView termText;
    private String name, email, password, confirm;

    private LinearLayout rootLayout;

    LinearLayout llSignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athlete_signup);

        getUIObjects();
    }

    private void getUIObjects()
    {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        rootLayout = (LinearLayout)findViewById(R.id.rootLayout);
        nameField = (EditText)findViewById(R.id.name_field);
        emailField = (EditText)findViewById(R.id.email_field);
        passwordField = (EditText)findViewById(R.id.password_field);
        confirmField = (EditText)findViewById(R.id.confirm_field);
        termCheck = (CheckBox)findViewById(R.id.checkbox_term);
        termText = (TextView)findViewById(R.id.textbox_term);

        llSignup = (LinearLayout)findViewById(R.id.llSignup);
        llSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nameField.getText().toString();
                email = emailField.getText().toString();
                password = passwordField.getText().toString();
                confirm = confirmField.getText().toString();
                //TherapistSignupActivity.this.finish();
                if (verifyReqFields())
                    isEmailAvailable(email, name, password);
            }
        });

        termText.setOnClickListener(this);
    }

    private void isEmailAvailable(final String email, final String name, final String password)
    {
        final boolean bExist = false;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        query.whereEqualTo("email", email);
        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null)
                {
                    if (objects == null || objects.size() == 0)
                        signUp(name, email, password);
                    else
                    {
                        AlertDialog alertDialog = new AlertDialog.Builder(TherapistSignupActivity.this).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage("You already have an account registed with us. Please try logging in.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Dismiss",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        return;
                    }
                }
                else
                {
                    return;
                }
            }
        });
    }

    private void signUp(String name, String email, String password)
    {
        ParseUser user = new ParseUser();
        user.put("name", name);
        user.put("isTherapist", true);
        user.setPassword(password);
        user.setUsername(email);
        user.setEmail(email);

        hideKeyboardFrom(this);

        showpDialog();
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    showAlertforProvidingDoc();
                } else {
                    final String title = "Error";
                    alertDisplayer(title, e.getLocalizedMessage());
                }

                hidepDialog();
            }
        });
    }

    private void showAlertforProvidingDoc()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(TherapistSignupActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        TextView myMsg = new TextView(this);
        myMsg.setText(getString(R.string.alert_provide_document_therapist));
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        builder.setCancelable(false);

        builder.setNeutralButton("Okay!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MyApp.getInstance().pref.createLoginSession(email);
                Intent intent = new Intent(TherapistSignupActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void hideKeyboardFrom(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            rootLayout.requestFocus();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private Boolean verifyReqFields()
    {
        nameField.setError(null);
        emailField.setError(null);
        confirmField.setError(null);
        passwordField.setError(null);

        Helper helper = new Helper();
        Animation shake = AnimationUtils.loadAnimation(TherapistSignupActivity.this, R.anim.shake);


        if (nameField.length() == 0)
        {
            nameField.setError("Required fields are empty");
            nameField.startAnimation(shake);
            return false;
        }

        if (emailField.length() == 0)
        {
            emailField.setError("Required fields are empty");
            emailField.startAnimation(shake);
            return false;
        }

        if (!helper.isValidEmail(email)) {
            emailField.setError("Wrong format");
            emailField.startAnimation(shake);
            return false;
        }

        if (passwordField.length() == 0)
        {
            passwordField.setError("Required fileds are empty");
            passwordField.startAnimation(shake);
            return false;
        }

        if (confirmField.length() == 0)
        {
            confirmField.setError("Required fileds are empty");
            confirmField.startAnimation(shake);
            return false;
        }

        if (termCheck.isChecked() == false)
        {
            termCheck.startAnimation(shake);
            return false;
        }

        if (password.equals(confirm) == false)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(TherapistSignupActivity.this, android.R.style.Theme_Holo_Light_Dialog);
            TextView myMsg = new TextView(this);
            myMsg.setText("Passwords do not match.");
            myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
            builder.setView(myMsg);

            TextView title = new TextView(this);
            title.setText("Warning");
            //title.setBackgroundColor(Color.DKGRAY);
            title.setPadding(10, 10, 10, 10);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.BLUE);
            title.setTextSize(20);
            builder.setCustomTitle(title);
            builder.setCancelable(false);

            builder.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.textbox_term:
                String url = meta_string_termurl;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApp.getInstance().setCurrentActivity(this);
    }

    void alertDisplayer(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TherapistSignupActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        TextView myMsg = new TextView(this);
        myMsg.setText(message);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        TextView txtTitle = new TextView(this);
        txtTitle.setText(title);
        //title.setBackgroundColor(Color.DKGRAY);
        txtTitle.setPadding(10, 10, 10, 10);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextColor(Color.BLUE);
        txtTitle.setTextSize(20);
        builder.setCustomTitle(txtTitle);
        builder.setCancelable(false);

        builder.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

