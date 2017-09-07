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

package uk.ac.sussex.wear.android.datalogger.bt.multibluetooth.bluetooth.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;



/**
 * Created by Rami MARTIN on 13/04/2014.
 */
public class BluetoothClient implements Runnable {

    private boolean CONTINUE_READ_WRITE = true;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private UUID mUuid;
    private String mAdressMac;

    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStreamWriter mOutputStreamWriter;

    private BluetoothConnector mBluetoothConnector;

    public BluetoothClient(BluetoothAdapter bluetoothAdapter, String adressMac) {
        mBluetoothAdapter = bluetoothAdapter;
        mAdressMac = adressMac;
        mUuid = UUID.fromString("e0917680-d427-11e4-8830-" + bluetoothAdapter.getAddress().replace(":", ""));
    }

    @Override
    public void run() {

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAdressMac);
//        List<UUID> uuidCandidates = new ArrayList<UUID>();
//        uuidCandidates.add(mUuid);

        while(mInputStream == null){
            mBluetoothConnector = new BluetoothConnector(mBluetoothDevice, true, mBluetoothAdapter, mUuid);

            try {
                mSocket = mBluetoothConnector.connect().getUnderlyingSocket();
                mInputStream = mSocket.getInputStream();
            } catch (IOException e1) {
                Log.e("", "===> mSocket IOException", e1);
                //EventBus.getDefault().post(new ClientConnectionFail());
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

            //EventBus.getDefault().post(new ClientConnectionSuccess());

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

                 //EventBus.getDefault().post(new BluetoothCommunicator(sb.toString()));

            }
        } catch (IOException e) {
            Log.e("", "===> Client run");
            e.printStackTrace();
            //EventBus.getDefault().post(new ClientConnectionFail());
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
