package com.kholabs.khoand.Fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Adapter.DropDownListAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.FragmentPopKeys;
import com.kholabs.khoand.Dialog.AddFieldDialog;
import com.kholabs.khoand.Dialog.DateDialog;
import com.kholabs.khoand.Interface.UploadProfileCallBack;
import com.kholabs.khoand.Model.ProfileData;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Service.LocationService;
import com.kholabs.khoand.Thread.ProfileUploadTask;
import com.kholabs.khoand.Utils.MyUtils;
import com.parse.GetCallback;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.yayandroid.locationmanager.base.LocationBaseFragment;
import com.yayandroid.locationmanager.configuration.Configurations;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileEditFragment extends LocationBaseFragment implements View.OnClickListener,
        DropDownListAdapter.DropDownItemListener, AddFieldDialog.AddFieldDialogCompliant,
        MainActivity.onKeyBackPressedListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private KProgressHUD hd;

    private View rootView;
    private LinearLayout llBack, llPopup;
    private RelativeLayout rlTherapist;
    private DatePicker datePicker;
    private EditText etName, etPhone, etBirthday, etGmail, etDesc;
    private TextView tvSelectSport;
    private Switch swtLocation;
    private Button btnUpdate, btnTherapist;

    private ExpandableLayout expandLayout;
    private RecyclerView mRecyclerPopup;
    private DropDownListAdapter sportsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //private PopupWindow pw;

    private boolean expanded;
    private boolean needsUpdate;
    private ArrayList<String> items = new ArrayList<String>();

    private LocationManager lm;
    private LocationService sproutService = null;
    private Object lockObj = new Object();

    private Bundle mSavedInstanceState;
    private boolean mHasInflated = false;
    private ViewStub mViewStub;
    private ExecutorService executor;
    private boolean isTherapist = false;

    public ProfileEditFragment() {
    }

    public static ProfileEditFragment newInstance(String param1, String param2) {
        ProfileEditFragment fragment = new ProfileEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public LocationConfiguration getLocationConfiguration() {
        return Configurations.defaultConfiguration("Give me the permission!", "Would you mind to turn GPS on?");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }


        rootView = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
            needsUpdate = false;
            ((MainActivity)getActivity()).setOnkeyBackPressedListener(this);

            llBack = (LinearLayout) rootView.findViewById(R.id.llBack);

            expandLayout = (ExpandableLayout) rootView.findViewById(R.id.expandable_list);
            expandLayout.setVisibility(View.GONE);
            expandLayout.collapse();

            mRecyclerPopup = (RecyclerView) rootView.findViewById(R.id.listSports);
            mRecyclerPopup.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerPopup.setLayoutManager(mLayoutManager);
            mRecyclerPopup.scrollToPosition(0);

            llPopup = (LinearLayout) rootView.findViewById(R.id.llPopup);
            //datePicker = (DatePicker)rootView.findViewById(R.id.datePicker);
            etName = (EditText) rootView.findViewById(R.id.et_name);
            etPhone = (EditText) rootView.findViewById(R.id.et_phone);
            etGmail = (EditText) rootView.findViewById(R.id.et_gmail);
            etDesc = (EditText) rootView.findViewById(R.id.et_desc);
            rlTherapist = (RelativeLayout) rootView.findViewById(R.id.rl_therapist);
            etBirthday = (EditText) rootView.findViewById(R.id.et_birthday);
            swtLocation = (Switch) rootView.findViewById(R.id.swt_location);
            btnUpdate = (Button) rootView.findViewById(R.id.btnUpdate);
            btnTherapist = (Button) rootView.findViewById(R.id.btnTherapistInfo);

            tvSelectSport = (TextView) rootView.findViewById(R.id.tvSelectSport);
            mSavedInstanceState = savedInstanceState;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initEventListener();
                    initializeSelectList();
                    setUpdateButtonStatus(false);
                    loadDataTask();
                }
            }, 500);
        }

        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadDataTask()
    {
        executor = Executors.newFixedThreadPool(2);
        Runnable thread1 = new Runnable() {
            @Override
            public void run() {
                loadProfile();
            }
        };

        Runnable thread2 = new Runnable() {
            @Override
            public void run() {
                checkIsAthlete();
            }
        };

        executor.submit(thread1);
        executor.submit(thread2);

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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llBack:
                //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                //fragmentManager.popBackStack(FragmentPopKeys.PROFILEPAGE, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
                break;
            case R.id.btnTherapistInfo:
                GotoTherapistAfterUpdate();
                break;
            case R.id.btnUpdate:
                UploadProcess();
                break;

        }
    }

    private void initEventListener()
    {
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setUpdateButtonStatus(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etBirthday.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setUpdateButtonStatus(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etGmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setUpdateButtonStatus(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setUpdateButtonStatus(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setUpdateButtonStatus(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    DateDialog dialog = new DateDialog(view);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    dialog.show(ft, "Date Picker");
                }
            }
        });

        tvSelectSport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setUpdateButtonStatus(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        swtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                boolean isPopOn = swtLocation.isChecked();
                if (!isPopOn)
                {
                    ParseGeoPoint geoPoint = new ParseGeoPoint(0, 0);
                    currentUser.put("currentLocation", geoPoint);
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                } else
                {
                    getLocation();
                    //Criteria criteria = new Criteria();
                    //criteria.setAccuracy(Criteria.ACCURACY_FINE);

                    /*
                    ParseGeoPoint.getCurrentLocationInBackground(10000, criteria, new LocationCallback() {
                        @Override
                        public void done(ParseGeoPoint geoPoint, ParseException e) {
                            if (e != null) { swtLocation.setChecked(false); return; }
                            currentUser.put("currentLocation", geoPoint);
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                }
                            });
                        }
                    });
                    */
                }
            }
        });


        btnUpdate.setOnClickListener(this);
        btnTherapist.setOnClickListener(this);
        llBack.setOnClickListener(this);


    }

    private void setUpdateButtonStatus(boolean isStat)
    {
        if (isStat)
        {
            needsUpdate = true;
            btnUpdate.setEnabled(true);
            btnUpdate.setAlpha(1.0f);
        } else
        {
            needsUpdate = false;
            btnUpdate.setEnabled(false);
            btnUpdate.setAlpha(0.4f);
        }
    }

    private void loadProfile()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();

        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (getActivity() == null)
                    return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String name = object.getString("name");
                        String phone = object.getString("phone");
                        Date birthDate = object.getDate("dob");
                        String email = object.getString("email");
                        String bio = object.getString("bio");
                        List<String> sportsArr = object.getList("sports");

                        if (name != null)
                            etName.setText(name);
                        if (phone != null)
                            etPhone.setText(phone);

                        if (birthDate != null)
                        {
                            String day          = (String) DateFormat.format("dd",   birthDate); // 20
                            String monthNumber  = (String) DateFormat.format("MM",   birthDate); // 06
                            String year         = (String) DateFormat.format("yyyy", birthDate);
                            String txtBirthday = day + "/"+ monthNumber +"/"+year;

                            etBirthday.setText(txtBirthday);
                        }

                        if (email != null)
                            etGmail.setText(email);

                        if (bio != null)
                            etDesc.setText(bio);

                        String sportsString = "Select Sports";

                        if (sportsArr != null) {
                            sportsString = MyUtils.JoinString(sportsArr);
                            tvSelectSport.setText(sportsString);
                            if (sportsAdapter != null)
                                sportsAdapter.setCheckArray(tvSelectSport.getText().toString());
                        }

                    }
                });


            }
        });
    }

    private void checkIsAthlete()
    {
        ParseUser user = ParseUser.getCurrentUser();
        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) return;;
                if (getActivity() == null)
                    return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isTherapist = object.getBoolean("isTherapist");

                        if (!isTherapist)
                            rlTherapist.setVisibility(View.GONE);
                        else
                            getLocation();
                    }
                });
            }
        });
    }

    private void checkLocationFields()
    {
        ParseGeoPoint currentLocation = ParseUser.getCurrentUser().getParseGeoPoint("currentLocation");
        if (currentLocation != null)
        {
            if (currentLocation.getLatitude() != 0.0f && currentLocation.getLongitude() != 0.0f)
                swtLocation.setChecked(true);
            else
                swtLocation.setChecked(false);

        }
        else
            swtLocation.setChecked(false);
    }

    private AsyncTask<ProfileData, Void, ProfileData> saveInProfileWithCompletion(UploadProfileCallBack callback)
    {
        ProfileData data = new ProfileData();
        data.setName(etName.getText().toString());
        data.setPhone((etPhone.getText().toString()));

        Date birthDate = MyUtils.convertDateFromString(etBirthday.getText().toString());
        data.setDob(birthDate);
        data.setBio(etDesc.getText().toString());
        List<String> sportsArr = MyUtils.getArrayString(tvSelectSport.getText().toString());
        data.setSportArr(sportsArr);

        return new ProfileUploadTask(callback).execute(data);
    }

    private void GotoTherapistAfterUpdate()
    {
        if (needsUpdate)
        {
            showDialog();
            saveInProfileWithCompletion(new UploadProfileCallBack() {
                @Override
                public void onSuccess() {
                    hideDialog();
                    setUpdateButtonStatus(false);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.profile_root_frame, TherapistInfoFragment.newInstance(), "TherapistInfo")
                            .addToBackStack(null)
                            .commit();
                    getActivity().invalidateOptionsMenu();
                }

                @Override
                public void onFailure(Exception e) {
                    hideDialog();
                }
            });
        } else
        {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.profile_root_frame, TherapistInfoFragment.newInstance(), "TherapistInfo")
                    .addToBackStack(null)
                    .commit();
            getActivity().invalidateOptionsMenu();
        }
    }

    private void UploadProcess()
    {
        showDialog();
        saveInProfileWithCompletion(new UploadProfileCallBack() {
            @Override
            public void onSuccess() {
                hideDialog();
                setUpdateButtonStatus(false);
                MyApp.getInstance().alertDisplayer("Updated", "Your Profile has been updated");
            }

            @Override
            public void onFailure(Exception e) {
                hideDialog();
            }
        });
    }


    private void initializeSelectList()
    {
        items.add("Athletics");
        items.add("Baseball");
        items.add("Basketball");
        items.add("Boxing");
        items.add("Cricket");
        items.add("Cycling");
        items.add("Football");
        items.add("Golf");
        items.add("Horse Racing");
        items.add("Ice Hockey");
        items.add("Rugby");
        items.add("Swimming");
        items.add("Tennis");
        items.add("Volleyball");
        items.add("Add Custom Field");

        sportsAdapter = new DropDownListAdapter(getContext(), items, tvSelectSport, ProfileEditFragment.this);
        mRecyclerPopup.setAdapter(sportsAdapter);
        mRecyclerPopup.setItemAnimator(new DefaultItemAnimator());

        tvSelectSport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!expanded) {
                    expanded = true;
                    expandLayout.expand();
                } else
                {
                    expanded = false;
                    expandLayout.collapse();

                }
            }
        });
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public void AddCustomField(int position) {
        AddFieldDialog dialog = new AddFieldDialog(ProfileEditFragment.this);
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        dialog.show(ft, "Add Custom Field");
    }

    @Override
    public void addToPopup(String field) {
        items.add(items.size()-1, field);
        sportsAdapter.addItem(field);
    }

    @Override
    public void onBack() {
        //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        //fragmentManager.popBackStack(FragmentPopKeys.PROFILEPAGE, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
        ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
    }

    @Override
    public void onPause()
    {
        if (executor != null && !executor.isShutdown())
            executor.shutdownNow();
        super.onPause();

    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude=location.getLatitude();
        double longitude=location.getLongitude();
        ParseGeoPoint geopoint = new ParseGeoPoint(latitude, longitude);
        if (geopoint != null)
        {
            ParseUser currentuser = ParseUser.getCurrentUser();
            currentuser.put("currentLocation", geopoint);
            currentuser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    checkLocationFields();
                }
            });
        }
    }

    @Override
    public void onLocationFailed(int type) {

    }
}
