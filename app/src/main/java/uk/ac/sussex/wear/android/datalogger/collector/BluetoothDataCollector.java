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
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import uk.ac.sussex.wear.android.datalogger.R;
import uk.ac.sussex.wear.android.datalogger.collector.BTHelper.DeviceAdapter;
import uk.ac.sussex.wear.android.datalogger.collector.BTHelper.ScannedDevice;
import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;
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

    @SuppressLint("ServiceCast")
    public BluetoothDataCollector(Context context, String sessionName, String sensorName, int samplingPeriodUs, long nanosOffset, int logFileMaxSize) {

        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        mSamplingPeriodUs = samplingPeriodUs;
        mContext = context;
        // Offset to match timestamps both in master and slaves devices
        mNanosOffset = nanosOffset;
        // call init
        init();
        if (mSamplingPeriodUs > 0) {
            mTimerHandler = new Handler();
            mTimerRunnable = new Runnable() {
                // still problems here !!! run() seems to not work properly. Log doesn't get called
                // no BT txt files are written
                @Override
                public void run() {
                    logBluetoothInfo(mDeviceAdapter.getScanList());
                    int millis = 1000 / mSamplingPeriodUs;
                    mTimerHandler.postDelayed(this, millis);
                }
            };
        } else {
            mBTInfoReceiver = new BluetoothInfoReceiver();
        }

    }

    private void init(){
        // initialise mBTAdapter
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDeviceAdapter = new DeviceAdapter(new ArrayList<ScannedDevice>());
    }

    private void logBluetoothInfo(List<ScannedDevice> scanList){
        // System local time in millis
        long currentMillis = (new Date()).getTime();

        // System nanoseconds since boot, including time spent in sleep.
        long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;

        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", nanoTime) + ";"
                + String.format("%s", mNanosOffset) + ";"
                + scanList.size();

        for (ScannedDevice scan : scanList){
            // Get BLE Beacon Device Address
            String address = scan.getDevice().getAddress();
            // Get BLE Beacon RSSI
            int rssi = scan.getRssi();
            // Get BLE Beacon Hex Info out of scanRecord
            String scanRecord = scan.getScanRecordHexString();

            message += ";"
                    + address + ";"
                    + rssi + ";"
                    + scanRecord;
        }
        //message += "Hello";
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

        mTimerHandler.postDelayed(mTimerRunnable, 0);

        logger.start();
    }

    @Override
    public void stop() {
        Log.i(TAG,"stop:: Stopping listener for sensor " + getSensorName());

        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;

        mTimerHandler.removeCallbacks(mTimerRunnable);

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
    public void onLeScan(final BluetoothDevice newDevice, final int newRssi, final byte[] newScanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String summary = mDeviceAdapter.updateDevice(newDevice, newRssi, newScanRecord);
            }
        });
    }
}
