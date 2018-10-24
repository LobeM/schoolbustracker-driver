package com.lobemusonda.schoolbustracker_driver;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

class GetDirectionsData extends AsyncTask<Object, String, String> {
    private Context mContext;

    private GoogleMap mMap;
    private String mURL;
    private LatLng startLatLng, endLatlng;

    private InputStream is;
    private StringBuilder mStringBuilder;
    private String data;

    public GetDirectionsData(Context applicationContext) {
        this.mContext = applicationContext;
    }

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        mURL = (String) objects[1];
        startLatLng = (LatLng) objects[2];
        endLatlng = (LatLng) objects[3];

        try {
            URL myurl = new URL(mURL);
            Log.d(TAG, "doInBackground: url = " + mURL);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) myurl.openConnection();
            httpsURLConnection.connect();

            is = httpsURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

            String line = "";
            mStringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                mStringBuilder.append(line);
            }

            data = mStringBuilder.toString();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0)
                    .getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

            int count = jsonArray.length();
            String[] polyline_array = new String[count];

            JSONObject jsonObject1;

            for (int i = 0; i < count; i++) {
                jsonObject1 = jsonArray.getJSONObject(i);

                String polygone = jsonObject1.getJSONObject("polyline").getString("points");

                polyline_array[i] = polygone;
            }

            int count1 = polyline_array.length;

            for (int i = 0; i < count1; i++) {
                PolylineOptions options = new PolylineOptions();
                options.color(Color.BLUE);
                options.width(10);
                options.addAll(PolyUtil.decode(polyline_array[i]));

                mMap.addPolyline(options);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
