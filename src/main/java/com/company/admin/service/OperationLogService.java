package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.entity.OperationLog;

public interface OperationLogService {
    PageResult<OperationLog> page(int pageNum, int pageSize);
}
