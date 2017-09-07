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

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import uk.ac.sussex.wear.android.datalogger.Constants;

public class HARecognizerApiLogger extends CustomLogger {

    private static final String TAG = HARecognizerApiLogger.class.getSimpleName();

    public HARecognizerApiLogger(Context context, String path, String sessionName, long nanosOffset, int logFileMaxSize) {
        super(context, path, sessionName, Constants.SENSOR_NAME_API_HAR, "txt", false, nanosOffset, logFileMaxSize);
    }

    public void logDetectedActivities (ArrayList<DetectedActivity> detectedActivities){
        HashMap<Integer, Integer> detectedActivitiesMap = new HashMap<>();
        for (DetectedActivity activity : detectedActivities) {
            detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
        }

        // Timestamp in system nanoseconds since boot, including time spent in sleep.
        long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;

        // System local time in millis
        long currentMillis = (new Date()).getTime();

        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", nanoTime) + ";"
                + String.format("%s", mNanosOffset);
        for (int i = 0; i < Constants.API_ACTIVITY_RECOGNIZER_LIST.length; i++) {
            message += ";" + Integer.toString(
                    detectedActivitiesMap.containsKey(Constants.API_ACTIVITY_RECOGNIZER_LIST[i]) ?
                            detectedActivitiesMap.get(Constants.API_ACTIVITY_RECOGNIZER_LIST[i]) : 0);
        }

        log(message);
        log(System.lineSeparator());
    }

    public void updateNanosOffset(long nanosOffset) {
        mNanosOffset = nanosOffset;
    }
}
