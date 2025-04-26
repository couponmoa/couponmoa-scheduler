DROP TABLE IF EXISTS user_coupons;
CREATE TABLE user_coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    status enum ('EXPIRED', 'UNUSED', 'USED') NOT NULL,
    modified_at DATETIME(6) NULL
);

DROP TABLE IF EXISTS coupon_daily_usage_stats;
CREATE TABLE coupon_daily_usage_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    usage_count BIGINT NOT NULL,
    stat_date DATE NOT NULL
);