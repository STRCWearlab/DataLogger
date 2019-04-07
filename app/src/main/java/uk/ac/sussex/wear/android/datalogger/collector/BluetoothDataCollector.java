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


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import uk.ac.sussex.wear.android.datalogger.Constants;
import uk.ac.sussex.wear.android.datalogger.R;
import uk.ac.sussex.wear.android.datalogger.collector.BTHelper.DeviceAdapter;
import uk.ac.sussex.wear.android.datalogger.collector.BTHelper.ScannedDevice;
import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;
import android.app.Activity;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// child class for collecting Bluetooth data
// functionality from iBeaconDetector https://github.com/youten/iBeaconDetector

public class BluetoothDataCollector extends AbstractDataCollector implements BluetoothAdapter.LeScanCallback{

    private static final String TAG = BluetoothDataCollector.class.getSimpleName();

    private CustomLogger logger = null;

    private int mSamplingPeriodUs;

    private Context mContext;

    // Timer to manage specific sampling rates
    private Handler mTimerHandler = null;
    private Runnable mTimerRunnable = null;

    // Receiver class for monitoring changes in WiFi states
    private BluetoothInfoReceiver mBTInfoReceiver;

    //needed Bluetooth variables
    private BluetoothAdapter mBTAdapter = null;
    private boolean mIsScanning;
    private DeviceAdapter mDeviceAdapter;
    private BluetoothManager mBTManager;

    @SuppressLint("ServiceCast")
    public BluetoothDataCollector(Context context, String sessionName, String sensorName, int samplingPeriodUs, long nanosOffset, int logFileMaxSize) {

        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        mSamplingPeriodUs = samplingPeriodUs;

        mBTManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);

        mContext = context;

        // Offset to match timestamps both in master and slaves devices
        mNanosOffset = nanosOffset;

        if (mSamplingPeriodUs > 0) {
            Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_BLUETOOTH + " test1");
            mTimerHandler = new Handler();
            Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_BLUETOOTH + " test4");
            // trying to write in txt file
            /*String message = "Hallo!";
            logger.log(message);*/
            mTimerRunnable = new Runnable() {
                // still problems here !!! run() seems to not work properly. Log doesn't get called
                // no BT txt files are written
                @Override
                public void run() {
                    Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_BLUETOOTH + " test2");
                    logBluetoothInfo(mDeviceAdapter.getScanList());
                    int millis = 1000 / mSamplingPeriodUs;
                    mTimerHandler.postDelayed(this, millis);
                }
            };
        } else {
            Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_BLUETOOTH + " test3");
            mBTInfoReceiver = new BluetoothInfoReceiver();
        }

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*not needed !
        // BLE check
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }*/

        // BT check
        // already initialised
        // mBTManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBTManager != null) {
            mBTAdapter = mBTManager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // not needed !
/*      // init listview
        ListView deviceListView = (ListView) findViewById(R.id.list);
        deviceListView.setAdapter(mDeviceAdapter);
        stopScan();*/

        mDeviceAdapter = new DeviceAdapter(this,0,
                new ArrayList<ScannedDevice>());

    }

    private void logBluetoothInfo(List<ScannedDevice> scanList){
        // System local time in millis
        long currentMillis = (new Date()).getTime();

        // System nanoseconds since boot, including time spent in sleep.
        long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;
        Log.e(TAG, "Error creating " + Constants.SENSOR_NAME_BLUETOOTH + " test6");
        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", nanoTime) + ";"
                + String.format("%s", mNanosOffset) + ";"
                + scanList.size();

        for (ScannedDevice scan : scanList){
            // Get BLE Beacon Hex Info out of scanRecord
            String scanRecord = scan.getScanRecordHexString();

            message += ";"
                    /*+ scanRecord;*/
                    + "Hallo!";
        }
        logger.log(message);
        logger.log(System.lineSeparator());
    }

    private class BluetoothInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            logBluetoothInfo(mDeviceAdapter.getScanList());
        }
    }

    @Override
    public void start() {
        Log.i(TAG, "start:: Starting listener for sensor: " + getSensorName());

        // if query from iBeaconDetector
        if ((mBTAdapter != null) && (!mIsScanning)) {
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
        }
        // if query from wifi, has been adapted for bluetooth, is currently not working
        /*if (mBTInfoReceiver != null){
            mContext.registerReceiver(mBTInfoReceiver,
                    new IntentFilter( ));//something is missing here
        }else {
            mTimerHandler.postDelayed(mTimerRunnable, 0);
        }*/

        logger.start();
    }

    @Override
    public void stop() {
        Log.i(TAG,"stop:: Stopping listener for sensor " + getSensorName());

        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;

        // if query from wifi, has been adapted for bluetooth, is currently not working
        /*if (mBTInfoReceiver != null) {
            mContext.unregisterReceiver(mBTInfoReceiver);
        } else {
            mTimerHandler.removeCallbacks(mTimerRunnable);
        }*/

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

    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String summary = mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
                if (summary != null) {
                    getActionBar().setSubtitle(summary);
                }
            }
        });
    }
}
