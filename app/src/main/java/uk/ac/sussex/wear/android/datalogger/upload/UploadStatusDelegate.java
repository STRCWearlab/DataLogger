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

/**
 * Defines the methods that has to be implemented by a class who wants to listen for upload status
 * events.
 *
 * @author Aleksandar Gotev
 */
public interface UploadStatusDelegate {
    /**
     * Called when the upload progress changes. Override this method to add your own logic.
     *
     * @param uploadInfo upload status information
     */
    void onProgress(final UploadInfo uploadInfo);

    /**
     * Called when an error happens during the upload. Override this method to add your own logic.
     *
     * @param uploadInfo upload status information
     * @param exception exception that caused the error
     */
    void onError(final UploadInfo uploadInfo, final Exception exception);

    /**
     * Called when the upload is completed successfully. Override this method to add your own logic.
     *
     * @param uploadInfo upload status information
     * @param serverResponse response got from the server
     */
    void onCompleted(final UploadInfo uploadInfo, final ServerResponse serverResponse);

    /**
     * Called when the upload is cancelled. Override this method to add your own logic.
     *
     * @param uploadInfo upload status information
     */
    void onCancelled(final UploadInfo uploadInfo);
}

