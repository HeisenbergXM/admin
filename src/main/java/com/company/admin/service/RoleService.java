package com.company.admin.service;

import com.company.admin.dto.request.RoleCreateRequest;
import com.company.admin.dto.request.RoleMenuRequest;
import com.company.admin.entity.Role;
import java.util.List;

public interface RoleService {
    List<Role> list();
    Role getById(Long id);
    void create(RoleCreateRequest request);
    void update(Long id, RoleCreateRequest request);
    void delete(Long id);
    List<Long> getRoleMenuIds(Long roleId);
    void assignMenus(RoleMenuRequest request);
}
