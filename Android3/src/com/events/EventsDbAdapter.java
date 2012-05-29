package com.events;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple events database access helper class. Defines the basic CRUD operations
 * for the eventpad example, and gives the ability to list all events as well as
 * retrieve or modify a specific event.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class EventsDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_CONTACT= "contact";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "EventsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table events (_id integer primary key autoincrement, "
        + "title text not null, date text not null, time text not null, contact text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "events";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS events");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public EventsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the events database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public EventsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new event using the title and date provided. If the event is
     * successfully created return the new rowId for that event, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the event
     * @param date the date of the event
     * @return rowId or -1 if failed
     */
    public long createEvent(String title, String date, String time, String contact) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_CONTACT, contact);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the event with the given rowId
     * 
     * @param rowId id of event to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEvent(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all events in the database
     * 
     * @return Cursor over all events
     */
    public Cursor fetchAllEvents() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_DATE, KEY_TIME, KEY_CONTACT}, null, null, null, null, KEY_DATE+", "+KEY_TIME);
    }

    /**
     * Return a Cursor positioned at the event that matches the given rowId
     * 
     * @param rowId id of event to retrieve
     * @return Cursor positioned to matching event, if found
     * @throws SQLException if event could not be found/retrieved
     */
    public Cursor fetchEvent(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_DATE, KEY_TIME, KEY_CONTACT}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    /**
     * Return a Cursor positioned at the event that matches the given rowId
     * 
     * @param rowId id of event to retrieve
     * @return Cursor positioned to matching event, if found
     * @throws SQLException if event could not be found/retrieved
     */
    public Cursor fetchEventTime(String date) throws SQLException {
        Cursor mCursor =

            mDb.query(false, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_DATE, KEY_TIME, KEY_CONTACT}, KEY_DATE + "=" + date, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the event using the details provided. The event to be updated is
     * specified using the rowId, and it is altered to use the title and date
     * values passed in
     * 
     * @param rowId id of event to update
     * @param title value to set event title to
     * @param date value to set event date to
     * @return true if the event was successfully updated, false otherwise
     */
    public boolean updateEvent(long rowId, String title, String date, String time, String contact) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_DATE, date);
        args.put(KEY_TIME, time);
        args.put(KEY_CONTACT, contact);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}

