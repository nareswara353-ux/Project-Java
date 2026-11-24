# ADR-0006: Full Observability Stack (Metrics + Logs + Traces)

- **Status**: Accepted
- **Tanggal**: 2026-02-10
- **Deciders**: Principal Engineer, SRE Team
- **Related ADRs**: ADR-0005

## Context

Aplikasi harus bisa di-**troubleshoot** dan di-**monitor** di production. Tanpa observability:

1. **Incident detection lambat** — tahu masalah setelah user komplain
2. **Root cause analysis sulit** — tidak ada cara lompat dari error → trace → query
3. **Capacity planning buta** — tidak tahu trend penggunaan

Portofolio harus mendemonstrasikan **3 pilar observability** (metrics, logs, traces) yang terintegrasi.

## Decision

Mengadopsi **full observability stack** — self-hosted, docker-compose:

| Pilar | Tool | Fungsi | Retention |
|-------|------|--------|-----------|
| **Metrics** | Prometheus + Micrometer | Scrape `/actuator/prometheus` tiap 10s | 15 hari |
| **Logs** | Loki + Promtail | Aggregate JSON log dari Docker | 30 hari |
| **Traces** | Tempo + OpenTelemetry | OTLP export, span correlation | 24 jam |
| **Alerts** | Alertmanager | Route by severity | - |
| **Visualization** | Grafana 11.3 | 3 dashboard auto-provisioned | - |

**Key features**:
- **Custom metrics** — `products_total` gauge, `products_operations_total` counter
- **Correlation** — `traceId` di log → klik buka trace di Tempo
- **Auto-generated metrics dari spans** — `traces_spanmetrics_*` (service graph, latency)
- **5 alert rules** — AppDown, HighErrorRate, CircuitBreakerOpen, HighHeapUsage, HighLatencyP95
- **Resilience** — Resilience4j circuit breaker terintegrasi sebagai health indicator

## Consequences

### Positif

- **MTTD rendah** — alert firing dalam 1-2 menit dari anomali
- **MTTR rendah** — log → trace → span dalam 1 klik
- **Capacity planning** — trend heap, HTTP rate, DB connection
- **Enterprise-grade** — stack production-ready, sama seperti perusahaan besar

### Negatif

- **Resource cost** — ~5 container tambahan (~2 GB RAM, ~1 GB disk/30 hari)
- **Setup complexity** — 6 config files, 5 services
- **Learning curve** — LogQL, PromQL, TraceQL

### Netral

- Butuh Docker compose orchestration
- Sampling 100% di dev (production bisa turun ke 10% untuk hemat storage)
- Manual scale untuk production (Prometheus federation, Tempo S3 backend)

## Alternatives Considered

| Alternatif | Alasan Ditolak |
|------------|----------------|
| **Hanya metrics** (Actuator only) | Tidak bisa troubleshoot incident karena tidak ada detail log |
| **ELK Stack** (Elasticsearch) | Heavy — 2 GB RAM hanya untuk Elasticsearch |
| **Datadog / New Relic** (SaaS) | Paid, tidak menunjukkan skill self-hosted |
| **Cloud-native** (CloudWatch, Stackdriver) | Vendor lock-in, tidak transferable |

## References

- Google SRE Book — Monitoring Distributed Systems
- OpenTelemetry Documentation
- Grafana LGTM Stack (Loki + Grafana + Tempo + Mimir)
