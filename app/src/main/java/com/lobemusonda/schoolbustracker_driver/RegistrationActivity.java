package com.lobemusonda.schoolbustracker_driver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RegistrationActivity extends AppCompatActivity {
    public static final String EXTRA_CHILD_IDS = "Child IDs";
    public static final String EXTRA_PARENT_IDS = "Parent IDs";
    private static final String TAG = "RegistrationActivity";

    private FirebaseDatabase mDatabase;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ChildrenAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> mChildrenIDs, mParentIDs;
    private ArrayList<Child> mChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Activity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mDatabase = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        mChildrenIDs = intent.getStringArrayListExtra(EXTRA_CHILD_IDS);
        mParentIDs = intent.getStringArrayListExtra(EXTRA_PARENT_IDS);

        mChildren = new ArrayList<>();

        mProgressBar = findViewById(R.id.progressBar);
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
        for (int i = 0; i < mChildrenIDs.size(); i++) {
            mDatabase.getReference("children").child(mParentIDs.get(i)).child(mChildrenIDs.get(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: snapshot = "+dataSnapshot );
                    Child child = dataSnapshot.getValue(Child.class);
                    mChildren.add(child);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        mAdapter = new ChildrenAdapter(mChildren);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.GONE);
    }
}
