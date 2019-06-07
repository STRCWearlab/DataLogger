/*
 * Copyright (c) 2019. Mathias Ciliberto, Francisco Javier Ordoñez Morales,
 * Hristijan Gjoreski, Daniel Roggen, Clara Wurm
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
// (c) 2016 youten, http://greety.sakura.ne.jp/redo/

package uk.ac.sussex.wear.android.datalogger.collector.BTHelper;

/*
* created by wurmc
* */

import android.bluetooth.BluetoothDevice;
import java.util.List;

public class DeviceAdapter {

    private List<ScannedDevice> mList;

    public DeviceAdapter(List<ScannedDevice> objects) {
        mList = objects;
    }

    public String updateDevice(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        if ((newDevice == null) || (newDevice.getAddress() == null)) {
            return "";
        }
        long now = System.currentTimeMillis();

        boolean contains = false;
        for (ScannedDevice device : mList) {
            if (newDevice.getAddress().equals(device.getDevice().getAddress())) {
                contains = true;
                // update
                device.setAddress(device.getDevice().getAddress());
                device.setRssi(rssi);
                /*device.setLastUpdatedMs(now);*/
                device.setScanRecord(scanRecord);
                break;
            }
        }
        if (!contains) {
            // add new BluetoothDevice
            mList.add(new ScannedDevice(newDevice, rssi, scanRecord, now));
        }

        // create summary
        int totalCount = 0;
        if (mList != null) {
            totalCount = mList.size();
        }
        String summary = /*"iBeacon:" + Integer.toString(iBeaconCount) +*/ " (Total:"
                + Integer.toString(totalCount) + ")";

        return summary;
    }

    public List<ScannedDevice> getScanList() {
        return mList;
    }
}
