-- Init SQL for SalesBoost
CREATE DATABASE IF NOT EXISTS salesboost;

USE salesboost;

-- Default admin account (password: admin1234!)
-- BCrypt hash of 'admin1234!' with strength 10
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
        true
    );