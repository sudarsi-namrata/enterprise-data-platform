# Interview Tips: Senior Software Engineer — Fortune 200 Energy Client (via Brooksource)

## Company/Role Context
- This is a Fortune 200 energy/utility company (likely Exelon, Dominion, Duke Energy, or similar)
- The role specifically calls out Oracle Utilities CC&B — this is the dominant customer care and billing system in the utility industry
- Meter-to-Cash is the end-to-end utility billing process: meter reading -> bill calculation -> payment processing -> collections
- They use Groovy for CC&B customization (service scripts, plug-in spots, batch jobs)
- This is a contract-to-hire, so they want someone who can contribute immediately

## Oracle Utilities Talking Points
Since the JD emphasizes Oracle Utilities CC&B, here's how to talk about it credibly:

"I'm familiar with the CC&B configuration model — service scripts, plug-in spots, and batch processing for billing cycles. In my utility billing engine project, I modeled the same pattern using a Groovy scripting engine so business rules can be modified without code deployments. I understand the meter-to-cash flow: meter reads come in, get validated, the billing engine applies rate schedules based on service class and season, generates bills, and then payments are allocated to outstanding balances. I've worked with Oracle DB extensively — schema design, materialized views, stored procedures — and I'm comfortable with the Oracle Utilities Application Framework patterns."

## Technical Questions to Prepare

### Java
- "Tell me about your experience with Java concurrency" — Talk about CompletableFuture at SWRCB, Kafka consumers at GM/J&J, and the thread-safe Groovy script execution in your billing engine project
- "How do you optimize database queries?" — N+1 fix at GM (3.2s -> 180ms), materialized views at SWRCB, index tuning at J&J (35% improvement)
- "Walk me through a distributed system you've built" — Kafka pipelines at SWRCB/Wells Fargo, the data platform's dual-ingestion architecture

### System Design
- "How would you design a billing system?" — Walk through the meter-to-cash flow from your billing engine project
- "How do you handle data from multiple sources?" — Enterprise data platform: validation pipeline, content-hash dedup, field mapping, reconciliation

### Testing
- "What's your testing strategy?" — JUnit 5 + Mockito for unit tests, Testcontainers for integration tests, SonarQube quality gates, specific coverage numbers (42% -> 87% at GM, 92% at Wells Fargo)

### Cloud & DevOps
- "Experience with CI/CD?" — Jenkins pipelines at every role, GitHub Actions in personal projects, Docker containerization, SonarQube gates
- "Cloud platform experience?" — AWS (EC2, S3, Lambda, EKS, RDS, CloudFormation), Azure, Docker + Kubernetes

## Behavioral Questions

### "Tell me about a time you mentored someone"
"At SWRCB, I'm currently mentoring 2 junior developers. One was struggling with Angular's change detection — components were re-rendering unnecessarily and the dashboards were janky. I paired with them for a few hours, showed them OnPush change detection strategy and how to think about immutable state. Within a couple weeks, they were applying those patterns independently. I've found that explaining the 'why' behind patterns is more effective than just showing the 'how'."

### "Tell me about a time you resolved a conflict"
"At Wells Fargo, we had a disagreement between the backend team and the frontend team about API contract changes. The backend team wanted to make a breaking change for cleaner architecture, and the frontend team didn't want to rewrite their integration. I proposed a versioned API approach — we'd deploy the new contract alongside the old one, the frontend team could migrate at their pace, and we'd deprecate the old version after a sprint. Both teams agreed, and it became our standard approach for API evolution."

### "How do you handle technical debt?"
"I don't ignore it, but I also don't try to fix it all at once. At GM, we had significant technical debt in the Jenkins pipeline — no quality gates, manual testing, no vulnerability scanning. I didn't propose a big-bang rewrite. Instead, I added SonarQube gates first, then automated integration tests, then Docker scanning. Each step delivered immediate value. Over three months, production defects dropped 60%."

## Questions to Ask the Interviewer

1. "Can you tell me more about the CC&B implementation — which version are you running, and are there plans to migrate to Oracle Utilities Cloud Service?"
2. "How does the development team handle CC&B configurations vs custom Java development? What's the ratio?"
3. "What does the deployment process look like — is it CI/CD, or are there manual steps for CC&B configurations?"
4. "How large is the engineering team I'd be working with, and what's the team structure?"
5. "What are the biggest technical challenges the team is facing right now?"
6. "Is there a mentoring expectation for senior engineers on this team?"
7. "What does the path from contract to full-time hire look like? What are you looking for in that transition?"

## Tone Reminders
- Use "I" not "we" for things you personally did — interviewers want to know YOUR contribution
- It's OK to say "I'm not deeply experienced with CC&B configurations specifically, but I understand the patterns and I've built similar configurable systems" — honesty about gaps + evidence you can learn fast
- When they ask about Oracle Utilities, pivot to your Oracle DB expertise and the billing engine project as evidence you can ramp up quickly
- Energy/utility is a regulated industry — emphasize your compliance experience (PCI-DSS, SOX, GxP, HIPAA)
- This is contract-to-hire — signal that you want the permanent role, you're not just looking for a short gig
