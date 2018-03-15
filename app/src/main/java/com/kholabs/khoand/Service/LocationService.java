package com.kholabs.khoand.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.kholabs.khoand.Manifest;

/**
 * Created by Aladar-PC2 on 2/11/2018.
 */

public class LocationService extends Service {
    final static String MY_ACTION = "MY_ACTION";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private Location mLastLocation;
    private LocationServiceActionListener sproutActionListener;

    private Object lockObj = new Object();

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
            sproutActionListener.singleLocationChanged(mLastLocation);
        }

        @Override
        public void onLocationChanged(Location location) {

            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyThread myThread = new MyThread();
        myThread.start();
        return super.onStartCommand(intent, flags, startId);
//        return START_STICKY;
    }

    public class MyThread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(5000);
                    Intent intent = new Intent();
                    intent.setAction(MY_ACTION);

                    intent.putExtra("latitude", mLastLocation.getLatitude());
                    intent.putExtra("longitude", mLastLocation.getLongitude());

                    sendBroadcast(intent);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            stopSelf();
        }

    }

    @Override
    public void onCreate() {
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {

                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void registerSproutActionListener(LocationServiceActionListener listener)
    {
        synchronized (lockObj) {
            this.sproutActionListener = listener;
        }
    }
    public void unregisterSproutActionListener(LocationServiceActionListener listener){
        synchronized (lockObj) {
            if (this.sproutActionListener == listener)
                this.sproutActionListener = null;
        }
    }

    public interface LocationServiceActionListener{
        public void singleLocationChanged(Location currentPos);

    }
}
