package com.example.aicodehelper.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果封装。
 * 所有非流式接口都返回它，前端按 code/msg/data 统一解析。
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码：1 成功，0 失败 */
    private Integer code;

    /** 提示信息（失败时是错误原因） */
    private String msg;

    /** 业务数据 */
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 1;
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.msg = msg;
        return result;
    }
}