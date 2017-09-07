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

package uk.ac.sussex.wear.android.datalogger.log;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.ac.sussex.wear.android.datalogger.db.DataLoggerDataSource;

public class CustomLogger {

    private static final String TAG = CustomLogger.class.getSimpleName();

    private BufferedOutputStream mOutputStream = null;
    private int mMaxSizeKByte;
    private int mByteCounter;

    protected Context mContext;
    protected long mNanosOffset = 0;
    private String mPath;
    private String mSessionName;
    private String mSensorName;
    private String mExtension;
    private boolean mAppend;
    private File logFile;

    public CustomLogger(Context context, String path, String sessionName, String sensorName, String extension, boolean toAppend, long nanoOffset) {
        this(context, path, sessionName, sensorName, extension, toAppend, nanoOffset, 0);
    }

    public CustomLogger(Context context, String path, String sessionName, String sensorName, String extension, boolean toAppend, long nanoOffset, int maxSizeKByte) {
        mContext = context;
        mPath = path;
        mSessionName = sessionName;
        mSensorName = sensorName;
        mExtension = extension;
        mAppend = toAppend;
        mMaxSizeKByte = maxSizeKByte;
        mNanosOffset = nanoOffset;
    }

    private String getBaseFilename(){
        return mSessionName + "_" + mSensorName;
    }

    public void start() {
        Log.i(TAG,"start:: Starting "+getBaseFilename());
        try {
            logFile = LoggerHelper.defineLogFilename(mContext, mPath, getBaseFilename(), mExtension, mAppend, mNanosOffset);
            Log.i(TAG, "Creating file: "+logFile.getAbsolutePath());
            mOutputStream = new BufferedOutputStream(new FileOutputStream(logFile, mAppend));
        }catch (FileNotFoundException e) {
            Log.e(TAG, "::start Error starting log file "+getBaseFilename());
        }
    }

    public void resetByteCounter(){
        mByteCounter = 0;
    }

    public void log(String s) {
        try {
            mOutputStream.write(s.getBytes());
        }catch(IOException e){
            Log.e(TAG, "::log Error writing in log file "+getBaseFilename());
        }
        if (mMaxSizeKByte>0){
            mByteCounter += s.getBytes().length;
            if (mByteCounter >= mMaxSizeKByte * 1024){
                stop();
                resetByteCounter();
                start();
            }
        }
    }

    public void stop() {
        Log.i(TAG,"stop:: Closing "+getBaseFilename());
        if (mOutputStream!=null) {
            try {
                if (!DataLoggerDataSource.existLogFile(mContext, logFile.getAbsolutePath())){
                    Log.i(TAG, "Adding file to database: "+logFile.getAbsolutePath());
                    DataLoggerDataSource.insertLogFile(mContext, logFile.getAbsolutePath(), mSensorName, mSessionName);
                }
                mOutputStream.flush();
                mOutputStream.close();
                logFile = null;
            } catch (IOException e) {
                Log.e(TAG, "::stop Error closing log file " + getBaseFilename());
            }
        }
    }
}