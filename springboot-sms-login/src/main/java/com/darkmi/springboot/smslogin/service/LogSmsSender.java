package com.darkmi.springboot.smslogin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 模拟短信服务商：验证码内容直接打印到应用日志，便于本地调试。
 *
 * <p>sms.provider=log 或未配置时生效，为默认实现；切换真实服务商见
 * AliyunSmsSender（阿里云 SDK）与 Sms4jSmsSender（sms4j 聚合框架）。</p>
 *
 * <p>真实短信模板示例：
 * 【springboot-demo】您正在登录，验证码 123456，5 分钟内有效，请勿泄露给他人。</p>
 */
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "log", matchIfMissing = true)
public class LogSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LogSmsSender.class);

    @Override
    public void send(String mobile, String code) {
        // 生产环境：调用短信服务商 SDK / HTTP 接口发送，并记录发送流水
        log.info("【模拟短信】向 {} 发送验证码：{}（5 分钟内有效）", mobile, code);
    }
}
