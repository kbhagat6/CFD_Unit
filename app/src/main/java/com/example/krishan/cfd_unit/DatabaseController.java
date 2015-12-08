package com.example.krishan.cfd_unit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Krishan on 11/23/2015.
 */
public class DatabaseController {

    private static final String TAG = "DatabaseController"; //used for logging database version changes

    public static final String idkey = "_id";
    public static final String dnamekey= "Name";
    public static final String valkey = "Value";

    public static final String[] ALL_KEYS = new String[] {idkey, dnamekey, valkey};


    private final Context context;
    private DatabaseHelper helperdb;
    private SQLiteDatabase db;

    public static final String DB_NAME = "drugsdata.db";
    public static final String DB_TABLE = "drug_sig_table";
    public static final int DB_VERSION = 1;


    private static final String DATABASE_CREATE_SQL = "CREATE TABLE " + DB_TABLE + " (" + idkey + " INTEGER PRIMARY KEY AUTOINCREMENT, " + dnamekey + " TEXT NOT NULL, " + valkey + " TEXT" + ");";




    public DatabaseController(Context cont) {
        this.context = cont;
        helperdb = new DatabaseHelper(context);
    }


    public DatabaseController open() {

        db = helperdb.getWritableDatabase(); //pass object to db

        return this;
    }


    public void close() {
        helperdb.close();
    }

    // Add a new set of values to be inserted into the database.
    public long insert_row(String dname, String val) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(dnamekey, dname);
        initialValues.put(valkey, val);

        return db.insert(DB_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean delete_row(long rowId) {
        String where = idkey + "=" + rowId;
        if((db.delete(DB_TABLE, where, null) != 0)){
            return true;
        }else{
            return false;
        }
    }


    public void delete_all() {
        Cursor c = get_allrow();
        long rowId = c.getColumnIndexOrThrow(idkey);
        if (c.moveToFirst()) {
            do {
                delete_row(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor get_allrow() {
        String where = null;
        Cursor c = 	db.query(true, DB_TABLE, ALL_KEYS, where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor get_row(long rowId) {
        String where = idkey + "=" + rowId;
        Cursor c = 	db.query(true, DB_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String dname, String val) {
        String where = idkey + "=" + rowId;
        ContentValues newValues = new ContentValues();
        newValues.put(dnamekey, dname);
        newValues.put(valkey, val);
        // Insert it into the database.
        return db.update(DB_TABLE, newValues, where, null) != 0;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(newVersion>oldVersion) {
                Log.w(TAG, "Upgrading db");
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
                onCreate(db);
            }
        }
    }


}
