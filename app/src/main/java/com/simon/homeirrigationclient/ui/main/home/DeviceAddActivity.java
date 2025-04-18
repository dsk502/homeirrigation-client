package com.simon.homeirrigationclient.ui.main.home;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.simon.homeirrigationclient.HICApplication;
import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.model.DeviceDatabaseHelper;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.model.RSAUtils;
import com.simon.homeirrigationclient.model.TCPClient;

public class DeviceAddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add);

        //Set the toolbar for the activity
        Toolbar toolbar = findViewById(R.id.toolbar_device_add);
        setSupportActionBar(toolbar);

        EditText editTextAddress = findViewById(R.id.editText_ip_address);

        editTextAddress.setFilters(new InputFilter[]{new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                //Allow "."
                if (source.toString().matches("[A-Za-z0-9.]")) {
                    return source;
                }
                return "";
            }
        }});

        //Set the event of the OK button
        Button buttonOK = findViewById(R.id.button_device_add_ok);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Get the user input (name, IP, port)
                EditText editTextName = findViewById(R.id.editText_device_add_name);
                EditText editTextAddress = findViewById(R.id.editText_ip_address);
                EditText editTextPort = findViewById(R.id.editText_port);
                String newName = editTextName.getText().toString();
                String newAddress = editTextAddress.getText().toString();
                int newPort = Integer.parseInt(editTextPort.getText().toString());

                //Read the client public key
                RSAUtils rsaUtils = HICApplication.getInstance().rsaUtils;
                String clientPubkey = rsaUtils.readKeyFromFile(true);

                //Read the timestamp now
                long timestamp = DeviceDatabaseHelper.getAddTime();

                //Create the DeviceInfo object and its TCP client
                DeviceInfo newDeviceInfo = new DeviceInfo("0", newName, clientPubkey, timestamp, newAddress, newPort, 1);
                newDeviceInfo.tcpClient = new TCPClient(getApplicationContext(), newAddress, newPort);

                //Add the device
                int addDeviceResult = newDeviceInfo.tcpClient.addDeviceRequest(clientPubkey, newDeviceInfo);
                switch(addDeviceResult) {
                    case 0:
                        HICApplication.getInstance().servers.add(newDeviceInfo);
                        Toast.makeText(DeviceAddActivity.this, "Device added", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_NETWORK_TIMEOUT:
                        Toast.makeText(DeviceAddActivity.this, "Error: Network timeout", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_MESSAGE_FORMAT:
                        Toast.makeText(DeviceAddActivity.this, "Error: Message format is wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_MESSAGE_CONTENT:
                        Toast.makeText(DeviceAddActivity.this, "Error: Message content is wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_INTERRUPTED:
                        Toast.makeText(DeviceAddActivity.this, "Error: Device added", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_OTHERS:
                        Toast.makeText(DeviceAddActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                        break;
                }

                //Back to the main activity
                finish();
            }
        });
    }
}