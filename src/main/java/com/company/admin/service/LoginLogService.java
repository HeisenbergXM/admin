package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.entity.LoginLog;

public interface LoginLogService {
    PageResult<LoginLog> page(int pageNum, int pageSize);
}
