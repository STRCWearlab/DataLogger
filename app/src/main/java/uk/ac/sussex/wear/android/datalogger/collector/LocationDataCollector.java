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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.Date;

import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;


public class LocationDataCollector extends AbstractDataCollector implements LocationListener {

    private static final String TAG = LocationDataCollector.class.getSimpleName();

    private CustomLogger logger = null;

    private LocationManager mLocationManager = null;


    public LocationDataCollector(Context context, String sessionName, String sensorName, long nanosOffset, int logFileMaxSize){

        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        // Offset to match timestamps both in master and slaves devices
        mNanosOffset = nanosOffset;

    }


    @Override
    public void onLocationChanged(Location location) {

        if (location == null) return;

        // Return the time of this fix, in elapsed real-time since system boot.
        long locationNanoTime = location.getElapsedRealtimeNanos() + mNanosOffset;

        // System local time in millis
        long currentMillis = (new Date()).getTime();

        // Get the estimated accuracy of this location, in meters.
        float accuracy = location.getAccuracy();

        // Get the latitude, in degrees.
        double latitude = location.getLatitude();

        // Get the longitude, in degrees.
        double longitude = location.getLongitude();

        // Get the altitude if available, in meters above the WGS 84 reference ellipsoid.
        double altitude = location.getAltitude();

        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", locationNanoTime) + ";"
                + String.format("%s", mNanosOffset) + ";"
                + accuracy + ";"
                + latitude + ";"
                + longitude + ";"
                + altitude;

//        // Get the latitude, in degrees.
//        String latitudeDegrees = Location.convert(latitude, Location.FORMAT_DEGREES);
//        // Get the longitude, in degrees.
//        String longitudeDegrees = Location.convert(longitude, Location.FORMAT_DEGREES);

        logger.log(message);
        logger.log(System.lineSeparator());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void start() {
        Log.i(TAG, "start:: Starting listener for sensor: " + getSensorName());
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        logger.start();
    }

    @Override
    public void stop() {
        Log.i(TAG,"stop:: Stopping listener for sensor " + getSensorName());
        logger.stop();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
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
