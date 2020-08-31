package com.flipkart.gap.usl.core.constant;


/**
 * Created by amarjeet.singh on 18/10/16.
 */
public class Constants {
    public static final int DIMENSION_VERSION = 1;
    public static final String LOCAL_ENVIRONEMT =  "local";
    public static String CONSUMER_PATH = "/consumers/usl-spark-worker";
    public static String OFFSET_PATH = String.format("%s/%s", CONSUMER_PATH, "offsets");

    public static class Metrics{
        public static String SUCCESS_METER="EVENT_SUCCESS";
        public static String VALIDATION_FAILED="VALIDATION_FAILED";
        public static String EXTERNAL_EVENT="EXTERNAL_EVENT";
        public static String PROCESS_EXCEPTION="PROCESS_EXCEPTION";
        public static final String FETCH_BATCH_SIZE="FETCH_BATCH_SIZE";
        public static final String PROCESS_BATCH_SIZE="PROCESS_BATCH_SIZE";
        public static final String PERSIST_BATCH_SIZE="PERSIST_BATCH_SIZE";
        public static final String ENTITY_ID_MISSING = "com.flipkart.gap.usl.meter.event.entityIdMissing";
        public static final String ENTITY_ID_MISSING_NAME_PATTERN = "com.flipkart.gap.usl.meter.event.entityIdMissing.%s.%s";
        public static final String VALIDATION_FAILURE_PATTERN = "com.flipkart.gap.usl.meter.event.validation.failure";
        public static final String VALIDATION_FAILURE_NAME_PATTERN = "com.flipkart.gap.usl.meter.event.failure.%s";
        public static final String DIMENSION_UPDATE_FAILURE_PATTERN = "com.flipkart.gap.usl.meter.dimension.update.failure";
        public static final String DIMENSION_UPDATE_FAILURE_NAME_PATTERN = "com.flipkart.gap.usl.meter.dimension.update.failure.%s";
        public static final String DIMENSION_UPDATE_SUCCESS_NAME_PATTERN = "com.flipkart.gap.usl.meter.dimension.update.success.%s";
        public static final String DIMENSION_UPDATE_SUCCESS_PATTERN = "com.flipkart.gap.usl.meter.dimension.update.success";
        public static final String DIMENSION_MERGE_SUCCESS_NAME_PATTERN = "com.flipkart.gap.usl.meter.dimension.merge.success.%s";
        public static final String DIMENSION_MERGE_SUCCESS_PATTERN = "com.flipkart.gap.usl.meter.dimension.merge.success";
        public static final String DIMENSION_MERGE_FAILURE_PATTERN = "com.flipkart.gap.usl.meter.dimension.merge.failure";
        public static final String DIMENSION_MERGE_FAILURE_NAME_PATTERN = "com.flipkart.gap.usl.meter.dimension.merge.failure.%s";
        public static final String EXTERNAL_EVENT_NOT_REGISTERED = "event.external.notRegistered";
        public static final String EXTERNAL_EVENT_NOT_REGISTERED_NAME_PATTERN = "event.external.notRegistered.%s";
        public static final String EXTERNAL_EVENT_MISSING_NAME = "event.external.missingName";
        public static final String EVENT_MAPPING_PARSING_STARTED = "event.mapping.parsing.started";
        public static final String EVENT_MAPPING_PARSING_STARTED_PATTERN = "event.mapping.parsing.started.%s.%s";
        public static final String EVENT_MAPPING_PARSING_SUCCESS = "event.mapping.parsing.success";
        public static final String EVENT_MAPPING_PARSING_SUCCESS_PATTERN = "event.mapping.parsing.success.%s.%s";
        public static final String EVENT_MAPPING_PARSING_FAILURE = "event.mapping.parsing.failure";
        public static final String EVENT_MAPPING_PARSING_FAILURE_PATTERN = "event.mapping.parsing.failure.%s.%s";
        public static final String NO_INTERNAL_EVENT = "event.internal.notPresent";
        public static final String NO_INTERNAL_EVENT_PATTERN = "event.internal.notPresent.%s";
        public static final String NOT_DIMENSION_EVENT = "event.mapping.notDimensionEvent";
        public static final String NOT_DIMENSION_EVENT_PATTERN = "event.mapping.notDimensionEvent.%s.%s";
        public static final String STREAM_PROCESSING_TIME = "com.flipkart.gap.usl.eventStream.endtoend.latency.%s";
    }

    public static final class Stream {
        public static final String GROUP_ID = "usl-event-processor";
        public static final int GROUP_KEY_PARTITION_COUNT = 100;
    }

    public static final String X_PERF_TEST = "X-PERF-TEST";
    public static final String LOG_ID = "logId";
    public static final String DELIMETER = "|";

    public static final class Event{
        public static final String DUMMY_MERGE_EVENT="DummyMergeEvent";
        public static final String DUMMY_UPDATE_EVENT="DummyUpdateEvent";
    }
}
