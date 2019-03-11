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
import android.os.SystemClock;

import java.util.Date;

import uk.ac.sussex.wear.android.datalogger.Constants;
import uk.ac.sussex.wear.android.datalogger.R;

/**
 * Created by fjordonez on 01/10/16.
 */

// child class for logging labels

public class LabelsLogger extends CustomLogger {

    private static final String TAG = LabelsLogger.class.getSimpleName();

    private int mCurrentActivity;

    private int mCurrentBodyPosition;

    private int mCurrentLocation;

    private long mCurrentLabelStartMillis;

    public LabelsLogger(Context context, String path, String sessionName, long nanoOffset) {
        super(context, path, sessionName, Constants.SENSOR_NAME_LABELS, "txt", true, nanoOffset);
    }

    public void setup(int activity, int bodyPosition, int location){
        setCurrentActivity(activity);
        setCurrentBodyPosition(bodyPosition);
        setCurrentLocation(location);
        mCurrentLabelStartMillis = 0;
    }

    public void setCurrentActivity(int activity){
        mCurrentActivity = activity;
    }

    public int getCurrentActivity(){
        return mCurrentActivity;
    }

    public void setCurrentBodyPosition(int bodyPosition){
        mCurrentBodyPosition = bodyPosition;
    }

    public int getCurrentBodyPosition(){
        return mCurrentBodyPosition;
    }

    public void setCurrentLocation(int location){
        mCurrentLocation = location;
    }

    public int getCurrentLocation(){
        return mCurrentLocation;
    }

    private String getBaseLabelMessage(int activity, int bodyPosition, int location) {

        // System local time in millis
        long currentMillis = (new Date()).getTime();

        // Timestamp in system nanoseconds since boot, including time spent in sleep.
        long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;

        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", nanoTime) + ";"
                + String.format("%s", mNanosOffset) + ";"
                + Integer.toString(activity) + ";";
        message += ((activity >= mContext.getResources().getInteger(R.integer.num_labels_wo_pos)) || activity == 0) ? Integer.toString(bodyPosition) + ";" : "-1;";
        message += (location == R.id.ui_iolocation_radioButton_outside) ? "0;" : "1;";
        return message;
    }

    public void logState(String state){
        String message = getBaseLabelMessage(mCurrentActivity, mCurrentBodyPosition, mCurrentLocation) + state;
        log(message);
        log(System.lineSeparator());
    }

    public void logStart(){
        logState("1");
        mCurrentLabelStartMillis = System.currentTimeMillis();
    }

    public void logEnd(){
        logState("0");
    }

}