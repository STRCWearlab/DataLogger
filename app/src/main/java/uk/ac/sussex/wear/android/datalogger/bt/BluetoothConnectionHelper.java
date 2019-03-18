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
 *//*


package uk.ac.sussex.wear.android.datalogger.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import uk.ac.sussex.wear.android.datalogger.Constants;
import uk.ac.sussex.wear.android.datalogger.R;
import uk.ac.sussex.wear.android.datalogger.SharedPreferencesHelper;
import uk.ac.sussex.wear.android.datalogger.data.CommandBase;
import uk.ac.sussex.wear.android.datalogger.data.CommandKA;

public class BluetoothConnectionHelper {

    private static final String TAG = BluetoothConnectionHelper.class.getSimpleName();

    private static final String NAME = "BluetoothConnectionHelper";

    // Member fields
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private final Context mContext;
    private AcceptThread[] mAcceptThreads = null;
    private ConnectThread mConnectThread = null;
    private ConnectedThread[] mConnectedThreads = null;
    private KeepAlive[] mKeepAlives = null;
    private int[] mStates;
    private boolean mIsServer;
    private Runnable keepAliveRunnable = null;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Unique UUIDs for this application. Randomly generated
    private static final String[] mUUIDs  = {
            "70a7c5d0-9aa6-11e6-9f33-a24fc0d9649c",
            "70a7c832-9aa6-11e6-9f33-a24fc0d9649c",
            "70a7c972-9aa6-11e6-9f33-a24fc0d9649c"
    };


    public BluetoothConnectionHelper(Context context, Handler handler, boolean isServer) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mIsServer = isServer;
        mStates = new int[]{STATE_NONE, STATE_NONE, STATE_NONE};
        mConnectedThreads = new ConnectedThread[]{null, null, null};
        mKeepAlives = new KeepAlive[]{null, null, null};
        if (isServer) {
            mAcceptThreads = new AcceptThread[]{null, null, null};
        }
    }

    */
/**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     *//*

    public synchronized void start(int index) {
        Log.d(TAG, "::start Starting bluetooth at index " + index);

        if (keepAliveRunnable != null) {
            mHandler.removeCallbacks(keepAliveRunnable);
            keepAliveRunnable = null;
        }
        keepAliveRunnable = new KeepAliveRunnable(index);

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThreads[index] != null) {
            mConnectedThreads[index].cancel();
            mConnectedThreads[index] = null;
        }

        if (mIsServer) {
            setState(STATE_LISTEN, index);

            if (mKeepAlives[index] != null) {
                mKeepAlives[index].cancel();
                mKeepAlives[index] = null;
            }

            // Start the thread to listen on a BluetoothServerSocket
            if (mAcceptThreads[index] == null) {
                mAcceptThreads[index] = new AcceptThread(index);
                mAcceptThreads[index].start();
            }

//            try{
//                Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
//                Log.i(TAG, "::start Supported features (UUIDs) of the local device are:");
//                for (ParcelUuid uuid : (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null)){
//                    Log.i(TAG, "::start "+ uuid.toString() + ". Is socket uuid: " + mUUIDs[index].equals(uuid.toString()));
//                }
//            } catch (Exception e){
//                Log.e(TAG,"::start ");
//            }

        }

    }

    */
/**
     * Stop all threads
     *//*

    public synchronized void stop(int index) {
        Log.d(TAG, "::stop Stopping bluetooth slot "+index);

        if (keepAliveRunnable != null) {
            mHandler.removeCallbacks(keepAliveRunnable);
            keepAliveRunnable = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThreads[index] != null) {
            mConnectedThreads[index].cancel();
            mConnectedThreads[index] = null;
        }

        if (mIsServer) {
            if (mAcceptThreads[index] != null) {
                mAcceptThreads[index].cancel();
                mAcceptThreads[index] = null;
            }

            if (mKeepAlives[index] != null) {
                mKeepAlives[index].cancel();
                mKeepAlives[index] = null;
            }
        }

        setState(STATE_NONE, index);
    }

    */
/**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     *//*

    private synchronized void setState(int state, int index) {
        Log.d(TAG, "setState("+index+") " + mStates[index] + " -> " + state);
        mStates[index] = state;

        // Give the new state to the Handler so the main Service is aware
        mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE, state, index).sendToTarget();
    }

    */
/**
     * Return the current connection state.
     *//*

    public synchronized int getState(int index) {
        return mStates[index];
    }

    public synchronized  boolean isConnected(int index) {
        if (mConnectedThreads[index] == null)
            return false;

        return mConnectedThreads[index].isConnected();
    }


    */
/**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     *//*

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, int index) {
        Log.d(TAG, "::connected Starting ConnectedThread for bluetooth at index "+index);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThreads[index] != null) {
            mConnectedThreads[index].cancel();
            mConnectedThreads[index] = null;
        }

        if (mIsServer) {
            if (mAcceptThreads[index] != null) {
                mAcceptThreads[index].cancel();
                mAcceptThreads[index] = null;
            }

            if (mKeepAlives[index] != null) {
                mKeepAlives[index].cancel();
                mKeepAlives[index] = null;
            }
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThreads[index] = new ConnectedThread(socket, index);
        mConnectedThreads[index].start();

        // If the keep alive mechanism is active
        if (SharedPreferencesHelper.isEnabledKeepalive(mContext)) {
            if (mIsServer) { // the master creates a thread to send KA messages at regular intervals
                mKeepAlives[index] = new KeepAlive(index);
                mKeepAlives[index].start();
            } else { // slaves post delayed runnables in the main looper to check for KA messages
                int slaveKA = SharedPreferencesHelper.getSlaveKeepaliveInterval(mContext);
                Log.d(TAG, "::connected Posting keepalive runnable with delay " + slaveKA + " millis at index "+ index);
                mHandler.postDelayed(keepAliveRunnable, slaveKA);
            }
        }

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_DEVICE_ADDRESS);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BLUETOOTH_CONNECTED_DEVICE_ADDRESS, device.getAddress());
        bundle.putInt(Constants.BLUETOOTH_CONNECTED_DEVICE_LOCATION, index);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED, index);
    }

    */
/**
     * Indicate that the connection attempt failed and notify the UI Activity.
     *//*

    private synchronized void connectionFailed(int index) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_CONNECTION_FAILED, index);
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.BLUETOOTH_CONNECTED_DEVICE_LOCATION, index);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    */
/**
     * Indicate that the connection was lost and notify the UI Activity.
     *//*

    private synchronized void connectionLost(int index) {
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_CONNECTION_LOST);
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.BLUETOOTH_CONNECTED_DEVICE_LOCATION, index);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    */
/**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param address The address to connect
     *//*

    public synchronized void connect(String address, int index) {
        Log.i(TAG, "::connect Trying to connect to address " + address + ". ");

        // Cancel any thread attempting to make a connection
        if (mStates[index] == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThreads[index] != null) {
            mConnectedThreads[index].cancel();
            mConnectedThreads[index] = null;
        }

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        switch (device.getBondState()){
            case BluetoothDevice.BOND_NONE:
                Log.i(TAG, "The remote device is not bonded (paired)");
                break;
            case BluetoothDevice.BOND_BONDING:
                Log.i(TAG, "Bonding (pairing) is in progress with the remote device");
                break;
            case BluetoothDevice.BOND_BONDED:
                Log.i(TAG, "The remote device is bonded (paired)");
                boolean remoteUuid = false;
                for (ParcelUuid uuid : device.getUuids()){
                    if (mUUIDs[index].equals(uuid.toString())) {
                        remoteUuid = true;
                    }
                    Log.i(TAG, "::connect "+ uuid.toString() + ". Is target uuid: " + mUUIDs[index].equals(uuid.toString()));
                }
                if (!remoteUuid) {
                    Log.e(TAG, "::connect Service UUID (" + mUUIDs[index] + ") not supported in remote device.");
                }
                break;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, index);
        mConnectThread.start();
        setState(STATE_CONNECTING, index);

    }

    public void broadcastMessage(String message){

        for (int i=0; i<mUUIDs.length; i++){
            sendMessage(message, i);
        }
    }


    public synchronized void sendMessage(String message, int index){
        // Check that we're actually connected before trying anything
        Log.d(TAG,"Sending message "+message+" to "+index);
        if (getState(index) != STATE_CONNECTED) {
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            write(send, index);
        }
    }


    */
/**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     *//*

    private void write(byte[] out, int index) {
        Log.d(TAG,"Writing message to "+index);
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (getState(index) != STATE_CONNECTED) return;
            Log.d(TAG,"Getting a synchronized copy of the ConnectedThread at "+index);
            r = mConnectedThreads[index];
        }
        // Perform the write unsynchronized
        r.write(out);
    }


    */
/**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     *//*

    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private final int mIndex;

        public AcceptThread(int index) {
            mIndex = index;
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        NAME,
                        UUID.fromString(mUUIDs[mIndex]));
            } catch (IOException e) {
                Log.e(TAG, "::AcceptThread Socket listen() at "+ mIndex + " failed.", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
//            Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread"+mIndex);

            BluetoothSocket socket;

            // Listen to the server socket if we're not connected
            while (mStates[mIndex] != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "::AcceptThread Socket accept() at index "+ mIndex +" failed.", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothConnectionHelper.this) {
                        switch (mStates[mIndex]) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(), mIndex);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "::AcceptThread Could not close unwanted socket at index " + mIndex, e);
                                }
                                break;
                        }
                    }
                }
            }
//            Log.i(TAG, "END mAcceptThread");

        }

        public void cancel() {
            Log.d(TAG, "::AcceptThread Socket cancel at index " + mIndex);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "::AcceptThread Socket close() of server failed at index " + mIndex, e);
            }
        }
    }


    */
/**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     *//*

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final int mIndex;

        public ConnectThread(BluetoothDevice device, int index) {
            mmDevice = device;
            mIndex = index;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice

            Log.i(TAG, "::ConnectThread Get a BluetoothSocket for a connection at index " + mIndex);
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(mUUIDs[mIndex]));
            } catch (IOException e) {
                Log.e(TAG, "::ConnectThread Socket create() failed at index "+mIndex, e);
            }
            mmSocket = tmp;
        }

        public void run() {
//            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread"+mIndex);

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG,"::ConnectThread Creating a connection to a BluetoothSocket in index "+mIndex);
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "::ConnectThread  Unable to close() socket during connection failure for index "+mIndex, e2);
                }
                connectionFailed(mIndex);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnectionHelper.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mIndex);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    private class KeepAlive extends Thread {

        private int mIndex;
        private boolean isRunning;

        public KeepAlive(int index) {
            mIndex = index;
            isRunning = true;
        }

        private String generateKeepAliveCommand(){
            boolean dataCollectionState = SharedPreferencesHelper.getDataCollectionState(mContext);
            String sessionId ="";
            long nanos = -1;
            if (dataCollectionState){
                sessionId = SharedPreferencesHelper.getDataCollectionSessionObject(mContext).getSessionId();
                nanos = SystemClock.elapsedRealtimeNanos();
            }

            boolean labelsAnnotationState = SharedPreferencesHelper.getLabelsAnnotationState(mContext);
            int activityLabel = -1;
            int bodyPositionLabel = -1;
            int locationLabel = R.id.ui_iolocation_radioButton_outside;
            if (labelsAnnotationState){
                activityLabel = SharedPreferencesHelper.getAnnotatedActivityLabel(mContext);
                bodyPositionLabel = SharedPreferencesHelper.getAnnotatedBodyPositionLabel(mContext);
                locationLabel = SharedPreferencesHelper.getAnnotatedLocationLabel(mContext);
            }

            return new CommandKA(dataCollectionState, sessionId,  nanos,
                    labelsAnnotationState, activityLabel, bodyPositionLabel, locationLabel)
                    .getMessageBluetooth();
        }

        public synchronized void run() {
            while (isRunning){

                Log.d(TAG,"KeepAlive::run Sending keep alive in slot "+mIndex);
                BluetoothConnectionHelper.this.sendMessage(generateKeepAliveCommand(), mIndex);

                try {
                    int masterKA = SharedPreferencesHelper.getMasterKeepaliveInterval(mContext);
                    wait(masterKA);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error in keepalive thread for slot" + mIndex);
                }

            }
        }

        public void cancel() {
            isRunning = false;
        }
    }

    */
/**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     *//*

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final int mIndex;

        public ConnectedThread(BluetoothSocket socket, int index) {
//            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            mIndex = index;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "::ConnectedThread Input/Output stream sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[CommandBase.MAX_LENGTH];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mStates[mIndex] == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // The obtained bytes are transformed into commands
                    ArrayList<String> list =  CommandBase.parseMessage(new String(buffer, 0, bytes));
                    if (SharedPreferencesHelper.isEnabledKeepalive(mContext)
                            && CommandBase.containsCommand(list, CommandBase.COMMAND_KEEP_ALIVE_EVENT)) {
                        mHandler.removeCallbacks(keepAliveRunnable);
                        int slaveKA = SharedPreferencesHelper.getSlaveKeepaliveInterval(mContext);
                        mHandler.postDelayed(keepAliveRunnable, slaveKA);
                    }

                    if (CommandBase.containsCommand(list, CommandBase.COMMAND_KEEP_ALIVE_EVENT)) {
                        assert !mIsServer;
                        mHandler.removeCallbacks(keepAliveRunnable);
                        int slaveKA = SharedPreferencesHelper.getSlaveKeepaliveInterval(mContext);
                        mHandler.postDelayed(keepAliveRunnable, slaveKA);
                    }

                    // Send the obtained commands to the service
                    Message msg = mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_READ);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(Constants.BLUETOOTH_MESSAGE_READ_COMMANDS, list);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                } catch (IOException e) {
                    Log.e(TAG, "::ConnectedThread Disconnected in "+mIndex, e);
                    connectionLost(mIndex);
                    // Start the service over to restart
//                    BluetoothConnectionHelper.this.start(mIndex);
                    break;
                }
            }
        }

        public boolean isConnected() {
            return mmSocket.isConnected();
        }

        */
/**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         *//*

        public void write(byte[] buffer) {
            try {

                if (mmSocket.isConnected()) {
                    mmOutStream.write(buffer);
//                    mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_WRITE, -1, -1, buffer)
//                            .sendToTarget();
                } else {
                    connectionLost(mIndex);
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                connectionLost(mIndex);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    class KeepAliveRunnable implements Runnable {
        int mIndex;

        KeepAliveRunnable(int index) {
            mIndex = index;
        }

        @Override
        public void run() {
            Log.i(TAG, "Keep alive timeout. Disconnecting socket at position: " + mIndex);
            mHandler.removeCallbacks(keepAliveRunnable);
            connectionLost(mIndex);
        }
    }

}*/
