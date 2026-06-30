package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
