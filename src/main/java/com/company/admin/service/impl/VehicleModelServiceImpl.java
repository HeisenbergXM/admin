package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.ModelCreateRequest;
import com.company.admin.dto.request.ModelQueryRequest;
import com.company.admin.dto.request.ModelUpdateRequest;
import com.company.admin.entity.VehicleModel;
import com.company.admin.mapper.VehicleModelMapper;
import com.company.admin.service.VehicleModelService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleModelServiceImpl implements VehicleModelService {

    private final VehicleModelMapper vehicleModelMapper;

    public VehicleModelServiceImpl(VehicleModelMapper vehicleModelMapper) {
        this.vehicleModelMapper = vehicleModelMapper;
    }

    @Override
    public PageResult<VehicleModel> pageModels(ModelQueryRequest request) {
        LambdaQueryWrapper<VehicleModel> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getMaterialCode())) {
            wrapper.like(VehicleModel::getMaterialCode, request.getMaterialCode());
        }
        if (StringUtils.hasText(request.getSeries())) {
            wrapper.eq(VehicleModel::getSeries, request.getSeries());
        }
        if (StringUtils.hasText(request.getSpec())) {
            wrapper.like(VehicleModel::getSpec, request.getSpec());
        }
        if (StringUtils.hasText(request.getModelName())) {
            wrapper.like(VehicleModel::getModelName, request.getModelName());
        }
        if (StringUtils.hasText(request.getModelCode())) {
            wrapper.like(VehicleModel::getModelCode, request.getModelCode());
        }
        if (StringUtils.hasText(request.getYearMake())) {
            wrapper.eq(VehicleModel::getYearMake, request.getYearMake());
        }
        if (request.getStatus() != null) {
            wrapper.eq(VehicleModel::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(VehicleModel::getCreateTime);
        Page<VehicleModel> page = vehicleModelMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public void createModel(ModelCreateRequest request) {
        VehicleModel model = new VehicleModel();
        BeanUtils.copyProperties(request, model);
        vehicleModelMapper.insert(model);
    }

    @Override
    @Transactional
    public void updateModel(ModelUpdateRequest request) {
        VehicleModel existing = vehicleModelMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(400, "车型不存在");
        }
        VehicleModel update = new VehicleModel();
        update.setId(request.getId());
        update.setMaterialCode(request.getMaterialCode());
        update.setSeries(request.getSeries());
        update.setSpec(request.getSpec());
        update.setModelName(request.getModelName());
        update.setModelCode(request.getModelCode());
        update.setYearMake(request.getYearMake());
        update.setStatus(request.getStatus());
        vehicleModelMapper.updateById(update);
    }

    @Override
    @Transactional
    public void deleteModel(Long id) {
        vehicleModelMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleModelStatus(Long id, Integer status) {
        VehicleModel model = vehicleModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(400, "车型不存在");
        }
        model.setStatus(status);
        vehicleModelMapper.updateById(model);
    }

    @Override
    public List<VehicleModel> listAllEnabled() {
        return vehicleModelMapper.selectList(
                new LambdaQueryWrapper<VehicleModel>()
                        .eq(VehicleModel::getStatus, 1)
                        .orderByAsc(VehicleModel::getModelName));
    }

    @Override
    public List<String> listSeries() {
        List<VehicleModel> models = vehicleModelMapper.selectList(
                new LambdaQueryWrapper<VehicleModel>()
                        .select(VehicleModel::getSeries)
                        .isNotNull(VehicleModel::getSeries)
                        .ne(VehicleModel::getSeries, "")
                        .orderByAsc(VehicleModel::getSeries));
        return models.stream()
                .map(VehicleModel::getSeries)
                .distinct()
                .collect(Collectors.toList());
    }
}