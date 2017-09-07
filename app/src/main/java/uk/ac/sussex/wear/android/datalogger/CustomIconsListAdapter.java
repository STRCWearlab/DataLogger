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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomIconsListAdapter extends BaseAdapter {
    private TypedArray mIcons;
    private String[] mNames;
    private int mColorText;
    private LayoutInflater mInflater;

    public CustomIconsListAdapter(Context applicationContext, TypedArray icons, String[] names) {
        this(applicationContext, icons, names, ContextCompat.getColor(applicationContext, R.color.colorSpinnerOn));
    }

    public CustomIconsListAdapter(Context context, TypedArray icons, String[] names, int colorText) {
        mNames = names;
        mIcons = icons;
        mColorText = colorText;
        mInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return mNames.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = mInflater.inflate(R.layout.custom_icons_list_item, null);
        ImageView icon = (ImageView) view.findViewById(R.id.imageView);
        icon.setImageResource(mIcons.getResourceId(i, -1));
        TextView names = (TextView) view.findViewById(R.id.textView);
        names.setTextColor(mColorText);
        names.setText(mNames[i]);
        return view;
    }

    public void clearAll() {
        mNames = new String[mNames.length];
    }


    public void addAll(String[] names) {
        mNames = names;
    }
}