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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.List;

import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;


public class WiFiDataCollector extends AbstractDataCollector {

    private static final String TAG = WiFiDataCollector.class.getSimpleName();

    private CustomLogger logger = null;

    private int mSamplingPeriodUs;

    private Context mContext;

    // The WiFi manager reference
    private WifiManager mWifiManager = null;

    // Receiver class for monitoring changes in WiFi states
    private WiFiInfoReceiver mWiFiInfoReceiver;

    // Timer to manage specific sampling rates
    private Handler mTimerHandler = null;
    private Runnable mTimerRunnable = null;

    public WiFiDataCollector(Context context, String sessionName, String sensorName, int samplingPeriodUs, long nanosOffset, int logFileMaxSize) {

        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        mSamplingPeriodUs = samplingPeriodUs;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mContext = context;

        // Offset to match timestamps both in master and slaves devices
        mNanosOffset = nanosOffset;

        if (mSamplingPeriodUs > 0) {
            mTimerHandler = new Handler();
            mTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    logWifiInfo(getScanList());
                    int millis = 1000 / mSamplingPeriodUs;
                    mTimerHandler.postDelayed(this, millis);
                }
            };
        } else {
            mWiFiInfoReceiver = new WiFiInfoReceiver();
        }

    }

    private List<ScanResult> getScanList(){
        synchronized (this) {
            return mWifiManager.getScanResults();
        }
    }

    private void logWifiInfo(List<ScanResult> scanList){
        // System local time in millis
        long currentMillis = (new Date()).getTime();

        // System nanoseconds since boot, including time spent in sleep.
        long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;

        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", nanoTime) + ";"
                + String.format("%s", mNanosOffset) + ";"
                + scanList.size();

        for (ScanResult scan : scanList){

            // The address of the access point.
            String BSSID = scan.BSSID;

            // The network name.
            String SSID = scan.SSID;

            // The detected signal level in dBm, also known as the RSSI.
            int level = scan.level;

            // The primary 20 MHz frequency (in MHz) of the channel over which the client is communicating with the access point.
            int frequency = scan.frequency;

            // Describes the authentication, key management, and encryption schemes supported by the access point.
            String capabilities = scan.capabilities;

            message += ";"
                    + BSSID + ";"
                    + SSID + ";"
                    + level + ";"
                    + frequency + ";"
                    + capabilities;
        }

        logger.log(message);
        logger.log(System.lineSeparator());
    }

    private class WiFiInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            logWifiInfo(getScanList());
        }
    }

    @Override
    public void start(){
        Log.i(TAG, "start:: Starting listener for sensor: " + getSensorName());

        if (mWiFiInfoReceiver != null){
            mContext.registerReceiver(mWiFiInfoReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        } else {
            mTimerHandler.postDelayed(mTimerRunnable, 0);
        }
        logger.start();
    }

    @Override
    public void stop(){
        Log.i(TAG,"stop:: Stopping listener for sensor " + getSensorName());

        if (mWiFiInfoReceiver != null) {
            mContext.unregisterReceiver(mWiFiInfoReceiver);
        } else {
            mTimerHandler.removeCallbacks(mTimerRunnable);
        }

        logger.stop();
    }

    @Override
    public void haltAndRestartLogging(){
        logger.stop();
        logger.resetByteCounter();
        logger.start();
    }

    @Override
    public void updateNanosOffset(long nanosOffset) {
        mNanosOffset = nanosOffset;
    }

}
