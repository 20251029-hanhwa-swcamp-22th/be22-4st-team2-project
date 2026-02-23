# SalesBoost 모니터링 가이드

## 1. 아키텍처 개요

SalesBoost 프로젝트는 **로그 수집(ELK Stack 7.17.29)**과 **메트릭 모니터링(Prometheus v2.53.3 / Grafana 11.5.2)** 두 가지 축으로 모니터링을 구성합니다.

### 로그 플로우 (ELK Stack)

```
Spring Boot App
    │ (Logback + LogstashEncoder)
    │ TCP JSON
    ▼
Logstash (:5001)
    │ (파싱 + 인덱싱)
    ▼
Elasticsearch (:9200)
    │ (저장 + 검색)
    ▼
Kibana (:5601)
    └── 로그 검색/시각화 대시보드
```

### 메트릭 플로우 (Prometheus/Grafana)

```
Spring Boot App
    │ /actuator/prometheus
    │ (Micrometer → Prometheus 포맷)
    ▼
Prometheus v2.53.3 (:9090)
    │ (15초 간격 스크래핑)
    │ (저장 + 쿼리 + 알림 규칙 평가)
    ▼
Grafana 11.5.2 (:3001)
    └── 메트릭 대시보드 + 알림
```

---

## 2. 서비스 접속 정보

| 서비스 | URL | 설명 |
|--------|-----|------|
| **Elasticsearch** | Docker 내부 전용 (9200) | REST API — 호스트 포트 미노출 (보안) |
| **Kibana** | http://localhost:5601 | 로그 검색 및 시각화 UI (127.0.0.1 바인딩) |
| **Logstash** | Docker 내부 전용 (5001 TCP) | 로그 수신 (직접 접속 불필요) |
| **Prometheus** | Docker 내부 전용 (9090) | 메트릭 쿼리 — 호스트 포트 미노출 (보안) |
| **Grafana** | http://localhost:3001 | 메트릭 대시보드 (기본 계정: admin/admin, 127.0.0.1 바인딩) |
| **Actuator Health** | http://localhost:8081/actuator/health | 백엔드 헬스체크 (관리 포트 8081) |
| **Actuator Prometheus** | http://localhost:8081/actuator/prometheus | Prometheus 메트릭 엔드포인트 (관리 포트 8081) |

> **참고**: Elasticsearch와 Prometheus는 보안을 위해 호스트 포트를 노출하지 않습니다.
> 외부 디버깅이 필요한 경우 `docker-compose.yml`에서 포트 주석을 해제하세요.

---

## 3. 실행 방법

```bash
# 전체 스택 실행 (모니터링 포함)
docker compose up --build

# 모니터링 서비스만 실행
docker compose up elasticsearch logstash kibana prometheus grafana
```

---

## 4. Kibana 초기 설정

Kibana에 처음 접속하면 Index Pattern을 설정해야 로그를 조회할 수 있습니다.

1. http://localhost:5601 접속
2. 좌측 메뉴 → **Stack Management** → **Index Patterns**
3. **Create index pattern** 클릭
4. Index pattern name: `salesboost-logs-*` 입력
5. Time field: `@timestamp` 선택
6. **Create index pattern** 클릭
7. 좌측 메뉴 → **Discover** 에서 로그 확인

### 유용한 검색 쿼리

```
# 특정 로그 레벨 필터
level: "ERROR"

# 특정 클래스 로그 검색
logger_name: "com.salesboost.domain.portfolio.*"

# 최근 에러 로그
level: "ERROR" AND @timestamp >= now-1h
```

---

## 5. Grafana 대시보드 설정

### 5.1 로그인

- URL: http://localhost:3001
- 기본 계정: `admin` / `admin` (`.env`에서 변경 가능)
- 첫 로그인 시 비밀번호 변경 프롬프트 → Skip 가능

### 5.2 Prometheus 데이터소스

Grafana 프로비저닝으로 Prometheus 데이터소스가 **자동 등록**됩니다.
확인: **Configuration** → **Data Sources** → `Prometheus` 항목 존재 확인

### 5.3 Spring Boot 대시보드 Import

JVM 및 Spring Boot 메트릭을 한눈에 볼 수 있는 커뮤니티 대시보드를 import 합니다.

1. 좌측 메뉴 → **Dashboards** → **Import**
2. **Import via grafana.com** 에 Dashboard ID 입력: `19004`
3. **Load** 클릭
4. Prometheus 데이터소스 선택: `Prometheus`
5. **Import** 클릭

> Dashboard ID 19004: "Spring Boot 3.x Statistics" — JVM 메모리, HTTP 요청, GC, 스레드 등 종합 대시보드

---

## 6. 주요 모니터링 항목

### 6.1 애플리케이션 메트릭 (Prometheus/Grafana)

| 메트릭 | PromQL 예시 | 설명 |
|--------|-------------|------|
| **JVM 메모리** | `jvm_memory_used_bytes` | 힙/논힙 메모리 사용량 |
| **HTTP 요청률** | `rate(http_server_requests_seconds_count[5m])` | 5분간 초당 요청 수 |
| **HTTP 응답 시간** | `http_server_requests_seconds_sum / http_server_requests_seconds_count` | 평균 응답 시간 |
| **HTTP 에러율** | `rate(http_server_requests_seconds_count{status=~"5.."}[5m])` | 5xx 에러 비율 |
| **GC 횟수** | `rate(jvm_gc_pause_seconds_count[5m])` | GC 발생 빈도 |
| **GC 시간** | `jvm_gc_pause_seconds_sum` | GC 누적 소요 시간 |
| **활성 스레드** | `jvm_threads_live_threads` | 현재 활성 스레드 수 |
| **DB 커넥션 풀** | `hikaricp_connections_active` | HikariCP 활성 커넥션 수 |

### 6.2 로그 모니터링 (ELK)

| 항목 | 설명 |
|------|------|
| **에러 로그** | `level: ERROR` 필터로 실시간 에러 추적 |
| **요청 로그** | 컨트롤러/서비스 레이어 로그 추적 |
| **슬로우 쿼리** | JPA/MyBatis 쿼리 실행 시간 로그 |
| **보안 이벤트** | 인증 실패, 권한 거부 등 보안 관련 로그 |

---

## 7. Prometheus 알림 규칙

Prometheus에 사전 정의된 알림 규칙이 설정되어 있습니다. 파일: `infra/docker/prometheus/alert_rules.yml`

| 알림 | 조건 | 대기 시간 | 심각도 |
|------|------|-----------|--------|
| **BackendDown** | `up{job="salesboost-backend"} == 0` | 5분 | critical |
| **HighErrorRate** | 5xx 에러율 > 5% | 5분 | warning |
| **HighJvmMemoryUsage** | JVM 힙 사용률 > 80% | 5분 | warning |
| **DatabaseConnectionPoolLow** | HikariCP 유휴 커넥션 < 2 | 3분 | warning |

### 알림 확인 방법

```
# Prometheus UI에서 확인 (Docker 내부 접근 시)
docker compose exec prometheus wget -qO- http://localhost:9090/api/v1/rules | python3 -m json.tool

# Grafana에서 Prometheus 알림을 Discord/Slack으로 연동 가능
# → docs/DEVOPS_ADVANCED_GUIDE.md "모니터링 알림" 섹션 참조
```

---

## 8. Prometheus 타겟 확인

Prometheus 타겟 상태 확인 (Docker 내부 접근):

```bash
docker compose exec prometheus wget -qO- http://localhost:9090/api/v1/targets | python3 -m json.tool
```

| Job | Target | 예상 상태 |
|-----|--------|-----------|
| `salesboost-backend` | `backend:8081` | UP |
| `prometheus` | `localhost:9090` | UP |

상태가 `DOWN`인 경우:
- 백엔드가 아직 기동 중인지 확인 (start_period: 60초)
- `docker compose logs backend` 로 에러 확인

---

## 9. 트러블슈팅

### Elasticsearch가 기동되지 않을 때

```bash
# 로그 확인
docker compose logs elasticsearch

# 가장 흔한 원인: vm.max_map_count 부족 (Linux)
sudo sysctl -w vm.max_map_count=262144

# 영구 설정 (Linux)
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

> Windows/Mac Docker Desktop 환경에서는 이 설정이 기본 적용되어 있어 보통 문제가 없습니다.

### 메모리 부족

ELK 스택은 메모리를 많이 사용합니다. Docker Desktop에 최소 **4GB 이상** RAM을 할당하세요.

- Docker Desktop → Settings → Resources → Memory: **6GB 이상 권장**
- 현재 리소스 제한 설정:

| 서비스 | 메모리 제한 | CPU 제한 |
|--------|------------|---------|
| Backend | 1536M | 2.0 |
| Frontend | 128M | 0.5 |
| MariaDB | 512M | 1.0 |
| Elasticsearch | 2G (JVM 1G) | 1.5 |
| Logstash | 1G (JVM 512M) | 1.0 |
| Kibana | 1G | 1.0 |
| Prometheus | 1G | 0.5 |
| Grafana | 256M | 0.5 |

### Logstash 연결 실패

```bash
# Logstash 상태 확인
docker compose logs logstash

# Elasticsearch와 연결 확인
docker compose exec logstash curl -s http://elasticsearch:9200/_cluster/health
```

### 포트 충돌

| 포트 | 서비스 | 충돌 시 대처 |
|------|--------|-------------|
| 5001 | Logstash | 5000에서 변경 (Mac AirPlay Receiver 충돌 방지) |
| 5601 | Kibana | 다른 Kibana 인스턴스 확인 |
| 9090 | Prometheus | 다른 Prometheus 인스턴스 확인 |
| 9200 | Elasticsearch | 다른 ES 인스턴스 확인 |
| 3001 | Grafana | 포트 3000 충돌 회피를 위해 3001로 매핑됨 |

### Grafana에서 데이터가 보이지 않을 때

1. **Data Sources** 확인: Prometheus URL이 `http://prometheus:9090`인지 확인
2. **Prometheus targets** 확인: http://localhost:9090/targets 에서 backend가 UP인지 확인
3. **시간 범위** 확인: Grafana 우상단 시간 범위를 "Last 15 minutes"로 설정
4. 백엔드에 요청을 몇 번 보내서 메트릭 데이터 생성 (최초에는 데이터 없음)

---

## 10. 환경변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `GRAFANA_ADMIN_USER` | `admin` | Grafana 관리자 계정 |
| `GRAFANA_ADMIN_PASSWORD` | `admin` | Grafana 관리자 비밀번호 |
| `LOGSTASH_HOST` | `logstash:5001` | Logstash TCP 주소 |
