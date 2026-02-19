# SalesBoost 백엔드 API 구현 완료 문서

## 📋 구현 개요
프로젝트 기획서와 요구사항 명세서(FR-01 ~ FR-10)를 기반으로 백엔드 API를 구현했습니다.

---

## ✅ 구현 완료 내용

### 1. 제휴문의(Inquiry) API

#### 1.1 공개(Public) API
| Method | Endpoint | 설명 | 요구사항 |
|--------|----------|------|----------|
| POST | `/api/inquiries` | 제휴 문의 등록 | FR-06 |

**Request Body (InquiryCreateRequest):**
```json
{
  "companyName": "기업명 (필수)",
  "contactName": "담당자명 (필수)",
  "email": "이메일 (필수, 형식 검증)",
  "phone": "전화번호 (필수, 형식: 010-1234-5678)",
  "inquiryType": "문의 유형 (필수, enum)",
  "content": "문의 내용 (필수)"
}
```

**Response:**
```json
{
  "success": true,
  "data": 1,
  "message": "제휴 문의가 정상적으로 등록되었습니다."
}
```

#### 1.2 관리자(Admin) API
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/admin/inquiries` | 제휴문의 목록 조회 (검색, 정렬, 페이징) | 필요 |
| GET | `/api/admin/inquiries/{id}` | 제휴문의 상세 조회 | 필요 |
| PATCH | `/api/admin/inquiries/{id}/status` | 문의 상태 변경 | 필요 |
| PATCH | `/api/admin/inquiries/{id}/memo` | 관리자 메모 변경 | 필요 |

**목록 조회 Query Parameters:**
- `status` (선택): 문의 상태 필터 (PENDING, IN_PROGRESS, COMPLETED)
- `keyword` (선택): 기업명, 담당자명, 이메일, 내용 검색
- `sort` (기본: latest): 정렬 방식 (latest, oldest)
- `page` (기본: 1): 페이지 번호
- `size` (기본: 10): 페이지 크기

---

### 2. 포트폴리오(Portfolio) API

#### 2.1 공개(Public) API
| Method | Endpoint | 설명 | 요구사항 |
|--------|----------|------|----------|
| GET | `/api/portfolios` | 공개 포트폴리오 목록 조회 | FR-04 |
| GET | `/api/portfolios/{id}` | 포트폴리오 상세 조회 | FR-05 |

**목록 Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "제목",
      "description": "설명",
      "clientName": "고객사명",
      "industry": "산업군",
      "thumbnailUrl": "썸네일 이미지 URL",
      "visible": true,
      "displayOrder": 1,
      "images": ["이미지1 URL", "이미지2 URL"],
      "createdAt": "2025-02-13T10:00:00"
    }
  ],
  "message": null
}
```

#### 2.2 관리자(Admin) API
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/admin/portfolios` | 전체 포트폴리오 목록 조회 | 필요 |
| POST | `/api/admin/portfolios` | 포트폴리오 등록 | 필요 |
| PUT | `/api/admin/portfolios/{id}` | 포트폴리오 수정 | 필요 |
| DELETE | `/api/admin/portfolios/{id}` | 포트폴리오 삭제 | 필요 |
| PATCH | `/api/admin/portfolios/{id}/visibility` | 공개/비공개 변경 | 필요 |
| PATCH | `/api/admin/portfolios/order` | 표시 순서 변경 | 필요 |

---

### 3. 관리자 인증(Admin Auth) API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/admin/login` | 관리자 로그인 (JWT 발급) |

**Request:**
```json
{
  "username": "관리자 계정",
  "password": "비밀번호"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "JWT 토큰",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "message": null
}
```

---

## 🏗️ 기술 구현 상세

### 1. 엔티티 설계

#### Inquiry (제휴문의)
```java
- id: Long (PK, Auto Increment)
- companyName: String (기업명)
- contactName: String (담당자명)
- email: String (이메일)
- phone: String (전화번호)
- inquiryType: InquiryType (문의 유형 enum)
- content: String (문의 내용)
- status: InquiryStatus (처리 상태 enum)
- adminMemo: String (관리자 메모)
- createdAt: LocalDateTime (생성일시)
- updatedAt: LocalDateTime (수정일시)
```

#### Portfolio (포트폴리오)
```java
- id: Long (PK, Auto Increment)
- title: String (제목)
- description: String (설명)
- clientName: String (고객사명)
- industry: String (산업군)
- thumbnailUrl: String (썸네일 이미지 URL)
- visible: boolean (공개 여부)
- displayOrder: int (표시 순서)
- images: List<PortfolioImage> (이미지 목록)
- createdAt: LocalDateTime (생성일시)
- updatedAt: LocalDateTime (수정일시)
```

#### PortfolioImage (포트폴리오 이미지)
```java
- id: Long (PK, Auto Increment)
- portfolio: Portfolio (FK)
- imageUrl: String (이미지 URL)
- displayOrder: int (표시 순서)
```

---

### 2. 주요 기술 스택

| 구분 | 기술 | 용도 |
|------|------|------|
| Framework | Spring Boot 3.5.10 | 백엔드 프레임워크 |
| ORM | JPA (Hibernate) | 엔티티 관리 |
| SQL Mapper | MyBatis 3.0.4 | 복잡한 쿼리 처리 |
| Database | MariaDB | 관계형 데이터베이스 |
| Security | Spring Security | 인증/인가 |
| JWT | JJWT 0.12.6 | 토큰 기반 인증 |
| Validation | Jakarta Validation | 요청 데이터 검증 |
| API 문서 | Springdoc OpenAPI | Swagger UI |

---

### 3. 주요 구현 패턴

#### 3.1 계층 구조 (Layered Architecture)
```
Controller → Service → Repository → Database
           ↘ QueryService (MyBatis) ↗
```

#### 3.2 예외 처리
- `GlobalExceptionHandler`: 전역 예외 처리
- `BusinessException`: 비즈니스 로직 예외
- `ErrorCode`: 에러 코드 enum 관리

#### 3.3 공통 응답 형식
```java
ApiResponse<T> {
  success: boolean
  data: T
  message: String
}
```

---

## 🔐 보안 구현

### JWT 인증
- 관리자 API는 JWT 토큰 인증 필요
- `Authorization: Bearer {token}` 헤더 사용
- 토큰 만료 시간: 1시간 (3600초)

### Spring Security 설정
- 공개 API: 인증 불필요
  - `/api/inquiries` (POST)
  - `/api/portfolios/**` (GET)
- 관리자 API: JWT 인증 필요
  - `/api/admin/**`

---

## 📂 프로젝트 구조

```
src/main/java/com/salesboost/
├── common/
│   ├── exception/          # 예외 처리
│   └── response/           # 공통 응답
├── config/                 # 설정
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── security/               # 보안
│   ├── jwt/               # JWT 처리
│   └── auth/              # 인증 처리
└── domain/
    ├── admin/             # 관리자
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   └── entity/
    ├── inquiry/           # 제휴문의
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── mapper/        # MyBatis
    │   ├── entity/
    │   └── dto/
    └── portfolio/         # 포트폴리오
        ├── controller/
        ├── service/
        ├── repository/
        ├── entity/
        └── dto/
```

---

## 🧪 테스트

### 빌드
```bash
./gradlew clean build
```

### 실행
```bash
./gradlew bootRun
```

### API 테스트
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

---

## 📌 구현 완료 체크리스트

### 제휴문의 (Inquiry)
- [x] 제휴문의 등록 API (FR-06)
- [x] 제휴문의 목록 조회 (검색, 정렬, 페이징)
- [x] 제휴문의 상세 조회
- [x] 제휴문의 상태 변경
- [x] 관리자 메모 작성/수정
- [x] MyBatis 동적 쿼리 (검색, 필터링)
- [x] 유효성 검증 (이메일, 전화번호)

### 포트폴리오 (Portfolio)
- [x] 포트폴리오 목록 조회 (공개만) (FR-04)
- [x] 포트폴리오 상세 조회 (FR-05)
- [x] 포트폴리오 CRUD (관리자)
- [x] 이미지 업로드 처리
- [x] 공개/비공개 설정
- [x] 표시 순서 관리

### 관리자 (Admin)
- [x] 관리자 로그인 (JWT)
- [x] Spring Security 설정
- [x] JWT 인증 필터

### 공통
- [x] 전역 예외 처리
- [x] 공통 응답 형식
- [x] Swagger API 문서화
- [x] CORS 설정

---

## 🚀 다음 단계 (선택 사항)

1. **프론트엔드 연동 테스트**
   - Vue.js에서 API 호출 테스트
   - CORS 설정 확인

2. **배포 준비**
   - Docker 이미지 빌드
   - Kubernetes 배포 설정
   - CI/CD 파이프라인 구성

3. **추가 기능**
   - 이메일 알림 (제휴문의 접수 시)
   - 파일 업로드 S3 연동
   - Redis 캐싱

---

## 📞 문의

구현 관련 문의사항이 있으시면 팀원에게 연락해주세요.

- 백엔드: 강성훈, 박찬진
- 프론트엔드 & 인프라: 정진호
