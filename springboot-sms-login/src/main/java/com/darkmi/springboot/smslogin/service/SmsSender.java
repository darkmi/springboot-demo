package com.darkmi.springboot.smslogin.service;

/**
 * 短信发送抽象：屏蔽底层短信服务商差异。
 *
 * <p>示例实现 LogSmsSender 只把短信内容打印到日志；接入阿里云、腾讯云等真实服务商时，
 * 只需按服务商 SDK 实现本接口并替换 Bean 即可，验证码流程代码无需任何改动。</p>
 */
public interface SmsSender {

    /**
     * 发送短信验证码
     *
     * @param mobile 手机号
     * @param code   验证码
     */
    void send(String mobile, String code);
}
