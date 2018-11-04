package com.example.kanatkapult;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.kanatkapult.WebSocketWorker.ChangeListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

class BluetoothConnect {
    private SharedPreferences.Editor editor;
    private String deviceAddress;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket bluetoothSocket = null;
    private InputStream is = null;
    private OutputStream os = null;
    private ChangeListener changeListener;
    private Handler handler;
    private String data = "";
    private BufferedReader bfRdr;
    private Context context;
    private boolean change = false;

    BluetoothConnect(Context context, ChangeListener changeListener) {
        this.changeListener = changeListener;
        handler = new Handler();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (state == BluetoothAdapter.STATE_ON) {
                        BluetoothStart();
                    }
                }
                if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    if (bluetoothSocket != null) {
                        try {
                            Toast.makeText(context, "disconnected", Toast.LENGTH_SHORT).show();
                            bluetoothSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        context.registerReceiver(receiver, filter);
        if (bluetoothAdapter.isEnabled()) {
            BluetoothStart();
        } else {
            bluetoothAdapter.enable();
        }
    }

    private void BluetoothStart() {
        SharedPreferences preference = context.getSharedPreferences("SvdDevs", Context.MODE_PRIVATE);
        editor = preference.edit();
        editor.apply();
        deviceAddress = preference.getString("devadr", "");
        if (!deviceAddress.isEmpty()) {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
            BluetootCreat();
        }
        else {
            ShowDialog();
        }
    }

    private void BluetootCreat() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (bluetoothSocket == null) {
                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                while (!bluetoothSocket.isConnected() && !change) {
                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        bluetoothSocket.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (!change) {
                    try {
                        os = bluetoothSocket.getOutputStream();
                        is = bluetoothSocket.getInputStream();
                        InputStreamReader inStRdr = new InputStreamReader(is);
                        bfRdr = new BufferedReader(inStRdr);
                        handler.post(connectreader);
                        while (bluetoothSocket.isConnected()) {
                            if (is.available() > 0) {
                                data = bfRdr.readLine();
                                handler.post(runreader);
                            }
                            if (change) {
                                bluetoothSocket.close();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                handler.post(connectreader);
            }
        }).start();
    }

    @Override
    protected void finalize() throws Throwable {
        context.unregisterReceiver(receiver);
        super.finalize();
    }

    private Runnable runreader = new Runnable() {
        @Override
        public void run() {
            if (changeListener != null) {
                changeListener.OnDataReadListener(data);
            }
        }
    };

    void Change() {
        change = true;
    }

    private Runnable connectreader = new Runnable() {
        @Override
        public void run() {
            if (changeListener != null) {
                changeListener.OnChangeListener(bluetoothSocket.isConnected());
            }
            if (!bluetoothSocket.isConnected()) {
                if (!change) {
                    BluetootCreat();
                } else {
                    change = false;
                    ShowDialog();
                }
            }
        }
    };

    private void ShowDialog() {
        ArrayAdapter<String> devices = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);
        final ArrayAdapter<BluetoothDevice> bluetoothdevices = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);
        final Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            devices.add(device.getName() + "\n" + device.getAddress());
            bluetoothdevices.add(device);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setAdapter(devices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bluetoothDevice = bluetoothdevices.getItem(which);
                if (bluetoothDevice != null) {
                    deviceAddress = bluetoothDevice.getAddress();
                }
                editor.putString("devadr", deviceAddress);
                    editor.apply();

                BluetootCreat();
                dialog.dismiss();
            }
        }).create();
        builder.show();
    }

    void WriteData(final String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    os.write(data.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
