package com.flipkart.gap.usl.container.utils;

import com.flipkart.gap.usl.container.exceptions.InvalidRequestException;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;



/**
 * Created by vinay.lodha on 22/05/18.
 */
public class USLPreconditions {

    public static void checkNotEmpty(Collection collection, String message) throws InvalidRequestException {
        if (CollectionUtils.isEmpty(collection)) {
            throw new InvalidRequestException(message);
        }
    }

    public static <T>  void checkNotNull(T t, String message) throws InvalidRequestException {
        if (t == null) {
            throw new InvalidRequestException(message);
        }
    }
}
