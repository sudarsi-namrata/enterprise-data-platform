package com.enterprise.platform.integration;

import com.enterprise.platform.service.IngestionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Profile("kafka")
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final IngestionService ingestionService;

    public KafkaConsumerService(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @KafkaListener(topics = "${app.kafka.topics.meter-data:meter-data-events}", groupId = "data-platform")
    public void consumeMeterData(String message) {
        processMessage(message, "METER_READ");
    }

    @KafkaListener(topics = "${app.kafka.topics.customer-data:customer-data-events}", groupId = "data-platform")
    public void consumeCustomerData(String message) {
        processMessage(message, "CUSTOMER");
    }

    @KafkaListener(topics = "${app.kafka.topics.billing-data:billing-data-events}", groupId = "data-platform")
    public void consumeBillingData(String message) {
        processMessage(message, "BILLING_RECORD");
    }

    private void processMessage(String message, String recordType) {
        try {
            JsonNode json = mapper.readTree(message);
            String externalId = json.has("id") ? json.get("id").asText() : null;
            Long sourceId = json.has("sourceId") ? json.get("sourceId").asLong() : 1L;

            ingestionService.ingestSingle(sourceId, externalId, recordType, message);
            log.debug("Consumed {} record: {}", recordType, externalId);
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage());
            // In production: send to DLQ topic
        }
    }
}
