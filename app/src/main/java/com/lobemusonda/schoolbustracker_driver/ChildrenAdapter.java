package com.lobemusonda.schoolbustracker_driver;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildrenViewHolder> {
    private static final String TAG = "ChildrenAdapter";
    private ArrayList<Child> mChild;
    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemCheckedChange(int position, CompoundButton compoundButton, boolean b);
    }

    public static class ChildrenViewHolder extends RecyclerView.ViewHolder {
        private TextView mChildName;
        private CheckBox mCheckBox;

        public ChildrenViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            mChildName = itemView.findViewById(R.id.card_name);
            mCheckBox = itemView.findViewById(R.id.card_checkBox);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null)  {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (listener != null)  {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemCheckedChange(position, compoundButton, b);
                        }
                    }
                }
            });
        }
    }

    public ChildrenAdapter(ArrayList<Child> childItems) {
        mChild = childItems;
    }

    @Override
    public ChildrenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item, parent, false);
        ChildrenViewHolder cvh = new ChildrenViewHolder(v, mListener);
        Log.d(TAG, "onCreateViewHolder: " + viewType);
        return cvh;
    }

    @Override
    public void onBindViewHolder(ChildrenViewHolder holder, int position) {
        Child currentItem = mChild.get(position);
        holder.mChildName.setText(currentItem.getFirstName() + " "+ currentItem.getLastName());
        checkStatus(currentItem.getStatus(), holder);
        Log.d(TAG, "binding: "+position);
    }

    private void checkStatus(String status, ChildrenViewHolder holder) {
        if (status.equals("inBus") || status.equals("absent")) {
            holder.mChildName.setPaintFlags(holder.mChildName.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: "+ mChild.size());
        return mChild.size();
    }
}
