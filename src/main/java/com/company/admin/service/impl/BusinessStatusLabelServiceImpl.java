package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.entity.DictItem;
import com.company.admin.entity.DictType;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.OrderStatus;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.DictItemMapper;
import com.company.admin.mapper.DictTypeMapper;
import com.company.admin.service.BusinessStatusLabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessStatusLabelServiceImpl implements BusinessStatusLabelService {

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    @Override
    public String lifecycleStageLabel(String code) {
        return enumLabel(LifecycleStage.class, code);
    }

    @Override
    public String stageStatusLabel(String code) {
        String label = enumLabel(StageStatus.class, code);
        if (label != null && !label.equals(code)) {
            return label;
        }
        return enumLabel(LifecycleStage.class, code);
    }

    @Override
    public String orderStatusLabel(String code) {
        return enumLabel(OrderStatus.class, code);
    }

    @Override
    public String dictLabel(String dictCode, String value) {
        if (value == null) {
            return null;
        }
        return dictLabels(dictCode).getOrDefault(value, value);
    }

    @Override
    public Map<String, String> dictLabels(String dictCode) {
        Map<String, String> labels = new LinkedHashMap<>();
        if (!StringUtils.hasText(dictCode)) {
            return labels;
        }
        DictType dictType = dictTypeMapper.selectOne(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictCode, dictCode)
                .eq(DictType::getStatus, 1));
        if (dictType == null) {
            return labels;
        }
        for (DictItem item : dictItemMapper.selectList(new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getDictTypeId, dictType.getId())
                .eq(DictItem::getStatus, 1)
                .orderByAsc(DictItem::getSortOrder))) {
            if (item.getItemValue() != null && item.getItemLabel() != null) {
                labels.put(item.getItemValue(), item.getItemLabel());
            }
        }
        return labels;
    }

    private <E extends Enum<E>> String enumLabel(Class<E> enumType, String code) {
        if (code == null) {
            return null;
        }
        try {
            if (enumType == LifecycleStage.class) {
                return ((LifecycleStage) Enum.valueOf((Class) enumType, code)).getLabel();
            }
            if (enumType == StageStatus.class) {
                return ((StageStatus) Enum.valueOf((Class) enumType, code)).getLabel();
            }
            if (enumType == OrderStatus.class) {
                return ((OrderStatus) Enum.valueOf((Class) enumType, code)).getLabel();
            }
        } catch (IllegalArgumentException ignored) {
            return code;
        }
        return code;
    }
}
