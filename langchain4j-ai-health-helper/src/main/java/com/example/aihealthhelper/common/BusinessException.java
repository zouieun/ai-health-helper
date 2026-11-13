package com.example.aihealthhelper.common;

/**
 * 业务异常：可预期的错误（账号不存在、密码错误、用户名已存在等）。
 * 它的 message 可以直接返回给用户；以此和未预期的系统异常区分开。
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}