package com.lobemusonda.schoolbustracker_driver;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.lobemusonda.schoolbustracker_driver.R;
import com.lobemusonda.schoolbustracker_driver.School;
import com.lobemusonda.schoolbustracker_driver.SpinAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by lobemusonda on 10/1/18.
 */

public class GetNearByPlaces extends AsyncTask<Object, String, String> {
    private static final String TAG = "GetNearByPlaces";

    private Spinner mSpinnerSchools;
    private ProgressBar mProgressBar;
    private String mURL;
    private InputStream is;
    private BufferedReader mBufferedReader;
    private StringBuilder mStringBuilder;
    private String data;

    private SpinAdapter mSpinAdapter;
    private ArrayList<School> mSchools;

    private Context mContext;

    public GetNearByPlaces (Context context){
        mContext = context;
    }

    @Override
    protected String doInBackground(Object... objects) {
        mURL = (String) objects[0];
        mSpinnerSchools = (Spinner) objects[1];
        mProgressBar = (ProgressBar) objects[2];

        mSchools = new ArrayList<>();

        try {
            URL myurl = new URL(mURL);
            Log.d(TAG, "doInBackground: url = " + mURL);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)myurl.openConnection();
            httpsURLConnection.connect();
            is = httpsURLConnection.getInputStream();
            mBufferedReader = new BufferedReader(new InputStreamReader(is));

            String line = "";
            mStringBuilder = new StringBuilder();

            while ((line = mBufferedReader.readLine()) != null) {
                mStringBuilder.append(line);
            }

            data = mStringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: json = " + s);
        try {
            JSONObject parentObject = new JSONObject(s);
            JSONArray resultsArray = parentObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject jsonObject = resultsArray.getJSONObject(i);
                JSONObject locationObject = jsonObject.getJSONObject("geometry").getJSONObject("location");

                String latitude = locationObject.getString("lat");
                String longitude = locationObject.getString("lng");

                JSONObject nameObject = resultsArray.getJSONObject(i);

                String stationName = nameObject.getString("name");

                School school = new School();
                school.setName(stationName);
                school.setLatitude(Double.parseDouble(latitude));
                school.setLongitude(Double.parseDouble(longitude));
                mSchools.add(school);
            }

//            Initialize the adapter sending the current context
            mSpinAdapter = new SpinAdapter(mContext, R.layout.support_simple_spinner_dropdown_item, mSchools);
            mSpinnerSchools.setAdapter(mSpinAdapter);
            mProgressBar.setVisibility(View.GONE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
