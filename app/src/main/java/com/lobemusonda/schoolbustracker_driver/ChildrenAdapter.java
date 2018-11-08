package com.lobemusonda.schoolbustracker_driver;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildrenViewHolder> {
    private ArrayList<Child> mChild;
    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class ChildrenViewHolder extends RecyclerView.ViewHolder {
        private TextView mChildName;
        private FrameLayout mColorLabel;

        public ChildrenViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            mChildName = itemView.findViewById(R.id.card_name);
            mColorLabel = itemView.findViewById(R.id.cardColorLabel);

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
        }
    }

    public ChildrenAdapter(ArrayList<Child> childItems) {
        mChild = childItems;
    }

    @Override
    public ChildrenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item, parent, false);
        ChildrenViewHolder cvh = new ChildrenViewHolder(v, mListener);
        return cvh;
    }

    @Override
    public void onBindViewHolder(ChildrenViewHolder holder, int position) {
        Child currentItem = mChild.get(position);
        holder.mChildName.setText(currentItem.getFirstName() + " "+ currentItem.getLastName());
    }

    @Override
    public int getItemCount() {
        return mChild.size();
    }
}
