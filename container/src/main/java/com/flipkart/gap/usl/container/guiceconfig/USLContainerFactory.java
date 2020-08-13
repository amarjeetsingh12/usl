package com.flipkart.gap.usl.container.guiceconfig;

import com.flipkart.gap.usl.app.execution.USLClient;
import com.flipkart.gap.usl.app.execution.USLEnvironment;
import com.flipkart.gap.usl.container.USLContainer;
import com.flipkart.gap.usl.core.exception.InvalidClientException;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by piyush.chhabra on 09/04/2019
 */
@Slf4j
public class USLContainerFactory {

    public static USLContainer getContainer(USLClient client, USLEnvironment env) throws InvalidClientException {
        //TODO: Implement creation of your specific tenant's container
        switch (client) {
            default:
                throw new InvalidClientException(String.format("Invalid ClientId : %s", client));
        }
    }
}