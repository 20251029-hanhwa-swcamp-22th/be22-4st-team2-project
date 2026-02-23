# DevOps 고급 기능 가이드

HPA 자동 확장, 부하 테스트, 모니터링 알림 등 고득점 요소 구현 가이드입니다.

---

## 1. HPA (Horizontal Pod Autoscaler)

### 1.1 개요

`salesboost-backend` Deployment의 CPU 사용률이 **70%를 초과**하면 Pod가 **최소 1개 → 최대 3개**까지 자동 확장됩니다.

```
[CPU 70% 초과] → HPA 감지 → Pod 1→2→3 Scale-out
[CPU 70% 이하] → HPA 감지 → Pod 3→2→1 Scale-in (5분 안정화 후)
```

### 1.2 사전 요구사항

HPA가 CPU 메트릭을 수집하려면 클러스터에 **Metrics Server**가 필요합니다.

```bash
# Metrics Server 설치 (최초 1회)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# 설치 확인
kubectl top nodes
kubectl top pods
```

> **minikube 사용 시**: `minikube addons enable metrics-server`

### 1.3 매니페스트

파일: [`infra/k8s/hpa.yaml`](../infra/k8s/hpa.yaml)

ArgoCD가 `infra/k8s/` 하위를 자동 동기화하므로, main 브랜치에 푸시하면 HPA가 자동 적용됩니다.

### 1.4 동작 확인

```bash
# HPA 상태 확인
kubectl get hpa

# 실시간 모니터링 (CPU%, 현재/최소/최대 Replicas)
kubectl get hpa salesboost-backend-hpa -w

# 출력 예시:
# NAME                      REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS
# salesboost-backend-hpa    Deployment/salesboost-backend   45%/70%   1         3         1
```

### 1.5 HPA 스케일링 정책

| 방향 | 안정화 대기 | 확장 속도 | 설명 |
|------|------------|-----------|------|
| **Scale-up** | 30초 | 1분당 1개 Pod | 급격한 확장 방지, 점진적 증가 |
| **Scale-down** | 300초 (5분) | 2분당 1개 Pod | 트래픽 재유입에 대비해 보수적 축소 |

---

## 2. 부하 테스트 (k6)

### 2.1 k6 설치

```bash
# macOS
brew install k6

# Windows
winget install k6

# Linux
snap install k6
```

### 2.2 테스트 스크립트

파일: [`tests/load-test.js`](../tests/load-test.js)

3단계 부하 시나리오:

| 단계 | 시간 | VU (가상 유저) | 목적 |
|------|------|--------------|------|
| Ramp-up | 0~1분 | 0 → 50 | 점진적 부하 증가 (워밍업) |
| Sustain | 1~4분 | 50 유지 | **최대 부하 유지 (HPA 트리거 구간)** |
| Ramp-down | 4~5분 | 50 → 0 | 부하 감소 (Scale-down 관찰) |

### 2.3 실행 방법

```bash
# 기본 실행 (localhost)
k6 run tests/load-test.js

# K8s 클러스터 Ingress IP로 실행
k6 run -e BASE_URL=http://<INGRESS-IP> tests/load-test.js

# Docker Compose 환경에서 실행
k6 run -e BASE_URL=http://localhost:80 tests/load-test.js
```

### 2.4 데모 시나리오: HPA + Grafana 연동 확인

발표에서 실시간으로 보여줄 수 있는 데모 시나리오입니다.

#### 터미널 구성 (3개 창)

```
┌──────────────────────────────┬──────────────────────────────┐
│ 터미널 1: k6 부하 테스트      │ 터미널 2: HPA 실시간 모니터링  │
│                              │                              │
│ k6 run tests/load-test.js   │ kubectl get hpa -w           │
│                              │                              │
├──────────────────────────────┴──────────────────────────────┤
│ 브라우저: Grafana 대시보드 (http://<GRAFANA-URL>)            │
│ → CPU 사용률 패널, Pod 개수 패널, HTTP 요청 수 패널           │
└─────────────────────────────────────────────────────────────┘
```

#### 시간별 예상 흐름

| 시간 | k6 상태 | HPA 상태 | Grafana 관찰 포인트 |
|------|---------|----------|-------------------|
| 0:00 | 시작, VU 증가 중 | Replicas: 1 | CPU 그래프 상승 시작 |
| 0:30 | 30 VU | CPU 50~60% | CPU 그래프 지속 상승 |
| 1:00 | 50 VU 도달 | **CPU > 70%, Scale-up 시작** | **CPU 70% 임계선 돌파** |
| 1:30 | 50 VU 유지 | **Replicas: 1 → 2** | Pod 개수 증가, CPU 분산 감소 |
| 2:30 | 50 VU 유지 | CPU 재상승 시 Replicas: 2 → 3 | 트래픽 분산 확인 |
| 4:00 | VU 감소 시작 | CPU 하락 | CPU 그래프 하강 |
| 5:00 | 종료 | 5분 안정화 후 Scale-down 시작 | Pod 개수 점진적 감소 |

#### Grafana에서 확인할 주요 PromQL 쿼리

Grafana 대시보드에서 아래 PromQL로 패널을 추가하면 데모 효과가 극대화됩니다.

```promql
# 1. 백엔드 CPU 사용률 (%)
rate(process_cpu_usage{application="salesboost-backend"}[1m]) * 100

# 2. JVM 메모리 사용량
jvm_memory_used_bytes{application="salesboost-backend", area="heap"}

# 3. HTTP 요청 수 (초당)
rate(http_server_requests_seconds_count{application="salesboost-backend"}[1m])

# 4. HTTP 응답 시간 (95 퍼센타일)
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{application="salesboost-backend"}[1m]))

# 5. 현재 활성 Pod 수 (kube-state-metrics 필요)
# 또는 kubectl get hpa -w 로 터미널에서 직접 관찰
```

---

## 3. 파이프라인 병렬 빌드 + GitOps 발표 대본

> 아래는 심사위원에게 CI/CD 파이프라인의 이점을 어필하는 **약 1분 분량** 발표 대본입니다.

---

저희 팀의 CI/CD 파이프라인은 **Jenkins와 ArgoCD**를 결합한 **GitOps 아키텍처**입니다.

먼저 **빌드 효율성**입니다. Jenkinsfile에서 Backend Docker 이미지와 Frontend Docker 이미지를 **parallel 블록으로 동시에 빌드**합니다. 순차 빌드 대비 **전체 빌드 시간을 약 40% 단축**하는 효과가 있습니다.

다음으로 **배포 안정성**입니다. 빌드된 이미지를 Docker Hub에 푸시한 뒤, Jenkins는 클러스터에 직접 `kubectl apply`를 실행하지 않습니다. 대신 K8s 매니페스트 파일의 이미지 태그만 업데이트하고 **Git에 커밋**합니다. 그러면 ArgoCD가 Git 변경을 감지하여 **자동으로 클러스터에 동기화**합니다.

이 방식의 핵심 이점은 세 가지입니다.

**첫째, Git이 배포의 Single Source of Truth입니다.** 현재 클러스터에 어떤 버전이 배포되어 있는지 Git 커밋 로그만 보면 알 수 있습니다.

**둘째, 롤백이 간단합니다.** 문제가 발생하면 `git revert` 한 번으로 이전 버전으로 즉시 복원됩니다. kubectl 명령어를 기억할 필요가 없습니다.

**셋째, ArgoCD의 Self-Heal 기능입니다.** 누군가 `kubectl`로 직접 클러스터를 변경하더라도, ArgoCD가 Git 상태와 다른 것을 감지하고 자동으로 원래 상태로 복구합니다. 이로써 **운영 환경의 일관성**이 보장됩니다.

추가로, **HPA(Horizontal Pod Autoscaler)**를 적용하여 CPU 사용률 70% 초과 시 백엔드 Pod가 최대 3개까지 자동 확장됩니다. k6 부하 테스트로 이를 검증했으며, Grafana 대시보드에서 스케일링 과정을 실시간 모니터링할 수 있습니다.

---

## 4. 모니터링 알림 (Grafana → Discord Webhook)

### 4.1 개요

Grafana에서 다음 상황 발생 시 **Discord 채널에 자동 알림**을 보냅니다:

| 알림 조건 | 임계값 | 의미 |
|----------|--------|------|
| CPU 사용률 급증 | > 80% (5분 지속) | HPA 한계 도달 전 조기 경고 |
| 메모리 사용률 급증 | > 85% (5분 지속) | OOM Kill 위험 사전 감지 |
| HTTP 5xx 에러율 | > 5% (3분 지속) | 서버 장애 발생 |

### 4.2 Step 1: Discord Webhook URL 생성

1. Discord 서버에서 알림을 받을 채널 선택
2. **채널 설정(톱니바퀴)** → **연동** → **웹후크** → **새 웹후크**
3. 이름: `SalesBoost Alert` (자유롭게 변경 가능)
4. **웹후크 URL 복사** (예: `https://discord.com/api/webhooks/1234567890/abcdef...`)

### 4.3 Step 2: Grafana Contact Point 설정

1. Grafana 접속 (`http://<GRAFANA-URL>:3000`)
2. 좌측 메뉴 **Alerting** → **Contact points**
3. **+ Add contact point** 클릭
4. 설정:

| 항목 | 값 |
|------|-----|
| Name | `Discord Alerts` |
| Integration | `Discord` |
| Webhook URL | Step 1에서 복사한 URL |
| Message | (기본값 사용 또는 아래 커스텀 템플릿 참고) |

5. **Test** 버튼으로 테스트 메시지 전송 확인
6. **Save contact point**

#### 메시지 커스텀 템플릿 (선택)

```
{{ range .Alerts }}
**[{{ .Status | toUpper }}]** {{ .Labels.alertname }}
{{ .Annotations.summary }}
값: {{ .ValueString }}
{{ end }}
```

### 4.4 Step 3: Notification Policy 설정

1. **Alerting** → **Notification policies**
2. 기본 정책(Default policy)의 **Contact point**를 `Discord Alerts`로 변경
3. **Save policy**

### 4.5 Step 4: Alert Rule 생성

#### Alert Rule 1: CPU 사용률 급증

1. **Alerting** → **Alert rules** → **+ New alert rule**
2. 설정:

| 항목 | 값 |
|------|-----|
| Rule name | `High CPU Usage` |
| Data source | `Prometheus` |
| PromQL | `rate(process_cpu_usage{application="salesboost-backend"}[5m]) * 100` |
| Condition | `IS ABOVE 80` |
| Evaluate every | `1m` |
| Pending period | `5m` |
| Summary | `Backend CPU 사용률이 80%를 초과했습니다` |

#### Alert Rule 2: 메모리 사용률 급증

| 항목 | 값 |
|------|-----|
| Rule name | `High Memory Usage` |
| PromQL | `jvm_memory_used_bytes{application="salesboost-backend", area="heap"} / jvm_memory_max_bytes{application="salesboost-backend", area="heap"} * 100` |
| Condition | `IS ABOVE 85` |
| Pending period | `5m` |
| Summary | `Backend Heap 메모리 사용률이 85%를 초과했습니다` |

#### Alert Rule 3: HTTP 5xx 에러율

| 항목 | 값 |
|------|-----|
| Rule name | `High Error Rate (5xx)` |
| PromQL | `rate(http_server_requests_seconds_count{application="salesboost-backend", status=~"5.."}[3m]) / rate(http_server_requests_seconds_count{application="salesboost-backend"}[3m]) * 100` |
| Condition | `IS ABOVE 5` |
| Pending period | `3m` |
| Summary | `Backend 5xx 에러율이 5%를 초과했습니다` |

### 4.6 알림 동작 흐름

```
[Prometheus 메트릭 수집 (15초 간격)]
    ↓
[Grafana Alert Rule 평가 (1분 간격)]
    ↓ 임계값 초과
[Pending 상태 (설정된 대기 시간)]
    ↓ 대기 시간 동안 지속
[Firing 상태 → Discord Webhook 발송]
    ↓
[Discord 채널에 알림 메시지 수신]

# 임계값 이하로 복구되면:
[Resolved 상태 → Discord에 복구 알림 발송]
```

### 4.7 알림 테스트 방법

```bash
# k6 부하 테스트로 CPU 알림 트리거
k6 run tests/load-test.js

# Grafana에서 Alert Rule 상태 확인:
# Alerting → Alert rules → 각 Rule의 상태 (Normal / Pending / Firing)
```

---

## 5. 전체 아키텍처 요약

```
[GitHub Push]
    ↓ Webhook
[Jenkins CI Pipeline]
    ├─ Backend Build & Test (./gradlew clean build)
    ├─ Docker Build (Backend + Frontend 병렬)
    ├─ Image Security Scan (Trivy: HIGH/CRITICAL 취약점 리포트)
    ├─ Docker Push → Docker Hub
    └─ GitOps Manifest Update (sed → git commit → push)
         ↓ Git 변경 감지
[ArgoCD CD]
    ├─ 자동 동기화 (prune + selfHeal)
    ├─ ArgoCD sync 상태 확인 (argocd app wait)
    └─ K8s 클러스터 배포
         ↓
[Kubernetes Cluster]
    ├─ salesboost-backend (HPA: 1~3 Pods, imagePullPolicy: Always)
    ├─ salesboost-frontend (imagePullPolicy: Always)
    ├─ Prometheus v2.53.3 → Grafana 11.5.2 (메트릭 모니터링 + 알림 규칙 + Discord 알림)
    └─ Logstash → Elasticsearch → Kibana 7.17.29 (로그 모니터링)
         ↓ 부하 발생 시
[HPA 자동 확장: CPU 70% → Scale-out]
         ↓ 장애 감지 시
[Prometheus Alert Rules → Grafana Alert → Discord Webhook → 팀 알림]
```

### Prometheus 알림 규칙 (사전 정의)

파이프라인과 별도로, Prometheus에 다음 알림 규칙이 `alert_rules.yml`로 사전 정의되어 있습니다:

| 알림 | 조건 | 대기 | 심각도 |
|------|------|------|--------|
| BackendDown | 메트릭 수집 실패 | 5분 | critical |
| HighErrorRate | 5xx > 5% | 5분 | warning |
| HighJvmMemoryUsage | 힙 > 80% | 5분 | warning |
| DatabaseConnectionPoolLow | 유휴 커넥션 < 2 | 3분 | warning |

Grafana에서 이 알림을 Discord/Slack으로 전달하려면 위 4.2~4.4 섹션의 Contact Point 설정을 참고하세요.
