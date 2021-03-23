package com.flipkart.gap.usl.container.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.gap.usl.client.kafka.KafkaEventRequest;
import com.flipkart.gap.usl.client.kafka.KafkaProducerClient;
import com.flipkart.gap.usl.client.kafka.ProducerEvent;
import com.flipkart.gap.usl.container.eventIngestion.EventIngestionConfig;
import com.flipkart.gap.usl.container.utils.SyncEventProcessorService;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.model.event.ExternalEvent;
import com.flipkart.gap.usl.core.model.external.ExternalEventSchema;
import com.flipkart.gap.usl.core.store.dimension.resilence.DecoratorExecutionException;
import com.flipkart.gap.usl.core.store.dimension.resilence.ResilenceDecorator;
import com.flipkart.gap.usl.core.store.event.EventTypeDBWrapper;
import com.flipkart.gap.usl.core.validator.SchemaValidator;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by ashish.khatkar on 14/02/18.
 */
@Slf4j
public class EventIngestorService {
    @Inject
    @Named("ingestionKafkaClient")
    private KafkaProducerClient kafkaProducerClient;

    @Inject
    private EventTypeDBWrapper eventTypeDBWrapper;

    @Inject
    private SyncEventProcessorService syncEventProcessorService;

    @Inject
    @Named("eventIngestionConfig")
    private EventIngestionConfig eventIngestionConfig;
    @Inject
    private ResilenceDecorator resilenceDecorator;
    private static final String EVENT_INGESTION_RESILIENCE_CONFIG = "eventIngestionConfig";

    /**
     * function to ingest the data to according to relative ingestionType
     *
     * @param payload
     * @param eventName
     * @param ingestionType
     * @return
     */
    public EventIngestorResponse ingest(ObjectNode payload, String eventName, IngestionType ingestionType) {
        ExternalEvent externalEvent;
        try {
            externalEvent = eventTypeDBWrapper.getExternalEvent(eventName);
        } catch (Throwable t) {
            log.error("External event mapping missing for event {}", eventName, t);
            markFailureMeters(eventName);
            return EventIngestorResponse.EVENT_MISSING;
        }
        try {
            if (SchemaValidator.getInstance().schemaApply(payload, externalEvent.getValidations())) {
                switch (ingestionType) {
                    case Sync:
                        return syncEventIngestion(payload, eventName);
                    case Async:
                        return kafkaEventIngestion(payload, eventName);
                    default:
                        log.error("Method not yet implemented");
                        return EventIngestorResponse.INGESTION_FAILURE;
                }
            } else {
                log.error("Event validation failed for payload {}", payload);
                markFailureMeters(eventName);
                return EventIngestorResponse.VALIDATION_FAILURE;
            }
        } catch (Throwable t) {
            log.error("Some error occurred, payload {}", payload, t);
            markFailureMeters(eventName);
            return EventIngestorResponse.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * ingesting events in sync in kafka
     *
     * @param payload
     * @param eventName
     * @return
     */
    private EventIngestorResponse kafkaEventIngestion(ObjectNode payload, String eventName) {
        try {
            resilenceDecorator.execute(EventIngestorService.EVENT_INGESTION_RESILIENCE_CONFIG, () -> {
                KafkaEventRequest kafkaEventRequest = new KafkaEventRequest(new ProducerEvent(eventName, payload), eventIngestionConfig.getKafkaTopicName());
                try {
                    kafkaProducerClient.sendEvent(kafkaEventRequest, ((metadata, exception) -> {
                        // if failed due to APIException try making sync call and if still fails log it.
                        if (exception != null) {
                            try {
                                kafkaProducerClient.sendEventSync(kafkaEventRequest);
                            } catch (Throwable t) {
                                markAPIFailureMeters(eventName);
                                log.error("Event failed to ingest due to ApiException. Event : {}, Payload {}", eventName, payload, exception);
                            }
                        }
                    }));
                    return true;
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (DecoratorExecutionException e) {
            log.error("Event failed to ingest into kafka {}", payload, e);
            markFailureMeters(eventName);
            return EventIngestorResponse.INGESTION_FAILURE;
        }
        markSuccessMeters(eventName);
        return EventIngestorResponse.INGESTION_SUCCESS;
    }

    /**
     * synchronous data ingestion in HBase
     *
     * @param payload
     * @param eventName
     * @return
     */
    private EventIngestorResponse syncEventIngestion(ObjectNode payload, String eventName) {
        try {
            ExternalEventSchema event = new ExternalEventSchema();
            event.setName(eventName);
            event.setPayload(payload);
            // Ignoring stage data as not needed here
            syncEventProcessorService.processEventAndGetStageData(event);
        } catch (Throwable t) {
            log.error("Unable to process and store the payload {}", payload, t);
            markFailureMeters(eventName);
            return EventIngestorResponse.INGESTION_FAILURE;
        }
        markSuccessMeters(eventName);
        return EventIngestorResponse.INGESTION_SUCCESS;
    }

    /**
     * function to register the failures and successes of requests
     *
     * @param eventName
     */
    private void markFailureMeters(String eventName) {
        JmxReporterMetricRegistry.getInstance().markMeter("ingestionFailure");
        JmxReporterMetricRegistry.getInstance().markMeter(String.format("%s.ingestionFailure", eventName));
    }

    private void markSuccessMeters(String eventName) {
        JmxReporterMetricRegistry.getInstance().markMeter("ingestionSuccess");
        JmxReporterMetricRegistry.getInstance().markMeter(String.format("%s.ingestionSuccess", eventName));
    }

    private void markAPIFailureMeters(String eventName) {
        JmxReporterMetricRegistry.getInstance().markMeter("kafkaAPIFailure");
        JmxReporterMetricRegistry.getInstance().markMeter(String.format("%s.kafkaAPIFailure", eventName));
    }
}