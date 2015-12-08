package com.example.krishan.cfd_unit;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.UnsupportedEncodingException;

public class AdvancedMenu extends Activity {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private int mState;

    private static ToggleButton laseronoff;
    private static ToggleButton heatsinkonoff;
    private static Button submitmt;
    private UartService mService;
    private BluetoothAdapter mBtAdapter;
    EditText sliderval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_menu);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter=bluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent prevact= getIntent();
        Bundle b = prevact.getExtras();
        if(b!=null)
        {
            mState= b.getInt("mState");

        }

        sliderval=(EditText)findViewById(R.id.sliderval);

        service_init();
        onClickLaseronoff();
        onClickHeatsinkonoff();
        onClickSendSlider();


    }


    public void onClickLaseronoff(){
        laseronoff=(ToggleButton)findViewById(R.id.Laserbutton);
        laseronoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (laseronoff.isChecked()) {
                    byte[] value;
                    try {
                        //send data to service
                        String message = "StartLaser";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(AdvancedMenu.this, "Laser powering on", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // recdatawait = false;
                    // stopped = true;

                } else {
                    byte[] value;
                    try {
                        //send data to service
                        String message = "StopLaser";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(AdvancedMenu.this, "Laser powering off", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //recdatawait = false;
                }
            }
        });
    }

    public void onClickHeatsinkonoff(){
        heatsinkonoff=(ToggleButton)findViewById(R.id.HeatsinkButton);
        heatsinkonoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (heatsinkonoff.isChecked()) {
                    byte[] value;
                    try {
                        //send data to service
                        String message = "Heatsinkon";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(AdvancedMenu.this, "Heat Sink powering on", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // recdatawait = false;
                    // stopped = true;

                } else {
                    byte[] value;
                    try {
                        //send data to service
                        String message = "Heatsinkoff";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(AdvancedMenu.this, "Heat Sink powering off", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void onClickSendSlider(){
        submitmt=(Button)findViewById(R.id.submitbut);
        submitmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(sliderval.getText().toString())) {
                    byte[] value;
                    try {
                        //send data to service
                        String message = sliderval.getText().toString();
                        int num = Integer.parseInt(message);
                        if(num>9 && num <241){
                            String newstring = "slide:" + message;
                            value = newstring.getBytes("UTF-8");
                            mService.writeRXCharacteristic(value);
                            Toast.makeText(AdvancedMenu.this, "Slider command sent", Toast.LENGTH_LONG).show();

                        }else{
                            Toast.makeText(AdvancedMenu.this, "Slider needs to be between 10 and 240", Toast.LENGTH_LONG).show();
                        }


                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        Log.d("DEbug", "slidervalnotsent");
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d("DEbug", "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e("Debug", "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };
    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        //LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
        Log.d("Debug", "after service init");
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_advanced_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
