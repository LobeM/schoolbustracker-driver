package com.lobemusonda.schoolbustracker_driver;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by lobemusonda on 10/12/18.
 */

public class SpinAdapter extends ArrayAdapter<School> {

    //    Your sent context
    private Context context;
    //    Your custom values for the spinner
//    private BusStation[] values;
    private ArrayList<School> values;


    public SpinAdapter(@NonNull Context context, int textViewResourceId, ArrayList<School> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public School getItem(int position){
        return values.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

//    And the magic goes here
//    This is for the "passive" state of the spinner

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        A dynamic text view is created here
        TextView label = (TextView) super.getView(position, convertView, parent);
//        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
//        Then you get the current item using the values array and the current position
//        You can now reference each method you created in your bean object
        label.setText(values.get(position).getName());

//        And finally return your dynamic view for each spinner
        return label;
    }

//    And here is when the "chooser" is popped up
//    Normally is the same view, but you can customize it if you want

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
//        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getName());
        return label;
    }
}
