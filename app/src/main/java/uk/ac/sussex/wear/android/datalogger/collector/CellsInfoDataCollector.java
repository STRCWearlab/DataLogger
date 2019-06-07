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

package uk.ac.sussex.wear.android.datalogger.collector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.List;

import uk.ac.sussex.wear.android.datalogger.log.CustomLogger;

// child class for collecting data about mobile cells (GSM, CDMA, LTE, WCDMA) in phone's range

public class CellsInfoDataCollector extends AbstractDataCollector {

    private static final String TAG = CellsInfoDataCollector.class.getSimpleName();

    private CustomLogger logger = null;

    private int mSamplingPeriodUs;

    // The telephony manager reference
    private TelephonyManager mTelephonyManager = null;

    // Listener class for monitoring changes in telephony states
    private CellInfoListener mCellInfoListener = null;

    // Timer to manage specific sampling rates
    private Handler mTimerHandler = null;
    private Runnable mTimerRunnable = null;

    private Context mcontext = null;


    public CellsInfoDataCollector(final Context context, String sessionName, String sensorName, int samplingPeriodUs, long nanosOffset, int logFileMaxSize) {

        mcontext = context;

        mSensorName = sensorName;
        String path = sessionName + File.separator + mSensorName + "_" + sessionName;

        // Offset to match timestamps both in master and slaves devices
        mNanosOffset = nanosOffset;

        logger = new CustomLogger(context, path, sessionName, mSensorName, "txt", false, mNanosOffset, logFileMaxSize);

        mSamplingPeriodUs = samplingPeriodUs;

        //Object to provide access to information about the telephony services on the device
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


        /**
         * If the the sampling rate is 0 (default), a listener is registered  for monitoring changes
         * in specific telephony states on the device. If there is a sampling rate defined by the
         * user (sampling rate greater than zero) a timer is defined to check the device state at
         * specific intervals.
         */
        if (mSamplingPeriodUs > 0) {
            mTimerHandler = new Handler();
            mTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    // Returns all observed cell information from all radios on the device including the primary and neighboring cells.
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    logCellInfo(mTelephonyManager.getAllCellInfo());
                    int millis = 1000 / mSamplingPeriodUs;
                    mTimerHandler.postDelayed(this, millis);
                }
            };
        } else {
            mCellInfoListener = new CellInfoListener();
        }

    }

    private void logCellInfo(List<CellInfo> cellInfo) {
        if (logger != null) {
            String message = getCellInfoString(cellInfo);
            logger.log(message);

            Log.d(TAG, message);

            logger.log(System.lineSeparator());
        }
    }

    @Override
    public void start() {
        Log.i(TAG, "start:: Starting listener for sensor: " + getSensorName());
        logger.start();

        if (mCellInfoListener != null) {
            mTelephonyManager.listen(mCellInfoListener, PhoneStateListener.LISTEN_CELL_INFO
                    | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    | PhoneStateListener.LISTEN_CELL_LOCATION);
        } else {
            mTimerHandler.postDelayed(mTimerRunnable, 0);
        }
    }

    @Override
    public void stop() {
        Log.i(TAG, "stop:: Stopping listener for sensor " + getSensorName());

        if (mCellInfoListener != null) {
            mTelephonyManager.listen(mCellInfoListener, PhoneStateListener.LISTEN_NONE);
        } else {
            mTimerHandler.removeCallbacks(mTimerRunnable);
        }

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

    /**
     * A listener class for monitoring changes in specific telephony states on the device,
     * including service state, signal strength, message waiting indicator (voicemail), and others.
     */
    private class CellInfoListener extends PhoneStateListener {

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            super.onCellInfoChanged(cellInfo);

            if (cellInfo == null) return;
            logCellInfo(cellInfo);
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);

            // getAllCellInfo() returns all observed cell information from all radios on the device including the primary and neighboring cells
            // This is preferred over using getCellLocation although for older devices this may return null in which case getCellLocation should be called.
            if (ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            logCellInfo(mTelephonyManager.getAllCellInfo());
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            // getAllCellInfo() returns all observed cell information from all radios on the device including the primary and neighboring cells
            // This is preferred over using getCellLocation although for older devices this may return null in which case getCellLocation should be called.
            if (ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            logCellInfo(mTelephonyManager.getAllCellInfo());
        }
    }


    private String getCellInfoString(List<CellInfo> cellsInfo){

        // Timestamp in system nanoseconds since boot, including time spent in sleep.
        long nanoTime = SystemClock.elapsedRealtimeNanos() + mNanosOffset;

        // System local time in millis
        long currentMillis = (new Date()).getTime();

        String message = String.format("%s", currentMillis) + ";"
                + String.format("%s", nanoTime) + ";"
                + String.format("%s", mNanosOffset) + ";"
                + Integer.toString(cellsInfo.size());

        // The list can include one or more CellInfoGsm, CellInfoCdma, CellInfoLte, and CellInfoWcdma objects, in any combination.
        for (final CellInfo cellInfo : cellsInfo) {
            if (cellInfo instanceof CellInfoGsm) {

                // True if this cell is registered to the mobile network.
                // 0, 1 or more CellInfo objects may return isRegistered() true.
                int isRegistered = (cellInfo.isRegistered()) ? 1 : 0;

                final CellSignalStrengthGsm gsmStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength();

                // Get the signal level as an asu value between 0..31, 99 is unknown Asu is calculated based on 3GPP RSRP.
                int asuLevel = gsmStrength.getAsuLevel();

                // Get the signal strength as dBm
                int dBm = gsmStrength.getDbm();

                // Get signal level as an int from 0..4
                int level = gsmStrength.getLevel();

                final CellIdentityGsm gsmId = ((CellInfoGsm) cellInfo).getCellIdentity();

                // CID Either 16-bit GSM Cell Identity described in TS 27.007, 0..65535, Integer.MAX_VALUE if unknown
                int cid = gsmId.getCid();

                // 16-bit Location Area Code, 0..65535, Integer.MAX_VALUE if unknown
                int lac = gsmId.getLac();

                // 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown
                int mcc = gsmId.getMcc();

                // 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown
                int mnc = gsmId.getMnc();

                message += ";GSM;"
                        + isRegistered + ";"
                        + cid + ";"
                        + lac + ";"
                        + mcc + ";"
                        + mnc + ";"
                        + asuLevel + ";"
                        + dBm + ";"
                        + level;

            } else if (cellInfo instanceof CellInfoCdma) {

                // True if this cell is registered to the mobile network.
                // 0, 1 or more CellInfo objects may return isRegistered() true.
                int isRegistered = (cellInfo.isRegistered()) ? 1 : 0;

                final CellSignalStrengthCdma cdmaStength = ((CellInfoCdma) cellInfo).getCellSignalStrength();

                // Get the signal level as an asu value between 0..97, 99 is unknown
                int asuLevel = cdmaStength.getAsuLevel();

                // Get the CDMA RSSI value in dBm
                int cdmaDbm = cdmaStength.getCdmaDbm();

                // Get the CDMA Ec/Io value in dB*10
                int cdmaEcio = cdmaStength.getCdmaEcio();

                // Get cdma as level 0..4
                int cdmaLevel = cdmaStength.getCdmaLevel();

                // Get the signal strength as dBm
                int dBm = cdmaStength.getDbm();

                // Get the EVDO RSSI value in dBm
                int evdoDbm = cdmaStength.getEvdoDbm();

                // Get the EVDO Ec/Io value in dB*10
                int evdoEcio = cdmaStength.getEvdoEcio();

                // Get Evdo as level 0..4
                int evdoLevel = cdmaStength.getEvdoLevel();

                // Get the signal to noise ratio.
                int evdoSnr = cdmaStength.getEvdoSnr();

                // Get signal level as an int from 0..4
                int level = cdmaStength.getLevel();

                final CellIdentityCdma cdmaId = ((CellInfoCdma) cellInfo).getCellIdentity();

                // Base Station Id 0..65535, Integer.MAX_VALUE if unknown
                int basestationId = cdmaId.getBasestationId();

                // Base station latitude, which is a decimal number as specified in 3GPP2 C.S0005-A v6.0.
                // It is represented in units of 0.25 seconds and ranges from -1296000 to 1296000, both
                // values inclusive (corresponding to a range of -90 to +90 degrees). Integer.MAX_VALUE if unknown.
                int latitude = cdmaId.getLatitude();

                // Base station longitude, which is a decimal number as specified in 3GPP2 C.S0005-A v6.0.
                // It is represented in units of 0.25 seconds and ranges from -2592000 to 2592000, both
                // values inclusive (corresponding to a range of -180 to +180 degrees). Integer.MAX_VALUE if unknown.
                int longitude = cdmaId.getLongitude();

                // Network Id 0..65535, Integer.MAX_VALUE if unknown
                int networkId = cdmaId.getNetworkId();

                // System Id 0..32767, Integer.MAX_VALUE if unknown
                int systemId = cdmaId.getSystemId();

                message += ";CDMA;"
                        + isRegistered + ";"
                        + basestationId + ";"
                        + latitude + ";"
                        + longitude + ";"
                        + networkId + ";"
                        + systemId + ";"
                        + asuLevel + ";"
                        + cdmaDbm + ";"
                        + cdmaEcio + ";"
                        + cdmaLevel + ";"
                        + dBm + ";"
                        + evdoDbm + ";"
                        + evdoEcio + ";"
                        + evdoLevel + ";"
                        + evdoSnr + ";"
                        + level;

            } else if (cellInfo instanceof CellInfoLte) {

                // True if this cell is registered to the mobile network.
                // 0, 1 or more CellInfo objects may return isRegistered() true.
                int isRegistered = (cellInfo.isRegistered()) ? 1 : 0;

                final CellSignalStrengthLte lteStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();

                // Get the LTE signal level as an asu value between 0..97, 99 is unknown Asu is calculated based on 3GPP RSRP.
                // Represents a normalized version of the signal strength as dBm
                int asuLevel = lteStrength.getAsuLevel();

                // Get the signal strength as dBm
                // Returns RSRP – Reference Signal Received Power
                int dBm = lteStrength.getDbm();

                // Get signal level as an int from 0..4
                int level = lteStrength.getLevel();

                final CellIdentityLte lteId = ((CellInfoLte) cellInfo).getCellIdentity();

                // 28-bit Cell Identity, Integer.MAX_VALUE if unknown
                int ci = lteId.getCi();

                // 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown
                int mcc = lteId.getMcc();

                // 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown
                int mnc = lteId.getMnc();

                // Physical Cell Id 0..503, Integer.MAX_VALUE if unknown
                int pci = lteId.getPci();

                // 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown
                int tac = lteId.getTac();

                message += ";LTE;"
                        + isRegistered + ";"
                        + ci + ";"
                        + mcc + ";"
                        + mnc + ";"
                        + pci + ";"
                        + tac + ";"
                        + asuLevel + ";"
                        + dBm + ";"
                        + level;

                // 13 129108338 114 -1
                //LTE;129108338;234;10;384;144;39;-101;3

            } else if (cellInfo instanceof CellInfoWcdma) {

                // True if this cell is registered to the mobile network.
                // 0, 1 or more CellInfo objects may return isRegistered() true.
                int isRegistered = (cellInfo.isRegistered()) ? 1 : 0;

                final CellSignalStrengthWcdma wcdmaStrength = ((CellInfoWcdma) cellInfo).getCellSignalStrength();

                // Get the signal level as an asu value between 0..31, 99 is unknown Asu is calculated based on 3GPP RSRP.
                int asuLevel = wcdmaStrength.getAsuLevel();

                // Get the signal strength as dBm
                int dBm = wcdmaStrength.getDbm();

                // Get signal level as an int from 0..4
                int level = wcdmaStrength.getLevel();

                final CellIdentityWcdma wcdmaId = ((CellInfoWcdma) cellInfo).getCellIdentity();

                // CID 28-bit UMTS Cell Identity described in TS 25.331, 0..268435455, Integer.MAX_VALUE if unknown
                int cid = wcdmaId.getCid();

                // 16-bit Location Area Code, 0..65535, Integer.MAX_VALUE if unknown
                int lac = wcdmaId.getLac();

                // 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown
                int mcc = wcdmaId.getMcc();

                // 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown
                int mnc = wcdmaId.getMnc();

                // 9-bit UMTS Primary Scrambling Code described in TS 25.331, 0..511, Integer.MAX_VALUE if unknown
                int psc = wcdmaId.getPsc();

                message += ";WCDMA;"
                        + isRegistered + ";"
                        + cid + ";"
                        + lac + ";"
                        + mcc + ";"
                        + mnc + ";"
                        + psc + ";"
                        + asuLevel + ";"
                        + dBm + ";"
                        + level;

            } else {
                Log.e(TAG, "Unknown type of cell signal" );
            }
        }
        return message;
    }

}
