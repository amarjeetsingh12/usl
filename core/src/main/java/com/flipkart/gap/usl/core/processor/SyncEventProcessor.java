package com.flipkart.gap.usl.core.processor;

import com.flipkart.gap.usl.core.model.external.ExternalEventSchema;
import com.flipkart.gap.usl.core.processor.exception.ProcessingException;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;

/**
 * Created by ashish.khatkar on 16/10/18.
 */
public interface SyncEventProcessor {
    ProcessingStageData processEvent(ExternalEventSchema externalEvent) throws ProcessingException;
}
