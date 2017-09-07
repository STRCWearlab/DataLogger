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


public class DataCollectionSessionTable {

    private static final String TAG = DataCollectionSessionTable.class.getSimpleName();

    public static final String TABLE_NAME_SESSIONS = "sessions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_END = "end";
    public static final String COLUMN_LENGTH = "length";

    // Database creation sql statement
    private static final String CREATE_TABLE_SESSIONS =
            "CREATE TABLE " + TABLE_NAME_SESSIONS + " ("
                    + COLUMN_ID + " TEXT PRIMARY KEY, "
                    + COLUMN_START + " TEXT NOT NULL, "
                    + COLUMN_END + " TEXT NOT NULL DEFAULT '', "
                    + COLUMN_LENGTH + " INTEGER DEFAULT 0);";

    public static  void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SESSIONS);
    }

    public static  void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SESSIONS);
        onCreate(db);
    }

    public static String[] getAllColumns(){
        return new String[] {COLUMN_ID, COLUMN_START, COLUMN_END, COLUMN_LENGTH};
    }

}

