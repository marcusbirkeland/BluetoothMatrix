package com.gmail.birkelandmarcus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class DeviceList extends AppCompatActivity {

    Set <BluetoothDevice> pairedDevices;
    private BluetoothAdapter myBluetooth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        makePairedDevicesList();
    }

    private void makePairedDevicesList() {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = myBluetooth.getBondedDevices();
        final List<String> addressList = new ArrayList<String>();
        final List<String> macList = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt  : pairedDevices) {
                addBondedDeviceToList(bt,addressList, macList);
            }
            ListView listView = (ListView)findViewById(R.id.deviceView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String connectionAddress = new String (macList.get(position));
                    Toast.makeText(getApplicationContext(),"Connecting to " + connectionAddress,Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getBaseContext(),MainActivity.class);
                    intent.putExtra("MAC_ADDRESS",connectionAddress);
                    intent.putExtra("START_CONNECTION",true);
                    startActivity(intent);
                }
            });
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_style, R.id.listStyle,addressList);
            listView.setAdapter(arrayAdapter);
        }
    }

    private void addBondedDeviceToList (BluetoothDevice device, List<String> addressList, List<String> macList ){
        addressList.add(device.getName() + " (" + device.getAddress() + ")");
        macList.add(device.getAddress());
    }
}
