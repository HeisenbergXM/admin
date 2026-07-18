package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.AllocationQueryRequest;
import com.company.admin.dto.response.AllocationResponse;
import com.company.admin.entity.VehAllocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehAllocationMapper extends BaseMapper<VehAllocation> {

    Page<AllocationResponse> selectAllocationPage(Page<AllocationResponse> page,
                                                  @Param("query") AllocationQueryRequest query);

    VehAllocation selectByIdForUpdate(@Param("id") Long id);
}
