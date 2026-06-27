package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DealerCreateRequest;
import com.company.admin.dto.request.DealerQueryRequest;
import com.company.admin.dto.request.DealerUpdateRequest;
import com.company.admin.entity.Dealer;

import java.util.List;

/**
 * 经销商管理服务接口
 */
public interface DealerService {

    /** 经销商分页列表 */
    PageResult<Dealer> pageDealers(DealerQueryRequest request);

    /** 新增经销商 */
    void createDealer(DealerCreateRequest request);

    /** 编辑经销商 */
    void updateDealer(DealerUpdateRequest request);

    /** 删除经销商 */
    void deleteDealer(Long id);

    /** 启停经销商 */
    void toggleDealerStatus(Long id, Integer status);

    /** 获取所有启用经销商（下拉用） */
    List<Dealer> listAllEnabled();
}