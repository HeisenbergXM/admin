package com.company.admin.service.impl;

import com.company.admin.entity.DictItem;
import com.company.admin.entity.DictType;
import com.company.admin.mapper.DictItemMapper;
import com.company.admin.mapper.DictTypeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessStatusLabelServiceImplTest {

    @Mock
    private DictTypeMapper dictTypeMapper;

    @Mock
    private DictItemMapper dictItemMapper;

    @InjectMocks
    private BusinessStatusLabelServiceImpl service;

    @Test
    void resolvesEnumLabelsAndFallsBackToCode() {
        assertEquals("待收款", service.lifecycleStageLabel("PENDING_PAYMENT"));
        assertEquals("草稿", service.stageStatusLabel("DRAFT"));
        assertEquals("已确认", service.orderStatusLabel("CONFIRMED"));
        assertEquals("UNEXPECTED", service.lifecycleStageLabel("UNEXPECTED"));
        assertNull(service.stageStatusLabel(null));
    }

    @Test
    void resolvesEnabledDictionaryLabelAndFallsBackToValue() {
        DictType type = new DictType();
        type.setId(14L);
        DictItem item = new DictItem();
        item.setDictTypeId(14L);
        item.setItemValue("UNPAID");
        item.setItemLabel("未收款");
        when(dictTypeMapper.selectOne(any())).thenReturn(type);
        when(dictItemMapper.selectList(any())).thenReturn(List.of(item));

        assertEquals("未收款", service.dictLabel("payment_status", "UNPAID"));
        assertEquals("UNKNOWN", service.dictLabel("payment_status", "UNKNOWN"));
        assertNull(service.dictLabel("payment_status", null));
    }
}
