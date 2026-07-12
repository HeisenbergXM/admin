package com.company.admin.service;

import java.util.Map;

/**
 * 车辆全生命周期业务状态展示标签查询服务。
 */
public interface BusinessStatusLabelService {

    String lifecycleStageLabel(String code);

    String stageStatusLabel(String code);

    String orderStatusLabel(String code);

    String dictLabel(String dictCode, String value);

    Map<String, String> dictLabels(String dictCode);
}
