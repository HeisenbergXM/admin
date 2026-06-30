package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.WaybillDealer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WaybillDealerMapper extends BaseMapper<WaybillDealer> {

    List<WaybillDealer> selectByWaybillId(@Param("waybillId") Long waybillId);

    List<WaybillDealer> selectByDispatchListId(@Param("dispatchListId") Long dispatchListId);
}
