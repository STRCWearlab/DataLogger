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
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.io.File;
import java.util.Date;

import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;

// old child class for collecting data about mobile cells

public class Depr_CellsInfoDataCollector extends AbstractDataCollector {

    private static final String TAG = Depr_CellsInfoDataCollector.class.getSimpleName();

    private CustomLogger logger = null;

    // The telephony manager reference
    private TelephonyManager mTelephonyManager = null;

    // Listener class for monitoring changes in telephony states
    private CellInfoListener mCellInfoListener = null;

    // Timer to manage specific sampling rates
    private Handler mTimerHandler = null;
    private Runnable mTimerRunnable = null;

    public Depr_CellsInfoDataCollector(Context context, String sessionName, String sensorName, long nanosOffset, int logFileMaxSize){

        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        //Object to provide access to information about the telephony services on the device
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // Offset to match timestamps both in master and slaves devices
        mNanosOffset = nanosOffset;

        mCellInfoListener = new CellInfoListener();
    }

    @Override
    public void start() {
        Log.i(TAG, "start:: Starting listener for sensor: " + getSensorName());
        logger.start();
        mTelephonyManager.listen(mCellInfoListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    @Override
    public void stop() {
        Log.i(TAG,"stop:: Stopping listener for sensor " + getSensorName());
        mTelephonyManager.listen(mCellInfoListener, PhoneStateListener.LISTEN_NONE);
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

    private static String networkTypeGeneral(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return "Unknown";
        }
    }

    private String getCellInfoString(SignalStrength signalStrength) {

        // Timestamp in system nanoseconds since boot, including time spent in sleep.
        long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;

        // System local time in millis
        long currentMillis = (new Date()).getTime();

        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", nanoTime) + ";"
                + String.format("%s", mNanosOffset) + ";";

        CellLocation cell = mTelephonyManager.getCellLocation();
        if (cell instanceof GsmCellLocation) {

            int lac, cid, dbm;

            lac = ((GsmCellLocation) cell).getLac();
            cid = ((GsmCellLocation) cell).getCid();

            String generalNetworkType = networkTypeGeneral(mTelephonyManager.getNetworkType());
            // "SignalStrength: mGsmSignalStrength mGsmBitErrorRate mCdmaDbm mCdmaEcio mEvdoDbm
            // mEvdoEcio mEvdoSnr mLteSignalStrength mLteRsrp mLteRsrq mLteRssnr mLteCqi
            // (isGsm ? "gsm|lte" : "cdma"));
            String ssignal = signalStrength.toString();
            String[] parts = ssignal.split(" ");

            // If the signal is not the right signal in db (a signal below -2) fallback
            // Fallbacks will be triggered whenever generalNetworkType changes
            if (generalNetworkType.equals("4G")) {
                dbm = Integer.parseInt(parts[11]);
                if (dbm >= -2) {
                    if (Integer.parseInt(parts[3]) < -2) {
                        dbm = Integer.parseInt(parts[3]);
                    } else {
                        dbm = signalStrength.getGsmSignalStrength();
                    }
                }
            } else if (generalNetworkType.equals("3G")) {
                dbm = Integer.parseInt(parts[3]);
                if (dbm >= -2) {
                    if (Integer.parseInt(parts[11]) < -2) {
                        dbm = Integer.parseInt(parts[11]);
                    } else {
                        dbm = signalStrength.getGsmSignalStrength();
                    }
                }
            } else {
                dbm = signalStrength.getGsmSignalStrength();
                if (dbm >= -2) {
                    if (Integer.parseInt(parts[3]) < -2) {
                        dbm = Integer.parseInt(parts[3]);
                    } else {
                        dbm = Integer.parseInt(parts[11]);
                    }
                }
            }

            // Returns the numeric name (MCC+MNC) of current registered operator.
            String mccMnc = mTelephonyManager.getNetworkOperator();
            String mcc, mnc;
            if (mccMnc != null && mccMnc.length() >= 4) {
                mcc = mccMnc.substring(0, 3);
                mnc = mccMnc.substring(3);
            } else {
                mcc = "NaN";
                mnc = "NaN";
            }

            if (dbm < -2) {
                message += mTelephonyManager.getNetworkType() + ";" + cid + ";" + lac + ";" + dbm + ";" + mcc + ";" + mnc;
            } else {
                message += mTelephonyManager.getNetworkType() + ";" + cid + ";" + lac + ";" + "NaN" + ";" + mcc + ";" + mnc;
            }

        }

//        // Deprecated behavior, not collecting data
//        List<NeighboringCellInfo> neighboringCellInfoList = mTelephonyManager.getNeighboringCellInfo();
//        for (NeighboringCellInfo neighboringCellInfo : neighboringCellInfoList){
//        }

        return message;
    }

    private void logCellInfo(String message){
        if (logger != null) {

            Log.d(TAG,message);

            logger.log(message);
            logger.log(System.lineSeparator());
        }
    }


    /**
     * A listener class for monitoring changes in specific telephony states on the device,
     * including service state, signal strength, message waiting indicator (voicemail), and others.
     */
    private class CellInfoListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
                logCellInfo(getCellInfoString(signalStrength));
            }

    }


}
