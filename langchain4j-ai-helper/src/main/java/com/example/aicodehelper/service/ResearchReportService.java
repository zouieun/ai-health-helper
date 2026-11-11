package com.example.aicodehelper.service;

import com.example.aicodehelper.pojo.vo.ResearchReportVO;

public interface ResearchReportService {
    //根据主题生成研究报告
    ResearchReportVO generate(String topic);
}
