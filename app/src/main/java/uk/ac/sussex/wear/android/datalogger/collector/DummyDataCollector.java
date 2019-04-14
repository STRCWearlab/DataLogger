/*
 * Copyright (c) 2019. Mathias Ciliberto, Francisco Javier Ordoñez Morales,
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

package uk.ac.sussex.wear.android.datalogger.collector;

import android.content.Context;
import android.os.Handler;
import java.io.File;

import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;

// child class for testing the txt file write function
// class should only write "Hello" into txt file

public class DummyDataCollector extends AbstractDataCollector {

    private static final String TAG = "DummyDataCollector";

    private CustomLogger logger = null;

    // Timer to manage specific sampling rates
    private Handler mTimerHandler = null;
    private Runnable mTimerRunnable = null;

    public DummyDataCollector(Context context, String sessionName, String sensorName, int samplingPeriodUs, long nanosOffset, int logFileMaxSize){
        //Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_constructor");
        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;
        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, nanosOffset, logFileMaxSize);
        //Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_aftertxt");
        mTimerHandler = new Handler();
        mTimerRunnable = new Runnable() {
            @Override
            public void run() {
                //Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_WIFI + " test");
                logDummyInfo();
                int millis = 1000;
                mTimerHandler.postDelayed(this, millis);
            }
        };
    }

    private void logDummyInfo(){
        //Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_beforelog");
        String message = "Hello";
        logger.log(message);
        logger.log(System.lineSeparator());
        //Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_afterlog");
    }

    @Override
    public void start() {
        //Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_start");
     //   logDummyInfo();
        mTimerHandler.postDelayed(mTimerRunnable, 0);
        logger.start();
    }

    @Override
    public void stop() {
        //Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_stop");
        mTimerHandler.removeCallbacks(mTimerRunnable);
        logger.stop();
    }

    @Override
    public void haltAndRestartLogging() {
        //Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_halt");
        logger.stop();
        logger.resetByteCounter();
        logger.start();
    }

    @Override
    public void updateNanosOffset(long nanosOffset) {
        mNanosOffset = nanosOffset;
    }
}
