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
import android.util.Log;

import java.io.File;

import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;

public class DummyDataCollector extends AbstractDataCollector {

    private static final String TAG = "DummyDataCollector";

    private CustomLogger logger = null;

    public DummyDataCollector(Context context, String sessionName, String sensorName, int samplingPeriodUs, long nanosOffset, int logFileMaxSize){
        Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_constructor");
        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;
        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, nanosOffset, logFileMaxSize);
        Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_aftertxt");
        logDummyInfo();
    }

    private void logDummyInfo(){
        Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_beforelog");
        String message = "Hello";
        logger.log("hello");
        Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_afterlog");
    }

    @Override
    public void start() {
        Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_start");
        logger.start();
    }

    @Override
    public void stop() {
        Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_stop");
        logger.stop();
    }

    @Override
    public void haltAndRestartLogging() {
        Log.e(TAG, "Error creating " + "DummyDataCollector" + " test_halt");
        logger.stop();
        logger.resetByteCounter();
        logger.start();
    }

    @Override
    public void updateNanosOffset(long nanosOffset) {

    }
}
