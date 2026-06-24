package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.RoleCreateRequest;
import com.company.admin.dto.request.RoleMenuRequest;
import com.company.admin.entity.Role;
import com.company.admin.entity.RoleMenu;
import com.company.admin.entity.UserRole;
import com.company.admin.mapper.RoleMapper;
import com.company.admin.mapper.RoleMenuMapper;
import com.company.admin.mapper.UserRoleMapper;
import com.company.admin.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper,
                           UserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public List<Role> list() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<Role>().orderByAsc(Role::getCreateTime));
    }

    @Override
    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    @Transactional
    public void create(RoleCreateRequest request) {
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, request.getRoleCode()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        roleMapper.insert(role);
    }

    @Override
    @Transactional
    public void update(Long id, RoleCreateRequest request) {
        Role existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(400, "角色不存在");
        }
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, request.getRoleCode())
                        .ne(Role::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        role.setId(id);
        roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        roleMapper.deleteById(id);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignMenus(RoleMenuRequest request) {
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, request.getRoleId()));
        if (request.getMenuIds() != null && !request.getMenuIds().isEmpty()) {
            List<RoleMenu> roleMenus = request.getMenuIds().stream()
                    .map(menuId -> {
                        RoleMenu rm = new RoleMenu();
                        rm.setRoleId(request.getRoleId());
                        rm.setMenuId(menuId);
                        return rm;
                    })
                    .collect(Collectors.toList());
            for (RoleMenu rm : roleMenus) {
                roleMenuMapper.insert(rm);
            }
        }
    }
}
