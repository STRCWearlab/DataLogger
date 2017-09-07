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

/**
 * HttpUrlConnection stack implementation.
 * @author gotev (Aleksandar Gotev)
 */
public class HurlStack implements HttpStack {

    private boolean mFollowRedirects;
    private boolean mUseCaches;
    private int mConnectTimeout;
    private int mReadTimeout;

    public HurlStack() {
        mFollowRedirects = true;
        mUseCaches = false;
        mConnectTimeout = 15000;
        mReadTimeout = 30000;
    }

    public HurlStack(boolean followRedirects,
                     boolean useCaches,
                     int connectTimeout,
                     int readTimeout) {
        mFollowRedirects = followRedirects;
        mUseCaches = useCaches;
        mConnectTimeout = connectTimeout;
        mReadTimeout = readTimeout;
    }

    @Override
    public HttpConnection createNewConnection(String method, String url) throws IOException {
        return new HurlStackConnection(method, url, mFollowRedirects, mUseCaches,
                mConnectTimeout, mReadTimeout);
    }

}