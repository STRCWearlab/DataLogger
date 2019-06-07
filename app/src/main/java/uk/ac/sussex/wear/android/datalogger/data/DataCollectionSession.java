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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fjordonez on 01/10/16.
 */

// class for data collection session

public class DataCollectionSession {
    private Date mStartDate;
    private Date mEndDate;
    private String mDateFormat;
    private String mUserName;
    private String mDeviceLocation;
    private long mMillisSignature;
    private String mMasterSessionId;
    private long mNanosOffset;
    private long mLength;
    private static String dateFormatISO8601 = "yyyy-MM-dd HH:mm:ss.SSS";

    public DataCollectionSession(Date startDate, String dateFormat, String userName, String deviceLocation, long millisSignature, String masterSessionId, long nanosOffset){
        mStartDate = startDate;
        mEndDate = null;
        mDateFormat = dateFormat;
        mUserName = userName;
        mDeviceLocation = deviceLocation;
        mMillisSignature = millisSignature;
        mMasterSessionId = masterSessionId;
        mNanosOffset = nanosOffset;
        mLength = 0;
    }

    public String getUserName(){
        return mUserName;
    }

    public String getDeviceLocation(){
        return mDeviceLocation;
    }

    public Date getStartDate(){
        return mStartDate;
    }

    public Date getEndDate(){
        return mEndDate;
    }

    public long getMillisSignature(){
        return mMillisSignature;
    }

    public long getLength(){
        return mLength;
    }

    public long getNanosOffset() {
        return mNanosOffset;
    }

    public void setEndDate(Date endDate){
        mEndDate = endDate;
        setLength((mEndDate.getTime()-mStartDate.getTime())/1000);
    }

    public void setLength(long length){
        mLength = length;
    }

    public String getSessionId(){
        return new SimpleDateFormat(mDateFormat).format(getStartDate())
               + String.valueOf(getMillisSignature() % 100000);
    }

    public String getSessionName(){
        String ret = getUserName() + "_"
             + getDeviceLocation() + "_"
             + getSessionId();
        ret += ("".equals(mMasterSessionId)) ? "" : "_" + mMasterSessionId;
        return ret;
    }

    public String getStartDateISO8601(){
        return new SimpleDateFormat(dateFormatISO8601).format(getStartDate());
    }

    public String getEndDateISO8601(){
        if (getEndDate() != null){
            return new SimpleDateFormat(dateFormatISO8601).format(getEndDate());
        }else{
            return "";
        }
    }

}