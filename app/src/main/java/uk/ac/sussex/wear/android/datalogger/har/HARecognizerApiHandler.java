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

package uk.ac.sussex.wear.android.datalogger.har;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.io.File;
import java.util.ArrayList;

import uk.ac.sussex.wear.android.datalogger.Constants;
import uk.ac.sussex.wear.android.datalogger.log.HARecognizerApiLogger;

// class for using google api client

public class HARecognizerApiHandler implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = HARecognizerApiHandler.class.getSimpleName();

    /**
     * A receiver for DetectedActivity objects broadcast by the
     * {@code ActivityDetectionIntentService}.
     */
    private HARecognizerBroadcastReceiver mBroadcastReceiver;

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    private HARecognizerApiLogger mHARecognizerApiLogger;

    private Context mContext;

    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life.
     */
    private int mdetectionIntervalHAR;

    public HARecognizerApiHandler(Context context, String sessionName, int detectionIntervalHAR, long nanosOffset, int logFileMaxSize){

        mContext = context;

        mdetectionIntervalHAR = detectionIntervalHAR;

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();

        String path = sessionName + File.separator + Constants.SENSOR_NAME_API_HAR + "_" + sessionName;

        mHARecognizerApiLogger = new HARecognizerApiLogger(mContext,
                path,
                sessionName,
                nanosOffset,
                logFileMaxSize);

        // Get a receiver for broadcasts from HARecognizerApiIntentService.
        mBroadcastReceiver = new HARecognizerBroadcastReceiver();
    }

    public void start(){


        mHARecognizerApiLogger.start();

        mGoogleApiClient.connect();

        // Register the broadcast receiver that informs this activity of the DetectedActivity
        // object broadcast sent by the intent service.
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
    }

    public void stop(){

        //Removes activity recognition updates
        removeActivityUpdates();

        // Unregister the broadcast receiver that was registered
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);

        mGoogleApiClient.disconnect();

        mHARecognizerApiLogger.stop();
    }

    public void haltAndRestartLogging() {
        mHARecognizerApiLogger.stop();
        mHARecognizerApiLogger.resetByteCounter();
        mHARecognizerApiLogger.start();
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle bundle) {
        requestActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * Runs when the result of calling requestActivityUpdates() and removeActivityUpdates() becomes
     * available. Either method can complete successfully or with an error.
     *
     * @param status The Status returned through a PendingIntent when requestActivityUpdates()
     *               or removeActivityUpdates() are called.
     */
    public void onResult(Status status) {
        if (!status.isSuccess()) {
            Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(mContext, HARecognizerApiIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Registers for activity recognition updates using
     * {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates} which
     * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
     * implements the PendingResult interface, the activity itself receives the callback, and the
     * code within {@code onResult} executes. Note: once {@code requestActivityUpdates()} completes
     * successfully, the {@code HARecognizerApiIntentService} starts receiving callbacks when
     * activities are detected.
     */
    private void requestActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                mdetectionIntervalHAR,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    /**
     * Removes activity recognition updates using
     * {@link com.google.android.gms.location.ActivityRecognitionApi#removeActivityUpdates} which
     * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
     * implements the PendingResult interface, the activity itself receives the callback, and the
     * code within {@code onResult} executes. Note: once {@code removeActivityUpdates()} completes
     * successfully, the {@code HARecognizerApiIntentService} stops receiving callbacks about
     * detected activities.
     */
    private void removeActivityUpdates() {
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    public void updateNanosOffset(long nanosOffset) {
        mHARecognizerApiLogger.updateNanosOffset(nanosOffset);
    }

    /**
     * Receiver for intents sent by DetectedActivitiesIntentService via a sendBroadcast().
     * Receives a list of one or more DetectedActivity objects associated with the current state of
     * the device.
     */
    public class HARecognizerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> detectedActivities =
                    intent.getParcelableArrayListExtra(Constants.DETECTED_ACTIVITIES_INTENT_KEY);

            mHARecognizerApiLogger.logDetectedActivities(detectedActivities);
        }
    }

}