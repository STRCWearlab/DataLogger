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

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

//import uk.ac.sussex.wear.android.datalogger.bt.BluetoothConnectionHelper;
import uk.ac.sussex.wear.android.datalogger.collector.DataCollectors;
import uk.ac.sussex.wear.android.datalogger.data.CommandBase;
import uk.ac.sussex.wear.android.datalogger.data.CommandDCE;
import uk.ac.sussex.wear.android.datalogger.data.CommandFUS;
import uk.ac.sussex.wear.android.datalogger.data.CommandKA;
import uk.ac.sussex.wear.android.datalogger.data.CommandLAE;
import uk.ac.sussex.wear.android.datalogger.data.DataCollectionSession;
import uk.ac.sussex.wear.android.datalogger.db.DataLoggerDataSource;
import uk.ac.sussex.wear.android.datalogger.har.HARecognizerApiHandler;
import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;
import uk.ac.sussex.wear.android.datalogger.log.LabelsLogger;
import uk.ac.sussex.wear.android.datalogger.upload.FileUploader;


public class DataLoggerService extends Service {

    private static final String TAG = DataLoggerService.class.getSimpleName();

    // Member fields

    private PowerManager.WakeLock wakeLock;

    private DataCollectors mDataCollectors;

    private LabelsLogger mLabelsLogger = null;

    private CustomLogger mFlaggedEventsLogger = null;

    private Long mNanosOffset = null;

    private DataCollectionSession mDataCollectionSession = null;

    private HARecognizerApiHandler mHARecognizerApiHandler;

    //private BluetoothConnectionHelper mBluetoothConnection = null;

    private BluetoothAdapter mBluetoothAdapter = null;

    private static DataLoggerStatusDelegate mDelegate = null;

    private FileUploader mFileUploader;

    private Notification mNotification;

    // Master device Bluetooth MAC address
    private String mMasterAddress;

    // Timer to manage maximum logging intervals
    private final Runnable mLogTimerRunnable = new Runnable() {
        @Override
        public void run() {
            haltAndRestartLogging();
            // Delay value defined in seconds
            mHandler.postDelayed(this, SharedPreferencesHelper.getLogMaxTime(DataLoggerService.this) * 1000);
        }
    };


   /* private final Runnable connectTimerRunnable = new Runnable() {
        @Override
        public void run() {
            int timeout = SharedPreferencesHelper.getSlaveConnectionTimeout(DataLoggerService.this);
            int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(DataLoggerService.this);
            if (BuildConfig.DEBUG && (deviceLocation == Constants.DEVICE_LOCATION_HAND))
                throw new RuntimeException("::connectTimerRunnable Runnable only required in slave devices");
            Log.i(TAG, "::ConnectTimerRunnable Re/Starting connection with index " + deviceLocation + ". Timed every " + timeout + " millis.");
            *//*if (!mBluetoothConnection.isConnected(deviceLocation) && !mMasterAddress.equals("")) {
                Log.i(TAG, "::ConnectTimerRunnable Current connection state at index " + deviceLocation + " is "
                        + mBluetoothConnection.isConnected(deviceLocation)
                        + ", starting connection to address " + mMasterAddress);
                updateBluetoothState(Constants.BLUETOOTH_STATE_CONNECTING);
                mHandler.postDelayed(this, timeout);
                mBluetoothConnection.start(deviceLocation);
                mBluetoothConnection.connect(mMasterAddress, deviceLocation);
            }*//*
        }
    };*/


    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean ret = true;
            // Location of the current device to determine its role (master/slave)
            int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(DataLoggerService.this);
            switch (msg.what) {
                case Constants.BLUETOOTH_MESSAGE_STATE_CHANGE:
                    /*switch (msg.arg1) {
                        case BluetoothConnectionHelper.STATE_CONNECTED:
                            Log.i(TAG, "::handleMessage Bluetooth connection established. Socket at position " + msg.arg2);
                            //Only the slaves change to connected state
                            if (deviceLocation != Constants.DEVICE_LOCATION_HAND) {
                                updateBluetoothState(Constants.BLUETOOTH_STATE_CONNECTED);
                                //The connection runnable is stopped
                                Log.i(TAG, "::handleMessage Removing callback for runnable connectTimerRunnable");
                                mHandler.removeCallbacks(connectTimerRunnable);
                            } else {
                                // Updates the int from SharedPreferences that defines the number of slaves connected to refresh the UI
                                int[] bitmasks = new int[]{0x4, 0x2, 0x1};
                                SharedPreferencesHelper.setSlavesConnected(DataLoggerService.this,
                                        (SharedPreferencesHelper.getSlavesConnected(DataLoggerService.this) | bitmasks[msg.arg2])
                                );
                            }
                            break;
                        case BluetoothConnectionHelper.STATE_CONNECTING:
                            if (deviceLocation != Constants.DEVICE_LOCATION_HAND) {
                                updateBluetoothState(Constants.BLUETOOTH_STATE_CONNECTING);
                            }
                            Log.i(TAG, "::handleMessage Bluetooth socket connecting. Socket at position " + msg.arg2);
                            break;
                        case BluetoothConnectionHelper.STATE_LISTEN:
                            Log.i(TAG, "::handleMessage Bluetooth socket listening. Socket at position " + msg.arg2);
                            break;
                        case BluetoothConnectionHelper.STATE_NONE:
                            Log.i(TAG, "::handleMessage Bluetooth socket waiting. Socket at position " + msg.arg2);
                            break;
                    }*/
                    break;
                case Constants.BLUETOOTH_MESSAGE_CONNECTION_LOST:
                    final int indexSocketLost = msg.getData().getInt(Constants.BLUETOOTH_CONNECTED_DEVICE_LOCATION);
                    Log.i(TAG, "::handleMessage Bluetooth connection lost. Socket at position " + indexSocketLost);
                    // If the device is master, it updates the int from SharedPreferences that defines
                    // the number of slaves connected to refresh the UI
                    if (deviceLocation == Constants.DEVICE_LOCATION_HAND) {
                        int[] bitmasks = new int[]{0x3, 0x5, 0x6};
                        SharedPreferencesHelper.setSlavesConnected(DataLoggerService.this,
                                (SharedPreferencesHelper.getSlavesConnected(DataLoggerService.this) & bitmasks[indexSocketLost])
                        );
                        // vibration for 800 milliseconds if data collection is on
                        if (SharedPreferencesHelper.getDataCollectionState(DataLoggerService.this)) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int[] bitmasks = new int[]{0x4, 0x2, 0x1};
                                    int value = SharedPreferencesHelper.getSlavesConnected(DataLoggerService.this);
                                    boolean isConnected = ((value & bitmasks[indexSocketLost]) == bitmasks[indexSocketLost]);
                                    if (!isConnected)
                                        // vibration for 800 milliseconds
                                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(800);
                                }
                            }, 7000);
                        }
                    }
                    // The bluetooth socket is forced to stop
                    //mBluetoothConnection.stop(indexSocketLost);
                    // If the bluetooth adapter keeps enabled (the connection can be lost when the adapter is manually disabled)
                    // the bluetooth connection tries to reconnect
                    int bluetoothState = mBluetoothAdapter.isEnabled() ? Constants.BLUETOOTH_STATE_ENABLED : Constants.BLUETOOTH_STATE_DISABLED;
                    Log.d(TAG, "Calling updateBluetoothState in connection lost with state " + bluetoothState);
                    updateBluetoothState(bluetoothState);
                    break;
//                case Constants.BLUETOOTH_MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
////                    mConversationArrayAdapter.add("Me:  " + writeMessage);
//                    Log.d(TAG, "Writing message "+writeMessage);
//                    break;
                case Constants.BLUETOOTH_MESSAGE_READ:
                    ArrayList<String> list = msg.getData().getStringArrayList(Constants.BLUETOOTH_MESSAGE_READ_COMMANDS);
                    if (list != null) {
                        if (list.size() > 0) {
                            Log.i(TAG, "::handleMessage Command read. Command '" + list.get(0) + "'");
                            try {
                                processCommands(list);
                            } catch (Exception e) {
                                Log.e(TAG, "::handleMessage Error processing command '" + list.get(0) + "'", e);
                            }
                        } else {
                            Log.e(TAG, "::handleMessage Command error. Empty list");
                        }
                    } else {
                        Log.e(TAG, "::handleMessage Error processing command. List is null");
                    }
                    break;
                case Constants.BLUETOOTH_MESSAGE_DEVICE_ADDRESS:
                    // Save the connected device's address
                    SharedPreferencesHelper.setBluetoothAddress(DataLoggerService.this,
                            msg.getData().getString(Constants.BLUETOOTH_CONNECTED_DEVICE_ADDRESS));
                    break;
                case Constants.BLUETOOTH_MESSAGE_CONNECTION_FAILED:
                    int indexSocketFailed = msg.getData().getInt(Constants.BLUETOOTH_CONNECTED_DEVICE_LOCATION);
                    Log.d(TAG, "::handleMessage Connection attempt failed. Socket at position " + indexSocketFailed);
//                    mBluetoothConnection.stop(indexSocketFailed);

                case Constants.UPLOAD_MESSAGE_ON_PROGRESS:
                    final int nbFilesUpload = msg.getData().getInt(Constants.UPLOAD_ONPROGRESS_NUMBER_FILES);
                    final int nbRemainingFilesUpload = msg.getData().getInt(Constants.UPLOAD_ONPROGRESS_NUMBER_FILES_UPLOADED);
                    final String rate = msg.getData().getString(Constants.UPLOAD_ONPROGRESS_RATE);
                    final long pct = msg.getData().getLong(Constants.UPLOAD_ONPROGRESS_PERCENTAGE);
                    if (mDelegate != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDelegate.onUploadProgress(nbFilesUpload, nbRemainingFilesUpload, rate, pct);
                            }
                        });
                    }
                    break;
                case Constants.UPLOAD_MESSAGE_CANCELLED:
                    if (mDelegate != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDelegate.onUploadCancelled();
                            }
                        });
                    }
                    break;
                case Constants.UPLOAD_MESSAGE_COMPLETED:
                    if (mDelegate != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDelegate.onUploadComplete();
                            }
                        });
                    }
                    break;
                case Constants.UI_MESSAGE_ERROR_TOAST:
                    final int errorCode = msg.getData().getInt(Constants.TOAST);
                    if (mDelegate != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDelegate.onError(errorCode);
                            }
                        });
                    }
                    break;
                default:
                    Log.e(TAG, "::handleMessage Invalid message " + msg.what);
                    ret = false;
                    break;
            }
            return ret;
        }
    });

    public void onCreate() {
        // Fires when a service is first initialized
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        mMasterAddress = "";

        mNotification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.notification_content_title))
                .setTicker(getResources().getString(R.string.notification_ticker))
                .setContentText(getResources().getString(R.string.notification_waiting_content_text))
                .setSmallIcon(R.drawable.ic_not_128)
                .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, DisplayActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setOngoing(true).build();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) { // Device does not support Bluetooth
            updateBluetoothState(Constants.BLUETOOTH_STATE_NOT_SUPPORTED);
        } else {
            registerReceiver(mBluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.hasExtra(Constants.COMMAND_SERVICE_INTENT_KEY)) {
                String message = intent.getStringExtra(Constants.COMMAND_SERVICE_INTENT_KEY);
                try {
                    processCommands(CommandBase.parseMessage(message));
                } catch (Exception e) {
                    Log.e(TAG, "::onStartCommand Error processing message '" + message + "': " + e.getMessage());
                }
            }
        }

        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        Log.d(TAG, "Calling startForeground");
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, mNotification);

        //Return "sticky" for services that are explicitly started and stopped as needed by the app
        return START_STICKY;
    }


    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        updateBluetoothState(Constants.BLUETOOTH_STATE_DISABLED);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        updateBluetoothState(Constants.BLUETOOTH_STATE_ENABLED);
                        break;
                }
            }
        }
    };

    private void haltAndRestartLogging() {
        if (mDataCollectors != null) {
            mDataCollectors.haltAndRestartLogging();
        }

        if (SharedPreferencesHelper.isEnabledHARAPI(DataLoggerService.this) && (mHARecognizerApiHandler != null)) {
            mHARecognizerApiHandler.haltAndRestartLogging();
        }
    }


    /**
     * Checks if external storage is available for read and write
     */
    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public boolean areDeviceSettingsCorrect() {
        boolean isStatusCorrect = true;

        // If there is a data collector related to the GPS, check GPS status
        if (SharedPreferencesHelper.isEnabledLocation(this)
                || SharedPreferencesHelper.isEnabledSatellite(this)) {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!enabledGPS) { // If the GPS is not enabled
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder
                        .setMessage("GPS is disabled. Enable it or disable related data collectors.")
                        .setCancelable(false)
                        .setPositiveButton("Go to Settings",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent callGPSSettingIntent = new Intent(
                                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        callGPSSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(callGPSSettingIntent);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = alertDialogBuilder.create();
                alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alert.show();
                isStatusCorrect = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
        }

        // If there is a data collector related to the WiFi, check WiFi status
        if (SharedPreferencesHelper.isEnabledWiFi(this)) {
            WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            if (!manager.isWifiEnabled()) {
                manager.setWifiEnabled(true);
            }
        }
        return isStatusCorrect;
    }

    private int startDataCollection(String masterSessionId, long nanosOffset) {

        mNanosOffset = nanosOffset;

        // Check whether external storage is available for read and write
        if (!isExternalStorageWritable()) {
            return Constants.UI_ERROR_CODE_EXTERNAL_STORAGE;
        }

        // Check whether device sensor settings are correct
        if (!areDeviceSettingsCorrect()) {
            return Constants.UI_ERROR_CODE_SENSORS_DISABLED;
        }

        final int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(this);
        if ("".equals(masterSessionId) && deviceLocation != Constants.DEVICE_LOCATION_HAND)
            Log.e(TAG, "::startDataCollection Error: Master session id cannot be empty");

        DataCollectionSession storedDataCollectionSessionObject = SharedPreferencesHelper.getDataCollectionSessionObject(this);
        if (storedDataCollectionSessionObject == null) {
            // A new data collection session object is created
            mDataCollectionSession = new DataCollectionSession(
                    Calendar.getInstance().getTime(),
                    getResources().getString(R.string.session_id_date_dateformat),
                    SharedPreferencesHelper.getUserName(this),
                    SharedPreferencesHelper.getDeviceLocation(this),
                    System.currentTimeMillis(),
                    masterSessionId,
                    nanosOffset);
        } else {
            mDataCollectionSession = storedDataCollectionSessionObject;
        }

        // The session id is generated for the new data collection session
        String sessionName = mDataCollectionSession.getSessionName();
        Log.i(TAG, "::startDataCollection Starting data collection with session name: " + sessionName + ". Offset is " + nanosOffset);

        mDataCollectors = DataCollectors.getInstance();
        try {
            mDataCollectors.startDataCollectors(this, sessionName, nanosOffset);
        } catch (Exception e) {
            mDataCollectionSession = null;
            Log.e(TAG, "Error starting data collectors: " + e.getMessage());
            return Constants.UI_ERROR_CODE_COLLECTORS_START;
        }

        // If there were no errors starting data collection, the data collection object is stored.
        SharedPreferencesHelper.setDataCollectionSessionObject(this, mDataCollectionSession);

        // Insert DataCollectionSession into db if not exist
        if (!DataLoggerDataSource.existDataCollectionSession(this, mDataCollectionSession)) {
            DataLoggerDataSource.insertDataCollectionSession(this, mDataCollectionSession);
        }

        if (SharedPreferencesHelper.isEnabledHARAPI(this)) {
            mHARecognizerApiHandler = new HARecognizerApiHandler(this,
                    sessionName,
                    SharedPreferencesHelper.getDetectionIntervalHARAPI(this),
                    nanosOffset,
                    SharedPreferencesHelper.getLogFilesMaxsize(this));

            mHARecognizerApiHandler.start();
        }

        if (SharedPreferencesHelper.getLogMaxTime(this) > 0) {
            mHandler.postDelayed(mLogTimerRunnable, SharedPreferencesHelper.getLogMaxTime(this) * 1000);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (deviceLocation == Constants.DEVICE_LOCATION_HAND) {
            //Pending intent for flagging a special event during the recording
            PendingIntent flagEventIntent = PendingIntent.getActivity(this,
                    1, new Intent(this, FlagEventActivity.class),
                    PendingIntent.FLAG_CANCEL_CURRENT);

            // Service notification is updated to show that the service is collecting data
            mNotification = new NotificationCompat.Builder(this)
                    .setContentTitle(getResources().getString(R.string.notification_content_title))
                    .setTicker(getResources().getString(R.string.notification_ticker))
                    .setContentText(getResources().getString(R.string.notification_collecting_content_text))
                    .setSmallIcon(R.drawable.ic_rec_128)
                    .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, DisplayActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                    .addAction(R.drawable.ic_record_voice_over_white_18dp, this.getResources().getString(R.string.flag_event), flagEventIntent)
                    .setOngoing(true)
                    .build();

            String path = sessionName + File.separator + "flagged_events" + "_" + sessionName;
            mFlaggedEventsLogger = new CustomLogger(this, path, sessionName,
                    "flagged_events", "txt", false,
                    nanosOffset, SharedPreferencesHelper.getLogFilesMaxsize(this));
            mFlaggedEventsLogger.start();

        } else {

            // Service notification is updated to show that the service is collecting data
            mNotification = new NotificationCompat.Builder(this)
                    .setContentTitle(getResources().getString(R.string.notification_content_title))
                    .setTicker(getResources().getString(R.string.notification_ticker))
                    .setContentText(getResources().getString(R.string.notification_collecting_content_text))
                    .setSmallIcon(R.drawable.ic_rec_128)
                    .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, DisplayActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                    .setOngoing(true).build();
        }

        notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, mNotification);

        // If the device is the master the command is broadcasted
        /*if ((deviceLocation == Constants.DEVICE_LOCATION_HAND) && (mBluetoothConnection != null)) {
            String broadcastMessage = new CommandDCE(true, mDataCollectionSession.getSessionId(), SystemClock.elapsedRealtimeNanos()).getMessageBluetooth();
            mBluetoothConnection.broadcastMessage(broadcastMessage);
        }*/

        return -1;
    }

    private boolean stopDataCollection() {

        if (mDataCollectionSession == null) {
            return false;
        }

        // The data Collection session object is updated
        mDataCollectionSession.setEndDate(Calendar.getInstance().getTime());
        DataLoggerDataSource.updateEndDateDataCollectionSession(this, mDataCollectionSession);

        // The data Collection session object is terminated
        mDataCollectionSession = null;
        SharedPreferencesHelper.setDataCollectionSessionObject(this, mDataCollectionSession);

        mDataCollectors.stopDataCollectors();
        if (mFlaggedEventsLogger != null) {
            mFlaggedEventsLogger.stop();
            mFlaggedEventsLogger = null;
            mNanosOffset = null;
        }

        if (SharedPreferencesHelper.isEnabledHARAPI(this)
                && mHARecognizerApiHandler != null) {
            mHARecognizerApiHandler.stop();
        }

        if (SharedPreferencesHelper.getLogMaxTime(this) > 0) {
            mHandler.removeCallbacks(mLogTimerRunnable);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Service notification is updated to show that the service is NOT collecting data
        mNotification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.notification_content_title))
                .setTicker(getResources().getString(R.string.notification_ticker))
                .setContentText(getResources().getString(R.string.notification_waiting_content_text))
                .setSmallIcon(R.drawable.ic_not_128)
                .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, DisplayActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setOngoing(true).build();
        notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, mNotification);

        // If the device is the master the command is broadcasted
        /*int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(this);
        if ((deviceLocation == Constants.DEVICE_LOCATION_HAND) && (mBluetoothConnection != null)) {
            String broadcastMessage = new CommandDCE(false).getMessageBluetooth();
            mBluetoothConnection.broadcastMessage(broadcastMessage);
        }*/

        return true;
    }

    private void updateDataCollectionNanoOffset(long nanosOffset) {

        mNanosOffset = nanosOffset;

        if (mDataCollectors != null)
            mDataCollectors.updateNanosOffset(nanosOffset);
        if (mHARecognizerApiHandler != null)
            mHARecognizerApiHandler.updateNanosOffset(nanosOffset);

    }

    private boolean startLabelAnnotation(int activity, int bodyPosition, int location) {

        if (mDataCollectionSession == null) {
            return false;
        }

        if (mLabelsLogger != null) {
            return false;
        }

        String sessionName = mDataCollectionSession.getSessionName();
        long nanosOffset = mDataCollectionSession.getNanosOffset();

        mLabelsLogger = new LabelsLogger(this,
                sessionName + File.separator + Constants.SENSOR_NAME_LABELS + "_" + sessionName,
                sessionName,
                nanosOffset);
        mLabelsLogger.start();
        mLabelsLogger.setup(activity, bodyPosition, location);
        mLabelsLogger.logStart();

        // Activity and body position are stored persistently in case the device is rebooted.
        SharedPreferencesHelper.setAnnotatedActivityLabel(this, activity);
        SharedPreferencesHelper.setAnnotatedBodyPositionLabel(this, bodyPosition);
        SharedPreferencesHelper.setAnnotatedLocationLabel(this, location);

        // For every label annotation event the logging system will create new files. This is to simplify
        // the sensor data processing, specially in the case of the audio. Most of the sensors have a
        // nano timestamp for each reading, but in the case of the audio this is not possible.
        haltAndRestartLogging();

        // If the device is the master the command is broadcasted
        /*int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(this);
        if ((deviceLocation == Constants.DEVICE_LOCATION_HAND) && (mBluetoothConnection != null)) {
            String broadcastMessage = new CommandLAE(true, activity, bodyPosition, location).getMessageBluetooth();
            mBluetoothConnection.broadcastMessage(broadcastMessage);
        }*/

        return true;
    }

    private boolean stopLabelAnnotation() {

        if (mLabelsLogger == null) {
            return false;
        }

        mLabelsLogger.logEnd();
        mLabelsLogger.stop();
        mLabelsLogger = null;

        // Activity and body position are stored persistently in case the device is rebooted.
        SharedPreferencesHelper.setAnnotatedActivityLabel(this, -1);
        SharedPreferencesHelper.setAnnotatedBodyPositionLabel(this, -1);
        SharedPreferencesHelper.setAnnotatedLocationLabel(this, R.id.ui_connectivity_second_message);

        // For every label annotation event the logging system will create new files. This is to simplify
        // the sensor data processing, specially in the case of the audio. Most of the sensors have a
        // nano timestamp for each reading, but in the case of the audio this is not possible.
        haltAndRestartLogging();

        Log.d(TAG, "stopLabelAnnotation " + SharedPreferencesHelper.getAnnotatedActivityLabel(this));

        // If the device is the master the command is broadcasted
        /*int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(this);
        if ((deviceLocation == Constants.DEVICE_LOCATION_HAND) && (mBluetoothConnection != null)) {
            String broadcastMessage = new CommandLAE(false).getMessageBluetooth();
            mBluetoothConnection.broadcastMessage(broadcastMessage);
        }*/

        return true;
    }

    private boolean stopAndStartLabelAnnotation(int activity, int bodyPosition, int location) {

        if (mLabelsLogger == null) {
            return false;
        }

        if (mDataCollectionSession == null) {
            return false;
        }

        mLabelsLogger.logEnd();
        mLabelsLogger.setup(activity, bodyPosition, location);
        mLabelsLogger.logStart();

        // Activity and body position are stored persistently in case the device is rebooted.
        SharedPreferencesHelper.setAnnotatedActivityLabel(this, activity);
        SharedPreferencesHelper.setAnnotatedBodyPositionLabel(this, bodyPosition);
        SharedPreferencesHelper.setAnnotatedLocationLabel(this, location);

        // For every label annotation event the logging system will create new files. This is to simplify
        // the sensor data processing, specially in the case of the audio. Most of the sensors have a
        // nano timestamp for each reading, but in the case of the audio this is not possible.
        haltAndRestartLogging();

        // If the device is the master the command is broadcasted
        /*int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(this);
        if ((deviceLocation == Constants.DEVICE_LOCATION_HAND) && (mBluetoothConnection != null)) {
            String broadcastMessage = new CommandLAE(false).setStopAndStartEvent(activity, bodyPosition, location)
                    .getMessageBluetooth();
            mBluetoothConnection.broadcastMessage(broadcastMessage);
        }*/

        return true;
    }

    private void processCommands(ArrayList<String> list) throws Exception {
        Iterator<String> itr = list.iterator();
        while (itr.hasNext()) {
            String comm = itr.next();
            Log.i(TAG, "::processCommands Processing command '" + comm + "'");
            switch (comm) {
                case CommandBase.COMMAND_DATA_COLLECTION_EVENT:
                    CommandDCE commandDCE = new CommandDCE();
                    itr = commandDCE.setParams(itr);
                    if (commandDCE.getState()) {
                        long nanosOffset = (commandDCE.getNanosOffset() != -1) ? (commandDCE.getNanosOffset() - SystemClock.elapsedRealtimeNanos()) : 0;
                        boolean isCollectingData = (mDataCollectionSession != null);
                        Log.i(TAG, "::processCommands Trying to start a data collection session. Current state is: " + isCollectingData);
                        if (!isCollectingData) {
                            final int code = startDataCollection(commandDCE.getMasterSessionId(), nanosOffset);
                            if ((code >= 0) && (mDelegate != null)) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDelegate.onError(code);
                                    }
                                });
                            }
                        }
                    } else {
                        stopDataCollection();
                    }
                    break;
                case CommandBase.COMMAND_LABEL_ANNOTATION_EVENT:
                    CommandLAE commandLAE = new CommandLAE();
                    itr = commandLAE.setParams(itr);
                    if (commandLAE.getState()) {
                        startLabelAnnotation(commandLAE.getActivityLabel(), commandLAE.getPositionLabel(), commandLAE.getLocationLabel());
                    } else {
                        if (commandLAE.getNextState()) {
                            stopAndStartLabelAnnotation(commandLAE.getNextActivityLabel(), commandLAE.getNextPositionLabel(), commandLAE.getNextLocationLabel());
                        } else {
                            stopLabelAnnotation();
                        }
                    }
                    break;
                /*case CommandBase.COMMAND_BLUETOOTH_START:
                    if (mBluetoothConnection != null) {
                        mBluetoothConnection.stop(Constants.DEVICE_LOCATION_TORSO);
                        mBluetoothConnection.stop(Constants.DEVICE_LOCATION_HIPS);
                        mBluetoothConnection.stop(Constants.DEVICE_LOCATION_BAG);
                        mBluetoothConnection = null;
                    }
                    if ((mBluetoothAdapter != null)) {
                        // Initialize the int from SharedPreferences that defines the number of slaves connected
                        SharedPreferencesHelper.setSlavesConnected(this, 0);
                        int bluetoothState = mBluetoothAdapter.isEnabled() ? Constants.BLUETOOTH_STATE_ENABLED : Constants.BLUETOOTH_STATE_DISABLED;
                        updateBluetoothState(bluetoothState);
                    }
                    break;*/
                case CommandBase.COMMAND_BLUETOOTH_CONNECT:
                    if (!itr.hasNext())
                        throw new Exception("Command '" + comm + "' is malformed or missing parameters");
                    mMasterAddress = itr.next();
                    //mHandler.post(connectTimerRunnable);
                    break;
                case CommandBase.COMMAND_FILES_UPLOAD_START:
                    mFileUploader = FileUploader.getInstance(this, mHandler);
                    mFileUploader.startUpload();
                    /*int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(this);
                    if ((deviceLocation == Constants.DEVICE_LOCATION_HAND) && (mBluetoothConnection != null)) {
                        String broadcastMessage = new CommandFUS().getMessageBluetooth();
                        mBluetoothConnection.broadcastMessage(broadcastMessage);
                    }*/
                    break;
                case CommandBase.COMMAND_FILES_UPLOAD_CANCEL:
                    mFileUploader.cancelUpload();
                    break;
                case CommandBase.COMMAND_KEEP_ALIVE_EVENT:
                    CommandKA commandKA = new CommandKA();
                    itr = commandKA.getParams(itr);
                    boolean isCollectingData = (mDataCollectionSession != null);
                    if (commandKA.getDataCollectionState()) {
                        if (!isCollectingData) {
                            Log.i(TAG, "::processCommands Master/slave synchronization disrupted. " +
                                    "Data logging is running in master and NOT in slave." +
                                    "Trying to start a data collection session");
                            long nanosOffset = (commandKA.getNanosOffset() != -1) ? (commandKA.getNanosOffset() - SystemClock.elapsedRealtimeNanos()) : 0;
                            Log.i(TAG, "Received offset from KA is: " + commandKA.getNanosOffset() + " .My offset is " + nanosOffset);
                            startDataCollection(commandKA.getMasterSessionId(), commandKA.getNanosOffset());
                        } else {
                            Log.i(TAG, "::processCommands Master/slave synchronization OK. mNanoOffset " + mNanosOffset + ", ");
                            if (mNanosOffset == 0) {
                                updateDataCollectionNanoOffset(commandKA.getNanosOffset() - SystemClock.elapsedRealtimeNanos());
                            }
                        }
                    } else {
                        if (isCollectingData) {
                            Log.i(TAG, "::processCommands Master/slave synchronization disrupted. " +
                                    "Data logging is running in slave and NOT in master. " +
                                    "Trying to stop data collection session");
                            stopDataCollection();
                        }
                    }

                    boolean currentLAE = SharedPreferencesHelper.getLabelsAnnotationState(this);
                    if (commandKA.getLabelsAnnotationState()) {
                        if (!currentLAE) {
                            Log.i(TAG, "::processCommands  Master/slave synchronization disrupted. " +
                                    "Label annotation is active in master and NOT in slave. " +
                                    "Trying to start label annotation");
                            startLabelAnnotation(commandKA.getActivityLabel(), commandKA.getBodyPositionLabel(), commandKA.getLocationLabel());
                        } else {
                            if (!((mLabelsLogger.getCurrentActivity() == commandKA.getActivityLabel())
                                    && (mLabelsLogger.getCurrentBodyPosition() == commandKA.getBodyPositionLabel())
                                    && (mLabelsLogger.getCurrentLocation() == commandKA.getLocationLabel())
                            )) {
                                Log.i(TAG, "::processCommands  Master/slave synchronization disrupted."
                                        + " Master labels: activity " + commandKA.getActivityLabel() + " body position " + commandKA.getBodyPositionLabel() + " location " + commandKA.getLocationLabel() + "."
                                        + " Slave labels: activity " + mLabelsLogger.getCurrentActivity() + " body position " + mLabelsLogger.getCurrentBodyPosition() + " location " + mLabelsLogger.getCurrentLocation() + "."
                                        + ".Trying to restart label annotation");
                                stopAndStartLabelAnnotation(commandKA.getActivityLabel(), commandKA.getBodyPositionLabel(), commandKA.getLocationLabel());
                            }
                        }
                    } else {
                        if (currentLAE) {
                            Log.i(TAG, "::processCommands  Master/slave synchronization disrupted. " +
                                    "Label annotation is active in slave and NOT in master. " +
                                    "Trying to stop label annotation");
                            stopLabelAnnotation();
                        }
                    }
                    break;
                case CommandBase.COMMAND_FLAG_EVENT:
                    if (!itr.hasNext())
                        throw new Exception("Command '" + comm + "' is malformed or missing parameters");
                    String eventLabel = itr.next();
                    if (!itr.hasNext())
                        throw new Exception("Command '" + comm + "' is malformed or missing parameters");
                    String eventNotes = itr.next();

                    // System nanoseconds since boot, including time spent in sleep.
                    long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;

                    // System local time in millis
                    long currentMillis = (new Date()).getTime();

                    String message = String.format("%s", currentMillis) + ";"
                            + String.format("%s", nanoTime) + ";"
                            + String.format("%s", mNanosOffset) + ";";

                    message += eventLabel + ";";

                    message += eventNotes + ";";

                    mFlaggedEventsLogger.log(message);
                    mFlaggedEventsLogger.log(System.lineSeparator());

                    break;
                default:
                    Log.e(TAG, "::processCommands Invalid command " + comm);
                    break;
            }
        }
    }

    public void updateBluetoothState(final int state) {

        String[] bluetoothStateList = SharedPreferencesHelper.getBluetoothStateList(this);
        Log.d(TAG, "::updateBluetoothState Updating bluetooth with state: " + bluetoothStateList[state]);

        SharedPreferencesHelper.setBluetoothStatus(this, state);
        /*switch (state) {
            case Constants.BLUETOOTH_STATE_DISABLED:
                mHandler.removeCallbacks(connectTimerRunnable);
                break;
            case Constants.BLUETOOTH_STATE_ENABLED:
                int deviceLocation = SharedPreferencesHelper.getDeviceLocationValue(this);
                boolean isServer = (deviceLocation == Constants.DEVICE_LOCATION_HAND);

                if (mBluetoothConnection == null) {
                    mBluetoothConnection = new BluetoothConnectionHelper(this, mHandler, isServer);
                }

                // Start the Bluetooth sockets
                if (isServer) {

                    for (int location : new int[]{
                            Constants.DEVICE_LOCATION_TORSO,
                            Constants.DEVICE_LOCATION_HIPS,
                            Constants.DEVICE_LOCATION_BAG}) {
                        if (mBluetoothConnection.getState(location) == BluetoothConnectionHelper.STATE_NONE) {
                            mBluetoothConnection.start(location);
                        }
                    }

                } else {

                    mMasterAddress = SharedPreferencesHelper.getBluetoothAddress(this);
                    mHandler.post(connectTimerRunnable);
                }

                break;
        }*/
    }


    public void onDestroy() {
        // Cleanup service before destruction
        super.onDestroy();
        stopDataCollection();
        unregisterReceiver(mBluetoothStateReceiver);
        stopForeground(true);

        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();

        Log.d(TAG, "onDestroy Service finished");
    }


    protected static void setStatusDelegate(DataLoggerStatusDelegate delegate) {
        if (delegate == null)
            return;
        mDelegate = delegate;
    }

    protected static DataLoggerStatusDelegate getStatusDelegate() {
        return mDelegate;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}