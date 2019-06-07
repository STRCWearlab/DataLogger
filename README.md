# DataLogger 
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

DataLogger is an high reliability Android application for multidevice multimodal mobile data acquisition and annotation.

## Functionalities
Application features:
- Logging of sensor data:
  - Inertial sensors data such as accelerometer, gyroscope, magnetometer, as well as software sensors data for linear acceleration, orientation (quaternions), gravity;
  - Environmental sensors data as temperature, light, pressure and humidity;
  - Positioning sensors data such as location and satellites;
  - Networks connectivity data, as network cells and WiFi networks;
  - Available Bluetooth beacons
  - Battery level
  - Microphone audio
  - Google activity recognition labels
- Every sensors can be configured in the settings to enable/disable it, set the sampling frequency, the remote upload of its data.
- Remote upload of data (`PHP_upload_script.txt` contains the PHP code for the server)
- Multidevice (master-slaves) configuration
- Bluetooth connection for synchronization among devices of time and status (logging/no logging, labels, sublabels)
- Autostart at boot/reboot
- Background logging using persistent notification
- Logging of data on files (a new file is created at regular interval to reduce data loss)

## Requirement
- Android, minimum SDK 19.
- Google play service (for Google activity recognition)

## Configuration required 
### Settings' password
In order to prevent changes to the setting by the users, the setting menu is protected by a password. It is recommended to change the password in the code before to deploy the application. Set the value of `ADMIN_CODE` in `Constants.java` to change the password before to install the application.

### Master & Slave configuration
It can be configured for collecting data on multiple devices synchronously, in a master-slave configuration. The configuration is set changing the position of the device in the settings. The Hand device is the master, while Hips, Torso and Bag device are the slaves.

<br>
<table border="0">
  <tr>
    <td border="0">
    <img src="./img/master.png" width="400">
    </td>
    <td border="0">
    <img src="./img/slave.png" width="400">
    </td>
  </tr>
</table>

### Remote upload
The remote upload functionality requires to set up the remote address for the upload. The address is in `string.xml` in the value folder.

### Permissions
Due to the changes to the permissions systems in the latest versions of Android, please check and authorize the access to Microphone, 
Storage, Telephone and Location in the system settings before to use the application.

## User guide
### Getting started
- Install the app on a smartphone via apk
- Make sure ALL permissions are enabled in the system settings for this app
- Enable Bluetooth, Wifi, Mobile Data and Location sensors of the phone

###  Configuration
- Start the app
- Open the settings by entering the setting's password
- Set the user name, device location (master or slave), maximum log file size and logging time intervals in "General"
- Set the server address for the file upload in "Synchronisation"
- Make sure all sensors are enabled
- Set the sampling rates for the sensors if needed

### Collecting data
- Connect all slaves to the master if needed
- Start the data collection by pressing the "OFF" button
- Data gets now collected until stopping it by pressing "ON"
- Logs can be labeled for more meta information
- To upload all log files to the server make sure the phone is connected to a Wifi network and press "Upload"

## LICENSE
This application has been developed for research purpose and release under MIT License. The usage of this application in research and publications must be acknowledged by citing the following publication:

[1] Mathias Ciliberto, Francisco Javier Ordoñez Morales, Hristijan Gjoreski, Daniel Roggen, Sami Mekki, Stefan Valentin. *"High reliability Android application for multidevice multimodal mobile data acquisition and annotation"* in ACM Conference on Embedded Networked Sensor Systems (Sensys), 2017.

```
The MIT License

Copyright (c) 2017. Mathias Ciliberto, Francisco Javier Ordoñez Morales,
Hristijan Gjoreski, Daniel Roggen

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
