package com.example.aihealthhelper.pojo.vo;

import java.util.List;

public record ResearchReportVO(
        Status status,
        String topic,
        String answer,
        List<Source> sources,
        List<ToolTrace> toolTrace,
        long elapsedMs
) {
    public enum Status {
        SUCCESS,
        DEGRADED,
        FAILED
    }

    public record Source(
            String id,
            String type,
            String title,
            String url,
            String snippet,
            Double score
    ) {
    }

    public record ToolTrace(
            String step,
            String tool,
            String status,
            String detail,
            long elapsedMs
    ) {
    }
}