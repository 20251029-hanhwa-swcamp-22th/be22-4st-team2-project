## 📌 관련 이슈
> 이 PR이 해결하는 이슈를 연결해주세요.

closes #

---

## 📝 작업 내용
> 이 PR에서 변경된 내용을 간략히 설명해주세요.

Mac 환경(Apple Silicon M1/M2/M3 포함)에서 `docker compose up --build` 후 `http://localhost` 접속 시 404가 발생하는 문제를 수정하고, `.env` 파일을 통한 환경변수 관리 체계를 도입합니다.

**원인 분석**

| # | 원인 | 증상 |
|---|------|------|
| 1 | **Apple Silicon(ARM) 아키텍처 불일치** | `node:20-alpine`, `amazoncorretto:21` 이미지가 ARM 환경에서 native 모듈 크래시 → `npm run build` 또는 Gradle 빌드 조용히 실패 |
| 2 | **빌드 실패 무시(Silent failure)** | Multi-stage Dockerfile에서 빌드 실패해도 nginx 이미지는 생성됨 → `/usr/share/nginx/html` 에 `index.html` 없음 → nginx 404 반환 |
| 3 | **MariaDB 준비 전 backend 시작** | `depends_on`이 컨테이너 시작 순서만 보장, MariaDB 준비 완료는 미보장 → backend crash-loop |
| 4 | **환경변수 하드코딩** | `docker-compose.yml`에 DB 비밀번호 등 민감 값이 직접 노출됨 |

**수정 내용**

- `.env.example` (신규)
  - 팀원 온보딩용 환경변수 템플릿 파일 추가
  - 모든 변수에 한국어 주석으로 설명 포함

- `.gitignore`
  - `.env` 파일 Git 추적 제외 패턴 추가 (`.env`, `.env.local`, `.env.*.local`)

- `docker-compose.yml`
  - `backend` / `frontend` 서비스에 `platforms: linux/amd64` 추가 → Apple Silicon 대응
  - 모든 환경변수를 `${VAR:-default}` 문법으로 `.env` 파일 참조하도록 변경
  - `frontend` 서비스에 `healthcheck` 추가 → `index.html` 서빙 여부 확인
  - `mariadb` healthcheck + `backend` `depends_on: service_healthy` 유지

- `frontend/Dockerfile`
  - `FROM --platform=linux/amd64 node:20-alpine` → Apple Silicon 대응
  - 빌드 후 `dist/index.html` 존재 검증 → 빌드 실패 시 즉시 오류 출력 후 중단

- `Dockerfile` (backend)
  - `FROM --platform=linux/amd64 amazoncorretto:21` → Apple Silicon 대응

- `README.md`
  - 섹션 8.2: `.env` 파일 생성 방법 단계별 안내 추가
  - 섹션 9: 환경변수 목록 및 설정 파일 우선순위 설명 추가

---

## 🔍 변경 사항 유형

- [ ] ✨ 새로운 기능 (feature)
- [x] 🐛 버그 수정 (bug fix)
- [ ] ♻️ 리팩토링 (refactor)
- [x] 🔧 인프라 / 환경 설정 (infra)
- [x] 📝 문서 수정 (docs)
- [ ] 🎨 UI / 스타일 변경 (style)
- [ ] ✅ 테스트 추가 / 수정 (test)

---

## ✅ 셀프 체크리스트
> 머지 전 본인이 직접 확인해주세요.

- [x] 로컬에서 정상 동작 확인
- [x] 기존 기능이 깨지지 않음 확인
- [x] 불필요한 console.log / 디버그 코드 제거
- [x] 환경변수 / 민감 정보가 코드에 하드코딩되지 않음
- [x] 커밋 메시지가 컨벤션에 맞게 작성됨

---

## 🖥️ 테스트 방법
> 리뷰어가 이 PR을 어떻게 테스트하면 되는지 알려주세요.

**Mac(Apple Silicon 포함) 환경에서 테스트**

```bash
# 1. .env 파일 생성
cp .env.example .env

# 2. 기존 이미지·캐시 정리 후 빌드
docker compose down -v
docker compose build --no-cache
docker compose up
```

**확인 순서**

1. `docker compose ps` → `frontend`, `backend`, `mariadb` 3개 모두 `Up` / `healthy` 상태인지 확인
2. `http://localhost` 접속 → 프론트엔드 정상 로딩 확인
3. `http://localhost/admin/login` → `admin` / `admin1234!` 로 로그인 확인
4. `http://localhost:8080/swagger-ui.html` → Swagger UI 정상 로딩 확인

**빌드 실패 감지 확인 (선택)**

```bash
# frontend 컨테이너 내부에 index.html 이 실제로 존재하는지 확인
docker exec -it $(docker compose ps -q frontend) ls /usr/share/nginx/html
```

> ⚠️ **Intel Mac / Windows / Linux 사용자**
> `platforms: linux/amd64` 설정은 기존 amd64 환경에서도 동일하게 동작하므로 영향 없습니다.
>
> `.env` 파일이 없어도 `docker-compose.yml`의 기본값이 적용되므로 기존 방식(`docker compose up --build`)도 동작합니다.

---

## 📸 스크린샷 (UI 변경 시)
> UI 변경이 있는 경우 Before / After 스크린샷을 첨부해주세요.

| Before | After |
|--------|-------|
| 해당 없음 | 해당 없음 |

---

## 💬 리뷰어에게 전달할 내용
> 리뷰 시 특별히 봐줬으면 하는 부분이나 논의가 필요한 사항이 있다면 작성해주세요.

- **`--platform=linux/amd64` 선택 이유**: Mac Apple Silicon(ARM64)에서 `node:20-alpine`, `amazoncorretto:21` 이미지가 ARM 네이티브로 빌드될 때 일부 native 모듈이 크래시 날 수 있습니다. `amd64`를 명시하면 Rosetta 2를 통해 에뮬레이션되어 안정적으로 동작합니다.
- **빌드 속도 트레이드오프**: `amd64` 강제 에뮬레이션으로 Apple Silicon에서 빌드 시간이 약간 늘어날 수 있습니다. 팀원이 전원 Intel Mac이라면 `--platform` 제거도 고려할 수 있습니다.
- **`.env` 기본값 동작**: `.env` 파일이 없으면 `docker-compose.yml`의 `${VAR:-default}` 기본값이 사용되므로 기존 방식도 그대로 동작합니다.
- **`frontend` healthcheck의 `wget` 사용**: `nginx:alpine` 이미지에는 `curl`이 없고 `wget`만 있어 `wget`을 사용했습니다.
- **`start_period: 30s` (mariadb healthcheck)**: `init.sql` 실행 시간을 고려한 값입니다. DB 초기화 스크립트가 무거워질 경우 늘려야 할 수 있습니다.
