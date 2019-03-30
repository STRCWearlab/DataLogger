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

package uk.ac.sussex.wear.android.datalogger.upload;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import uk.ac.sussex.wear.android.datalogger.Constants;
import uk.ac.sussex.wear.android.datalogger.R;
import uk.ac.sussex.wear.android.datalogger.SharedPreferencesHelper;
import uk.ac.sussex.wear.android.datalogger.db.DataLoggerDataSource;


public class FileUploader {

    private static final String TAG = FileUploader.class.getSimpleName();

    private static FileUploader instance = null;

    private final Handler mHandler;
    private final Context mContext;

    private ArrayList<String> mFilesToUpload = null;
    private int nbRemainingFilesUpload, nbBytesUpload, nbFilesToUpload, nbFilesOnError;
    private long uploadedBytes = 0;

    private FileUploader(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    public static FileUploader getInstance(Context context, Handler handler) {
        Log.d(TAG, "::getInstance Creating singleton instance from FileUploader");
        if(instance == null) {
            instance = new FileUploader(context, handler);
        }
        return instance;
    }


    private String getFilename(String filepath) {
        if (filepath == null)
            return null;
        final String[] filepathParts = filepath.split("/");
        return filepathParts[filepathParts.length - 1];
    }


    private UploadNotificationConfig getNotificationConfig(String filename) {

        return new UploadNotificationConfig()
                .setIcon(R.drawable.ic_upload)
                .setCompletedIcon(R.drawable.ic_upload_success)
                .setErrorIcon(R.drawable.ic_upload_error)
                .setTitle(filename)
                .setInProgressMessage(mContext.getString(R.string.upload_notification_second_text))
                .setCompletedMessage(mContext.getString(R.string.upload_success))
                .setErrorMessage(mContext.getString(R.string.upload_error))
                .setAutoClearOnSuccess(true)
                .setAutoClearOnError(true)
                .setClearOnAction(true)
                .setRingToneEnabled(false);
    }

    public void cancelUpload(){
        if (mFilesToUpload != null){
            Log.d(TAG,"::cancelUpload Cancelling " + mFilesToUpload.size() + " UploadTasks");
        } else {
            Log.d(TAG,"::cancelUpload Files list is null");
        }
        UploadService.stopAllUploads();
        onCancelledUpload();
    }

    private void uploadFilesList(){

        // The shared preference that tracks whether the files upload service has started is true
        SharedPreferencesHelper.setUploadServiceState(mContext, true);

        // The persistent value that contains the URL of the server to upload files is retrieved
        String urlServer = SharedPreferencesHelper.getUploadServerURL(mContext);

        // Number of files to upload
        nbFilesToUpload = mFilesToUpload.size();
        nbRemainingFilesUpload = nbFilesToUpload;

        OnProgressUpdate(nbFilesToUpload, nbRemainingFilesUpload, "", 0);

        uploadedBytes = 0;
        nbFilesOnError = 0;

        // Number of bytes to upload
        nbBytesUpload = 0;
        for (String fileToUploadPath : mFilesToUpload){
            nbBytesUpload += new File(fileToUploadPath).length();
        }

        // Each log file is uploaded individually
        for (String fileToUploadPath : mFilesToUpload) {
            try {
                new MultipartUploadRequest(mContext, urlServer)
                        .addFileToUpload(fileToUploadPath, "uploaded_file")
                        .setNotificationConfig(getNotificationConfig(getFilename(fileToUploadPath)))
                        .setUtf8Charset()
                        .setMaxRetries(3)
                        .setDelegate(new UploadStatusDelegate() {
                            @Override
                            public void onProgress(UploadInfo uploadInfo) {
                                OnProgressUpdate(nbFilesToUpload,
                                        nbRemainingFilesUpload,
                                        uploadInfo.getUploadRateString(),
                                        ((uploadedBytes + uploadInfo.getUploadedBytes()) * 100) / nbBytesUpload);
                            }

                            @Override
                            public void onError(UploadInfo uploadInfo, Exception exception) {
                                nbRemainingFilesUpload--;
                                nbFilesOnError++;
                                if (nbRemainingFilesUpload == 0){
                                    checkUploadResult();
                                }
                            }

                            @Override
                            public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                                nbRemainingFilesUpload--;

                                // If the upload was successful
                                if ((serverResponse.getHttpCode() == 200)
                                        && !isUploadingError(serverResponse.getBodyAsString())) {
                                    uploadedBytes += uploadInfo.getTotalBytes();
                                    for (String filePath : uploadInfo.getSuccessfullyUploadedFiles()){
                                        DataLoggerDataSource.markLogFileAsSync(mContext, filePath);
                                    }
                                } else {
                                    nbFilesOnError++;
                                }

                                if (nbRemainingFilesUpload == 0){
                                    checkUploadResult();
                                }
                            }

                            @Override
                            public void onCancelled(UploadInfo uploadInfo) {
                                // called for every UploadTask active when cancelled
                            }

                        })
                        .startUpload();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error. File error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error. Missing some arguments: " + e.getMessage());
            } catch (MalformedURLException e) {
                Log.e(TAG, "Error. URL error: " + e.getMessage());
            }
        }
    }

    private boolean isUploadingError(String result){
        return result.split(":")[0].equals("ERR");
    }

    private void checkUploadResult(){
        if (nbFilesOnError < nbFilesToUpload) {
            onCompletedUpload();
        } else {
            Message msg = mHandler.obtainMessage(Constants.UI_MESSAGE_ERROR_TOAST);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.TOAST, Constants.UI_ERROR_CODE_FILE_UPLOAD);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    private boolean checkFileToUpload(String fileToUpload) throws IllegalArgumentException, FileNotFoundException {
        if (fileToUpload == null || "".equals(fileToUpload))
            throw new IllegalArgumentException("Please specify a file path!");

        File file = new File(fileToUpload);
        if (!file.exists())
            throw new FileNotFoundException("Could not find file at path: " + fileToUpload);
        if (file.isDirectory())
            throw new FileNotFoundException("The specified path refers to a directory: " + fileToUpload);

        return true;
    }

    private void addFilesToUpload(ArrayList<String> filesToUpload){
        for (String fileName : filesToUpload){
            try {
                if (checkFileToUpload(fileName))
                    mFilesToUpload.add(fileName);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error. File error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error. Missing some arguments: " + e.getMessage());
            }
        }
    }

    private void createFilesList()  {

        mFilesToUpload = new ArrayList<>();

        // The files containing labels must be uploaded
        addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_LABELS));

        // The files containing activities recognized from the API must be uploaded
        addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_API_HAR));

        if (SharedPreferencesHelper.toSyncCellsInfo(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_CELL));
        }

        if (SharedPreferencesHelper.toSyncWiFi(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_WIFI));
        }

        if (SharedPreferencesHelper.toSyncBluetooth(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_BLUETOOTH));
        }

        if (SharedPreferencesHelper.toSyncAccelerometer(mContext)) {
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_ACC));
        }

        if (SharedPreferencesHelper.toSyncGyroscope(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_GYR));
        }

        if (SharedPreferencesHelper.toSyncMagnetometer(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_MAG));
        }

        if (SharedPreferencesHelper.toSyncMicrophone(mContext)) {
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_MIC));
        }

        if (SharedPreferencesHelper.toSyncBattery(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_BAT));
        }


        if (SharedPreferencesHelper.toSyncLocation(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_LOC));
        }

        if (SharedPreferencesHelper.toSyncSatellite(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_SAT));
        }

        if (SharedPreferencesHelper.toSyncTemperature(mContext)) {
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_TEMP));
        }

        if (SharedPreferencesHelper.toSyncLight(mContext)) {
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_LT));
        }

        if (SharedPreferencesHelper.toSyncPressure(mContext)) {
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_PRES));
        }

        if (SharedPreferencesHelper.toSyncHumidity(mContext)) {
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_HUM));
        }



        if (SharedPreferencesHelper.toSyncOrientation(mContext)){
            addFilesToUpload(DataLoggerDataSource.selectUnsyncLogFiles(mContext, Constants.SENSOR_NAME_ORIEN));
        }

    }


    public void startUpload() {

        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            createFilesList();
            if (mFilesToUpload.size() > 0) { //if there are log files not syncronized
                uploadFilesList();
            } else {
                Message msg = mHandler.obtainMessage(Constants.UI_MESSAGE_ERROR_TOAST);
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.TOAST, Constants.UI_ERROR_CODE_NO_UNSYNC_FILES);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }

        } else {
            Message msg = mHandler.obtainMessage(Constants.UI_MESSAGE_ERROR_TOAST);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.TOAST, Constants.UI_ERROR_CODE_INTERNET_CONNECTIVITY);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

    }

    private synchronized void onCompletedUpload() {
        // Give the upload state to the Handler so the main Service is aware
        mHandler.obtainMessage(Constants.UPLOAD_MESSAGE_COMPLETED).sendToTarget();
    }

    private synchronized void onCancelledUpload() {
        // Give the upload state to the Handler so the main Service is aware
        mHandler.obtainMessage(Constants.UPLOAD_MESSAGE_CANCELLED).sendToTarget();
    }

    private synchronized void OnProgressUpdate(int nbFilesUpload, int nbRemainingFilesUpload, String uploadRate, long pct) {
        Message msg = mHandler.obtainMessage(Constants.UPLOAD_MESSAGE_ON_PROGRESS);
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.UPLOAD_ONPROGRESS_NUMBER_FILES, nbFilesUpload);
        bundle.putInt(Constants.UPLOAD_ONPROGRESS_NUMBER_FILES_UPLOADED, nbRemainingFilesUpload);
        bundle.putString(Constants.UPLOAD_ONPROGRESS_RATE, uploadRate);
        bundle.putLong(Constants.UPLOAD_ONPROGRESS_PERCENTAGE, pct);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

}
