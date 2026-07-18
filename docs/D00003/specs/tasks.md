# 任务清单 — D00003 运输管理改造（按 VIN 单车维护物流信息）

> 本清单为 `/cx-aicode:cx-work` 的直接输入。设计批准后执行。
> 本 spec 仅改造 D00001 的「运输管理」模块，其余模块沿用 D00001 不变。

## 阶段 0：数据模型（新增，旧表保留）

- [ ] T0.1 新增阶段表 `t_veh_inbound`（入库：saic_buy_off_date/date_to_storage_yard/remark2 + 统一公共字段 stage_status/确认人时间/审计），1:1 于 t_vehicle
- [ ] T0.2 新增阶段表 `t_veh_delivery`（配送：etd/eta/trolly_type/fully_load/received_date/delivery_status/remark7 + 统一公共字段），1:1 于 t_vehicle；经销商不冗余（取 t_veh_allocation）
- [ ] T0.3 旧单据式 6 张表（t_transport_order/_item、t_dispatch_list/t_waybill/t_waybill_dealer/_vin）**保留不删、不改结构**；仅确认新旧两套表并存、共享同一 t_vehicle

## 阶段 1：卡控与确认（复用现有机制）

- [ ] T1.1 复用生命周期卡控服务与阶段确认通用机制，将 `t_veh_inbound`/`t_veh_delivery` 接入「草稿→确认锁定→推进」流程（单车粒度）
- [ ] T1.2 入库确认卡控：校验 VIN 处 PENDING_INBOUND → 确认锁定 → 推进 PENDING_ALLOCATION（移除“未被其他运输单占用”校验）
- [ ] T1.3 配送确认卡控：校验 VIN 处 PENDING_DELIVERY 且已分配经销商 → 确认锁定 → 推进 PENDING_REGISTRATION（移除单据行归属校验）

## 阶段 2：运输管理功能（按 VIN 单车维护）

- [ ] T2.1 车厂→中转仓库：PENDING_INBOUND 待办列表（VIN + 基本信息，多条件查询）+ 单车入库维护（draft 可改）+ 单车确认接口与页面
- [ ] T2.2 中转仓库→经销商：PENDING_DELIVERY 待办列表（VIN + 基本信息 + 只读经销商）+ 单车配送维护（draft 可改）+ 单车确认签收接口与页面
- [ ] T2.3 车辆基本信息与经销商实时带出（取 t_vehicle/主数据/t_veh_allocation，不冗余存储）

## 阶段 3：旧入口停用、全景视图与收尾

- [ ] T3.1 车辆全景视图物流环节改为聚合 t_veh_inbound/t_veh_delivery（新阶段表为准）
- [ ] T3.2 停用旧单据式入口：从菜单管理隐藏/移除运输单、发车清单/行车路单/经销商行的菜单项与路由；**保留后端 Controller/Service/Repository 与 API 不动**，可快速恢复
- [ ] T3.3 验证两套共存互不影响：旧套代码/API/表仍存在且结构未改，仅前端无入口；旧 API 直接调用仍可正常工作；新功能为运输管理唯一可见入口且能独立推进生命周期
- [ ] T3.4 端到端联调：一辆车 PENDING_INBOUND→入库确认→…→PENDING_DELIVERY→配送确认→PENDING_REGISTRATION 全链路卡控跑通
- [ ] T3.5 回归验证（对照设计 §8 隔离约束）：生产/销售/财务/上牌/主数据/系统管理/全景视图行为不变；共享卡控服务/字典/t_vehicle 未被就地改写；项目既有自动化测试（含旧运输管理）全部继续通过、断言未删改
- [ ] T3.6 验收测试（对照设计文档第 10 节）

## 实现护栏（§8，全程遵守）

- 只增不改：仅新增 t_veh_inbound/t_veh_delivery 与新 Controller/Service/Repository；不删改旧 6 张表、不改 t_vehicle 及其他 t_veh_* 结构
- 复用而非改写：生命周期卡控/确认通用机制以调用/扩展方式接入，不就地改写既有方法签名与行为（新增分支不动旧分支）
- 共享资源只读安全用：阶段枚举/字典 delivery_status 复用不新增不重命名；经销商只读取 t_veh_allocation 不写回
- 停用=仅隐藏菜单入口，旧后端代码/API 保留可恢复

## 依赖关系摘要

```
T0.1/T0.2（新增阶段表，旧表保留）→ T1.*（复用卡控确认，不改写）→ T2.*（单车维护功能）→ T3.*（旧入口停用/全景视图/共存验证/联调/回归/验收）
T3.2 仅隐藏旧菜单入口，旧后端代码/表/API 全部保留；T3.3/T3.5 专项验证不回归
其余模块沿用 D00001，不在本清单范围
```
