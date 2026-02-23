#!/bin/bash
# Elasticsearch ILM (Index Lifecycle Management) 초기 설정 스크립트
# 사용법: docker compose up 후 ES가 healthy 상태일 때 실행
#   bash infra/docker/elasticsearch/setup-ilm.sh

ES_HOST="${ES_HOST:-http://localhost:9200}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Elasticsearch ILM 설정 시작..."
echo "대상: ${ES_HOST}"

# 1. ILM 정책 생성 (7일 보존, 1GB 또는 1일 단위 롤오버)
echo ""
echo "[1/3] ILM 정책 생성 (salesboost-logs-policy)..."
curl -s -X PUT "${ES_HOST}/_ilm/policy/salesboost-logs-policy" \
  -H 'Content-Type: application/json' \
  -d @"${SCRIPT_DIR}/ilm-policy.json"
echo ""

# 2. 인덱스 템플릿 생성 (새 인덱스에 ILM 정책 자동 적용)
echo "[2/3] 인덱스 템플릿 생성 (salesboost-logs-template)..."
curl -s -X PUT "${ES_HOST}/_index_template/salesboost-logs-template" \
  -H 'Content-Type: application/json' \
  -d '{
  "index_patterns": ["salesboost-logs-*"],
  "template": {
    "settings": {
      "index.lifecycle.name": "salesboost-logs-policy"
    }
  }
}'
echo ""

# 3. 기존 인덱스에도 ILM 정책 적용
echo "[3/3] 기존 인덱스에 ILM 정책 적용..."
curl -s -X PUT "${ES_HOST}/salesboost-logs-*/_settings" \
  -H 'Content-Type: application/json' \
  -d '{
  "index.lifecycle.name": "salesboost-logs-policy"
}' 2>/dev/null || echo "(기존 인덱스 없음 - 정상)"
echo ""

echo ""
echo "ILM 설정 완료!"
echo "  - 정책: 7일 경과 인덱스 자동 삭제"
echo "  - 롤오버: 1일 또는 1GB 초과 시"
echo "  - 확인: curl ${ES_HOST}/_ilm/policy/salesboost-logs-policy"
