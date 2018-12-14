package com.lobemusonda.schoolbustracker_driver;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CompleteActivity extends AppCompatActivity {
    private static final String TAG = "CompleteActivity";

    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;

    private TextView mTxtComplete, mTxtPicked, mTxtAbsent, mTxtTotal;
    private SwipeButton mSwipeButton;

    private ArrayList<Child> mChildren;
    private ArrayList<String> mChildrenIDs, mParentIDs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        mTxtComplete = findViewById(R.id.txt_view_complete);
        mTxtPicked = findViewById(R.id.txt_view_picked);
        mTxtAbsent = findViewById(R.id.txt_view_absent);
        mTxtTotal = findViewById(R.id.txt_view_total);
        mSwipeButton = findViewById(R.id.swipe_button);
        mSwipeButton.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                if (active) {
                    updateChildrenStatus();
                }
            }
        });

        mChildren = new ArrayList<>();
        mChildrenIDs = new ArrayList<>();
        mParentIDs = new ArrayList<>();
    }

    private void updateChildrenStatus() {
        for (int i = 0; i < mChildren.size(); i++) {
            final String parentID = mParentIDs.get(i);
            final String childID = mChildrenIDs.get(i);
            final Child currentChild = mChildren.get(i);


            if (currentChild.getStatus().equals("inBus")) {
                mDatabase.getReference("children").child(parentID)
                        .child(childID).child("status").setValue("dropped")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                updateChildLocation(childID, "dropped");
                            }
                        });
            } else {
                mDatabase.getReference("children").child(parentID)
                        .child(childID).child("status").setValue("absent")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                updateChildLocation(childID, "absent");
                                addToAbsent(parentID, childID, currentChild);
                            }
                        });
            }

        }
        mTxtComplete.setText(String.valueOf("Trip Complete"));
        driverOffline();
    }

    private void driverOffline() {
        mDatabase.getReference("users").child(mUser.getUid())
                .child("status").setValue("offline");
        Intent intent = new Intent(CompleteActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void addToAbsent(String parentID, String childID, Child child) {
        child.setStatus("absent");
        mDatabase.getReference("absent").child(parentID)
                .child(childID)
                .setValue(child);
    }

    private void updateChildLocation(String childID, String status) {
        mDatabase.getReference("locations").child(mUser.getUid()).child(childID)
                .child("status").setValue(status);
    }



    @Override
    protected void onStart() {
        super.onStart();
        getIDs();
    }

    private void showTripDetails() {
        int picked = 0;
        int absent = 0;
        for (Child child : mChildren){
            if (child.getStatus().equals("inBus")) {
                picked++;
            } else {
                absent++;
            }
        }
        Log.d(TAG, "showTripDetails: picked="+ picked + " absent="+absent);
        mTxtPicked.setText(String.valueOf(picked));
        mTxtAbsent.setText(String.valueOf(absent));
        mTxtAbsent.setTextColor(Color.RED);
        mTxtTotal.setText(String.valueOf(mChildren.size()));
    }

    private void getIDs() {
        mDatabase.getReference("locations").child(mUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mChildrenIDs.clear();
                        mParentIDs.clear();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                            String childID = childSnapshot.getKey();
                            String parentID = childSnapshot.child("parentID").getValue(String.class);

                            mChildrenIDs.add(childID);
                            mParentIDs.add(parentID);
                            Log.d(TAG, "childID: " + childID);
                            Log.d(TAG, "parentID: "+ parentID);
                        }
                        getChildren();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getChildren() {
        for (int i = 0; i < mParentIDs.size(); i++){
            mDatabase.getReference("children").child(mParentIDs.get(i))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mChildren.clear();
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                Child child = childSnapshot.getValue(Child.class);
                                Log.d(TAG, "snapshot: "+childSnapshot.toString());
                                Log.d(TAG, "childZipza: "+child.toString());
                                mChildren.add(child);

                                showTripDetails();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }
}
