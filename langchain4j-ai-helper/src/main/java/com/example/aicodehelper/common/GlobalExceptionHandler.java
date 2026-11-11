package com.example.aicodehelper.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 全局异常处理器：拦截所有 Controller 抛出的异常，统一转成 Result 格式。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 业务异常：可预期，message 直接返回给用户 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /** report 接口目前的参数校验异常（第3步会换成 @Valid） */
    @ExceptionHandler(ResponseStatusException.class)
    public Result<Void> handleResponseStatusException(ResponseStatusException e) {
        log.warn("请求参数异常：{}", e.getReason());
        return Result.error(e.getReason());
    }

    /** 兜底：未预期的系统异常，不暴露内部细节，完整堆栈记进日志 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后再试");
    }
}