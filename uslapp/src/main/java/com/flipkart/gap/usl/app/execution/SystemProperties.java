package com.flipkart.gap.usl.app.execution;

import com.flipkart.gap.usl.core.exception.InvalidClientException;
import com.flipkart.gap.usl.core.exception.InvalidEnvironmentException;
import org.apache.commons.lang3.EnumUtils;

/**
 * Created by piyush.chhabra on 25/03/2019
 */
public class SystemProperties {

    public static final String CLIENT_NAME_PROPERTY_IDENTIFIER = "gap.usl.clientId";
    public static final String ENVIRONMENT_NAME_PROPERTY_IDENTIFIER = "gap.usl.environment";
    private static USLClient uslClient;
    private static USLEnvironment uslEnvironment;

    public static USLClient getUslClient() {
        if (uslClient == null) {
            setUslClient(System.getProperty(CLIENT_NAME_PROPERTY_IDENTIFIER));
        }
        return uslClient;
    }

    public static USLEnvironment getUslEnvironment() {
        if (uslEnvironment == null) {
            setUslEnvironment(System.getProperty(ENVIRONMENT_NAME_PROPERTY_IDENTIFIER));
        }
        return uslEnvironment;
    }

    private static void setUslClient(String clientId) {
        if (clientId == null) {
            throw new InvalidClientException("Client can not be null");
        }

        uslClient = EnumUtils.getEnum(USLClient.class, clientId.toUpperCase());

        if (uslClient == null) {
            throw new InvalidClientException("Invalid client");
        }
    }

    private static void setUslEnvironment(String environment) {
        if (environment == null) {
            throw new InvalidEnvironmentException("Environment can not be null");
        }

        uslEnvironment = EnumUtils.getEnum(USLEnvironment.class, environment.toUpperCase());

        if (uslEnvironment == null) {
            throw new InvalidEnvironmentException("Invalid environment");
        }
    }


}
