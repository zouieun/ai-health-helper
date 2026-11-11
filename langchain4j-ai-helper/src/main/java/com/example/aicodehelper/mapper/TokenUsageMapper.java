package com.example.aicodehelper.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface TokenUsageMapper {

    @Insert("INSERT INTO token_usage(input_tokens, output_tokens, total_tokens) " +
            "VALUES(#{input}, #{output}, #{total})")
    void record(@Param("input") Integer input,
                @Param("output") Integer output,
                @Param("total") Integer total);
}