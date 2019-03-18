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

package uk.ac.sussex.wear.android.datalogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import uk.ac.sussex.wear.android.datalogger.data.DataCollectionSession;

public final class SharedPreferencesHelper {

    private static final String TAG = SharedPreferencesHelper.class.getSimpleName();

    /**
     * Retrieves the default SharedPreference object used to store or read values in this app.
     */
    public static SharedPreferences getSharedPrefsInstance(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**********************************************************************************************/
    /* Definition of the getter methods for preferences
    /**********************************************************************************************/

    /**
     * Retrieves the String from SharedPreferences with an specific error message for the ui.
     */
    public static String getUiErrorMessage(Context context, int code){
        return getUiErrorsMessagesList(context)[code];
    }

    /**
     * Retrieves the String list from SharedPreferences with the different errors messages for the ui.
     */
    public static String[] getUiErrorsMessagesList(Context context){
        return context.getResources().getStringArray(R.array.ui_errors_messages_snackbar_list);
    }

    public static int getLogFilesMaxsize(Context context){
        String value = getSharedPrefsInstance(context).
                getString(context.getResources().getString(R.string.pref_general_key_log_files_maxSize), "1000");
        if (!value.equals("")){
            return Integer.parseInt(value);
        } else {
            return 0;
        }
    }

    public static int getLogMaxTime(Context context){
        String value = getSharedPrefsInstance(context).
                getString(context.getResources().getString(R.string.pref_general_key_log_maxTime),
                        context.getResources().getString(R.string.pref_general_default_log_maxTime));
        if (!value.equals("")){
            return Integer.parseInt(value);
        } else {
            return 0;
        }
    }

    public static boolean isEnabledHARAPI(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_general_key_enabled_HAR_API), true);
    }

    public static int getDetectionIntervalHARAPI(Context context){
        String value = getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_general_key_HAR_API_detection_interval), "0");
        if (!value.equals("")){
            return Integer.parseInt(value);
        } else {
            return 0;
        }
    }

    //SharedPreferences Sensors - Accelerometer
    public static boolean isEnabledAccelerometer(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_accelerometer), true);
    }

    public static int getSamplingPeriodAccelerometer(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_accelerometer), "0"));
    }

    public static boolean toSyncAccelerometer(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_accelerometer), true);
    }

    //SharedPreferences Sensors - Gyroscope
    public static boolean isEnabledGyroscope(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_gyroscope), true);
    }

    public static int getSamplingPeriodGyroscope(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_gyroscope), "0"));
    }

    public static boolean toSyncGyroscope(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_gyroscope), true);
    }

    //SharedPreferences Sensors - Magnetometer
    public static boolean isEnabledMagnetometer(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_magnetometer), true);
    }

    public static int getSamplingPeriodMagnetometer(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_magnetometer), "0"));
    }

    public static boolean toSyncMagnetometer(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_magnetometer), true);
    }

    //SharedPreferences Sensors - Microphone
    public static boolean isEnabledMicrophone(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_microphone), true);
    }

    public static int getSamplingPeriodMicrophone(Context context){
        String value = getSharedPrefsInstance(context).
                getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_microphone), "0");
        if (!value.equals("")){
            return Integer.parseInt(value);
        } else {
            return 0;
        }
    }

    public static boolean toSyncMicrophone(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_microphone), true);
    }

    //SharedPreferences Sensors - Battery
    public static boolean isEnabledBattery(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_battery), true);
    }

    public static boolean toSyncBattery(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_battery), true);
    }

    //SharedPreferences Sensors - Cells network
    public static boolean isEnabledCellsInfo(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_cells), true);
    }

    public static int getSamplingPeriodCellsInfo(Context context){
        String value = getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_cells), "1");
        if (!value.equals("")){
            return Integer.parseInt(value);
        } else {
            return 0;
        }
    }

    public static boolean toSyncCellsInfo(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_cells), true);
    }

    //SharedPreferences Sensors - Cells network (deprecated)
    public static boolean isEnabledDeprCellsInfo(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_depr_cells), true);
    }

    public static boolean toSyncDeprCellsInfo(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_depr_cells), true);
    }

    //SharedPreferences Sensors - Location
    public static boolean isEnabledLocation(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_location), true);
    }

    public static boolean toSyncLocation(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_location), true);
    }

    //SharedPreferences Sensors - Satellites
    public static boolean isEnabledSatellite(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_satellite), true);
    }

    public static boolean toSyncSatellite(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_satellite), true);
    }

    //SharedPreferences Sensors - Temperature
    public static boolean isEnabledTemperature(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_temperature), true);
    }

    public static int getSamplingPeriodTemperature(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_temperature), "0"));
    }

    public static boolean toSyncTemperature(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_temperature), true);
    }

    //SharedPreferences Sensors - Ambient light
    public static boolean isEnabledLight(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_light), true);
    }

    public static int getSamplingPeriodLight(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_light), "0"));
    }

    public static boolean toSyncLight(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_light), true);
    }

    //SharedPreferences Sensors - Pressure
    public static boolean isEnabledPressure(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_pressure), true);
    }

    public static int getSamplingPeriodPressure(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_pressure), "0"));
    }

    public static boolean toSyncPressure(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_pressure), true);
    }

    //SharedPreferences Sensors - Humidity
    public static boolean isEnabledHumidity(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_humidity), true);
    }

    public static int getSamplingPeriodHumidity(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_humidity), "0"));
    }

    public static boolean toSyncHumidity(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_humidity), true);
    }

    //SharedPreferences Sensors - WiFi
    public static boolean isEnabledWiFi(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_WiFi), true);
    }

    public static int getSamplingPeriodWiFiInfo(Context context){
        String value = getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_WiFi), "1");
        if (!value.equals("")){
            return Integer.parseInt(value);
        } else {
            return 0;
        }
    }

    public static boolean toSyncWiFi(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_WiFi), true);
    }

    //SharedPreferences Sensors - Orientation
    public static boolean isEnabledOrientation(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_orientation), true);
    }

    public static int getSamplingPeriodOrientation(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_orientation), "0"));
    }

    public static boolean toSyncOrientation(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_orientation), true);
    }

    //SharedPreferences Sensors - Linear Acceleration
    public static boolean isEnabledLinearAcceleration(Context context) {
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_lin_acceleration), true);
    }

    public static int getSamplingPeriodLinearAcceleration(Context context) {
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_lin_acceleration), "0"));
    }

    public static boolean toSyncLinearAcceleration(Context context) {
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_lin_acceleration), true);
    }

    //SharedPreferences Sensors - Gravity
    public static boolean isEnabledGravity(Context context) {
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_enabled_gravity), true);
    }

    public static int getSamplingPeriodGravity(Context context) {
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sensors_key_samplingPeriod_gravity), "0"));
    }

    public static boolean toSyncGravity(Context context) {
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sensors_key_sync_gravity), true);
    }

    /**
     * Retrieves the DataLoggingSession object from SharedPreferences that contains the data of the collecting session.
     */
    public static DataCollectionSession getDataCollectionSessionObject(Context context){
        String value = getSharedPrefsInstance(context).getString(Constants.DATA_COLLECTION_SESSION_OBJECT_KEY, "");
        if (value.equals("")) {
            return null;
        } else {
            return new Gson().fromJson(value, DataCollectionSession.class);
        }
    }

    /**
     * Checks using SharedPreferences whether we are collecting data.
     */
    public static boolean getDataCollectionState(Context context){
        return (getDataCollectionSessionObject(context) != null);
    }

    /**
     * Retrieves the int from SharedPreferences that control the current annotated activity label.
     */
    public static int getAnnotatedActivityLabel(Context context){
        return getSharedPrefsInstance(context).
                getInt(Constants.LABEL_ANNOTATION_ACTIVITY_KEY, -1);
    }

    /**
     * Retrieves the int from SharedPreferences that control the current annotated body position label.
     */
    public static int getAnnotatedBodyPositionLabel(Context context){
        return getSharedPrefsInstance(context).
                getInt(Constants.LABEL_ANNOTATION_BODYPOSITION_KEY, -1);
    }

    /**
     * Retrieves the int from SharedPreferences that control the current annotated location label.
     */
    public static int getAnnotatedLocationLabel(Context context){
        return getSharedPrefsInstance(context).
                getInt(Constants.LABEL_ANNOTATION_LOCATION_KEY, R.id.ui_iolocation_radioButton_outside);
    }

    /**
     * Checks using SharedPreferences whether we are annotating labels.
     */
    public static boolean getLabelsAnnotationState(Context context){
        return (getAnnotatedActivityLabel(context) != -1);
    }

    /**
     * Retrieves the int from SharedPreferences that control the activity label selected in the UI.
     */
    public static int getSelectedActivityLabel(Context context){
        return getSharedPrefsInstance(context).
                getInt(Constants.LABEL_SELECTED_UI_ACTIVITY_KEY, -1);
    }

    /**
     * Retrieves the int from SharedPreferences that control the body position label selected in the UI.
     */
    public static int getSelectedBodyPositionLabel(Context context){
        return getSharedPrefsInstance(context).
                getInt(Constants.LABEL_SELECTED_UI_BODYPOSITION_KEY, -1);
    }

    /**
     * Retrieves the int from SharedPreferences that control the location label selected in the UI.
     */
    public static int getSelectedLocationLabel(Context context){
        return getSharedPrefsInstance(context).
                getInt(Constants.LABEL_SELECTED_UI_LOCATION_KEY, R.id.ui_iolocation_radioButton_outside);
    }

    /**
     * Retrieves the long from SharedPreferences that stores the starting millis of the last annotation event.
     */
    public static long getLabelsAnnotationStartingTime(Context context){
        return getSharedPrefsInstance(context)
                .getLong(Constants.ANNOTATING_EVENT_START_KEY, 0);
    }

    /**
     * Retrieves the int from SharedPreferences that control the location of the device.
     */
    public static int getDeviceLocationValue(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_general_key_device_location), "3"));
    }

    /**
     * Retrieves the String from SharedPreferences that describes the location of the device.
     */
    public static String getDeviceLocation(Context context){
        String[] deviceLocations = context.getResources().getStringArray(R.array.pref_general_list_device_location);
        return deviceLocations[getDeviceLocationValue(context)];
    }

    /**
     * Retrieves the String from SharedPreferences that contains the user name.
     */
    public static String getUserName(Context context){
        return getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_general_key_user_name), "User");
    }

    /**
     * Retrieves the String from SharedPreferences that contains the URL of the server to upload files.
     */
    public static String getUploadServerURL(Context context){
        return getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sync_key_server_address),
                        context.getResources().getString(R.string.pref_sync_default_server_address));
    }

    /**
     * Retrieves the String list from SharedPreferences with the different states that describe
     * the bluetooth connection.
     */
    public static String[] getBluetoothStateList(Context context){
        return context.getResources().getStringArray(R.array.ui_bluetooth_list_state);
    }

    /**
     * Retrieves the int from SharedPreferences that controls the bluetooth status.
     */
    public static int getBluetoothStatus(Context context){
        return getSharedPrefsInstance(context)
                .getInt(Constants.BLUETOOTH_STATE_KEY, 0);
    }

    /**
     * Retrieves the String from SharedPreferences that contains the address of the bluetooth node.
     */
    public static String getBluetoothAddress(Context context){
        return getSharedPrefsInstance(context)
                .getString(Constants.BLUETOOTH_ADDRESS_KEY, "");
    }

    /**
     * Retrieves the int from SharedPreferences that defines the number of slaves connected
     */
    public static int getSlavesConnected(Context context){
        return getSharedPrefsInstance(context)
                .getInt(Constants.BLUETOOTH_SLAVES_CONNECTED_KEY, 0);
    }

    /**
     * Retrieves the int from SharedPreferences that controls the slaves timeout.
     */
    /*public static int getSlaveConnectionTimeout(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sync_key_slave_timeout),
                        context.getResources().getString(R.string.pref_sync_default_slave_timeout)));
    }*/


    /*public static boolean isEnabledKeepalive(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(context.getResources().getString(R.string.pref_sync_key_enabled_keepalive), true);
    }*/

    /**
     * Retrieves the int from SharedPreferences that controls the master keepalive interval
     */
    /*public static int getMasterKeepaliveInterval(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sync_key_master_keepalive),
                        context.getResources().getString(R.string.pref_sync_default_master_keepalive)));
    }*/

    /**
     * Retrieves the int from SharedPreferences that controls the slaves keepalive interval
     */
    /*public static int getSlaveKeepaliveInterval(Context context){
        return Integer.parseInt(getSharedPrefsInstance(context)
                .getString(context.getResources().getString(R.string.pref_sync_key_slave_keepalive),
                        context.getResources().getString(R.string.pref_sync_default_slave_keepalive)));
    }*/

    /**
     * Retrieves the String from SharedPreferences that contains the date of the last completed server upload.
     */
    public static String getDateLastUpload(Context context){
        return getSharedPrefsInstance(context)
                .getString(Constants.UPLOAD_DATE_LAST_COMPLETED_KEY,
                        context.getResources().getString(R.string.ui_connectivity_second_message_default_text));
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether the files upload service has started.
     */
    public static boolean getUploadServiceState(Context context){
        return getSharedPrefsInstance(context)
                .getBoolean(Constants.IS_UPLOAD_SERVICE_RUNNING, false);
    }

    /**
     * Retrieves the int from SharedPreferences that stores the annotation time for each label.
     */
    public static int getLabelAnnotationTime(Context context, int activityLabel){
        return getSharedPrefsInstance(context)
                .getInt(Constants.ANNOTATION_TIME_PER_LABEL_KEY + Integer.toString(activityLabel), 0);
    }

    /**********************************************************************************************/
    /* Definition of the setter methods for preferences
    /**********************************************************************************************/

    /**
     * Modifies the int from SharedPreferences that stores the annotation time for each label.
     */
    public static void setLabelAnnotationTime(Context context, int activityLabel, int seconds){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.ANNOTATION_TIME_PER_LABEL_KEY + Integer.toString(activityLabel), seconds)
                .commit();
    }

    /**
     * Increases the int from SharedPreferences that stores the annotation time for each label.
     */
    public static void addLabelAnnotationTime(Context context, int activityLabel, int seconds){
        int currentSecs = getLabelAnnotationTime(context, activityLabel);
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.ANNOTATION_TIME_PER_LABEL_KEY + Integer.toString(activityLabel), currentSecs + seconds)
                .commit();
    }

    /**
     *  Modifies the boolean from SharedPreferences that tracks whether the files upload service has started.
     * @param isRunning
     */
    public static void setUploadServiceState(Context context, boolean isRunning){
        getSharedPrefsInstance(context).edit()
                .putBoolean(Constants.IS_UPLOAD_SERVICE_RUNNING, isRunning)
                .commit();
    }

    /**
     * Modifies the String from SharedPreferences that contains the date of the last completed server upload.
     */
    public static void setDateLastUpload(Context context, String date){
        getSharedPrefsInstance(context).edit().
                putString(Constants.UPLOAD_DATE_LAST_COMPLETED_KEY, date)
                .commit();
    }

    /**
     * Modifies the long from SharedPreferences that stores the starting millis of the last annotation event.
     */
    public static void setLabelsAnnotationStartingTime(Context context, long millis){
        getSharedPrefsInstance(context).edit()
                .putLong(Constants.ANNOTATING_EVENT_START_KEY, millis)
                .commit();
    }

    /**
     * Modifies the DataLoggingSession object from SharedPreferences that contains the data of the collecting session.
     */
    public static void setDataCollectionSessionObject(Context context, DataCollectionSession dataCollectionSession){
        getSharedPrefsInstance(context).edit()
                .putString(Constants.DATA_COLLECTION_SESSION_OBJECT_KEY,
                        (dataCollectionSession == null) ? "" : new Gson().toJson(dataCollectionSession))
                .commit();
    }

    /**
     * Modifies the int from SharedPreferences that control the current annotated activity label.
     */
    public static void setAnnotatedActivityLabel(Context context, int activity){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.LABEL_ANNOTATION_ACTIVITY_KEY, activity)
                .commit();
    }

    /**
     * Modifies the int from SharedPreferences that control the current annotated body position label.
     */
    public static void setAnnotatedBodyPositionLabel(Context context, int bodyPosition){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.LABEL_ANNOTATION_BODYPOSITION_KEY, bodyPosition)
                .commit();
    }

    /**
     * Modifies the int from SharedPreferences that control the current annotated location label.
     */
    public static void setAnnotatedLocationLabel(Context context, int location){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.LABEL_ANNOTATION_LOCATION_KEY, location)
                .commit();
    }

    /**
     * Modifies the int from SharedPreferences that control the activity label selected in the UI.
     */
    public static void setSelectedActivityLabel(Context context, int activity){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.LABEL_SELECTED_UI_ACTIVITY_KEY, activity)
                .commit();
    }

    /**
     * Modifies the int from SharedPreferences that control the body position label selected in the UI.
     */
    public static void setSelectedBodyPositionLabel(Context context, int bodyPosition){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.LABEL_SELECTED_UI_BODYPOSITION_KEY, bodyPosition)
                .commit();
    }

    /**
     * Modifies the int from SharedPreferences that control the location label selected in the UI.
     */
    public static void setSelectedLocationLabel(Context context, int location){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.LABEL_SELECTED_UI_LOCATION_KEY, location)
                .commit();
    }

    /**
     * Modifies the int from SharedPreferences that control the bluetooth status.
     */
    public static void setBluetoothStatus(Context context, int status){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.BLUETOOTH_STATE_KEY, status)
                .commit();
    }

    /**
     * Modifies the String from SharedPreferences that contains the address of the bluetooth node.
     */
    public static void setBluetoothAddress(Context context, String address){
        getSharedPrefsInstance(context).edit().
                putString(Constants.BLUETOOTH_ADDRESS_KEY, address)
                .commit();
    }

    /**
     * Modifies the int from SharedPreferences that defines the number of slaves connected
     */
    public static void setSlavesConnected(Context context, int value){
        getSharedPrefsInstance(context).edit()
                .putInt(Constants.BLUETOOTH_SLAVES_CONNECTED_KEY, value)
                .commit();
    }

}
