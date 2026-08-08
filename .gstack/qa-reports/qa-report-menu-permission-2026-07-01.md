# QA Report - 菜单权限半选回归

**日期**: 2026-07-01  
**项目**: admin-system  
**测试人员**: gstack /qa  
**范围**: D00001、D00002、superpowers 文档约束下的 RBAC 菜单授权回归

## 执行摘要

针对“菜单权限页面只有勾选某个页面的全部子项才会生效，不勾选全部就不会生效”的已知问题，补充了 mapper 层回归测试并完成修复。当前用户菜单查询会返回已授权菜单及其所有启用父级，因此角色只保存部分按钮/叶子权限时，登录菜单仍可构建出所属页面与目录。

## 文档对照

- D00001: RBAC 基于用户-角色-菜单，前端按菜单授权渲染。
- D00001: 菜单管理为目录/菜单两级，角色管理仅菜单级授权，不应要求全选按钮子项才显示页面。
- D00002: 前后端双校验，前端禁用/显隐仅作体验优化，后端仍需保证权限数据可正确生效。
- superpowers 设计: `/api/auth/info` 返回当前用户信息、权限与授权菜单，后端接口通过 `@PreAuthorize` 校验权限标识。

## 根因

角色授权半选时，前端树组件可能只提交叶子按钮权限，不提交半选父级页面。原 `MenuMapper.selectByUserId` 只返回 `sys_role_menu` 中直接保存的菜单；`AuthServiceImpl` 构建导航时又过滤按钮类型，导致父级页面不在集合内，菜单树为空或缺失。全选子项时父页面会变成 checked key 被保存，所以表现为“全选才生效”。

## 修复验证

### 红灯

```bash
C:\Users\arthu\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd -Dtest=MapperSqlSmokeTest#userMenusIncludeAncestorsWhenOnlyLeafPermissionIsAssigned test
```

结果: 失败，期望 `[100, 101, 102]`，实际仅 `[102]`。

### 绿灯

同一测试修复后通过：

```bash
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 全量回归

```bash
C:\Users\arthu\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd test
```

结果：

```bash
Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 结论

**状态**: 通过  
**建议**: 前端角色授权保存时仍建议同时提交 checked keys 与 half-checked keys；后端当前已能兼容历史或异常半选叶子数据。
