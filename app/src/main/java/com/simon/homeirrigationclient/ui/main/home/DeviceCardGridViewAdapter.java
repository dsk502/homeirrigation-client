package com.simon.homeirrigationclient.ui.main.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;

import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.model.DeviceItem;

import java.util.ArrayList;

public class DeviceCardGridViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<DeviceItem> deviceItems;    //The data of all device cards

    public DeviceCardGridViewAdapter(Context context, ArrayList<DeviceItem> deviceItems) {
        this.context = context;
        this.deviceItems = deviceItems;
    }

    @Override
    public int getCount() {
        return deviceItems.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Set a single card view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.device_card, parent, false);
        }

        TextView deviceCardName = convertView.findViewById(R.id.device_card_name);
        TextView deviceCardAddress = convertView.findViewById(R.id.device_card_address);
        TextView deviceCardMode = convertView.findViewById(R.id.device_card_mode);
        //CardView cardView = convertView.findViewById(R.id.cardView_device_card_item);

        //Get current device item
        DeviceItem deviceItem = deviceItems.get(position);

        //Set the name, address and mode on the UI
        deviceCardName.setText(deviceItem.name);
        deviceCardAddress.setText(deviceItem.address);
        deviceCardMode.setText(deviceItem.mode);

        return convertView;
    }
}