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

import android.util.Log;

import java.util.Iterator;

import uk.ac.sussex.wear.android.datalogger.R;

public class CommandLAE extends CommandBase {

    private static final String TAG = CommandLAE.class.getSimpleName();

    private boolean mState;
    private int mActivityLabel;
    private int mPositionLabel;
    private int mLocationLabel;
    private boolean mNextState;
    private int mNextActivityLabel;
    private int mNextPositionLabel;
    private int mNextLocationLabel;

    public CommandLAE(){
        this(false, -1, -1, R.id.ui_iolocation_radioButton_outside);
    }

    public CommandLAE(boolean state){
        this(state, -1, -1, R.id.ui_iolocation_radioButton_outside);
    }

    public CommandLAE(boolean state, int activityLabel, int positionLabel, int locationLabel){
        mState = state;
        if (state && (activityLabel == -1))
            Log.e(TAG,"::CommandLAE Missing activity label with label annotation event = "+ state);
        mActivityLabel = activityLabel;
        mPositionLabel = positionLabel;
        mLocationLabel = locationLabel;
        mNextState = false;
        mNextActivityLabel = -1;
        mNextPositionLabel = -1;
        mNextLocationLabel = R.id.ui_iolocation_radioButton_outside;
    }

    public boolean getState(){
        return mState;
    }

    public int getActivityLabel(){
        return mActivityLabel;
    }

    public int getPositionLabel(){
        return mPositionLabel;
    }

    public int getLocationLabel(){
        return mLocationLabel;
    }

    public boolean getNextState(){
        return mNextState;
    }

    public int getNextActivityLabel(){
        return mNextActivityLabel;
    }

    public int getNextPositionLabel(){
        return mNextPositionLabel;
    }

    public int getNextLocationLabel(){
        return mNextLocationLabel;
    }

    public CommandLAE setStopAndStartEvent(int nextActivityLabel, int nextPositionLabel, int nextLocationLabel){
        if (mState)
            Log.e(TAG,"::CommandLAE Wrong event in stop and start event. First label should be false");
        mNextActivityLabel = nextActivityLabel;
        mNextPositionLabel = nextPositionLabel;
        mNextLocationLabel = nextLocationLabel;
        return this;
    }

    public Iterator<String> setParams(Iterator<String> itr) throws Exception {
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_LABEL_ANNOTATION_EVENT + "' is malformed or missing parameters");
        mState = Boolean.parseBoolean(itr.next());
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_LABEL_ANNOTATION_EVENT + "' is malformed or missing parameters");
        mActivityLabel = Integer.parseInt(itr.next());
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_LABEL_ANNOTATION_EVENT + "' is malformed or missing parameters");
        mPositionLabel = Integer.parseInt(itr.next());
        if (!itr.hasNext())
            throw new Exception("Command '" + COMMAND_LABEL_ANNOTATION_EVENT + "' is malformed or missing parameters");
        mLocationLabel = Integer.parseInt(itr.next());
        if (itr.hasNext()){
            mNextState = Boolean.parseBoolean(itr.next());
            if (!itr.hasNext())
                throw new Exception("Command '" + COMMAND_LABEL_ANNOTATION_EVENT + "' is malformed or missing parameters");
            mNextActivityLabel = Integer.parseInt(itr.next());
            if (!itr.hasNext())
                throw new Exception("Command '" + COMMAND_LABEL_ANNOTATION_EVENT + "' is malformed or missing parameters");
            mNextPositionLabel = Integer.parseInt(itr.next());
            if (!itr.hasNext())
                throw new Exception("Command '" + COMMAND_LABEL_ANNOTATION_EVENT + "' is malformed or missing parameters");
            mNextLocationLabel = Integer.parseInt(itr.next());
        }
        return itr;
    }

    @Override
    public String getMessage() {
        String ret = COMMAND_START
                + COMMAND_LABEL_ANNOTATION_EVENT + COMMAND_SEPARATOR
                + Boolean.toString(mState) + PARAMETER_SEPARATOR
                + Integer.toString(mActivityLabel) + PARAMETER_SEPARATOR
                + Integer.toString(mPositionLabel) + PARAMETER_SEPARATOR
                + Integer.toString(mLocationLabel) + PARAMETER_SEPARATOR;
        if (mNextActivityLabel != -1){
            ret += Boolean.toString(true) + PARAMETER_SEPARATOR
                    + Integer.toString(mNextActivityLabel) + PARAMETER_SEPARATOR
                    + Integer.toString(mNextPositionLabel) + PARAMETER_SEPARATOR
                    + Integer.toString(mNextLocationLabel) + PARAMETER_SEPARATOR;
        }
        return ret;
    }

}
