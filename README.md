# Enterprise Data Platform

An enterprise data integration platform built with Java 17 and Spring Boot 3.2 that consolidates upstream data sources into a unified API layer. Supports both real-time Kafka streaming and batch ETL via Spring Batch, with Oracle-to-PostgreSQL migration capabilities and configurable data validation/transformation pipelines.

## Architecture

```
   Upstream Sources                         Downstream Consumers
  ┌──────────────┐                         ┌──────────────────┐
  │ Legacy Oracle ├──┐                  ┌──►│   Billing System │
  │ SCADA/Meters  │  │   ┌───────────┐  │  └──────────────────┘
  │ Customer CRM  ├──┼──►│  Ingestion │  │  ┌──────────────────┐
  │ Third-party   │  │   │   Layer    ├──┼──►│  Analytics/BI    │
  │ APIs          ├──┘   └─────┬──────┘  │  └──────────────────┘
  └──────────────┘         │       │     │  ┌──────────────────┐
                    Kafka──┘  Batch┘     └──►│  Mobile Apps     │
                           │       │        └──────────────────┘
              ┌────────────▼───────▼────────────────────┐
              │         Validation & Transform            │
              │  Schema checks │ Dedup │ Enrich │ Map    │
              └────────────────┬────────────────────────┘
                               │
              ┌────────────────▼────────────────────────┐
              │           Unified Data Store              │
              │    PostgreSQL + Redis (cache layer)       │
              └────────────────┬────────────────────────┘
                               │
              ┌────────────────▼────────────────────────┐
              │              REST API Layer               │
              │  /data-feeds  /sources  /jobs  /health   │
              └─────────────────────────────────────────┘
```

## Key Design Decisions

### Dual Ingestion Modes
Energy utilities deal with both real-time streams (meter events, SCADA telemetry) and bulk data loads (legacy system extracts, financial batch files). The platform supports both:
- **Kafka consumers** for real-time data (< 1s latency)
- **Spring Batch jobs** for nightly ETL from Oracle and flat files (fault-tolerant, restartable)

### Validation Pipeline
Each record passes through a configurable validation chain before persistence:
1. **Schema validation** — required fields, types, ranges
2. **Deduplication** — content-hash based, catches replayed messages
3. **Enrichment** — joins with reference data (account lookup, geo-coding)
4. **Transformation** — field mapping, unit conversion, format normalization

### Reconciliation
After each batch run, the platform generates a reconciliation report comparing source record counts with loaded counts, flagging discrepancies. Critical for utility regulatory compliance.

### Multi-Cloud Support
Deployable on both AWS and Azure via Docker. Configuration profiles handle cloud-specific services (S3 vs Blob Storage for file staging, CloudWatch vs Azure Monitor for metrics).

## Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15+ (or H2 for local dev)
- Apache Kafka (optional, for streaming mode)

### Run locally
```bash
./mvnw spring-boot:run
```

### Run with Kafka
```bash
./mvnw spring-boot:run -Dspring.profiles.active=kafka
```

### Run tests
```bash
./mvnw test
```

### Docker
```bash
docker build -t enterprise-data-platform .
docker run -p 8080:8080 enterprise-data-platform
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/data-feeds | List configured data feeds |
| POST | /api/data-feeds/{sourceId}/ingest | Trigger manual ingestion |
| GET | /api/data-feeds/{sourceId}/records | Query ingested records |
| POST | /api/jobs/etl/run | Trigger batch ETL job |
| GET | /api/jobs/{jobId}/status | Get job execution status |
| GET | /api/jobs/history | List recent job executions |
| GET | /api/reconciliation/{jobId} | Get reconciliation report |
| GET | /api/sources | List registered data sources |
| POST | /api/sources | Register new data source |
| GET | /api/health/pipeline | Pipeline health dashboard data |
