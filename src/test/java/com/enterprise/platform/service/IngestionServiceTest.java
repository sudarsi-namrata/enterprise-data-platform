package com.enterprise.platform.service;

import com.enterprise.platform.model.DataRecord;
import com.enterprise.platform.model.DataSource;
import com.enterprise.platform.model.JobExecution;
import com.enterprise.platform.repository.DataRecordRepository;
import com.enterprise.platform.repository.DataSourceRepository;
import com.enterprise.platform.repository.JobExecutionRepository;
import com.enterprise.platform.service.IngestionService.RawRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock private DataSourceRepository sourceRepo;
    @Mock private DataRecordRepository recordRepo;
    @Mock private JobExecutionRepository jobRepo;
    @Mock private ValidationService validationService;
    @Mock private TransformationService transformService;

    private IngestionService ingestionService;
    private DataSource testSource;

    @BeforeEach
    void setup() {
        ingestionService = new IngestionService(sourceRepo, recordRepo, jobRepo,
                validationService, transformService);
        testSource = new DataSource("legacy-oracle", DataSource.SourceType.ORACLE_DB,
                DataSource.IngestionMode.BATCH);
    }

    @Test
    void ingestBatch_processesRecordsThroughPipeline() {
        when(sourceRepo.findById(1L)).thenReturn(Optional.of(testSource));
        when(jobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sourceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Validation returns VALIDATED, transform returns TRANSFORMED
        when(validationService.validate(any())).thenAnswer(inv -> {
            DataRecord r = inv.getArgument(0);
            r.setStatus(DataRecord.RecordStatus.VALIDATED);
            r.setContentHash("abc123");
            return r;
        });
        when(transformService.transform(any())).thenAnswer(inv -> {
            DataRecord r = inv.getArgument(0);
            r.setStatus(DataRecord.RecordStatus.TRANSFORMED);
            r.setTransformedData("{\"canonical\":true}");
            return r;
        });

        List<RawRecord> records = List.of(
                new RawRecord("EXT-1", "METER_READ", "{\"meterId\":\"M1\",\"readingValue\":100,\"readDate\":\"2024-01\"}"),
                new RawRecord("EXT-2", "METER_READ", "{\"meterId\":\"M2\",\"readingValue\":200,\"readDate\":\"2024-01\"}")
        );

        JobExecution job = ingestionService.ingestBatch(1L, records);

        assertNotNull(job);
        assertEquals(2, job.getRecordsRead());
        assertEquals(2, job.getRecordsWritten());
        assertEquals(0, job.getRecordsFailed());
        assertEquals(JobExecution.JobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void ingestBatch_skipsDuplicates() {
        when(sourceRepo.findById(1L)).thenReturn(Optional.of(testSource));
        when(jobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sourceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(validationService.validate(any())).thenAnswer(inv -> {
            DataRecord r = inv.getArgument(0);
            r.setStatus(DataRecord.RecordStatus.DUPLICATE);
            return r;
        });

        List<RawRecord> records = List.of(
                new RawRecord("DUP-1", "METER_READ", "{\"data\":\"same\"}")
        );

        JobExecution job = ingestionService.ingestBatch(1L, records);

        assertEquals(0, job.getRecordsWritten());
        assertEquals(1, job.getRecordsSkipped());
    }

    @Test
    void ingestBatch_countsInvalidRecords() {
        when(sourceRepo.findById(1L)).thenReturn(Optional.of(testSource));
        when(jobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sourceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(validationService.validate(any())).thenAnswer(inv -> {
            DataRecord r = inv.getArgument(0);
            r.setStatus(DataRecord.RecordStatus.INVALID);
            r.setValidationErrors("bad data");
            return r;
        });

        List<RawRecord> records = List.of(
                new RawRecord("BAD-1", "METER_READ", "{\"broken\":true}")
        );

        JobExecution job = ingestionService.ingestBatch(1L, records);

        assertEquals(0, job.getRecordsWritten());
        assertEquals(1, job.getRecordsFailed());
        verify(recordRepo, times(1)).save(any()); // invalid record still persisted
    }

    @Test
    void ingestBatch_throwsForUnknownSource() {
        when(sourceRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> ingestionService.ingestBatch(999L, List.of()));
    }
}
