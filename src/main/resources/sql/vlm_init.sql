-- ========================================================================
-- VLM 车辆全生命周期管理系统 - 数据库 DDL
-- 新增 18 张表，与现有 sys_* 表共存
-- ========================================================================

USE admin_system;

-- ========== 字典管理 ==========

CREATE TABLE IF NOT EXISTS t_sys_dict_type (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
    remark VARCHAR(200) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB COMMENT='字典类型';

CREATE TABLE IF NOT EXISTS t_sys_dict_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL COMMENT '所属字典类型ID',
    item_value VARCHAR(100) NOT NULL COMMENT '字典值',
    item_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
    remark VARCHAR(200) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_dict_type_id (dict_type_id)
) ENGINE=InnoDB COMMENT='字典项';

-- ========== 主数据 ==========

CREATE TABLE IF NOT EXISTS t_md_model (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) DEFAULT NULL COMMENT '物料编码',
    series VARCHAR(100) DEFAULT NULL COMMENT '车系',
    spec VARCHAR(100) DEFAULT NULL COMMENT '配置规格(SPEC)',
    model_name VARCHAR(100) DEFAULT NULL COMMENT '车型名称(展示用)',
    model_code VARCHAR(50) DEFAULT NULL COMMENT '车型代码',
    year_make VARCHAR(20) DEFAULT NULL COMMENT '年款',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='车型数据';

CREATE TABLE IF NOT EXISTS t_md_exterior_color (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    color_name VARCHAR(100) NOT NULL COMMENT '颜色名称(英文)',
    color_name_cn VARCHAR(100) DEFAULT NULL COMMENT '颜色中文名',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='外饰颜色';

CREATE TABLE IF NOT EXISTS t_md_interior_color (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    color_name VARCHAR(100) NOT NULL COMMENT '颜色名称(英文)',
    color_name_cn VARCHAR(100) DEFAULT NULL COMMENT '颜色中文名',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='内饰颜色';

CREATE TABLE IF NOT EXISTS t_md_dealer (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dealer_code VARCHAR(50) NOT NULL COMMENT '经销商编码',
    dealer_name VARCHAR(200) NOT NULL COMMENT '经销商名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dealer_code (dealer_code)
) ENGINE=InnoDB COMMENT='经销商';

-- ========== 车辆主表 ==========

CREATE TABLE IF NOT EXISTS t_vehicle (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vin VARCHAR(17) NOT NULL COMMENT '车辆识别码(VIN)',
    lifecycle_stage VARCHAR(30) NOT NULL COMMENT '当前生命周期阶段',
    created_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    updated_by VARCHAR(50) DEFAULT NULL COMMENT '修改人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vin (vin)
) ENGINE=InnoDB COMMENT='车辆主表';

-- ========== 生命周期阶段表 ==========
-- 公共字段：id, vehicle_id, stage_status, confirmed_by, confirmed_at,
--           created_by, created_at, updated_by, updated_at, deleted

CREATE TABLE IF NOT EXISTS t_veh_production (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL COMMENT 'FK t_vehicle',
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
    model_id BIGINT DEFAULT NULL COMMENT 'FK t_md_model',
    exterior_color_id BIGINT DEFAULT NULL COMMENT 'FK t_md_exterior_color',
    interior_color_id BIGINT DEFAULT NULL COMMENT 'FK t_md_interior_color',
    engine_number VARCHAR(50) DEFAULT NULL COMMENT '发动机号',
    year_make VARCHAR(20) DEFAULT NULL COMMENT '年款',
    material VARCHAR(100) DEFAULT NULL COMMENT '物料',
    shipment VARCHAR(100) DEFAULT NULL COMMENT 'Shipment',
    batch VARCHAR(100) DEFAULT NULL COMMENT 'Batch',
    offline_epmb_date DATE DEFAULT NULL COMMENT 'Offline EPMB日期',
    epmb_ok_date DATE DEFAULT NULL COMMENT 'EPMB ok日期',
    remark1 VARCHAR(500) DEFAULT NULL COMMENT '备注1',
    confirmed_by VARCHAR(50) DEFAULT NULL COMMENT '确认人',
    confirmed_at DATETIME DEFAULT NULL COMMENT '确认时间',
    created_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) DEFAULT NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='生产阶段';

CREATE TABLE IF NOT EXISTS t_veh_allocation (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL COMMENT 'FK t_vehicle',
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    allocated_date DATE DEFAULT NULL COMMENT '分配日期',
    dealer_id BIGINT DEFAULT NULL COMMENT 'FK t_md_dealer',
    sales_status VARCHAR(50) DEFAULT NULL COMMENT '销售状态(字典)',
    remark3 VARCHAR(500) DEFAULT NULL COMMENT '备注3',
    confirmed_by VARCHAR(50) DEFAULT NULL,
    confirmed_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='销售分配阶段';

CREATE TABLE IF NOT EXISTS t_veh_invoice (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL COMMENT 'FK t_vehicle',
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    invoice_seq INT NOT NULL COMMENT '1=首次开票 2=转正记录',
    invoice_type VARCHAR(50) DEFAULT NULL COMMENT '发票种类(正式/形式，字典)',
    invoice_no VARCHAR(100) DEFAULT NULL COMMENT '发票号',
    invoice_date DATE DEFAULT NULL COMMENT '发票日期',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注(remark4/remark6)',
    confirmed_by VARCHAR(50) DEFAULT NULL,
    confirmed_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='发票阶段(1:N)';

CREATE TABLE IF NOT EXISTS t_veh_payment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL COMMENT 'FK t_vehicle',
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    payment_date DATE DEFAULT NULL COMMENT '收款日期',
    credit_full_payment_date DATE DEFAULT NULL COMMENT '信用全款日期',
    payment_status VARCHAR(50) DEFAULT NULL COMMENT '收款状态(字典)',
    remark5 VARCHAR(500) DEFAULT NULL COMMENT '备注5',
    confirmed_by VARCHAR(50) DEFAULT NULL,
    confirmed_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='收款阶段';

CREATE TABLE IF NOT EXISTS t_veh_registration (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL COMMENT 'FK t_vehicle',
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    drosstech_status VARCHAR(50) DEFAULT NULL COMMENT 'Drosstech状态(字典)',
    upload_date DATE DEFAULT NULL COMMENT '上传日期',
    registration_date DATE DEFAULT NULL COMMENT '注册日期',
    customer_region VARCHAR(100) DEFAULT NULL COMMENT '客户区域',
    remark8 VARCHAR(500) DEFAULT NULL COMMENT '备注8',
    confirmed_by VARCHAR(50) DEFAULT NULL,
    confirmed_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='上牌阶段';

-- ========== 物流单据 ==========

CREATE TABLE IF NOT EXISTS t_transport_order (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) DEFAULT NULL COMMENT '运输单号',
    date_to_storage_yard DATE DEFAULT NULL COMMENT '到仓库日期(整单共享)',
    remark2 VARCHAR(500) DEFAULT NULL COMMENT '备注2',
    order_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
    confirmed_by VARCHAR(50) DEFAULT NULL,
    confirmed_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB COMMENT='运输单(车厂到仓库)';

CREATE TABLE IF NOT EXISTS t_transport_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    transport_order_id BIGINT NOT NULL COMMENT 'FK t_transport_order',
    vehicle_id BIGINT NOT NULL COMMENT 'FK t_vehicle',
    saic_buy_off_date DATE DEFAULT NULL COMMENT 'SAIC buy off日期(逐车)',
    KEY idx_transport_order_id (transport_order_id),
    KEY idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='运输单明细';

CREATE TABLE IF NOT EXISTS t_dispatch_list (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dispatch_no VARCHAR(50) DEFAULT NULL COMMENT '发车清单号',
    list_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dispatch_no (dispatch_no)
) ENGINE=InnoDB COMMENT='发车清单';

CREATE TABLE IF NOT EXISTS t_waybill (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waybill_no VARCHAR(50) DEFAULT NULL COMMENT '路单号',
    dispatch_list_id BIGINT NOT NULL COMMENT 'FK t_dispatch_list',
    trolly_type VARCHAR(20) DEFAULT NULL COMMENT '轿运车类型(4 units/6 units)',
    fully_load TINYINT DEFAULT 0 COMMENT '是否满载',
    KEY idx_dispatch_list_id (dispatch_list_id)
) ENGINE=InnoDB COMMENT='行车路单';

CREATE TABLE IF NOT EXISTS t_waybill_dealer (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waybill_id BIGINT NOT NULL COMMENT 'FK t_waybill',
    dealer_id BIGINT NOT NULL COMMENT 'FK t_md_dealer',
    etd_to_dealer DATE DEFAULT NULL COMMENT '发车日期',
    eta_to_dealer DATE DEFAULT NULL COMMENT '预计到达',
    received_date DATE DEFAULT NULL COMMENT '签收日期',
    delivery_status VARCHAR(50) DEFAULT NULL COMMENT '配送状态(字典)',
    remark7 VARCHAR(500) DEFAULT NULL COMMENT '备注7',
    row_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
    confirmed_by VARCHAR(50) DEFAULT NULL,
    confirmed_at DATETIME DEFAULT NULL,
    KEY idx_waybill_id (waybill_id)
) ENGINE=InnoDB COMMENT='行车路单-经销商行';

CREATE TABLE IF NOT EXISTS t_waybill_dealer_vin (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waybill_dealer_id BIGINT NOT NULL COMMENT 'FK t_waybill_dealer',
    vehicle_id BIGINT NOT NULL COMMENT 'FK t_vehicle',
    KEY idx_waybill_dealer_id (waybill_dealer_id),
    KEY idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='经销商行-VIN';
