# CI/CD 파이프라인 테스트 결과

> 테스트 일시: 2026-02-20
> Jenkins Build #2 — **SUCCESS**

---

## 1. 파이프라인 구성 요약

| 항목 | 값 |
|------|---|
| CI/CD 도구 | Jenkins (Windows 로컬, `http://localhost:18080`) |
| 소스 저장소 | GitHub (`20251029-hanhwa-swcamp-22th/be22-4st-team2-project`) |
| 이미지 레지스트리 | Docker Hub (`ckato9173`) |
| 배포 대상 | Docker Desktop Kubernetes |
| GitOps CD | ArgoCD (`https://localhost:30443`) |
| 트리거 | GitHub Webhook (`githubPush()`) + 수동 빌드 |

---

## 2. 파이프라인 스테이지별 결과

```
Checkout → Skip CI Check → Backend Build & Test → Docker Build → Image Security Scan (Trivy) → Docker Push → Update GitOps Manifest
```

> **참고**: Build #2 당시 파이프라인에서 이후 추가된 스테이지: `Skip CI Check`, `Image Security Scan (Trivy)`, `Update GitOps Manifest` (kubectl 직접 배포 → GitOps 방식으로 전환)

### Stage 1: Checkout

| 항목 | 결과 |
|------|------|
| 상태 | ✅ SUCCESS |
| 방식 | GitHub PAT 토큰 기반 HTTPS clone |
| 브랜치 | `main` |
| 커밋 | `Merge pull request #22 from feat/k8s-cicd-pipeline` |

```
Fetching upstream changes from https://****@github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git
Checking out Revision 3a8b51da80781b1b7e6d11bf947c193cc480942e (refs/remotes/origin/main)
```

### Stage 2: Backend Test

| 항목 | 결과 |
|------|------|
| 상태 | ✅ SUCCESS |
| 명령 | `gradlew.bat clean test` |
| 소요 시간 | 36초 |
| 테스트 결과 | 6 tasks executed, 전체 통과 |
| 리포트 | JUnit XML (`**/build/test-results/test/*.xml`) |

```
> Task :compileJava
> Task :processResources
> Task :classes
> Task :compileTestJava
> Task :processTestResources
> Task :testClasses
> Task :test
BUILD SUCCESSFUL in 36s
6 actionable tasks: 6 executed
```

### Stage 3: Docker Build (병렬)

| 이미지 | 태그 | 결과 |
|--------|------|------|
| `ckato9173/salesboost-backend` | `:2`, `:latest` | ✅ SUCCESS |
| `ckato9173/salesboost-frontend` | `:2`, `:latest` | ✅ SUCCESS |

**Backend Image:**
- 기반: Amazon Corretto 21 (Multi-stage)
- Gradle 빌드 포함 (`build -x test`)
- 소요 시간: ~69초

```
> Task :build
BUILD SUCCESSFUL in 1m 8s
6 actionable tasks: 6 executed
```

**Frontend Image:**
- 기반: Node 20 Alpine → Nginx Alpine (Multi-stage)
- Vite 프로덕션 빌드 포함
- 빌드 출력: `index.html` (0.47 kB), `index.css` (44.25 kB), `index.js` (219.86 kB)

```
vite v7.3.1 building client environment for production...
✓ 1778 modules transformed.
✓ built in 8.70s
```

### Stage 4: Docker Push

| 이미지 | 태그 | 결과 |
|--------|------|------|
| `ckato9173/salesboost-backend:2` | Push 완료 | ✅ SUCCESS |
| `ckato9173/salesboost-backend:latest` | Push 완료 | ✅ SUCCESS |
| `ckato9173/salesboost-frontend:2` | Push 완료 | ✅ SUCCESS |
| `ckato9173/salesboost-frontend:latest` | Push 완료 | ✅ SUCCESS |

```
Login Succeeded
salesboost-backend:2    → digest: sha256:a46dc833...
salesboost-frontend:2   → digest: sha256:7aad26cc...
```

### Stage 5: Deploy to K8s

| 리소스 | 명령 | 결과 |
|--------|------|------|
| Secret/ConfigMap/PVC | `kubectl apply -f common.yaml` | ✅ configured/unchanged |
| Backend Deployment | `kubectl apply -f backend.yaml` | ✅ configured |
| Frontend Deployment | `kubectl apply -f frontend.yaml` | ✅ configured |
| Ingress | `kubectl apply -f ingress.yaml` | ✅ unchanged |
| Backend Restart | `kubectl rollout restart` | ✅ restarted |
| Frontend Restart | `kubectl rollout restart` | ✅ restarted |

```
secret/db-secret configured
secret/app-secret configured
configmap/app-config unchanged
deployment.apps/salesboost-backend configured
deployment.apps/salesboost-frontend configured
ingress.networking.k8s.io/salesboost-ingress unchanged
deployment.apps/salesboost-backend restarted
deployment.apps/salesboost-frontend restarted
```

### Stage 6: Verify

| Deployment | Timeout | 결과 |
|------------|---------|------|
| `salesboost-backend` | 180s | ✅ successfully rolled out |
| `salesboost-frontend` | 60s | ✅ successfully rolled out |

```
Waiting for deployment "salesboost-backend" rollout to finish: 1 old replicas are pending termination...
deployment "salesboost-backend" successfully rolled out
deployment "salesboost-frontend" successfully rolled out
```

### Post Actions

```
docker logout → Removing login credentials for https://index.docker.io/v1/
Pipeline SUCCESS - image tag: 2
```

---

## 3. 빌드 이력

| Build # | 트리거 | 결과 | 실패 원인 | 비고 |
|---------|--------|------|-----------|------|
| #1 | 수동 (Build Now) | ❌ FAILURE | Jenkins SYSTEM 계정에 kubeconfig 없음 | Checkout~Push 성공, K8s Deploy에서 실패 |
| #2 | 수동 (Build Now) | ✅ SUCCESS | - | `KUBECONFIG` 경로 추가 후 전 스테이지 통과 |

### Build #1 실패 원인 및 해결

**증상:**
```
error: error validating "infra/k8s/common.yaml": failed to download openapi:
Get "http://localhost:8080/openapi/v2?timeout=32s": dial tcp [::1]:8080: connectex:
No connection could be made because the target machine actively refused it.
```

**원인:** Jenkins가 Windows 서비스(SYSTEM 계정)로 실행되어 사용자(`playdata2`)의 `~/.kube/config`를 찾지 못함

**해결:** Jenkinsfile `environment` 블록에 `KUBECONFIG` 경로 명시
```groovy
environment {
    KUBECONFIG = 'C:\\Users\\playdata2\\.kube\\config'
}
```

---

## 4. 전체 파이프라인 흐름도

```
┌──────────────────────────────────────────────────────────────────┐
│                    Jenkins Pipeline (Build #2)                    │
├──────────┬──────────┬──────────┬──────────┬──────────┬──────────┤
│ Checkout │ Backend  │ Docker   │ Docker   │ Deploy   │ Verify   │
│          │ Test     │ Build    │ Push     │ to K8s   │          │
│   ✅     │   ✅     │   ✅     │   ✅     │   ✅     │   ✅     │
│          │          │ (병렬)   │          │          │          │
│ GitHub   │ Gradle   │ Backend  │ Hub에    │ kubectl  │ rollout  │
│ clone    │ test     │ Frontend │ 4개 Push │ apply +  │ status   │
│          │ (36s)    │ 동시빌드 │          │ restart  │ 확인     │
└──────────┴──────────┴──────────┴──────────┴──────────┴──────────┘
                                                           │
                                                           ▼
                                              Pipeline SUCCESS ✅
                                              image tag: 2
```

---

## 5. 검증된 인프라 구성

```
┌─────────────────────────────────────────────────────────┐
│              Docker Desktop Kubernetes                    │
│                                                           │
│  ┌─────────────────┐    ┌──────────────────┐             │
│  │ salesboost-     │    │ salesboost-      │             │
│  │ frontend        │    │ backend          │             │
│  │ (Nginx + Vue)   │    │ (Spring Boot)    │             │
│  │ :80             │    │ :8080            │             │
│  └────────┬────────┘    └────────┬─────────┘             │
│           │                      │                        │
│  ┌────────▼──────────────────────▼─────────┐             │
│  │         NGINX Ingress Controller         │             │
│  │   / → frontend    /api → backend         │             │
│  └──────────────────────────────────────────┘             │
│                              │                            │
└──────────────────────────────┼────────────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   외부 공용 DB       │
                    │ 221.148.116.109:10002│
                    │ MariaDB (salesboost) │
                    └─────────────────────┘
```

---

## 6. 사용 포트 현황

| 서비스 | 포트 | 상태 |
|--------|------|------|
| Frontend + Ingress | `localhost:80` | ✅ 정상 |
| Backend API | `localhost/api/*` (Ingress 경유) | ✅ 정상 |
| 공용 DB | `221.148.116.109:10002` | ✅ 연결됨 |
| Jenkins | `localhost:18080` | ✅ 정상 |
| ArgoCD | `localhost:30443` | ✅ Synced |
| Docker Hub | `hub.docker.com/u/ckato9173` | ✅ 이미지 업로드됨 |

---

## 7. 현재 파이프라인 변경 사항 (Build #2 이후)

Build #2 테스트 이후 파이프라인에 다음 개선이 적용되었습니다:

| 변경 사항 | 이전 | 이후 |
|-----------|------|------|
| **트리거** | pollSCM (5분 주기) | GitHub Webhook (`githubPush()`) |
| **배포 방식** | kubectl apply + rollout restart | GitOps (매니페스트 업데이트 → ArgoCD 자동 sync) |
| **보안 스캔** | 없음 | Trivy 이미지 취약점 스캔 (HIGH/CRITICAL) |
| **CI 무한루프 방지** | 없음 | `[ci skip]` 커밋 메시지 감지 |
| **ArgoCD 상태 확인** | 없음 | `argocd app wait` sync/health 확인 |
| **Git push 충돌 방지** | 없음 | 최대 3회 rebase + retry |

### 현재 파이프라인 흐름

```
┌──────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐
│ Checkout │ Skip CI  │ Build &  │ Docker   │ Trivy    │ Docker   │ GitOps   │
│          │ Check    │ Test     │ Build    │ Scan     │ Push     │ Manifest │
│ GitHub   │ [ci skip]│ Gradle   │ Backend  │ HIGH/    │ Hub에    │ sed →    │
│ clone    │ 감지     │ clean    │ Frontend │ CRITICAL │ Push     │ commit → │
│          │          │ build    │ (병렬)   │ 리포트   │          │ push     │
└──────────┴──────────┴──────────┴──────────┴──────────┴──────────┴──────────┘
                                                                       │
                                                              ArgoCD 자동 Sync
```
