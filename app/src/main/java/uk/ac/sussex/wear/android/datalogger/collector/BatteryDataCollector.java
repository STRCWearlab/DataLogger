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

package uk.ac.sussex.wear.android.datalogger.collector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.util.Date;

import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;

// child class for collecting battery data of the phone

public class BatteryDataCollector extends AbstractDataCollector {

    private static final String TAG = BatteryDataCollector.class.getSimpleName();

    private CustomLogger logger = null;

    private Context mContext;

    private BroadcastReceiver mBatteryInfoReceiver;

    public BatteryDataCollector(Context context, String sessionName, String sensorName, long nanosOffset, int logFileMaxSize){

        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        mContext = context;

        // Offset to match timestamps both in master and slaves devices
        mNanosOffset = nanosOffset;

        mBatteryInfoReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float temperature = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0) / 10);
                int level = -1;
                if (currentLevel >= 0 && scale > 0) {
                    level = (currentLevel * 100) / scale;
                }

                // System nanoseconds since boot, including time spent in sleep.
                long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;

                // System local time in millis
                long currentMillis = (new Date()).getTime();

                String message = String.format("%s", currentMillis) + ";"
                        + String.format("%s", nanoTime) + ";"
                        + String.format("%s", mNanosOffset) + ";";

                // Field containing the current battery level, from 0 to scale.
                message += Integer.toString(level) + ";";

                // Integer containing the current battery temperature
                message += Float.toString(temperature);

                logger.log(message);
                logger.log(System.lineSeparator());
            }
        };

    }

    @Override
    public void start() {
        mContext.registerReceiver(mBatteryInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Log.i(TAG, "start:: Starting listener for sensor: " + getSensorName());
        logger.start();
    }

    @Override
    public void stop() {
        Log.i(TAG,"stop:: Stopping listener for sensor " + getSensorName());
        mContext.unregisterReceiver(mBatteryInfoReceiver);
        logger.stop();
    }

    @Override
    public void haltAndRestartLogging() {
        logger.stop();
        logger.resetByteCounter();
        logger.start();
    }

    @Override
    public void updateNanosOffset(long nanosOffset) {
        mNanosOffset = nanosOffset;
    }

}
