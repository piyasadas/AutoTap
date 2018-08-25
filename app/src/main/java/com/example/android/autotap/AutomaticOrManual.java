package com.example.android.autotap;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

public class AutomaticOrManual extends AppCompatActivity {

    String address = null;
    Button automatic;
    Button manual;
    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    BluetoothAdapter myBluetoothAdapter = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Spinner btnSpinner;
    Button btnManualCancel;
    Button btnManualOk;
    String[] Time = {"seconds", "minutes", "hours"};
    EditText timeInput;
    String valueOfTime;
    public int time;
    Button btnBluetoothCancel;
    Button btnBluetoothOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final Intent newIntent = getIntent();
        address = newIntent.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        super.onCreate(savedInstanceState);
        final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                            == BluetoothAdapter.STATE_OFF){
                        automatic.setEnabled(false);
                        manual.setEnabled(false);
                        final Dialog bluetoothOffDialog = new Dialog(AutomaticOrManual.this);
                        bluetoothOffDialog.setContentView(R.layout.bluetooth_state_changed);
                        btnBluetoothCancel = (Button)bluetoothOffDialog.findViewById(R.id.bluetooth_state_changed_cancel_button);
                        btnBluetoothOk = (Button)bluetoothOffDialog.findViewById(R.id.bluetooth_state_changed_ok_button);
                        bluetoothOffDialog.show();

                        btnBluetoothCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });

                        btnBluetoothOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(AutomaticOrManual.this, MainActivity.class);
                                startActivity(i);
                            }
                        });


                    }
                    else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                            == BluetoothAdapter.STATE_ON){
                        Intent i = new Intent(AutomaticOrManual.this, MainActivity.class);
                        startActivity(i);
                    }
                }
            }
        };



        setContentView(R.layout.activity_automatic_or_manual);

        automatic = (Button) findViewById(R.id.automatic_mode);
        automatic.setEnabled(false);
        manual = (Button) findViewById(R.id.manual_mode);
        manual.setEnabled(false);

        registerReceiver(mReceiver2, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        new ConnectBT().execute(); //Call the class to connect

        automatic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket!=null)
                {
                    try
                    {
                        int i = -5;
                        PrintWriter output = new PrintWriter(btSocket.getOutputStream());
                        output.print(i);
                        output.flush();
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }

            }
        });

        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog manualModeDialog = new Dialog(AutomaticOrManual.this);
                manualModeDialog.setContentView(R.layout.manual_mode);
                btnSpinner = (Spinner) manualModeDialog.findViewById(R.id.spinner_button);
                btnManualCancel = (Button) manualModeDialog.findViewById(R.id.manual_mode_cancel_button);
                btnManualOk = (Button) manualModeDialog.findViewById(R.id.manual_mode_ok_button);

                final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();

                        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                                    == BluetoothAdapter.STATE_OFF){
                                automatic.setEnabled(false);
                                manual.setEnabled(false);
                                manualModeDialog.dismiss();
                                final Dialog bluetoothOffDialog = new Dialog(AutomaticOrManual.this);
                                bluetoothOffDialog.setContentView(R.layout.bluetooth_state_changed);
                                btnBluetoothCancel = (Button)bluetoothOffDialog.findViewById(R.id.bluetooth_state_changed_cancel_button);
                                btnBluetoothOk = (Button)bluetoothOffDialog.findViewById(R.id.bluetooth_state_changed_ok_button);
                                bluetoothOffDialog.show();

                                btnBluetoothCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                });

                                btnBluetoothOk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(AutomaticOrManual.this, MainActivity.class);
                                        startActivity(i);
                                    }
                                });

                            }
                            else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                                    == BluetoothAdapter.STATE_ON){
                                Intent i = new Intent(AutomaticOrManual.this, MainActivity.class);
                                startActivity(i);
                            }
                        }
                    }
                };
                registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));


                manualModeDialog.show();

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(AutomaticOrManual.this, android.R.layout.simple_spinner_item, Time);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                btnSpinner.setAdapter(spinnerAdapter);


                btnManualCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        manualModeDialog.dismiss();
                    }
                });


                        btnSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                                final Object item = parent.getItemAtPosition(position);

                                btnManualOk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        timeInput = (EditText) manualModeDialog.findViewById(R.id.time_input_text);
                                        valueOfTime = timeInput.getText().toString();
                                        if(valueOfTime.equals("") || valueOfTime.length() == 0){
                                            time = 0;
                                        }
                                        else {
                                            time = Integer.parseInt(valueOfTime);
                                        }
                                        if(time == 0){
                                            Toast.makeText(getApplicationContext(),
                                                    "Please set duration",
                                                    Toast.LENGTH_LONG).show();
                                        } else {

                                            if(item.toString().equals("minutes")){
                                                final int timeInSeconds = time * 60;
                                                        if (btSocket!=null)
                                                        {
                                                            try
                                                            {

                                                                PrintWriter output = new PrintWriter(btSocket.getOutputStream());
                                                                output.print(timeInSeconds);
                                                                output.flush();
                                                            }
                                                    catch (IOException e)
                                                    {
                                                        msg("Error");
                                                    }
                                                }if (btSocket!=null)
                                                        {
                                                            try
                                                            {
                                                                btSocket.getOutputStream().write(timeInSeconds);
                                                                btSocket.getOutputStream().flush();
                                                            }
                                                    catch (IOException e)
                                                    {
                                                        msg("Error");
                                                    }
                                                }
                                            }
                                            else if(item.toString().equals("hours")){
                                                final int timeInSeconds = time * 3600;
                                                if (btSocket!=null)
                                                {
                                                    try
                                                    {
                                                        PrintWriter output = new PrintWriter(btSocket.getOutputStream());
                                                        output.print(timeInSeconds);
                                                        output.flush();
                                                    }
                                                    catch (IOException e)
                                                    {
                                                        msg("Error");
                                                    }
                                                }
                                            }
                                            else if(item.toString().equals("seconds")){
                                                final int timeInSeconds = time;
                                                if (btSocket!=null)
                                                {
                                                    try
                                                    {
                                                        PrintWriter output = new PrintWriter(btSocket.getOutputStream());
                                                        output.print(timeInSeconds);
                                                        output.flush();
                                                    }
                                                    catch (IOException e)
                                                    {
                                                        msg("Error");
                                                    }
                                                }
                                            }

                                            Toast.makeText(getApplicationContext(),
                                                    "Time is set for " + time + " " +  " " + item.toString(),
                                                    Toast.LENGTH_LONG).show();
                                            manualModeDialog.dismiss();
                                        }

                                    }
                                });

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                    }
        });

    }

   // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(AutomaticOrManual.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
                automatic.setEnabled(true);
                manual.setEnabled(true);
            }
            progress.dismiss();
        }

    }
}
