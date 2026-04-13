package com.enterprise.platform.repository;

import com.enterprise.platform.model.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    List<JobExecution> findTop20ByOrderByStartedAtDesc();

    List<JobExecution> findByStatus(JobExecution.JobStatus status);

    List<JobExecution> findBySourceIdOrderByStartedAtDesc(Long sourceId);
}
