-- 如果 sys_user 已存在，请先确认有以下字段：
-- ALTER TABLE sys_user ADD COLUMN phone VARCHAR(20) NULL;
-- ALTER TABLE sys_user ADD COLUMN email VARCHAR(100) NULL;
-- ALTER TABLE sys_user ADD COLUMN member_card_no VARCHAR(30) NULL;
-- ALTER TABLE sys_user ADD COLUMN balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    member_card_no VARCHAR(30),
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS billiard_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    table_no VARCHAR(30) NOT NULL UNIQUE,
    table_name VARCHAR(50) NOT NULL,
    area_name VARCHAR(50),
    status VARCHAR(20) NOT NULL COMMENT 'IDLE/IN_USE/MAINTENANCE',
    hourly_price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255),
    current_use_start_time DATETIME NULL,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    contact_phone VARCHAR(20),
    status VARCHAR(20) NOT NULL COMMENT 'BOOKED/CANCELLED',
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_reservation_table FOREIGN KEY (table_id) REFERENCES billiard_table(id)
);

CREATE TABLE IF NOT EXISTS billiard_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    reservation_id BIGINT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration_minutes BIGINT NOT NULL,
    hourly_price DECIMAL(10, 2) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL COMMENT 'UNPAID/PAID',
    order_status VARCHAR(20) NOT NULL COMMENT 'FINISHED',
    member_card_last_four VARCHAR(4),
    paid_time DATETIME NULL,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_order_table FOREIGN KEY (table_id) REFERENCES billiard_table(id),
    CONSTRAINT fk_order_reservation FOREIGN KEY (reservation_id) REFERENCES reservation_record(id)
);

INSERT INTO sys_user (id, username, password, nickname, phone, email, member_card_no, balance, status, create_time, update_time)
VALUES
    (1, 'admin', '123456', '系统管理员', '13800000000', 'admin@example.com', '888800001234', 1000.00, 1, NOW(), NOW()),
    (2, 'zhangsan', '123456', '张三', '13800000001', 'zhangsan@example.com', '888800005678', 300.00, 1, NOW(), NOW()),
    (3, 'lisi', '123456', '李四', '13800000002', 'lisi@example.com', '888800009999', 500.00, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname),
    phone = VALUES(phone),
    email = VALUES(email),
    member_card_no = VALUES(member_card_no),
    balance = VALUES(balance),
    status = VALUES(status),
    update_time = NOW();

INSERT INTO billiard_table (table_no, table_name, area_name, status, hourly_price, image_url, current_use_start_time, remark)
VALUES
    ('A01', '1号台', '大厅A区', 'IDLE', 48.00, 'https://images.unsplash.com/photo-1511884642898-4c92249e20b6?auto=format&fit=crop&w=900&q=80', NULL, '标准美式球台'),
    ('A02', '2号台', '大厅A区', 'IN_USE', 58.00, 'https://images.unsplash.com/photo-1629294898907-e593d1b0ef93?auto=format&fit=crop&w=900&q=80', NOW(), '赛事级球台'),
    ('B01', '3号台', '包间B区', 'MAINTENANCE', 68.00, 'https://images.unsplash.com/photo-1612872087720-bb876e2e67d1?auto=format&fit=crop&w=900&q=80', NULL, '灯光待检修')
ON DUPLICATE KEY UPDATE
    table_name = VALUES(table_name),
    area_name = VALUES(area_name),
    status = VALUES(status),
    hourly_price = VALUES(hourly_price),
    image_url = VALUES(image_url),
    current_use_start_time = VALUES(current_use_start_time),
    remark = VALUES(remark),
    update_time = NOW();
