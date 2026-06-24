package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.Role;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
