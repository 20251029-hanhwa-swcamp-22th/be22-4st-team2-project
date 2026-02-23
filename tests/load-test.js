/**
 * SalesBoost 부하 테스트 스크립트 (k6)
 *
 * 설치: https://grafana.com/docs/k6/latest/set-up/install-k6/
 *   - macOS:   brew install k6
 *   - Windows: winget install k6
 *   - Linux:   snap install k6
 *
 * 실행 예시 (K8s Ingress 경유):
 *   k6 run tests/load-test.js
 *
 * 환경변수로 대상 URL 변경:
 *   k6 run -e BASE_URL=http://your-ingress-ip tests/load-test.js
 */

import http from "k6/http";
import { check, sleep } from "k6";

// 테스트 대상 URL (기본값: K8s Ingress를 통한 접근)
const BASE_URL = __ENV.BASE_URL || "http://localhost";

/**
 * 부하 시나리오: 3단계 (Ramp-up → Sustain → Ramp-down)
 *
 * 1단계 (0~1분):  0 → 50 VU 점진적 증가 (워밍업)
 * 2단계 (1~4분):  50 VU 유지 (최대 부하 - HPA 트리거 구간)
 * 3단계 (4~5분):  50 → 0 VU 점진적 감소 (쿨다운)
 */
export const options = {
  stages: [
    { duration: "1m", target: 50 },
    { duration: "3m", target: 50 },
    { duration: "1m", target: 0 },
  ],
  thresholds: {
    http_req_duration: ["p(95)<3000"], // 95% 요청이 3초 이내 응답
    http_req_failed: ["rate<0.05"],    // 에러율 5% 미만
  },
};

export default function () {
  // 1. 포트폴리오 목록 조회 (Public API - 가장 빈번한 요청)
  const portfolioList = http.get(`${BASE_URL}/api/portfolios`);
  check(portfolioList, {
    "GET /api/portfolios - 200 OK": (r) => r.status === 200,
    "응답시간 < 2초": (r) => r.timings.duration < 2000,
  });

  // 2. 메인 페이지 (Frontend - Nginx 정적 파일)
  const mainPage = http.get(`${BASE_URL}/`);
  check(mainPage, {
    "GET / - 200 OK": (r) => r.status === 200,
  });

  // 요청 간 1~3초 랜덤 대기 (실제 사용자 패턴 시뮬레이션)
  sleep(Math.random() * 2 + 1);
}
