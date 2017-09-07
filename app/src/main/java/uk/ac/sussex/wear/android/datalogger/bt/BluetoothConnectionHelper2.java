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

package uk.ac.sussex.wear.android.datalogger.bt;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;

import uk.ac.sussex.wear.android.datalogger.bt.multibluetooth.bluetooth.client.BluetoothClient;
import uk.ac.sussex.wear.android.datalogger.bt.multibluetooth.bluetooth.server.BluetoothServer;

public class BluetoothConnectionHelper2 {

    public static final String TAG = BluetoothConnectionHelper2.class.getSimpleName();

    public static final String NAME = "BluetoothConnectionHelper2";

    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private final Context mContext;
    private boolean mIsServer;
    private BluetoothClient mClient = null;
    private BluetoothServer[] mServers = null;
    private int[] mStates;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Unique UUIDs for this application. Randomly generated
    private static final String[] mUUIDs = {
            "70a7c5d0-9aa6-11e6-9f33-a24fc0d9649c",
            "70a7c832-9aa6-11e6-9f33-a24fc0d9649c",
            "70a7c972-9aa6-11e6-9f33-a24fc0d9649c"
    };

    public BluetoothConnectionHelper2(Context context, Handler handler, boolean isServer) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mIsServer = isServer;
        mStates = new int[]{STATE_NONE, STATE_NONE, STATE_NONE};
    }

    public void stop(int indexSocketLost) {

    }

    public void start(int deviceLocation) {

    }

    public void connect(String mMasterAddress, int deviceLocation) {

    }

    public boolean isConnected(int deviceLocation) {
        return false;
    }

    public void broadcastMessage(String broadcastMessage) {

    }

    public int getState(int location) {
        return 0;
    }
}
