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

package uk.ac.sussex.wear.android.datalogger.data;

import java.util.Iterator;

import uk.ac.sussex.wear.android.datalogger.R;

// child class for keep alive commands

public class CommandKA extends CommandBase {

    private boolean mDataCollectionState;
    private String mMasterSessionId;
    private long mNanosOffset;
    private boolean mLabelsAnnotationState;
    private int mActivityLabel;
    private int mBodyPositionLabel;
    private int mLocationLabel;

    public CommandKA() {
        this(false, "", -1, false, -1, -1, R.id.ui_iolocation_radioButton_outside);
    }

    public CommandKA(boolean dataCollectionState, String masterSessionId, long nanosOffset, boolean labelsAnnotationState, int activityLabel, int bodyPositionLabel, int locationLabel) {
        mDataCollectionState = dataCollectionState;
        mMasterSessionId = masterSessionId;
        mNanosOffset = nanosOffset;
        mLabelsAnnotationState = labelsAnnotationState;
        mActivityLabel = activityLabel;
        mBodyPositionLabel = bodyPositionLabel;
        mLocationLabel = locationLabel;
    }

    public boolean getDataCollectionState(){ return mDataCollectionState; }
    public String getMasterSessionId(){ return mMasterSessionId; }
    public long getNanosOffset(){ return mNanosOffset; }
    public boolean getLabelsAnnotationState(){ return mLabelsAnnotationState; }
    public int getActivityLabel(){ return mActivityLabel; }
    public int getBodyPositionLabel(){ return mBodyPositionLabel; }
    public int getLocationLabel(){ return mLocationLabel; }

    public Iterator<String> getParams(Iterator<String> itr) throws Exception {
        //Processing data collection event
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_KEEP_ALIVE_EVENT + "' is malformed or missing parameters");
        mDataCollectionState = Boolean.parseBoolean(itr.next());
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_KEEP_ALIVE_EVENT + "' is malformed or missing parameters");
        mMasterSessionId = itr.next();
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_KEEP_ALIVE_EVENT + "' is malformed or missing parameters");
        mNanosOffset = Long.parseLong(itr.next());

        //Processing label annotation event
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_KEEP_ALIVE_EVENT + "' is malformed or missing parameters");
        mLabelsAnnotationState = Boolean.parseBoolean(itr.next());
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_KEEP_ALIVE_EVENT + "' is malformed or missing parameters");
        mActivityLabel = Integer.parseInt(itr.next());
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_KEEP_ALIVE_EVENT + "' is malformed or missing parameters");
        mBodyPositionLabel = Integer.parseInt(itr.next());
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_KEEP_ALIVE_EVENT + "' is malformed or missing parameters");
        mLocationLabel = Integer.parseInt(itr.next());

        return itr;
    }

    @Override
    public String getMessage() {
        return COMMAND_START
                + COMMAND_KEEP_ALIVE_EVENT + COMMAND_SEPARATOR
                + Boolean.toString(mDataCollectionState) + PARAMETER_SEPARATOR
                + mMasterSessionId + PARAMETER_SEPARATOR
                + Long.toString(mNanosOffset) + PARAMETER_SEPARATOR
                + Boolean.toString(mLabelsAnnotationState) + PARAMETER_SEPARATOR
                + Integer.toString(mActivityLabel) + PARAMETER_SEPARATOR
                + Integer.toString(mBodyPositionLabel) + PARAMETER_SEPARATOR
                + Integer.toString(mLocationLabel) + PARAMETER_SEPARATOR;
    }
}
