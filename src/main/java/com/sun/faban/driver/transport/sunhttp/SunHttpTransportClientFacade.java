/* The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html or
 * install_dir/legal/LICENSE
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at install_dir/legal/LICENSE.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faban.driver.transport.sunhttp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.faban.driver.HttpClientFacade;
import com.sun.faban.driver.HttpRequestMethod;
import com.sun.faban.driver.MultiPartForm;

/**
 * Implements the HttpClientFacade around an ExtensibleSunHttpTransport
 * instance.
 */
public class SunHttpTransportClientFacade extends SunHttpTransport implements
        HttpClientFacade {
    public static SunHttpTransportClientFacade newInstance() {
        return new SunHttpTransportClientFacade();
    }

    @Override
    public FetchRequest asFetchRequest() {
        return new FetchRequestView();
    }

    @Override
    public MatchRequest asMatchRequest() {
        return new MatchRequestView();
    }

    @Override
    public ReadRequest asReadRequest() {
        return new ReadRequestView();
    }

    public HttpURLConnection doRequest(URL url, HttpRequestMethod method,
            RequestSubmissionType requestType, byte[] postRequest,
            Map<String, String> headers, Map<String, Object> multipart)
            throws IOException {
        HttpURLConnection c = super.getConnection(url);

        MultiPartForm form = null;
        String boundary = null;

        if (RequestSubmissionType.POST.equals(requestType)) {
            if (headers != null) {
                checkContentType(headers);
            } else {
                headers = postHeadersBinary;
            }
        } else if (RequestSubmissionType.MULTIPART.equals(requestType)) {
            boundary = MultiPartForm.createRandomBoundary();
            headers.put("Content-type", MultiPartForm.getContentType(boundary));
        }

        c.setRequestMethod(method.name());

        setHeaders(c, headers);
        c.setDoInput(true);

        if (RequestSubmissionType.POST.equals(requestType)) {
            c.setDoOutput(true);
            OutputStream out = c.getOutputStream();
            out.write(postRequest);
            out.flush();
            out.close();
        } else if (RequestSubmissionType.MULTIPART.equals(requestType)) {
            c.setDoOutput(true);
            OutputStream out = c.getOutputStream();
            form = new MultiPartForm(out, boundary);

            for (Map.Entry<String, Object> entry : multipart.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();
                if (fieldValue instanceof String) {
                    form.outputString(fieldName, (String) fieldValue);
                } else if (fieldValue instanceof MultiPartFile) {
                    MultiPartFile multiFile = (MultiPartFile) fieldValue;
                    form.outputFile(fieldName, multiFile.getMimeType(),
                            multiFile.getFile());
                }
            }

            form.close();
            out.flush();
            out.close();
        }

        return c;
    }

    public int readURL(URL url, HttpRequestMethod method,
            RequestSubmissionType requestType, byte[] postRequest,
            Map<String, String> headers, Map<String, Object> multipart)
            throws IOException {
        HttpURLConnection c = doRequest(url, method, requestType, postRequest,
                headers, multipart);

        responseCode = c.getResponseCode();
        responseHeader = c.getHeaderFields();

        return readResponse(c);
    }

    public StringBuilder fetchURL(URL url, HttpRequestMethod method,
            RequestSubmissionType requestType, byte[] postRequest,
            Map<String, String> headers, Map<String, Object> multipart)
            throws IOException {
        HttpURLConnection c = doRequest(url, method, requestType, postRequest,
                headers, multipart);

        return fetchResponse(c);
    }

    public abstract class HttpRequestView<U> implements HttpRequest<U> {
        protected URL url = null;
        protected Map<String, String> headers = new LinkedHashMap<String, String>();
        protected HttpRequestMethod method = HttpRequestMethod.GET;
        protected byte[] postBody = null;
        protected Map<String, Object> multipart = new LinkedHashMap<String, Object>();
        protected RequestSubmissionType requestType = RequestSubmissionType.GET;

        @Override
        public void addTextType(String texttype) {
            SunHttpTransportClientFacade.this.addTextType(texttype);
        }

        @Override
        public int getContentSize() {
            return SunHttpTransportClientFacade.this.getContentSize();
        }

        @Override
        public String[] getCookieValuesByName(String name) {
            return SunHttpTransportClientFacade.this
                    .getCookieValuesByName(name);
        }

        @Override
        public StringBuilder getResponseBuffer() {
            return SunHttpTransportClientFacade.this.getResponseBuffer();
        }

        @Override
        public int getResponseCode() {
            return SunHttpTransportClientFacade.this.getResponseCode();
        }

        @Override
        public String[] getResponseHeader(String name) {
            return SunHttpTransportClientFacade.this.getResponseHeader(name);
        }

        @Override
        public boolean isFollowRedirects() {
            return SunHttpTransportClientFacade.this.isFollowRedirects();
        }

        @Override
        public void setFollowRedirects(boolean follow) {
            SunHttpTransportClientFacade.this.setFollowRedirects(follow);
        }

        @Override
        public HttpRequest<U> withHeaders(Map<String, String> headers) {
            this.headers.clear();
            this.headers.putAll(headers);

            return this;
        }

        @Override
        public HttpRequest<U> withHeader(String name, String value) {
            this.headers.put(name, value);

            return this;
        }

        @Override
        public HttpRequest<U> withPostRequest(byte[] postRequest) {
            this.requestType = RequestSubmissionType.POST;
            this.postBody = postRequest;

            return this;
        }

        @Override
        public HttpRequest<U> withPostRequest(String postRequest) {
            this.requestType = RequestSubmissionType.POST;
            this.postBody = postRequest.getBytes();

            return this;
        }

        @Override
        public HttpRequest<U> withMultipartFormField(String name, String value) {
            this.requestType = RequestSubmissionType.MULTIPART;
            this.multipart.put(name, value);

            return this;
        }

        @Override
        public HttpRequest<U> withMultipartFormField(String name,
                String mimeType, File file) {
            this.requestType = RequestSubmissionType.MULTIPART;
            this.multipart.put(name, new MultiPartFile(mimeType, file));

            return this;
        }

        @Override
        public HttpRequest<U> withRequestMethod(HttpRequestMethod method) {
            this.method = method;

            return this;
        }

        @Override
        public HttpRequest<U> withUrl(String url) {
            try {
                this.url = new URL(url);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return this;
        }
    }

    public class FetchRequestView extends HttpRequestView<StringBuilder>
            implements FetchRequest {
        @Override
        public StringBuilder execute() throws IOException {
            SunHttpTransportClientFacade.this.fetchURL(this.url, this.method,
                    this.requestType, this.postBody, this.headers,
                    this.multipart);

            return this.getResponseBuffer();
        }
    }

    public class ReadRequestView extends HttpRequestView<Integer> implements
            ReadRequest {
        @Override
        public Integer execute() throws IOException {
            return SunHttpTransportClientFacade.this.readURL(this.url,
                    this.method, this.requestType, this.postBody, this.headers,
                    this.multipart);
        }
    }

    public class MatchRequestView extends HttpRequestView<Boolean> implements
            MatchRequest {
        protected String regex;

        @Override
        public MatchRequest withRegex(String regex) {
            this.regex = regex;

            return this;
        }

        @Override
        public Boolean execute() throws IOException {
            if (this.regex == null) {
                throw new IllegalStateException("No regex provided!");
            }

            SunHttpTransportClientFacade.this.fetchURL(this.url, this.method,
                    this.requestType, this.postBody, this.headers,
                    this.multipart);

            return SunHttpTransportClientFacade.this.matchResponse(regex);
        }
    }

    public class MultiPartFile {
        protected final String mimeType;
        protected final File file;

        public MultiPartFile(String mimeType, File file) {
            this.mimeType = mimeType;
            this.file = file;
        }

        public String getMimeType() {
            return mimeType;
        }

        public File getFile() {
            return file;
        }
    }
}
