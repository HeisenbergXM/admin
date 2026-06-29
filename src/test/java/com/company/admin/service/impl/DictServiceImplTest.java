package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.DictItemSaveRequest;
import com.company.admin.dto.request.DictTypeUpdateRequest;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictServiceImplTest {

    @Mock
    private DictTypeMapper dictTypeMapper;

    @Mock
    private DictItemMapper dictItemMapper;

    @InjectMocks
    private DictServiceImpl service;

    @Test
    void updateDictTypeRejectsMissingTypeWithSpecificErrorCode() {
        DictTypeUpdateRequest request = new DictTypeUpdateRequest();
        request.setId(3L);
        when(dictTypeMapper.selectById(3L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateDictType(request));

        assertEquals(ErrorCode.DICT_TYPE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void updateDictItemRequiresIdWithSpecificErrorCode() {
        DictItemSaveRequest request = new DictItemSaveRequest();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateDictItem(request));

        assertEquals(ErrorCode.DICT_ITEM_ID_REQUIRED.getCode(), exception.getCode());
        verifyNoInteractions(dictItemMapper);
    }

    @Test
    void updateDictItemRejectsMissingItemWithSpecificErrorCode() {
        DictItemSaveRequest request = new DictItemSaveRequest();
        request.setId(4L);
        when(dictItemMapper.selectById(4L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateDictItem(request));

        assertEquals(ErrorCode.DICT_ITEM_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void toggleDictTypeStatusRejectsInvalidStatusBeforeUpdating() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.toggleDictTypeStatus(1L, 2));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), exception.getCode());
        verifyNoInteractions(dictTypeMapper);
    }

    @Test
    void toggleDictTypeStatusUpdatesOnlyStatusAndDetectsMissingRows() {
        when(dictTypeMapper.update(isNull(), any())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.toggleDictTypeStatus(1L, 1));

        assertEquals(ErrorCode.DICT_TYPE_NOT_FOUND.getCode(), exception.getCode());
        verify(dictTypeMapper).update(isNull(), any());
        verify(dictTypeMapper, never()).updateById(any(DictType.class));
        verify(dictTypeMapper, never()).selectById(any());
    }

    @Test
    void toggleDictItemStatusRejectsInvalidStatusBeforeUpdating() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.toggleDictItemStatus(1L, null));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), exception.getCode());
        verifyNoInteractions(dictItemMapper);
    }

    @Test
    void toggleDictItemStatusUpdatesOnlyStatusAndDetectsMissingRows() {
        when(dictItemMapper.update(isNull(), any())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.toggleDictItemStatus(1L, 0));

        assertEquals(ErrorCode.DICT_ITEM_NOT_FOUND.getCode(), exception.getCode());
        verify(dictItemMapper).update(isNull(), any());
        verify(dictItemMapper, never()).updateById(any(DictItem.class));
        verify(dictItemMapper, never()).selectById(any());
    }

    @Test
    void deleteDictItemRejectsMissingItem() {
        when(dictItemMapper.selectById(9L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.deleteDictItem(9L));

        assertEquals(ErrorCode.DICT_ITEM_NOT_FOUND.getCode(), exception.getCode());
        verify(dictItemMapper, never()).deleteById(9L);
    }

    @Test
    void getItemsByCodeRejectsMissingEnabledDictType() {
        when(dictTypeMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.getItemsByCode("invoice_status"));

        assertEquals(ErrorCode.DICT_TYPE_NOT_FOUND.getCode(), exception.getCode());
        verifyNoInteractions(dictItemMapper);
    }

    @Test
    void getItemsByCodeReturnsEnabledItemsForExistingType() {
        DictType dictType = new DictType();
        dictType.setId(7L);
        DictItem item = new DictItem();
        item.setId(8L);
        when(dictTypeMapper.selectOne(any())).thenReturn(dictType);
        when(dictItemMapper.selectList(any())).thenReturn(List.of(item));

        List<DictItem> items = service.getItemsByCode("invoice_status");

        assertEquals(1, items.size());
        assertEquals(8L, items.get(0).getId());
    }
}
