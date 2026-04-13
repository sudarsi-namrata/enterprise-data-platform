package com.enterprise.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DATA_SOURCES")
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SOURCE_NAME", unique = true, nullable = false)
    private String sourceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "SOURCE_TYPE", nullable = false)
    private SourceType sourceType;

    @Column(name = "CONNECTION_CONFIG", columnDefinition = "TEXT")
    private String connectionConfig; // JSON blob with JDBC url, Kafka topic, API endpoint, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "INGESTION_MODE")
    private IngestionMode ingestionMode;

    @Column(name = "SCHEDULE_CRON")
    private String scheduleCron; // for batch sources

    @Column(name = "ENABLED")
    private boolean enabled = true;

    @Column(name = "LAST_SYNC")
    private LocalDateTime lastSync;

    @Column(name = "RECORD_COUNT")
    private Long recordCount = 0L;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();

    public DataSource() {}

    public DataSource(String sourceName, SourceType sourceType, IngestionMode mode) {
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.ingestionMode = mode;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public String getConnectionConfig() { return connectionConfig; }
    public void setConnectionConfig(String connectionConfig) { this.connectionConfig = connectionConfig; }
    public IngestionMode getIngestionMode() { return ingestionMode; }
    public void setIngestionMode(IngestionMode ingestionMode) { this.ingestionMode = ingestionMode; }
    public String getScheduleCron() { return scheduleCron; }
    public void setScheduleCron(String scheduleCron) { this.scheduleCron = scheduleCron; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getLastSync() { return lastSync; }
    public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }
    public Long getRecordCount() { return recordCount; }
    public void setRecordCount(Long recordCount) { this.recordCount = recordCount; }

    public enum SourceType {
        ORACLE_DB, POSTGRESQL_DB, KAFKA_TOPIC, REST_API, FLAT_FILE
    }

    public enum IngestionMode {
        BATCH, STREAMING, HYBRID
    }
}
