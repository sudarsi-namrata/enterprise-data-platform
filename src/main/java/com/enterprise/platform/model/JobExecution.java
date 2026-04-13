package com.enterprise.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "JOB_EXECUTIONS")
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "JOB_NAME", nullable = false)
    private String jobName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_ID")
    private DataSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private JobStatus status = JobStatus.RUNNING;

    @Column(name = "RECORDS_READ")
    private long recordsRead = 0;

    @Column(name = "RECORDS_WRITTEN")
    private long recordsWritten = 0;

    @Column(name = "RECORDS_SKIPPED")
    private long recordsSkipped = 0;

    @Column(name = "RECORDS_FAILED")
    private long recordsFailed = 0;

    @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    public JobExecution() {}

    public JobExecution(String jobName, DataSource source) {
        this.jobName = jobName;
        this.source = source;
    }

    public void markCompleted() {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String error) {
        this.status = JobStatus.FAILED;
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }

    public String getDuration() {
        if (startedAt == null) return "N/A";
        LocalDateTime end = completedAt != null ? completedAt : LocalDateTime.now();
        Duration d = Duration.between(startedAt, end);
        return String.format("%dm %ds", d.toMinutes(), d.toSecondsPart());
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getJobName() { return jobName; }
    public DataSource getSource() { return source; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public long getRecordsRead() { return recordsRead; }
    public void setRecordsRead(long recordsRead) { this.recordsRead = recordsRead; }
    public long getRecordsWritten() { return recordsWritten; }
    public void setRecordsWritten(long recordsWritten) { this.recordsWritten = recordsWritten; }
    public long getRecordsSkipped() { return recordsSkipped; }
    public void setRecordsSkipped(long recordsSkipped) { this.recordsSkipped = recordsSkipped; }
    public long getRecordsFailed() { return recordsFailed; }
    public void setRecordsFailed(long recordsFailed) { this.recordsFailed = recordsFailed; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public enum JobStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED
    }
}
