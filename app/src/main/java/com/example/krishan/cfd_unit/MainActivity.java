package com.example.krishan.cfd_unit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private static Button vdrugsbutton;
    private static Button bltbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnClickViewData();
        OnClickCheckBluetooth();
        // BluetoothManager manager=(BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
    }

    public void OnClickViewData() {
        vdrugsbutton=(Button)findViewById(R.id.vdata_but);
        vdrugsbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("com.example.krishan.cfd_unit.ViewDrugs");
                        startActivity(intent);
                    }
                }
        );
    }

    public void OnClickCheckBluetooth(){
        bltbutton=(Button)findViewById(R.id.BLEclass_but);
        bltbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("com.example.krishan.cfd_unit.BluetoothActivity");
                        startActivity(intent);
                    }
                }
        );
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
