package com.enterprise.platform.service;

import com.enterprise.platform.model.DataRecord;
import com.enterprise.platform.model.DataSource;
import com.enterprise.platform.model.JobExecution;
import com.enterprise.platform.repository.DataRecordRepository;
import com.enterprise.platform.repository.DataSourceRepository;
import com.enterprise.platform.repository.JobExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final DataSourceRepository sourceRepo;
    private final DataRecordRepository recordRepo;
    private final JobExecutionRepository jobRepo;
    private final ValidationService validationService;
    private final TransformationService transformService;

    public IngestionService(DataSourceRepository sourceRepo, DataRecordRepository recordRepo,
                            JobExecutionRepository jobRepo, ValidationService validationService,
                            TransformationService transformService) {
        this.sourceRepo = sourceRepo;
        this.recordRepo = recordRepo;
        this.jobRepo = jobRepo;
        this.validationService = validationService;
        this.transformService = transformService;
    }

    /**
     * Ingest a batch of raw records from a data source.
     * Each record goes through: validate -> transform -> persist
     */
    @Transactional
    public JobExecution ingestBatch(Long sourceId, List<RawRecord> rawRecords) {
        DataSource source = sourceRepo.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));

        JobExecution job = new JobExecution("manual-ingest", source);
        jobRepo.save(job);

        long written = 0, skipped = 0, failed = 0;

        for (RawRecord raw : rawRecords) {
            job.setRecordsRead(job.getRecordsRead() + 1);

            try {
                DataRecord record = new DataRecord(source, raw.externalId(), raw.recordType(), raw.data());

                // Validate
                record = validationService.validate(record);
                if (record.getStatus() == DataRecord.RecordStatus.DUPLICATE) {
                    skipped++;
                    continue;
                }
                if (record.getStatus() == DataRecord.RecordStatus.INVALID) {
                    recordRepo.save(record); // persist for error reporting
                    failed++;
                    continue;
                }

                // Transform
                record = transformService.transform(record);
                if (record.getStatus() == DataRecord.RecordStatus.INVALID) {
                    recordRepo.save(record);
                    failed++;
                    continue;
                }

                record.setStatus(DataRecord.RecordStatus.LOADED);
                recordRepo.save(record);
                written++;

            } catch (Exception e) {
                log.error("Failed to process record {}: {}", raw.externalId(), e.getMessage());
                failed++;
            }
        }

        job.setRecordsWritten(written);
        job.setRecordsSkipped(skipped);
        job.setRecordsFailed(failed);
        job.markCompleted();
        jobRepo.save(job);

        // Update source metadata
        source.setLastSync(LocalDateTime.now());
        source.setRecordCount(source.getRecordCount() + written);
        sourceRepo.save(source);

        log.info("Ingestion complete for {}: read={}, written={}, skipped={}, failed={}",
                source.getSourceName(), rawRecords.size(), written, skipped, failed);
        return job;
    }

    /**
     * Ingest a single record (used by Kafka consumer for real-time mode)
     */
    @Transactional
    public DataRecord ingestSingle(Long sourceId, String externalId, String recordType, String rawData) {
        DataSource source = sourceRepo.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));

        DataRecord record = new DataRecord(source, externalId, recordType, rawData);
        record = validationService.validate(record);

        if (record.getStatus() == DataRecord.RecordStatus.VALIDATED) {
            record = transformService.transform(record);
            if (record.getStatus() == DataRecord.RecordStatus.TRANSFORMED) {
                record.setStatus(DataRecord.RecordStatus.LOADED);
            }
        }

        return recordRepo.save(record);
    }

    public record RawRecord(String externalId, String recordType, String data) {}
}
