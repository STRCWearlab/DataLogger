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
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

import uk.ac.sussex.wear.android.datalogger.Constants;
import uk.ac.sussex.wear.android.datalogger.data.CommandBase;

*/
/**
 * Created by fjordonez on 27/01/17.
 *//*


public class MultiBluetoothManager {

    private static final String TAG = MultiBluetoothManager.class.getSimpleName();

    private static final String NAME = "MultiBluetoothManager";

    // Member fields
   */
/* private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private final Context mContext;
    private int[] mStates;
    private boolean mIsServer;*//*


    private BluetoothClient mBluetoothClient;
    private ArrayList<BluetoothServer> mServerConnectedList;


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


    public MultiBluetoothManager(Context context, Handler handler, boolean isServer) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mIsServer = isServer;
        mStates = new int[]{STATE_NONE, STATE_NONE, STATE_NONE};

    }

    public synchronized void start(int index) {
        Log.d(TAG, "::start Starting bluetooth at index " + index);



    }


    */
/**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param address The address to connect
     *//*

    public synchronized void connect(String address, int index) {
        Log.i(TAG, "::connect Trying to connect to address " + address + ". ");

        mBluetoothClient = new BluetoothClient(mBluetoothAdapter, address, index);
        new Thread(mBluetoothClient).start();

    }



    private void connectionSuccess(String address, int index) {
        Log.d(TAG, "::connected Starting ConnectedThread for bluetooth at index "+index);

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_DEVICE_ADDRESS);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BLUETOOTH_CONNECTED_DEVICE_ADDRESS, address);
        bundle.putInt(Constants.BLUETOOTH_CONNECTED_DEVICE_LOCATION, index);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }



    */
/**
     * Indicate that the connection attempt failed and notify the UI Activity.
     *//*

    private void connectionFailed(int index) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_CONNECTION_FAILED, index);
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.BLUETOOTH_CONNECTED_DEVICE_LOCATION, index);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }



    private class BluetoothClient implements Runnable {

        private boolean CONTINUE_READ_WRITE = true;

        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothDevice mBluetoothDevice;
        private UUID mUuid;
        private String mAdressMac;
        private int mIndex;

        private BluetoothSocket mSocket;
        private InputStream mInputStream;
        private OutputStreamWriter mOutputStreamWriter;

        private BluetoothConnector mBluetoothConnector;

        public BluetoothClient(BluetoothAdapter bluetoothAdapter, String adressMac, int index) {
            mBluetoothAdapter = bluetoothAdapter;
            mAdressMac = adressMac;
            mIndex = index;
//            mUuid = UUID.fromString("e0917680-d427-11e4-8830-" + bluetoothAdapter.getAddress().replace(":", ""));
            mUuid = UUID.fromString(mUUIDs[mIndex]);
        }

        @Override
        public void run() {

            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAdressMac);

            while(mInputStream == null){
                mBluetoothConnector = new BluetoothConnector(mBluetoothDevice, true, mBluetoothAdapter, mUuid);

                try {
                    mSocket = mBluetoothConnector.connect().getUnderlyingSocket();
                    mInputStream = mSocket.getInputStream();
                } catch (IOException e1) {
                    Log.e("", "===> mSocket IOException", e1);

                    connectionFailed(mIndex);
//                    EventBus.getDefault().post(new ClientConnectionFail());

                    e1.printStackTrace();
                }
            }

            if (mSocket == null) {
                Log.e("", "===> mSocket == Null");
                return;
            }

            try {

                mOutputStreamWriter = new OutputStreamWriter(mSocket.getOutputStream());

                int bufferSize = 1024;
                int bytesRead = -1;
                byte[] buffer = new byte[bufferSize];

//                EventBus.getDefault().post(new ClientConnectionSuccess());
                connectionSuccess(mAdressMac, mIndex);

                while (CONTINUE_READ_WRITE) {

                    final StringBuilder sb = new StringBuilder();
                    bytesRead = mInputStream.read(buffer);
                    if (bytesRead != -1) {
                        String result = "";
                        while ((bytesRead == bufferSize) && (buffer[bufferSize] != 0)) {
                            result = result + new String(buffer, 0, bytesRead);
                            bytesRead = mInputStream.read(buffer);
                        }
                        result = result + new String(buffer, 0, bytesRead);
                        sb.append(result);
                    }

                    // The obtained bytes are transformed into commands
                    ArrayList<String> list =  CommandBase.parseMessage(sb.toString());

                    // Send the obtained commands to the service
                    Message msg = mHandler.obtainMessage(Constants.BLUETOOTH_MESSAGE_READ);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(Constants.BLUETOOTH_MESSAGE_READ_COMMANDS, list);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

//                    EventBus.getDefault().post(new BluetoothCommunicator(sb.toString()));

                }
            } catch (IOException e) {
                Log.e("", "===> Client run");
                e.printStackTrace();

                connectionFailed(mIndex);
//                EventBus.getDefault().post(new ClientConnectionFail());
            }
        }

        public void write(String message) {
            try {
                mOutputStreamWriter.write(message);
                mOutputStreamWriter.flush();
            } catch (IOException e) {
                Log.e("", "===> Client write");
                e.printStackTrace();
            }
        }

        public void closeConnexion() {
            if (mSocket != null) {
                try {
                    mInputStream.close();
                    mInputStream = null;
                    mOutputStreamWriter.close();
                    mOutputStreamWriter = null;
                    mSocket.close();
                    mSocket = null;
                    mBluetoothConnector.close();
                } catch (Exception e) {
                    Log.e("", "===> Client closeConnexion");
                }
                CONTINUE_READ_WRITE = false;
            }
        }
    }


    public class BluetoothServer implements Runnable {

        private boolean CONTINUE_READ_WRITE = true;

        private UUID mUUID;
        public String mClientAddress;
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothServerSocket mServerSocket;
        private BluetoothSocket mSocket;
        private InputStream mInputStream;
        private OutputStreamWriter mOutputStreamWriter;

        public BluetoothServer(BluetoothAdapter bluetoothAdapter){
            mBluetoothAdapter = bluetoothAdapter;
            //mClientAddress = clientAddress;
            mUUID = UUID.fromString("e0917680-d427-11e4-8830-" + mClientAddress.replace(":", ""));
        }

        @Override
        public void run() {
            try {
                mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BLTServer", mUUID);
                mSocket = mServerSocket.accept();
                mInputStream = mSocket.getInputStream();
                mOutputStreamWriter = new OutputStreamWriter(mSocket.getOutputStream());

                int bufferSize = 1024;
                int bytesRead = -1;
                byte[] buffer = new byte[bufferSize];

                //EventBus.getDefault().post(new ServeurConnectionSuccess(mClientAddress));

                while(CONTINUE_READ_WRITE) {
                    final StringBuilder sb = new StringBuilder();
                    bytesRead = mInputStream.read(buffer);
                    if (bytesRead != -1) {
                        String result = "";
                        while ((bytesRead == bufferSize) && (buffer[bufferSize] != 0)) {
                            result = result + new String(buffer, 0, bytesRead);
                            bytesRead = mInputStream.read(buffer);
                        }
                        result = result + new String(buffer, 0, bytesRead);
                        sb.append(result);
                    }
                    //EventBus.getDefault().post(new BluetoothCommunicator(sb.toString()));

                }
            } catch (IOException e) {
                Log.e("", "ERROR : " + e.getMessage());
                //EventBus.getDefault().post(new ServeurConnectionFail(mClientAddress));
            }
        }

        public void write(String message) {
            try {
                if(mOutputStreamWriter != null) {
                    mOutputStreamWriter.write(message);
                    mOutputStreamWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getClientAddress(){
            return mClientAddress;
        }

        public void closeConnection(){
            if(mSocket != null){
                try{
                    mInputStream.close();
                    mInputStream = null;
                    mOutputStreamWriter.close();
                    mOutputStreamWriter = null;
                    mSocket.close();
                    mSocket = null;
                    mServerSocket.close();
                    mServerSocket = null;
                    CONTINUE_READ_WRITE = false;
                }catch(Exception e){}
                CONTINUE_READ_WRITE = false;
            }
        }
    }

}
*/
