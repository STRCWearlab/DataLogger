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

package uk.ac.sussex.wear.android.datalogger;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

//import uk.ac.sussex.wear.android.datalogger.bt.BluetoothDeviceListActivity;
import uk.ac.sussex.wear.android.datalogger.data.CommandBTC;
import uk.ac.sussex.wear.android.datalogger.data.CommandBTS;
import uk.ac.sussex.wear.android.datalogger.data.CommandDCE;
import uk.ac.sussex.wear.android.datalogger.data.CommandFUC;
import uk.ac.sussex.wear.android.datalogger.data.CommandFUS;
import uk.ac.sussex.wear.android.datalogger.data.CommandLAE;


public class DisplayActivity extends AppCompatActivity implements DataLoggerStatusDelegate, View.OnLongClickListener {


    private static final String TAG = DisplayActivity.class.getSimpleName();

    // UI elements.
    private CoordinatorLayout mCoordinatorLayoutView;

    private Button mDataCollectionButton;
    private Button mLabelAnnotationButton;

    private Chronometer mLabelsChronometer;

    private Spinner mActivitiesSpinner, mBodyPositionsSpinner;
    private RadioGroup mLocationRadioGroup;

    private Menu menu;


    private static final int REQUEST_CONNECT_DEVICE = 1;
    private Button mBluetoothButton;

    private int mDeviceLocation = 3;

    // Control stop and start labelling when the activity is created. Otherwise onCreate will trigger
    // a label annotation event.
    private boolean allowStopStartLabelAnnotation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCoordinatorLayoutView = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        mLabelsChronometer = (Chronometer) findViewById(R.id.label_annotation_chronometer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Registers a callback to be invoked when a change happens to a preference.
        getSharedPreferencesInstance().registerOnSharedPreferenceChangeListener(sharedPrefsListener);

        /******************************************************************************************/
        // Labels selection spinners initialization
        /******************************************************************************************/

        // Setting adapters for UI spinners
        mBodyPositionsSpinner = (Spinner) findViewById(R.id.ui_body_positions_spinner);
        assert mBodyPositionsSpinner != null;
        mBodyPositionsSpinner.setOnItemSelectedListener(mBodyPositionsSpinnerListener);

        mActivitiesSpinner = (Spinner) findViewById(R.id.ui_activities_spinner);
        assert mActivitiesSpinner != null;
        mActivitiesSpinner.setOnItemSelectedListener(mActivitiesSpinnerListener);
        mActivitiesSpinner.setAdapter(new CustomIconsListAdapter(
                getApplicationContext(),
                getResources().obtainTypedArray(R.array.activities_icons_array),
                getResources().getStringArray(R.array.activities_names_array))
        );
        // The activities spinner adapter is updated to the correct value
        mActivitiesSpinner.setSelection(SharedPreferencesHelper.getSelectedActivityLabel(this));

        // In case the label annotation is active in the service, the label chronometer is updated
        if (SharedPreferencesHelper.getLabelsAnnotationState(this)) {
            allowStopStartLabelAnnotation = false;
            long labelStartingTime = SharedPreferencesHelper.getLabelsAnnotationStartingTime(this);
            if (labelStartingTime > 0) {
                onLabelTimerStart(System.currentTimeMillis() - labelStartingTime);
            }
        }

        /******************************************************************************************/
        // Labels selection radio buttons initialization
        /******************************************************************************************/

        mLocationRadioGroup = (RadioGroup) findViewById(R.id.ui_iolocation_radioGroup);
        assert mLocationRadioGroup != null;
        mLocationRadioGroup.check(SharedPreferencesHelper.getSelectedLocationLabel(this));
        mLocationRadioGroup.setOnCheckedChangeListener(mBodyLocationRadioGroupListener);

        /******************************************************************************************/
        // Data collection and label annotation switches initialization
        /******************************************************************************************/

        // Setting listener for data collection switch
        mDataCollectionButton = (Button) findViewById(R.id.ui_data_collection_button);
        assert mDataCollectionButton != null;
        mDataCollectionButton.setOnLongClickListener(this);

        // Setting listener for labels annotation switch
        mLabelAnnotationButton = (Button) findViewById(R.id.ui_label_annotation_button);
        assert mLabelAnnotationButton != null;
        mLabelAnnotationButton.setOnLongClickListener(this);

        updateTextviewSwitch(false, R.id.ui_data_collection_custom_text);
        // Data collection switch can be activated/deactivated by default
        mDataCollectionButton.setEnabled(true);

        updateTextviewSwitch(false, R.id.ui_label_annotation_custom_text);
        // Data labelling switch can be activated only when data collection switch is active
        mLabelAnnotationButton.setEnabled(false);

        /******************************************************************************************/
        // Defining buttons listeners
        /******************************************************************************************/

        // Setting listener for bluetooth button
        /*mBluetoothButton = (Button) findViewById(R.id.ui_bluetooth_status_button);
        assert mBluetoothButton != null;
        mBluetoothButton.setOnClickListener(mClickBluetoothButton);*/

        /******************************************************************************************/
        // Starting main DataLogger service with 'Bluetooth start' command
        /******************************************************************************************/

        DataLoggerService.setStatusDelegate(this);
        startService(new Intent(this, DataLoggerService.class)
                .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY, new CommandBTS().getMessage()));

    }


    @Override
    protected void onStart() {
        super.onStart();

        mDeviceLocation = SharedPreferencesHelper.getDeviceLocationValue(this);

        // If the state of the data collector when the activity starts was "collecting data"
        // a message is sent to the service to be sure the state is correct
        if (SharedPreferencesHelper.getDataCollectionState(this)) {
            startService(new Intent(this, DataLoggerService.class)
                    .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY,
                            new CommandDCE(true).getMessage()));
            mDataCollectionButton.setEnabled(true);
            updateTextviewSwitch(true, R.id.ui_data_collection_custom_text);
            setChecked(mDataCollectionButton, true);
            mLabelAnnotationButton.setEnabled(true);
        } else {
            updateTextviewSwitch(false, R.id.ui_data_collection_custom_text);
            setChecked(mDataCollectionButton, false);
            mLabelAnnotationButton.setEnabled(false);
        }

        if (SharedPreferencesHelper.getLabelsAnnotationState(this)) {
            startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                    .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY,
                            new CommandLAE(true,
                                    SharedPreferencesHelper.getAnnotatedActivityLabel(this),
                                    SharedPreferencesHelper.getAnnotatedBodyPositionLabel(this),
                                    SharedPreferencesHelper.getAnnotatedLocationLabel(this)
                            ).getMessage()
                    )
            );
            mDataCollectionButton.setEnabled(false);
            updateTextviewSwitch(true, R.id.ui_label_annotation_custom_text);
            setChecked(mLabelAnnotationButton, true);
        } else {
            mDataCollectionButton.setEnabled(true);
            updateTextviewSwitch(false, R.id.ui_label_annotation_custom_text);
            setChecked(mLabelAnnotationButton, false);
        }


        // Update the bluetooth RelativeLayout based on the bluetooth adapter status.
        /*refreshBluetoothLayout();

        if (mDeviceLocation == Constants.DEVICE_LOCATION_HAND) { //if this is the master device
            // Update the TextView which shows the connectivity status of the slave devices.
            refreshBluetoothSlavesLayout();
        }*/

        // Update the main UI layout based on the device profile (master or slave)
        refreshContentLayout();

    }

    private void setChecked(Button button, boolean bool) {
        if (bool) {
            button.setText(R.string.on);
        } else {
            button.setText(R.string.off);
        }

    }


    /**********************************************************************************************/
    /**********************************************************************************************/
    /*                          Methods and fields related to the UI                              */
    /**********************************************************************************************/
    /**********************************************************************************************/

    /*public void refreshBluetoothSlavesLayout() {
        int value = SharedPreferencesHelper.getSlavesConnected(this);
        int[] bitmasks = new int[]{0x4, 0x2, 0x1};
        int[] layouts = new int[]{R.id.ui_bluetooth_status_slaves_torso,
                R.id.ui_bluetooth_status_slaves_hips,
                R.id.ui_bluetooth_status_slaves_bag};
        for (int i = 0; i < bitmasks.length; i++) {
            boolean isConnected = ((value & bitmasks[i]) == bitmasks[i]);
            TextView textView = (TextView) findViewById(layouts[i]);
            assert textView != null;
            textView.setTextColor(ContextCompat.getColor(DisplayActivity.this,
                    isConnected ? R.color.colorBtSlavesOn : R.color.colorBtSlavesOff));
        }
    }*/

    /**
     * The layout of the bluetooth controls must change based on the device location and the state
     * of the bluetooth adapter.
     */
    /*private void refreshBluetoothLayout() {
        // Current state of the bluetooth adapter
        int state = SharedPreferencesHelper.getBluetoothStatus(this);
        boolean slavesVisible = true;
        boolean buttonVisible = true;
        String[] bluetoothStateList = SharedPreferencesHelper.getBluetoothStateList(this);
        TextView bluetoothCustomText = (TextView) findViewById(R.id.ui_bluetooth_status_state_custom_text);

        if (mDeviceLocation != Constants.DEVICE_LOCATION_HAND) {
            slavesVisible = false;
        }

        switch (state) {
            case Constants.BLUETOOTH_STATE_NOT_SUPPORTED: // Bluetooth not supported
                buttonVisible = false;
                slavesVisible = false;
                bluetoothCustomText.setText(bluetoothStateList[state]);
                break;
            case Constants.BLUETOOTH_STATE_DISABLED: // Bluetooth disabled
                ((Button) findViewById(R.id.ui_bluetooth_status_button)).setText(
                        R.string.ui_bluetooth_button_text_enable);
                bluetoothCustomText.setText(bluetoothStateList[state]);
                break;
            case Constants.BLUETOOTH_STATE_ENABLED: // Bluetooth enabled
                int text = R.string.ui_bluetooth_button_text_connect;
                ((Button) findViewById(R.id.ui_bluetooth_status_button)).setText(text);
                bluetoothCustomText.setText(bluetoothStateList[state]);
                break;
            case Constants.BLUETOOTH_STATE_CONNECTING:
            case Constants.BLUETOOTH_STATE_CONNECTED:
                if (mDeviceLocation != Constants.DEVICE_LOCATION_HAND) {
                    bluetoothCustomText.setText(bluetoothStateList[state]);
                }
                break;
        }

        LinearLayout layoutSlaves = (LinearLayout) findViewById(R.id.ui_bluetooth_status_slaves);
        for (int i = 0; i < layoutSlaves.getChildCount(); i++) {
            layoutSlaves.getChildAt(i).setVisibility(slavesVisible ? View.VISIBLE : View.GONE);
        }

        LinearLayout layoutButton = (LinearLayout) findViewById(R.id.ui_bluetooth_button);
        for (int i = 0; i < layoutButton.getChildCount(); i++) {
            layoutButton.getChildAt(i).setVisibility(buttonVisible ? View.VISIBLE : View.GONE);
        }

    }*/

    public void updateTextviewSwitch(boolean isActive, int id) {
        TextView customText = (TextView) findViewById(id);
        if (isActive) {
            customText.setTextColor(ContextCompat.getColor(this, R.color.colorTextCollectionOn));
            customText.setText(getResources().getString(R.string.ui_switch_text_on));
        } else {
            customText.setTextColor(ContextCompat.getColor(this, R.color.colorTextCollectionOff));
            customText.setText(getResources().getString(R.string.ui_switch_text_off));
        }
    }

    private void setBodyPositionsSpinnerAdapter(TypedArray icons, int colorText) {
        mBodyPositionsSpinner.setAdapter(new CustomIconsListAdapter(
                this,
                icons,
                getResources().getStringArray(R.array.body_positions_names_array),
                colorText)
        );
    }

    private void setupLocationRadioButton() {
        boolean includeInOut = SharedPreferencesHelper.getSelectedActivityLabel(this)
                < getResources().getInteger(R.integer.num_labels_w_io_location);
        for (int i = 0; i < mLocationRadioGroup.getChildCount(); i++) {
            ((RadioButton) mLocationRadioGroup.getChildAt(i)).setEnabled(includeInOut);
        }
        TextView bodyLocationTextview = (TextView) findViewById(R.id.body_iolocation_textview);
        if (includeInOut) {
            bodyLocationTextview.setTextColor(ContextCompat.getColor(this, R.color.colorSpinnerOn));
        } else {
            bodyLocationTextview.setTextColor(ContextCompat.getColor(this, R.color.colorSpinnerOff));
            mLocationRadioGroup.check(R.id.ui_iolocation_radioButton_outside);
        }
    }

    private void setupBodyPositionsSpinner() {
        int activityLabel = SharedPreferencesHelper.getSelectedActivityLabel(this);
        boolean hasBodyPosition = (activityLabel
                >= getResources().getInteger(R.integer.num_labels_wo_pos) || activityLabel == 0);
        mBodyPositionsSpinner.setEnabled(hasBodyPosition);

        TextView bodyPositionsTextview = (TextView) findViewById(R.id.body_positions_textview);
        assert bodyPositionsTextview != null;

        // The adapter of the body positions spinner is modified according to the activity
        // This step will trigger the onItemSelected method of the body positions adapter
        if (hasBodyPosition) {
            setBodyPositionsSpinnerAdapter(
                    getResources().obtainTypedArray(R.array.body_positions_on_icons_array),
                    ContextCompat.getColor(this, R.color.colorSpinnerOn));
            bodyPositionsTextview.setTextColor(ContextCompat.getColor(this, R.color.colorSpinnerOn));
        } else {
            setBodyPositionsSpinnerAdapter(
                    getResources().obtainTypedArray(R.array.body_positions_off_icons_array),
                    ContextCompat.getColor(this, R.color.colorSpinnerOff));
            bodyPositionsTextview.setTextColor(ContextCompat.getColor(this, R.color.colorSpinnerOff));
        }
    }

    AdapterView.OnItemSelectedListener
            mActivitiesSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // Store the activity label in SharedPreferences
            SharedPreferencesHelper.setSelectedActivityLabel(DisplayActivity.this, position);
            Log.i(TAG, "mActivitiesSpinnerListener::onItemSelected Current activity: " + position);
            setupBodyPositionsSpinner();
            setupLocationRadioButton();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void checkStopStartLabelAnnotation() {
        // If the app is currently annotating
        if (SharedPreferencesHelper.getLabelsAnnotationState(DisplayActivity.this)) {
            if (allowStopStartLabelAnnotation) {

                long lengthLabelMillis = System.currentTimeMillis() - SharedPreferencesHelper.getLabelsAnnotationStartingTime(DisplayActivity.this);
                SharedPreferencesHelper.addLabelAnnotationTime(DisplayActivity.this,
                        SharedPreferencesHelper.getAnnotatedActivityLabel(DisplayActivity.this),
                        (int) (lengthLabelMillis / 1000)
                );

                SharedPreferencesHelper.setLabelsAnnotationStartingTime(DisplayActivity.this, 0);
                mLabelsChronometer.stop();
                startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                        .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY,
                                new CommandLAE(false)
                                        .setStopAndStartEvent(
                                                SharedPreferencesHelper.getSelectedActivityLabel(DisplayActivity.this),
                                                SharedPreferencesHelper.getSelectedBodyPositionLabel(DisplayActivity.this),
                                                SharedPreferencesHelper.getSelectedLocationLabel(DisplayActivity.this))
                                        .getMessage()
                        )
                );
                SharedPreferencesHelper.setLabelsAnnotationStartingTime(DisplayActivity.this, System.currentTimeMillis());
                onLabelTimerStart(0);

            } else {
                allowStopStartLabelAnnotation = true;
            }
        }
    }

    AdapterView.OnItemSelectedListener
            mBodyPositionsSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            SharedPreferencesHelper.setSelectedBodyPositionLabel(DisplayActivity.this, position);
            Log.i(TAG, "mBodyPositionsSpinnerListener::onItemSelected Current body position: " + position);
            checkStopStartLabelAnnotation();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


    RadioGroup.OnCheckedChangeListener
            mBodyLocationRadioGroupListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            SharedPreferencesHelper.setSelectedLocationLabel(DisplayActivity.this, checkedId);
            String logMessage = "mBodyLocationRadioGroupListener::onCheckedChanged Current body location: ";
            if (checkedId == R.id.ui_iolocation_radioButton_outside) {
                logMessage += "outside";
            } else if (checkedId == R.id.ui_iolocation_radioButton_inside) {
                logMessage += "inside";
            }
            Log.i(TAG, logMessage);
            checkStopStartLabelAnnotation();
        }
    };

    public void refreshContentLayout() {

        // The toolbar is updated to include a reference to the current location of the device
        getSupportActionBar().setTitle(
                getResources().getString(R.string.toolbar_base_title)
                        + " | @"
                        + SharedPreferencesHelper.getDeviceLocation(this)
        );

        // When the location is not the hand, the visibility of several layouts (file upload,
        // activity selection, data collection switch and label annotation) is GONE
        int visibility = (mDeviceLocation != Constants.DEVICE_LOCATION_HAND) ? View.GONE : View.VISIBLE;
        int[] layoutsId = new int[]{R.id.connectivity_ui,
                R.id.activities_labels_ui,
                R.id.data_collection_ui,
                R.id.label_annotation_ui,
                R.id.label_annotation_timer};

        for (int layoutId : layoutsId) {
            if (layoutId == R.id.data_collection_ui) {
                Button dataCollectionButton = ((Button) findViewById(R.id.ui_data_collection_button));
                if (dataCollectionButton != null)
                    dataCollectionButton.setVisibility(visibility);
            } else {
                RelativeLayout layout = (RelativeLayout) findViewById(layoutId);
                for (int i = 0; i < layout.getChildCount(); i++) {
                    layout.getChildAt(i).setVisibility(visibility);
                }
            }
        }

        /*mBluetoothButton = (Button) findViewById(R.id.ui_bluetooth_status_button);
        if (mDeviceLocation == Constants.DEVICE_LOCATION_HAND) {
            mBluetoothButton.setVisibility(View.INVISIBLE);
            if (menu != null)
                menu.findItem(R.id.action_pair).setVisible(true);
        } else if (menu != null)
            menu.findItem(R.id.action_pair).setVisible(false);*/
    }

    // IMPORTANT ! this creates the menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        /*if (SharedPreferencesHelper.getDeviceLocationValue(this) != Constants.DEVICE_LOCATION_HAND)
            menu.findItem(R.id.action_pair).setVisible(false);
        else
            menu.findItem(R.id.action_pair).setVisible(true);*/
        return true;
    }

    /**********************************************************************************************/
    /**********************************************************************************************/


    /*View.OnClickListener mClickBluetoothButton = new View.OnClickListener() {

        public void onClick(View v) {
            int state = SharedPreferencesHelper.getBluetoothStatus(DisplayActivity.this);
            switch (state) {
                case Constants.BLUETOOTH_STATE_DISABLED:
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
                    break;
                case Constants.BLUETOOTH_STATE_ENABLED:
                case Constants.BLUETOOTH_STATE_CONNECTING:
                case Constants.BLUETOOTH_STATE_CONNECTED:
                    boolean isMaster = (mDeviceLocation == Constants.DEVICE_LOCATION_HAND);
                    if (isMaster) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoverableIntent);
                    } else {
                        Intent serverIntent = new Intent(DisplayActivity.this, BluetoothDeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                    }
                    break;
            }
        }

    };*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    String address = data.getExtras().getString(Constants.BLUETOOTH_DEVICE_ADDRESS_INTENT_KEY);
                    startService(new Intent(this, DataLoggerService.class)
                            .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY, new CommandBTC(address).getMessage()));
                }
                break;
        }
    }


    public void onLabelTimerStart(long elapsedMillis) {
        mLabelsChronometer.setBase(SystemClock.elapsedRealtime() - elapsedMillis);
        mLabelsChronometer.start();
    }


    /**
     * Retrieves the default SharedPreference object used to store or read values in this app.
     */
    private SharedPreferences getSharedPreferencesInstance() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_flag_event:
                //User chose the "Flag event" item.
                Intent flagEventIntent = new Intent(this, FlagEventActivity.class);
                startActivity(flagEventIntent);
                return true;

            case R.id.action_settings:
                // User chose the "Settings" item.
                if (SharedPreferencesHelper.getDataCollectionState(this)) {
                    Snackbar.make(mCoordinatorLayoutView, "Stop data collection before change settings.", Snackbar.LENGTH_LONG).show();
                } else {
                    final EditText view = new EditText(this);
                    view.setInputType(InputType.TYPE_CLASS_NUMBER);
                    new AlertDialog.Builder(this)
                            .setTitle("Settings access protected")
                            .setMessage("Enter admin pass:")
                            .setView(view)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Constants.ADMIN_CODE == Integer.parseInt(view.getText().toString())) {
                                        startActivity(new Intent(DisplayActivity.this, SettingsActivity.class));
                                    } else {
                                        Snackbar.make(mCoordinatorLayoutView, "Incorrect admin password", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                return true;

            case R.id.action_upload:
                String upload_confirmation_message = String.format("Before to confirm the upload, please check that:\n"
                        + "- The phones are connected to the chargers\n"
                        + "- The phones are connected to a WiFi network\n"
                        + "The upload could take a while. Please, do not use the phone while they are uploading.");
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.upload_confirmation_title))
                        .setMessage(upload_confirmation_message)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                                        .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY, new CommandFUS().getMessage()));
                                menu.findItem(R.id.action_cancel_upload).setVisible(true);
                                menu.findItem(R.id.action_upload).setVisible(false);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            case R.id.action_cancel_upload:
                String cancel_confirmation_message = "Are you sure to cancel the upload?";
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.cancel_confirmation_title))
                        .setMessage(cancel_confirmation_message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                                        .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY, new CommandFUC().getMessage()));
                                menu.findItem(R.id.action_upload).setVisible(true);
                                menu.findItem(R.id.action_cancel_upload).setVisible(false);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            /*case R.id.action_pair:
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
                return true;*/

            case R.id.action_stats:
                Intent showStatsIntent = new Intent(this, ShowStatsActivity.class);
                startActivity(showStatsIntent);
                return true;

            case R.id.action_about:
                Intent showAppInfo = new Intent(this, AboutActivity.class);
                startActivity(showAppInfo);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }

    }

    SharedPreferences.OnSharedPreferenceChangeListener sharedPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

            if (key.equals(getResources().getString(R.string.pref_general_key_device_location))) {
                startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                        .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY, new CommandBTS().getMessage()));
            }

            /*if (key.equals(Constants.BLUETOOTH_STATE_KEY)) {
                refreshBluetoothLayout();
            }*/

            /*if (key.equals(Constants.BLUETOOTH_SLAVES_CONNECTED_KEY)) {
                refreshBluetoothSlavesLayout();
            }*/

            if (key.equals(Constants.DATA_COLLECTION_SESSION_OBJECT_KEY)) {
                if (SharedPreferencesHelper.getDeviceLocationValue(DisplayActivity.this) != Constants.DEVICE_LOCATION_HAND)
                    updateTextviewSwitch(SharedPreferencesHelper.getDataCollectionState(DisplayActivity.this), R.id.ui_data_collection_custom_text);
            }
        }
    };

    View.OnLongClickListener mClickUploadButton = new View.OnLongClickListener() {

        public boolean onLongClick(View v) {

            if (!SharedPreferencesHelper.getUploadServiceState(DisplayActivity.this)) {
                startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                        .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY, new CommandFUS().getMessage()));
            } else {
                startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                        .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY, new CommandFUC().getMessage()));
            }
            return true;
        }

    };

    private void refreshConnectivityTextview(String firstMessage, String secondMessage) {

        TextView textviewFirst = (TextView) findViewById(R.id.ui_connectivity_first_message);
        assert textviewFirst != null;
        textviewFirst.setText(firstMessage);
        TextView textviewSecond = (TextView) findViewById(R.id.ui_connectivity_second_message);
        assert textviewSecond != null;
        textviewSecond.setText(secondMessage);
    }


    /**********************************************************************************************/
    /**********************************************************************************************/
    /*                          Data logger service delegate methods                               */
    /**********************************************************************************************/
    /**********************************************************************************************/

    @Override
    public void onError(int errorCode) {
        Log.d(TAG, "::onError Receiving from DataLoggerService delegate the error code " + errorCode);

        switch (errorCode) {
            case Constants.UI_ERROR_CODE_EXTERNAL_STORAGE:
            case Constants.UI_ERROR_CODE_SENSORS_DISABLED:
            case Constants.UI_ERROR_CODE_COLLECTORS_START:
                setChecked(mDataCollectionButton, false);
                break;
            case Constants.UI_ERROR_CODE_FILE_UPLOAD:
                SharedPreferencesHelper.setUploadServiceState(this, false);
                refreshConnectivityTextview(
                        getResources().getString(R.string.ui_connectivity_first_message_default_text),
                        SharedPreferencesHelper.getDateLastUpload(this));
                break;
        }

        String errorMessage = SharedPreferencesHelper.getUiErrorMessage(this, errorCode);
        Snackbar.make(mCoordinatorLayoutView, errorMessage, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onUploadProgress(int nbFilesUpload, int nbRemainingFilesUpload, String uploadRate, long pct) {
        if (SharedPreferencesHelper.getUploadServiceState(this)) {
            refreshConnectivityTextview(
                    "Processed " + (nbFilesUpload - nbRemainingFilesUpload) + " of " + nbFilesUpload + " files",
                    " Rate: " + uploadRate + " Pct: " + pct + "%"
            );
        }
    }

    @Override
    public void onUploadCancelled() {
        SharedPreferencesHelper.setUploadServiceState(this, false);
        refreshConnectivityTextview(
                getResources().getString(R.string.ui_connectivity_first_message_default_text),
                SharedPreferencesHelper.getDateLastUpload(this)
        );
    }

    @Override
    public void onUploadComplete() {
        SharedPreferencesHelper.setUploadServiceState(this, false);
        String dateFormat = getResources().getString(R.string.ui_connectivity_upload_file_date_dateformat);
        String date = new SimpleDateFormat(dateFormat).format(Calendar.getInstance().getTime());
        SharedPreferencesHelper.setDateLastUpload(this, date);
        refreshConnectivityTextview(
                getResources().getString(R.string.ui_connectivity_first_message_default_text),
                date
        );
    }


    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ui_data_collection_button:
                if (!SharedPreferencesHelper.getDataCollectionState(DisplayActivity.this)) {
                    startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                            .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY,
                                    new CommandDCE(true).getMessage()));
                    setChecked(mDataCollectionButton, true);
                    MenuItem flagEventItem = menu.findItem(R.id.action_flag_event);
                    flagEventItem.setVisible(true);
                    updateTextviewSwitch(true, R.id.ui_data_collection_custom_text);
                    mLabelAnnotationButton.setEnabled(true);
                } else {
                    startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                            .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY,
                                    new CommandDCE(false).getMessage()));
                    setChecked(mDataCollectionButton, false);
                    MenuItem flagEventItem = menu.findItem(R.id.action_flag_event);
                    flagEventItem.setVisible(false);
                    updateTextviewSwitch(false, R.id.ui_data_collection_custom_text);
                    mLabelAnnotationButton.setEnabled(false);
                }
                break;
            case R.id.ui_label_annotation_button:
                if (!SharedPreferencesHelper.getLabelsAnnotationState(DisplayActivity.this)) {
                    startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                            .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY,
                                    new CommandLAE(true,
                                            SharedPreferencesHelper.getSelectedActivityLabel(DisplayActivity.this),
                                            SharedPreferencesHelper.getSelectedBodyPositionLabel(DisplayActivity.this),
                                            SharedPreferencesHelper.getSelectedLocationLabel(DisplayActivity.this)
                                    ).getMessage()
                            )
                    );
                    SharedPreferencesHelper.setLabelsAnnotationStartingTime(DisplayActivity.this, System.currentTimeMillis());
                    onLabelTimerStart(0);
                    setChecked(mLabelAnnotationButton, true);
                    updateTextviewSwitch(true, R.id.ui_label_annotation_custom_text);
                    mDataCollectionButton.setEnabled(false);
                } else {
                    long lengthLabelMillis = System.currentTimeMillis() - SharedPreferencesHelper.getLabelsAnnotationStartingTime(DisplayActivity.this);
                    SharedPreferencesHelper.addLabelAnnotationTime(DisplayActivity.this,
                            SharedPreferencesHelper.getAnnotatedActivityLabel(DisplayActivity.this),
                            (int) (lengthLabelMillis / 1000));

                    SharedPreferencesHelper.setLabelsAnnotationStartingTime(DisplayActivity.this, 0);
                    mLabelsChronometer.stop();
                    startService(new Intent(DisplayActivity.this, DataLoggerService.class)
                            .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY,
                                    new CommandLAE(false).getMessage()
                            )
                    );
                    setChecked(mLabelAnnotationButton, false);
                    updateTextviewSwitch(false, R.id.ui_label_annotation_custom_text);
                    mDataCollectionButton.setEnabled(true);
                }
                break;
        }
        return true;
    }

}




