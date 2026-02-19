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
-- 5. 기본 관리자 계정 삽입
--    username : admin
--    password : admin1234!  (BCrypt strength=10)
-- -------------------------------------------------------------
INSERT IGNORE INTO
    admin_user (
        username,
        password,
        role,
        enabled
    )
VALUES (
        'admin',
        '$2a$10$eACCYoNOHEqXve8aIWT8Nu3PkMXWBaOxJ9aORUYzfywok1LiLAt22',
        'ROLE_ADMIN',
        1
    );