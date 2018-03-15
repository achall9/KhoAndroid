package com.kholabs.khoand.Fragment.ChildProfileFragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Model.MarkerItem;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Thread.NetworkClass;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ChildInfoFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener{
    private ParseUser inc_user;
    private View rootView, athleteView, therapistView;
    private LinearLayout llMapLayout;
    private TextView tvBioInfo, tvStateInfo, tvSportsInfo, tvSiteInfo;
    private TextView tvTherapistBio, tvTherapistState, tvTherapistPhone, tvTherapistSite;

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Marker selectedMarker;
    private TextView tv_marker;
    private View marker_root_view;

    public ChildInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.inc_user = MyApp.getInstance().getShareUser();
        this.rootView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_child_info, container, false);
            athleteView = (View)rootView.findViewById(R.id.layout_athlete);
            therapistView = (View)rootView.findViewById(R.id.layout_therapist);
            llMapLayout = (LinearLayout)rootView.findViewById(R.id.map_linearlayout);

            tvBioInfo = (TextView) athleteView.findViewById(R.id.tvBioInfo);
            tvStateInfo = (TextView) athleteView.findViewById(R.id.tvStateInfo);
            tvSportsInfo = (TextView) athleteView.findViewById(R.id.tvSportsInfo);
            tvSiteInfo = (TextView) athleteView.findViewById(R.id.tvWebsiteInfo);

            tvTherapistBio = (TextView) therapistView.findViewById(R.id.tvBioInfo);
            tvTherapistState = (TextView) therapistView.findViewById(R.id.tvStateInfo);
            tvTherapistPhone = (TextView) therapistView.findViewById(R.id.tvPhoneInfo);
            tvTherapistSite = (TextView) therapistView.findViewById(R.id.tvWebsiteInfo);

            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_therapist);
            mapFragment.getMapAsync(this);

            initProfileForAthlete();
            initProfileForTherapist();

        }


        return rootView;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        changeSelectedMarker(null);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(center);
        changeSelectedMarker(marker);
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        setCustomMarkerView();
        initTherapistMapView();

    }

    private void setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(getContext()).inflate(R.layout.marker_layout, null);
        tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
    }

    private Marker addMarker(MarkerItem markerItem, boolean isSelectedMarker) {
        LatLng position = new LatLng(markerItem.getLat(), markerItem.getLon());
        String name = markerItem.getName();

        tv_marker.setText(name);

        if (isSelectedMarker) {
            tv_marker.setBackgroundResource(R.drawable.ic_marker_phone_blue);
            tv_marker.setTextColor(Color.WHITE);
        } else {
            tv_marker.setBackgroundResource(R.drawable.ic_marker_phone);
            tv_marker.setTextColor(Color.BLACK);
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("Therapist");
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));

        return mMap.addMarker(markerOptions);

    }

    private Marker addMarker(Marker marker, boolean isSelectedMarker) {
        double lat = marker.getPosition().latitude;
        double lon = marker.getPosition().longitude;
        String name = marker.getTitle();
        MarkerItem temp = new MarkerItem(lat, lon, name);
        return addMarker(temp, isSelectedMarker);

    }

    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private void changeSelectedMarker(Marker marker) {
        if (selectedMarker != null) {
            addMarker(selectedMarker, false);
            selectedMarker.remove();
        }

        if (marker != null) {
            selectedMarker = addMarker(marker, false);
            marker.remove();
        }


    }

    public void initProfileForAthlete()
    {
        if (inc_user == null) return;
        String strBio = inc_user.getString("bio");
        if (strBio != null)
            tvBioInfo.setText(strBio);
        ParseGeoPoint currLocation = inc_user.getParseGeoPoint("currentLocation");
        if (currLocation != null)
        {
            String strAddress = GeolocationToAddress(currLocation);
            if (strAddress.equals(""))
                tvStateInfo.setText("-");
            else
                tvStateInfo.setText(strAddress);
        }

        List<String> sports = inc_user.getList("sports");
        if (sports != null && sports.size() != 0)
        {
            String title = "";
            for (int i=0; i<sports.size(); i++)
                title = title + sports.get(i) + ",";
            title = title.substring(0, title.length() - 1);

            tvSportsInfo.setText(title);
        }

    }

    public void initProfileForTherapist()
    {
        if (inc_user == null) return;
        boolean isTherapist = inc_user.getBoolean("isTherapist");
        boolean verifiedTherapist = inc_user.getBoolean("verifiedTherapist");

        if (!isTherapist || !verifiedTherapist)
        {
            therapistView.setVisibility(View.GONE);
            llMapLayout.setVisibility(View.GONE);
            mapFragment.getView().setVisibility(View.GONE);
            return;
        }

        HashMap<String, String> therapistInfo = (HashMap<String, String>) inc_user.get("therapistInfo");
        if (therapistInfo.containsKey("therapistBio"))
        {
            String strBio = therapistInfo.get("therapistBio");
            if (strBio != null)
                tvTherapistBio.setText(strBio);
        }

        if (therapistInfo.containsKey("address") && therapistInfo.containsKey("state") && therapistInfo.containsKey("country"))
        {
            String strAddress = (therapistInfo.get("address") != null) ? therapistInfo.get("address") : "";
            String strState = (therapistInfo.get("state") != null) ? therapistInfo.get("state") : "";
            String strCountry = (therapistInfo.get("country") != null) ? therapistInfo.get("country") : "";

            tvTherapistState.setText(String.format("%s, %s, %s", strAddress, strState, strCountry));
        }

        if (therapistInfo.containsKey("phone"))
        {
            String strPhone = therapistInfo.get("phone");
            tvTherapistPhone.setText(strPhone);
        }

        if (therapistInfo.containsKey("website"))
        {
            String strWebsite = therapistInfo.get("website");
            tvTherapistSite.setText(strWebsite);
        }

    }

    private void initTherapistMapView()
    {
        if (inc_user == null) return;

        boolean isTherapist = inc_user.getBoolean("isTherapist");
        boolean verifiedTherapist = inc_user.getBoolean("verifiedTherapist");
        if (!isTherapist || !verifiedTherapist)
        {
            therapistView.setVisibility(View.GONE);
            llMapLayout.setVisibility(View.GONE);
            mapFragment.getView().setVisibility(View.GONE);
            return;
        }

        HashMap<String, String> therapistInfo = (HashMap<String, String>) inc_user.get("therapistInfo");
        if (therapistInfo != null)
        {
            if (therapistInfo.containsKey("address") && therapistInfo.containsKey("state") && therapistInfo.containsKey("country"))
            {
                String address = String.format("%s,%s,%s", therapistInfo.get("address").trim(), therapistInfo.get("state").trim(), therapistInfo.get("country").trim());

                addMarkerFromPosition(address);
            }
            else
            {
                llMapLayout.setVisibility(View.GONE);
                mapFragment.getView().setVisibility(View.GONE);
            }
        }
    }

    private boolean addMarkerFromPosition(String strAddress) {
        JSONObject result = null;
        try {
            result = new NetworkClass().execute(strAddress).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (result != null)
        {
            try {
                double longi = ((JSONArray) result.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                double latit = ((JSONArray) result.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");
                if (latit != 0.0d && longi != 0.0d)
                {
                    String username = inc_user.getString("name");
                    MarkerItem mkMarker = new MarkerItem(latit, longi, username);
                    addMarker(mkMarker, true);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latit, longi), 14));
                }
            } catch (JSONException e) {
                return false;

            }
            return true;
        } else
            return false;
    }


    public String GeolocationToAddress(ParseGeoPoint pm)
    {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(pm.getLatitude(), pm.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String city = "";
            String country = "";
            String address = "";
            if (addresses != null && addresses.size() > 0)
            {
                city = addresses.get(0).getLocality();
                country = addresses.get(0).getCountryName();
                address = String.format("%s, %s", city, country);
            }
            return address;
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return "";
    }
}
