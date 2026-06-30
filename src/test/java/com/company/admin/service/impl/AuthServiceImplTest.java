package com.company.admin.service.impl;

import com.company.admin.dto.response.UserInfoResponse;
import com.company.admin.entity.Menu;
import com.company.admin.entity.User;
import com.company.admin.mapper.LoginLogMapper;
import com.company.admin.mapper.MenuMapper;
import com.company.admin.mapper.UserMapper;
import com.company.admin.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    @Test
    void getCurrentUserInfoReturnsRoleCodesAndMenuTreeWithoutButtons() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtTokenUtil jwtTokenUtil = mock(JwtTokenUtil.class);
        UserMapper userMapper = mock(UserMapper.class);
        MenuMapper menuMapper = mock(MenuMapper.class);
        LoginLogMapper loginLogMapper = mock(LoginLogMapper.class);
        AuthServiceImpl service = new AuthServiceImpl(
                authenticationManager, jwtTokenUtil, userMapper, menuMapper, loginLogMapper);

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("管理员");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("ADMIN"));
        when(userMapper.selectPermissionsByUserId(1L)).thenReturn(List.of("sys:user:list"));
        when(menuMapper.selectByUserId(1L)).thenReturn(List.of(
                menu(10L, 0L, 1, "系统管理"),
                menu(11L, 10L, 2, "用户管理"),
                menu(12L, 11L, 3, "用户查询")));

        UserInfoResponse response = service.getCurrentUserInfo("admin");

        assertEquals(List.of("ADMIN"), response.getRoles());
        assertEquals(1, response.getMenus().size());
        assertEquals("系统管理", response.getMenus().get(0).getMenuName());
        assertEquals(1, response.getMenus().get(0).getChildren().size());
        assertEquals("用户管理", response.getMenus().get(0).getChildren().get(0).getMenuName());
    }

    private Menu menu(Long id, Long parentId, Integer type, String name) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuType(type);
        menu.setMenuName(name);
        menu.setSortOrder(id.intValue());
        return menu;
    }
}
