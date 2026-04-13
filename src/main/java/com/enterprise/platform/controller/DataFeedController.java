package com.enterprise.platform.controller;

import com.enterprise.platform.model.DataRecord;
import com.enterprise.platform.model.DataSource;
import com.enterprise.platform.model.JobExecution;
import com.enterprise.platform.repository.DataRecordRepository;
import com.enterprise.platform.repository.DataSourceRepository;
import com.enterprise.platform.service.IngestionService;
import com.enterprise.platform.service.IngestionService.RawRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data-feeds")
public class DataFeedController {

    private final DataSourceRepository sourceRepo;
    private final DataRecordRepository recordRepo;
    private final IngestionService ingestionService;

    public DataFeedController(DataSourceRepository sourceRepo, DataRecordRepository recordRepo,
                               IngestionService ingestionService) {
        this.sourceRepo = sourceRepo;
        this.recordRepo = recordRepo;
        this.ingestionService = ingestionService;
    }

    @GetMapping
    public ResponseEntity<List<DataSource>> listFeeds() {
        return ResponseEntity.ok(sourceRepo.findByEnabledTrue());
    }

    @PostMapping("/{sourceId}/ingest")
    public ResponseEntity<JobExecution> ingest(@PathVariable Long sourceId,
                                                @RequestBody List<RawRecord> records) {
        JobExecution job = ingestionService.ingestBatch(sourceId, records);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/{sourceId}/records")
    public ResponseEntity<Page<DataRecord>> getRecords(
            @PathVariable Long sourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(
                recordRepo.findBySourceIdOrderByIngestedAtDesc(sourceId, PageRequest.of(page, size)));
    }
}
