package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.WaybillDealerVin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WaybillDealerVinMapper extends BaseMapper<WaybillDealerVin> {

    Long countOpenVin(@Param("vehicleId") Long vehicleId,
                      @Param("dealerRowId") Long dealerRowId);

    List<WaybillDealerVin> selectByDealerRowId(@Param("dealerRowId") Long dealerRowId);
}
