package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.RegistrationQueryRequest;
import com.company.admin.dto.response.RegistrationResponse;
import com.company.admin.entity.VehRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehRegistrationMapper extends BaseMapper<VehRegistration> {

    Page<RegistrationResponse> selectRegistrationPage(Page<RegistrationResponse> page,
                                                      @Param("query") RegistrationQueryRequest query);
}
