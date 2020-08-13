package com.flipkart.gap.usl.core.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by piyush.chhabra on 25/03/2019
 */
@Slf4j
public enum HttpClientService {
    INSTANCE;

    public String executeGetRequest(String apiPath) {
        String responseString = "";
        HttpResponse response = null;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(apiPath);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            for (int retry = 0; retry < 3; retry++) {
                response = client.execute(request);
                if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    responseString = EntityUtils.toString(entity, "UTF-8");
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error Occurred while doing the http call for the request apiPath {}", apiPath);
        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException ioEx) {
                    log.error("HttpClientService:: Error closing clientResponse " + ioEx.getMessage());
                }
            }
        }

        return responseString;
    }

    public String executePostRequest(String apiPath, String requestBody) {
        String responseString = "";
        HttpResponse response = null;
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(apiPath);
            StringEntity requestEntity = new StringEntity(requestBody);
            httpPost.setEntity(requestEntity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            for (int retry = 0; retry < 3; retry++) {
                response = httpClient.execute(httpPost);
                if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    responseString = EntityUtils.toString(entity, "UTF-8");
                    break;
                }
            }
            //MetricsRegistry.markMeter(this.getClass(), POST_HTTP_REQUEST_SUCCESS);
        } catch (IOException e) {
            //MetricsRegistry.markMeter(this.getClass(), POST_HTTP_REQUEST_FAILURE);
            log.error("Error Occurred while doing the http call for the request apiPath {} and requestBody {} ", apiPath, requestBody);
        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException ioEx) {
                    log.error("HttpClientService:: Error closing clientResponse " + ioEx.getMessage());
                }
            }
        }

        return responseString;
    }
}