package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.InteriorColorCreateRequest;
import com.company.admin.dto.request.InteriorColorQueryRequest;
import com.company.admin.dto.request.InteriorColorUpdateRequest;
import com.company.admin.entity.InteriorColor;
import com.company.admin.mapper.InteriorColorMapper;
import com.company.admin.service.InteriorColorService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class InteriorColorServiceImpl implements InteriorColorService {

    private final InteriorColorMapper interiorColorMapper;

    public InteriorColorServiceImpl(InteriorColorMapper interiorColorMapper) {
        this.interiorColorMapper = interiorColorMapper;
    }

    @Override
    public PageResult<InteriorColor> pageInteriorColors(InteriorColorQueryRequest request) {
        LambdaQueryWrapper<InteriorColor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getColorName())) {
            wrapper.like(InteriorColor::getColorName, request.getColorName())
                   .or()
                   .like(InteriorColor::getColorNameCn, request.getColorName());
        }
        if (request.getStatus() != null) {
            wrapper.eq(InteriorColor::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(InteriorColor::getCreateTime);
        Page<InteriorColor> page = interiorColorMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public void createInteriorColor(InteriorColorCreateRequest request) {
        InteriorColor color = new InteriorColor();
        BeanUtils.copyProperties(request, color);
        interiorColorMapper.insert(color);
    }

    @Override
    @Transactional
    public void updateInteriorColor(InteriorColorUpdateRequest request) {
        InteriorColor existing = interiorColorMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(400, "内饰颜色不存在");
        }
        InteriorColor update = new InteriorColor();
        update.setId(request.getId());
        update.setColorName(request.getColorName());
        update.setColorNameCn(request.getColorNameCn());
        update.setStatus(request.getStatus());
        interiorColorMapper.updateById(update);
    }

    @Override
    @Transactional
    public void deleteInteriorColor(Long id) {
        interiorColorMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleInteriorColorStatus(Long id, Integer status) {
        InteriorColor color = interiorColorMapper.selectById(id);
        if (color == null) {
            throw new BusinessException(400, "内饰颜色不存在");
        }
        color.setStatus(status);
        interiorColorMapper.updateById(color);
    }

    @Override
    public List<InteriorColor> listAllEnabled() {
        return interiorColorMapper.selectList(
                new LambdaQueryWrapper<InteriorColor>()
                        .eq(InteriorColor::getStatus, 1)
                        .orderByAsc(InteriorColor::getColorName));
    }
}