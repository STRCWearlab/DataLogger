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

// child class for collecting orientation data (rotation sensor)

public class OrientationDataCollector extends AbstractDataCollector implements SensorEventListener {

    private static final String TAG = OrientationDataCollector.class.getSimpleName();

    private CustomLogger logger = null;

    private int mSamplingPeriodUs;

    private SensorManager mSensorManager = null;

    public OrientationDataCollector(Context context, String sessionName, String sensorName, int samplingPeriodUs, long nanosOffset, int logFileMaxSize) throws Exception {

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        mNanosOffset = nanosOffset;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        mSamplingPeriodUs = samplingPeriodUs;

    }

    private void logOrientation(SensorEvent event){
        /*// Rotation matrix based on current readings from accelerometer and magnetometer.
        final float[] rotationMatrix = new float[9];
        // Convert the rotation-vector to a 4x4 matrix.
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        final float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        // Optionally convert the result from radians to degrees
        orientationValues[0] = (float) Math.toDegrees(orientationValues[0]);
        orientationValues[1] = (float) Math.toDegrees(orientationValues[1]);
        orientationValues[2] = (float) Math.toDegrees(orientationValues[2]);*/

        final float[] quaternionValue = new float[4];
        SensorManager.getQuaternionFromVector(quaternionValue, event.values);

        // System local time in millis
        long currentMillis = (new Date()).getTime();

        // Sensor timestamp defines uptime in nanos (nanoseconds since boot)
        long eventTimestamp = event.timestamp + mNanosOffset;

        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", eventTimestamp) + ";"
                + String.format("%s", mNanosOffset);

        for (float v : quaternionValue) {
            message += String.format(";%.7f", v);
        }

        logger.log(message);
        logger.log(System.lineSeparator());
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        // Check that we received the proper sensor event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            logOrientation(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void start() {
        Log.i(TAG, "start:: Starting listener for sensor: "+getSensorName());
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), mSamplingPeriodUs);
        logger.start();
    }

    @Override
    public void stop() {
        Log.i(TAG,"stop:: Stopping listener for sensor "+getSensorName());
        mSensorManager.unregisterListener(this);
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