package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.Menu;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {

    List<Menu> selectAllEnabled();

    List<Menu> selectByUserId(@Param("userId") Long userId);
}
