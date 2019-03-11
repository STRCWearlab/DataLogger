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

package uk.ac.sussex.wear.android.datalogger.log;

import android.content.Context;
import android.os.SystemClock;

import java.io.File;

/**
 * Created by fjordonez on 13/10/16.
 */

// class for storing logs

public final class LoggerHelper {

    private static final String TAG = LoggerHelper.class.getSimpleName();

    public static File defineLogFilename(Context context, String path, String baseFilename, String extension, boolean toAppend, long nanosOffset){

        // Get the absolute path to the directory on the primary shared/external storage device
        // where the application can place persistent files.
        File absoluteDir = new File(context.getExternalFilesDir(null), path);
        if (!absoluteDir.exists()){
            absoluteDir.mkdirs();
        }

        String nanoTimeString = Long.toString(SystemClock.elapsedRealtimeNanos() + nanosOffset);
        File[] files = absoluteDir.listFiles();
        if (files.length == 0) { // In case there are no files in the dir
            // The first logging file is indexed as 0
            baseFilename = baseFilename + "__" + nanoTimeString + "__0" + "." + extension;
        } else {
            int lastIndex = -1;
            for (File file : files){
                if (file.isFile()){
                    String[] items = file.getName().split("\\.")[0].split("__");
                    // There must be two "__" tokens in the file name: sessionName__nanoTime__index
                    // items[0]=sessionName, items[1]=nanoTime. items[2]=index
                    int index = Integer.parseInt(items[2]);
                    // The index of the last file is obtained
                    if ((index > lastIndex) && (items[0].equals(baseFilename))){
                        lastIndex = index;
                        if (toAppend){
                            nanoTimeString = items[1];
                        }
                    }
                }
            }
            if (toAppend){
                baseFilename = baseFilename + "__" + nanoTimeString + "__" + lastIndex + "." + extension;
            } else {
                baseFilename = baseFilename + "__" + nanoTimeString + "__" + (lastIndex+1) + "." + extension;
            }
        }

        return new File(absoluteDir.getAbsolutePath() + File.separator + baseFilename);
    }

}
