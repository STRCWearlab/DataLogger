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

package uk.ac.sussex.wear.android.datalogger.upload;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Generic HTTP Upload Task.<br>
 * Subclass to create your custom upload task.
 *
 * @author cankov
 * @author gotev (Aleksandar Gotev)
 * @author mabdurrahman
 */
public abstract class HttpUploadTask extends UploadTask {

    private static final String TAG = HttpUploadTask.class.getSimpleName();

    /**
     * Contains all the parameters set in {@link HttpUploadRequest}.
     */
    protected HttpUploadTaskParameters httpParams = null;

    /**
     * {@link HttpConnection} used to perform the upload task.
     */
    private HttpConnection connection;

    @Override
    protected void init(UploadService service, Intent intent) throws IOException {
        super.init(service, intent);
        this.httpParams = intent.getParcelableExtra(HttpUploadTaskParameters.PARAM_HTTP_TASK_PARAMETERS);
    }

    /**
     * Implementation of the upload logic.<br>
     * If you want to take advantage of the automations which Android Upload Service provides,
     * do not override or change the implementation of this method in your subclasses. If you do,
     * you have full control on how the upload is done, so for example you can use your custom
     * http stack, but you have to manually setup the request to the server with everything you
     * set in your {@link HttpUploadRequest} subclass and to get the response from the server.
     *
     * @throws Exception if an error occurs
     */
    @SuppressLint("NewApi")
    protected void upload() throws Exception {

        Log.d(TAG, "Starting upload task with ID " + params.getId());

        try {
            getSuccessfullyUploadedFiles().clear();
            uploadedBytes = 0;
            totalBytes = getBodyLength();

            if (httpParams.isCustomUserAgentDefined()) {
                httpParams.addRequestHeader("User-Agent", httpParams.getCustomUserAgent());
            }

            connection = UploadService.HTTP_STACK.createNewConnection(httpParams.getMethod(),
                    params.getServerUrl());

            connection.setHeaders(httpParams.getRequestHeaders(),
                    httpParams.isUsesFixedLengthStreamingMode(), getBodyLength());

            writeBody(connection);

            final int serverResponseCode = connection.getServerResponseCode();
            Log.d(TAG, "Server responded with HTTP " + serverResponseCode
                    + " to upload with ID: " + params.getId());

            // Broadcast completion only if the user has not cancelled the operation.
            // It may happen that when the body is not completely written and the client
            // closes the connection, no exception is thrown here, and the server responds
            // with an HTTP status code. Without this, what happened was that completion was
            // broadcasted and then the cancellation. That behaviour was not desirable as the
            // library user couldn't execute code on user cancellation.
            if (shouldContinue) {
                broadcastCompleted(serverResponseCode, connection.getServerResponseBody(),
                        connection.getServerResponseHeaders());
            }

        } finally {
            connection.close();
        }
    }

    /**
     * Implement in subclasses to provide the expected upload in the progress notifications.
     * @return The expected size of the http request body.
     * @throws UnsupportedEncodingException
     */
    protected abstract long getBodyLength() throws UnsupportedEncodingException;

    /**
     * Implement in subclasses to write the body of the http request.
     * @param connection connection on which to write the body
     * @throws IOException
     */
    protected abstract void writeBody(HttpConnection connection) throws IOException;

    protected final void writeStream(InputStream stream) throws IOException {
        byte[] buffer = new byte[UploadService.BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = stream.read(buffer, 0, buffer.length)) > 0 && shouldContinue) {
            connection.writeBody(buffer, bytesRead);
            uploadedBytes += bytesRead;
            broadcastProgress(uploadedBytes, totalBytes);
        }
    }

}
