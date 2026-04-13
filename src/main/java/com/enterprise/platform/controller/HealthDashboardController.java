package com.enterprise.platform.controller;

import com.enterprise.platform.model.DataRecord;
import com.enterprise.platform.model.JobExecution;
import com.enterprise.platform.repository.DataRecordRepository;
import com.enterprise.platform.repository.DataSourceRepository;
import com.enterprise.platform.repository.JobExecutionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthDashboardController {

    private final DataSourceRepository sourceRepo;
    private final DataRecordRepository recordRepo;
    private final JobExecutionRepository jobRepo;

    public HealthDashboardController(DataSourceRepository sourceRepo,
                                      DataRecordRepository recordRepo,
                                      JobExecutionRepository jobRepo) {
        this.sourceRepo = sourceRepo;
        this.recordRepo = recordRepo;
        this.jobRepo = jobRepo;
    }

    @GetMapping("/pipeline")
    public ResponseEntity<Map<String, Object>> pipelineHealth() {
        Map<String, Object> health = new HashMap<>();

        // Overall counts
        health.put("totalSources", sourceRepo.count());
        health.put("activeSources", sourceRepo.findByEnabledTrue().size());
        health.put("totalRecords", recordRepo.count());

        // Last 24h stats
        LocalDateTime dayAgo = LocalDateTime.now().minusHours(24);
        var sources = sourceRepo.findByEnabledTrue();
        var sourceStats = sources.stream().map(src -> {
            long last24h = recordRepo.countBySourceAndDateRange(src.getId(), dayAgo, LocalDateTime.now());
            return Map.of(
                    "name", src.getSourceName(),
                    "type", src.getSourceType().name(),
                    "lastSync", src.getLastSync() != null ? src.getLastSync().toString() : "never",
                    "records24h", last24h,
                    "totalRecords", src.getRecordCount()
            );
        }).toList();
        health.put("sources", sourceStats);

        // Recent job status
        var recentJobs = jobRepo.findTop20ByOrderByStartedAtDesc();
        long failedJobs = recentJobs.stream()
                .filter(j -> j.getStatus() == JobExecution.JobStatus.FAILED)
                .count();
        health.put("recentJobCount", recentJobs.size());
        health.put("failedJobCount", failedJobs);

        // Data freshness check: warn if any source hasn't synced in 25+ hours
        var staleSourceNames = sources.stream()
                .filter(s -> s.getLastSync() == null || s.getLastSync().isBefore(dayAgo.minusHours(1)))
                .map(s -> s.getSourceName())
                .toList();
        health.put("staleSources", staleSourceNames);
        health.put("healthy", staleSourceNames.isEmpty() && failedJobs == 0);

        return ResponseEntity.ok(health);
    }
}
