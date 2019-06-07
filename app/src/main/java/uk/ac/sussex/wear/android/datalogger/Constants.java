/*
 * Copyright (c) 2017. Mathias Ciliberto, Francisco Javier Ordoñez Morales,
 * Hristijan Gjoreski, Daniel Roggen, Clara Wurm
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

import com.google.android.gms.location.DetectedActivity;

/**
 * Constants used by multiple classes in this app
 */
public final class Constants {

    private Constants() {
    }

    public static final String PACKAGE_NAME = "uk.ac.sussex.wear.android.datalogger";
    public static final int ADMIN_CODE = 4004;

    /**
     * Defines a shared preference that stores the data collection object. It's empty when
     * the data collection is not running. It is stored persistently in case the device is rebooted.
     */
    public static final String DATA_COLLECTION_SESSION_OBJECT_KEY = PACKAGE_NAME + ".DATA_COLLECTION_SESSION_OBJECT_KEY";

    /**
     * Defines a shared preference that stores the current activity being annotated. It's empty when
     * the user is not annotating. It is stored persistently in case the device is rebooted.
     */
    public static final String LABEL_ANNOTATION_ACTIVITY_KEY = PACKAGE_NAME + ".LABEL_ANNOTATION_ACTIVITY_KEY";

    /**
     * Defines a shared preference that stores the body position in current activity being annotated. It's empty when
     * the user is not annotating. It is stored persistently in case the device is rebooted.
     */
    public static final String LABEL_ANNOTATION_BODYPOSITION_KEY = PACKAGE_NAME + ".LABEL_ANNOTATION_BODYPOSITION_KEY";

    /**
     * Defines a shared preference that stores the location in the current activity being annotated. It's empty when
     * the user is not annotating. It is stored persistently in case the device is rebooted.
     */
    public static final String LABEL_ANNOTATION_LOCATION_KEY = PACKAGE_NAME + ".LABEL_ANNOTATION_LOCATION_KEY";

    /**
     * Defines a shared preference that stores the current activity selected in the UI. It is stored
     * persistently in case DisplayActivity has to be recreated.
     */
    public static final String LABEL_SELECTED_UI_ACTIVITY_KEY = PACKAGE_NAME + ".LABEL_SELECTED_UI_ACTIVITY_KEY";

    /**
     * Defines a shared preference that stores the body position in current activity selected in the
     * UI. It is stored persistently in case DisplayActivity has to be recreated.
     */
    public static final String LABEL_SELECTED_UI_BODYPOSITION_KEY = PACKAGE_NAME + ".LABEL_SELECTED_UI_BODYPOSITION_KEY";

    /**
     * Defines a shared preference that stores the location of the user given for activity selected in the
     * UI. It is stored persistently in case DisplayActivity has to be recreated.
     */
    public static final String LABEL_SELECTED_UI_LOCATION_KEY = PACKAGE_NAME + ".LABEL_SELECTED_UI_LOCATION_KEY";

//    public static final String ACTIVITY_LABEL_KEY = PACKAGE_NAME + ".ACTIVITY_LABEL";
//
//    public static final String BODY_POSITION_LABEL_KEY = PACKAGE_NAME + ".BODY_POSITION_LABEL";

    /**
     * Defines a shared preference that stores the annotation time for each label.
     */
    public static final String ANNOTATION_TIME_PER_LABEL_KEY = PACKAGE_NAME + ".ANNOTATION_TIME_PER_LABEL_KEY_";

    /**
     * Defines a shared preference that stores the starting millis of the last annotation event.
     */
    public static final String ANNOTATING_EVENT_START_KEY = PACKAGE_NAME + ".ANNOTATING_EVENT_START_KEY";

    /**
     * Defines a shared preference that tracks whether the files upload service has started.
     */
    public static final String IS_UPLOAD_SERVICE_RUNNING = PACKAGE_NAME + ".IS_UPLOAD_SERVICE_RUNNING";

    /**
     * Defines a shared preference that stores the current status of the bluetooth adapter
     */
    public static final String BLUETOOTH_STATE_KEY = PACKAGE_NAME + ".BLUETOOTH_STATE_KEY";
    public static final int BLUETOOTH_STATE_NOT_SUPPORTED = 0;
    public static final int BLUETOOTH_STATE_DISABLED = 1;
    public static final int BLUETOOTH_STATE_ENABLED = 2;
    public static final int BLUETOOTH_STATE_CONNECTING = 3;
    public static final int BLUETOOTH_STATE_CONNECTED = 4;

    /**
     * Defines a shared preference that stores the date of the last completed server upload
     */
    public static final String UPLOAD_DATE_LAST_COMPLETED_KEY = PACKAGE_NAME + ".UPLOAD_DATE_LAST_COMPLETED_KEY";


    // Names for each sensor/collector with log files associated
    public static final String SENSOR_NAME_CELL = "Cells";
    public static final String SENSOR_NAME_WIFI = "WiFi";
    public static final String SENSOR_NAME_BLUETOOTH = "Bluetooth";
    public static final String SENSOR_NAME_API_HAR = "API_HAR";
    public static final String SENSOR_NAME_LABELS = "Labels";
    public static final String SENSOR_NAME_ACC = "Accelerometer";
    public static final String SENSOR_NAME_GYR = "Gyroscope";
    public static final String SENSOR_NAME_MAG = "Magnetometer";
    public static final String SENSOR_NAME_MIC = "Audio";
    public static final String SENSOR_NAME_BAT = "Battery";

    public static final String SENSOR_NAME_DEPR_CELL = "DeprCells";
    public static final String SENSOR_NAME_LOC = "Location";
    public static final String SENSOR_NAME_SAT = "GPS";
    public static final String SENSOR_NAME_TEMP = "Temperature";
    public static final String SENSOR_NAME_LT = "Ambient";
    public static final String SENSOR_NAME_PRES = "Pressure";
    public static final String SENSOR_NAME_HUM = "Humidity";

    public static final String SENSOR_NAME_ORIEN = "Orientation";
    public static final String SENSOR_NAME_LIN_ACC = "LinearAcceleration";
    public static final String SENSOR_NAME_GRA = "Gravity";


    public static final String BLUETOOTH_ADDRESS_KEY = PACKAGE_NAME + ".BLUETOOTH_ADDRESS_KEY";

    public static final String BLUETOOTH_SLAVES_CONNECTED_KEY = PACKAGE_NAME + ".BLUETOOTH_SLAVES_CONNECTED_KEY";

    public static final String BLUETOOTH_DEVICE_ADDRESS_INTENT_KEY = PACKAGE_NAME + ".BLUETOOTH_DEVICE_ADDRESS_INTENT_KEY";

    public static final String COMMAND_SERVICE_INTENT_KEY = PACKAGE_NAME + ".COMMAND_SERVICE_INTENT_KEY";

    public static final String DETECTED_ACTIVITIES_INTENT_KEY = PACKAGE_NAME + ".DETECTED_ACTIVITIES_INTENT_KEY";

    public static final String BROADCAST_ACTION = PACKAGE_NAME + ".BROADCAST_ACTION";

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    /**
     * List of DetectedActivity types that the activity recognizer can detect.
     */
    public static final int[] API_ACTIVITY_RECOGNIZER_LIST = {
            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };


    public static final int UI_ERROR_CODE_EXTERNAL_STORAGE = 0;
    public static final int UI_ERROR_CODE_SENSORS_DISABLED = 1;
    public static final int UI_ERROR_CODE_INTERNET_CONNECTIVITY = 2;
    public static final int UI_ERROR_CODE_FILE_UPLOAD = 3;
    public static final int UI_ERROR_CODE_NO_UNSYNC_FILES = 4;
    public static final int UI_ERROR_CODE_COLLECTORS_START = 5;


    public static final int DEVICE_LOCATION_TORSO = 0;
    public static final int DEVICE_LOCATION_HIPS = 1;
    public static final int DEVICE_LOCATION_BAG = 2;
    public static final int DEVICE_LOCATION_HAND = 3;


    /**
     * Message types sent to the DataLoggerService Handler
     */
    public static final int BLUETOOTH_MESSAGE_STATE_CHANGE = 0;
    public static final int BLUETOOTH_MESSAGE_READ = 1;
    public static final int BLUETOOTH_MESSAGE_WRITE = 2;
    public static final int BLUETOOTH_MESSAGE_DEVICE_ADDRESS = 3;
    public static final int BLUETOOTH_MESSAGE_CONNECTION_LOST = 4;
    public static final int BLUETOOTH_MESSAGE_CONNECTION_FAILED = 5;
    public static final int UPLOAD_MESSAGE_ON_PROGRESS = 6;
    public static final int UPLOAD_MESSAGE_COMPLETED = 7;
    public static final int UPLOAD_MESSAGE_CANCELLED = 8;
    public static final int UI_MESSAGE_ERROR_TOAST = 10;



    // Key names received to the DataLoggerService Handler
    public static final String BLUETOOTH_CONNECTED_DEVICE_ADDRESS = "bluetooth_connected_device_address";
    public static final String BLUETOOTH_CONNECTED_DEVICE_LOCATION = "bluetooth_connected_device_location";
    public static final String BLUETOOTH_MESSAGE_READ_COMMANDS = "bluetooth_message_read_commands";
    public static final String UPLOAD_ONPROGRESS_NUMBER_FILES = "upload_onprogress_number_files";
    public static final String UPLOAD_ONPROGRESS_NUMBER_FILES_UPLOADED = "upload_onprogress_number_files_uploaded";
    public static final String UPLOAD_ONPROGRESS_RATE = "upload_onprogress_rate";
    public static final String UPLOAD_ONPROGRESS_PERCENTAGE = "upload_onprogress_percentage";
    public static final String TOAST = "TOAST";

}
