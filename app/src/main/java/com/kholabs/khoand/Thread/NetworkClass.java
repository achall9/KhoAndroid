package com.kholabs.khoand.Thread;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Aladar-PC2 on 3/3/2018.
 */

public class NetworkClass extends AsyncTask<String, String, JSONObject>
{
    @Override
    protected JSONObject doInBackground(String... params) {
        String address = params[0];
        StringBuilder stringBuilder = new StringBuilder();
        try {
            address = address.replaceAll(" ","%20");

            URL url = new URL("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
            HttpURLConnection httpconn = (HttpURLConnection)url.openConnection();
            if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()), 8192);
                String strLine = null;
                while ((strLine = input.readLine()) != null)
                {
                    stringBuilder.append(strLine);
                }

                input.close();
            }

        } catch (IOException e) {
        }


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
    }
}