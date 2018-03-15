package com.kholabs.khoand.Fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Activity.StartActivity;
import com.kholabs.khoand.Activity.TutorialActivity;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.FragmentPopKeys;
import com.kholabs.khoand.Dialog.ChangePasswordDialog;
import com.kholabs.khoand.Dialog.ResetPasswordDialog;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.MyUtils;
import com.parse.ParseUser;

import java.io.File;

public class ProfileSettingFragment extends Fragment implements  View.OnClickListener,
        ChangePasswordDialog.PasswordChangeListener,
        MainActivity.onKeyBackPressedListener
{

    private static final String ARG_AVATAR = "avatar";
    private boolean isAvatarLoaded;

    private View rootView;
    private LinearLayout llBack;
    private RelativeLayout logoutLayout, rlChangePsw, rlViewHelp;

    public ProfileSettingFragment() {

    }

    public static ProfileSettingFragment newInstance(boolean isAvatarLoaded) {
        ProfileSettingFragment fragment = new ProfileSettingFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_AVATAR, isAvatarLoaded);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAvatarLoaded = getArguments().getBoolean(ARG_AVATAR, false);
        }
        rootView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_setting, container, false);
            llBack = (LinearLayout) rootView.findViewById(R.id.llBack);
            logoutLayout = (RelativeLayout) rootView.findViewById(R.id.logout_layout);
            rlChangePsw = (RelativeLayout) rootView.findViewById(R.id.rl_changepsw) ;
            rlViewHelp = (RelativeLayout) rootView.findViewById(R.id.rl_tutorial);

            ((MainActivity)getActivity()).setOnkeyBackPressedListener(this);
        }

        return rootView;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        initEvent();
    }

    private void initEvent()
    {
        llBack.setOnClickListener(this);

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDisplayer("Log out", "Are you sure you want to log out?");
            }
        });

        rlChangePsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangePasswordDialog dialog = new ChangePasswordDialog(ProfileSettingFragment.this);
                dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                dialog.show(ft, "Change Password");
            }
        });

        rlViewHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tutorialActivity = new Intent(getActivity(), TutorialActivity.class);
                tutorialActivity.putExtra("isAvatarLoaded", true);
                getActivity().startActivity(tutorialActivity);
            }
        });
    }


    void alertDisplayer(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        TextView myMsg = new TextView(getContext());
        myMsg.setText(message);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        TextView txtTitle = new TextView(getContext());
        txtTitle.setText(title);
        //title.setBackgroundColor(Color.DKGRAY);
        txtTitle.setPadding(10, 10, 10, 10);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextColor(Color.BLUE);
        txtTitle.setTextSize(20);
        builder.setCustomTitle(txtTitle);
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ParseUser parseUser = ParseUser.getCurrentUser();
                if (parseUser != null) {
                    MyApp.getInstance().pref.logout();
                    ParseUser.logOut();
                    MyApp.getInstance().goToStartActivity();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llBack:
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
                break;
        }
    }

    @Override
    public void onPasswordChanged() {
        MyApp.getInstance().alertDisplayer("Success", getString(R.string.alert_message_password_changed), "Okay");
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            ParseUser.logOut();
            MyApp.getInstance().pref.logout();
            MyApp.getInstance().goToStartActivity();
        }
    }

    @Override
    public void onBack() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();

    }
}

