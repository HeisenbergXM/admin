# 后台管理系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个基于 SpringBoot + SpringSecurity + MyBatis + MySQL 的企业后台管理系统，提供用户管理、角色管理、菜单管理、登录登出及操作日志功能。

**Architecture:** 经典分层架构（Controller → Service → Mapper），采用 JWT 无状态认证，RBAC 权限模型，AOP + 自定义注解实现操作日志异步记录。

**Tech Stack:** SpringBoot 2.7.x, SpringSecurity, JWT (jjwt 0.9.1), MyBatis-Plus 3.5.x, MySQL 8.0, Knife4j 4.x, BCryptPasswordEncoder, Lombok

## Global Constraints

- 项目包路径: `com.company.admin`
- 所有表带 `deleted` 字段做逻辑删除
- 密码使用 BCrypt 加密，新增用户默认密码 `123456`
- JWT Token 过期时间 24 小时
- 统一响应格式: `{ "code": 200, "data": {}, "message": "..." }`
- 分页响应: `{ "code": 200, "data": { "list": [], "total": N, "pageNum": N, "pageSize": N } }`
- API 路径前缀: `/api/`
- 所有接口返回 JSON，Security 异常也返回 JSON

---

## Task 1: 项目脚手架与数据库初始化

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/company/admin/AdminApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/sql/init.sql`

**Interfaces:**
- Produces: Maven 项目可编译运行，数据库表结构就绪

### 步骤

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>
    <groupId>com.company</groupId>
    <artifactId>admin</artifactId>
    <version>1.0.0</version>
    <name>admin-system</name>
    <description>企业内部后台管理系统</description>

    <properties>
        <java.version>1.8</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jjwt.version>0.9.1</jjwt.version>
        <knife4j.version>4.3.0</knife4j.version>
    </properties>

    <dependencies>
        <!-- SpringBoot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- SpringSecurity -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <!-- Spring Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <!-- AOP -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <!-- Knife4j -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类 `AdminApplication.java`**

```java
package com.company.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 `application.yml`**

```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/admin_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

jwt:
  secret: YWRtaW4tc3lzdGVtLWp3dC1zZWNyZXQta2V5LTIwMjY=
  expiration: 86400000

knife4j:
  enable: true
```

- [ ] **Step 4: 创建数据库初始化脚本 `src/main/resources/sql/init.sql`**

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS admin_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE admin_system;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    avatar VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID（0为顶级）',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type TINYINT NOT NULL COMMENT '类型：1目录 2菜单 3按钮',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    permission VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB COMMENT='菜单/权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID'
) ENGINE=InnoDB COMMENT='用户-角色关联表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID'
) ENGINE=InnoDB COMMENT='角色-菜单关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    operation VARCHAR(50) DEFAULT NULL COMMENT '操作描述',
    method VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    params TEXT DEFAULT NULL COMMENT '请求参数',
    result TEXT DEFAULT NULL COMMENT '返回结果',
    ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) ENGINE=InnoDB COMMENT='操作日志表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) DEFAULT NULL COMMENT '登录用户名',
    login_type TINYINT NOT NULL COMMENT '类型：1登录 2登出',
    ip VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    location VARCHAR(50) DEFAULT NULL COMMENT '登录地点',
    status TINYINT NOT NULL COMMENT '状态：0失败 1成功',
    message VARCHAR(200) DEFAULT NULL COMMENT '提示消息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间'
) ENGINE=InnoDB COMMENT='登录日志表';
```

- [ ] **Step 5: 验证项目可编译运行**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 6: 提交**

```bash
git add pom.xml src/main/java/com/company/admin/AdminApplication.java src/main/resources/application.yml src/main/resources/sql/init.sql
git commit -m "feat: project scaffolding, pom.xml, application.yml, database init script"
```

---

## Task 2: 公共模块 — 统一响应、异常处理、错误码

**Files:**
- Create: `src/main/java/com/company/admin/common/Result.java`
- Create: `src/main/java/com/company/admin/common/PageResult.java`
- Create: `src/main/java/com/company/admin/common/ErrorCode.java`
- Create: `src/main/java/com/company/admin/common/BusinessException.java`
- Create: `src/main/java/com/company/admin/common/GlobalExceptionHandler.java`

**Interfaces:**
- Consumes: (none — 这是第一个代码模块)
- Produces:
  - `Result<T>` — 统一响应包装类，`static <T> Result<T> success(T data)` / `static Result<Void> success()` / `static Result<Void> error(int code, String message)`
  - `PageResult<T>` — 分页响应，字段 `List<T> list`, `long total`, `int pageNum`, `int pageSize`
  - `ErrorCode` — 错误码枚举，包含所有设计的错误码常量
  - `BusinessException` — 业务异常，构造器 `BusinessException(ErrorCode)` 和 `BusinessException(int code, String message)`
  - `GlobalExceptionHandler` — `@RestControllerAdvice`，处理 BusinessException / MethodArgumentNotValidException / AccessDeniedException / AuthenticationException / Exception

### 步骤

- [ ] **Step 1: 创建 `Result.java`**

```java
package com.company.admin.common;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private T data;
    private String message;

    private Result() {}

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.data = data;
        r.message = "操作成功";
        return r;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.data = data;
        r.message = message;
        return r;
    }

    public static Result<Void> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
}
```

- [ ] **Step 2: 创建 `PageResult.java`**

```java
package com.company.admin.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;

    public PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
```

- [ ] **Step 3: 创建 `ErrorCode.java`**

```java
package com.company.admin.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),

    // 用户模块 1xxx
    USERNAME_PASSWORD_ERROR(1001, "用户名或密码错误"),
    USER_DISABLED(1002, "用户已禁用"),
    USERNAME_EXISTS(1003, "用户名已存在"),
    USER_OLD_PASSWORD_ERROR(1004, "原密码错误"),

    // 角色模块 2xxx
    ROLE_CODE_EXISTS(2001, "角色编码已存在"),

    // 菜单模块 3xxx
    MENU_NAME_EXISTS(3001, "菜单名称已存在"),
    MENU_HAS_CHILDREN(3002, "存在子菜单，无法删除"),

    // Token 模块 4xxx
    TOKEN_EXPIRED(4001, "Token 已过期"),
    TOKEN_INVALID(4002, "Token 无效"),

    // 系统错误 5xxx
    SYSTEM_ERROR(5000, "系统内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

- [ ] **Step 4: 创建 `BusinessException.java`**

```java
package com.company.admin.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
```

- [ ] **Step 5: 创建 `GlobalExceptionHandler.java`**

```java
package com.company.admin.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ":" + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.error(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        return Result.error(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR);
    }
}
```

- [ ] **Step 6: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add src/main/java/com/company/admin/common/
git commit -m "feat: add common module — Result, PageResult, ErrorCode, BusinessException, GlobalExceptionHandler"
```

---

## Task 3: 实体类

**Files:**
- Create: `src/main/java/com/company/admin/entity/User.java`
- Create: `src/main/java/com/company/admin/entity/Role.java`
- Create: `src/main/java/com/company/admin/entity/Menu.java`
- Create: `src/main/java/com/company/admin/entity/UserRole.java`
- Create: `src/main/java/com/company/admin/entity/RoleMenu.java`
- Create: `src/main/java/com/company/admin/entity/OperationLog.java`
- Create: `src/main/java/com/company/admin/entity/LoginLog.java`

**Interfaces:**
- Consumes: (none)
- Produces: 7 个实体类，MyBatis-Plus 注解映射数据库表

### 步骤

- [ ] **Step 1: 创建 `User.java`**

```java
package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
    private String avatar;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: 创建 `Role.java`**

```java
package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 3: 创建 `Menu.java`**

```java
package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;
    private String menuName;
    private Integer menuType;   // 1目录 2菜单 3按钮
    private String path;
    private String permission;
    private String icon;
    private Integer sortOrder;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 4: 创建 `UserRole.java`**

```java
package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class UserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long roleId;
}
```

- [ ] **Step 5: 创建 `RoleMenu.java`**

```java
package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_role_menu")
public class RoleMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;
    private Long menuId;
}
```

- [ ] **Step 6: 创建 `OperationLog.java`**

```java
package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String result;
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 7: 创建 `LoginLog.java`**

```java
package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class LoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private Integer loginType;   // 1登录 2登出
    private String ip;
    private String location;
    private Integer status;      // 0失败 1成功
    private String message;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 8: 创建 MyBatis-Plus 自动填充处理器 `src/main/java/com/company/admin/config/MyBatisConfig.java`**

```java
package com.company.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDateTime;

@Configuration
public class MyBatisConfig {

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

- [ ] **Step 9: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 10: 提交**

```bash
git add src/main/java/com/company/admin/entity/ src/main/java/com/company/admin/config/MyBatisConfig.java
git commit -m "feat: add entity classes and MyBatis auto-fill config"
```

---

## Task 4: DTO 类

**Files:**
- Create: `src/main/java/com/company/admin/dto/request/LoginRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/UserQueryRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/UserCreateRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/UserUpdateRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/UserRoleRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/RoleCreateRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/RoleMenuRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/MenuCreateRequest.java`
- Create: `src/main/java/com/company/admin/dto/response/LoginResponse.java`
- Create: `src/main/java/com/company/admin/dto/response/UserInfoResponse.java`
- Create: `src/main/java/com/company/admin/dto/response/MenuTreeResponse.java`

**Interfaces:**
- Consumes: (none)
- Produces: 所有请求/响应 DTO 类

### 步骤

- [ ] **Step 1: 创建请求 DTO — `src/main/java/com/company/admin/dto/request/`**

```java
// LoginRequest.java
package com.company.admin.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

```java
// UserQueryRequest.java
package com.company.admin.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest {
    private String username;
    private String nickname;
    private Integer status;
}
```

```java
// UserCreateRequest.java
package com.company.admin.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50")
    private String username;
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长50个字符")
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
}
```

```java
// UserUpdateRequest.java
package com.company.admin.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserUpdateRequest {
    @NotNull(message = "用户ID不能为空")
    private Long id;
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长50个字符")
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
}
```

```java
// UserRoleRequest.java
package com.company.admin.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserRoleRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    private List<Long> roleIds;
}
```

```java
// RoleCreateRequest.java
package com.company.admin.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RoleCreateRequest {
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最长50个字符")
    private String roleName;
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最长50个字符")
    private String roleCode;
    @Size(max = 200, message = "描述最长200个字符")
    private String description;
    private Integer status;
}
```

```java
// RoleMenuRequest.java
package com.company.admin.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class RoleMenuRequest {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
    private List<Long> menuIds;
}
```

```java
// MenuCreateRequest.java
package com.company.admin.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class MenuCreateRequest {
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称最长50个字符")
    private String menuName;
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;
    private String path;
    private String permission;
    private String icon;
    private Integer sortOrder;
    private Integer status;
}
```

```java
// PageRequest.java (基类)
package com.company.admin.dto.request;

import lombok.Data;

@Data
public class PageRequest {
    private int pageNum = 1;
    private int pageSize = 10;
}
```

- [ ] **Step 2: 创建响应 DTO — `src/main/java/com/company/admin/dto/response/`**

```java
// LoginResponse.java
package com.company.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
}
```

```java
// UserInfoResponse.java
package com.company.admin.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> roles;
    private Set<String> permissions;
}
```

```java
// MenuTreeResponse.java
package com.company.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuTreeResponse {
    private Long id;
    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String path;
    private String permission;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private List<MenuTreeResponse> children;
}
```

- [ ] **Step 3: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/company/admin/dto/
git commit -m "feat: add request and response DTO classes"
```

---

## Task 5: Mapper 层

**Files:**
- Create: `src/main/java/com/company/admin/mapper/UserMapper.java`
- Create: `src/main/java/com/company/admin/mapper/RoleMapper.java`
- Create: `src/main/java/com/company/admin/mapper/MenuMapper.java`
- Create: `src/main/java/com/company/admin/mapper/UserRoleMapper.java`
- Create: `src/main/java/com/company/admin/mapper/RoleMenuMapper.java`
- Create: `src/main/java/com/company/admin/mapper/OperationLogMapper.java`
- Create: `src/main/java/com/company/admin/mapper/LoginLogMapper.java`
- Create: `src/main/resources/mapper/UserMapper.xml`
- Create: `src/main/resources/mapper/RoleMapper.xml`
- Create: `src/main/resources/mapper/MenuMapper.xml`

**Interfaces:**
- Consumes: Entity 类
- Produces:
  - `UserMapper` — extends `BaseMapper<User>`, 方法 `List<String> selectPermissionsByUserId(Long userId)`, `List<Long> selectRoleIdsByUserId(Long userId)`
  - `RoleMapper` — extends `BaseMapper<Role>`, 方法 `List<Long> selectMenuIdsByRoleId(Long roleId)`
  - `MenuMapper` — extends `BaseMapper<Menu>`, 方法 `List<Menu> selectAllEnabled()`, `List<Menu> selectByUserId(Long userId)`
  - `UserRoleMapper` — extends `BaseMapper<UserRole>`
  - `RoleMenuMapper` — extends `BaseMapper<RoleMenu>`
  - `OperationLogMapper` — extends `BaseMapper<OperationLog>`
  - `LoginLogMapper` — extends `BaseMapper<LoginLog>`

### 步骤

- [ ] **Step 1: 创建所有 Mapper 接口**

```java
// UserMapper.java
package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
```

```java
// RoleMapper.java
package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.Role;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
```

```java
// MenuMapper.java
package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.Menu;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {

    List<Menu> selectAllEnabled();

    List<Menu> selectByUserId(@Param("userId") Long userId);
}
```

```java
// UserRoleMapper.java
package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.UserRole;

public interface UserRoleMapper extends BaseMapper<UserRole> {
}
```

```java
// RoleMenuMapper.java
package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.RoleMenu;

public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
}
```

```java
// OperationLogMapper.java
package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.OperationLog;

public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
```

```java
// LoginLogMapper.java
package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.LoginLog;

public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
```

- [ ] **Step 2: 创建 MyBatis XML 映射文件**

```xml
<!-- src/main/resources/mapper/UserMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.company.admin.mapper.UserMapper">

    <select id="selectPermissionsByUserId" resultType="java.lang.String">
        SELECT DISTINCT m.permission
        FROM sys_user_role ur
        INNER JOIN sys_role r ON ur.role_id = r.id AND r.status = 1 AND r.deleted = 0
        INNER JOIN sys_role_menu rm ON r.id = rm.role_id
        INNER JOIN sys_menu m ON rm.menu_id = m.id AND m.status = 1 AND m.deleted = 0
        WHERE ur.user_id = #{userId}
          AND m.permission IS NOT NULL
          AND m.permission != ''
    </select>

    <select id="selectRoleIdsByUserId" resultType="java.lang.Long">
        SELECT role_id FROM sys_user_role WHERE user_id = #{userId}
    </select>

</mapper>
```

```xml
<!-- src/main/resources/mapper/RoleMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.company.admin.mapper.RoleMapper">

    <select id="selectMenuIdsByRoleId" resultType="java.lang.Long">
        SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}
    </select>

</mapper>
```

```xml
<!-- src/main/resources/mapper/MenuMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.company.admin.mapper.MenuMapper">

    <select id="selectAllEnabled" resultType="com.company.admin.entity.Menu">
        SELECT * FROM sys_menu
        WHERE status = 1 AND deleted = 0
        ORDER BY sort_order ASC
    </select>

    <select id="selectByUserId" resultType="com.company.admin.entity.Menu">
        SELECT DISTINCT m.*
        FROM sys_user_role ur
        INNER JOIN sys_role r ON ur.role_id = r.id AND r.status = 1 AND r.deleted = 0
        INNER JOIN sys_role_menu rm ON r.id = rm.role_id
        INNER JOIN sys_menu m ON rm.menu_id = m.id AND m.status = 1 AND m.deleted = 0
        WHERE ur.user_id = #{userId}
        ORDER BY m.sort_order ASC
    </select>

</mapper>
```

- [ ] **Step 3: 创建 `@MapperScan` 配置**

确保 `AdminApplication.java` 添加 `@MapperScan`:

```java
package com.company.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.company.admin.mapper")
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

- [ ] **Step 4: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/company/admin/mapper/ src/main/resources/mapper/ src/main/java/com/company/admin/AdminApplication.java
git commit -m "feat: add mapper interfaces and MyBatis XML mapping files"
```

---

## Task 6: SpringSecurity + JWT 模块

**Files:**
- Create: `src/main/java/com/company/admin/security/JwtTokenUtil.java`
- Create: `src/main/java/com/company/admin/security/CustomUserDetailsService.java`
- Create: `src/main/java/com/company/admin/security/JwtAuthenticationFilter.java`
- Create: `src/main/java/com/company/admin/security/CustomAccessDeniedHandler.java`
- Create: `src/main/java/com/company/admin/security/CustomAuthEntryPoint.java`
- Create: `src/main/java/com/company/admin/security/CustomLogoutHandler.java`
- Create: `src/main/java/com/company/admin/config/SecurityConfig.java`

**Interfaces:**
- Consumes: `UserMapper`, `RoleMapper`, `MenuMapper`
- Produces:
  - `JwtTokenUtil` — `String generateToken(String username)`, `String getUsernameFromToken(String token)`, `boolean validateToken(String token)`
  - `CustomUserDetailsService` — implements `UserDetailsService`, 加载用户+角色+权限
  - `JwtAuthenticationFilter` — extends `OncePerRequestFilter`
  - `SecurityConfig` — 完整 Security 配置，配置白名单、过滤器、异常处理

### 步骤

- [ ] **Step 1: 创建 `JwtTokenUtil.java`**

```java
package com.company.admin.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("TOKEN_EXPIRED");
        } catch (JwtException e) {
            throw new RuntimeException("TOKEN_INVALID");
        }
    }
}
```

- [ ] **Step 2: 创建 `CustomUserDetailsService.java`**

```java
package com.company.admin.security;

import com.company.admin.entity.User;
import com.company.admin.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已禁用");
        }

        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }
}
```

- [ ] **Step 3: 创建 `JwtAuthenticationFilter.java`**

```java
package com.company.admin.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
                                    CustomUserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Token 无效，不设置认证信息，让后续的 EntryPoint 处理
        }
        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 4: 创建三个 Handler**

```java
// CustomAccessDeniedHandler.java
package com.company.admin.security;

import com.company.admin.common.ErrorCode;
import com.company.admin.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(ErrorCode.FORBIDDEN)));
    }
}
```

```java
// CustomAuthEntryPoint.java
package com.company.admin.security;

import com.company.admin.common.ErrorCode;
import com.company.admin.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(ErrorCode.UNAUTHORIZED)));
    }
}
```

```java
// CustomLogoutHandler.java
package com.company.admin.security;

import com.company.admin.entity.LoginLog;
import com.company.admin.mapper.LoginLogMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final LoginLogMapper loginLogMapper;

    public CustomLogoutHandler(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        if (authentication != null) {
            LoginLog log = new LoginLog();
            log.setUsername(authentication.getName());
            log.setLoginType(2);
            log.setIp(getIp(request));
            log.setStatus(1);
            log.setMessage("登出成功");
            log.setCreateTime(LocalDateTime.now());
            loginLogMapper.insert(log);
        }
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

- [ ] **Step 5: 创建 `SecurityConfig.java`**

```java
package com.company.admin.config;

import com.company.admin.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthEntryPoint authEntryPoint;
    private final CustomLogoutHandler logoutHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          CustomAuthEntryPoint authEntryPoint,
                          CustomLogoutHandler logoutHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authEntryPoint = authEntryPoint;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/auth/login").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/knife4j/**",
                             "/doc.html", "/webjars/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authEntryPoint)
            .and()
            .logout()
                .logoutUrl("/api/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((req, resp, auth) -> {
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"code\":200,\"message\":\"登出成功\"}");
                })
            .and()
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

- [ ] **Step 6: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add src/main/java/com/company/admin/security/ src/main/java/com/company/admin/config/SecurityConfig.java
git commit -m "feat: add SpringSecurity + JWT authentication module"
```

---

## Task 7: 菜单管理模块

**Files:**
- Create: `src/main/java/com/company/admin/service/MenuService.java`
- Create: `src/main/java/com/company/admin/service/impl/MenuServiceImpl.java`
- Create: `src/main/java/com/company/admin/controller/MenuController.java`

**Interfaces:**
- Consumes: `MenuMapper`, `RoleMenuMapper`, `MenuTreeResponse`, `MenuCreateRequest`
- Produces: `MenuService` — `List<MenuTreeResponse> getMenuTree()`, `List<Menu> getAllMenus()`, `Menu getById(Long id)`, `void create(MenuCreateRequest req)`, `void update(MenuCreateRequest req)`, `void delete(Long id)`
- Controller: `MenuController` — `/api/menu/**` 全部受 `@PreAuthorize` 保护

### 步骤

- [ ] **Step 1: 创建 `MenuService.java`**

```java
package com.company.admin.service;

import com.company.admin.dto.request.MenuCreateRequest;
import com.company.admin.dto.response.MenuTreeResponse;
import com.company.admin.entity.Menu;
import java.util.List;

public interface MenuService {
    List<MenuTreeResponse> getMenuTree();
    List<Menu> getAllMenus();
    Menu getById(Long id);
    void create(MenuCreateRequest request);
    void update(MenuCreateRequest request);
    void delete(Long id);
}
```

- [ ] **Step 2: 创建 `MenuServiceImpl.java`**

```java
package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.MenuCreateRequest;
import com.company.admin.dto.response.MenuTreeResponse;
import com.company.admin.entity.Menu;
import com.company.admin.mapper.MenuMapper;
import com.company.admin.mapper.RoleMenuMapper;
import com.company.admin.service.MenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    public MenuServiceImpl(MenuMapper menuMapper, RoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<MenuTreeResponse> getMenuTree() {
        List<Menu> allMenus = menuMapper.selectAllEnabled();
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<Menu> getAllMenus() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSortOrder));
    }

    @Override
    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    @Transactional
    public void create(MenuCreateRequest request) {
        // 检查菜单名称是否已存在
        Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getMenuName, request.getMenuName()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.MENU_NAME_EXISTS);
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(request, menu);
        if (request.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuMapper.insert(menu);
    }

    @Override
    @Transactional
    public void update(MenuCreateRequest request) {
        Menu menu = menuMapper.selectById(request.getParentId()); // parentId here is actually the menu id being updated
        // Note: We need to handle this differently. Let's adjust - MenuCreateRequest needs an id field for updates.
        // For now, use the request's parentId as the actual id for update context.
        // Actually, let's treat this properly — the request for update should have an id.
        // We'll handle this in the controller layer.
        BeanUtils.copyProperties(request, menu);
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有子菜单
        Long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.MENU_HAS_CHILDREN);
        }
        // 删除角色-菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<com.company.admin.entity.RoleMenu>()
                        .eq(com.company.admin.entity.RoleMenu::getMenuId, id));
        // 逻辑删除菜单
        menuMapper.deleteById(id);
    }

    private List<MenuTreeResponse> buildTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .map(m -> {
                    MenuTreeResponse node = new MenuTreeResponse();
                    BeanUtils.copyProperties(m, node);
                    node.setChildren(buildTree(menus, m.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }
}
```

等一下，MenuServiceImpl 的 update 方法设计有问题。让我重新设计——需要为更新单独处理 id。让我在 MenuCreateRequest 中加一个 id 字段，或者在 Controller 中处理。

实际上更简洁的做法是让 MenuCreateRequest 仅用于创建，更新时在 Controller 中从路径参数获取 id。让我重写 MenuServiceImpl:

```java
package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.MenuCreateRequest;
import com.company.admin.dto.response.MenuTreeResponse;
import com.company.admin.entity.Menu;
import com.company.admin.entity.RoleMenu;
import com.company.admin.mapper.MenuMapper;
import com.company.admin.mapper.RoleMenuMapper;
import com.company.admin.service.MenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    public MenuServiceImpl(MenuMapper menuMapper, RoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<MenuTreeResponse> getMenuTree() {
        List<Menu> allMenus = menuMapper.selectAllEnabled();
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<Menu> getAllMenus() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSortOrder));
    }

    @Override
    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    @Transactional
    public void create(MenuCreateRequest request) {
        Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getMenuName, request.getMenuName()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.MENU_NAME_EXISTS);
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(request, menu);
        if (request.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuMapper.insert(menu);
    }

    @Override
    @Transactional
    public void update(Long id, MenuCreateRequest request) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(400, "菜单不存在");
        }
        Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getMenuName, request.getMenuName())
                        .ne(Menu::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.MENU_NAME_EXISTS);
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(request, menu);
        menu.setId(id);
        if (request.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.MENU_HAS_CHILDREN);
        }
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getMenuId, id));
        menuMapper.deleteById(id);
    }

    private List<MenuTreeResponse> buildTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .map(m -> {
                    MenuTreeResponse node = new MenuTreeResponse();
                    BeanUtils.copyProperties(m, node);
                    node.setChildren(buildTree(menus, m.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }
}
```

同时更新 MenuService 接口的 update 方法签名:

```java
// 更新 MenuService.java
package com.company.admin.service;

import com.company.admin.dto.request.MenuCreateRequest;
import com.company.admin.dto.response.MenuTreeResponse;
import com.company.admin.entity.Menu;
import java.util.List;

public interface MenuService {
    List<MenuTreeResponse> getMenuTree();
    List<Menu> getAllMenus();
    Menu getById(Long id);
    void create(MenuCreateRequest request);
    void update(Long id, MenuCreateRequest request);
    void delete(Long id);
}
```

- [ ] **Step 3: 创建 `MenuController.java`（不含 @OperationLog，Task 11 后补加）**

```java
package com.company.admin.controller;

import com.company.admin.common.Result;
import com.company.admin.dto.request.MenuCreateRequest;
import com.company.admin.entity.Menu;
import com.company.admin.service.MenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result<?> list() {
        return Result.success(menuService.getAllMenus());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result<?> tree() {
        return Result.success(menuService.getMenuTree());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result<Menu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:menu:add')")
    public Result<Void> create(@Valid @RequestBody MenuCreateRequest request) {
        menuService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:edit')")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody MenuCreateRequest request) {
        menuService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
```

注意: `@OperationLog` 注解在 Task 11 创建后，通过 Task 11 Step 4 统一添加到所有 Controller 上。

- [ ] **Step 4: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/company/admin/service/MenuService.java src/main/java/com/company/admin/service/impl/MenuServiceImpl.java src/main/java/com/company/admin/controller/MenuController.java
git commit -m "feat: add menu management module — service and controller"
```

---

## Task 8: 角色管理模块

**Files:**
- Create: `src/main/java/com/company/admin/service/RoleService.java`
- Create: `src/main/java/com/company/admin/service/impl/RoleServiceImpl.java`
- Create: `src/main/java/com/company/admin/controller/RoleController.java`

**Interfaces:**
- Consumes: `RoleMapper`, `RoleMenuMapper`, `UserRoleMapper`, `RoleCreateRequest`, `RoleMenuRequest`
- Produces: `RoleService` — `List<Role> list()`, `Role getById(Long id)`, `void create(RoleCreateRequest req)`, `void update(RoleCreateRequest req)`, `void delete(Long id)`, `List<Long> getRoleMenuIds(Long roleId)`, `void assignMenus(RoleMenuRequest req)`

### 步骤

- [ ] **Step 1: 创建 `RoleService.java`**

```java
package com.company.admin.service;

import com.company.admin.dto.request.RoleCreateRequest;
import com.company.admin.dto.request.RoleMenuRequest;
import com.company.admin.entity.Role;
import java.util.List;

public interface RoleService {
    List<Role> list();
    Role getById(Long id);
    void create(RoleCreateRequest request);
    void update(Long id, RoleCreateRequest request);
    void delete(Long id);
    List<Long> getRoleMenuIds(Long roleId);
    void assignMenus(RoleMenuRequest request);
}
```

- [ ] **Step 2: 创建 `RoleServiceImpl.java`**

```java
package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.RoleCreateRequest;
import com.company.admin.dto.request.RoleMenuRequest;
import com.company.admin.entity.Role;
import com.company.admin.entity.RoleMenu;
import com.company.admin.entity.UserRole;
import com.company.admin.mapper.RoleMapper;
import com.company.admin.mapper.RoleMenuMapper;
import com.company.admin.mapper.UserRoleMapper;
import com.company.admin.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper,
                           UserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public List<Role> list() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<Role>().orderByAsc(Role::getCreateTime));
    }

    @Override
    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    @Transactional
    public void create(RoleCreateRequest request) {
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, request.getRoleCode()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        roleMapper.insert(role);
    }

    @Override
    @Transactional
    public void update(Long id, RoleCreateRequest request) {
        Role existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(400, "角色不存在");
        }
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, request.getRoleCode())
                        .ne(Role::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        role.setId(id);
        roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 删除角色-菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        // 删除用户-角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        roleMapper.deleteById(id);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignMenus(RoleMenuRequest request) {
        // 删除旧关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, request.getRoleId()));
        // 插入新关联
        if (request.getMenuIds() != null && !request.getMenuIds().isEmpty()) {
            List<RoleMenu> roleMenus = request.getMenuIds().stream()
                    .map(menuId -> {
                        RoleMenu rm = new RoleMenu();
                        rm.setRoleId(request.getRoleId());
                        rm.setMenuId(menuId);
                        return rm;
                    })
                    .collect(Collectors.toList());
            for (RoleMenu rm : roleMenus) {
                roleMenuMapper.insert(rm);
            }
        }
    }
}
```

- [ ] **Step 3: 创建 `RoleController.java`**

```java
package com.company.admin.controller;

import com.company.admin.common.Result;
import com.company.admin.dto.request.RoleCreateRequest;
import com.company.admin.dto.request.RoleMenuRequest;
import com.company.admin.entity.Role;
import com.company.admin.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<?> list() {
        return Result.success(roleService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:add')")
    public Result<Void> create(@Valid @RequestBody RoleCreateRequest request) {
        roleService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody RoleCreateRequest request) {
        roleService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<?> getRoleMenus(@PathVariable Long id) {
        return Result.success(roleService.getRoleMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('sys:role:menu')")
    public Result<Void> assignMenus(@PathVariable Long id,
                                    @Valid @RequestBody RoleMenuRequest request) {
        request.setRoleId(id);
        roleService.assignMenus(request);
        return Result.success();
    }
}
```

- [ ] **Step 4: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/company/admin/service/RoleService.java src/main/java/com/company/admin/service/impl/RoleServiceImpl.java src/main/java/com/company/admin/controller/RoleController.java
git commit -m "feat: add role management module — service and controller"
```

---

## Task 9: 用户管理模块

**Files:**
- Create: `src/main/java/com/company/admin/service/UserService.java`
- Create: `src/main/java/com/company/admin/service/impl/UserServiceImpl.java`
- Create: `src/main/java/com/company/admin/controller/UserController.java`

**Interfaces:**
- Consumes: `UserMapper`, `UserRoleMapper`, `RoleMapper`, `PasswordEncoder`, DTO 类
- Produces:
  - `UserService` — `PageResult<User> page(UserQueryRequest req)`, `User getById(Long id)`, `void create(UserCreateRequest req)`, `void update(UserUpdateRequest req)`, `void delete(Long id)`, `List<Long> getUserRoleIds(Long userId)`, `void assignRoles(UserRoleRequest req)`, `void resetPassword(Long id)`, `void updateProfile(Long userId, UserUpdateRequest req)`, `void updatePassword(Long userId, String oldPwd, String newPwd)`

### 步骤

- [ ] **Step 1: 创建 `UserService.java`**

```java
package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.*;
import com.company.admin.entity.User;
import java.util.List;

public interface UserService {
    PageResult<User> page(UserQueryRequest request);
    User getById(Long id);
    void create(UserCreateRequest request);
    void update(UserUpdateRequest request);
    void delete(Long id);
    List<Long> getUserRoleIds(Long userId);
    void assignRoles(UserRoleRequest request);
    void resetPassword(Long id);
    void updateProfile(Long userId, UserUpdateRequest request);
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
```

- [ ] **Step 2: 创建 `UserServiceImpl.java`**

```java
package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.*;
import com.company.admin.entity.User;
import com.company.admin.entity.UserRole;
import com.company.admin.mapper.UserMapper;
import com.company.admin.mapper.UserRoleMapper;
import com.company.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<User> page(UserQueryRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(User::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getNickname())) {
            wrapper.like(User::getNickname, request.getNickname());
        }
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }
        wrapper.orderByAsc(User::getCreateTime);
        Page<User> page = userMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional
    public void create(UserCreateRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.insert(user);
    }

    @Override
    @Transactional
    public void update(UserUpdateRequest request) {
        User user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        User updateUser = new User();
        BeanUtils.copyProperties(request, updateUser);
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        userMapper.deleteById(id);
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    @Transactional
    public void assignRoles(UserRoleRequest request) {
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, request.getUserId()));
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<UserRole> userRoles = request.getRoleIds().stream()
                    .map(roleId -> {
                        UserRole ur = new UserRole();
                        ur.setUserId(request.getUserId());
                        ur.setRoleId(roleId);
                        return ur;
                    })
                    .collect(Collectors.toList());
            for (UserRole ur : userRoles) {
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setNickname(request.getNickname());
        updateUser.setEmail(request.getEmail());
        updateUser.setPhone(request.getPhone());
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
```

- [ ] **Step 3: 创建 `UserController.java`**

```java
package com.company.admin.controller;

import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.*;
import com.company.admin.entity.User;
import com.company.admin.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<PageResult<User>> list(UserQueryRequest request) {
        return Result.success(userService.page(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:add')")
    public Result<Void> create(@Valid @RequestBody UserCreateRequest request) {
        userService.create(request);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<Void> update(@Valid @RequestBody UserUpdateRequest request) {
        userService.update(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<?> getUserRoles(@PathVariable Long id) {
        return Result.success(userService.getUserRoleIds(id));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:user:role')")
    public Result<Void> assignRoles(@PathVariable Long id,
                                    @Valid @RequestBody UserRoleRequest request) {
        request.setUserId(id);
        userService.assignRoles(request);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('sys:user:resetPwd')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication authentication,
                                      @Valid @RequestBody UserUpdateRequest request) {
        // 从当前登录用户获取 userId
        Long userId = getCurrentUserId(authentication);
        userService.updateProfile(userId, request);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(Authentication authentication,
                                       @RequestBody PasswordUpdateRequest request) {
        Long userId = getCurrentUserId(authentication);
        userService.updatePassword(userId, request.getOldPassword(),
                request.getNewPassword());
        return Result.success();
    }

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getByUsername(username);
        return user.getId();
    }
}
```

需要添加 `getByUsername` 到 UserService 和 PasswordUpdateRequest DTO:

在 `UserService.java` 中添加:
```java
User getByUsername(String username);
```

在 `UserServiceImpl.java` 中添加:
```java
@Override
public User getByUsername(String username) {
    return userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, username));
}
```

创建 `src/main/java/com/company/admin/dto/request/PasswordUpdateRequest.java`:
```java
package com.company.admin.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PasswordUpdateRequest {
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String newPassword;
}
```

- [ ] **Step 4: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/company/admin/service/UserService.java src/main/java/com/company/admin/service/impl/UserServiceImpl.java src/main/java/com/company/admin/controller/UserController.java src/main/java/com/company/admin/dto/request/PasswordUpdateRequest.java
git commit -m "feat: add user management module — service and controller"
```

---

## Task 10: 认证模块

**Files:**
- Create: `src/main/java/com/company/admin/service/AuthService.java`
- Create: `src/main/java/com/company/admin/service/impl/AuthServiceImpl.java`
- Create: `src/main/java/com/company/admin/controller/AuthController.java`

**Interfaces:**
- Consumes: `AuthenticationManager`, `JwtTokenUtil`, `UserMapper`, `LoginLogMapper`, `LoginRequest`, `LoginResponse`, `UserInfoResponse`
- Produces:
  - `AuthService` — `LoginResponse login(LoginRequest req, HttpServletRequest request)`, `UserInfoResponse getCurrentUserInfo(String username)`
  - `AuthController` — `POST /api/auth/login`, `GET /api/auth/info`

### 步骤

- [ ] **Step 1: 创建 `AuthService.java`**

```java
package com.company.admin.service;

import com.company.admin.dto.request.LoginRequest;
import com.company.admin.dto.response.LoginResponse;
import com.company.admin.dto.response.UserInfoResponse;
import javax.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);
    UserInfoResponse getCurrentUserInfo(String username);
}
```

- [ ] **Step 2: 创建 `AuthServiceImpl.java`**

```java
package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.LoginRequest;
import com.company.admin.dto.response.LoginResponse;
import com.company.admin.dto.response.UserInfoResponse;
import com.company.admin.entity.LoginLog;
import com.company.admin.entity.Menu;
import com.company.admin.entity.User;
import com.company.admin.mapper.LoginLogMapper;
import com.company.admin.mapper.MenuMapper;
import com.company.admin.mapper.UserMapper;
import com.company.admin.security.JwtTokenUtil;
import com.company.admin.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;
    private final MenuMapper menuMapper;
    private final LoginLogMapper loginLogMapper;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenUtil jwtTokenUtil,
                           UserMapper userMapper,
                           MenuMapper menuMapper,
                           LoginLogMapper loginLogMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userMapper = userMapper;
        this.menuMapper = menuMapper;
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
            String token = jwtTokenUtil.generateToken(authentication.getName());
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, request.getUsername()));

            // 记录登录成功日志
            LoginLog loginLog = new LoginLog();
            loginLog.setUsername(request.getUsername());
            loginLog.setLoginType(1);
            loginLog.setIp(getIp(httpRequest));
            loginLog.setStatus(1);
            loginLog.setMessage("登录成功");
            loginLog.setCreateTime(LocalDateTime.now());
            loginLogMapper.insert(loginLog);

            return new LoginResponse(token, user.getId());
        } catch (BadCredentialsException e) {
            recordLoginFail(request.getUsername(), httpRequest, "用户名或密码错误");
            throw new BusinessException(ErrorCode.USERNAME_PASSWORD_ERROR);
        } catch (DisabledException e) {
            recordLoginFail(request.getUsername(), httpRequest, "用户已禁用");
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
    }

    @Override
    public UserInfoResponse getCurrentUserInfo(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 获取用户角色
        List<String> roles = userMapper.selectRoleIdsByUserId(user.getId())
                .stream().map(String::valueOf).collect(Collectors.toList());

        // 获取用户权限
        Set<String> permissions = new HashSet<>(userMapper.selectPermissionsByUserId(user.getId()));

        // 获取用户菜单
        List<Menu> menus = menuMapper.selectByUserId(user.getId());

        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRoles(roles);
        response.setPermissions(permissions);
        return response;
    }

    private void recordLoginFail(String username, HttpServletRequest request,
                                  String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginType(1);
        loginLog.setIp(getIp(request));
        loginLog.setStatus(0);
        loginLog.setMessage(message);
        loginLog.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(loginLog);
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

- [ ] **Step 3: 创建 `AuthController.java`**

```java
package com.company.admin.controller;

import com.company.admin.common.Result;
import com.company.admin.dto.request.LoginRequest;
import com.company.admin.dto.response.LoginResponse;
import com.company.admin.dto.response.UserInfoResponse;
import com.company.admin.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                        HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);
        return Result.success("登录成功", response);
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> info(Authentication authentication) {
        UserInfoResponse response = authService.getCurrentUserInfo(
                authentication.getName());
        return Result.success(response);
    }
}
```

- [ ] **Step 4: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/company/admin/service/AuthService.java src/main/java/com/company/admin/service/impl/AuthServiceImpl.java src/main/java/com/company/admin/controller/AuthController.java
git commit -m "feat: add auth module — login, logout, user info"
```

---

## Task 11: 操作日志模块

**Files:**
- Create: `src/main/java/com/company/admin/annotation/OperationLog.java`
- Create: `src/main/java/com/company/admin/aspect/OperationLogAspect.java`
- Create: `src/main/java/com/company/admin/service/OperationLogService.java`
- Create: `src/main/java/com/company/admin/service/impl/OperationLogServiceImpl.java`
- Create: `src/main/java/com/company/admin/controller/LogController.java`

**Interfaces:**
- Consumes: `OperationLogMapper`, `Result`, `BusinessException`
- Produces:
  - `@OperationLog` — 自定义注解，属性 `value` (操作描述), `type` (LogType 枚举), `saveParams` (默认 true), `saveResult` (默认 false)
  - `OperationLogAspect` — `@Aspect`，拦截 `@OperationLog` 注解的方法，异步写入日志
  - `OperationLogService` — `PageResult<OperationLog> page(int pageNum, int pageSize)`
  - `LogController` — `/api/log/operation/list`

### 步骤

- [ ] **Step 1: 创建 `@OperationLog` 注解**

```java
package com.company.admin.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    String value() default "";

    LogType type() default LogType.OTHER;

    boolean saveParams() default true;

    boolean saveResult() default false;

    enum LogType {
        OTHER, INSERT, UPDATE, DELETE, SELECT
    }
}
```

- [ ] **Step 2: 创建 `OperationLogAspect.java`**

```java
package com.company.admin.aspect;

import com.company.admin.annotation.OperationLog;
import com.company.admin.entity.OperationLog;
import com.company.admin.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OperationLogAspect(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Around("@annotation(com.company.admin.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            saveLog(joinPoint, result, exception, elapsed);
        }
    }

    @Async
    public void saveLog(ProceedingJoinPoint joinPoint, Object result,
                        Exception exception, long elapsed) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        OperationLog entity = new OperationLog();
        entity.setOperation(annotation.value());
        entity.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        entity.setCreateTime(LocalDateTime.now());

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            entity.setUsername(authentication.getName());
        }

        // 获取 IP
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            entity.setIp(getIp(request));
        }

        // 保存请求参数
        if (annotation.saveParams()) {
            try {
                entity.setParams(objectMapper.writeValueAsString(joinPoint.getArgs()));
            } catch (Exception ignored) {}
        }

        // 保存返回结果
        if (annotation.saveResult() && exception == null) {
            try {
                entity.setResult(objectMapper.writeValueAsString(result));
            } catch (Exception ignored) {}
        }

        // 记录异常
        if (exception != null) {
            entity.setResult("异常: " + exception.getMessage());
        }

        operationLogMapper.insert(entity);
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

- [ ] **Step 3: 创建 `OperationLogService.java` 和 `OperationLogServiceImpl.java`**

```java
// OperationLogService.java
package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.entity.OperationLog;

public interface OperationLogService {
    PageResult<OperationLog> page(int pageNum, int pageSize);
}
```

```java
// OperationLogServiceImpl.java
package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.PageResult;
import com.company.admin.entity.OperationLog;
import com.company.admin.mapper.OperationLogMapper;
import com.company.admin.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public PageResult<OperationLog> page(int pageNum, int pageSize) {
        Page<OperationLog> page = operationLogMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<OperationLog>()
                        .orderByDesc(OperationLog::getCreateTime));
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }
}
```

- [ ] **Step 4: 创建 `LogController.java`**

```java
package com.company.admin.controller;

import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.entity.LoginLog;
import com.company.admin.entity.OperationLog;
import com.company.admin.service.LoginLogService;
import com.company.admin.service.OperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/log")
public class LogController {

    private final OperationLogService operationLogService;
    private final LoginLogService loginLogService;

    public LogController(OperationLogService operationLogService,
                         LoginLogService loginLogService) {
        this.operationLogService = operationLogService;
        this.loginLogService = loginLogService;
    }

    @GetMapping("/operation/list")
    @PreAuthorize("hasAuthority('sys:log:list')")
    public Result<PageResult<OperationLog>> operationList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(operationLogService.page(pageNum, pageSize));
    }

    @GetMapping("/login/list")
    @PreAuthorize("hasAuthority('sys:log:list')")
    public Result<PageResult<LoginLog>> loginList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(loginLogService.page(pageNum, pageSize));
    }
}
```

- [ ] **Step 5: 为所有 Controller 补加 @OperationLog 注解**

修改 `MenuController.java`，在 create/update/delete 方法上添加:

```java
import com.company.admin.annotation.OperationLog;

// 在 create 方法上:
@OperationLog(value = "新增菜单", type = OperationLog.LogType.INSERT)
@PostMapping
@PreAuthorize("hasAuthority('sys:menu:add')")

// 在 update 方法上:
@OperationLog(value = "修改菜单", type = OperationLog.LogType.UPDATE)
@PutMapping("/{id}")
@PreAuthorize("hasAuthority('sys:menu:edit')")

// 在 delete 方法上:
@OperationLog(value = "删除菜单", type = OperationLog.LogType.DELETE)
@DeleteMapping("/{id}")
@PreAuthorize("hasAuthority('sys:menu:delete')")
```

修改 `RoleController.java`，在 create/update/delete/assignMenus 方法上添加:

```java
import com.company.admin.annotation.OperationLog;

@OperationLog(value = "新增角色", type = OperationLog.LogType.INSERT)
@PostMapping
@PreAuthorize("hasAuthority('sys:role:add')")

@OperationLog(value = "修改角色", type = OperationLog.LogType.UPDATE)
@PutMapping("/{id}")
@PreAuthorize("hasAuthority('sys:role:edit')")

@OperationLog(value = "删除角色", type = OperationLog.LogType.DELETE)
@DeleteMapping("/{id}")
@PreAuthorize("hasAuthority('sys:role:delete')")

@OperationLog(value = "分配角色菜单", type = OperationLog.LogType.UPDATE)
@PutMapping("/{id}/menus")
@PreAuthorize("hasAuthority('sys:role:menu')")
```

修改 `UserController.java`，在 create/update/delete/resetPassword/assignRoles 方法上添加:

```java
import com.company.admin.annotation.OperationLog;

@OperationLog(value = "新增用户", type = OperationLog.LogType.INSERT)
@PostMapping
@PreAuthorize("hasAuthority('sys:user:add')")

@OperationLog(value = "修改用户", type = OperationLog.LogType.UPDATE)
@PutMapping
@PreAuthorize("hasAuthority('sys:user:edit')")

@OperationLog(value = "删除用户", type = OperationLog.LogType.DELETE)
@DeleteMapping("/{id}")
@PreAuthorize("hasAuthority('sys:user:delete')")

@OperationLog(value = "分配用户角色", type = OperationLog.LogType.UPDATE)
@PutMapping("/{id}/roles")
@PreAuthorize("hasAuthority('sys:user:role')")

@OperationLog(value = "重置密码", type = OperationLog.LogType.UPDATE)
@PutMapping("/{id}/reset-password")
@PreAuthorize("hasAuthority('sys:user:resetPwd')")
```

修改 `LogController.java`，在 operationList/loginList 方法上添加:

```java
import com.company.admin.annotation.OperationLog;

@OperationLog(value = "查询操作日志", type = OperationLog.LogType.SELECT, saveResult = true)
@GetMapping("/operation/list")
@PreAuthorize("hasAuthority('sys:log:list')")

@OperationLog(value = "查询登录日志", type = OperationLog.LogType.SELECT, saveResult = true)
@GetMapping("/login/list")
@PreAuthorize("hasAuthority('sys:log:list')")
```

- [ ] **Step 6: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add src/main/java/com/company/admin/annotation/ src/main/java/com/company/admin/aspect/ src/main/java/com/company/admin/service/OperationLogService.java src/main/java/com/company/admin/service/impl/OperationLogServiceImpl.java src/main/java/com/company/admin/controller/LogController.java src/main/java/com/company/admin/controller/MenuController.java src/main/java/com/company/admin/controller/RoleController.java src/main/java/com/company/admin/controller/UserController.java
git commit -m "feat: add operation log module — annotation, aspect, service, controller"
```

---

## Task 12: 登录日志 + Async/Swagger 配置 + 初始数据

**Files:**
- Create: `src/main/java/com/company/admin/service/LoginLogService.java`
- Create: `src/main/java/com/company/admin/service/impl/LoginLogServiceImpl.java`
- Create: `src/main/java/com/company/admin/config/AsyncConfig.java`
- Create: `src/main/java/com/company/admin/config/SwaggerConfig.java`
- Create: `src/main/resources/sql/data.sql`

**Interfaces:**
- Consumes: `LoginLogMapper`, `PageResult`
- Produces:
  - `LoginLogService` — `PageResult<LoginLog> page(int pageNum, int pageSize)`
  - `AsyncConfig` — 启用 `@Async`
  - `SwaggerConfig` — Knife4j OpenAPI 3 配置
  - `data.sql` — 初始管理员数据

### 步骤

- [ ] **Step 1: 创建 `LoginLogService.java` 和 `LoginLogServiceImpl.java`**

```java
// LoginLogService.java
package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.entity.LoginLog;

public interface LoginLogService {
    PageResult<LoginLog> page(int pageNum, int pageSize);
}
```

```java
// LoginLogServiceImpl.java
package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.PageResult;
import com.company.admin.entity.LoginLog;
import com.company.admin.mapper.LoginLogMapper;
import com.company.admin.service.LoginLogService;
import org.springframework.stereotype.Service;

@Service
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    public LoginLogServiceImpl(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public PageResult<LoginLog> page(int pageNum, int pageSize) {
        Page<LoginLog> page = loginLogMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<LoginLog>()
                        .orderByDesc(LoginLog::getCreateTime));
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }
}
```

- [ ] **Step 2: 创建 `AsyncConfig.java`**

```java
package com.company.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
}
```

- [ ] **Step 3: 创建 `SwaggerConfig.java`**

```java
package com.company.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("后台管理系统 API")
                        .version("1.0.0")
                        .description("企业内部后台管理系统接口文档")
                        .contact(new Contact()
                                .name("Admin")
                                .email("admin@company.com")));
    }
}
```

- [ ] **Step 4: 创建初始数据脚本 `src/main/resources/sql/data.sql`**

```sql
-- 初始管理员用户 (密码: admin123 的 BCrypt 加密)
INSERT INTO sys_user (username, password, nickname, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '超级管理员', 1);

-- 初始角色
INSERT INTO sys_role (role_name, role_code, description, status) VALUES
('超级管理员', 'ADMIN', '拥有所有权限', 1),
('普通用户', 'USER', '普通用户权限', 1);

-- 初始菜单（目录 + 菜单 + 按钮）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, permission, icon, sort_order) VALUES
-- 系统管理目录
(1, 0, '系统管理', 1, '/system', NULL, 'system', 1),
-- 用户管理
(2, 1, '用户管理', 2, '/system/user', 'sys:user:list', 'user', 10),
(3, 2, '新增用户', 3, NULL, 'sys:user:add', NULL, 11),
(4, 2, '修改用户', 3, NULL, 'sys:user:edit', NULL, 12),
(5, 2, '删除用户', 3, NULL, 'sys:user:delete', NULL, 13),
(6, 2, '分配角色', 3, NULL, 'sys:user:role', NULL, 14),
(7, 2, '重置密码', 3, NULL, 'sys:user:resetPwd', NULL, 15),
-- 角色管理
(8, 1, '角色管理', 2, '/system/role', 'sys:role:list', 'role', 20),
(9, 8, '新增角色', 3, NULL, 'sys:role:add', NULL, 21),
(10, 8, '修改角色', 3, NULL, 'sys:role:edit', NULL, 22),
(11, 8, '删除角色', 3, NULL, 'sys:role:delete', NULL, 23),
(12, 8, '分配菜单', 3, NULL, 'sys:role:menu', NULL, 24),
-- 菜单管理
(13, 1, '菜单管理', 2, '/system/menu', 'sys:menu:list', 'menu', 30),
(14, 13, '新增菜单', 3, NULL, 'sys:menu:add', NULL, 31),
(15, 13, '修改菜单', 3, NULL, 'sys:menu:edit', NULL, 32),
(16, 13, '删除菜单', 3, NULL, 'sys:menu:delete', NULL, 33),
-- 日志管理目录
(17, 0, '日志管理', 1, '/log', NULL, 'log', 2),
(18, 17, '操作日志', 2, '/log/operation', 'sys:log:list', 'documentation', 40),
(19, 17, '登录日志', 2, '/log/login', 'sys:log:list', 'logininfor', 50);

-- 初始角色-菜单关联（ADMIN 拥有全部菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 初始用户-角色关联（admin 为 ADMIN）
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
```

- [ ] **Step 5: 更新 `AdminApplication.java` 添加 `@EnableAsync` 和 `@EnableAspectJAutoProxy`**

```java
package com.company.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.company.admin.mapper")
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

注意: `@EnableAsync` 在 `AsyncConfig` 中已配置，`@EnableAspectJAutoProxy` 在 SpringBoot 中自动启用。

- [ ] **Step 6: 验证编译**

```bash
mvn compile
```

期望: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add src/main/java/com/company/admin/service/LoginLogService.java src/main/java/com/company/admin/service/impl/LoginLogServiceImpl.java src/main/java/com/company/admin/config/AsyncConfig.java src/main/java/com/company/admin/config/SwaggerConfig.java src/main/resources/sql/data.sql
git commit -m "feat: add login log service, async/swagger config, initial data"
```

---

## Task 13: 项目集成验证与最终测试

**Files:**
- Create: `src/test/resources/application-test.yml`
- Create: `src/test/java/com/company/admin/AdminApplicationTests.java`

**目标:** 验证项目全部编译通过，集成测试通过，确保所有模块正确协作

### 步骤

- [ ] **Step 1: 创建测试配置 `src/test/resources/application-test.yml`**

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:admin_test;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:

jwt:
  secret: dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3Rz
  expiration: 86400000
```

- [ ] **Step 2: 创建集成测试 `AdminApplicationTests.java`**

```java
package com.company.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AdminApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 3: 运行完整编译和测试**

```bash
mvn clean compile test
```

期望: BUILD SUCCESS, 所有测试通过

- [ ] **Step 4: 提交**

```bash
git add src/test/
git commit -m "test: add integration test and H2 test profile"
```

---

## 实现顺序总结

```
Task 1  → 项目脚手架 + pom.xml + 数据库初始化
Task 2  → 公共模块 (Result, ErrorCode, BusinessException, GlobalExceptionHandler)
Task 3  → 实体类 (7 个 Entity)
Task 4  → DTO 类 (请求/响应)
Task 5  → Mapper 层 (接口 + XML)
Task 6  → Security + JWT 模块
Task 7  → 菜单管理模块
Task 8  → 角色管理模块
Task 9  → 用户管理模块
Task 10 → 认证模块 (登录/登出/用户信息)
Task 11 → 操作日志模块 (注解 + AOP + Service + Controller)
Task 12 → 登录日志 + Async/Swagger 配置 + 初始数据
Task 13 → 集成测试验证
```

**依赖关系:**
- Task 2-5 顺序依赖（后可并行）
- Task 6 依赖 Task 5
- Task 7-9 依赖 Task 5（可并行开发）
- Task 10 依赖 Task 6 和 Task 9
- Task 11 依赖 Task 5
- Task 12 依赖 Task 5 和 Task 11
- Task 13 依赖所有 Task 完成