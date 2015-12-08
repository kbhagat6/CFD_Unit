package com.example.krishan.cfd_unit;

import com.example.krishan.cfd_unit.UartService;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ServiceConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;

public class ScanningActivity extends Activity {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private static Button startcalibbut;
    private static Button startscanbut;
    private static ToggleButton laseronoff;
    private static ToggleButton heatsinkonoff;
    private static ImageButton settingsbut;

    private static CheckBox pillcheck;
    private UartService mService;
    private BluetoothAdapter mBtAdapter;
    private boolean pillck=false;
    private boolean calibration=false;
    private boolean scanstart=false;
    private boolean finishedlowpass=false;
    private boolean recdatawait=false;
    private boolean stopped=true;
    private int count;
    private int mState;
    private int highpass[];
    private int lowpass[];
    private int emptyspec[];
    private int nofilter[];
    private int sumarr[];


    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        Log.d("Debug", "In Scanning create");

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter=bluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listView);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);

        Intent prevact= getIntent();
        Bundle b = prevact.getExtras();
        if(b!=null)
        {
            mState= b.getInt("mState");

        }
        count=0;
        emptyspec= new int[236];
        nofilter = new int[236];
        highpass= new int[236];
        lowpass= new int[236];
        sumarr= new int[236];


        Log.d("Debug", "Before Scanning Service init");
        service_init();
        if(mState==UART_PROFILE_CONNECTED){
            Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Not Connected", Toast.LENGTH_LONG).show();
            finish();
        }

        onchkpill();
        onClickStartCalibration();
        onClickStartScanning();
        onClickSettings();
        //onClickStopScanning();
    }

    public void onchkpill(){
        pillcheck=(CheckBox)findViewById(R.id.checkboxplcdrug);
        pillcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (((CheckBox) view).isChecked()) {
                    Toast.makeText(ScanningActivity.this, "Confirming capsule is in container", Toast.LENGTH_LONG).show();
                    pillck=true;
                }else{
                    Toast.makeText(ScanningActivity.this, "Capsule is not in container", Toast.LENGTH_LONG).show();
                    pillck=false;
                }
            }
        });
    }

    public void onClickSettings(){
        settingsbut=(ImageButton)findViewById(R.id.imageButton);
        settingsbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.example.krishan.cfd_unit.AdvancedMenu");
                intent.putExtra("mState",mState);
                startActivity(intent);
            }
        });


    }

    public void onClickStartCalibration(){
        startcalibbut=(Button)findViewById(R.id.calibbutton);
        startcalibbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recdatawait==false) {
                    byte[] value;
                    try {
                        //send data to service
                        String message = "Calibration";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    calibration = true;
                    stopped=false;
                }else{
                    Toast.makeText(ScanningActivity.this, "Still collecting data", Toast.LENGTH_LONG).show();

                }

            }
        });
    }

    public void onClickStartScanning(){
        startscanbut=(Button)findViewById(R.id.scanbutton);
        startscanbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (recdatawait == false &&pillck==true) {
                        byte[] value;
                        try {
                            //send data to service
                                String message = "StartScan";
                                value = message.getBytes("UTF-8");
                                mService.writeRXCharacteristic(value);

                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    scanstart = true;
                    stopped=false;
                    recdatawait = true;
                } else if(pillck==false) {
                   Toast.makeText(ScanningActivity.this, "Cannot Scan: no confirmation that pill is in container", Toast.LENGTH_LONG).show();
               }else if(pillck==true&&recdatawait==false) {
                   Toast.makeText(ScanningActivity.this, "Still collecting data", Toast.LENGTH_LONG).show();
               }

                   //Toast.makeText(ScanningActivity.this, "Still collecting data", Toast.LENGTH_LONG).show();
                   /* if(recdatawait==true) {
                        Toast.makeText(ScanningActivity.this, "Still collecting data", Toast.LENGTH_LONG).show();
                    }else if(pillck==false){
                        Toast.makeText(ScanningActivity.this, "Cannot Scan: no confirmation that pill is in container", Toast.LENGTH_LONG).show();
                    }*/

            }


        });
    }
/*
    public void onClickLaseronoff(){
        laseronoff=(ToggleButton)findViewById(R.id.Laserbutton);
        laseronoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(laseronoff.isChecked()){
                    byte[] value;
                    try {
                        //send data to service
                        String message = "StartLaser";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(ScanningActivity.this, "Laser powering on", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                   // recdatawait = false;
                   // stopped = true;

                }else{
                    byte[] value;
                    try {
                        //send data to service
                        String message = "StopLaser";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(ScanningActivity.this, "Laser powering off", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //recdatawait = false;
                }
            }
        });
    }*/
   /*
    public void onClickHeatsinkonoff(){
        heatsinkonoff=(ToggleButton)findViewById(R.id.HeatsinkButton);
        heatsinkonoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(heatsinkonoff.isChecked()){
                    byte[] value;
                    try {
                        //send data to service
                        String message = "Heatsinkon";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(ScanningActivity.this, "Heat Sink powering on", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // recdatawait = false;
                    // stopped = true;

                }else{
                    byte[] value;
                    try {
                        //send data to service
                        String message = "Heatsinkoff";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(ScanningActivity.this, "Heat Sink powering off", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    } */

    /*
    public void onClickStopScanning(){
        stopscanbut=(Button)findViewById(R.id.stopbutton);
        stopscanbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(recdatawait==false) {
                if (stopped == false) {
                    byte[] value;
                    try {
                        //send data to service
                        String message = "Stop";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        Toast.makeText(ScanningActivity.this, "Stop request sent", Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    recdatawait = false;
                    stopped = true;
                } else {
                    Toast.makeText(ScanningActivity.this, "Request to stop has already been sent", Toast.LENGTH_LONG).show();
                }
                // calibration = true;
                // }else{
                //   Toast.makeText(ScanningActivity.this, "Still collecting data", Toast.LENGTH_LONG).show();

                // }
            }

        });

    }*/

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

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
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

    private void comparespectrum(){
        Intent intent = new Intent("com.example.krishan.cfd_unit.ViewDrugs");
        intent.putExtra("sumarr",sumarr);
        startActivity(intent);
    }

    private void populate_intarray(final byte txValue[], int whicharray) {
        for (int i = 0; i < txValue.length; i += 2) {
            int result = (txValue[i] & 0xff) |
                    ((txValue[i + 1] & 0xff) << 8);
            String ct = String.valueOf(result);

            if(finishedlowpass==false) {
                Log.d("low: ", ct);
                if (count < lowpass.length) {
                    lowpass[count] = result;
                    count++;
                    String a= String.valueOf(count);
                    Log.d("count:", a);
                    if(count==236) {
                        finishedlowpass = true;
                        Log.d("Debug", "finishedlowpass");
                        count = 0;
                    }
                }
            }else{
                Log.d("high: ", ct);
                if (count < highpass.length) {
                    highpass[count] = result;
                    sumarr[count]=lowpass[count]+result;
                    count++;
                    String a= String.valueOf(count);
                    Log.d("count:", a);
                    if(count==236) {
                        finishedlowpass = false;
                        recdatawait = false;
                        count = 0;
                        comparespectrum();
                    }
                }
            }
        }
    }
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");

                       // btnConnectDisconnect.setText("Disconnect");
                       // edtMessage.setEnabled(true);
                       // btnSend.setEnabled(true);
                       // ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                       // listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());   //connected
                       // messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                        Log.d("Debug", "Scanning serv connected");
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                   //     btnConnectDisconnect.setText("Connect");
                    //    edtMessage.setEnabled(false);
                    //    btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                   //     listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String mstate = Integer.toString(mState);
                            //Log.d("mstate:", mstate);
                            Log.d("Debug", "ScanningActivity, data received");

                            String text = new String(txValue, "UTF-8");
                            Log.d("Debug",text);
                            int size= text.length();

                          if (text.matches("^[a-zA-Z0-9 ]*$")){  //if there are only numbers in the 20 byte packet.. process in array, otherwise we call it log data
                              String msg= gettinglog(text);
                              String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                              listAdapter.add("[" + currentDateTimeString + "]: " + msg);            ///  outputting the read data
                              messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                            }else{
                              recdatawait=true;
                              populate_intarray(txValue, 1);
                            }

                          //  String b= String.valueOf(txValue.length);
                          //  Log.d("lengthtxVal:", b);


                        } catch (Exception e) {
                            Log.e("Debug", e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };


    private String gettinglog(String log){
        String retstring;
        if(log.contains("aa")){
            retstring="Starting the data collection process!";
        }else if(log.contains("bb")){
            retstring="First, the laser will remain off and an empty spectrum will be captured for noise removal procedure";
        }else if(log.contains("bcd")){
            retstring="Printing captured spectrum";
        }else if(log.contains("cc")){
            retstring="Next the Laser will be turned on and a spectrum with No Filter will be captured for calibration purposes";
        }else if(log.contains("dd")){
            retstring="Laser emissions have been started!";
        }else if(log.contains("ee")){
            retstring="You should now see a flashing LED on the outside indicating that the laser emissions have been started!";
        }else if(log.contains("ff")){
            retstring="The app also should indicate that the laser emissions are undergoing and the device shouldn't be opened at this point";
        }else if(log.contains("gg")){
            retstring="Laser will now be turned off!";
        }else if(log.contains("hh")){
            retstring="Printing captured spectrum";
        }else if(log.contains("ii")){
            retstring="Moving the Filter assembly to the Low Pass Edge Position";
        }else if(log.contains("jj")){
            retstring="During this time, the cooling systems will not work properly";
        }else if(log.contains("kk")){
            retstring="The Filter Assembly is now on the Low Pass Edge Position";
        }else if(log.contains("ll")){
            retstring="Next, the laser will be turned on again and the spectrum with Low Pass Edge configuration will be captured";
        }else if(log.contains("mm")){
            retstring="Laser will now be turned off!";
        }else if(log.contains("nn")){
            retstring="Printing captured spectrum";
        }else if(log.contains("oo")){
            retstring="Moving the Filter assembly to the High Pass Edge Position";
        }else if(log.contains("pp")){
            retstring="During this time, the cooling systems will not work properly";
        }else if(log.contains("qq")){
            retstring="Next, the laser will be turned on again and the spectrum with High Pass Edge configuration will be captured";
        }else if(log.contains("rr")){
            retstring="The Filter Assembly is now on the High Pass Edge Position";
        }else if(log.contains("ss")){
            retstring="Data capture is complete. The laser will be turned off completely";
        }else if(log.contains("tt")){
            retstring="Printing captured spectrum";
        }else if(log.contains("uu")){
            retstring="The collected data is now being processed! Wait for a few seconds!";
        }else if(log.contains("vv")){
            retstring="The processed data is now being reported via Bluetooth";
        }else if(log.contains("ww")){
            retstring="Performing cleanup actions";
        }else if(log.contains("xx")){
            retstring="Filter Assembly is being reset to No Filter Position";
        }else{
            return log;
        }
        return retstring;
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("Debug", "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanning, menu);
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
    /*
    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent("com.example.krishan.cfd_unit.BluetoothActivity");
            //   startMain.addCategory(Intent.CATEGORY_HOME);
            // startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            //showMessage("Currently Connected\n             Disconnect to exit");
        }
    } */
}
