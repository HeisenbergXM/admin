package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class PageRequest {
    @Schema(description = "页码，从 1 开始", example = "1")
    private int pageNum = 1;
    @Schema(description = "每页条数", example = "10")
    private int pageSize = 10;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
