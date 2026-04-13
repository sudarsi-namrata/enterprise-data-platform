# Interview Script: Experience Walkthrough

## SWRCB — State Water Resources Control Board (Jun 2024 – Present)

### Quick Context
"I'm currently working on the California Integrated Water Quality System — it's the statewide platform that 9 Regional Water Boards use to track NPDES permits, compliance violations, and water quality monitoring. Pretty heavy regulated environment."

### Key Talking Points
**Modernization story**: "When I joined, there were legacy modules that were brittle and hard to maintain. I led the redesign of the compliance tracking piece — broke it into Spring Boot microservices with Angular on the frontend. The big win was getting reporting from minutes-per-query down to sub-second by designing materialized views in Oracle."

**Event-driven architecture**: "We added Kafka pipelines for things like EPA export generation and water quality breach alerts. Before that, these were cron jobs running SQL queries. Now when a violation is recorded, the notification goes out in real-time instead of waiting for a nightly batch."

**Groovy scripting**: "I also introduced Groovy-based configuration scripts for compliance rule validation. The compliance rules change with legislation, and having non-developers modify validation logic without a full deployment cycle was a big win."

**Mentoring**: "I'm mentoring 2 junior developers — mostly on Angular patterns and component architecture, but also on how to think about API design. I've also been presenting architecture decisions to a mixed audience of engineers and Regional Board case workers, which has been good practice for translating technical concepts to non-technical stakeholders."

---

## Johnson & Johnson (Sep 2023 – May 2024)

### Quick Context
"This was a healthcare analytics platform called Engagement.AI — it provided HCP interaction insights across 14 global markets. Very data-intensive, serving multiple international teams."

### Key Talking Points
**Kafka + Spring Cloud Stream**: "I built the event-driven data ingestion layer using Kafka and Spring Cloud Stream. We were processing about 50K events daily from CRM integrations. The nice thing about Spring Cloud Stream was the abstraction — we could swap the message broker without changing application code."

**Performance optimization**: "One of my bigger wins was cutting query latency by 35% on the MongoDB and PostgreSQL schemas. It was mostly index tuning and query refactoring — nothing exotic, but the kind of thing where you have to really understand the data access patterns."

**Cross-functional collaboration**: "This was my first time working closely with data scientists. Translating their analytics requirements into technical specs was interesting — they'd come to me with 'I need all engagement events for a provider over the last 90 days, enriched with prescribing behavior', and I'd figure out how to make that performant."

---

## General Motors (Aug 2022 – Jul 2023)

### Quick Context
"I worked on GM's dealer service management platform — migrating it from legacy infrastructure to AWS. The system handles vehicle diagnostics, parts ordering, and service scheduling for their dealership network."

### Key Talking Points
**Spring Batch migration**: "The biggest project was migrating 2.5 million historical service records from a legacy Oracle database to PostgreSQL. I used Spring Batch with checkpoint restart, so if anything failed we didn't have to start over. Zero data loss, full reconciliation reporting."

**Performance detective work**: "I found an N+1 query bug on the parts-lookup service that was causing P95 response times of 3.2 seconds. Switched to batch fetching and added a couple of indexes — got it down to 180ms. That was satisfying because it was a mystery bug the team had been living with for months."

**CI/CD improvements**: "I also strengthened the Jenkins pipeline by adding SonarQube quality gates, automated integration tests, and Docker vulnerability scanning. That cut production defects by about 60%. It's one of those things where the investment upfront pays for itself quickly."

---

## Wells Fargo / Infosys (Dec 2019 – Jun 2022)

### Quick Context
"I spent two and a half years on Wells Fargo's consumer lending platform — the loan origination system. This handled everything from pre-qualification through underwriting to closing. Very high-stakes, heavily regulated environment with PCI-DSS and SOX requirements."

### Key Talking Points
**Monolith-to-microservices migration**: "I led the migration from J2EE monoliths to Spring Boot microservices. The impact was tangible — loan processing turnaround went from 7 business days down to 2. A lot of that was just decoupling services so they could process in parallel instead of sequentially."

**Kafka for real-time status**: "We had 6 Kafka topics covering mortgage status changes, closing document triggers, and escrow payment workflows. Before Kafka, the loan officer dashboard would show stale data — sometimes hours behind. After, status updates were real-time."

**Redis caching for cost savings**: "I introduced a Redis caching layer for credit bureau API calls. These third-party calls cost money per hit, and we were making redundant calls for the same customer within short windows. Redis cut redundant requests by 70% and saved about $180K annually in vendor fees. That's the kind of impact that gets attention."

**Compliance**: "PCI-DSS and SOX compliance was non-negotiable. Every financial transaction had audit logging, role-based access was enforced through Spring Security, and we had SonarQube quality gates that would block builds if security rules were violated."

---

## Procial (Jun 2018 – Nov 2019)

### Quick Context
"This was where I started — a pharmaceutical company in Hyderabad. I worked on drug inventory management and clinical trial tracking in a GxP-compliant environment, which means everything had to be FDA audit-ready."

### Talking Point
"What I took away from Procial was the discipline of working in regulated environments. GxP compliance means complete audit trails, full traceability, and rigorous testing. I used Spring AOP for cross-cutting audit logging — every database operation was logged with who, what, and when. That discipline carried forward to every role after that."
