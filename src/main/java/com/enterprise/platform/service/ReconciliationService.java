package com.enterprise.platform.service;

import com.enterprise.platform.model.DataRecord;
import com.enterprise.platform.model.JobExecution;
import com.enterprise.platform.repository.DataRecordRepository;
import com.enterprise.platform.repository.JobExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final JobExecutionRepository jobRepo;
    private final DataRecordRepository recordRepo;

    public ReconciliationService(JobExecutionRepository jobRepo, DataRecordRepository recordRepo) {
        this.jobRepo = jobRepo;
        this.recordRepo = recordRepo;
    }

    /**
     * Generate reconciliation report for a job execution.
     * Compares source counts vs loaded counts — flags discrepancies.
     */
    public Map<String, Object> reconcile(Long jobId) {
        JobExecution job = jobRepo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        Long sourceId = job.getSource() != null ? job.getSource().getId() : null;

        long loaded = sourceId != null
                ? recordRepo.countBySourceAndStatus(sourceId, DataRecord.RecordStatus.LOADED)
                : 0;
        long invalid = sourceId != null
                ? recordRepo.countBySourceAndStatus(sourceId, DataRecord.RecordStatus.INVALID)
                : 0;
        long duplicates = sourceId != null
                ? recordRepo.countBySourceAndStatus(sourceId, DataRecord.RecordStatus.DUPLICATE)
                : 0;

        long totalProcessed = job.getRecordsWritten() + job.getRecordsSkipped() + job.getRecordsFailed();
        boolean balanced = totalProcessed == job.getRecordsRead();

        if (!balanced) {
            log.warn("Reconciliation mismatch for job {}: read={}, processed={}",
                    jobId, job.getRecordsRead(), totalProcessed);
        }

        return Map.of(
                "jobId", jobId,
                "jobName", job.getJobName(),
                "status", job.getStatus().name(),
                "duration", job.getDuration(),
                "recordsRead", job.getRecordsRead(),
                "recordsWritten", job.getRecordsWritten(),
                "recordsSkipped", job.getRecordsSkipped(),
                "recordsFailed", job.getRecordsFailed(),
                "totalInStore_loaded", loaded,
                "totalInStore_invalid", invalid,
                "totalInStore_duplicates", duplicates,
                "balanced", balanced
        );
    }
}
