package com.enterprise.platform.controller;

import com.enterprise.platform.model.JobExecution;
import com.enterprise.platform.repository.JobExecutionRepository;
import com.enterprise.platform.service.ReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobExecutionRepository jobRepo;
    private final ReconciliationService reconciliationService;

    public JobController(JobExecutionRepository jobRepo, ReconciliationService reconciliationService) {
        this.jobRepo = jobRepo;
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<JobExecution> getJobStatus(@PathVariable Long jobId) {
        return jobRepo.findById(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<JobExecution>> getHistory() {
        return ResponseEntity.ok(jobRepo.findTop20ByOrderByStartedAtDesc());
    }

    @GetMapping("/reconciliation/{jobId}")
    public ResponseEntity<Map<String, Object>> reconcile(@PathVariable Long jobId) {
        return ResponseEntity.ok(reconciliationService.reconcile(jobId));
    }
}
