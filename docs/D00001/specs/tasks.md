# 任务清单 — D00001 车辆全生命周期管理系统（VLM）

> 本清单为 `/cx-aicode:cx-work` 的直接输入。设计批准后执行。
> 任务按依赖顺序编号；前缀分组便于并行分工。

## 阶段 0：项目骨架与基础设施

- [ ] T0.1 确定技术栈并初始化前后端工程骨架（分层结构、依赖管理、代码规范）
- [ ] T0.2 设计并创建数据库 schema：精简主表 t_vehicle + 五张阶段表（t_veh_production/t_veh_allocation/t_veh_invoice/t_veh_payment/t_veh_registration）+ 物流单据表（车厂到仓库 t_transport_order/t_transport_order_item；仓库到经销商 t_dispatch_list/t_waybill/t_waybill_dealer/t_waybill_dealer_vin，物流业务数据存于单据、不单设阶段表）+ 主数据表 + 系统表；所有表名统一加 t_ 前缀；VIN 唯一约束；阶段表统一含 stage_status/确认人/确认时间/创建/修改审计字段
- [ ] T0.3 搭建统一响应/异常/分页基础框架
- [ ] T0.4 登录鉴权与会话/Token 机制

## 阶段 1：系统管理（RBAC 基座）

- [ ] T1.1 字典管理：t_sys_dict_type / t_sys_dict_item CRUD + 接口 + 页面
- [ ] T1.2 菜单管理：菜单树（目录/菜单两级）CRUD + 维护菜单时设置其路由；不含按钮/权限点
- [ ] T1.3 角色管理：角色 CRUD + 角色-菜单授权（仅菜单级，无按钮权限）
- [ ] T1.4 用户管理：用户 CRUD、启停、重置密码、分配角色
- [ ] T1.5 前端按菜单授权渲染（仅菜单级）+ 后端接口级鉴权拦截
- [ ] T1.6 初始化基础字典数据（发票/收款/配送/上牌/销售状态等取值）

## 阶段 2：主数据管理

- [ ] T2.1 车型数据 t_md_model：CRUD + 启停 + 查询（含物料编码/车系/SPEC/年款）
- [ ] T2.2 内饰颜色 t_md_interior_color：CRUD + 启停
- [ ] T2.3 外饰颜色 t_md_exterior_color：CRUD + 启停
- [ ] T2.4 经销商维护 t_md_dealer：CRUD + 启停 + 按编码/名称查询（导入 Dealer Code 样本）；菜单归属「主数据管理/经销商维护」

## 阶段 3：车辆主记录与生命周期卡控

- [ ] T3.1 t_vehicle 精简主表（id/vin/lifecycle_stage + 审计字段）CRUD 基础（全景视图见 T8.1）
- [ ] T3.2 生命周期状态机与卡控服务：阶段流转 + 前置「已确认」校验，后端为准
- [ ] T3.3 阶段确认通用机制：阶段表 draft↔confirmed 状态、确认后锁定不可改/不可重复确认、确认即推进 lifecycle_stage；统一公共字段（确认人/确认时间 + 创建/修改审计字段）；各阶段涉及车辆处除 VIN 外可显示车型/颜色/车型年（实时取主表与主数据，不冗余存储）
- [ ] T3.4 车辆列表多条件查询（无导出）

## 阶段 4：生产管理

- [ ] T4.1 车辆录入 t_veh_production：VIN 唯一校验 + 车型/颜色主数据引用 + 生产字段；首次保存创建主记录（初始 PENDING_OFFLINE，可反复修改）→ 确认 → PENDING_INBOUND

## 阶段 5：运输管理（单据驱动）

- [ ] T5.1 车厂到仓库 — 运输单：表头(date_to_storage_yard/remark2) + 多 VIN 明细(逐车 saic_buy_off)；候选仅 PENDING_INBOUND 且未占用的 VIN；草稿可改 → 整单确认 → 所有 VIN 推进 PENDING_ALLOCATION（物流业务数据存于运输单）
- [ ] T5.2 仓库到经销商 — 发车清单→行车路单(trolly_type/满载)→经销商行(选经销商+ETD/ETA/签收/Delivery Status/remark7+多VIN)；候选仅该经销商 PENDING_DELIVERY 的 VIN；草稿可改 → 按经销商行确认签收 → 该行 VIN 推进 PENDING_REGISTRATION（物流业务数据存于经销商行）

## 阶段 6：销售管理

- [ ] T6.1 车辆销售 t_veh_allocation：分配经销商/分配日期/销售状态，草稿可改 → 确认（前置 PENDING_ALLOCATION）→ PENDING_INVOICE
- [ ] T6.2 车辆上牌 t_veh_registration：Drosstech/上传/注册/客户区域，草稿可改 → 确认（前置 PENDING_REGISTRATION）→ COMPLETED

## 阶段 7：财务管理

- [ ] T7.1 发票确认 t_veh_invoice：发票种类/号/日期，写 invoice_seq=1，草稿可改 → 确认（前置 PENDING_INVOICE）→ PENDING_PAYMENT
- [ ] T7.2 形式发票转正：新增 invoice_seq=2 保留两组，草稿可改 → 确认（前置 PENDING_PAYMENT 且首张为形式发票）
- [ ] T7.3 收款确认 t_veh_payment：收款日期/信用全款/收款状态，草稿可改 → 确认（前置：PENDING_PAYMENT 且持有正式发票，形式发票须先经 T7.2 转正）→ PENDING_DELIVERY

## 阶段 8：全局与收尾

- [ ] T8.1 车辆全景视图（仅业务主管可访问）：按 VIN 聚合五张阶段表 + 物流单据数据与时间线，按车辆展示全生命周期，支持导出
- [ ] T8.2 （可选）Excel 历史数据导入模板与导入工具
- [ ] T8.3 端到端联调：跑通一辆车从录入到上牌的完整卡控链路
- [ ] T8.4 验收测试（对照设计文档第 10 节验收标准）

## 依赖关系摘要

```
T0.* → T1.*（RBAC 基座）→ T2.*（主数据，含经销商维护）
                         → T3.*（精简主表 + 卡控 + 确认机制）→ T4 → T5.1 → T6.1 → T7.1 → T7.2（形式转正）→ T7.3（收款）→ T5.2 → T6.2
T3.2（卡控服务）+ T3.3（阶段确认机制）为 T4~T7 所有阶段操作的公共依赖
收款 T7.3 前置：持有正式发票（首张即正式，或经 T7.2 转正）
T8.* 在主流程完成后进行
```
