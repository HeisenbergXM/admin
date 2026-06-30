# check1 后端检查总结

检查日期：2026-06-30

## 检查范围

- 需求/设计文档：`docs/superpowers/`、`docs/D00001/`、`docs/D00002/`
- 后端实现：Spring Boot API、Service、Mapper XML、SQL 初始化脚本、单元测试
- D00002 为前端页面设计文档，本次只参考其后端接口契约和状态流转要求，未实现前端功能。

## 冒烟测试结果

- 直接执行 `mvn test` 失败：当前 shell 的 PATH 中没有 `mvn`。
- 使用本机 Maven 显式路径执行成功：

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' test
```

结果：`Tests run: 45, Failures: 0, Errors: 0, Skipped: 0`。

## 本次发现并修复的问题

1. 发票阶段不符合“草稿可改 -> 确认锁定”的统一阶段范式。
   - 原实现创建发票时直接 `CONFIRMED` 并推进到 `PENDING_PAYMENT`，导致发票草稿无法编辑。
   - 已改为首次开票先保存 `DRAFT`，新增 `POST /api/invoices/{id}/confirm` 确认接口；seq=1 确认后推进 `PENDING_INVOICE -> PENDING_PAYMENT`，seq=2 转正发票确认后只锁定记录，不推进生命周期。

2. 形式发票转正未支持草稿态。
   - 原实现转正时直接创建已确认正式发票。
   - 已改为先创建 seq=2 正式发票草稿，再通过确认接口锁定。

3. 发票/收款列表漏掉“尚未创建阶段草稿”的待办车辆。
   - 原 Mapper 从 `t_veh_invoice` / `t_veh_payment` 起查，导致待开票、待收款但尚未保存草稿的车辆不出现在页面列表。
   - 已改为从 `t_vehicle` 起查并左关联阶段记录；有效正式发票只认 `stage_status='CONFIRMED' AND invoice_type='INVOICED'`。

4. 全景视图把草稿正式发票误判为有效正式发票。
   - 已改为同时校验 `CONFIRMED + INVOICED`。

5. `/api/auth/info` 返回角色 ID 而不是角色编码，且未返回菜单树。
   - 已新增按用户查询 `role_code`，响应中补充 `menus` 菜单树。
   - 菜单树只返回目录/菜单，不把按钮权限点混入导航；权限点仍通过 `permissions` 返回。

6. VLM 核心请求 DTO 必填校验不完整。
   - 已补充生产录入、运输明细、运输单到仓日期、发票、形式发票转正、收款日期、销售分配经销商、配送经销商行等关键字段的 Bean Validation 注解。
   - 新增 DTO 校验测试覆盖关键必填约束。

## 新增/调整测试

- `VehInvoiceServiceImplTest`：覆盖发票草稿、首次确认推进、转正草稿、转正确认不推进生命周期。
- `VehiclePanoramaServiceImplTest`：覆盖草稿正式发票不算有效正式发票。
- `AuthServiceImplTest`：覆盖角色编码和授权菜单树。
- `VlmRequestValidationTest`：覆盖核心 VLM 请求必填校验。
- `MapperSqlSmokeTest`：使用 H2 MySQL 模式启动 Spring/MyBatis 上下文，真实执行发票列表、收款列表、用户角色编码 Mapper SQL。

## 未修改但建议关注

- 当前工作树是 detached HEAD，但提交点与本地 `check1` 分支一致；`check1` 分支本身在另一个本地工作树中检出。
- `docs/VLM-execution-plan.md` 在本次检查开始前已是修改状态，本次未处理该既有改动。
- D00001 中提到系统表统一 `t_sys_*`、菜单级授权、记录级留痕即可；当前代码仍保留早期 `docs/superpowers` 后台系统设计中的 `sys_*` RBAC 表、按钮级权限点和操作日志模块。这属于较大迁移范围，本次未做表结构/权限模型重构。
- 已补充 Spring Boot + H2 MySQL 模式 Mapper 冒烟，覆盖本次改动过的核心 SQL；但仍未连接真实 MySQL 执行完整 `init.sql` / `vlm_init.sql` / `data.sql` / `vlm_data.sql` 初始化脚本，生产库初始化建议在目标 MySQL 环境再跑一次。
