package com.company.admin.service;

import com.company.admin.dto.request.MenuCreateRequest;
import com.company.admin.dto.response.MenuTreeResponse;
import com.company.admin.entity.Menu;
import java.util.List;

public interface MenuService {
    List<MenuTreeResponse> getMenuTree();
    List<Menu> getAllMenus();
    Menu getById(Long id);
    void create(MenuCreateRequest request);
    void update(Long id, MenuCreateRequest request);
    void delete(Long id);
}
