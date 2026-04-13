package com.enterprise.platform.service;

import com.enterprise.platform.model.DataRecord;
import com.enterprise.platform.model.DataSource;
import com.enterprise.platform.repository.DataRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock private DataRecordRepository recordRepo;

    private ValidationService validationService;
    private DataSource testSource;

    @BeforeEach
    void setup() {
        validationService = new ValidationService(recordRepo);
        testSource = new DataSource("test-source", DataSource.SourceType.ORACLE_DB,
                DataSource.IngestionMode.BATCH);
    }

    @Test
    void validate_validMeterRead_returnsValidated() {
        when(recordRepo.existsByContentHash(anyString())).thenReturn(false);

        String json = "{\"meterId\":\"MTR-001\",\"readingValue\":12345.5,\"readDate\":\"2024-03-01\"}";
        DataRecord record = new DataRecord(testSource, "EXT-001", "METER_READ", json);

        DataRecord result = validationService.validate(record);

        assertEquals(DataRecord.RecordStatus.VALIDATED, result.getStatus());
        assertNotNull(result.getContentHash());
        assertNull(result.getValidationErrors());
    }

    @Test
    void validate_missingRequiredField_returnsInvalid() {
        when(recordRepo.existsByContentHash(anyString())).thenReturn(false);

        String json = "{\"meterId\":\"MTR-001\",\"readDate\":\"2024-03-01\"}"; // missing readingValue
        DataRecord record = new DataRecord(testSource, "EXT-002", "METER_READ", json);

        DataRecord result = validationService.validate(record);

        assertEquals(DataRecord.RecordStatus.INVALID, result.getStatus());
        assertTrue(result.getValidationErrors().contains("readingValue"));
    }

    @Test
    void validate_duplicateRecord_returnsDuplicate() {
        when(recordRepo.existsByContentHash(anyString())).thenReturn(true);

        String json = "{\"meterId\":\"MTR-001\",\"readingValue\":100,\"readDate\":\"2024-03-01\"}";
        DataRecord record = new DataRecord(testSource, "EXT-003", "METER_READ", json);

        DataRecord result = validationService.validate(record);

        assertEquals(DataRecord.RecordStatus.DUPLICATE, result.getStatus());
    }

    @Test
    void validate_negativeMeterReading_returnsInvalid() {
        when(recordRepo.existsByContentHash(anyString())).thenReturn(false);

        String json = "{\"meterId\":\"MTR-001\",\"readingValue\":-50,\"readDate\":\"2024-03-01\"}";
        DataRecord record = new DataRecord(testSource, "EXT-004", "METER_READ", json);

        DataRecord result = validationService.validate(record);

        assertEquals(DataRecord.RecordStatus.INVALID, result.getStatus());
        assertTrue(result.getValidationErrors().contains("negative"));
    }

    @Test
    void validate_invalidJson_returnsInvalid() {
        when(recordRepo.existsByContentHash(anyString())).thenReturn(false);

        DataRecord record = new DataRecord(testSource, "EXT-005", "METER_READ", "not json{{{");

        DataRecord result = validationService.validate(record);

        assertEquals(DataRecord.RecordStatus.INVALID, result.getStatus());
        assertTrue(result.getValidationErrors().contains("Invalid JSON"));
    }

    @Test
    void validate_validCustomerRecord_returnsValidated() {
        when(recordRepo.existsByContentHash(anyString())).thenReturn(false);

        String json = "{\"customerId\":\"CUST-001\",\"name\":\"Jane Doe\",\"serviceAddress\":\"123 Main St\"}";
        DataRecord record = new DataRecord(testSource, "EXT-006", "CUSTOMER", json);

        DataRecord result = validationService.validate(record);

        assertEquals(DataRecord.RecordStatus.VALIDATED, result.getStatus());
    }
}
