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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Broadcast receiver to subclass to create a receiver for {@link UploadService} events.
 *
 * It provides the boilerplate code to properly handle broadcast messages coming from the
 * upload service and dispatch them to the proper handler method.
 *
 * @author gotev (Aleksandar Gotev)
 * @author eliasnaur
 * @author cankov
 * @author mabdurrahman
 *
 */
public class UploadServiceBroadcastReceiver extends BroadcastReceiver
        implements UploadStatusDelegate {

    private static final String TAG = UploadService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !UploadService.getActionBroadcast().equals(intent.getAction()))
            return;

        BroadcastData data = intent.getParcelableExtra(UploadService.PARAM_BROADCAST_DATA);

        if (data == null) {
            Log.e(TAG, "Missing intent parameter: " + UploadService.PARAM_BROADCAST_DATA);
            return;
        }

        switch (data.getStatus()) {
            case ERROR:
                onError(data.getUploadInfo(), data.getException());
                break;

            case COMPLETED:
                onCompleted(data.getUploadInfo(), data.getServerResponse());
                break;

            case IN_PROGRESS:
                onProgress(data.getUploadInfo());
                break;

            case CANCELLED:
                onCancelled(data.getUploadInfo());
                break;

            default:
                break;
        }
    }

    /**
     * Register this upload receiver.<br>
     * If you use this receiver in an {@link android.app.Activity}, you have to call this method inside
     * {@link android.app.Activity#onResume()}, after {@code super.onResume();}.<br>
     * If you use it in a {@link android.app.Service}, you have to
     * call this method inside {@link android.app.Service#onCreate()}, after {@code super.onCreate();}.
     *
     * @param context context in which to register this receiver
     */
    public void register(final Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UploadService.getActionBroadcast());
        context.registerReceiver(this, intentFilter);
    }

    /**
     * Unregister this upload receiver.<br>
     * If you use this receiver in an {@link android.app.Activity}, you have to call this method inside
     * {@link android.app.Activity#onPause()}, after {@code super.onPause();}.<br>
     * If you use it in a {@link android.app.Service}, you have to
     * call this method inside {@link android.app.Service#onDestroy()}.
     *
     * @param context context in which to unregister this receiver
     */
    public void unregister(final Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onProgress(final UploadInfo uploadInfo) {
    }

    @Override
    public void onError(final UploadInfo uploadInfo, final Exception exception) {
    }

    @Override
    public void onCompleted(final UploadInfo uploadInfo, final ServerResponse serverResponse) {
    }

    @Override
    public void onCancelled(final UploadInfo uploadInfo) {
    }
}
