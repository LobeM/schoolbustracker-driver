package com.lobemusonda.schoolbustracker_driver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "MapsActivity";
    public static final String EXTRA_PCIDS = "PCIDs";
    private static final float DEFAULT_ZOOM = 15f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUser, mDatabaseLocation;

    private Intent mIntent;

    private Button mButtonStatus;
    private ProgressBar mProgressBar;

    private GoogleMap mMap;
    private GoogleApiClient mClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LatLng mDestination;
    private Marker mCurrentLocationMarker;

    private boolean mIsOnline, mIsMoving;
    private ArrayList<ChildLocation> mChildrenLocations;
    private ArrayList<String> mChildrenIDs, mParentIDs, mPCIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        mDatabaseLocation = FirebaseDatabase.getInstance().getReference("locations").child(mAuth.getCurrentUser().getUid());

        mIntent = getIntent();
        if (mIntent.hasExtra(EXTRA_PCIDS)) {
            mPCIDs = mIntent.getStringArrayListExtra(EXTRA_PCIDS);
        } else {
            mPCIDs = new ArrayList<>();
        }
        mChildrenLocations = new ArrayList<>();
        mChildrenIDs = new ArrayList<>();
        mParentIDs = new ArrayList<>();

        getStatus();

        mButtonStatus = findViewById(R.id.buttonTrip);
        mProgressBar = findViewById(R.id.progressBar);

        mButtonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                toggleStatus();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
//            Checks if the driver registered a school
            checkSchool();
        }
    }

    private void checkSchool() {
        mDatabaseUser.child("school")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Intent intent = new Intent(MapsActivity.this, AddSchoolActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuLogout:
                if (mIsOnline) {
                    toggleStatus();
                }
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(MapsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;

            case R.id.menuChangeSch:
                intent = new Intent(MapsActivity.this, AddSchoolActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleStatus() {
        mDatabaseUser.child("status")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mProgressBar.setVisibility(View.GONE);
                        String value = dataSnapshot.getValue(String.class);
                        if (value.equals("offline")) {
                            updateStatus("online");
                        } else {
                            updateStatus("offline");
                            mMap.clear();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStatus(String status) {
        mDatabaseUser.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getStatus() {
        mDatabaseUser.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("status").getValue().equals("online")) {
                    mIsOnline = true;
                    if (mIntent.hasExtra(EXTRA_PCIDS)) {
                        mIsMoving = false;
                    } else {
                        mIsMoving = true;
                    }
                } else {
                    mIsOnline = false;
                    mIsMoving = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getLocations() {
        mDatabaseLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChildrenLocations.clear();
                mChildrenIDs.clear();
                mParentIDs.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChildLocation childLocation = snapshot.getValue(ChildLocation.class);
                    if (!mPCIDs.contains(snapshot.getKey())) {
                        mChildrenLocations.add(childLocation);
                        mChildrenIDs.add(snapshot.getKey());
                        mParentIDs.add(childLocation.getParentID());
                    }
                }
                getDestination();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setLocationMarkers() {
        int i = 0;
        for (ChildLocation childLocation: mChildrenLocations) {
            MarkerOptions markerOptions = new MarkerOptions();
            double latitude = childLocation.getPickUp().getLatitude();
            double longitude = childLocation.getPickUp().getLongitude();
            String station = childLocation.getPickUp().getName();

            LatLng latLng = new LatLng(latitude, longitude);
            markerOptions.position(latLng);
            markerOptions.title(station);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(markerOptions);

            if (hasArrived(latLng) == 1) {
                if (mIsMoving){
                    Intent intent = new Intent(MapsActivity.this, RegistrationActivity.class);
                    intent.putExtra(RegistrationActivity.EXTRA_CHILD_IDS, mChildrenIDs);
                    intent.putExtra(RegistrationActivity.EXTRA_PARENT_IDS, mParentIDs);
                    mIsMoving = false;
                    startActivity(intent);
                }
                break;
            } else if (hasArrived(latLng) == 2) {
                if (mIsMoving) {
                    Intent intent = new Intent(MapsActivity.this, CompleteActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mIsMoving = false;
                    startActivity(intent);
                }
                break;
            }
        }
    }

    private int hasArrived(LatLng markerLatLng) {
        // marker location
        Location markerLocation = new Location("");
        markerLocation.setLatitude(markerLatLng.latitude);
        markerLocation.setLongitude(markerLatLng.longitude);

        // destination location
        Location destinationLocation = new Location("");
        destinationLocation.setLatitude(mDestination.latitude);
        destinationLocation.setLongitude(mDestination.longitude);

        float distanceInMetersOne = mLastLocation.distanceTo(markerLocation);
        float distanceInMetersTwo = mLastLocation.distanceTo(destinationLocation);

        if (distanceInMetersOne > 30) {
            mIsMoving = true;
        } else {
            if (distanceInMetersOne <= 30) {
                return 1;
            }
        }
        if (distanceInMetersTwo <= 30) {
            return 2;
        }
        else {
            return 0;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Permission is granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else { // Permission is denied
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: called");
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mLocationRequest, this);
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Log.d(TAG, "onLocationChanged: called in request location updates");
                mLastLocation = location;
                if (mCurrentLocationMarker != null) {
                    mCurrentLocationMarker.remove();
                }

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (mIsOnline) {
                    mButtonStatus.setText(R.string.online);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("ChildLocation");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                    mCurrentLocationMarker = mMap.addMarker(markerOptions);

                    if (mIsMoving) {
                        shareLocation(latLng);
                    }

                    getLocations();
                } else {
                    mButtonStatus.setText(R.string.offline);
                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d(TAG, "onStatusChanged: called");
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d(TAG, "onProviderEnabled: called");
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d(TAG, "onProviderDisabled: called");
            }
        });
    }

    private void shareLocation(final LatLng latLng) {
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDatabaseUser.child("latitude").setValue(latLng.latitude);
                mDatabaseUser.child("longitude").setValue(latLng.longitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: called");
        mLastLocation = location;
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("ChildLocation");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mCurrentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
    }


    public void getDirections(){
        //LatLng destination = getDestination();
        StringBuilder sb = new StringBuilder();
        Object[] dataTransfer = new Object[4];

        sb.append("https://maps.googleapis.com/maps/api/directions/json?");
        sb.append("origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        sb.append("&destination="+ mDestination.latitude + "," + mDestination.longitude);
        sb.append("&waypoints="+ waypoints());
        sb.append("&key="+getResources().getString(R.string.API_key));

        dataTransfer[0] = mMap;
        dataTransfer[1] = sb.toString();
        dataTransfer[2] = new LatLng(mLastLocation.getLatitude() , mLastLocation.getLongitude());
        dataTransfer[3] = mDestination;

        GetDirectionsData getDirectionsData = new GetDirectionsData(getApplicationContext());
        getDirectionsData.execute(dataTransfer);
    }

    private void getDestination() {
        mDatabaseUser.child("school").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                School school = dataSnapshot.getValue(School.class);
                LatLng destination = new LatLng(school.getLatitude(), school.getLongitude());
                mDestination = destination;
                getDirections();
                setLocationMarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String waypoints() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < mChildrenLocations.size(); i++) {
            String point = mChildrenLocations.get(i).getPickUp().getLatitude() + "," + mChildrenLocations.get(i).getPickUp().getLongitude();
            sb.append("via:"+point);
            if (i != (mChildrenLocations.size() - 1)) {
                // Not last iteration
                sb.append("|");
            }
        }

        return sb.toString();
    }
}
