package com.lobemusonda.schoolbustracker_driver;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
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

public class RegistrationActivity extends AppCompatActivity {
    public static final String EXTRA_CHILD_IDS = "Child IDs";
    public static final String EXTRA_PARENT_IDS = "Parent IDs";
    private static final String TAG = "RegistrationActivity";

    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private SwipeButton mSwipeButton;
    private ChildrenAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> mChildrenIDs, mParentIDs, mPCIDs, mPPIDs;
    private ArrayList<Child> mChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Activity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        mChildrenIDs = intent.getStringArrayListExtra(EXTRA_CHILD_IDS);
        mParentIDs = intent.getStringArrayListExtra(EXTRA_PARENT_IDS);

        mChildren = new ArrayList<>();
        mPCIDs = new ArrayList<>();
        mPPIDs = new ArrayList<>();

        mProgressBar = findViewById(R.id.progressBar);
        mSwipeButton = findViewById(R.id.swipe_button);
        mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        getChildren();
    }

    private void getChildren() {
        Log.d(TAG, "getChildren: called");
        mProgressBar.setVisibility(View.VISIBLE);
        mChildren.clear();
        for (int i = 0; i < mChildrenIDs.size(); i++) {
            mDatabase.getReference("children").child(mParentIDs.get(i)).child(mChildrenIDs.get(i))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Child child = dataSnapshot.getValue(Child.class);
                        mChildren.add(child);
                        loadRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }


    }

    private void loadRecyclerView() {
        Log.d(TAG, "array count: " + mChildren.size());
        mAdapter = new ChildrenAdapter(mChildren);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mAdapter.setOnItemClickListener(new ChildrenAdapter.OnItemClickListener() {
            @Override
            public void onItemCheckedChange(int position, CompoundButton compoundButton, boolean b) {
                registerChild(position, compoundButton, b);
            }

            @Override
            public void onItemClick(int position) {

            }
        });
        mProgressBar.setVisibility(View.GONE);

        mSwipeButton.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                if (active) {
                    updateChildrenStatus();
                }
            }
        });
    }

    private void updateChildrenStatus() {
        for (int i = 0; i < mPCIDs.size(); i++) {
            final String parentID = mPPIDs.get(i);
            final String childID = mPCIDs.get(i);
            mDatabase.getReference("children").child(parentID)
                .child(childID).child("status").setValue("inBus")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    updateChildLocation(childID);
                }
            });
        }

        Intent intent = new Intent(RegistrationActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void updateChildLocation(String childID) {
        mDatabase.getReference("locations").child(mUser.getUid()).child(childID).child("status").setValue("inBus");
    }

    private void registerChild(final int position, CompoundButton compoundButton, boolean b) {

        if (compoundButton.isChecked()) {

            TextView cardName = mRecyclerView.findViewHolderForAdapterPosition(position)
                    .itemView.findViewById(R.id.card_name);
            cardName.setPaintFlags(cardName.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
            mPCIDs.add(mChildrenIDs.get(position));
            mPPIDs.add(mParentIDs.get(position));

        } else {
            TextView cardName = mRecyclerView.findViewHolderForAdapterPosition(position)
                    .itemView.findViewById(R.id.card_name);
            cardName.setPaintFlags(cardName.getPaintFlags()& (~ Paint.STRIKE_THRU_TEXT_FLAG));
            mPCIDs.remove(mChildrenIDs.get(position));
            mPPIDs.remove(mParentIDs.get(position));
        }
    }
}
