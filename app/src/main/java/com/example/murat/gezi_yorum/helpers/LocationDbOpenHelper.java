package com.example.murat.gezi_yorum.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.classes.mLocation;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Performs database operations.
 */

public class LocationDbOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "locations.db";
    private static final String TABLE_LOCATIONS = "Locations";
    private static final String COLUMN_LONGTITUDE = "longtitude";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_ALTITUDE = "altitude";
    private static final String COLUMN_FEATURE_NAME = "featureName";
    private static final String COLUMN_DATE = "date";

    private static final String TABLE_TRIPS = "Trips";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_STARTDATE = "startDate";
    private static final String COLUMN_FINISHDATE = "finishDate";

    private static final String TABLE_MEDIA = "Media";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_TRIPID = "trip_id";

    public LocationDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String locationTableCreateQuery = "CREATE TABLE " + TABLE_LOCATIONS +
                " (" + COLUMN_LONGTITUDE + " double NOT NULL, " +
                COLUMN_LATITUDE + " double NOT NULL, " +
                COLUMN_ALTITUDE + " double NOT NULL," +
                COLUMN_FEATURE_NAME + " varchar(255)," +
                COLUMN_DATE + " integer); ";
        String tripsTableCreateQuery = "CREATE TABLE " + TABLE_TRIPS +
                "("+COLUMN_ID+" integer PRIMARY KEY AUTOINCREMENT," +
                 COLUMN_STARTDATE +" INTEGER not null," +
                 COLUMN_FINISHDATE+" INTEGER not null)";
        String mediaTableCreateQuery = "CREATE TABLE " + TABLE_MEDIA +
                " (" + COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TYPE +" varchar(255)," +
                COLUMN_LONGTITUDE + " double NOT NULL, " +
                COLUMN_LATITUDE + " double NOT NULL, " +
                COLUMN_ALTITUDE + " double NOT NULL," +
                COLUMN_PATH + " varchar(255)," +
                COLUMN_TRIPID + " integer,"+
                COLUMN_DATE + " integer ); ";
        db.execSQL(locationTableCreateQuery);
        db.execSQL(tripsTableCreateQuery);
        db.execSQL(mediaTableCreateQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        onCreate(db);
    }

    public void saveLocation(mLocation location, SQLiteDatabase writableDatabase) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LONGTITUDE, location.getLongitude());
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_ALTITUDE, location.getAltitude());
        values.put(COLUMN_DATE, location.getTime());
        writableDatabase.insert(TABLE_LOCATIONS, null, values);
    }
    public long insertTripInfo(long startdate,long finishdate){
        ContentValues values = new ContentValues();
        values.put(COLUMN_STARTDATE,startdate);
        values.put(COLUMN_FINISHDATE,finishdate);
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(TABLE_TRIPS,null ,values);
    }
    public long endTrip(long trip_id, long finishdate){
        ContentValues values = new ContentValues();
        values.put(COLUMN_FINISHDATE,finishdate);
        SQLiteDatabase database = getWritableDatabase();
        return database.update(TABLE_TRIPS,values,COLUMN_ID+"="+trip_id,null);
    }
    public ArrayList<Integer> getTripsIDs(){
        String query = "SELECT "+COLUMN_ID+" FROM "+TABLE_TRIPS +" WHERE "+COLUMN_FINISHDATE+"!="+Long.MAX_VALUE;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<Integer> trip_ids = new ArrayList<>();
        int id_column_index = cursor.getColumnIndex(COLUMN_ID);
        while (!cursor.isAfterLast()) {
            trip_ids.add(cursor.getInt(id_column_index));
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        return trip_ids;
    }
    public HashMap<String, String> getTripInfo(long trip_id){
        String query = "SELECT * FROM "+TABLE_TRIPS+" WHERE "+COLUMN_ID+"='"+trip_id+"'";
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToFirst();
        HashMap<String,String> trip_info = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-YYYY");
        Long starttime = cursor.getLong(cursor.getColumnIndex(COLUMN_STARTDATE));
        trip_info.put("startdate",dateFormat.format(new Date(starttime)));
        Long finishtime = cursor.getLong(cursor.getColumnIndex(COLUMN_FINISHDATE));
        trip_info.put("finishdate",dateFormat.format(new Date(finishtime)));
        cursor.moveToNext();
        cursor.close();
        database.close();
        return trip_info;
    }
    public ArrayList<LatLng> getTripPath(long trip_id) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_LATITUDE + " ," + COLUMN_LONGTITUDE +","+COLUMN_DATE+
                " FROM " + TABLE_LOCATIONS + " AS loc, "+ TABLE_TRIPS+" AS trip " +
                "WHERE  trip."+COLUMN_ID+"='"+trip_id+"' AND " + COLUMN_DATE + ">= trip."+COLUMN_STARTDATE+
                " AND " + COLUMN_DATE+ "<= trip."+COLUMN_FINISHDATE;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<LatLng> points = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            points.add(new LatLng(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGTITUDE))));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return points;
    }

    public long insertMediaFile(MediaFile mediaFile){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE,mediaFile.type);
        values.put(COLUMN_PATH,mediaFile.path);
        values.put(COLUMN_LATITUDE,mediaFile.location.getLatitude());
        values.put(COLUMN_LONGTITUDE, mediaFile.location.getLongitude());
        values.put(COLUMN_ALTITUDE, mediaFile.location.getAltitude());
        values.put(COLUMN_TRIPID, mediaFile.trip_id);
        values.put(COLUMN_DATE,mediaFile.location.getTime());
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(TABLE_MEDIA,null ,values);
    }
    public ArrayList<MediaFile> getMediaFiles(long trip_id, String type , String additionalQuery) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_MEDIA+" WHERE "+COLUMN_TRIPID+"="+trip_id + " AND "+ COLUMN_TYPE + "='" +type+"' "+additionalQuery;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<MediaFile> points = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            points.add(new MediaFile(
                                    cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                                    cursor.getString(cursor.getColumnIndex(COLUMN_PATH)),
                                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGTITUDE)),
                                    cursor.getDouble(cursor.getColumnIndex(COLUMN_ALTITUDE)),
                                    cursor.getLong(cursor.getColumnIndex(COLUMN_TRIPID)),
                                    cursor.getLong(cursor.getColumnIndex(COLUMN_DATE))
                    ));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return points;
    }
    public ArrayList<MediaFile> getMediaFilesForPreview(long trip_id, String type){
        return getMediaFiles(trip_id,type,"LIMIT 4");
    }

    public void logInfoTrip() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_TRIPS;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Log.d("ID", ""+cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            Log.d("STARTDATE",""+ cursor.getString(cursor.getColumnIndex(COLUMN_STARTDATE)));
            Log.d("FINISHDATE",""+ cursor.getString(cursor.getColumnIndex(COLUMN_FINISHDATE)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
    }
    public void logInfoLocation() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_LOCATIONS;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Log.d("LATITUDE", ""+cursor.getString(cursor.getColumnIndex(COLUMN_LATITUDE)));
            Log.d("LONGITUDE",""+ cursor.getString(cursor.getColumnIndex(COLUMN_LONGTITUDE)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
    }
}
