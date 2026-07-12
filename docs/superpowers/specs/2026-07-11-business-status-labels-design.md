# 业务状态中文展示设计

## 背景

车辆全生命周期业务接口当前返回了较多内部编码，例如 `DRAFT`、`CONFIRMED`、`PENDING_PAYMENT`、`IN_TRANSIT`。这些编码适合数据库存储、查询和状态流转，但不适合直接作为页面展示内容。项目已有生命周期、阶段状态、物流单据状态枚举的中文标签，同时发票、收款、配送、上牌和销售状态已经使用字典表，但字典项展示名称仍为英文，且现有标签没有统一注入接口响应。

## 目标

1. 保留现有业务编码字段，确保已有查询、筛选和状态流转兼容。
2. 在车辆全生命周期业务响应中补充对应的中文 `*Label` 字段。
3. 将动态业务状态的字典项展示名称维护为中文。
4. 对未知或未配置的编码提供原编码兜底，避免页面显示为空。
5. 不处理用户、角色、菜单、车型、经销商、内外饰颜色等系统管理对象的 `0/1` 启停状态。

## 范围

### 需要处理

- 车辆生命周期：`lifecycleStage` / `lifecycleStageLabel`
- 阶段记录：`stageStatus` / `stageStatusLabel`
- 物流单据：`orderStatus`、`listStatus`、`rowStatus` 及对应中文字段
- 动态业务状态：发票类型、收款状态、配送状态、上牌状态、销售状态及对应中文字段
- 车辆列表、车辆基本信息、阶段详情、发票列表、车辆全景、物流详情及嵌套响应
- `admin_system.sql` 中相关字典类型和字典项的中文展示名称

### 不需要处理

- 数据库存储编码和字段类型
- 请求参数中的业务编码
- 业务查询和生命周期推进条件
- 系统管理对象的 `status=0/1` 启停字段

## 方案

采用“编码字段 + 中文标签字段”的兼容方案。

### 固定枚举状态

复用现有 `StageStatus`、`OrderStatus`、`LifecycleStage` 的 `getLabel()`，由统一展示转换组件完成编码到中文的转换，不在业务服务中散落重复的 `if/else` 或 `switch`。

固定映射至少覆盖：

| 编码 | 中文 |
| --- | --- |
| `DRAFT` | 草稿 |
| `CONFIRMED` | 已确认 |
| `PENDING_OFFLINE` | 草稿（新录入） |
| `PENDING_INBOUND` | 待入库 |
| `PENDING_ALLOCATION` | 待分配 |
| `PENDING_INVOICE` | 待开票 |
| `PENDING_PAYMENT` | 待收款 |
| `PENDING_DELIVERY` | 待配送 |
| `PENDING_REGISTRATION` | 待上牌 |
| `COMPLETED` | 已完结 |

### 字典状态

沿用 `t_sys_dict_type` / `t_sys_dict_item` 作为动态状态主数据来源。统一转换组件按字典编码读取启用字典项并建立 `itemValue -> itemLabel` 映射；业务服务在列表或详情组装时复用映射，避免逐条查询。

当前字典展示名称调整为中文：

- `invoice_status`：正式发票、形式发票
- `payment_status`：已收款、已收款-中信保、未收款
- `delivery_status`：已配送、运输中
- `drosstech_status`：已上传、待上传
- `sales_status`：已分配、已交付

字典类型名称和备注同步改为中文，编码保持不变。

### 响应字段

现有编码字段继续返回；新增字段按原字段名加 `Label` 后缀。例如：

```json
{
  "lifecycleStage": "PENDING_PAYMENT",
  "lifecycleStageLabel": "待收款",
  "stageStatus": "DRAFT",
  "stageStatusLabel": "草稿",
  "paymentStatus": "UNPAID",
  "paymentStatusLabel": "未收款"
}
```

全景时间线中的 `stage` 保留编码，补充 `stageLabel`；已有 `name` 作为业务节点名称保留。

未知编码或停用字典项的 `*Label` 返回原编码；空值保持空值。

## 数据流

1. 数据库和请求层继续使用英文业务编码。
2. Mapper 查询返回现有响应编码字段。
3. 业务服务完成响应组装后调用统一展示转换组件。
4. 转换组件对固定枚举使用枚举标签，对动态状态使用字典映射。
5. Controller 对外返回同时包含编码和中文标签。

## 测试与验收

- 单元测试验证固定枚举、字典状态、未知编码和空值的转换结果。
- 业务服务测试验证车辆列表、阶段详情、发票/收款/上牌响应和车辆全景中的标签字段。
- SQL/种子数据检查验证相关字典项已为中文。
- 执行完整 Maven 测试，确保既有编码断言、查询筛选和生命周期流转不受影响。

## 兼容性

这是向后兼容的响应扩展：原字段名称和值不变，仅新增标签字段。前端可逐步切换到 `*Label` 字段；后端的请求参数、Mapper 条件和业务状态机不改变。
