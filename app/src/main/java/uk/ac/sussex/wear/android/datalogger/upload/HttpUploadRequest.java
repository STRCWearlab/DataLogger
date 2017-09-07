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

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Represents a generic HTTP upload request.<br>
 * Subclass to create your own custom HTTP upload request.
 *
 * @author gotev (Aleksandar Gotev)
 * @author eliasnaur
 * @author cankov
 */
public abstract class HttpUploadRequest extends UploadRequest {

    protected final HttpUploadTaskParameters httpParams = new HttpUploadTaskParameters();

    /**
     * Creates a new http upload request.
     *
     * @param context application context
     * @param uploadId unique ID to assign to this upload request. If is null or empty, a random
     *                 UUID will be automatically generated. It's used in the broadcast receiver
     *                 when receiving updates.
     * @param serverUrl URL of the server side script that handles the request
     */
    public HttpUploadRequest(final Context context, final String uploadId, final String serverUrl) {
        super(context, uploadId, serverUrl);
    }

    @Override
    protected void initializeIntent(Intent intent) {
        super.initializeIntent(intent);
        intent.putExtra(HttpUploadTaskParameters.PARAM_HTTP_TASK_PARAMETERS, httpParams);
    }

    @Override
    protected void validate() throws IllegalArgumentException, MalformedURLException {
        super.validate();

        if (!params.getServerUrl().startsWith("http://")
                && !params.getServerUrl().startsWith("https://")) {
            throw new IllegalArgumentException("Specify either http:// or https:// as protocol");
        }

        // Check if the URL is valid
        new URL(params.getServerUrl());

    }

    /**
     * Adds a header to this upload request.
     *
     * @param headerName header name
     * @param headerValue header value
     * @return {@link HttpUploadRequest}
     */
    public HttpUploadRequest addHeader(final String headerName, final String headerValue) {
        httpParams.addRequestHeader(headerName, headerValue);
        return this;
    }

    /**
     * Sets the HTTP Basic Authentication header.
     * @param username HTTP Basic Auth username
     * @param password HTTP Basic Auth password
     * @return {@link HttpUploadRequest}
     */
    public HttpUploadRequest setBasicAuth(final String username, final String password) {
        String auth = Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
        httpParams.addRequestHeader("Authorization", "Basic " + auth);
        return this;
    }

    /**
     * Adds a parameter to this upload request.
     *
     * @param paramName parameter name
     * @param paramValue parameter value
     * @return {@link HttpUploadRequest}
     */
    public HttpUploadRequest addParameter(final String paramName, final String paramValue) {
        httpParams.addRequestParameter(paramName, paramValue);
        return this;
    }

    /**
     * Adds a parameter with multiple values to this upload request.
     *
     * @param paramName parameter name
     * @param array values
     * @return {@link HttpUploadRequest}
     */
    public HttpUploadRequest addArrayParameter(final String paramName, final String... array) {
        for (String value : array) {
            httpParams.addRequestParameter(paramName, value);
        }
        return this;
    }

    /**
     * Adds a parameter with multiple values to this upload request.
     *
     * @param paramName parameter name
     * @param list values
     * @return {@link HttpUploadRequest}
     */
    public HttpUploadRequest addArrayParameter(final String paramName, final List<String> list) {
        for (String value : list) {
            httpParams.addRequestParameter(paramName, value);
        }
        return this;
    }

    /**
     * Sets the HTTP method to use. By default it's set to POST.
     *
     * @param method new HTTP method to use
     * @return {@link HttpUploadRequest}
     */
    public HttpUploadRequest setMethod(final String method) {
        httpParams.setMethod(method);
        return this;
    }

    /**
     * Sets the custom user agent to use for this upload request.
     * Note! If you set the "User-Agent" header by using the "addHeader" method,
     * that setting will be overwritten by the value set with this method.
     *
     * @param customUserAgent custom user agent string
     * @return {@link HttpUploadRequest}
     */
    public HttpUploadRequest setCustomUserAgent(String customUserAgent) {
        httpParams.setCustomUserAgent(customUserAgent);
        return this;
    }

    /**
     * Sets if this upload request is using fixed length streaming mode.
     * If it uses fixed length streaming mode, then the value returned by
     * {@link HttpUploadTask#getBodyLength()} will be automatically used to properly set the
     * underlying {@link java.net.HttpURLConnection}, otherwise chunked streaming mode will be used.
     * @param fixedLength true to use fixed length streaming mode (this is the default setting) or
     *                    false to use chunked streaming mode.
     * @return {@link HttpUploadRequest}
     */
    public HttpUploadRequest setUsesFixedLengthStreamingMode(boolean fixedLength) {
        httpParams.setUsesFixedLengthStreamingMode(fixedLength);
        return this;
    }
}
