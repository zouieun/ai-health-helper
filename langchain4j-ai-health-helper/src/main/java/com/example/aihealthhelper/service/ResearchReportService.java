package com.example.aihealthhelper.service;

import com.example.aihealthhelper.pojo.vo.ResearchReportVO;

public interface ResearchReportService {
    //根据主题生成研究报告
    ResearchReportVO generate(String topic);
}
