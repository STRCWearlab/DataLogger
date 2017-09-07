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

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Class which contains all the data passed in broadcast intents to notify task progress, errors,
 * completion or cancellation.
 *
 * @author gotev (Aleksandar Gotev)
 */
class BroadcastData implements Parcelable {

    private static final String TAG = BroadcastData.class.getSimpleName();

    public enum Status {
        IN_PROGRESS,
        ERROR,
        COMPLETED,
        CANCELLED
    }

    private Status status;
    private Exception exception;
    private UploadInfo uploadInfo;
    private ServerResponse serverResponse;

    public BroadcastData() {

    }

    public Intent getIntent() {
        Intent intent = new Intent(UploadService.getActionBroadcast());
        intent.putExtra(UploadService.PARAM_BROADCAST_DATA, this);
        return intent;
    }

    // This is used to regenerate the object.
    // All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<BroadcastData> CREATOR =
            new Parcelable.Creator<BroadcastData>() {
                @Override
                public BroadcastData createFromParcel(final Parcel in) {
                    return new BroadcastData(in);
                }

                @Override
                public BroadcastData[] newArray(final int size) {
                    return new BroadcastData[size];
                }
            };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(status.ordinal());
        parcel.writeSerializable(exception);
        parcel.writeParcelable(uploadInfo, flags);
        parcel.writeParcelable(serverResponse, flags);
    }

    private BroadcastData(Parcel in) {
        status = Status.values()[in.readInt()];
        exception = (Exception) in.readSerializable();
        uploadInfo = in.readParcelable(UploadInfo.class.getClassLoader());
        serverResponse = in.readParcelable(ServerResponse.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Status getStatus() {
        if (status == null) {
            Log.e(TAG, "Status not defined! Returning " + Status.CANCELLED);
            return Status.CANCELLED;
        }

        return status;
    }

    public BroadcastData setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public BroadcastData setException(Exception exception) {
        this.exception = exception;
        return this;
    }

    public UploadInfo getUploadInfo() {
        return uploadInfo;
    }

    public BroadcastData setUploadInfo(UploadInfo uploadInfo) {
        this.uploadInfo = uploadInfo;
        return this;
    }

    public ServerResponse getServerResponse() {
        return serverResponse;
    }

    public BroadcastData setServerResponse(ServerResponse serverResponse) {
        this.serverResponse = serverResponse;
        return this;
    }
}