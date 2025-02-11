package com.simon.homeirrigationclient.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;
import androidx.cardview.widget.CardView;

import com.simon.homeirrigationclient.R;

import java.util.ArrayList;

public class DeviceCardGridViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> items;    //The data of all device cards

    public DeviceCardGridViewAdapter(Context context, ArrayList<String> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.device_card, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textView);
        CardView cardView = convertView.findViewById(R.id.cardView_device_card_item);

        textView.setText(items.get(position));

        return cardView;
    }
}