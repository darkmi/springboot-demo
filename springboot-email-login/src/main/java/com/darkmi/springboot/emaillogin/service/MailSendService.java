package com.darkmi.springboot.emaillogin.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务：基于 spring-boot-starter-mail 的 JavaMailSender 发送 HTML 验证码邮件。
 */
@Service
public class MailSendService {

    private final JavaMailSender mailSender;

    /** 发件人，与 spring.mail.username 保持一致，否则多数邮箱服务商会拒信 */
    @Value("${spring.mail.username}")
    private String from;

    public MailSendService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送 HTML 格式的验证码邮件
     */
    public void sendCaptchaMail(String to, String code) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            // 第二个参数 multipart=true 时支持附件/内联图片；纯 HTML 文本传 false 即可
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【springboot-demo】登录验证码");
            helper.setText(buildHtml(code), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("邮件构建失败：" + e.getMessage(), e);
        }
    }

    /**
     * 验证码邮件 HTML 模板：验证码大字号居中展示，减少用户误读
     */
    private String buildHtml(String code) {
        return """
                <div style="max-width:480px;margin:0 auto;padding:24px;
                            font-family:'Microsoft YaHei',sans-serif;color:#333;">
                  <h2 style="font-size:18px;">登录验证码</h2>
                  <p style="font-size:14px;color:#666;">
                    您正在使用邮箱验证码登录 springboot-demo，本次验证码为：
                  </p>
                  <p style="font-size:32px;font-weight:bold;letter-spacing:8px;
                            color:#5b6ff0;text-align:center;margin:24px 0;">%s</p>
                  <p style="font-size:13px;color:#999;">
                    验证码 10 分钟内有效，请勿泄露给他人。若非本人操作，请忽略本邮件。
                  </p>
                </div>
                """.formatted(code);
    }
}
