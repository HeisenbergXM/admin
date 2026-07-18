package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.VehProduction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehProductionMapper extends BaseMapper<VehProduction> {

    VehProduction selectByIdForUpdate(@Param("id") Long id);
}
