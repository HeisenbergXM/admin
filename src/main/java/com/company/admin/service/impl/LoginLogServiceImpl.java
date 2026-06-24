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
