package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.Waybill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WaybillMapper extends BaseMapper<Waybill> {

    List<Waybill> selectByDispatchListId(@Param("dispatchListId") Long dispatchListId);
}
