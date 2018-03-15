package com.kholabs.khoand.Fragment.RootFragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.Fragment.FeedFragment;
import com.kholabs.khoand.Fragment.InstagramProfileFragment;
import com.kholabs.khoand.Fragment.ProfileSettingFragment;
import com.kholabs.khoand.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragmentRoot extends Fragment implements ConstValues{

    public static final String ARG_OBJECT = "object";//testing

    public ProfileFragmentRoot() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        MyApp.getInstance().setFromParent(ProfileFrom.fromProfile);
        View rootview = inflater.inflate(R.layout.fragment_profile_fragment_root, container, false);
        Bundle args = getArguments();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.profile_root_frame, new InstagramProfileFragment());
        transaction.commit();
        getActivity().invalidateOptionsMenu();
        return rootview;
    }

}
