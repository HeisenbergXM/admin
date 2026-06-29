package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DictItemSaveRequest;
import com.company.admin.dto.request.DictTypeCreateRequest;
import com.company.admin.dto.request.DictTypeQueryRequest;
import com.company.admin.dto.request.DictTypeUpdateRequest;
import com.company.admin.entity.DictItem;
import com.company.admin.entity.DictType;
import com.company.admin.mapper.DictItemMapper;
import com.company.admin.mapper.DictTypeMapper;
import com.company.admin.service.DictService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DictServiceImpl implements DictService {

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    public DictServiceImpl(DictTypeMapper dictTypeMapper,
                           DictItemMapper dictItemMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

    @Override
    public PageResult<DictType> pageDictTypes(DictTypeQueryRequest request) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getDictCode())) {
            wrapper.like(DictType::getDictCode, request.getDictCode());
        }
        if (StringUtils.hasText(request.getDictName())) {
            wrapper.like(DictType::getDictName, request.getDictName());
        }
        if (request.getStatus() != null) {
            wrapper.eq(DictType::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(DictType::getCreateTime);
        Page<DictType> page = dictTypeMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public void createDictType(DictTypeCreateRequest request) {
        Long count = dictTypeMapper.selectCount(
                new LambdaQueryWrapper<DictType>()
                        .eq(DictType::getDictCode, request.getDictCode()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.DICT_CODE_EXISTS);
        }
        DictType dictType = new DictType();
        BeanUtils.copyProperties(request, dictType);
        if (dictType.getStatus() == null) {
            dictType.setStatus(1);
        }
        dictTypeMapper.insert(dictType);
    }

    @Override
    @Transactional
    public void updateDictType(DictTypeUpdateRequest request) {
        DictType dictType = dictTypeMapper.selectById(request.getId());
        if (dictType == null) {
            throw new BusinessException(ErrorCode.DICT_TYPE_NOT_FOUND);
        }
        DictType update = new DictType();
        update.setId(request.getId());
        update.setDictName(request.getDictName());
        update.setStatus(request.getStatus());
        update.setRemark(request.getRemark());
        dictTypeMapper.updateById(update);
    }

    @Override
    @Transactional
    public void deleteDictType(Long id) {
        Long itemCount = dictItemMapper.selectCount(
                new LambdaQueryWrapper<DictItem>()
                        .eq(DictItem::getDictTypeId, id));
        if (itemCount > 0) {
            throw new BusinessException(ErrorCode.DICT_TYPE_HAS_ITEMS);
        }
        dictTypeMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleDictTypeStatus(Long id, Integer status) {
        validateStatus(status);
        int rows = dictTypeMapper.update(null, new UpdateWrapper<DictType>()
                .eq("id", id)
                .set("status", status));
        if (rows == 0) {
            throw new BusinessException(ErrorCode.DICT_TYPE_NOT_FOUND);
        }
    }

    @Override
    public List<DictItem> listDictItems(Long dictTypeId) {
        return dictItemMapper.selectList(
                new LambdaQueryWrapper<DictItem>()
                        .eq(DictItem::getDictTypeId, dictTypeId)
                        .orderByAsc(DictItem::getSortOrder));
    }

    @Override
    @Transactional
    public void createDictItem(DictItemSaveRequest request) {
        DictItem item = new DictItem();
        BeanUtils.copyProperties(request, item);
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        dictItemMapper.insert(item);
    }

    @Override
    @Transactional
    public void updateDictItem(DictItemSaveRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ErrorCode.DICT_ITEM_ID_REQUIRED);
        }
        DictItem existing = dictItemMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.DICT_ITEM_NOT_FOUND);
        }
        DictItem update = new DictItem();
        update.setId(request.getId());
        update.setItemValue(request.getItemValue());
        update.setItemLabel(request.getItemLabel());
        update.setSortOrder(request.getSortOrder());
        update.setStatus(request.getStatus());
        update.setRemark(request.getRemark());
        dictItemMapper.updateById(update);
    }

    @Override
    @Transactional
    public void deleteDictItem(Long id) {
        DictItem item = dictItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.DICT_ITEM_NOT_FOUND);
        }
        dictItemMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleDictItemStatus(Long id, Integer status) {
        validateStatus(status);
        int rows = dictItemMapper.update(null, new UpdateWrapper<DictItem>()
                .eq("id", id)
                .set("status", status));
        if (rows == 0) {
            throw new BusinessException(ErrorCode.DICT_ITEM_NOT_FOUND);
        }
    }

    @Override
    public List<DictItem> getItemsByCode(String dictCode) {
        DictType dictType = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<DictType>()
                        .eq(DictType::getDictCode, dictCode)
                        .eq(DictType::getStatus, 1));
        if (dictType == null) {
            throw new BusinessException(ErrorCode.DICT_TYPE_NOT_FOUND);
        }
        return dictItemMapper.selectList(
                new LambdaQueryWrapper<DictItem>()
                        .eq(DictItem::getDictTypeId, dictType.getId())
                        .eq(DictItem::getStatus, 1)
                        .orderByAsc(DictItem::getSortOrder));
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
