# Build stage
# --platform=linux/amd64 : Mac Apple Silicon(M1/M2/M3) 환경에서 native 모듈 크래시 방지
FROM --platform=linux/amd64 node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the app
RUN npm run build

# dist 디렉토리 존재 여부 검증: 빌드 실패 시 명확한 에러 메시지 출력 후 중단
RUN test -d /app/dist && test -f /app/dist/index.html \
    || (echo "ERROR: /app/dist/index.html not found. npm run build may have failed." && exit 1)

# Production stage
FROM --platform=linux/amd64 nginx:alpine

# curl 설치: healthcheck에서 사용 (nginx:alpine에는 기본 미포함)
RUN apk add --no-cache curl

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built files from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# nginx 유저가 필요한 디렉토리에 쓰기 권한 부여
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && chown nginx:nginx /var/run/nginx.pid

USER nginx

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
