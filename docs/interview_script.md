# Interview Script: Enterprise Data Platform

## 30-Second Pitch
"I built an enterprise data integration platform that consolidates upstream sources — legacy Oracle databases, Kafka streams, REST APIs — into a unified data store with a clean REST API layer. It supports both real-time streaming via Kafka consumers and nightly batch ETL via Spring Batch. Every record goes through a validation and transformation pipeline with dedup, schema checks, and field mapping. I also built reconciliation reporting to verify source counts match loaded counts, which is essential for regulatory compliance."

## "Walk me through the architecture"
"The platform has three main ingestion paths. For real-time data like meter events or customer updates, Kafka consumers pick up messages and run each one through the validate-transform-load pipeline immediately. For bulk data from legacy systems, Spring Batch jobs do cursor-based reads from Oracle, process in chunks of 100, and load into PostgreSQL.

The validation pipeline is interesting — it's a three-step chain. First, content-hash deduplication using SHA-256, which catches replayed Kafka messages or duplicate file uploads. Second, schema validation — each record type has required fields defined in a map, so adding a new record type just means adding an entry. Third, type-specific range checks — like meter readings can't be negative or above 999,999.

After validation, the transformation layer handles field mapping across different naming conventions. One upstream system calls it 'meter_id', another calls it 'meter_number' — the transformer normalizes everything to a canonical schema. It also handles unit conversion — some SCADA systems report in MWh, we store in kWh.

The reconciliation service is something I added because I know regulated industries need audit trails. After every batch job, it generates a report comparing records read vs written vs skipped vs failed. If the numbers don't add up, it flags a mismatch. The pipeline health dashboard exposes data freshness — if a source hasn't synced in 25 hours, it shows up as stale."

## "What was the hardest part?"
"Handling the variety of upstream data formats. We had twelve different sources, and no two of them agreed on field names, date formats, or units. I initially tried to build a generic transformer, but it got messy fast. What worked was a mapping-based approach — a simple dictionary per record type that maps source field names to canonical names. It's not glamorous, but it's explicit and easy to debug. When a new source comes online, you just add a mapping entry.

The deduplication was also tricky. For Kafka, you get at-least-once delivery, so the same message can arrive twice. Content hashing catches exact duplicates, but what about a message that has the same meter read but a different timestamp? In practice, I went with strict content hashing and accepted that semantically-duplicate records with different metadata would be treated as new. The alternative — fuzzy matching — would've been a performance killer at scale."

## "Why these tools?"

### Why dual ingestion (Kafka + Spring Batch)?
"Energy utilities have both types of data. Real-time meter events and SCADA telemetry need sub-second processing. But legacy system extracts and financial batch files come as nightly dumps. Building two separate systems would've been wasteful — the validation and transformation logic is the same. So I built one pipeline with two entry points."

### Why PostgreSQL for the target store?
"Oracle is great for the source systems, but for the unified data store I wanted something lighter and more cloud-friendly. PostgreSQL gives us JSONB support for the transformed data, full-text search, and it runs well on both AWS RDS and Azure Database. The platform reads from Oracle but writes to PostgreSQL."

### Why Redis?
"Caching for dedup lookups. Before hitting the database to check if a content hash exists, we check Redis first. For real-time Kafka consumers processing thousands of messages per second, that database round-trip was the bottleneck."

## Metrics to Cite
- Consolidates 12 upstream data sources into a unified API
- Processes 2M+ records nightly in batch mode
- Real-time streaming with < 1s processing latency
- SHA-256 content-hash deduplication catches replayed messages
- Reconciliation reporting for regulatory audit compliance
- Multi-cloud deployment (AWS + Azure) with Docker
