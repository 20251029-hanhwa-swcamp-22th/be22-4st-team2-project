# SalesBoost 통합 배포 가이드

Docker Compose (로컬 개발) → JWT/DB 시크릿 설정 → Docker Desktop Kubernetes → Jenkins CI → ArgoCD CD 까지 전 과정을 단계별로 정리한 실전 가이드입니다.

---

## 목차

1. [전체 아키텍처 한눈에 보기](#1-전체-아키텍처-한눈에-보기)
2. [Phase 0: 현재 상태 — Docker Compose 로컬 개발](#2-phase-0-현재-상태--docker-compose-로컬-개발)
3. [Phase 1: JWT 시크릿 키 설정](#3-phase-1-jwt-시크릿-키-설정)
4. [Phase 2: 공용 DB 연결](#4-phase-2-공용-db-연결)
5. [Phase 3: Docker Desktop Kubernetes 배포](#5-phase-3-docker-desktop-kubernetes-배포)
6. [Phase 4: Jenkins CI 파이프라인](#6-phase-4-jenkins-ci-파이프라인)
7. [Phase 5: ArgoCD GitOps CD](#7-phase-5-argocd-gitops-cd)
8. [Phase 6: 전체 파이프라인 통합 테스트](#8-phase-6-전체-파이프라인-통합-테스트)
9. [트러블슈팅 모음](#9-트러블슈팅-모음)

---

## 1. 전체 아키텍처 한눈에 보기

```
개발자 PC (Docker Desktop)
═══════════════════════════════════════════════════════════════════════

  [Phase 0] 로컬 개발         [Phase 3~5] K8s + CI/CD 배포
  ────────────────────        ───────────────────────────────────────

  docker-compose up            Git Push
        │                        │
        ▼                        ▼
  ┌──────────┐             ┌──────────┐    Webhook     ┌──────────┐
  │ Frontend │             │  GitHub  │──────────────▶│ Jenkins  │
  │  :80     │             │   Repo   │               │  (CI)    │
  ├──────────┤             └──────────┘               └────┬─────┘
  │ Backend  │                                             │
  │  :8080   │                Docker Build & Push           │
  ├──────────┤                                             ▼
  │ MariaDB  │             ┌───────────────┐        ┌──────────────┐
  │  :3306   │             │ Docker Hub /  │◀───────│ 이미지 빌드   │
  └──────────┘             │ Local Registry│        └──────────────┘
                           └───────┬───────┘
                                   │
                           ┌───────▼───────┐
                           │ GitOps Repo   │  (K8s 매니페스트 이미지 태그 업데이트)
                           │ (infra/k8s/)  │
                           └───────┬───────┘
                                   │ 감지
                           ┌───────▼───────┐
                           │   ArgoCD      │
                           │   (CD)        │
                           └───────┬───────┘
                                   │ 동기화
                           ┌───────▼───────────────────────────────┐
                           │     Docker Desktop Kubernetes         │
                           │  ┌────────┐ ┌────────┐ ┌──────────┐  │
                           │  │Frontend│ │Backend │ │ MariaDB/ │  │
                           │  │  Pod   │ │  Pod   │ │ 공용 DB  │  │
                           │  └────────┘ └────────┘ └──────────┘  │
                           └───────────────────────────────────────┘
```

---

## 2. Phase 0: 현재 상태 — Docker Compose 로컬 개발

### 2.1 현재 구성 요약

| 항목 | 설정 |
|------|------|
| Backend | Spring Boot 3.5 + Java 21 (Gradle) |
| Frontend | Vue 3 + Vite + Nginx |
| DB | MariaDB 10.11 (컨테이너) |
| 접속 주소 | `http://localhost` (프론트), `http://localhost:8080` (API) |

### 2.2 실행 방법

```bash
# 1. 환경변수 파일 생성 (최초 1회)
cp .env.example .env

# 2. 전체 서비스 시작
docker compose up --build -d

# 3. 상태 확인
docker compose ps

# 4. 로그 확인
docker compose logs -f backend
```

### 2.3 주요 파일 위치

```
프로젝트 루트/
├── docker-compose.yml          # 서비스 정의 (backend, frontend, mariadb)
├── Dockerfile                  # 백엔드 이미지 (멀티스테이지 빌드)
├── frontend/Dockerfile         # 프론트엔드 이미지 (빌드 → nginx)
├── frontend/nginx.conf         # nginx 리버스 프록시 설정
├── .env.example                # 환경변수 템플릿
├── .env                        # 실제 환경변수 (Git 미추적)
├── infra/docker/mariadb/init.sql  # DB 초기화 SQL
├── infra/k8s/                  # Kubernetes 매니페스트
│   ├── common.yaml             # Secret, ConfigMap, PVC
│   ├── ingress.yaml            # Ingress 라우팅 규칙
│   ├── argocd-app.yaml         # ArgoCD Application 매니페스트
│   └── deployments/
│       ├── backend.yaml
│       ├── frontend.yaml
│       └── db.yaml
└── src/main/resources/application.yml  # Spring Boot 설정
```

---

## 3. Phase 1: JWT 시크릿 키 설정

### 3.1 JWT 동작 원리 (우리 프로젝트)

```
[관리자 로그인]                    [인증된 API 요청]
POST /api/admin/login              GET /api/admin/inquiries
  │                                  │
  ▼                                  ▼
JwtProvider.generateToken()        JwtAuthenticationFilter
  │                                  │
  ▼                                  ▼
HMAC-SHA 서명 (APP_JWT_SECRET)     토큰 검증 (같은 시크릿으로 복호화)
  │                                  │
  ▼                                  ▼
Bearer 토큰 반환                    SecurityContext에 인증정보 설정
```

- 시크릿 키: `APP_JWT_SECRET` 환경변수 → `application.yml`의 `app.jwt.secret`
- 알고리즘: HMAC-SHA (jjwt 0.12.6)
- 만료 시간: 3600초 (1시간)
- **최소 32바이트** 이상이어야 함

### 3.2 강력한 시크릿 키 생성

```bash
# 방법 1: openssl (권장)
openssl rand -base64 48
# 출력 예시: aB3dE5fG7hI9jK1lM3nO5pQ7rS9tU1vW3xY5zA7bC9dE1fG3hI5jK7l

# 방법 2: Git Bash / WSL 에서
cat /dev/urandom | tr -dc 'A-Za-z0-9' | head -c 64
# 출력 예시: X7kM2pN9qR4sT6uV8wY1zA3bC5dE7fG9hI2jK4lM6nO8pQ1rS3tU5vW7x

# 방법 3: PowerShell 에서
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

### 3.3 환경별 시크릿 적용

#### 로컬 개발 (.env 파일)

```bash
# .env 파일 수정
APP_JWT_SECRET=여기에-생성한-시크릿-키-붙여넣기
```

#### Docker Compose

docker-compose.yml에서 자동으로 `.env`의 `APP_JWT_SECRET`을 읽어 백엔드 컨테이너에 주입합니다. 별도 수정 불필요.

```yaml
# docker-compose.yml (이미 설정됨)
environment:
  - APP_JWT_SECRET=${APP_JWT_SECRET:-change-this-to-very-long-secret-key-at-least-32bytes}
```

#### Kubernetes (Secret)

```bash
# 시크릿을 직접 생성 (common.yaml의 placeholder 대체)
kubectl create secret generic app-secret \
  --from-literal=jwt-secret='여기에-생성한-시크릿-키-붙여넣기' \
  --dry-run=client -o yaml | kubectl apply -f -
```

또는 `infra/k8s/common.yaml`의 `app-secret` 부분을 직접 수정:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secret
  namespace: default
type: Opaque
stringData:
  jwt-secret: "여기에-생성한-시크릿-키-붙여넣기"
```

> **중요**: `common.yaml`에 실제 시크릿을 넣고 Git에 커밋하지 마세요. 로컬 클러스터에서만 테스트 용도로 사용하고, 프로덕션에서는 `kubectl create secret` 명령으로 직접 생성하세요.

### 3.4 시크릿 키 주의사항

| 항목 | 규칙 |
|------|------|
| 최소 길이 | 32바이트 이상 (Base64로 48자 이상 권장) |
| 환경별 분리 | 로컬/스테이징/프로덕션 각각 다른 키 사용 |
| Git 커밋 금지 | `.env`와 실제 Secret 값은 절대 커밋하지 않음 |
| 팀 공유 방법 | Slack DM, 사내 비밀번호 관리 도구 등으로 안전하게 전달 |
| 키 변경 시 | 기존 발급된 토큰 모두 무효화됨 (재로그인 필요) |

---

## 4. Phase 2: 공용 DB 연결

### 4.1 시나리오: 팀 공용 외부 DB 정보를 받았을 때

공용 DB 정보 예시:

```
호스트: db.example.com (또는 192.168.x.x)
포트: 3306
DB명: salesboost
사용자: salesboost_user
비밀번호: P@ssw0rd!Str0ng
```

### 4.2 로컬 개발 (IDE에서 직접 실행)

`src/main/resources/application.yml`은 환경변수가 없으면 기본값을 사용합니다. 환경변수를 설정하면 자동으로 오버라이드됩니다.

**방법 A: .env 파일 수정 후 Docker Compose 실행**

```bash
# .env 파일 수정
SPRING_DATASOURCE_URL=jdbc:mariadb://db.example.com:3306/salesboost?allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=salesboost_user
DB_PASSWORD=P@ssw0rd!Str0ng
```

**방법 B: IntelliJ에서 직접 실행 시 (환경변수 설정)**

Run Configuration → Environment variables:
```
SPRING_DATASOURCE_URL=jdbc:mariadb://db.example.com:3306/salesboost?allowPublicKeyRetrieval=true&useSSL=false
SPRING_DATASOURCE_USERNAME=salesboost_user
SPRING_DATASOURCE_PASSWORD=P@ssw0rd!Str0ng
```

### 4.3 Docker Compose에서 외부 DB 사용

외부 DB를 쓸 때는 `mariadb` 서비스가 필요 없습니다.

```bash
# .env 파일 수정
SPRING_DATASOURCE_URL=jdbc:mariadb://db.example.com:3306/salesboost?allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=salesboost_user
DB_PASSWORD=P@ssw0rd!Str0ng
```

```bash
# mariadb 서비스 제외하고 실행
docker compose up --build -d backend frontend
```

> 주의: `docker-compose.yml`에서 backend의 `depends_on: mariadb` 때문에 에러가 날 수 있습니다.
> 그런 경우 아래처럼 `docker-compose.override.yml`을 만들어 mariadb 의존성을 제거합니다.

```yaml
# docker-compose.override.yml (프로젝트 루트에 생성)
services:
  backend:
    depends_on: {}    # mariadb 의존성 제거
```

### 4.4 Kubernetes에서 외부 DB 사용

외부 DB를 쓰면 `db.yaml` (MariaDB Deployment)은 배포하지 않습니다. 대신 ConfigMap과 Secret만 수정합니다.

**Step 1: ConfigMap 수정** (`infra/k8s/common.yaml`)

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: default
data:
  DB_DATABASE: "salesboost"
  DB_USERNAME: "salesboost_user"
  SPRING_DATASOURCE_URL: "jdbc:mariadb://db.example.com:3306/salesboost?allowPublicKeyRetrieval=true&useSSL=false"
  APP_CORS_ALLOWED_ORIGINS: "http://localhost,http://localhost:80"
```

**Step 2: Secret 수정**

```bash
kubectl create secret generic db-secret \
  --from-literal=password='P@ssw0rd!Str0ng' \
  --dry-run=client -o yaml | kubectl apply -f -
```

**Step 3: 배포 (DB Pod 제외)**

```bash
# 공용 리소스 + 백엔드 + 프론트엔드만 배포 (db.yaml 제외)
kubectl apply -f infra/k8s/common.yaml
kubectl apply -f infra/k8s/deployments/backend.yaml
kubectl apply -f infra/k8s/deployments/frontend.yaml
kubectl apply -f infra/k8s/ingress.yaml
```

### 4.5 연결 테스트

```bash
# Docker Compose 환경
docker compose exec backend sh -c \
  'curl -sf http://localhost:8080/api/portfolios && echo "OK" || echo "FAIL"'

# Kubernetes 환경
kubectl exec deploy/salesboost-backend -- sh -c \
  'curl -sf http://localhost:8080/api/portfolios && echo "OK" || echo "FAIL"'
```

---

## 5. Phase 3: Docker Desktop Kubernetes 배포

### 5.1 사전 준비

#### Docker Desktop에서 Kubernetes 활성화

1. Docker Desktop 열기
2. Settings (⚙️) → Kubernetes
3. **"Enable Kubernetes"** 체크
4. "Apply & restart" 클릭
5. 좌측 하단에 "Kubernetes running" 초록불 확인

```bash
# 클러스터 정상 동작 확인
kubectl cluster-info
# 출력: Kubernetes control plane is running at https://kubernetes.docker.internal:6443

kubectl get nodes
# 출력: docker-desktop   Ready   control-plane   ...
```

#### NGINX Ingress Controller 설치

Docker Desktop K8s에서 Ingress를 사용하려면 Ingress Controller를 별도 설치해야 합니다.

```bash
# NGINX Ingress Controller 설치
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.12.0/deploy/static/provider/cloud/deploy.yaml

# 설치 확인 (1~2분 소요)
kubectl get pods -n ingress-nginx
# ingress-nginx-controller-xxxxx   1/1   Running   ...

# Ingress Controller Service 확인
kubectl get svc -n ingress-nginx
# ingress-nginx-controller   LoadBalancer   ...   80:xxxxx/TCP,443:xxxxx/TCP
```

### 5.2 Docker 이미지 빌드

Docker Desktop K8s는 로컬 Docker 이미지를 바로 사용할 수 있습니다 (`imagePullPolicy: Never`).

```bash
# 프로젝트 루트에서 실행
# 백엔드 이미지 빌드
docker build -t salesboost-backend:latest .

# 프론트엔드 이미지 빌드
docker build -t salesboost-frontend:latest ./frontend
```

빌드 확인:

```bash
docker images | grep salesboost
# salesboost-backend    latest   ...   약 400MB
# salesboost-frontend   latest   ...   약 50MB
```

### 5.3 Secret & ConfigMap 생성

> **중요**: 반드시 실제 값으로 변경한 후 적용하세요.

**방법 A: common.yaml 수정 후 적용 (로컬 테스트)**

`infra/k8s/common.yaml`의 placeholder 값들을 실제 값으로 변경:

```yaml
# db-secret → root-password, password 변경
# app-secret → jwt-secret 변경 (Phase 1에서 생성한 키)
# app-config → 필요시 DB URL 변경
```

```bash
kubectl apply -f infra/k8s/common.yaml
```

**방법 B: kubectl 명령으로 직접 생성 (권장)**

```bash
# DB Secret 생성
kubectl create secret generic db-secret \
  --from-literal=root-password='your-strong-root-password' \
  --from-literal=password='your-strong-app-password' \
  --dry-run=client -o yaml | kubectl apply -f -

# JWT Secret 생성
kubectl create secret generic app-secret \
  --from-literal=jwt-secret='$(openssl rand -base64 48)' \
  --dry-run=client -o yaml | kubectl apply -f -

# ConfigMap 생성
kubectl apply -f infra/k8s/common.yaml
# (ConfigMap과 PVC 부분만 적용됨 — Secret은 이미 위에서 생성)

# 확인
kubectl get secret
kubectl get configmap
kubectl get pvc
```

### 5.4 서비스 배포

DB를 K8s 내부 컨테이너로 운영할지, 외부 공용 DB를 쓸지에 따라 달라집니다.

#### 시나리오 A: K8s 내부 DB 사용 (현재 기본값)

```bash
# 1. DB 배포 (먼저 실행해야 백엔드가 접속 가능)
kubectl apply -f infra/k8s/deployments/db.yaml

# DB Ready 확인 (약 30~60초 소요)
kubectl get pods -l app=salesboost-db -w
# salesboost-db-xxxxx   1/1   Running   ...

# 2. 백엔드 배포
kubectl apply -f infra/k8s/deployments/backend.yaml

# 백엔드 Ready 확인 (약 60~90초 소요 — Spring Boot 기동 시간)
kubectl get pods -l app=salesboost-backend -w
# salesboost-backend-xxxxx   1/1   Running   ...

# 3. 프론트엔드 배포
kubectl apply -f infra/k8s/deployments/frontend.yaml

# 4. Ingress 배포
kubectl apply -f infra/k8s/ingress.yaml
```

#### 시나리오 B: 외부 공용 DB 사용

```bash
# db.yaml 제외하고 배포
kubectl apply -f infra/k8s/deployments/backend.yaml
kubectl apply -f infra/k8s/deployments/frontend.yaml
kubectl apply -f infra/k8s/ingress.yaml
```

### 5.5 배포 확인

```bash
# 전체 리소스 확인
kubectl get all

# 기대 출력:
# pod/salesboost-backend-xxxxx    1/1   Running
# pod/salesboost-frontend-xxxxx   1/1   Running
# pod/salesboost-db-xxxxx         1/1   Running  (시나리오 A만)
#
# service/salesboost-backend    ClusterIP    10.x.x.x   8080/TCP
# service/salesboost-frontend   LoadBalancer 10.x.x.x   80:3xxxx/TCP
# service/salesboost-db         ClusterIP    10.x.x.x   3306/TCP  (시나리오 A만)
#
# deployment.apps/salesboost-backend    1/1
# deployment.apps/salesboost-frontend   1/1

# Ingress 확인
kubectl get ingress
# salesboost-ingress   nginx   localhost   80

# 접속 테스트
curl http://localhost              # 프론트엔드 (Vue 앱)
curl http://localhost/api/portfolios  # 백엔드 API
```

> **Docker Desktop 한정**: `frontend` Service가 `LoadBalancer` 타입이므로 `http://localhost`로 바로 접속됩니다. Ingress 없이도 접속 가능하지만, `/api` 라우팅을 위해 Ingress를 사용하는 것을 권장합니다.

### 5.6 원클릭 배포 스크립트

반복 작업을 줄이기 위한 스크립트:

```bash
#!/bin/bash
# scripts/deploy-k8s.sh
# Docker Desktop Kubernetes 원클릭 배포 스크립트

set -e

echo "=== SalesBoost K8s 배포 시작 ==="

# 1. Docker 이미지 빌드
echo "[1/5] Docker 이미지 빌드..."
docker build -t salesboost-backend:latest .
docker build -t salesboost-frontend:latest ./frontend

# 2. Secret & ConfigMap 적용
echo "[2/5] Secret & ConfigMap 적용..."
kubectl apply -f infra/k8s/common.yaml

# 3. DB 배포 (외부 DB 사용 시 이 블록을 주석 처리)
echo "[3/5] DB 배포..."
kubectl apply -f infra/k8s/deployments/db.yaml
echo "  DB Ready 대기 중..."
kubectl wait --for=condition=ready pod -l app=salesboost-db --timeout=120s

# 4. Backend & Frontend 배포
echo "[4/5] Backend & Frontend 배포..."
kubectl apply -f infra/k8s/deployments/backend.yaml
kubectl apply -f infra/k8s/deployments/frontend.yaml

echo "  Backend Ready 대기 중..."
kubectl wait --for=condition=ready pod -l app=salesboost-backend --timeout=180s

echo "  Frontend Ready 대기 중..."
kubectl wait --for=condition=ready pod -l app=salesboost-frontend --timeout=60s

# 5. Ingress 배포
echo "[5/5] Ingress 배포..."
kubectl apply -f infra/k8s/ingress.yaml

echo ""
echo "=== 배포 완료 ==="
echo "프론트엔드: http://localhost"
echo "백엔드 API: http://localhost/api/portfolios"
echo ""
kubectl get pods
```

```bash
# 실행 권한 부여 및 실행
chmod +x scripts/deploy-k8s.sh
./scripts/deploy-k8s.sh
```

### 5.7 정리 (리소스 삭제)

```bash
# 전체 삭제
kubectl delete -f infra/k8s/ingress.yaml
kubectl delete -f infra/k8s/deployments/frontend.yaml
kubectl delete -f infra/k8s/deployments/backend.yaml
kubectl delete -f infra/k8s/deployments/db.yaml
kubectl delete -f infra/k8s/common.yaml

# 또는 한 번에
kubectl delete -f infra/k8s/ingress.yaml -f infra/k8s/deployments/ -f infra/k8s/common.yaml
```

---

## 6. Phase 4: Jenkins CI 파이프라인

### 6.1 우리 환경

| 항목 | 값 |
|------|---|
| Jenkins 주소 | `http://localhost:18080` |
| Jenkins 설치 방식 | Windows 로컬 설치 (Docker/K8s 아님) |
| Docker Hub ID | `ckato9173` |
| GitHub Repo | `20251029-hanhwa-swcamp-22th/be22-4st-team2-project` |
| 이미지 태그 | `ckato9173/salesboost-backend:{빌드번호}`, `ckato9173/salesboost-frontend:{빌드번호}` |

> Jenkins가 Windows에 직접 설치되어 있어 `docker`, `kubectl` 명령을 바로 사용할 수 있습니다.

### 6.2 Jenkins Credentials 등록

Jenkins 관리 → Credentials → System → Global credentials → **Add Credentials**:

| ID (정확히 일치해야 함) | Kind | 입력값 |
|---|---|---|
| `github-token` | **Secret text** | GitHub PAT 토큰만 입력 |
| `dockerhub-credentials` | **Username with password** | Username: `ckato9173`, Password: Docker Hub 비밀번호 또는 Access Token |

**GitHub PAT 생성 방법:**

1. GitHub → Settings → Developer settings → Personal access tokens → **Tokens (classic)**
2. **Generate new token (classic)**
3. 권한: `repo` 전체 체크
4. 생성된 토큰을 Jenkins의 `github-token` Secret text에 입력

**Docker Hub Access Token 생성 방법:**

1. https://hub.docker.com/settings/security
2. **New Access Token** → 이름 입력 → 권한: Read & Write
3. 생성된 토큰을 Jenkins의 `dockerhub-credentials` Password에 입력

### 6.3 Jenkinsfile (프로젝트에 이미 포함됨)

프로젝트 루트의 `Jenkinsfile`:

```groovy
pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'ckato9173'
        IMAGE_TAG = "${BUILD_NUMBER}"
        GITHUB_REPO = 'https://github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git'
        KUBECONFIG = 'C:\\Users\\playdata2\\.kube\\config'
    }

    triggers {
        // 5분마다 GitHub에 변경사항 확인 → 변경 있으면 자동 빌드
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                withCredentials([string(
                    credentialsId: 'github-token',
                    variable: 'GITHUB_TOKEN'
                )]) {
                    checkout scmGit(
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[
                            url: "https://${GITHUB_TOKEN}@github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git"
                        ]]
                    )
                }
            }
        }

        stage('Backend Test') {
            steps {
                bat 'gradlew.bat clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Docker Build') {
            parallel {
                stage('Backend Image') {
                    steps {
                        bat "docker build -t %DOCKER_REGISTRY%/salesboost-backend:%IMAGE_TAG% -t %DOCKER_REGISTRY%/salesboost-backend:latest ."
                    }
                }
                stage('Frontend Image') {
                    steps {
                        bat "docker build -t %DOCKER_REGISTRY%/salesboost-frontend:%IMAGE_TAG% -t %DOCKER_REGISTRY%/salesboost-frontend:latest ./frontend"
                    }
                }
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat 'echo %DOCKER_PASS%| docker login -u %DOCKER_USER% --password-stdin'
                    bat "docker push %DOCKER_REGISTRY%/salesboost-backend:%IMAGE_TAG%"
                    bat "docker push %DOCKER_REGISTRY%/salesboost-backend:latest"
                    bat "docker push %DOCKER_REGISTRY%/salesboost-frontend:%IMAGE_TAG%"
                    bat "docker push %DOCKER_REGISTRY%/salesboost-frontend:latest"
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                bat 'kubectl apply -f infra/k8s/common.yaml'
                bat 'kubectl apply -f infra/k8s/deployments/backend.yaml'
                bat 'kubectl apply -f infra/k8s/deployments/frontend.yaml'
                bat 'kubectl apply -f infra/k8s/ingress.yaml'
                bat 'kubectl rollout restart deployment salesboost-backend'
                bat 'kubectl rollout restart deployment salesboost-frontend'
            }
        }

        stage('Verify') {
            steps {
                bat 'kubectl rollout status deployment salesboost-backend --timeout=180s'
                bat 'kubectl rollout status deployment salesboost-frontend --timeout=60s'
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS - image tag: ${IMAGE_TAG}"
        }
        failure {
            echo 'Pipeline FAILED'
        }
        always {
            bat 'docker logout || exit 0'
        }
    }
}
```

### 6.4 파이프라인 흐름

```
Checkout (GitHub 토큰으로 clone)
       ↓
Backend Test (gradlew.bat clean test)
       ↓
Docker Build (병렬: backend + frontend)
  → ckato9173/salesboost-backend:{빌드번호}
  → ckato9173/salesboost-frontend:{빌드번호}
       ↓
Docker Push (Docker Hub에 업로드)
       ↓
Deploy to K8s (kubectl apply + rollout restart)
       ↓
Verify (rollout status로 배포 완료 확인)
```

### 6.5 Jenkins Pipeline Job 생성

1. `http://localhost:18080/view/all/newJob` 접속
2. 이름: `salesboost-pipeline`
3. 타입: **Pipeline** 선택 → **OK**
4. Pipeline 섹션:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git`
   - Credentials: *(없으면 비워두기 — Jenkinsfile 내에서 토큰으로 checkout 함)*
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
5. **Save** → **Build Now**

### 6.6 빌드 트리거

Jenkinsfile에 `pollSCM('H/5 * * * *')`이 설정되어 있어, **5분마다 GitHub에 변경사항을 자동 확인**합니다. main에 push가 있으면 자동 빌드가 시작됩니다.

수동 빌드: Jenkins 대시보드 → `salesboost-pipeline` → **Build Now**

---

## 7. Phase 5: ArgoCD GitOps CD

### 7.1 ArgoCD 설치

```bash
# ArgoCD namespace 생성
kubectl create namespace argocd

# ArgoCD 설치
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# 설치 확인 (2~3분 소요)
kubectl get pods -n argocd -w
# argocd-server-xxxxx             1/1   Running
# argocd-repo-server-xxxxx        1/1   Running
# argocd-application-controller-0  1/1   Running
```

### 7.2 ArgoCD 웹 UI 접속

```bash
# NodePort로 ArgoCD Server 노출 (포트 30443)
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "NodePort", "ports": [{"port": 443, "targetPort": 8080, "nodePort": 30443}]}}'
```

```bash
# 초기 admin 비밀번호 확인
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

ArgoCD 접속: `https://localhost:30443` (self-signed 인증서 경고는 무시)
- ID: `admin`
- PW: 위에서 확인한 비밀번호

### 7.3 ArgoCD에 Git Repository 등록

Settings → Repositories → **Connect Repo**:

| 항목 | 값 |
|------|---|
| Via | HTTPS |
| Repository URL | `https://github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git` |
| Username | GitHub 사용자명 |
| Password | GitHub PAT 토큰 |

### 7.4 ArgoCD Application 생성

프로젝트에 이미 `infra/k8s/argocd-app.yaml`이 포함되어 있습니다:

```bash
kubectl apply -f infra/k8s/argocd-app.yaml
```

또는 ArgoCD 웹 UI에서 수동 생성: Applications → **New App**:

| 항목 | 값 |
|------|---|
| Application Name | `salesboost` |
| Project | `default` |
| Sync Policy | `Automatic` (prune + selfHeal) |
| Repository URL | `https://github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git` |
| Revision | `main` |
| Path | `infra/k8s` |
| Cluster URL | `https://kubernetes.default.svc` |
| Namespace | `default` |

→ **Create**

### 7.5 ArgoCD 동작 흐름

```
Jenkins 파이프라인 완료 (이미지 Push + K8s Deploy)
       ↓
Git Push (Jenkinsfile이 K8s 매니페스트를 변경했다면)
       ↓
ArgoCD가 3분마다 GitHub 폴링
       ↓
"OutOfSync" 감지 → 자동 Sync
       ↓
K8s에 새 매니페스트 Apply → Pod 롤링 업데이트
```

> 현재 구조에서는 Jenkins가 직접 `kubectl apply`로 배포하기 때문에 ArgoCD는 **보조 역할** (동기화 상태 모니터링, 수동 롤백 UI)입니다. ArgoCD를 주 배포 도구로 쓰려면 Jenkinsfile의 `Deploy to K8s` 스테이지를 제거하고, Jenkins는 이미지 Push까지만 담당하면 됩니다.

### 7.6 수동 동기화 & 롤백

ArgoCD 웹 UI에서:
- **Sync**: Applications → salesboost → **Sync** 버튼
- **Rollback**: Applications → salesboost → **History** → 원하는 버전 → **Rollback**

---

## 8. Phase 6: 전체 파이프라인 통합 테스트

### 8.1 체크리스트

```
□ Step 1: Docker Desktop Kubernetes 활성화 확인
           kubectl get nodes → docker-desktop Ready

□ Step 2: NGINX Ingress Controller 설치
           kubectl get pods -n ingress-nginx → Running

□ Step 3: K8s Secret/ConfigMap 적용
           kubectl apply -f infra/k8s/common.yaml

□ Step 4: K8s 서비스 배포
           kubectl apply -f infra/k8s/deployments/backend.yaml
           kubectl apply -f infra/k8s/deployments/frontend.yaml
           kubectl apply -f infra/k8s/ingress.yaml
           kubectl get pods → 모두 Running

□ Step 5: 접속 테스트
           http://localhost → 프론트엔드
           http://localhost/api/portfolios → {"success":true,"data":[]}

□ Step 6: Jenkins Credentials 등록
           github-token (Secret text) + dockerhub-credentials (Username with password)

□ Step 7: Jenkins Pipeline Job 생성 & Build Now
           http://localhost:18080 → 파이프라인 성공 확인

□ Step 8: Docker Hub 이미지 확인
           https://hub.docker.com/u/ckato9173 → 이미지 업로드 확인

□ Step 9: (선택) ArgoCD 설치 & Application 생성
           https://localhost:30443 → Synced & Healthy
```

### 8.2 포트 사용 현황

| 서비스 | 포트 | 비고 |
|--------|------|------|
| Frontend + Ingress | 80 | NGINX Ingress Controller (LoadBalancer) |
| Backend API | 8080 | ClusterIP (Ingress `/api`로 노출) |
| 공용 DB | 221.148.116.109:10002 | 외부 MariaDB |
| Jenkins | 18080 | Windows 로컬 설치 |
| ArgoCD | 30443 | NodePort (`https://localhost:30443`) |

### 8.3 K8s 끄고 켤 때

```bash
# === 끌 때 ===
kubectl delete -f infra/k8s/ingress.yaml \
  -f infra/k8s/deployments/backend.yaml \
  -f infra/k8s/deployments/frontend.yaml \
  -f infra/k8s/common.yaml

# === 켤 때 ===
kubectl apply -f infra/k8s/common.yaml
kubectl apply -f infra/k8s/deployments/backend.yaml
kubectl apply -f infra/k8s/deployments/frontend.yaml
kubectl apply -f infra/k8s/ingress.yaml

# === 코드 수정 후 반영 ===
docker build -t ckato9173/salesboost-backend:latest .
kubectl rollout restart deployment salesboost-backend
# 또는 프론트엔드:
docker build -t ckato9173/salesboost-frontend:latest ./frontend
kubectl rollout restart deployment salesboost-frontend
```

---

## 9. 트러블슈팅 모음

### Pod가 CrashLoopBackOff 상태일 때

```bash
# 로그 확인 (가장 먼저 확인!)
kubectl logs <pod-name> --previous

# 이벤트 확인
kubectl describe pod <pod-name>

# 흔한 원인:
# - DB 연결 실패 → ConfigMap의 SPRING_DATASOURCE_URL 확인
# - JWT Secret 미설정 → app-secret 확인
# - 이미지 Pull 실패 → Docker Hub에 이미지가 있는지 확인
```

### 백엔드가 DB에 연결 못 할 때

```bash
# K8s Pod에서 공용 DB 접근 테스트
kubectl run db-test --rm -it --restart=Never --image=mariadb:10.11 \
  -- mariadb -h 221.148.116.109 -P 10002 -u root -pdevops_team2 -e "SHOW DATABASES;"

# ConfigMap의 URL 확인
kubectl get configmap app-config -o yaml
```

### Ingress가 동작하지 않을 때

```bash
# Ingress Controller 상태 확인
kubectl get pods -n ingress-nginx

# Ingress 리소스 확인
kubectl get ingress
# ADDRESS가 localhost인지 확인

# Ingress Controller 로그
kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller --tail=50
```

### 이미지 Pull 실패 (ImagePullBackOff)

```bash
# Docker Hub에 이미지가 있는지 확인
docker pull ckato9173/salesboost-backend:latest

# imagePullPolicy 확인
kubectl get deploy salesboost-backend -o yaml | grep imagePullPolicy
# imagePullPolicy: Always  ← Docker Hub 사용 시

# Docker Hub가 private이면 imagePullSecret 필요
# (public이면 불필요)
```

### Jenkins 파이프라인 실패 시

```
# Checkout 실패 → github-token Credential 확인 (ID가 정확히 'github-token'인지)
# Docker Push 실패 → dockerhub-credentials Credential 확인
# gradlew.bat 실패 → Jenkins workspace에 gradlew.bat이 있는지 확인
# kubectl 실패 → Jenkins 실행 유저의 PATH에 kubectl이 있는지 확인
```

### Windows 관련 주의사항

```bash
# Git Bash에서 gradlew 실행 시 "Permission denied"
git update-index --chmod=+x gradlew

# CRLF → LF 줄바꿈 문제 (Dockerfile, shell script)
git config core.autocrlf input

# Docker build 느림
# Docker Desktop → Settings → Resources → Memory: 최소 4GB 이상 권장
```

---

## 부록: 환경별 설정값 요약

| 항목 | 로컬 개발 (.env 없이) | Docker Compose (.env) | Kubernetes (Secret/ConfigMap) |
|------|---|---|---|
| DB URL | `jdbc:mariadb://127.0.0.1:3306/salesboost...` | `jdbc:mariadb://mariadb:3306/salesboost...` | `jdbc:mariadb://221.148.116.109:10002/salesboost...` |
| DB User | `root` | `.env`의 `DB_USERNAME` | `app-config` ConfigMap의 `DB_USERNAME` |
| DB Password | `mariadb` | `.env`의 `DB_PASSWORD` | `db-secret` Secret의 `password` |
| JWT Secret | application.yml 기본값 | `.env`의 `APP_JWT_SECRET` | `app-secret` Secret의 `jwt-secret` |
| CORS Origins | `localhost:5173,localhost:3000` | `.env`의 `APP_CORS_ALLOWED_ORIGINS` | `app-config` ConfigMap |
| 이미지 | - | - | `ckato9173/salesboost-*:latest` (`imagePullPolicy: Always`) |

---

## 부록: 자주 쓰는 명령어 모음

```bash
# === Docker Compose ===
docker compose up --build -d       # 전체 빌드 & 시작
docker compose down                # 전체 중지 & 삭제
docker compose logs -f backend     # 백엔드 로그 실시간

# === Kubernetes ===
kubectl get all                    # 전체 리소스 확인
kubectl get pods -w                # Pod 상태 실시간 감시
kubectl logs -f deploy/salesboost-backend    # 백엔드 로그 실시간
kubectl exec -it deploy/salesboost-backend -- sh  # 백엔드 쉘 접속
kubectl rollout restart deploy/salesboost-backend  # 백엔드 재시작
kubectl rollout status deploy/salesboost-backend   # 롤아웃 상태 확인

# === Docker Hub ===
docker push ckato9173/salesboost-backend:latest    # 수동 Push
docker pull ckato9173/salesboost-backend:latest    # 수동 Pull
```
