# Kubernetes Deployment Guide

Manifest K8s untuk **Java Enterprise Portfolio** dengan kustomize.

## Prasyarat

| Tool | Versi Minimum | Fungsi |
|------|---------------|--------|
| `kubectl` | 1.28+ | Apply manifest |
| `kustomize` | 5.0+ | Build overlay (opsional, `kubectl -k` juga bisa) |
| Cluster | k3d/kind/EKS/GKE/AKS | Target deployment |
| Ingress NGINX | - | Untuk `Ingress` resource |
| cert-manager | - | Untuk TLS otomatis via Let's Encrypt |
| metrics-server | - | Untuk HPA membaca CPU/memory |

## Struktur Manifest
k8s/
├── namespace.yaml ← Namespace dengan Pod Security baseline
├── configmap.yaml ← Env non-secret
├── secret.yaml ← JWT_SECRET (ganti di production!)
├── deployment.yaml ← 3 replicas, 3 probes, resource limits
├── service.yaml ← ClusterIP port 80 → 8080
├── hpa.yaml ← Autoscale 2-5 pods berdasarkan CPU/memory
├── ingress.yaml ← nginx + TLS via cert-manager
└── kustomization.yaml ← Orchestrator

text

## Deploy

### 1. Sesuaikan secret (WAJIB untuk production)

```bash
JWT=$(openssl rand -base64 48)
sed -i "s|JWT_SECRET: .*|JWT_SECRET: \"$JWT\"|" k8s/secret.yaml
Atau lebih aman: pakai Sealed Secrets atau External Secrets Operator (tidak commit secret ke git).

2. Sesuaikan domain ingress
bash
sed -i 's|portfolio.example.com|your-domain.com|g' k8s/ingress.yaml
3. Apply semua manifest
bash
kubectl apply -k k8s/
4. Verifikasi
bash
kubectl get all -n java-enterprise-portfolio
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=java-enterprise-portfolio -n java-enterprise-portfolio --timeout=180s
kubectl logs -n java-enterprise-portfolio -l app.kubernetes.io/name=java-enterprise-portfolio --tail=50
5. Akses aplikasi
Via port-forward (debug):

bash
kubectl port-forward -n java-enterprise-portfolio svc/java-enterprise-portfolio 8080:80
curl http://localhost:8080/api/health
Via Ingress (production):

bash
curl https://your-domain.com/api/health
Rollback
bash
kubectl rollout undo deployment/java-enterprise-portfolio -n java-enterprise-portfolio
kubectl rollout status deployment/java-enterprise-portfolio -n java-enterprise-portfolio
Update Image
Setelah CI push image baru ke GHCR:

bash
kubectl set image deployment/java-enterprise-portfolio \
  app=ghcr.io/nareswara353-ux/project-java:sha-<COMMIT> \
  -n java-enterprise-portfolio
kubectl rollout status deployment/java-enterprise-portfolio -n java-enterprise-portfolio
Scaling
HPA otomatis menyesuaikan 2-5 replica berdasarkan CPU (70%) dan memory (80%). Untuk manual:

bash
kubectl scale deployment/java-enterprise-portfolio --replicas=4 -n java-enterprise-portfolio
Troubleshooting
Gejala	Kemungkinan	Solusi
ImagePullBackOff	Image private / tag salah	kubectl describe pod; buat image pull secret
CrashLoopBackOff	Env salah / startup error	kubectl logs <pod> --previous
Readiness probe failed	App lambat start	Naikkan startupProbe.failureThreshold
HPA <unknown>	metrics-server tidak ada	Install metrics-server
Ingress 404	Ingress controller tidak ada	Install ingress-nginx
TLS pending	cert-manager tidak ada	Install cert-manager + ClusterIssuer
Security Notes
runAsNonRoot: true + runAsUser: 1000 — container bukan root

readOnlyRootFilesystem: false — diperlukan Spring Boot untuk tmp files; pertimbangkan emptyDir di production

drop ALL capabilities — no privileged operations

Pod Security baseline di namespace

Secret: ganti stringData dengan eksternal secret manager di production