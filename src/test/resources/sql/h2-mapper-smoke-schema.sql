DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS t_veh_payment;
DROP TABLE IF EXISTS t_veh_registration;
DROP TABLE IF EXISTS t_veh_invoice;
DROP TABLE IF EXISTS t_veh_allocation;
DROP TABLE IF EXISTS t_md_dealer;
DROP TABLE IF EXISTS t_md_interior_color;
DROP TABLE IF EXISTS t_md_exterior_color;
DROP TABLE IF EXISTS t_md_model;
DROP TABLE IF EXISTS t_veh_production;
DROP TABLE IF EXISTS t_vehicle;

CREATE TABLE t_vehicle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin VARCHAR(17) NOT NULL,
    lifecycle_stage VARCHAR(30) NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE t_veh_production (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20),
    model_id BIGINT,
    year_make VARCHAR(20),
    exterior_color_id BIGINT,
    interior_color_id BIGINT,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE t_md_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(100),
    series VARCHAR(100),
    spec VARCHAR(100),
    model_code VARCHAR(100),
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE t_md_exterior_color (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    color_name VARCHAR(100),
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE t_md_interior_color (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    color_name VARCHAR(100),
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

CREATE TABLE t_veh_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20),
    drosstech_status VARCHAR(50),
    upload_date DATE,
    registration_date DATE,
    customer_region VARCHAR(100),
    remark8 VARCHAR(500),
    confirmed_by VARCHAR(50),
    confirmed_at TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE t_veh_allocation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20),
    allocated_date DATE,
    dealer_id BIGINT,
    sales_status VARCHAR(50),
    remark3 VARCHAR(500),
    confirmed_by VARCHAR(50),
    confirmed_at TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE t_md_dealer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dealer_name VARCHAR(100),
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
