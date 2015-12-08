package com.example.krishan.cfd_unit;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;




public class ViewDrugs extends Activity {

    ListView dlist;
    SimpleCursorAdapter mycadapt;
    // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DatabaseController myDb;
    EditText drugname;
    private static Button add_drug_but, delete_druglist_but;
    private int[] comparray;
    private String arrstr;
    private int[] ds_array;
    private boolean noscan;
    //private static CheckBox drugcheckbox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drugs);
        drugname=(EditText)findViewById(R.id.editText_dname);

        Bundle extras = getIntent().getExtras();
        noscan=false;
        if (extras!=null) {
            comparray = extras.getIntArray("sumarr");
                arrstr= Integer.toString(comparray[0]);
            for(int i=1; i<comparray.length; i++){
                arrstr+=","+ Integer.toString(comparray[i]);
            }
        }else{
            Toast.makeText(getApplicationContext(), "No Scan Performed", Toast.LENGTH_LONG).show();
            noscan=true;
        }

        open_database();
        onClick_adddrug();
       // onClick_deletedruglist();
        // onClick_itemclick();
        onClickEdittext();
        populatedruglist();

        registerForContextMenu(dlist);

    }




    private void open_database(){
        myDb= new DatabaseController(this);
        myDb.open();
    }

    public void onClick_adddrug(){
        add_drug_but=(Button)findViewById(R.id.button_adddrug);
        add_drug_but.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty((drugname.getText().toString()))) {
                            myDb.insert_row(drugname.getText().toString(), arrstr);
                            drugname.setText("");
                            // myDb.insert_row(drugname.getText().toString(), "200");
                        } else {
                            Toast.makeText(getApplicationContext(), "Enter Data", Toast.LENGTH_LONG).show();
                        }
                        populatedruglist();
                    }
                }
        );

    }

    public void onClickEdittext(){
        drugname=(EditText)findViewById(R.id.editText_dname);
        drugname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){


            }
        });
    }

/*
    public void onClick_deletedruglist(){
        delete_druglist_but=(Button)findViewById(R.id.button_cleardrugs);
        delete_druglist_but.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDb.delete_all();
                        populatedruglist();

                    }
                }
        );
    }*/

    /*
    private void onClick_itemclick(){
        ListView list=(ListView) findViewById(R.id.listView_druglist);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                myDb.delete_row(id);
                populatedruglist();

                return false;
            }
        });


    }
    */

    private void deserializearr(String input){
        String[] strArray = input.split(",");
        ds_array = new int[strArray.length];
        for(int i = 0; i < strArray.length; i++) {
            ds_array[i] = Integer.parseInt(strArray[i]);
        }

    }
    private void populatedruglist(){
        Cursor cursor;
        cursor=myDb.get_allrow();
        //String[] fromfieldnames= new String[]{DatabaseController.idkey, DatabaseController.dnamekey};
        String[] fromfieldnames= new String[]{DatabaseController.dnamekey, DatabaseController.valkey};
        int[] toviewids = new int[]{R.id.TextView_dr, R.id.textView_val};
        // SimpleCursorAdapter mycadapt;
        mycadapt= new SimpleCursorAdapter(getBaseContext(),R.layout.single_drug_layout,cursor,fromfieldnames, toviewids,0);
        //now set adapter to listview
        dlist= (ListView) findViewById(R.id.listView_druglist);
        dlist.setAdapter(mycadapt);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,v.getId(),0,"Compare Drug");
        menu.add(0,v.getId(),0,"Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo information = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int rowPosition = information.position;
        long id= information.id;  //id of the drug in the database

        if(item.getTitle()=="Compare Drug"){
            Log.d("Debug", "dksjflkssfdfjksdlfjslkdjfsdlfsdklfjslkdf");
            Toast.makeText(this, "Comparing Drug", Toast.LENGTH_LONG).show();
            Cursor row;
            row=myDb.get_row(id);
            String values;
            if(row!= null) {
                Log.d("Debug",  "isarow");
                row=myDb.get_row(id);
                values=row.getString(row.getColumnIndex(DatabaseController.valkey));
                deserializearr(values);

                if(noscan==false) {
                    Log.d("Debug","noscan");
                    if(comparray.length!=ds_array.length){
                        Toast.makeText(this, "Cannot Compare. size mismatch", Toast.LENGTH_LONG).show();
                    }else {
                        compareresults();
                    }
                }else{

                   Toast.makeText(this, "Need to Scan first to compare", Toast.LENGTH_LONG).show();
                }

                Log.d("Debugvalues", values);

            }

            //Log.d("Debug", row);

        }else{
            myDb.delete_row(id);
            populatedruglist();
            Toast.makeText(this,"Deleted",Toast.LENGTH_LONG);
        }

        return true;
    }

    public void compareresults(){
        int ctmatch=0;
        int threshold=7;
        int percent;
        for(int i=0; i<comparray.length; i++){
            if(Math.abs(comparray[i]- ds_array[i])<threshold){
                ctmatch++;
            }
        }

        percent=(ctmatch/comparray.length)*100;
        if(percent>80){
            Toast.makeText(this,"DRUG IS A MATCH",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"DRUG IS NOT A MATCH",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_drugs, menu);
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
