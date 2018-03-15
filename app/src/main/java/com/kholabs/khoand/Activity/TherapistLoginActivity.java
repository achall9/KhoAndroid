package com.kholabs.khoand.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityBase;
import com.kholabs.khoand.Dialog.ResetPasswordDialog;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.Helper;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class TherapistLoginActivity extends ActivityBase implements View.OnClickListener, ResetPasswordDialog.ResetPasswordDialogCompliant {

    private LinearLayout rootLayout;
    private EditText emailField, passwordField;
    private String email, password;
    private TextView txtForgotPassword;
    Button btnLogin, btnSignup, btnFBLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_login);

        getUIObjects();
    }

    public void getUIObjects()
    {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        rootLayout = (LinearLayout)findViewById(R.id.rootLayout);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnSignup = (Button)findViewById(R.id.btnSignup);
        btnFBLogin = (Button)findViewById(R.id.btnFacebook);

        txtForgotPassword = (TextView) findViewById(R.id.txt_forgotpassword);
        emailField = (EditText)findViewById(R.id.txt_email);
        passwordField = (EditText)findViewById(R.id.txt_password);

        txtForgotPassword.setOnClickListener(this);
        txtForgotPassword.setClickable(true);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailField.getText().toString();
                password = passwordField.getText().toString();
                if (verifyReqFields())
                    loginActivity(email, password);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TherapistLoginActivity.this, TherapistSignupActivity.class);
                startActivity(intent);
            }
        });

        btnFBLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showpDialog();

                hideKeyboardFrom(TherapistLoginActivity.this);
                List<String> permissions = Arrays.asList("public_profile", "email");

                ParseFacebookUtils.logInWithReadPermissionsInBackground(TherapistLoginActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if (user == null) {
                            Toast.makeText(getApplicationContext(), "Log-out from Facebook and try again please!", Toast.LENGTH_SHORT).show();
                            ParseUser.logOut();
                            hidepDialog();
                            return;
                        }

                        user.put("isTherapist", true);
                        user.saveInBackground();

                        getUserDetailFromFB(user);

                        hidepDialog();
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.txt_forgotpassword:
                ResetPasswordDialog dialog = new ResetPasswordDialog(this);
                dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                dialog.show(ft, "Reset Password");
                break;
        }
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

    private void loginActivity(String email, String password)
    {
        showpDialog();
        hideKeyboardFrom(this);

        ParseUser.logInInBackground(email, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                hidepDialog();

                if (e != null || parseUser == null)
                {
                    alertDisplayer("Error", e.getLocalizedMessage() + " Please re-try");
                    return;
                }

                boolean isTherapist = parseUser.getBoolean("isTherapist");
                if (!isTherapist)
                {
                    alertDisplayer("Error", "You have an Athlete account.\nPlease go back and select the Athlete Login option.");
                    ParseUser.logOut();
                    return;
                }

                MyApp.getInstance().pref.createLoginSession(email);
                Intent intent = new Intent(TherapistLoginActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        MyApp.getInstance().setCurrentActivity(this);
    }

    private Boolean verifyReqFields()
    {
        emailField.setError(null);
        passwordField.setError(null);

        Helper helper = new Helper();
        Animation shake = AnimationUtils.loadAnimation(TherapistLoginActivity.this, R.anim.shake);

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

        return true;
    }

    void getUserDetailFromFB(final ParseUser user){
        String username = user.getUsername();
        String useremail = user.getEmail();
        final ParseFile userAvatar = user.getParseFile("avatar");

        if (username != null && useremail != null && userAvatar != null) {
            MyApp.getInstance().pref.createLoginSession(useremail);
            Intent intent = new Intent(TherapistLoginActivity.this, StartActivity.class);
            startActivity(intent);
            finish();

            return;
        }

        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                String facebookID = null;
                String name = "";
                String email = "";
                String birthday = "";
                try {
                    facebookID = object.getString("id");
                    name = object.getString("name");
                    email = object.getString("email");
                    user.put("name", name);
                    user.setUsername(email);
                    user.setEmail(email);

                    user.saveInBackground();

                    //saveNewUser(name, email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (userAvatar == null)
                {
                    try {
                        URL imageUrl = new URL("https://graph.facebook.com/" + facebookID + "/picture?type=large&return_ssl_resources=1");
                        byte[] imgByteArray = null;

                        Bitmap mIcon = BitmapFactory
                                .decodeStream(imageUrl.openConnection()
                                        .getInputStream());
                        if (mIcon != null)
                            imgByteArray = encodeToByteArray(mIcon);

                        final ParseFile imageFile = new ParseFile("avatar.jpg", imgByteArray);

                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    user.put("avatar", imageFile);
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                MyApp.getInstance().pref.createLoginSession(email);
                Intent intent = new Intent(TherapistLoginActivity.this, StartActivity.class);
                startActivity(intent);
                finish();

            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    void saveProfiletoUser(byte[] bytes)
    {

    }

    void saveNewUser(String username, String email){
        ParseUser user = ParseUser.getCurrentUser();
        user.setUsername(username);
        user.setEmail(email);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
    }

    void alertDisplayer(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TherapistLoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
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

    public byte[] encodeToByteArray(Bitmap image) {
        Bitmap b= image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imgByteArray = baos.toByteArray();

        return imgByteArray ;
    }

    @Override
    public void doScanEmail(String email) {
        if (email.length() == 0)
            return;

        showpDialog();

        hideKeyboardFrom(this);
        ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
            @Override
            public void done(ParseException e) {
                hidepDialog();
                if (e == null)
                    alertDisplayer(getString(R.string.alert_title_email_sent), getString(R.string.alert_content_email_sent));
                else
                    alertDisplayer(getString(R.string.alert_title_error), e.getLocalizedMessage());
            }
        });
    }
}
