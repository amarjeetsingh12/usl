package com.flipkart.gap.usl.core.store.event.mongo;

public class MongoConstants {
    public static final class EVENT {
        public static final String EVENT_ID = "eventId";
        public static final String SAMPLE_DATA = "sampleData";
        public static final String VALIDATOR = "validations";
        public static final String SOURCE = "source";
        public static final String PIVOT_PATH = "pivotPath";
    }

    public static final class EVENT_MAPPING {
        public static final String SOURCE_EVENT_ID = "sourceEventId";
        public static final String DIMENSION_UPDATE_EVENT = "dimensionUpdateEvent";
        public static final String MAPPING = "mapping";
        public static final String ACTIVE = "active";
        public static final String UPDATED = "updated";
    }
}
