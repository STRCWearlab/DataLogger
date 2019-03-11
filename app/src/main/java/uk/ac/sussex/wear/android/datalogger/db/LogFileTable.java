/*
 * Copyright (c) 2017. Mathias Ciliberto, Francisco Javier Ordoñez Morales,
 * Hristijan Gjoreski, Daniel Roggen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.ac.sussex.wear.android.datalogger.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

// class for creation of database with sql statement

public class LogFileTable {

    private static final String TAG = LogFileTable.class.getSimpleName();

    public static final String TABLE_NAME_LOGFILES = "logFiles";
    public static final String COLUMN_PATH = "absolute_path";
    public static final String COLUMN_SENSOR_NAME = "sensor_name";
    public static final String COLUMN_SESSION_ID = "session_id";
    public static final String COLUMN_SYNC = "is_sync";

    // Database creation sql statement
    private static final String CREATE_TABLE_LOGFILES =
            "CREATE TABLE " + TABLE_NAME_LOGFILES + " ("
                    + COLUMN_PATH + " TEXT PRIMARY KEY, "
                    + COLUMN_SENSOR_NAME + " TEXT NOT NULL, "
                    + COLUMN_SESSION_ID + " TEXT NOT NULL, "
                    + COLUMN_SYNC + " INTEGER DEFAULT 0, "
                    + "FOREIGN KEY(" + COLUMN_SESSION_ID + ") REFERENCES "
                    + DataCollectionSessionTable.TABLE_NAME_SESSIONS + "("
                    + DataCollectionSessionTable.COLUMN_ID + ") );";

    public static  void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_LOGFILES);
    }

    public static  void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LOGFILES);
        onCreate(db);
    }

    public static String[] getAllColumns(){
        return new String[] {COLUMN_PATH, COLUMN_SENSOR_NAME, COLUMN_SESSION_ID, COLUMN_SYNC};
    }


}
