-- =============================================================
-- SalesBoost DB 초기화 스크립트
-- MariaDB 컨테이너 최초 기동 시 1회 실행됩니다.
-- (이후 실행 시에는 data 볼륨이 존재하므로 실행되지 않음)
--
-- ⚠️ 주의: INSERT IGNORE는 테이블이 존재해야 동작합니다.
--   이 파일에서 테이블을 먼저 생성한 뒤 INSERT해야 합니다.
--   application.yml의 ddl-auto: update와 중복되지만,
--   init.sql 실행 시점에는 Spring Boot가 아직 기동되지 않으므로
--   이 파일에서 테이블을 직접 생성해야 합니다.
-- =============================================================

CREATE DATABASE IF NOT EXISTS salesboost CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE salesboost;

-- -------------------------------------------------------------
-- 1. admin_user 테이블
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 2. inquiry 테이블
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inquiry (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    inquiry_type VARCHAR(30) NOT NULL,
    content VARCHAR(3000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    admin_memo VARCHAR(3000),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 3. portfolio 테이블
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS portfolio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(3000) NOT NULL,
    client_name VARCHAR(150) NOT NULL,
    industry VARCHAR(100) NOT NULL,
    thumbnail_url VARCHAR(1000),
    visible TINYINT(1) NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 4. portfolio_image 테이블
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS portfolio_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    portfolio_id BIGINT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    image_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_image_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolio (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 5. refresh_token 테이블
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token_token (token),
    CONSTRAINT fk_refresh_token_admin_user FOREIGN KEY (admin_user_id) REFERENCES admin_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 6. 기본 관리자 계정 삽입 (개발 환경 전용)
--    username : admin
--    ⚠️ 운영 환경에서는 이 계정을 삭제하고 별도 관리자 생성 필요
-- -------------------------------------------------------------
INSERT IGNORE INTO admin_user (username, password, role, enabled)
VALUES (
    'admin',
    '$2a$10$BeQwlhKlYhekHpFB1MAr..tz6.KbZH/o.1rpe1k/7nEV1FgQ8KvQm',
    'ROLE_ADMIN',
    1
);

-- -------------------------------------------------------------
-- 6. 포트폴리오 샘플 데이터
--    썸네일/이미지: Unsplash 무료 이미지 (업종별 선별)
-- -------------------------------------------------------------
INSERT IGNORE INTO portfolio
    (id, title, description, client_name, industry, thumbnail_url, visible, display_order, created_at, updated_at)
VALUES
(1,
 '글로벌 전자부품 제조사 수출 자동화',
 'PI/PO 문서 자동화 및 출하관리 시스템 구축으로 업무 효율 40% 향상. 반복 문서 작업을 시스템화하여 담당자 1인이 처리하던 물량을 3배 확대했습니다.',
 'ABC Electronics',
 '전자/반도체',
 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&auto=format&fit=crop&q=80',
 1, 1, NOW(), NOW()),

(2,
 '자동차 부품 수출기업 CI/PL 자동화',
 '복잡한 CI/PL 문서 작업을 자동화하여 연간 2,000시간 절감. 거래처별 서식을 템플릿화하고 ERP 연동으로 오기입을 원천 차단했습니다.',
 'AutoParts Korea',
 '자동차/기계',
 'https://images.unsplash.com/photo-1565043666747-69f6646db940?w=800&auto=format&fit=crop&q=80',
 1, 2, NOW(), NOW()),

(3,
 '화학제품 무역회사 다국어 서류 관리',
 '다국어 무역서류 지원 및 실시간 출하현황 모니터링 시스템 구축. 영문·중문·일문 동시 발행으로 해외 바이어 대응 속도를 50% 단축했습니다.',
 'ChemTrade Co.',
 '화학/소재',
 'https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=800&auto=format&fit=crop&q=80',
 1, 3, NOW(), NOW()),

(4,
 '섬유/의류 수출업체 시즌 대량 주문 관리',
 '시즌별 대량 주문 관리 및 생산지시 자동화로 리드타임 30% 단축. 발주서에서 출고까지 전 프로세스를 단일 플랫폼에서 관리합니다.',
 'Fashion Export',
 '섬유/의류',
 'https://images.unsplash.com/photo-1558171813-4c088753af8f?w=800&auto=format&fit=crop&q=80',
 1, 4, NOW(), NOW()),

(5,
 '식품기업 농산물 수출 이력 관리',
 '검역·인증서류 통합 관리 및 배치별 이력추적 시스템 도입. 수입국 규정에 맞는 서류를 자동 생성하여 통관 지연율을 80% 감소시켰습니다.',
 'K-Food Global',
 '식품/농산물',
 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=800&auto=format&fit=crop&q=80',
 1, 5, NOW(), NOW());

-- 포트폴리오 상세 이미지 샘플
INSERT IGNORE INTO portfolio_image (portfolio_id, image_url, image_order)
VALUES
(1, 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&auto=format&fit=crop&q=80', 1),
(1, 'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=1200&auto=format&fit=crop&q=80', 2),
(2, 'https://images.unsplash.com/photo-1565043666747-69f6646db940?w=1200&auto=format&fit=crop&q=80', 1),
(2, 'https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=1200&auto=format&fit=crop&q=80', 2),
(3, 'https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=1200&auto=format&fit=crop&q=80', 1),
(4, 'https://images.unsplash.com/photo-1558171813-4c088753af8f?w=1200&auto=format&fit=crop&q=80', 1),
(5, 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=1200&auto=format&fit=crop&q=80', 1);

-- -------------------------------------------------------------
-- 7. 제휴 문의 샘플 데이터
-- -------------------------------------------------------------
INSERT IGNORE INTO inquiry
    (id, company_name, contact_name, email, phone, inquiry_type, content, status, admin_memo, created_at, updated_at)
VALUES
(1,
 '삼성전자 글로벌 사업부',
 '김민준',
 'minjun.kim@samsung-global.com',
 '02-2255-1234',
 'PARTNERSHIP',
 '안녕하세요. 당사는 연간 3,000건 이상의 수출 건을 처리하고 있으며, SalesBoost 솔루션 도입을 검토 중입니다. 현재 사용 중인 ERP(SAP)와의 연동 가능 여부와 도입 일정에 대해 상담을 요청드립니다.',
 'IN_PROGRESS',
 '담당 영업 배정 완료. 2월 25일 온라인 미팅 예정.',
 DATE_SUB(NOW(), INTERVAL 5 DAY),
 DATE_SUB(NOW(), INTERVAL 2 DAY)),

(2,
 'LG화학 해외영업팀',
 '박지영',
 'jiyoung.park@lgchem.com',
 '02-3773-5678',
 'GENERAL',
 '안녕하세요. 화학소재 수출 관련 서류 자동화에 관심이 있습니다. 특히 MSDS 연동 기능과 위험물 서류 자동 생성 기능이 있는지 확인하고 싶습니다. 데모 시연을 요청드립니다.',
 'PENDING',
 '',
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_SUB(NOW(), INTERVAL 3 DAY)),

(3,
 '현대자동차 부품 협력사',
 '이상호',
 'sangho.lee@hyundai-partner.com',
 '031-498-9012',
 'QUOTE',
 '중소 부품사입니다. 월 500건 내외 수출 건 처리에 적합한 플랜과 비용이 궁금합니다. 사용자 수 제한과 추가 비용 여부도 알고 싶습니다.',
 'DONE',
 '표준 플랜 가격표 이메일 발송 완료. 추가 문의 없음.',
 DATE_SUB(NOW(), INTERVAL 10 DAY),
 DATE_SUB(NOW(), INTERVAL 7 DAY)),

(4,
 'CJ대한통운 국제물류',
 '최수연',
 'suyeon.choi@cjlogistics.com',
 '1588-1255',
 'ETC',
 '현재 베타 사용 중인데 BL 자동완성 기능에서 Port of Discharge 항목이 자동으로 지워지는 현상이 발생합니다. 재현 조건을 첨부하오니 확인 부탁드립니다.',
 'PENDING',
 '',
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_SUB(NOW(), INTERVAL 1 DAY)),

(5,
 '포스코인터내셔널',
 '강동현',
 'donghyun.kang@poscoint.com',
 '02-6900-3456',
 'ETC',
 '수출 컨소시엄 형태로 5개 중소기업이 공동으로 솔루션을 활용하는 방식이 가능한지 문의드립니다. 각 기업별 데이터 격리와 공동 바이어 관리가 동시에 필요한 상황입니다.',
 'PENDING',
 '',
 NOW(),
 NOW());
