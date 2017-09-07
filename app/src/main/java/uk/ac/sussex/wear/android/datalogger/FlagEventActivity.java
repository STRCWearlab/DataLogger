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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import uk.ac.sussex.wear.android.datalogger.data.CommandFLE;

public class FlagEventActivity extends Activity implements View.OnClickListener {

    Button confirmButton;
    Button cancelButton;
    EditText notesEditText;
    RadioGroup flagsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_event);

        flagsRadioGroup = (RadioGroup) findViewById(R.id.flags_radio_group);

        confirmButton = (Button) findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(this);

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);

        notesEditText = (EditText) findViewById(R.id.notes_text);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.confirm_button:
                String notes = notesEditText.getText().toString();
                if ("".equals(notes))
                    notes = " ";
                int radioButtonID = flagsRadioGroup.getCheckedRadioButtonId();
                View radioButton = flagsRadioGroup.findViewById(radioButtonID);
                int idx = flagsRadioGroup.indexOfChild(radioButton);
                startService(new Intent(FlagEventActivity.this, DataLoggerService.class)
                        .putExtra(Constants.COMMAND_SERVICE_INTENT_KEY,
                                new CommandFLE(this.getResources().getStringArray(R.array.flaggable_events)[idx], notes).getMessage()));
                this.finish();
                break;
            case R.id.cancel_button:
                this.finish();
                break;
        }
    }
}
