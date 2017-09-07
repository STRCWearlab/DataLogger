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
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import uk.ac.sussex.wear.android.datalogger.db.DataLoggerDataSource;
import uk.ac.sussex.wear.android.datalogger.log.LoggerHelper;


public class AudioDataCollector extends AbstractDataCollector {

    private static final String TAG = AudioDataCollector.class.getSimpleName();

    private int mSamplingRate;

    private Context mContext;

    private MediaRecorder mRecorder = null;

    private String mPath;

    private String mBaseLogFilename;

    private String mSessionName;

    private File logFile;

    public AudioDataCollector(Context context, String sessionName, String sensorName, int samplingRate, long nanosOffset) {

        mSensorName = sensorName;
        mSessionName = sessionName;
        mPath = sessionName+ File.separator + mSensorName + "_" + sessionName;
        mBaseLogFilename = sessionName + "_" + mSensorName;

        mSamplingRate = samplingRate;

        mContext = context;

        // Offset to match timestamps both in master and slaves devices
        mNanosOffset = nanosOffset;

    }

    public void prepare(int samplingRate) {
        Log.i(TAG,"prepare:: Preparing listener for sensor "+getSensorName());
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        if (samplingRate != 0){
            mRecorder.setAudioSamplingRate(samplingRate);
        }
        logFile = LoggerHelper.defineLogFilename(mContext, mPath, mBaseLogFilename, "3gp", false, mNanosOffset);
        mRecorder.setOutputFile(logFile.getAbsolutePath());
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "::prepare Error creating collector: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        prepare(mSamplingRate);
        Log.i(TAG, "start:: Starting listener for sensor: "+getSensorName());
        mRecorder.start();
    }

    @Override
    public void stop() {
        Log.i(TAG,"stop:: Stopping listener for sensor "+getSensorName());
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch(RuntimeException ex) {
                Log.d(TAG,"No valid audio data has been received when stop() is called");
            }
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            DataLoggerDataSource.insertLogFile(mContext, logFile.getAbsolutePath(), getSensorName(), mSessionName);
        }
    }

    @Override
    public void haltAndRestartLogging() {
        stop();
        start();
    }

    @Override
    public void updateNanosOffset(long nanosOffset) {
        mNanosOffset = nanosOffset;
    }

}
