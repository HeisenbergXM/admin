# 车辆数据修订主表列表与导出设计

**日期：** 2026-07-18
**分支：** `release2.2`
**状态：** 已确认

## 1. 背景与目标

管理员使用“车辆数据修订”页面处理确认后发现的数据错误。当前分页接口
`GET /api/vehicle-corrections` 仍复用普通车辆列表 DTO，只返回车辆基础信息，
无法在列表中直接查看车辆在生产、入库、销售、财务、配送和上牌阶段的完整业务数据。

本次调整目标：

1. 分页列表字段、显示名称和顺序与 `docs/Sample of Master Sheet.xlsx` 一致。
2. 新增列表导出能力，导出当前筛选条件下的全部车辆。
3. 导出文件完整保留模板中的两层表头、部门分组和合并单元格结构。
4. 列表与导出共用同一套数据查询和字段映射，避免两处结果不一致。
5. 保持现有车辆详情和修订接口行为不变，VIN 仍不可修改。

## 2. 模板基准

基准文件为 `docs/Sample of Master Sheet.xlsx`，工作表 `Sheet1` 的有效范围为
`A1:AS7`。模板定义 45 个展示列，字段表头位于第一行，部门维护分组位于第二行，
示例车辆数据从第三行开始。

实现时保持模板原始文字，包括大小写、空格、换行和既有拼写，例如：

- `NO.`
- `Date to Strogare Yard`
- `Trolly type\n4 units/ 6units`
- `Fully load or not`
- `Received date by Dealer `

模板中的示例车辆数据不会出现在实际导出文件中。

## 3. 方案选择

采用“专用平铺 DTO + 单次聚合查询 + 程序生成同版式 Excel”方案。

未采用的方案：

- 逐车复用全景详情查询：每辆车需要查询多个阶段表，批量导出会形成明显的 N+1 查询。
- 动态 `Map`：缺少字段类型和接口契约约束，不利于 OpenAPI 文档、前端联调及回归测试。

## 4. 接口设计

### 4.1 分页查询完整车辆列表

```http
GET /api/vehicle-corrections
```

权限保持为：

```text
vlm:vehicle-correction:list
```

请求参数继续使用 `VehicleQueryRequest`：

- `pageNum`
- `pageSize`
- `vin`
- `modelId`
- `modelName`
- `series`
- `lifecycleStage`
- `dealerId`

响应类型从 `PageResult<VehicleListResponse>` 调整为：

```text
PageResult<VehicleCorrectionListResponse>
```

`VehicleCorrectionListResponse` 是主表专用平铺 DTO：

- `id` 为车辆主键，仅用于打开修订详情，不作为主表展示列。
- `no` 对应 `NO.`，按当前筛选结果的全局分页序号生成。
- 其余属性与模板列一一对应。
- JSON 属性使用稳定的 camelCase 名称；OpenAPI 描述和前端列标题使用模板原始表头文字。

全局分页序号计算公式：

```text
(pageNum - 1) * pageSize + 当前页行号（从 1 开始）
```

默认排序保持为 `t_vehicle.id DESC`。

### 4.2 导出完整车辆列表

新增接口：

```http
GET /api/vehicle-corrections/export
```

新增权限：

```text
vlm:vehicle-correction:export
```

接口接受与分页列表相同的筛选参数，但忽略 `pageNum` 和 `pageSize`，导出当前筛选
条件下的全部车辆。导出记录顺序与列表一致，`NO.` 从 1 连续编号。

响应：

```text
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="vehicle-corrections-yyyyMMddHHmmss.xlsx"
```

导出权限作为“车辆数据修订”菜单下的按钮权限，仅分配给已有管理层角色，保持补救入口
的管理层访问边界。

## 5. 字段映射

| Excel 列 | 表头 | JSON 属性 | 数据来源 |
|---|---|---|---|
| A | NO. | `no` | 服务层生成 |
| B | MODEL | `model` | `t_md_model.model_name` |
| C | EXTERIOR COLOR | `exteriorColor` | `t_md_exterior_color.color_name` |
| D | INTERIOR COLOR | `interiorColor` | `t_md_interior_color.color_name` |
| E | VIN NUMBER | `vinNumber` | `t_vehicle.vin` |
| F | ENGINE NUMBER | `engineNumber` | `t_veh_production.engine_number` |
| G | MODEL CODE | `modelCode` | `t_md_model.model_code` |
| H | Year Make | `yearMake` | `t_veh_production.year_make` |
| I | Material | `material` | `t_veh_production.material` |
| J | Shipment | `shipment` | `t_veh_production.shipment` |
| K | Batch | `batch` | `t_veh_production.batch` |
| L | Offline EPMB | `offlineEpmb` | `t_veh_production.offline_epmb_date` |
| M | EPMB ok | `epmbOk` | `t_veh_production.epmb_ok_date` |
| N | Remark1 | `remark1` | `t_veh_production.remark1` |
| O | SAIC buy off | `saicBuyOff` | `t_veh_inbound.saic_buy_off_date` |
| P | Date to Strogare Yard | `dateToStorageYard` | `t_veh_inbound.date_to_storage_yard` |
| Q | remark2 | `remark2` | `t_veh_inbound.remark2` |
| R | Allocated Date | `allocatedDate` | `t_veh_allocation.allocated_date` |
| S | Dealer Code | `dealerCode` | `t_md_dealer.dealer_code` |
| T | Dealer | `dealer` | `t_md_dealer.dealer_name` |
| U | Remark3 | `remark3` | `t_veh_allocation.remark3` |
| V | Status1 | `status1` | 首次发票 `invoice_seq=1` 的类型显示值 |
| W | Invoice# | `invoiceNo1` | 首次发票 |
| X | Invoice Date | `invoiceDate1` | 首次发票 |
| Y | remark4 | `remark4` | 首次发票 |
| Z | Payment Date | `paymentDate` | `t_veh_payment.payment_date` |
| AA | Credit Full Payment Date | `creditFullPaymentDate` | `t_veh_payment.credit_full_payment_date` |
| AB | Payment Status | `paymentStatus` | 收款状态显示值 |
| AC | remark5 | `remark5` | `t_veh_payment.remark5` |
| AD | Status2 | `status2` | 第二次发票 `invoice_seq=2` 的类型显示值 |
| AE | Invoice# | `invoiceNo2` | 第二次发票 |
| AF | Invoice Date | `invoiceDate2` | 第二次发票 |
| AG | remark6 | `remark6` | 第二次发票 |
| AH | ETD to Dealer | `etdToDealer` | `t_veh_delivery.etd_to_dealer` |
| AI | ETA to Dealer | `etaToDealer` | `t_veh_delivery.eta_to_dealer` |
| AJ | Trolly type / 4 units/ 6units | `trollyType` | `t_veh_delivery.trolly_type` |
| AK | Fully load or not | `fullyLoad` | `t_veh_delivery.fully_load` 的显示值 |
| AL | Received date by Dealer | `receivedDateByDealer` | `t_veh_delivery.received_date` |
| AM | Delivery Status | `deliveryStatus` | 配送状态显示值 |
| AN | remark7 | `remark7` | `t_veh_delivery.remark7` |
| AO | Drosstech Status | `drosstechStatus` | Drosstech 状态显示值 |
| AP | Upload Date | `uploadDate` | `t_veh_registration.upload_date` |
| AQ | Registration | `registration` | `t_veh_registration.registration_date` |
| AR | Customer region | `customerRegion` | `t_veh_registration.customer_region` |
| AS | remark8 | `remark8` | `t_veh_registration.remark8` |

状态类字段输出业务字典显示值，而不是内部代码。日期字段使用 `yyyy-MM-dd`；无值字段保持为空。
`fullyLoad=true` 显示为 `Full`，`false` 显示为 `Not Full`，空值保持为空。

## 6. 查询设计

`VehicleMapper` 新增主表专用查询结果映射和可复用 SQL 片段：

1. 以 `t_vehicle` 为主表。
2. 左连接生产、入库、销售分配、收款、配送、上牌和主数据表。
3. 对发票表分别以 `invoice_seq=1` 和 `invoice_seq=2` 使用两个别名左连接，避免一车两票导致重复行。
4. 复用现有 `VehicleQueryRequest` 筛选条件。
5. 分页接口通过 MyBatis-Plus `Page` 执行同一查询。
6. 导出接口执行不分页版本，不逐车调用全景详情接口。

列表与导出必须经过同一个字段显示值转换方法，保证状态、布尔值和日期表现一致。

## 7. Excel 结构

导出工作表名称保持为 `Sheet1`，包含两行表头，数据从第三行开始。

第一行严格使用 45 个模板字段名。第二行保留以下部门分组及合并范围：

| 合并范围 | 部门分组文字 |
|---|---|
| B2:N2 | 生产部门维护 |
| O2:Q2 | 物流部门维护 |
| R2:U2 | 销售部门维护 |
| V2:Y2 | 财务部门第一次维护（发票种类） |
| Z2:AC2 | 财务部门第二次维护（收款状态） |
| AD2:AG2 | 财务部门第三次维护（如果第一次发票为 Proforma Invoice） |
| AH2:AN2 | 物流部门负责维护 |
| AO2:AS2 | 销售部门根据列X维护 |

导出使用 Apache POI/EasyExcel 所依赖的工作簿能力程序化创建表头、合并区域、列宽、换行、
边框和分组底色，不在运行时依赖 `docs` 目录文件。这样打包部署后仍能稳定导出，并避免模板
示例数据误入生产文件。

## 8. 错误处理与边界

- 筛选条件无匹配车辆时，仍返回包含完整两层表头的空 Excel。
- 某阶段尚无记录时，对应列返回空值，不丢弃车辆。
- 第二次发票不存在时，`Status2` 至 `remark6` 为空。
- 导出生成失败时通过现有全局异常机制返回系统错误，不返回半成品文件。
- 列表分页上限继续沿用现有 `PageRequest` 校验；导出不受分页参数限制。
- 导出数据量由后端一次查询并流式写入工作簿，避免在内存中构造全景对象树。

## 9. 测试策略

### Mapper 测试

- 验证 45 列跨阶段字段映射。
- 验证 `invoice_seq=1/2` 正确落入两组发票列且不产生重复车辆。
- 验证现有 VIN、车型、车系、阶段和经销商筛选条件。

### Service 测试

- 验证分页全局序号。
- 验证列表与导出的状态显示值、布尔显示值和日期格式一致。
- 验证导出忽略分页参数并保留其他筛选参数。

### Controller 与权限测试

- 验证列表响应类型和字段。
- 验证导出响应头、文件名和 MIME 类型。
- 验证无 `vlm:vehicle-correction:export` 权限时返回 403。

### Excel 回归测试

- 使用 POI 打开导出结果，验证工作表名、45 个第一行表头和顺序。
- 验证第二行部门文字及 8 个合并区域。
- 验证数据从第三行开始、空结果仍有两层表头。
- 验证日期单元格格式和示例数据未混入导出。

## 10. 文档与兼容性

- 更新 `docs/api-document.md`。
- 更新本次后端接口变更清单，补充响应字段调整和新增导出接口。
- `GET /api/vehicle-corrections/{vehicleId}` 与 `PUT /api/vehicle-corrections/{vehicleId}` 不变。
- 分页列表响应属于有意的契约调整，前端车辆数据修订页面需同步切换到新的字段名称和列顺序。
