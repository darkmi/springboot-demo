package com.darkmi.springboot.captchaplus.framework.captcha.core;

import com.xingyuv.captcha.model.common.RepCodeEnum;
import com.xingyuv.captcha.model.common.ResponseModel;
import com.xingyuv.captcha.model.vo.CaptchaVO;
import com.xingyuv.captcha.service.impl.AbstractCaptchaService;
import com.xingyuv.captcha.service.impl.CaptchaServiceFactory;
import com.xingyuv.captcha.util.AESUtil;
import com.xingyuv.captcha.util.ImageUtils;
import com.xingyuv.captcha.util.RandomUtils;
import com.xingyuv.captcha.util.StringUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 图片文字验证码（SPI 扩展实现）
 *
 * <p>迁移自 bioai-boot 项目的 {@code PictureWordCaptchaServiceImpl}，原项目基于 anji-plus
 * aj-captcha，本类适配 captcha-plus（com.xingyuv 包名）分支。</p>
 *
 * <p>SPI 注册：{@code META-INF/services/com.xingyuv.captcha.service.CaptchaService}，
 * 由 CaptchaServiceFactory 的 ServiceLoader 按 {@link #captchaType()} 装入实例表；
 * 当 {@code aj.captcha.type=default} 时，DefaultCaptchaServiceImpl 按请求体中的
 * captchaType 路由到本实现（blockPuzzle 滑块 / clickWord 点选 / pictureWord 图形文字）。</p>
 *
 * <p>与滑块/点选不同：验证码文本只存服务端，不回传前端（见 get 中被注释的 setPointJson），
 * 前端把用户输入原样放进 pointJson 提交 check（无需 AES 加密），通过后按库约定拼出
 * captchaVerification = AES(token + "---" + 输入, secretKey) 供登录接口二次校验。</p>
 *
 * @author Tsui
 * @since 2025/7/23
 * @author darkmi 迁移适配 captcha-plus
 */
public class PictureWordCaptchaServiceImpl extends AbstractCaptchaService {

    /**
     * 验证码的基础字符（去掉易混淆的 0/O、1/I）
     */
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    /**
     * 验证码长度
     */
    private static final Integer LENGTH = 4;

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int LINES = 10;

    @Override
    public void init(Properties config) {
        super.init(config);
    }

    @Override
    public void destroy(Properties config) {
        logger.info("start-clear-history-data-{}", captchaType());
    }

    @Override
    public String captchaType() {
        return "pictureWord";
    }

    @Override
    public ResponseModel get(CaptchaVO captchaVO) {
        // 先走父类：设置 clientUid 并做 get 接口频控校验（与 BlockPuzzle 的实现方式保持一致）
        ResponseModel r = super.get(captchaVO);
        if (!validatedReq(r)) {
            return r;
        }

        String text = generateRandomText(LENGTH);
        CaptchaVO imageData = getImageData(text);
        // pointJson 不传到前端，只做后端校验，测试时放开
//        imageData.setPointJson(text);
        return ResponseModel.successData(imageData);
    }

    @Override
    public ResponseModel check(CaptchaVO captchaVO) {
        ResponseModel r = super.check(captchaVO);
        if (!validatedReq(r)) {
            return r;
        }

        // 取出验证码
        String codeKey = String.format(REDIS_CAPTCHA_KEY, captchaVO.getToken());
        if (!CaptchaServiceFactory.getCache(cacheType).exists(codeKey)) {
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_INVALID);
        }
        // 正确的验证码
        String codeValue = CaptchaServiceFactory.getCache(cacheType).get(codeKey);
        String code = getCodeByCodeValue(codeValue);
        String secretKey = getSecretKeyByCodeValue(codeValue);
        // 验证码只用一次，即刻失效
        CaptchaServiceFactory.getCache(cacheType).delete(codeKey);

        // 用户输入的验证码(CaptchaVO 中 没有预留字段，暂时用 pointJson 无需加解密)
        String userCode = captchaVO.getPointJson();
        if (!StringUtils.equalsIgnoreCase(code, userCode)) {
            afterValidateFail(captchaVO);
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_COORDINATE_ERROR);
        }

        // 校验成功，将信息存入缓存
        String value;
        try {
            // 与前端 verify.js 同款拼接：aesStatus 关闭时前端提交明文 token---input，这里需保持一致
            value = captchaAesStatus
                    ? AESUtil.aesEncrypt(captchaVO.getToken().concat("---").concat(userCode), secretKey)
                    : captchaVO.getToken().concat("---").concat(userCode);
        } catch (Exception e) {
            logger.error("AES 加密失败", e);
            afterValidateFail(captchaVO);
            return ResponseModel.errorMsg(e.getMessage());
        }
        String secondKey = String.format(REDIS_SECOND_CAPTCHA_KEY, value);
        CaptchaServiceFactory.getCache(cacheType).set(secondKey, captchaVO.getToken(), EXPIRESIN_THREE);
        captchaVO.setResult(true);
        captchaVO.resetClientFlag();
        return ResponseModel.successData(captchaVO);
    }

    @Override
    public ResponseModel verification(CaptchaVO captchaVO) {
        ResponseModel r = super.verification(captchaVO);
        if (!validatedReq(r)) {
            return r;
        }
        try {
            String codeKey = String.format(REDIS_SECOND_CAPTCHA_KEY, captchaVO.getCaptchaVerification());
            if (!CaptchaServiceFactory.getCache(cacheType).exists(codeKey)) {
                return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_INVALID);
            }
            // 二次校验取值后，即刻失效
            CaptchaServiceFactory.getCache(cacheType).delete(codeKey);
        } catch (Exception e) {
            logger.error("验证码解析失败", e);
            return ResponseModel.errorMsg(e.getMessage());
        }
        return ResponseModel.success();
    }


    private CaptchaVO getImageData(String text) {
        CaptchaVO dataVO = new CaptchaVO();
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景色
        g.setColor(getRandomColor(200, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        // 绘制干扰线
        for (int i = 0; i < LINES; i++) {
            g.setColor(getRandomColor(100, 200));
            int x1 = RandomUtils.getRandomInt(WIDTH);
            int y1 = RandomUtils.getRandomInt(HEIGHT);
            int x2 = RandomUtils.getRandomInt(WIDTH);
            int y2 = RandomUtils.getRandomInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 24));
        // 绘制验证码文本
        for (int i = 0; i < text.length(); i++) {
            g.setColor(getRandomColor(20, 130));
            // 文字旋转
            AffineTransform affineTransform = new AffineTransform();
            int x = 20 + i * 20;
            int y = 24 + RandomUtils.getRandomInt(8);
            // 旋转范围 -45 ~ 45
            affineTransform.setToRotation(Math.toRadians(RandomUtils.getRandomInt(-45, 45)), x, y);
            g.setTransform(affineTransform);
            g.drawString(text.charAt(i) + "", x, y);
        }
        // 添加噪点
        for (int i = 0; i < 100; i++) {
            int x = RandomUtils.getRandomInt(WIDTH);
            int y = RandomUtils.getRandomInt(HEIGHT);
            image.setRGB(x, y, getRandomColor(0, 255).getRGB());
        }
        g.dispose();

        String secretKey = null;
        if (captchaAesStatus) {
            secretKey = AESUtil.getKey();
        }
        dataVO.setSecretKey(secretKey);

        dataVO.setOriginalImageBase64(ImageUtils.getImageToBase64Str(image).replaceAll("\r|\n", ""));
        dataVO.setToken(RandomUtils.getUUID());
        // 将坐标信息存入 redis 中
        String codeKey = String.format(REDIS_CAPTCHA_KEY, dataVO.getToken());
        CaptchaServiceFactory.getCache(cacheType).set(codeKey, getCodeValue(text, secretKey), EXPIRESIN_SECONDS);
        return dataVO;
    }

    private String getCodeValue(String text, String secretKey) {
        return text + "," + secretKey;
    }

    private String getCodeByCodeValue(String codeValue) {
        return codeValue.split(",")[0];
    }

    private String getSecretKeyByCodeValue(String codeValue) {
        return codeValue.split(",")[1];
    }

    private Color getRandomColor(int min, int max) {
        int minVal = Math.min(min, max);
        int maxVal = Math.max(min, max);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int r = random.nextInt(minVal, maxVal);
        int g = random.nextInt(minVal, maxVal);
        int b = random.nextInt(minVal, maxVal);
        return new Color(r, g, b);
    }

    /**
     * 生成指定长度的随机字符串
     *
     * @param length 长度
     * @return {@link String}
     */
    public static String generateRandomText(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

}
