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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.File;
import java.util.Date;

import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;

public class SensorDataCollector extends AbstractDataCollector implements SensorEventListener {

    private static final String TAG = SensorDataCollector.class.getSimpleName();

    private int mSensorType;

    private CustomLogger logger = null;

    private int mSamplingPeriodUs;

    private SensorManager mSensorManager;

    /**
     *
     * @param context
     * @param sessionName
     * @param sensorType
     * @param samplingPeriodUs The rate {@link android.hardware.SensorEvent sensor events} are
     *            delivered at. This is only a hint to the system. Events may be received faster or
     *            slower than the specified rate. Usually events are received faster. The value must
     *            be one of SENSOR_DELAY_NORMAL (rate suitable for screen orientation changes),
     *            SENSOR_DELAY_UI (ate suitable for the user interface), SENSOR_DELAY_GAME (rate
     *            suitable for games), or SENSOR_DELAY_FASTEST (get sensor data as fast as possible)
     *            or, the desired delay between events in microseconds. Specifying the delay in
     *            microseconds only works from Android 2.3 (API level 9) onwards. For earlier
     *            releases, you must use one of SENSOR_DELAY_ constants.
     */
    public SensorDataCollector(Context context, String sessionName, int sensorType, String sensorName, int samplingPeriodUs, long nanosOffset, int logFileMaxSize) throws Exception {

        mSensorType = sensorType;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(mSensorType);

        // In case the sensor cannot be locked or does not exist in the device
        if (sensor == null){
            throw new Exception("Sensor type " + sensorType + " can't be locked or does not exist");
        }

        mSensorName = sensorName;

        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        mSamplingPeriodUs = samplingPeriodUs;

        // Offset to match timestamps both in master and slave devices
        mNanosOffset = nanosOffset;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Check that we received the proper sensor event
        if (event.sensor.getType() == mSensorType) {
            // System local time in millis
            long currentMillis = (new Date()).getTime();

            // Sensor timestamp defines uptime in nanos (nanoseconds since boot)
            long eventTimestamp = event.timestamp + mNanosOffset;

            //        // System nanoseconds since boot, including time spent in sleep.
            //        long nanoTime = SystemClock.elapsedRealtimeNanos();

            //        // Conversion of sensor timestamp in terms of the system local time
            //        long timeInMillis = currentMillis + (eventTimestamp - nanoTime) / 1000000L;

            String message = String.format("%s", currentMillis) + ";"
                    + String.format("%s", eventTimestamp) + ";"
                    + String.format("%s", mNanosOffset);

            for (float v : event.values) {
                message += String.format(";%s", v);
            }

            logger.log(message);
            logger.log(System.lineSeparator());
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void start() {
        Log.i(TAG, "start:: Starting listener for sensor: "+getSensorName());
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(mSensorType), mSamplingPeriodUs);
        logger.start();
    }

    @Override
    public void stop() {
        Log.i(TAG,"stop:: Stopping listener for sensor "+getSensorName());

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
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
