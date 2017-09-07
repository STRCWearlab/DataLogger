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
import android.util.Log;

import java.util.ArrayList;

import uk.ac.sussex.wear.android.datalogger.Constants;
import uk.ac.sussex.wear.android.datalogger.SharedPreferencesHelper;


public class DataCollectors {

    private static final String TAG = DataCollectors.class.getSimpleName();

    // Collectors objects array. One collector per sensor.
    private ArrayList<AbstractDataCollector> mCollectors = null;

    private static DataCollectors instance = null;

    private DataCollectors() {
        // Exists only to defeat instantiation.
    }

    public static DataCollectors getInstance() {
        Log.i(TAG, "::getInstance Creating singleton instance from DataCollectors");
        if(instance == null) {
            instance = new DataCollectors();
        }
        return instance;
    }

    public void startDataCollectors(Context context, String sessionName, long nanosOffset) {

        Log.i(TAG, "::startDataCollectors Starting data collectors");

        // Initialization of collectors objects array
        mCollectors = new ArrayList<>();
        
        // Measures the acceleration force in m/s2 that is applied to a device on all three physical axes (x, y, and z), including the force of gravity.
        // SensorEvent.values[0]	Acceleration force along the x axis (including gravity).
        // SensorEvent.values[1]	Acceleration force along the y axis (including gravity).
        // SensorEvent.values[2]	Acceleration force along the z axis (including gravity).
        if (SharedPreferencesHelper.isEnabledAccelerometer(context)) {
            try{
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_ACCELEROMETER,
                        Constants.SENSOR_NAME_ACC,
                        SharedPreferencesHelper.getSamplingPeriodAccelerometer(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e){
                Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_ACC + " data collector: " + e.getMessage());
            }
        }

        // Measures a device's rate of rotation in rad/s around each of the three physical axes (x, y, and z).
        // SensorEvent.values[0]	Rate of rotation around the x axis.
        // SensorEvent.values[1]	Rate of rotation around the y axis.
        // SensorEvent.values[2]	Rate of rotation around the z axis.
        if (SharedPreferencesHelper.isEnabledGyroscope(context)){
            try{
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_GYROSCOPE,
                        Constants.SENSOR_NAME_GYR,
                        SharedPreferencesHelper.getSamplingPeriodGyroscope(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
               } catch (Exception e){
                Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_GYR + " data collector: " + e.getMessage());
            }
        }

        // Measures the ambient geomagnetic field for all three physical axes (x, y, z) in μT
        // SensorEvent.values[0]	Geomagnetic field strength along the x axis.
        // SensorEvent.values[1]	Geomagnetic field strength along the y axis.
        // SensorEvent.values[2]	Geomagnetic field strength along the z axis.
        if (SharedPreferencesHelper.isEnabledMagnetometer(context)){
            try{
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_MAGNETIC_FIELD,
                        Constants.SENSOR_NAME_MAG,
                        SharedPreferencesHelper.getSamplingPeriodMagnetometer(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e){
                Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_MAG + " data collector: " + e.getMessage());
            }
        }

        // Records audio
        if (SharedPreferencesHelper.isEnabledMicrophone(context)){
            try{
                mCollectors.add(new AudioDataCollector(context, sessionName,
                        Constants.SENSOR_NAME_MIC,
                        SharedPreferencesHelper.getSamplingPeriodMicrophone(context),
                        nanosOffset));
            } catch (Exception e){
                Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_MIC + " data collector: " + e.getMessage());
            }
        }

        // Measures the battery level as a percentage
        if (SharedPreferencesHelper.isEnabledBattery(context)){
            mCollectors.add(new BatteryDataCollector(context, sessionName,
                    Constants.SENSOR_NAME_BAT,
                    nanosOffset,
                    SharedPreferencesHelper.getLogFilesMaxsize(context)));
        }

        // Measures the status of the cellular network in number of cells and signal strength
        if (SharedPreferencesHelper.isEnabledCellsInfo(context)){
            try{
                mCollectors.add(new CellsInfoDataCollector(context, sessionName,
                        Constants.SENSOR_NAME_CELL,
                        SharedPreferencesHelper.getSamplingPeriodCellsInfo(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e){
                Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_CELL + " data collector: " + e.getMessage());
            }
        }

        // Measures the status of the cellular network in number of cells and signal strength (deprecated version)
        if (SharedPreferencesHelper.isEnabledDeprCellsInfo(context)){
            try{
                mCollectors.add(new Depr_CellsInfoDataCollector(context, sessionName,
                        Constants.SENSOR_NAME_DEPR_CELL,
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e){
                Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_DEPR_CELL + " data collector: " + e.getMessage());
            }
        }

        // Measures geographic location in latitude, longitude and altitude
        if (SharedPreferencesHelper.isEnabledLocation(context)){
            mCollectors.add(new LocationDataCollector(context, sessionName,
                    Constants.SENSOR_NAME_LOC,
                    nanosOffset,
                    SharedPreferencesHelper.getLogFilesMaxsize(context)));
        }

        // Statores an array of GpsSatellite objects, which represent the current state of the GPS engine.
        if (SharedPreferencesHelper.isEnabledSatellite(context)){
            mCollectors.add(new SatelliteDataCollector(context, sessionName,
                    Constants.SENSOR_NAME_SAT,
                    nanosOffset,
                    SharedPreferencesHelper.getLogFilesMaxsize(context)));
        }

        // Measures the ambient room temperature in degrees Celsius (°C).
        if (SharedPreferencesHelper.isEnabledTemperature(context)) {
            try{
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_AMBIENT_TEMPERATURE,
                        Constants.SENSOR_NAME_TEMP,
                        SharedPreferencesHelper.getSamplingPeriodTemperature(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e){
                Log.e(TAG, "Error creating data collector: " + e.getMessage());
            }
        }

        // Measures the ambient light level (illumination) in lx.
        if (SharedPreferencesHelper.isEnabledLight(context)) {
            try{
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_LIGHT,
                        Constants.SENSOR_NAME_LT,
                        SharedPreferencesHelper.getSamplingPeriodLight(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e){
                Log.e(TAG, "Error creating data collector: " + e.getMessage());
            }
        }

        // Measures the ambient air pressure in hPa or mbar.
        if (SharedPreferencesHelper.isEnabledPressure(context)) {
            try{
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_PRESSURE,
                        Constants.SENSOR_NAME_PRES,
                        SharedPreferencesHelper.getSamplingPeriodPressure(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e){
                Log.e(TAG, "Error creating data collector: " + e.getMessage());
            }
        }

        // Measures the relative ambient humidity in percent (%).
        if (SharedPreferencesHelper.isEnabledHumidity(context)) {
            try{
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_RELATIVE_HUMIDITY,
                        Constants.SENSOR_NAME_HUM,
                        SharedPreferencesHelper.getSamplingPeriodHumidity(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e){
                Log.e(TAG, "Error creating data collector: " + e.getMessage());
            }
        }

        // Measures the status of the WiFi networks
        if (SharedPreferencesHelper.isEnabledWiFi(context)){
            mCollectors.add(new WiFiDataCollector(context, sessionName,
                    Constants.SENSOR_NAME_WIFI,
                    SharedPreferencesHelper.getSamplingPeriodWiFiInfo(context),
                    nanosOffset,
                    SharedPreferencesHelper.getLogFilesMaxsize(context)));
        }

        // Measures position of the device relative to the earth's frame of reference (specifically,
        // the magnetic north pole).
        // Sensor type TYPE_ORIENTATION was deprecated in Android 2.2 (API level 8), and this sensor
        // type was deprecated in Android 4.4W (API level 20).
        // OrientationValues[0]	Azimuth (degrees of rotation about the -z axis).
        // OrientationValues[1]	Pitch (degrees of rotation about the -x axis).
        // OrientationValues[2]	Roll (degrees of rotation about the y axis).
        if (SharedPreferencesHelper.isEnabledOrientation(context)) {
            try {
                mCollectors.add(new OrientationDataCollector(context, sessionName,
                        Constants.SENSOR_NAME_ORIEN,
                        SharedPreferencesHelper.getSamplingPeriodOrientation(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e) {
                Log.e(TAG, "Error creating data collector: " + e.getMessage());
            }
        }


        // Linear acceleration TYPE_LINEAR_ACCELERATION
        // Measures the acceleration force in m/s2 that is applied to a device on all three physical axes (x, y, and z), excluding the force of gravity.
        // SensorEvent.values[0] Acceleration force along the x axis (excluding gravity). m/s2
        // SensorEvent.values[1] Acceleration force along the y axis (excluding gravity).
        // SensorEvent.values[2] Acceleration force along the z axis (excluding gravity).
        if (SharedPreferencesHelper.isEnabledLinearAcceleration(context)) {
            try {
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_LINEAR_ACCELERATION,
                        Constants.SENSOR_NAME_LIN_ACC,
                        SharedPreferencesHelper.getSamplingPeriodMagnetometer(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e) {
                Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_LIN_ACC + " data collector: " + e.getMessage());
            }
        }


        // Gravity TYPE_GRAVITY
        // Measures the force of gravity in m/s2 that is applied to a device on all three physical axes (x, y, z).
        // SensorEvent.values[0] Force of gravity along the x axis.	m/s2
        // SensorEvent.values[1] Force of gravity along the y axis.
        // SensorEvent.values[2] Force of gravity along the z axis.
        if (SharedPreferencesHelper.isEnabledGravity(context)) {
            try {
                mCollectors.add(new SensorDataCollector(context, sessionName,
                        Sensor.TYPE_GRAVITY,
                        Constants.SENSOR_NAME_GRA,
                        SharedPreferencesHelper.getSamplingPeriodMagnetometer(context),
                        nanosOffset,
                        SharedPreferencesHelper.getLogFilesMaxsize(context)));
            } catch (Exception e) {
                Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_GRA + " data collector: " + e.getMessage());
            }
        }



        // All data collectors are started
        for (AbstractDataCollector collector : mCollectors) {
            Log.i(TAG, "Calling collector " + collector.getSensorName());
            collector.start();
        }

    }

    public void stopDataCollectors() {
        // Data collectors are closed
        Log.i(TAG, "::stopDataCollectors Stopping "+Integer.toString(mCollectors.size())+" collectors");
        for (AbstractDataCollector collector : mCollectors) {
            collector.stop();
        }
    }

    public void haltAndRestartLogging(){
        if (mCollectors != null) {
            // Data logging is restarted for evey collector
            for (AbstractDataCollector collector : mCollectors) {
                Log.i(TAG, "::haltAndRestartLogging Restarting logging in collector " + collector.getSensorName());
                collector.haltAndRestartLogging();
            }
        }
    }

    public void updateNanosOffset(long nanosOffset) {
        for (AbstractDataCollector collector : mCollectors) {
            collector.updateNanosOffset(nanosOffset);
        }
    }
}
