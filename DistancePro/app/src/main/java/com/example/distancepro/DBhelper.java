package com.example.distancepro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Date;

public class DBhelper extends SQLiteOpenHelper {
    public DBhelper(Context context) {
        super(context, "SessionData.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table SessionData(sessionID TEXT primary key,distance TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop table if exists SessionData ");

    }
    public Boolean insertsessiondata(String sessionID ,String distance){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put("date", String.valueOf(date));
        contentValues.put("sessionID",sessionID);
        contentValues.put("distance",distance);
        long result = DB.insert("SessionData",null,contentValues);
        if (result==-1){
            return  false;
        } else {
            return true;
        }

    }
    public Boolean updatesessiondata(String sessionID ,String distance){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put("date", String.valueOf(date));
        //contentValues.put("sessionID",sessionID);
        contentValues.put("distance",distance);
        Cursor cursor = DB.rawQuery("Select * from SessionData where sessionID = ?",new String[] {sessionID});
        if (cursor.getCount()>0) {
            long result = DB.update("SessionData", contentValues, "name=?", new String[]{sessionID});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }else {
            return false;
        }

    }
     public Cursor getData( ){
         SQLiteDatabase DB = this.getWritableDatabase();
         Cursor cursor = DB.rawQuery("Select * from SessionData", null);
         return cursor;
     }
}
