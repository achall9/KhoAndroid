package com.kholabs.khoand.Common;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kholabs.khoand.Context.ConstValues;

/**
 * Created by Administrator on 22.02.2015.
 */

public class ActivityBase extends AppCompatActivity implements ConstValues {

    public static final String TAG = "ActivityBase";

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initpDialog();

    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing())
            pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
