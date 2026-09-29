package com.darkmi.springboot.captchaplus.controller;

import com.xingyuv.captcha.model.common.ResponseModel;
import com.xingyuv.captcha.model.vo.CaptchaVO;
import com.xingyuv.captcha.service.CaptchaService;
import com.xingyuv.captcha.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码接口：获取验证码 / 前端核对。
 *
 * <p>captcha-plus 只提供服务层能力，未内置 Controller，需自行暴露两个接口：
 * 与芋道源码保持一致，接口路径为 /captcha/get、/captcha/check，
 * 请求头/请求体均由官方前端 verify.js 插件约定，直接对接即可。</p>
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /** 获取验证码（滑动拼图底图 / 文字点选底图，Base64 编码返回） */
    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.get(data);
    }

    /** 前端滑动/点选完成后的核对接口，通过后返回二次校验令牌 captchaVerification */
    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request) {
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.check(data);
    }

    /** 以 客户端IP + User-Agent 作为浏览器标识，参与 AES 加密与接口频控 */
    public static String getRemoteId(HttpServletRequest request) {
        String xfwd = request.getHeader("X-Forwarded-For");
        String ip = getRemoteIpFromXfwd(xfwd);
        String ua = request.getHeader("user-agent");
        if (StringUtils.isNotBlank(ip)) {
            return ip + ua;
        }
        return request.getRemoteAddr() + ua;
    }

    private static String getRemoteIpFromXfwd(String xfwd) {
        if (StringUtils.isNotBlank(xfwd)) {
            String[] ipList = xfwd.split(",");
            return StringUtils.trim(ipList[0]);
        }
        return null;
    }
}
