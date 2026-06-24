package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.MenuCreateRequest;
import com.company.admin.dto.response.MenuTreeResponse;
import com.company.admin.entity.Menu;
import com.company.admin.entity.RoleMenu;
import com.company.admin.mapper.MenuMapper;
import com.company.admin.mapper.RoleMenuMapper;
import com.company.admin.service.MenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    public MenuServiceImpl(MenuMapper menuMapper, RoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<MenuTreeResponse> getMenuTree() {
        List<Menu> allMenus = menuMapper.selectAllEnabled();
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<Menu> getAllMenus() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSortOrder));
    }

    @Override
    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    @Transactional
    public void create(MenuCreateRequest request) {
        Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getMenuName, request.getMenuName()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.MENU_NAME_EXISTS);
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(request, menu);
        if (request.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuMapper.insert(menu);
    }

    @Override
    @Transactional
    public void update(Long id, MenuCreateRequest request) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(400, "菜单不存在");
        }
        Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getMenuName, request.getMenuName())
                        .ne(Menu::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.MENU_NAME_EXISTS);
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(request, menu);
        menu.setId(id);
        if (request.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.MENU_HAS_CHILDREN);
        }
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getMenuId, id));
        menuMapper.deleteById(id);
    }

    private List<MenuTreeResponse> buildTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .map(m -> {
                    MenuTreeResponse node = new MenuTreeResponse();
                    BeanUtils.copyProperties(m, node);
                    node.setChildren(buildTree(menus, m.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }
}
