package com.flipkart.gap.usl.container.filters;

import com.flipkart.gap.usl.container.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Created by ajaysingh on 31/03/17.
 */

public class RequestFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestFilter.class.getSimpleName());

    private static String serverIP = null;

    @Override
    public void init(FilterConfig filterConfig) {
        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.exit(-1);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String serverRequestId = "R-" + UUID.randomUUID().toString();
        boolean isTestRequest = Boolean.parseBoolean(httpRequest.getHeader(Constants.X_PERF_TEST));
        String customerUserAgent = httpRequest.getHeader(Constants.USER_AGENT_HEADER_KEY);
        if (isTestRequest) {
            logger.info("Test Request received. Request ID {}", serverRequestId);
        }
        insertIntoThreadLocalDispatchContext(serverRequestId, serverIP, isTestRequest, customerUserAgent);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            clearThreadLocalDispatchContext();
        }
    }

    @Override
    public void destroy() {

    }

    private void insertIntoThreadLocalDispatchContext(String serverRequestId, String serverIP, boolean isTestRequest, String customerUserAgent) {
        org.slf4j.MDC.put(Constants.X_SERVER_IP, serverIP);
        org.slf4j.MDC.put(Constants.X_SERVER_REQUEST_ID, serverRequestId);
        org.slf4j.MDC.put(Constants.X_PERF_TEST, Boolean.toString(isTestRequest));
        org.slf4j.MDC.put(Constants.USER_AGENT_HEADER_KEY, customerUserAgent);
    }

    private void clearThreadLocalDispatchContext() {
        org.slf4j.MDC.remove(Constants.X_SERVER_IP);
        org.slf4j.MDC.remove(Constants.X_SERVER_REQUEST_ID);
        org.slf4j.MDC.remove(Constants.X_PERF_TEST);
        org.slf4j.MDC.remove(Constants.X_DOMAIN);
    }
}