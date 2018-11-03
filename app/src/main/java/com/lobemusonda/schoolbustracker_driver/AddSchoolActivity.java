package com.lobemusonda.schoolbustracker_driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddSchoolActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "AddSchoolActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleApiClient mClient;


    private Spinner mSpinnerSchools;
    private Button mButtonSave;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_school);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mSpinnerSchools = findViewById(R.id.spinnerSchools);
        mButtonSave = findViewById(R.id.btnSave);
        mProgressBar = findViewById(R.id.progressBar);

        buildGoogleApiClient();
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSchool();
            }
        });
    }

    private void saveSchool() {
        mProgressBar.setVisibility(View.VISIBLE);
        School school = (School) mSpinnerSchools.getSelectedItem();
        if (school == null) {
            Toast.makeText(getApplicationContext(), "Select School", Toast.LENGTH_SHORT).show();
            return;
        }
        //Update database
        mDatabase.child("schools").child(mAuth.getCurrentUser().getUid()).setValue(school.getName());

        // Update driver profile
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("school")
                .setValue(school).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgressBar.setVisibility(View.GONE);
                Intent intent = new Intent(AddSchoolActivity.this, MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void findSchools(LatLng currentLocation) {
        mProgressBar.setVisibility(View.VISIBLE);
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location="+currentLocation.latitude+","+currentLocation.longitude);
        stringBuilder.append("&radius=" + 50000);
        stringBuilder.append("&type="+"school");
        stringBuilder.append("&key="+getResources().getString(R.string.API_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = url;
        dataTransfer[1] = mSpinnerSchools;
        dataTransfer[2] = mProgressBar;

        GetNearByPlaces getNearByPlaces = new GetNearByPlaces(getApplicationContext());
        getNearByPlaces.execute(dataTransfer);
    }

    protected synchronized void buildGoogleApiClient() {
        mClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: called");

        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        findSchools(currentLocation);

        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: called");
        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
