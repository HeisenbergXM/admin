package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.ExteriorColorCreateRequest;
import com.company.admin.dto.request.ExteriorColorQueryRequest;
import com.company.admin.dto.request.ExteriorColorUpdateRequest;
import com.company.admin.entity.ExteriorColor;
import com.company.admin.mapper.ExteriorColorMapper;
import com.company.admin.service.ExteriorColorService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ExteriorColorServiceImpl implements ExteriorColorService {

    private final ExteriorColorMapper exteriorColorMapper;

    public ExteriorColorServiceImpl(ExteriorColorMapper exteriorColorMapper) {
        this.exteriorColorMapper = exteriorColorMapper;
    }

    @Override
    public PageResult<ExteriorColor> pageExteriorColors(ExteriorColorQueryRequest request) {
        LambdaQueryWrapper<ExteriorColor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getColorName())) {
            wrapper.like(ExteriorColor::getColorName, request.getColorName())
                   .or()
                   .like(ExteriorColor::getColorNameCn, request.getColorName());
        }
        if (request.getStatus() != null) {
            wrapper.eq(ExteriorColor::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(ExteriorColor::getCreateTime);
        Page<ExteriorColor> page = exteriorColorMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public void createExteriorColor(ExteriorColorCreateRequest request) {
        ExteriorColor color = new ExteriorColor();
        BeanUtils.copyProperties(request, color);
        exteriorColorMapper.insert(color);
    }

    @Override
    @Transactional
    public void updateExteriorColor(ExteriorColorUpdateRequest request) {
        ExteriorColor existing = exteriorColorMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(400, "外饰颜色不存在");
        }
        ExteriorColor update = new ExteriorColor();
        update.setId(request.getId());
        update.setColorName(request.getColorName());
        update.setColorNameCn(request.getColorNameCn());
        update.setStatus(request.getStatus());
        exteriorColorMapper.updateById(update);
    }

    @Override
    @Transactional
    public void deleteExteriorColor(Long id) {
        exteriorColorMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleExteriorColorStatus(Long id, Integer status) {
        ExteriorColor color = exteriorColorMapper.selectById(id);
        if (color == null) {
            throw new BusinessException(400, "外饰颜色不存在");
        }
        color.setStatus(status);
        exteriorColorMapper.updateById(color);
    }

    @Override
    public List<ExteriorColor> listAllEnabled() {
        return exteriorColorMapper.selectList(
                new LambdaQueryWrapper<ExteriorColor>()
                        .eq(ExteriorColor::getStatus, 1)
                        .orderByAsc(ExteriorColor::getColorName));
    }
}