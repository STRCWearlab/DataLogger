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

package uk.ac.sussex.wear.android.datalogger.data;

import android.util.Log;

import java.util.ArrayList;

// parent class for commands

public abstract class CommandBase {

    private static final String TAG = CommandBase.class.getSimpleName();

    public static final int MAX_LENGTH = 70;

    protected static final String COMMAND_START = "##";
    protected static final String COMMAND_SEPARATOR = "!";
    protected static final String PARAMETER_SEPARATOR = ";";
    protected static final String FILLING_TOKEN = "*";

    public static final String COMMAND_DATA_COLLECTION_EVENT = "DCE";
    public static final String COMMAND_LABEL_ANNOTATION_EVENT = "LAE";
    public static final String COMMAND_BLUETOOTH_START = "BTS";
    public static final String COMMAND_BLUETOOTH_CONNECT = "BTC";
    public static final String COMMAND_FILES_UPLOAD_START = "FUS";
    public static final String COMMAND_FILES_UPLOAD_CANCEL = "FUC";
    public static final String COMMAND_KEEP_ALIVE_EVENT = "KAE";
    public static final String COMMAND_FLAG_EVENT = "FLE";

    public abstract String getMessage();

    protected static String extendCommand(String command){
        while (command.length() < MAX_LENGTH){
            command += FILLING_TOKEN;
        }
        return command;
    }

    public String getMessageBluetooth(){
        return extendCommand(getMessage());
    }

    public static ArrayList<String> parseMessage(String message) {
        ArrayList<String> ret = new ArrayList<>();
        Log.i(TAG, "::parseCommand Parsing message '" + message + "'. Size in bytes: " + message.length());
        for (String command : message.split(COMMAND_START)) {
            String code = command.split(COMMAND_SEPARATOR)[0];
            if (!code.equals("")){
                ret.add(code);
                if (command.split(COMMAND_SEPARATOR).length > 1) {
                    String[] params = command.split(COMMAND_SEPARATOR)[1].split(PARAMETER_SEPARATOR);
                    for (String param : params) {
                        if (param.indexOf(FILLING_TOKEN) == -1) {
                            ret.add(param);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static boolean containsCommand(ArrayList<String> list, String command){
        for (String element : list){
            if (element.equals(command))
                return true;
        }
        return false;
    }

}
