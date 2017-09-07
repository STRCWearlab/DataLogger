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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Defines the methods that has to be implemented by an HTTP connection.
 * If you're implementing your custom HTTP connection, remember to never cache anything,
 * especially in writeBody methods, as this will surely cause memory issues when uploading
 * large files. The only things which you are allowed to cache are the response code and body
 * from the server, which must not be large though.
 * @author gotev (Aleksandar Gotev)
 */
public interface HttpConnection {

    /**
     * Set request headers.
     * @param requestHeaders request headers to set
     * @param isFixedLengthStreamingMode true if the fixed length streaming mode must be used. If
     *                                   it's false, chunked streaming mode has to be used
     * @param totalBodyBytes total number of bytes
     * @throws IOException if an error occurs while setting request headers
     */
    void setHeaders(List<NameValue> requestHeaders, boolean isFixedLengthStreamingMode,
                    long totalBodyBytes) throws IOException;

    /**
     * Write a byte array into the request body.
     * @param bytes array with the bytes to write
     * @throws IOException if an error occurs while writing
     */
    void writeBody(byte[] bytes) throws IOException;

    /**
     * Write a portion of a byte array into the request body.
     * @param bytes array with the bytes to write
     * @param lengthToWriteFromStart how many bytes to write, starting from the first one in
     *                               the array
     * @throws IOException if an error occurs while writing
     */
    void writeBody(byte[] bytes, int lengthToWriteFromStart) throws IOException;

    /**
     * Gets the HTTP response code from the server.
     * @return an integer representing the HTTP response code (e.g. 200)
     * @throws IOException if an error occurs while getting the server response code
     */
    int getServerResponseCode() throws IOException;

    /**
     * Gets the server response body.
     * @return response body bytes
     * @throws IOException if an error occurs while getting the server response body
     */
    byte[] getServerResponseBody() throws IOException;

    /**
     * Gets the server response headers.
     * @return map containing all the response headers
     * @throws IOException if an error occurs while getting the server response headers
     */
    LinkedHashMap<String, String> getServerResponseHeaders() throws IOException;

    /**
     * Closes the connection and frees all the allocated resources.
     */
    void close();
}
