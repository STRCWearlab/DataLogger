/*
 * Copyright (c) 2019. Mathias Ciliberto, Francisco Javier Ordoñez Morales,
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

package uk.ac.sussex.wear.android.datalogger.collector.BTHelper;

import android.bluetooth.BluetoothDevice;

public class ScannedDevice {

    /* BluetoothDevice */
    private BluetoothDevice mDevice;
    /* Advertise Scan Record */
    private byte[] mScanRecord;
    /* RSSI */
    private int mRssi;
    private String mDevAddress;

    public ScannedDevice(BluetoothDevice device, int rssi, byte[] scanRecord, long now) {
        if (device == null) {
            throw new IllegalArgumentException("BluetoothDevice is null");
        }
        /*mLastUpdatedMs = now;*/
        mDevice = device;
        /*mDisplayName = device.getName();
        if ((mDisplayName == null) || (mDisplayName.length() == 0)) {
            mDisplayName = UNKNOWN;
        }*/
        mRssi = rssi;
        mScanRecord = scanRecord;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public void setAddress(String address){
        mDevAddress = address;
    }

    public String getScanRecordHexString() {
        return ScannedDevice.asHex(mScanRecord);
    }

    private static String asHex(byte bytes[]) {
        if ((bytes == null) || (bytes.length == 0)) {
            return "";
        }

        // バイト配列の２倍の長さの文字列バッファを生成。
        // Generate a string buffer twice as long as a byte array.
        StringBuffer sb = new StringBuffer(bytes.length * 2);

        // バイト配列の要素数分、処理を繰り返す。
        // Repeat the process for the number of elements in the byte array.
        for (int index = 0; index < bytes.length; index++) {
            // バイト値を自然数に変換。
            // Convert byte values ​​to natural numbers.
            int bt = bytes[index] & 0xff;

            // バイト値が0x10以下か判定。
            // Determine whether the byte value is 0x10 or less.
            if (bt < 0x10) {
                // 0x10以下の場合、文字列バッファに0を追加。
                // Add 0 to string buffer if it is less than 0x10.
                sb.append("0");
            }

            // バイト値を16進数の文字列に変換して、文字列バッファに追加。
            // Convert byte value to hexadecimal string and add to string buffer.
            sb.append(Integer.toHexString(bt).toUpperCase());
        }

        // 16進数の文字列を返す。
        // Return a hexadecimal string.
        return sb.toString();
    }

    public void setScanRecord(byte[] scanRecord) {
        mScanRecord = scanRecord;
    }
}
