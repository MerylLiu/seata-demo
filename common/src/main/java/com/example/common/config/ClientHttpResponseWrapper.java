package com.example.common.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class ClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse response;
    @Nullable
    private PushbackInputStream pushbackInputStream;

    public ClientHttpResponseWrapper(ClientHttpResponse response) throws IOException {
        this.response = response;
    }

    public boolean hasMessageBody() throws IOException {
        HttpStatus status = HttpStatus.resolve(this.getRawStatusCode());
        if (status == null || !status.is1xxInformational() && status != HttpStatus.NO_CONTENT && status != HttpStatus.NOT_MODIFIED) {
            return this.getHeaders().getContentLength() != 0L;
        } else {
            return false;
        }
    }

    public boolean hasEmptyMessageBody() throws IOException {
        InputStream body = this.response.getBody();
        if (body == null) {
            return true;
        } else if (body.markSupported()) {
            body.mark(1);
            if (body.read() == -1) {
                return true;
            } else {
                body.reset();
                return false;
            }
        } else {
            this.pushbackInputStream = new PushbackInputStream(body);
            int b = this.pushbackInputStream.read();
            if (b == -1) {
                return true;
            } else {
                this.pushbackInputStream.unread(b);
                return false;
            }
        }
    }

    public HttpHeaders getHeaders() {
        return this.response.getHeaders();
    }

    public InputStream getBody() throws IOException {
        return (InputStream)(this.pushbackInputStream != null ? this.pushbackInputStream : this.response.getBody());
    }

    public HttpStatus getStatusCode() throws IOException {
        return this.response.getStatusCode();
    }

    public int getRawStatusCode() throws IOException {
        return this.response.getRawStatusCode();
    }

    public String getStatusText() throws IOException {
        return this.response.getStatusText();
    }

    public void close() {
        this.response.close();
    }
}
