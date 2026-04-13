package com.enterprise.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DATA_RECORDS", indexes = {
    @Index(name = "idx_records_source", columnList = "SOURCE_ID"),
    @Index(name = "idx_records_hash", columnList = "CONTENT_HASH"),
    @Index(name = "idx_records_ingested", columnList = "INGESTED_AT")
})
public class DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_ID", nullable = false)
    private DataSource source;

    @Column(name = "EXTERNAL_ID")
    private String externalId; // ID from the source system

    @Column(name = "RECORD_TYPE")
    private String recordType; // e.g. "METER_READ", "CUSTOMER", "BILLING_RECORD"

    @Column(name = "RAW_DATA", columnDefinition = "TEXT")
    private String rawData; // JSON of the original record

    @Column(name = "TRANSFORMED_DATA", columnDefinition = "TEXT")
    private String transformedData; // JSON after transformation

    @Column(name = "CONTENT_HASH", length = 64)
    private String contentHash; // SHA-256 for dedup

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private RecordStatus status = RecordStatus.RAW;

    @Column(name = "VALIDATION_ERRORS", columnDefinition = "TEXT")
    private String validationErrors;

    @Column(name = "INGESTED_AT")
    private LocalDateTime ingestedAt = LocalDateTime.now();

    @Column(name = "PROCESSED_AT")
    private LocalDateTime processedAt;

    public DataRecord() {}

    public DataRecord(DataSource source, String externalId, String recordType, String rawData) {
        this.source = source;
        this.externalId = externalId;
        this.recordType = recordType;
        this.rawData = rawData;
    }

    // Getters and setters
    public Long getId() { return id; }
    public DataSource getSource() { return source; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
    public String getTransformedData() { return transformedData; }
    public void setTransformedData(String transformedData) { this.transformedData = transformedData; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public RecordStatus getStatus() { return status; }
    public void setStatus(RecordStatus status) { this.status = status; }
    public String getValidationErrors() { return validationErrors; }
    public void setValidationErrors(String validationErrors) { this.validationErrors = validationErrors; }
    public LocalDateTime getIngestedAt() { return ingestedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public enum RecordStatus {
        RAW, VALIDATED, INVALID, TRANSFORMED, LOADED, DUPLICATE
    }
}
