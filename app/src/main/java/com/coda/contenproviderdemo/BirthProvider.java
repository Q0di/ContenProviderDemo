package com.coda.contenproviderdemo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


import android.database.SQLException;
import java.util.HashMap;

public class BirthProvider extends ContentProvider {
    //fields for my content provider
    static final String PROVIDER_NAME= "com.coda.provider.BirthdayProv";
    static final String URL = "content://" + PROVIDER_NAME + "/friends";
    static final Uri CONTENT_URI = Uri.parse(URL);


    //integers for content URI
    static final int FRIENDS = 1;
    static final int FRIENDS_ID= 2;

    DBHelper dbHelper;

    //projection map for query
    public static HashMap<String,String> birthMap;

    //content uri patterns
    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "friends", FRIENDS);
        uriMatcher.addURI(PROVIDER_NAME,"friends/#", FRIENDS_ID);
    }


    //db DECLARATIONS
    private SQLiteDatabase database;
    static final String DATABASE_NAME = "BirthdayReminder.db";
    static final String TABLE_NAME ="birthTable";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_TABLE =  " CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + " name TEXT NOT NULL, "
            + " birthday TEXT NOT NULL);";

    //fields for db
    static final String ID = "id";
    static final String NAME = "name";
    static final String BIRTHDAY = "birthday";

    //helper DB class
    private static class DBHelper extends SQLiteOpenHelper{


        public DBHelper(Context context) {
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(DBHelper.class.getName(), " Upgrading database from version " + oldVersion + " to "
            + newVersion + ". Old data will be destroyed");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

    }//end DBHelper

    public BirthProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
     int count = 0;

        switch (uriMatcher.match(uri)){
            //deletes all the records of the table
            case FRIENDS:
                count = database.delete(TABLE_NAME,selection,selectionArgs);
                break;
            case FRIENDS_ID:
                String id = uri.getLastPathSegment();//gets id
                count = database.delete(TABLE_NAME,ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ")" : "" ), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case FRIENDS:
                return "vnd.android.cursor.dir/vnd.coda.friends";
            case FRIENDS_ID:
                return "vnd.android.cursor.item/vnd.coda.friend";
            default:
                throw new IllegalArgumentException("Unsupported uri " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = database.insert(TABLE_NAME,"",values);

        if(row > 0){
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, row);
            getContext().getContentResolver().notifyChange(newUri,null);

            return newUri;
        }
        throw new SQLException("Failed to add a new record into" +uri);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)){
            case FRIENDS:
                queryBuilder.setProjectionMap(birthMap);
                break;
            case FRIENDS_ID:
                queryBuilder.appendWhere(ID + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown uri "+uri);

        }

        if(sortOrder == null || sortOrder == ""){
            sortOrder = NAME;

        }

        Cursor cursor = queryBuilder.query(database,projection,selection,selectionArgs,null,null,sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            //deletes all the records of the table
            case FRIENDS:
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case FRIENDS_ID:
                String id = uri.getLastPathSegment();//gets id
                count = database.update(TABLE_NAME, values, ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri "+uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}
