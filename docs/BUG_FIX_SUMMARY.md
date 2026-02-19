# 버그 수정 정리

## 개요

로컬 Docker 환경에서 admin 계정 로그인이 불가능한 문제를 해결한 과정입니다.
발생한 오류는 총 세 단계로 나타났습니다.

---

## 버그 1: 로그인 시 500 Internal Server Error

### 증상
```
Failed to load resource: the server responded with a status of 500 ()
```

### 원인
`GlobalExceptionHandler`에 Spring Security의 `AuthenticationException` 처리 핸들러가 없었습니다.

로그인 실패 시 `BadCredentialsException` (AuthenticationException의 하위 클래스)이 발생하는데, 이를 처리하는 `@ExceptionHandler`가 없어서 범용 `Exception` 핸들러에 잡혀 500을 반환했습니다.

```
// 백엔드 로그
ERROR c.s.c.exception.GlobalExceptionHandler : Unhandled exception
org.springframework.security.authentication.BadCredentialsException: 자격 증명에 실패하였습니다.
```

### 수정 파일
`src/main/java/com/salesboost/common/exception/GlobalExceptionHandler.java`

```java
// 추가된 핸들러
@ExceptionHandler(AuthenticationException.class)
public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail("아이디 또는 비밀번호가 올바르지 않습니다."));
}
```

---

## 버그 2: 올바른 비밀번호로도 로그인 실패 (401)

### 증상
```
POST http://localhost/api/admin/login 401 (Unauthorized)
```
`docker compose down -v` 후 재빌드해도 동일 증상.

### 원인
`infra/docker/mariadb/init.sql`에 삽입된 admin 계정의 BCrypt 해시값이 `admin1234!`와 **일치하지 않았습니다.**

Spring Security의 `BCryptPasswordEncoder`로 직접 검증:
```java
// 검증 결과
encoder.matches("admin1234!", "$2a$10$eACCYoNOHEqXve8aIWT8Nu3PkMXWBaOxJ9aORUYzfywok1LiLAt22")
// → false (해시 불일치)
```

### 수정 파일
`infra/docker/mariadb/init.sql`

```sql
-- 기존 (잘못된 해시)
'$2a$10$eACCYoNOHEqXve8aIWT8Nu3PkMXWBaOxJ9aORUYzfywok1LiLAt22'

-- 수정 (admin1234! 에 대한 올바른 BCrypt 해시)
'$2a$10$BeQwlhKlYhekHpFB1MAr..tz6.KbZH/o.1rpe1k/7nEV1FgQ8KvQm'
```

> **참고**: BCrypt는 호출할 때마다 랜덤 솔트로 새 해시를 생성하지만, `matches()`는 솔트 정보가 해시에 포함되어 있어 항상 올바르게 검증됩니다.

---

## 버그 3: 로그인 성공 직후 SyntaxError 발생

### 증상
```
SyntaxError: "undefined" is not valid JSON
    at JSON.parse (<anonymous>)
    at index-JT1PK9st.js:6:6125
```

### 원인
프론트엔드 `auth.js`가 API 응답에서 존재하지 않는 필드를 참조했습니다.

실제 백엔드 응답 구조:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer"
  },
  "message": null
}
```

기존 코드의 문제점:
```javascript
// 잘못된 필드 참조
token.value = response.data.token          // undefined (실제: response.data.data.accessToken)
user.value = response.data.user            // undefined (해당 필드 없음)

// undefined가 문자열 "undefined"로 저장됨
localStorage.setItem('admin_user', JSON.stringify(undefined))
// → localStorage에 "undefined" 문자열 저장

// 다음 페이지 로드 시
JSON.parse("undefined")  // → SyntaxError 발생
```

### 수정 파일

**`frontend/src/stores/auth.js`**
```javascript
// 수정 전
const user = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))
// ...
token.value = response.data.token
user.value = response.data.user
localStorage.setItem('admin_token', response.data.token)
localStorage.setItem('admin_user', JSON.stringify(response.data.user))

// 수정 후
const user = ref(null)
// ...
const accessToken = response.data.data.accessToken
token.value = accessToken
localStorage.setItem('admin_token', accessToken)
```

**`frontend/src/services/api.js`**
```javascript
// 수정 전
localStorage.removeItem('admin_token')
localStorage.removeItem('admin_user')

// 수정 후
localStorage.removeItem('admin_token')
```

---

## 수정 파일 목록

| 파일 | 수정 내용 |
|------|-----------|
| `src/main/java/com/salesboost/common/exception/GlobalExceptionHandler.java` | `AuthenticationException` 핸들러 추가 (500 → 401) |
| `infra/docker/mariadb/init.sql` | admin BCrypt 해시 수정 |
| `frontend/src/stores/auth.js` | API 응답 필드 올바르게 참조, `admin_user` localStorage 제거 |
| `frontend/src/services/api.js` | `admin_user` localStorage 제거 |

---

## 재배포 시 주의사항

init.sql 변경이 반영되려면 반드시 볼륨을 초기화해야 합니다.
MariaDB 컨테이너는 볼륨이 존재하면 init.sql을 재실행하지 않습니다.

```bash
docker compose down -v
docker compose up --build -d
```