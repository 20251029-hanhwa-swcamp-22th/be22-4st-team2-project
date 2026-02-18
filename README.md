# SalesBoost

B2B 수출 업무를 지원하는 웹 서비스 프로젝트입니다.  
Public 화면(서비스 소개/포트폴리오/제휴문의)과 Admin 화면(로그인/문의관리/포트폴리오관리)으로 구성됩니다.

## 1. 프로젝트 목표
- Public: 서비스 가치 전달, 포트폴리오 노출, 제휴 문의 유입
- Admin: 문의 처리 효율화, 포트폴리오 운영(등록/수정/삭제/노출관리)

## 2. 기술 스택
- Backend: Java 17, Spring Boot 3.5.10, Spring Security, JPA, MyBatis
- Frontend: Vue.js (예정)
- DB: MariaDB
- Docs: Swagger/OpenAPI
- Infra: Docker, Kubernetes

## 3. 디렉터리 구조
```text
salesboost/
├─ README.md
├─ .gitignore
├─ .editorconfig
├─ docs/
│  ├─ requirements.md
│  ├─ erd/
│  └─ api/
├─ scripts/
│  ├─ dev.sh
│  ├─ build.sh
│  └─ deploy.sh
├─ frontend/
│  ├─ package.json
│  ├─ .env.development
│  ├─ .env.production
│  └─ src/
│     ├─ api/
│     ├─ pages/
│     │  ├─ public/
│     │  └─ admin/
│     ├─ components/
│     │  ├─ common/
│     │  ├─ portfolio/
│     │  └─ inquiry/
│     ├─ router/
│     ├─ stores/
│     ├─ composables/
│     └─ utils/
├─ src/
│  └─ main/
│     ├─ java/com/salesboost/
│     └─ resources/
│        ├─ application.yml
│        └─ mappers/
├─ infra/
│  ├─ docker/
│  │  ├─ docker-compose.yml
│  │  ├─ nginx/default.conf
│  │  └─ mariadb/init.sql
│  └─ k8s/
│     ├─ deployments/
│     ├─ services/
│     └─ ingress/
├─ build.gradle
├─ settings.gradle
├─ gradlew
└─ gradle/
```

## 4. 요구사항 범위
### 4.1 Public (FR-01~FR-10)
- 랜딩 페이지
- 서비스 소개/프로세스 흐름
- 포트폴리오 목록/상세
- 제휴문의 등록/완료 안내
- 반응형, 헤더/푸터 공통 UI

### 4.2 Admin (FR-11~FR-20)
- 관리자 로그인(JWT)
- 제휴문의 목록/상세/상태변경/메모
- 포트폴리오 목록/등록/수정/삭제/노출관리

### 4.3 Non-Functional (NF-01~NF-09)
- 성능(초기 로딩 3초 이내 목표)
- 보안(BCrypt, CORS, XSS 대응)
- 배포(Docker/K8s)
- 유지보수(코드 컨벤션, API 문서화)
- 브라우저 호환성

## 5. API 요약
### 5.1 Public API
- `GET /api/portfolios`
- `GET /api/portfolios/{id}`
- `POST /api/inquiries`

### 5.2 Admin API
- `POST /api/admin/login`
- `GET /api/admin/inquiries`
- `GET /api/admin/inquiries/{id}`
- `PATCH /api/admin/inquiries/{id}/status`
- `PATCH /api/admin/inquiries/{id}/memo`
- `GET /api/admin/portfolios`
- `POST /api/admin/portfolios`
- `PUT /api/admin/portfolios/{id}`
- `DELETE /api/admin/portfolios/{id}`
- `PATCH /api/admin/portfolios/{id}/visibility`

## 6. 로컬 실행
### 6.1 Backend
```bash
./gradlew bootRun
```

### 6.2 Test
```bash
./gradlew test
```

### 6.3 Build
```bash
./gradlew clean build
```

## 7. 환경 변수/설정
- 기본 설정 파일: `src/main/resources/application.yml`
- 민감 정보(DB 계정, JWT secret)는 환경 변수 또는 별도 설정 파일로 관리

## 8. 현재 상태
- ✅ 백엔드 API 구현 완료 (2025-02-13)
  - 제휴문의 등록/조회/관리 API
  - 포트폴리오 CRUD API
  - 관리자 인증 (JWT)
  - Spring Security 설정
  - Swagger API 문서화
- 📝 상세 구현 문서
  - `docs/IMPLEMENTATION_SUMMARY.md` - 구현 요약
  - `docs/API_IMPLEMENTATION.md` - API 상세 문서
- 🔄 프론트엔드 구현 예정
- 🔄 Docker/K8s 배포 설정 예정

## 9. 구현 완료된 기능

### 백엔드 API
- ✅ 제휴문의 등록 (FR-06)
- ✅ 포트폴리오 목록/상세 조회 (FR-04, FR-05)
- ✅ 관리자 로그인 (JWT)
- ✅ 제휴문의 관리 (목록/상세/상태변경/메모)
- ✅ 포트폴리오 관리 (CRUD/공개설정/순서관리)
- ✅ MyBatis 동적 쿼리 (검색/필터링/페이징)
- ✅ 유효성 검증
- ✅ 예외 처리
- ✅ CORS 설정

## 10. 빌드 성공 확인
```bash
$ ./gradlew clean build
BUILD SUCCESSFUL in 2s
```
