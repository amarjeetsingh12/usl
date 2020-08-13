package com.flipkart.gap.usl.container.responseobjects;

import lombok.Data;

@Data
public class HttpResponseObject<T> {
    T responseObject;
    int httpResponseCode;

    public HttpResponseObject(int httpResponseCode, T responseObject) {
        this.responseObject = responseObject;
        this.httpResponseCode = httpResponseCode;
    }

    public boolean isEmpty() {
        return responseObject == null;
    }
}
