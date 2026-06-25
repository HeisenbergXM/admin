# 需求来源说明 — D00001 车辆全生命周期管理系统

## 来源文件

- `sources/Sample of Master Sheet.xlsx`（原始 Excel 台账，复制自项目根目录 `Sample of Master Sheet.xlsx`）

## 背景

当前业务通过一张 Excel 主表（`Master Vin List (EV ICE)`）维护一辆车从**生产下线 → 运输入库 → 销售分配 → 财务开票/收款 → 配送经销商 → 上牌**的全过程数据。涉及生产、物流、销售、财务多个部门接力维护同一行记录。现需将该 Excel 台账升级为系统化维护，解决以下痛点：

- 多部门共用一张表，缺乏字段级权限与操作留痕
- 状态值（销售状态、发票状态、收款状态等）靠手工填写，口径不统一
- 生命周期阶段无卡控，易出现跳步、漏填、数据不一致
- 主数据（车型、颜色、经销商）散落在辅助 Sheet，靠手工复制

## Excel 结构解析

源文件含 4 个工作表：

| 工作表 | 作用 | 规模 |
|--------|------|------|
| `Master Vin List (EV ICE)` | 车辆全生命周期主台账 | 45 列（A~AS） |
| `DATA` | 车型/物料/颜色主数据 | ~59 行 |
| `Detail1` | 车辆明细透视（派生数据） | ~108 行 |
| `Dealer Code` | 经销商字典 | 33 家 |

### 主台账列 → 业务阶段映射

| 列范围 | 维护部门 | 业务阶段 | 关键字段 |
|--------|----------|----------|----------|
| B~N | 生产部门 | 车辆生产录入 | MODEL、EXTERIOR/INTERIOR COLOR、VIN、ENGINE NUMBER、MODEL CODE、Year Make、Material、Shipment、Batch、Offline EPMB、EPMB ok、Remark1 |
| O~Q | 物流部门 | 车厂 → 仓库 | SAIC buy off、Date to Storage Yard、remark2 |
| R~U | 销售部门 | 车辆销售（分配经销商） | Allocated Date、Dealer Code、Dealer、Remark3 |
| V~Y | 财务部门（第一次） | 发票确认（发票种类） | Status1（Invoiced / Proforma Invoiced）、Invoice#、Invoice Date、remark4 |
| Z~AC | 财务部门（第二次） | 收款确认 | Payment Date、Credit Full Payment Date、Payment Status、remark5 |
| AD~AG | 财务部门（第三次） | 形式发票转正式发票 | Status2、Invoice#、Invoice Date、remark6 |
| AH~AN | 物流部门 | 仓库 → 经销商配送 | ETD/ETA to Dealer、Trolly type、Fully load、Received date、Delivery Status、remark7 |
| AO~AS | 销售部门 | 车辆上牌 | Drosstech Status、Upload Date、Registration、Customer region、remark8 |

> 注：A 列为序号（NO.），系统中由自增主键/列表序号替代。

## 关键决策（已与用户确认）

1. **严格流程卡控**：生命周期阶段强制按序推进，前置阶段未完成不允许执行后续操作。
2. **保留两组发票记录**：形式发票（Proforma）转正式发票（Invoice）时，第一次开票（V/W/X）与转正记录（AD/AE/AF）两组数据均保留留痕。
3. **从 DATA 表拆分主数据**：车型、内饰颜色、外饰颜色拆分为独立主数据，录入车辆时引用选择。
