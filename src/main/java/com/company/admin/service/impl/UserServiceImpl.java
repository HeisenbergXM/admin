package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.*;
import com.company.admin.entity.User;
import com.company.admin.entity.UserRole;
import com.company.admin.mapper.UserMapper;
import com.company.admin.mapper.UserRoleMapper;
import com.company.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<User> page(UserQueryRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(User::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getNickname())) {
            wrapper.like(User::getNickname, request.getNickname());
        }
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }
        wrapper.orderByAsc(User::getCreateTime);
        Page<User> page = userMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    @Transactional
    public void create(UserCreateRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.insert(user);
    }

    @Override
    @Transactional
    public void update(UserUpdateRequest request) {
        User user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        User updateUser = new User();
        BeanUtils.copyProperties(request, updateUser);
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        userMapper.deleteById(id);
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    @Transactional
    public void assignRoles(UserRoleRequest request) {
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, request.getUserId()));
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<UserRole> userRoles = request.getRoleIds().stream()
                    .map(roleId -> {
                        UserRole ur = new UserRole();
                        ur.setUserId(request.getUserId());
                        ur.setRoleId(roleId);
                        return ur;
                    })
                    .collect(Collectors.toList());
            for (UserRole ur : userRoles) {
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setNickname(request.getNickname());
        updateUser.setEmail(request.getEmail());
        updateUser.setPhone(request.getPhone());
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
