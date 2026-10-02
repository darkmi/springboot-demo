package com.darkmi.springboot.smslogin.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信实现：基于短信 SDK V2.0（dysmsapi 2017-05-25 模型）直连调用 SendSms 接口。
 *
 * <p>前置条件：已完成阿里云实名认证，且短信签名、短信模板审核通过；
 * AccessKey 建议使用仅授予 AliyunDysmsFullAccess 权限的 RAM 子账号。</p>
 *
 * <p>当 {@code sms.provider=aliyun} 时该 Bean 生效，替换默认的 LogSmsSender。</p>
 */
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "aliyun")
public class AliyunSmsSender implements SmsSender {

    /** 发送成功的业务状态码；非 OK 均为失败，如 isv.SMS_SIGNATURE_ILLEGAL（签名不合法） */
    private static final String OK = "OK";

    private final Client client;
    private final String signName;
    private final String templateCode;

    public AliyunSmsSender(
            @Value("${sms.aliyun.access-key-id}") String accessKeyId,
            @Value("${sms.aliyun.access-key-secret}") String accessKeySecret,
            @Value("${sms.aliyun.sign-name}") String signName,
            @Value("${sms.aliyun.template-code}") String templateCode,
            @Value("${sms.aliyun.endpoint:dysmsapi.aliyuncs.com}") String endpoint) throws Exception {
        this.client = new Client(new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(endpoint));
        this.signName = signName;
        this.templateCode = templateCode;
    }

    @Override
    public void send(String mobile, String code) {
        SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(mobile)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                // 模板变量必须是 JSON 字符串，与模板中的 ${code} 变量名对应；
                // 验证码为纯数字，无注入风险，直接拼接即可
                .setTemplateParam("{\"code\":\"" + code + "\"}");
        try {
            SendSmsResponse response = client.sendSms(request);
            String bizCode = response.getBody().getCode();
            if (!OK.equals(bizCode)) {
                // 业务失败（签名/模板/频控等），错误码及排查见阿里云短信错误码文档
                throw new SmsSendException("阿里云短信发送失败：" + bizCode + " - " + response.getBody().getMessage());
            }
        } catch (SmsSendException e) {
            throw e;
        } catch (Exception e) {
            // 网络异常、鉴权失败等 SDK 层异常
            throw new SmsSendException("阿里云短信发送异常：" + e.getMessage(), e);
        }
    }
}
