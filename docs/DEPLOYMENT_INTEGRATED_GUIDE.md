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

### 6.1 Jenkins 설치 (Docker Desktop Kubernetes)

```bash
# Jenkins namespace 생성
kubectl create namespace jenkins

# Jenkins를 Helm으로 설치 (가장 간편)
# Helm이 없다면: https://helm.sh/docs/intro/install/
helm repo add jenkins https://charts.jenkins.io
helm repo update

helm install jenkins jenkins/jenkins \
  --namespace jenkins \
  --set controller.serviceType=LoadBalancer \
  --set controller.servicePort=9090

# 설치 확인 (2~3분 소요)
kubectl get pods -n jenkins -w
# jenkins-0   2/2   Running   ...

# Jenkins 초기 관리자 비밀번호 확인
kubectl exec -n jenkins jenkins-0 -- cat /run/secrets/additional/chart-admin-password
# 또는
kubectl get secret -n jenkins jenkins -o jsonpath="{.data.jenkins-admin-password}" | base64 -d
```

Jenkins 접속: `http://localhost:9090`
- ID: `admin`
- PW: 위에서 확인한 비밀번호

### 6.2 Jenkins 플러그인 설치

Jenkins 관리 → Plugins → Available plugins에서 설치:

- **Pipeline** (기본 포함)
- **Git** (기본 포함)
- **Docker Pipeline**
- **Kubernetes** (K8s 에이전트 사용 시)
- **Credentials Binding**

### 6.3 Jenkins Credentials 등록

Jenkins 관리 → Credentials → System → Global credentials:

| ID | 종류 | 용도 |
|----|------|------|
| `github-credentials` | Username with password (또는 SSH key) | GitHub 레포 접근 |
| `dockerhub-credentials` | Username with password | Docker Hub 이미지 Push |

> Docker Hub 대신 로컬 레지스트리를 쓰면 `dockerhub-credentials`는 불필요합니다 (5.2절 로컬 이미지 빌드 참고).

### 6.4 Jenkinsfile 작성

프로젝트 루트에 `Jenkinsfile`을 생성합니다:

```groovy
pipeline {
    agent any

    environment {
        // Docker Hub 사용 시 (예: your-dockerhub-id/salesboost-backend)
        DOCKER_REGISTRY = 'your-dockerhub-id'
        IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Build & Test') {
            steps {
                sh './gradlew clean build'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build') {
            parallel {
                stage('Backend Image') {
                    steps {
                        sh "docker build -t ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG} ."
                        sh "docker tag ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG} ${DOCKER_REGISTRY}/salesboost-backend:latest"
                    }
                }
                stage('Frontend Image') {
                    steps {
                        sh "docker build -t ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG} ./frontend"
                        sh "docker tag ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG} ${DOCKER_REGISTRY}/salesboost-frontend:latest"
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
                    sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"
                    sh "docker push ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG}"
                    sh "docker push ${DOCKER_REGISTRY}/salesboost-backend:latest"
                    sh "docker push ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG}"
                    sh "docker push ${DOCKER_REGISTRY}/salesboost-frontend:latest"
                }
            }
        }

        stage('Update K8s Manifests') {
            steps {
                // GitOps: K8s 매니페스트의 이미지 태그를 업데이트
                sh """
                    sed -i 's|image: .*salesboost-backend:.*|image: ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG}|' infra/k8s/deployments/backend.yaml
                    sed -i 's|image: .*salesboost-frontend:.*|image: ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG}|' infra/k8s/deployments/frontend.yaml
                    sed -i 's|imagePullPolicy: Never|imagePullPolicy: Always|' infra/k8s/deployments/backend.yaml
                    sed -i 's|imagePullPolicy: Never|imagePullPolicy: Always|' infra/k8s/deployments/frontend.yaml
                """

                withCredentials([usernamePassword(
                    credentialsId: 'github-credentials',
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_TOKEN'
                )]) {
                    sh """
                        git config user.email "jenkins@salesboost.com"
                        git config user.name "Jenkins CI"
                        git add infra/k8s/deployments/backend.yaml infra/k8s/deployments/frontend.yaml
                        git commit -m "ci: update image tags to ${IMAGE_TAG}"
                        git push https://${GIT_USER}:${GIT_TOKEN}@github.com/your-org/your-repo.git HEAD:main
                    """
                }
            }
        }
    }

    post {
        success {
            echo "파이프라인 성공! 이미지 태그: ${IMAGE_TAG}"
        }
        failure {
            echo "파이프라인 실패. 로그를 확인하세요."
        }
        always {
            sh 'docker logout || true'
        }
    }
}
```

### 6.5 Jenkins Pipeline 생성

1. Jenkins 대시보드 → **New Item**
2. 이름: `salesboost-pipeline`
3. 타입: **Pipeline** 선택
4. Pipeline 섹션:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/your-org/your-repo.git`
   - Credentials: `github-credentials`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
5. **Save**

### 6.6 GitHub Webhook 설정 (자동 빌드 트리거)

> Docker Desktop은 외부에서 접근 불가하므로, 로컬 환경에서는 수동 빌드 또는 Poll SCM을 사용합니다.

**로컬 환경: Poll SCM 방식**

Jenkins Pipeline 설정 → Build Triggers → **Poll SCM**:
```
H/5 * * * *
```
→ 5분마다 GitHub에 변경사항이 있는지 확인 후 자동 빌드

**외부 서버 환경: Webhook 방식**

GitHub → Settings → Webhooks → Add webhook:
- Payload URL: `http://your-jenkins-url:9090/github-webhook/`
- Content type: `application/json`
- Events: `Just the push event`

### 6.7 로컬 전용 간소화 파이프라인 (Docker Hub 없이)

Docker Hub를 사용하지 않고 로컬 K8s에 바로 배포하는 간소화 버전:

```groovy
// Jenkinsfile.local — 로컬 전용 간소화 버전
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Test') {
            steps {
                sh './gradlew clean test'
            }
        }

        stage('Docker Build (Local)') {
            parallel {
                stage('Backend Image') {
                    steps {
                        sh 'docker build -t salesboost-backend:latest .'
                    }
                }
                stage('Frontend Image') {
                    steps {
                        sh 'docker build -t salesboost-frontend:latest ./frontend'
                    }
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                sh '''
                    kubectl apply -f infra/k8s/common.yaml
                    kubectl apply -f infra/k8s/deployments/db.yaml
                    kubectl apply -f infra/k8s/deployments/backend.yaml
                    kubectl apply -f infra/k8s/deployments/frontend.yaml
                    kubectl apply -f infra/k8s/ingress.yaml
                    kubectl rollout restart deployment salesboost-backend
                    kubectl rollout restart deployment salesboost-frontend
                '''
            }
        }
    }
}
```

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
# ...

# ArgoCD CLI 설치 (선택사항, 웹 UI로도 가능)
# Windows: https://github.com/argoproj/argo-cd/releases 에서 다운로드
# 또는 Chocolatey: choco install argocd-cli
```

### 7.2 ArgoCD 웹 UI 접속

```bash
# 방법 1: LoadBalancer로 변경 (Docker Desktop 권장)
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "LoadBalancer"}}'

# 포트 확인
kubectl get svc argocd-server -n argocd
# argocd-server   LoadBalancer   10.x.x.x   localhost   80:3xxxx/TCP,443:3xxxx/TCP

# 방법 2: 포트포워딩 (LoadBalancer 충돌 시)
kubectl port-forward svc/argocd-server -n argocd 8443:443
# https://localhost:8443 으로 접속
```

```bash
# 초기 admin 비밀번호 확인
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

ArgoCD 접속: `https://localhost` (또는 `https://localhost:8443`)
- ID: `admin`
- PW: 위에서 확인한 비밀번호

> 접속 후 Settings → Account → admin → Update Password에서 비밀번호를 변경하세요.

### 7.3 ArgoCD에 Git Repository 등록

**웹 UI 방식:**

1. Settings → Repositories → **Connect Repo**
2. Via: **HTTPS**
3. Repository URL: `https://github.com/your-org/your-repo.git`
4. Username: GitHub 사용자명
5. Password: GitHub Personal Access Token (PAT)
6. **Connect**

**CLI 방식:**

```bash
argocd repo add https://github.com/your-org/your-repo.git \
  --username your-github-id \
  --password your-github-pat
```

### 7.4 ArgoCD Application 생성

**웹 UI 방식:**

1. Applications → **New App**
2. 설정:
   - Application Name: `salesboost`
   - Project: `default`
   - Sync Policy: `Automatic` (자동 배포) 또는 `Manual` (수동 승인)
   - Repository URL: `https://github.com/your-org/your-repo.git`
   - Revision: `main`
   - Path: `infra/k8s`  (매니페스트 경로)
   - Cluster URL: `https://kubernetes.default.svc`
   - Namespace: `default`
3. **Create**

**YAML 파일 방식:**

```yaml
# infra/argocd/application.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: salesboost
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/your-org/your-repo.git
    targetRevision: main
    path: infra/k8s
    # 특정 파일만 배포하려면 directory 설정 사용
    directory:
      recurse: true
  destination:
    server: https://kubernetes.default.svc
    namespace: default
  syncPolicy:
    automated:
      prune: true       # Git에서 삭제된 리소스 자동 제거
      selfHeal: true     # 클러스터에서 수동 변경 시 원상복구
    syncOptions:
      - CreateNamespace=true
```

```bash
kubectl apply -f infra/argocd/application.yaml
```

### 7.5 ArgoCD 배포 흐름 확인

```
                    Git Push (이미지 태그 변경)
                         │
                         ▼
                   ┌───────────┐
                   │  GitHub   │
                   │  (main)   │
                   └─────┬─────┘
                         │ ArgoCD가 3분마다 폴링
                         ▼
                   ┌───────────┐
                   │  ArgoCD   │  "OutOfSync" 감지
                   │  Server   │
                   └─────┬─────┘
                         │ syncPolicy.automated
                         ▼
                   ┌───────────┐
                   │ K8s Apply │  새 이미지로 Pod 교체
                   │ (Rolling  │
                   │  Update)  │
                   └───────────┘
```

ArgoCD 웹 UI에서 확인할 수 있는 것들:

- **Sync Status**: `Synced` (정상) / `OutOfSync` (변경 감지)
- **Health Status**: `Healthy` / `Degraded` / `Progressing`
- **리소스 트리**: Pod, Service, Deployment 등 시각적으로 표시
- **Diff**: Git과 클러스터 간 차이점

### 7.6 수동 동기화 & 롤백

```bash
# CLI로 수동 동기화
argocd app sync salesboost

# 이전 버전으로 롤백
argocd app rollback salesboost

# 특정 Git 커밋으로 롤백
argocd app sync salesboost --revision <commit-hash>

# 또는 웹 UI에서:
# Applications → salesboost → History → 원하는 버전 → Rollback
```

---

## 8. Phase 6: 전체 파이프라인 통합 테스트

### 8.1 체크리스트

아래 순서대로 전체 파이프라인을 검증합니다:

```
□ Step 1: Docker Desktop Kubernetes 활성화 확인
           kubectl get nodes → Ready

□ Step 2: NGINX Ingress Controller 설치
           kubectl get pods -n ingress-nginx → Running

□ Step 3: JWT 시크릿 생성
           openssl rand -base64 48 → .env에 저장

□ Step 4: K8s Secret/ConfigMap 적용
           kubectl apply -f infra/k8s/common.yaml
           kubectl get secret,configmap

□ Step 5: Docker 이미지 빌드
           docker build -t salesboost-backend:latest .
           docker build -t salesboost-frontend:latest ./frontend

□ Step 6: K8s 서비스 배포
           kubectl apply -f infra/k8s/deployments/
           kubectl apply -f infra/k8s/ingress.yaml
           kubectl get pods → 모두 Running

□ Step 7: 접속 테스트
           curl http://localhost → 프론트엔드 HTML
           curl http://localhost/api/portfolios → JSON 응답

□ Step 8: Jenkins 설치 & 파이프라인 생성
           http://localhost:9090 → Pipeline 수동 실행

□ Step 9: ArgoCD 설치 & Application 생성
           https://localhost:8443 → Synced & Healthy

□ Step 10: E2E 테스트
            코드 수정 → git push → Jenkins 빌드 →
            이미지 태그 업데이트 → ArgoCD 자동 동기화 →
            새 Pod 배포 확인
```

### 8.2 포트 사용 현황

| 서비스 | 포트 | 비고 |
|--------|------|------|
| Frontend (K8s) | 80 | LoadBalancer 또는 Ingress |
| Backend API | 8080 | ClusterIP (Ingress `/api` 로 노출) |
| MariaDB | 3306 | ClusterIP (내부 전용) |
| Jenkins | 9090 | LoadBalancer |
| ArgoCD | 443 (→8443) | LoadBalancer 또는 포트포워딩 |

> 포트 충돌 주의: Frontend(80)와 Ingress Controller(80)가 겹칠 수 있습니다. Ingress를 쓸 경우 frontend Service 타입을 `ClusterIP`로 변경하는 것을 권장합니다.

### 8.3 포트 충돌 해결

만약 80번 포트가 이미 사용 중이라면:

```bash
# 어떤 프로세스가 80번 포트를 사용하는지 확인 (PowerShell)
netstat -ano | findstr :80

# Docker Compose가 80 포트를 사용 중이면 먼저 중지
docker compose down
```

frontend Service를 ClusterIP로 변경하고 Ingress만 사용:

```yaml
# infra/k8s/deployments/frontend.yaml 수정
spec:
  type: ClusterIP    # LoadBalancer → ClusterIP
```

---

## 9. 트러블슈팅 모음

### Pod가 CrashLoopBackOff 상태일 때

```bash
# 로그 확인
kubectl logs <pod-name> --previous

# 이벤트 확인
kubectl describe pod <pod-name>

# 흔한 원인:
# - DB 연결 실패 → Secret/ConfigMap의 DB 정보 확인
# - JWT Secret 미설정 → app-secret 확인
# - 이미지가 없음 → docker images 에서 빌드 확인
```

### 백엔드가 DB에 연결 못 할 때

```bash
# DB Pod 상태 확인
kubectl get pods -l app=salesboost-db
kubectl logs -l app=salesboost-db

# 백엔드에서 DB 접근 가능한지 확인
kubectl exec deploy/salesboost-backend -- sh -c \
  'curl -sf salesboost-db:3306 || echo "DB 접속 불가"'

# ConfigMap의 URL 확인
kubectl get configmap app-config -o yaml
# SPRING_DATASOURCE_URL 값이 jdbc:mariadb://salesboost-db:3306/... 인지 확인
```

### Ingress가 동작하지 않을 때

```bash
# Ingress Controller 확인
kubectl get pods -n ingress-nginx

# Ingress 상태 확인
kubectl describe ingress salesboost-ingress

# ADDRESS가 비어있으면 Ingress Controller가 아직 준비 안 된 것
kubectl get ingress
# NAME                CLASS   HOSTS       ADDRESS     PORTS
# salesboost-ingress  nginx   localhost   localhost   80

# Ingress Controller 로그 확인
kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller --tail=50
```

### 이미지 Pull 실패 (ImagePullBackOff)

```bash
# 로컬 이미지를 K8s에서 못 찾는 경우
# 원인: imagePullPolicy가 Always로 되어있으면 레지스트리에서 받으려 함

# 해결: imagePullPolicy를 Never로 확인
kubectl get deploy salesboost-backend -o yaml | grep imagePullPolicy
# imagePullPolicy: Never  ← 이어야 함 (로컬 빌드 사용 시)

# 이미지 존재 확인
docker images | grep salesboost
```

### Jenkins에서 Docker 명령 실행 불가

```bash
# Jenkins Pod에서 Docker 소켓 접근이 필요
# Docker Desktop K8s에서는 Docker-in-Docker 또는 Docker 소켓 마운트 필요

# Helm 설치 시 옵션 추가:
helm upgrade jenkins jenkins/jenkins \
  --namespace jenkins \
  --set persistence.enabled=true \
  --set controller.serviceType=LoadBalancer \
  --set controller.servicePort=9090 \
  --set agent.volumes[0].type=HostPath \
  --set agent.volumes[0].hostPath=/var/run/docker.sock \
  --set agent.volumes[0].mountPath=/var/run/docker.sock
```

### ArgoCD Application이 OutOfSync인데 Sync 안 될 때

```bash
# Sync 에러 확인
argocd app get salesboost

# 강제 동기화
argocd app sync salesboost --force

# 프루닝 포함 동기화
argocd app sync salesboost --prune

# 웹 UI에서: App Details → Sync → Replace (체크) → Synchronize
```

### Windows 관련 주의사항

```bash
# Git Bash에서 gradlew 실행 시
# "Permission denied" → 실행 권한 부여
git update-index --chmod=+x gradlew

# 줄바꿈 문제 (CRLF → LF)
# Dockerfile, shell script가 실행 안 될 때
git config core.autocrlf input

# Docker build 시 Gradle 빌드 느림
# Docker Desktop → Settings → Resources → Memory: 최소 4GB 이상 권장
```

---

## 부록: 환경별 설정값 요약

| 항목 | 로컬 개발 (.env) | Docker Compose (.env) | Kubernetes (Secret/ConfigMap) |
|------|-------------------|----------------------|-------------------------------|
| DB URL | `jdbc:mariadb://127.0.0.1:3306/salesboost...` | `jdbc:mariadb://mariadb:3306/salesboost...` | `jdbc:mariadb://salesboost-db:3306/salesboost...` (내부 DB) 또는 `jdbc:mariadb://db.example.com:3306/salesboost...` (외부 DB) |
| DB User | `root` (기본값) | `salesboost` (.env) | `salesboost` (app-config ConfigMap) |
| DB Password | `mariadb` (기본값) | `salesboost` (.env) | db-secret Secret |
| JWT Secret | application.yml 기본값 | `APP_JWT_SECRET` (.env) | app-secret Secret |
| CORS Origins | `localhost:5173,localhost:3000` | `localhost,localhost:5173,localhost:3000` | `localhost,localhost:80` |
| 이미지 정책 | - | - | `imagePullPolicy: Never` (로컬) / `Always` (레지스트리) |

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

# === Jenkins ===
kubectl get pods -n jenkins        # Jenkins Pod 상태
kubectl logs -n jenkins jenkins-0  # Jenkins 로그

# === ArgoCD ===
kubectl get pods -n argocd         # ArgoCD Pod 상태
argocd app list                    # 등록된 Application 목록
argocd app get salesboost          # 상세 상태 확인
argocd app sync salesboost         # 수동 동기화
argocd app history salesboost      # 배포 히스토리
```
