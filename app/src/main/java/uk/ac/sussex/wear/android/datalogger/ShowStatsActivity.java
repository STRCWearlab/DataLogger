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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.concurrent.TimeUnit;

/**
 * Created by mathias on 15/02/17.
 */
public class ShowStatsActivity extends Activity implements View.OnClickListener {

    Button clearButton;
    Button cancelButton;
    ListView labelListView;
    String[] activitiesLabelsArray;
    CustomIconsListAdapter labelsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_stats);

        clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(this);

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);

        labelListView = (ListView) findViewById(R.id.labels_list);

        activitiesLabelsArray = getResources().getStringArray(R.array.activities_names_array);
        String[] adapterArray = new String[activitiesLabelsArray.length];
        for (int i = 0; i < activitiesLabelsArray.length; i++) {
            int annotatedSeconds = SharedPreferencesHelper.getLabelAnnotationTime(this, i);
            long minutes = TimeUnit.SECONDS.toMinutes(annotatedSeconds);
            adapterArray[i] = String.format("%-15s", activitiesLabelsArray[i] + " time:  ")
                    + String.format("%02d", minutes);
        }

        labelsAdapter = new CustomIconsListAdapter(
                this,
                getResources().obtainTypedArray(R.array.activities_icons_array),
                adapterArray);
        labelListView.setAdapter(labelsAdapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.cancel_button:
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.cancel_stats_confirmation_title))
                        .setMessage(R.string.cancel_stats_confirmation_message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShowStatsActivity.this.finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
            case R.id.clear_button:
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.clear_stats_confirmation_title))
                        .setMessage(R.string.clear_stats_confirmation_message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < activitiesLabelsArray.length; i++) {
                                    SharedPreferencesHelper.setLabelAnnotationTime(ShowStatsActivity.this, i, 0);
                                }
                                labelsAdapter.clearAll();
                                String[] adapterArray = new String[activitiesLabelsArray.length];
                                for (int i = 0; i < activitiesLabelsArray.length; i++) {
                                    int annotatedSeconds = SharedPreferencesHelper.getLabelAnnotationTime(ShowStatsActivity.this, i);
                                    long minutes = TimeUnit.SECONDS.toMinutes(annotatedSeconds);
                                    adapterArray[i] = String.format("%-15s", activitiesLabelsArray[i] + " time:  ")
                                            + String.format("%02d", minutes);
                                }
                                labelsAdapter.addAll(adapterArray);
                                labelsAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShowStatsActivity.this.finish();
                            }
                        })
                        .show();
                break;
        }
    }
}
