package com.darkmi.springboot.smslogin.service;

import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * sms4j 聚合框架实现：一套 API 对接阿里云、腾讯云、华为云等多家厂商。
 *
 * <p>厂商配置在 application.properties 的 {@code sms.blends.*} 中声明，
 * 可同时配置多个厂商实例；发送时按 configId 从工厂获取实例，
 * 切换厂商、多实例负载均衡都只需改配置，业务代码零改动。</p>
 *
 * <p>当 {@code sms.provider=sms4j} 时该 Bean 生效，替换默认的 LogSmsSender。</p>
 */
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "sms4j")
public class Sms4jSmsSender implements SmsSender {

    /** 使用的厂商实例标识，对应 sms.blends.<configId> */
    private final String configId;

    public Sms4jSmsSender(@Value("${sms4j.config-id:aliyun1}") String configId) {
        this.configId = configId;
    }

    @Override
    public void send(String mobile, String code) {
        // sendMessage 走配置中预设的模板（template-id + template-name），
        // message 即模板变量的值："您的验证码为${code}"中的 code=123456
        SmsBlend smsBlend = SmsFactory.getSmsBlend(configId);
        SmsResponse response = smsBlend.sendMessage(mobile, code);
        if (!response.isSuccess()) {
            throw new SmsSendException("sms4j 短信发送失败：" + response.getData());
        }
    }
}
