package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DealerCreateRequest;
import com.company.admin.dto.request.DealerQueryRequest;
import com.company.admin.dto.request.DealerUpdateRequest;
import com.company.admin.entity.Dealer;
import com.company.admin.mapper.DealerMapper;
import com.company.admin.service.DealerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DealerServiceImpl implements DealerService {

    private final DealerMapper dealerMapper;

    public DealerServiceImpl(DealerMapper dealerMapper) {
        this.dealerMapper = dealerMapper;
    }

    @Override
    public PageResult<Dealer> pageDealers(DealerQueryRequest request) {
        LambdaQueryWrapper<Dealer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getDealerCode())) {
            wrapper.like(Dealer::getDealerCode, request.getDealerCode());
        }
        if (StringUtils.hasText(request.getDealerName())) {
            wrapper.like(Dealer::getDealerName, request.getDealerName());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Dealer::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Dealer::getCreateTime);
        Page<Dealer> page = dealerMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public void createDealer(DealerCreateRequest request) {
        Long count = dealerMapper.selectCount(
                new LambdaQueryWrapper<Dealer>()
                        .eq(Dealer::getDealerCode, request.getDealerCode()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.DEALER_CODE_EXISTS);
        }
        Dealer dealer = new Dealer();
        BeanUtils.copyProperties(request, dealer);
        dealerMapper.insert(dealer);
    }

    @Override
    @Transactional
    public void updateDealer(DealerUpdateRequest request) {
        Dealer existing = dealerMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(400, "经销商不存在");
        }

        // 检查经销商编码是否重复（排除自身）
        if (StringUtils.hasText(request.getDealerCode())) {
            Long count = dealerMapper.selectCount(
                    new LambdaQueryWrapper<Dealer>()
                            .eq(Dealer::getDealerCode, request.getDealerCode())
                            .ne(Dealer::getId, request.getId()));
            if (count > 0) {
                throw new BusinessException(ErrorCode.DEALER_CODE_EXISTS);
            }
        }

        Dealer update = new Dealer();
        update.setId(request.getId());
        update.setDealerCode(request.getDealerCode());
        update.setDealerName(request.getDealerName());
        update.setStatus(request.getStatus());
        dealerMapper.updateById(update);
    }

    @Override
    @Transactional
    public void deleteDealer(Long id) {
        dealerMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleDealerStatus(Long id, Integer status) {
        Dealer dealer = dealerMapper.selectById(id);
        if (dealer == null) {
            throw new BusinessException(400, "经销商不存在");
        }
        dealer.setStatus(status);
        dealerMapper.updateById(dealer);
    }

    @Override
    public List<Dealer> listAllEnabled() {
        return dealerMapper.selectList(
                new LambdaQueryWrapper<Dealer>()
                        .eq(Dealer::getStatus, 1)
                        .orderByAsc(Dealer::getDealerCode));
    }
}