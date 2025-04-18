package com.simon.homeirrigationclient.ui.main.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.Toast;
import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.model.TCPClient;

import java.util.ArrayList;

public class DeviceCardGridViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<DeviceInfo> deviceInfos;    //The data of all device cards (a pointer to application class's server

    public boolean showDeleteButton = false;

    public DeviceCardGridViewAdapter(Context context, ArrayList<DeviceInfo> deviceInfos) {
        this.context = context;
        this.deviceInfos = deviceInfos;
    }

    @Override
    public int getCount() {
        return deviceInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Set a single card view
    @SuppressLint("DefaultLocale")
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
        DeviceInfo deviceInfo = deviceInfos.get(position);

        //Set the name, address and mode on the UI
        deviceCardName.setText(deviceInfo.name);
        deviceCardAddress.setText(String.format("%s:%d", deviceInfo.host, deviceInfo.port));
        if(deviceInfo.mode == 1) {
            deviceCardMode.setText("Scheduled");
        } else {    //mode = 2
            deviceCardMode.setText("Manual");
        }

        //Determine whether to show the delete button
        ImageButton deleteButton = convertView.findViewById(R.id.device_card_delete_button);
        if(showDeleteButton) {
            deleteButton.setVisibility(View.VISIBLE);
            //Log.d("Set delete avail: ", String.valueOf(position));

        } else {
            deleteButton.setVisibility(View.GONE);
        }

        //Set delete button event

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clear the settings on server and delete the device in the client database
                int deleteDeviceResult = deviceInfo.tcpClient.deleteDeviceRequest(deviceInfo.serverId, deviceInfo.serverPubkey);
                switch(deleteDeviceResult) {
                    case 0:
                        //Delete device in the ArrayList
                        deviceInfos.remove(deviceInfo);
                        //Notify data change, to refresh the gridview
                        notifyDataSetChanged();
                        break;
                    case TCPClient.ERR_NETWORK_TIMEOUT:
                        Toast.makeText(context, "Error: Network timeout", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_MESSAGE_FORMAT:
                        Toast.makeText(context, "Error: Message format is wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_MESSAGE_CONTENT:
                        Toast.makeText(context, "Error: Message content is wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_INTERRUPTED:
                        Toast.makeText(context, "Error: Device added", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_OTHERS:
                        Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        return convertView;
    }
}