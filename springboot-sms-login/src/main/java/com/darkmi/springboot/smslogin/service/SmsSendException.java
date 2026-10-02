package com.darkmi.springboot.smslogin.service;

/**
 * 短信发送失败异常：真实服务商实现（阿里云 SDK / sms4j）在业务失败或网络异常时抛出，
 * 由 SmsCodeService 统一捕获并转换为友好的业务提示。
 */
public class SmsSendException extends RuntimeException {

    public SmsSendException(String message) {
        super(message);
    }

    public SmsSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
