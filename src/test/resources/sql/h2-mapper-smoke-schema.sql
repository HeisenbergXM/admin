DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS t_veh_payment;
DROP TABLE IF EXISTS t_veh_invoice;
DROP TABLE IF EXISTS t_vehicle;

CREATE TABLE t_vehicle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin VARCHAR(17) NOT NULL,
    lifecycle_stage VARCHAR(30) NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE t_veh_invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20) NOT NULL,
    invoice_seq INT NOT NULL,
    invoice_type VARCHAR(50),
    invoice_no VARCHAR(100),
    invoice_date DATE,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE t_veh_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20),
    payment_date DATE,
    credit_full_payment_date DATE,
    payment_status VARCHAR(50),
    remark5 VARCHAR(500),
    confirmed_by VARCHAR(50),
    confirmed_at TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    menu_type TINYINT NOT NULL,
    path VARCHAR(200),
    permission VARCHAR(100),
    icon VARCHAR(50),
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);

CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL
);
