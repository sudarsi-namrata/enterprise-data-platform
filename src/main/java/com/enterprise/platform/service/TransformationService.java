package com.enterprise.platform.service;

import com.enterprise.platform.model.DataRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransformationService {

    private static final Logger log = LoggerFactory.getLogger(TransformationService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    // Field mapping: source field name -> canonical field name
    // Handles different naming conventions across upstream systems
    private static final Map<String, Map<String, String>> FIELD_MAPPINGS = Map.of(
            "METER_READ", Map.of(
                    "meter_id", "meterId",
                    "meter_number", "meterId",
                    "reading_val", "readingValue",
                    "read_dt", "readDate",
                    "read_date", "readDate"
            ),
            "CUSTOMER", Map.of(
                    "cust_id", "customerId",
                    "customer_number", "customerId",
                    "full_name", "name",
                    "svc_address", "serviceAddress",
                    "service_addr", "serviceAddress"
            )
    );

    /**
     * Transform a validated record into the canonical schema.
     * Handles field mapping, unit conversion, and enrichment.
     */
    public DataRecord transform(DataRecord record) {
        if (record.getStatus() != DataRecord.RecordStatus.VALIDATED) {
            return record; // only transform validated records
        }

        try {
            JsonNode rawJson = mapper.readTree(record.getRawData());
            ObjectNode transformed = mapper.createObjectNode();

            // Apply field mappings
            Map<String, String> mappings = FIELD_MAPPINGS.getOrDefault(record.getRecordType(), Map.of());
            rawJson.fields().forEachRemaining(entry -> {
                String canonicalName = mappings.getOrDefault(entry.getKey(), entry.getKey());
                transformed.set(canonicalName, entry.getValue());
            });

            // Add metadata
            transformed.put("_sourceId", record.getSource().getId());
            transformed.put("_sourceName", record.getSource().getSourceName());
            transformed.put("_ingestedAt", record.getIngestedAt().toString());
            transformed.put("_recordType", record.getRecordType());

            // Unit conversion for meter reads (some sources report in MWh, we store kWh)
            if ("METER_READ".equals(record.getRecordType()) && rawJson.has("unit")) {
                String unit = rawJson.get("unit").asText();
                if ("MWH".equalsIgnoreCase(unit) && transformed.has("readingValue")) {
                    double mwhValue = transformed.get("readingValue").asDouble();
                    transformed.put("readingValue", mwhValue * 1000);
                    transformed.put("unit", "KWH");
                }
            }

            record.setTransformedData(mapper.writeValueAsString(transformed));
            record.setStatus(DataRecord.RecordStatus.TRANSFORMED);
            record.setProcessedAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Transform failed for record {}: {}", record.getExternalId(), e.getMessage());
            record.setStatus(DataRecord.RecordStatus.INVALID);
            record.setValidationErrors("Transform error: " + e.getMessage());
        }

        return record;
    }
}
