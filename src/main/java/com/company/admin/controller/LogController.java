package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
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
    @OpLog(value = "查询操作日志", type = OpLog.LogType.SELECT, saveResult = true)
    @PreAuthorize("hasAuthority('sys:log:list')")
    public Result<PageResult<OperationLog>> operationList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(operationLogService.page(pageNum, pageSize));
    }

    @GetMapping("/login/list")
    @OpLog(value = "查询登录日志", type = OpLog.LogType.SELECT, saveResult = true)
    @PreAuthorize("hasAuthority('sys:log:list')")
    public Result<PageResult<LoginLog>> loginList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(loginLogService.page(pageNum, pageSize));
    }
}
