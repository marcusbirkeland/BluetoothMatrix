package com.gmail.birkelandmarcus;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler mainHandler = new Handler();
    TextView statusText;
    EditText editTextDisplayString;
    ImageButton btButton;
    Button sendBtn, dcButton;
    SeekBar brightnessSeekBar, speedSeekBar;
    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket bluetoothSocket = null;
    private String macAddress;
    private boolean btIsConnected = false;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        btButton = findViewById(R.id.btButton);
        dcButton = findViewById(R.id.dcButton);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        brightnessSeekBar.setMax(8);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        speedSeekBar.setMax(100);
        editTextDisplayString = findViewById(R.id.editTextDisplayString);
        sendBtn = findViewById(R.id.sendBtn);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();

        Intent intent = getIntent();
        macAddress = intent.getStringExtra("MAC_ADDRESS");
        boolean startConnection = intent.getBooleanExtra("START_CONNECTION", false);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable checkConnectionRunnable = new checkConnectionRunnable();
        Runnable connectBtRunnable = new connectBtRunnable();
        Callable<Boolean> checkConnectionCallable = new checkConnectionCallable();

        //checkConnectionThread.start();

        if (startConnection == true) {
            executorService.execute(connectBtRunnable);
            executorService.execute(checkConnectionRunnable);
        }
        btButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDeviceList();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothSocket.isConnected()) {
                    String output = editTextDisplayString.getText().toString();
                    try {
                        sendStringBluetooth(output);
                        editTextDisplayString.getText().clear();
                    } catch (IOException e) {
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Connect a device first!", Toast.LENGTH_LONG).show();
                }
            }
        });
        dcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    disconnect();
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected!", Toast.LENGTH_LONG).show();
                }
            }
        });

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (bluetoothSocket.isConnected()) {
                    String sliderValue = "INTENSITY" + Integer.toString(brightnessSeekBar.getProgress());
                    try {
                        sendStringBluetooth(sliderValue);
                    } catch (IOException e) {
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Connect a device first!", Toast.LENGTH_LONG).show();
                }
            }
        });
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (bluetoothSocket.isConnected()) {
                    String sliderValue = "SPEED" + Integer.toString(speedSeekBar.getProgress());
                    try {
                        sendStringBluetooth(sliderValue);
                    } catch (IOException e) {
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Connect a device first!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void checkBluetoothState() {
        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth adapter!", Toast.LENGTH_LONG).show();
            finish();
        } else if (!myBluetooth.isEnabled()) {
            Intent intent;
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }
    }

    private void viewDeviceList() {
        Intent intent = new Intent(this, DeviceList.class);
        startActivity(intent);
    }

    private void disconnect() {
        try {
            bluetoothSocket.close();
            Toast.makeText(getApplicationContext(), "Disconnecting...", Toast.LENGTH_LONG).show();
        } catch (IOException E) {
            Toast.makeText(getApplicationContext(), "No device connected!", Toast.LENGTH_LONG).show();
        }
    }

    private void sendStringBluetooth(String inputString) throws IOException {
        if (inputString.length() <= 256) {
            try {
                OutputStream socketOutputStream = bluetoothSocket.getOutputStream();
                byte[] buffer = inputString.getBytes();
                Log.d("Buffer:", inputString);
                socketOutputStream.write(buffer);
                Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
            }
        } else {
            Toast.makeText(getApplicationContext(), "Message too long", Toast.LENGTH_LONG).show();
        }
    }

   /* private String readStringBluetooth() throws IOException {
        InputStream socketInputStream = bluetoothSocket.getInputStream();
        byte[] buffer = new byte[256];
        String input = "";
        int bytes;
        while (true) {
            try {
                bytes = socketInputStream.read(buffer);
                input += new String(buffer, 0, bytes);
            } catch (IOException e) {
                break;
            }
        }
        return input;
    }
 */

    class connectBtRunnable implements Runnable {
        connectBtRunnable() {
        }

        @Override
        public void run() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    statusText.setText("Connecting. . .");
                    statusText.setTextColor(Color.YELLOW);
                }
            });
            try {
                if (bluetoothSocket == null) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice bluetoothDevice = myBluetooth.getRemoteDevice(macAddress);
                    bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("Connected");
                            statusText.setTextColor(Color.GREEN);
                        }
                    });
                }
            } catch (IOException e) {
                // If connection failed
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Failed.");
                        statusText.setTextColor(Color.RED);
                    }
                });
            }
        }
    }

    class checkConnectionRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    statusText.setText("Connected");
                                    statusText.setTextColor(Color.GREEN);
                                }
                            });
                        } else {
                            btIsConnected = false;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    statusText.setText("Disconnected");
                                    statusText.setTextColor(getResources().getColor(R.color.colorAccent));
                                }
                            });
                        }
                    }
                });
                SystemClock.sleep(10);
                if (Thread.interrupted()) {
                    return;
                }
            }
        }
    }

    public class checkConnectionCallable implements Callable<Boolean> {
        @Override
        public Boolean call() throws IOException {
            while (true) {
                try {
                    bluetoothSocket.getInputStream(); // Will throw exception if device is disconnected.
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("Connected");
                            statusText.setTextColor(Color.GREEN);
                        }
                    });
                } catch (IOException e) {mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Disconnected");
                        statusText.setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                    });
                }
                SystemClock.sleep(20);
            }
        }
    }
}




