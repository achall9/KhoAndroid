package com.kholabs.khoand.Common;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * Created by Aladar-PC2 on 2/21/2018.
 */

public class FragmentBase extends Fragment {
    private KProgressHUD hd;

    public FragmentBase() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showDialog()
    {
        hd = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
    }
    public void hideDialog()
    {
        if (hd != null && hd.isShowing())
            hd.dismiss();
    }
}

