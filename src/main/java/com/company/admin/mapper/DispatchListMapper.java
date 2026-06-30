package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.DispatchListQueryRequest;
import com.company.admin.dto.response.DispatchListResponse;
import com.company.admin.entity.DispatchList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DispatchListMapper extends BaseMapper<DispatchList> {

    Page<DispatchListResponse> selectDispatchPage(Page<DispatchListResponse> page,
                                                  @Param("query") DispatchListQueryRequest query);
}
