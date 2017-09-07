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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import uk.ac.sussex.wear.android.datalogger.data.DataCollectionSession;

public class DataLoggerDataSource {

    private static final String TAG = DataLoggerDataSource.class.getSimpleName();

    public static boolean insertLogFile(Context context, String absolutePath, String sensorName, String sessionName) {
        DataLoggerOpenHelper dbHelper = new DataLoggerOpenHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean insertionSuccess = false;

        ContentValues values = new ContentValues();
        values.put(LogFileTable.COLUMN_PATH, absolutePath);
        values.put(LogFileTable.COLUMN_SENSOR_NAME, sensorName);
        values.put(LogFileTable.COLUMN_SESSION_ID, sessionName);

        long insertId = database.insert(LogFileTable.TABLE_NAME_LOGFILES,
                null,
                values);

        // check db insert was correct
        if(insertId != -1){
            insertionSuccess = true;
        }

        dbHelper.close();
        return insertionSuccess;
    }

    public static boolean existLogFile(Context context, String absolutePath){
        DataLoggerOpenHelper dbHelper = new DataLoggerOpenHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean exist = false;

        Cursor cursor = database.query(LogFileTable.TABLE_NAME_LOGFILES,
                LogFileTable.getAllColumns(), LogFileTable.COLUMN_PATH + " =?",
                new String[] {absolutePath}, null, null, null, null);

        if(cursor.getCount() == 1){
            exist = true;
        }
        cursor.close();
        dbHelper.close();
        return exist;
    }

    public static ArrayList<String> selectUnsyncLogFiles(Context context, String sensorName){
        DataLoggerOpenHelper dbHelper = new DataLoggerOpenHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ArrayList<String> logFilePaths = new ArrayList<>();

        String MY_QUERY = "SELECT " + LogFileTable.COLUMN_PATH
                + " FROM " + LogFileTable.TABLE_NAME_LOGFILES
                + " WHERE " + LogFileTable.COLUMN_SYNC + " = ?"
                + " AND " + LogFileTable.COLUMN_SENSOR_NAME + " = ?";

        Cursor cursor = database.rawQuery(MY_QUERY, new String[]{"0", sensorName});

        while (cursor.moveToNext()) {
            logFilePaths.add(cursor.getString(0));
        }
        dbHelper.close();
        return logFilePaths;
    }

    public static boolean markLogFileAsSync(Context context, String filePath){
        boolean updateSuccess = false;
        DataLoggerOpenHelper dbHelper = new DataLoggerOpenHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LogFileTable.COLUMN_SYNC, 1);
        String whereClause = LogFileTable.COLUMN_PATH + " = '" + filePath +"'";;

        long updateId = database.update(LogFileTable.TABLE_NAME_LOGFILES,
                        values,
                        whereClause,
                        null);

        // check db update was correct
        if (updateId > 0){
            updateSuccess = true;
        }

        return updateSuccess;
    }

    public static boolean insertDataCollectionSession(Context context, DataCollectionSession dataCollectionSession){
        DataLoggerOpenHelper dbHelper = new DataLoggerOpenHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean insertionSuccess = false;

        ContentValues values = new ContentValues();
        values.put(DataCollectionSessionTable.COLUMN_ID, dataCollectionSession.getSessionName());
        values.put(DataCollectionSessionTable.COLUMN_START, dataCollectionSession.getStartDateISO8601());

        long insertId = database.insert(DataCollectionSessionTable.TABLE_NAME_SESSIONS,
                null,
                values);

        // check db insert was correct
        if(insertId != -1){
            insertionSuccess = true;
            Log.i(TAG, "Data collection session inserted with id: " + dataCollectionSession.getSessionName());
        }
        dbHelper.close();
        return insertionSuccess;
    }

    public static boolean updateEndDateDataCollectionSession(Context context, DataCollectionSession dataCollectionSession){
        DataLoggerOpenHelper dbHelper = new DataLoggerOpenHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean updateSuccess = false;

        ContentValues values = new ContentValues();
        values.put(DataCollectionSessionTable.COLUMN_END, dataCollectionSession.getEndDateISO8601());
        values.put(DataCollectionSessionTable.COLUMN_LENGTH, dataCollectionSession.getLength());

        long updateId = database.update(DataCollectionSessionTable.TABLE_NAME_SESSIONS,
                values,
                DataCollectionSessionTable.COLUMN_ID + "='" + dataCollectionSession.getSessionName() + "'",
                null);

        // check db update was correct
        if (updateId > 0){
            updateSuccess = true;
            Log.i(TAG, "Data collection session update with id: " + dataCollectionSession.getSessionName());
        }
        dbHelper.close();
        return updateSuccess;
    }


    public static boolean existDataCollectionSession(Context context, DataCollectionSession dataCollectionSession){
        DataLoggerOpenHelper dbHelper = new DataLoggerOpenHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        boolean exist = false;

        Cursor cursor = database.query(DataCollectionSessionTable.TABLE_NAME_SESSIONS,
                DataCollectionSessionTable.getAllColumns(), DataCollectionSessionTable.COLUMN_ID + " =?",
                new String[] {dataCollectionSession.getSessionName()}, null, null, null, null);

        if(cursor.getCount() == 1){
            exist = true;
        }
        cursor.close();
        dbHelper.close();
        return exist;
    }


}
