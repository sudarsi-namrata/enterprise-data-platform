package com.enterprise.platform.service;

import com.enterprise.platform.model.DataRecord;
import com.enterprise.platform.repository.DataRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    // Required fields per record type
    private static final Map<String, List<String>> REQUIRED_FIELDS = Map.of(
            "METER_READ", List.of("meterId", "readingValue", "readDate"),
            "CUSTOMER", List.of("customerId", "name", "serviceAddress"),
            "BILLING_RECORD", List.of("accountId", "amount", "billingDate"),
            "PAYMENT", List.of("accountId", "amount", "paymentDate")
    );

    private final DataRecordRepository recordRepo;

    public ValidationService(DataRecordRepository recordRepo) {
        this.recordRepo = recordRepo;
    }

    /**
     * Validate a data record through the full pipeline:
     * 1. Compute content hash (dedup)
     * 2. Schema validation (required fields)
     * 3. Range/format checks
     */
    public DataRecord validate(DataRecord record) {
        List<String> errors = new ArrayList<>();

        // Step 1: Deduplication via content hash
        String hash = computeHash(record.getRawData());
        record.setContentHash(hash);

        if (recordRepo.existsByContentHash(hash)) {
            record.setStatus(DataRecord.RecordStatus.DUPLICATE);
            log.debug("Duplicate record detected: hash={}", hash);
            return record;
        }

        // Step 2: Parse and validate schema
        try {
            JsonNode json = mapper.readTree(record.getRawData());

            List<String> requiredFields = REQUIRED_FIELDS.getOrDefault(
                    record.getRecordType(), List.of());

            for (String field : requiredFields) {
                if (!json.has(field) || json.get(field).isNull() || json.get(field).asText().isBlank()) {
                    errors.add("Missing required field: " + field);
                }
            }

            // Step 3: Type-specific range checks
            if ("METER_READ".equals(record.getRecordType()) && json.has("readingValue")) {
                double value = json.get("readingValue").asDouble();
                if (value < 0) errors.add("readingValue cannot be negative");
                if (value > 999999) errors.add("readingValue exceeds max (999999)");
            }

            if (record.getRecordType() != null && record.getRecordType().contains("PAYMENT") && json.has("amount")) {
                double amount = json.get("amount").asDouble();
                if (amount <= 0) errors.add("Payment amount must be positive");
            }

        } catch (Exception e) {
            errors.add("Invalid JSON: " + e.getMessage());
        }

        if (errors.isEmpty()) {
            record.setStatus(DataRecord.RecordStatus.VALIDATED);
        } else {
            record.setStatus(DataRecord.RecordStatus.INVALID);
            record.setValidationErrors(String.join("; ", errors));
            log.warn("Validation failed for record {}: {}", record.getExternalId(), errors);
        }

        return record;
    }

    private String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
