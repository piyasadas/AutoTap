package com.example.android.autotap;

import android.app.ActionBar;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    static final int REQUEST_CODE = 1;
    public BluetoothAdapter myBluetoothAdapter = null;
    public Switch bluetoothSwitch;
    public TextView pairedDevicesTextView;
    public ListView pairedDevicesListView;
    public boolean found = false;
    public ArrayList<String> list;
    public TextView noPairedDeviceFoundTextView;
    public Set<BluetoothDevice> pairedDevices;
    public ArrayAdapter<String> pairedDevicesAdapter;
    public BluetoothDevice device;
    public static String EXTRA_ADDRESS = "device_address";


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {

                    bluetoothSwitch.setChecked(false);
                    pairedDevicesTextView.setVisibility(View.INVISIBLE);
                    noPairedDeviceFoundTextView.setVisibility(View.INVISIBLE);
                    pairedDevicesListView.setVisibility(View.INVISIBLE);
                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_ON) {
                    pairedDevicesTextView.setVisibility(View.INVISIBLE);
                    noPairedDeviceFoundTextView.setVisibility(View.INVISIBLE);
                    pairedDevicesListView.setVisibility(View.INVISIBLE);
                    Intent turnBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnBluetoothOn, REQUEST_CODE);
                    pairedDevicesMethod();

                }
            }

        }

    };

    private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    pairedDevicesMethod();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothSwitch = (Switch) findViewById(R.id.bluetooth_switch);
        bluetoothSwitch.setChecked(false);
        pairedDevicesTextView = (TextView) findViewById(R.id.paired_devices_text_view);
        pairedDevicesTextView.setVisibility(View.INVISIBLE);
        pairedDevicesListView = (ListView) findViewById(R.id.paired_devices_list_view);
        pairedDevicesListView.setVisibility(View.INVISIBLE);
        noPairedDeviceFoundTextView = (TextView) findViewById(R.id.no_paired_devices_found_text_view);
        noPairedDeviceFoundTextView.setVisibility(View.INVISIBLE);
        noPairedDeviceFoundTextView.setText("No paired devices were found.");
        list = new ArrayList<>();
        pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        pairedDevicesAdapter.clear();


        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    if (myBluetoothAdapter == null) {
                        noPairedDeviceFoundTextView.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else if (!myBluetoothAdapter.isEnabled()) {
                        bluetoothSwitch.setChecked(false);
                        pairedDevicesTextView.setVisibility(View.INVISIBLE);
                        noPairedDeviceFoundTextView.setVisibility(View.INVISIBLE);
                        Intent turnBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnBluetoothOn, REQUEST_CODE);

                    }
                } else {
                    myBluetoothAdapter.disable();
                    pairedDevicesTextView.setVisibility(View.INVISIBLE);
                    noPairedDeviceFoundTextView.setVisibility(View.INVISIBLE);
                    pairedDevicesListView.setVisibility(View.INVISIBLE);
                    list.clear();
                    pairedDevicesAdapter.notifyDataSetChanged();
                }
            }
        });
        if (myBluetoothAdapter.isEnabled()) {
            bluetoothSwitch.setChecked(true);

            pairedDevicesMethod();
        }

        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                bluetoothSwitch.setChecked(true);

            } else if (resultCode == RESULT_CANCELED) {
                bluetoothSwitch.setChecked(false);
            }
        } else
            bluetoothSwitch.setChecked(false);

    }

    public void pairedDevicesMethod() {
        list.clear();
        pairedDevices = myBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDevicesTextView.setVisibility(View.VISIBLE);
            pairedDevicesListView.setVisibility(View.VISIBLE);
            noPairedDeviceFoundTextView.setVisibility(View.INVISIBLE);
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());//Get the device's name and the address

            }

            pairedDevicesListView.setAdapter(pairedDevicesAdapter);
            pairedDevicesListView.setOnItemClickListener(myListClickListener);

        } else {
            pairedDevicesTextView.setVisibility(View.VISIBLE);
            noPairedDeviceFoundTextView.setVisibility(View.VISIBLE);
            final Dialog pairDialog = new Dialog(this);
            pairDialog.setContentView(R.layout.pair_devices_dialog);
            pairDialog.setTitle("Pair Device");
            pairDialog.show();
            Button cancelButton = (Button) pairDialog.findViewById(R.id.pair_devices_dialog_cancel_button);
            final Button pairButton = (Button) pairDialog.findViewById(R.id.pair_devices_dialog_pair_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    pairDialog.dismiss();

                }
            });
            pairButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent pairedDeviceIntent = new Intent();
                    pairedDeviceIntent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                    pairedDeviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(pairedDeviceIntent);
                    registerReceiver(mReceiver2, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
                    pairDialog.dismiss();
                }
            });

        }

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(MainActivity.this, AutomaticOrManual.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }

    };
}
