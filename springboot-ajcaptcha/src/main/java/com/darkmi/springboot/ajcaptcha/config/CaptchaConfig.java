package com.darkmi.springboot.ajcaptcha.config;

import com.anji.captcha.model.common.Const;
import com.anji.captcha.properties.AjCaptchaProperties;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import com.anji.captcha.util.ImageUtils;
import com.anji.captcha.util.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * AJ-Captcha 行为验证码装配。
 *
 * <p>官方 spring-boot-starter-captcha 通过 META-INF/spring.factories 完成自动装配，
 * Spring Boot 3+ 已不再支持该机制，因此这里手动注册官方的 CaptchaService Bean，
 * 装配逻辑与官方 AjCaptchaServiceAutoConfiguration 保持一致。</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AjCaptchaProperties.class)
public class CaptchaConfig {

	@Bean
	public CaptchaService captchaService(AjCaptchaProperties prop) {
		// 将 aj.captcha.* 配置项转换为官方 CaptchaService 所需的 Properties
		Properties config = new Properties();
		config.put(Const.CAPTCHA_CACHETYPE, prop.getCacheType().name());
		config.put(Const.CAPTCHA_WATER_MARK, prop.getWaterMark());
		config.put(Const.CAPTCHA_FONT_TYPE, prop.getFontType());
		config.put(Const.CAPTCHA_TYPE, prop.getType().getCodeValue());
		config.put(Const.CAPTCHA_INTERFERENCE_OPTIONS, prop.getInterferenceOptions());
		config.put(Const.ORIGINAL_PATH_JIGSAW, prop.getJigsaw());
		config.put(Const.ORIGINAL_PATH_PIC_CLICK, prop.getPicClick());
		config.put(Const.CAPTCHA_SLIP_OFFSET, prop.getSlipOffset());
		config.put(Const.CAPTCHA_AES_STATUS, String.valueOf(prop.getAesStatus()));
		config.put(Const.CAPTCHA_WATER_FONT, prop.getWaterFont());
		config.put(Const.CAPTCHA_CACAHE_MAX_NUMBER, prop.getCacheNumber());
		config.put(Const.CAPTCHA_TIMING_CLEAR_SECOND, prop.getTimingClear());
		config.put(Const.HISTORY_DATA_CLEAR_ENABLE, prop.isHistoryDataClearEnable() ? "1" : "0");
		config.put(Const.REQ_FREQUENCY_LIMIT_ENABLE, prop.getReqFrequencyLimitEnable() ? "1" : "0");
		config.put(Const.REQ_GET_LOCK_LIMIT, prop.getReqGetLockLimit() + "");
		config.put(Const.REQ_GET_LOCK_SECONDS, prop.getReqGetLockSeconds() + "");
		config.put(Const.REQ_GET_MINUTE_LIMIT, prop.getReqGetMinuteLimit() + "");
		config.put(Const.REQ_CHECK_MINUTE_LIMIT, prop.getReqCheckMinuteLimit() + "");
		config.put(Const.REQ_VALIDATE_MINUTE_LIMIT, prop.getReqVerifyMinuteLimit() + "");
		config.put(Const.CAPTCHA_FONT_SIZE, prop.getFontSize() + "");
		config.put(Const.CAPTCHA_FONT_STYLE, prop.getFontStyle() + "");
		config.put(Const.CAPTCHA_WORD_COUNT, prop.getClickWordCount() + "");

		if ((StringUtils.isNotBlank(prop.getJigsaw()) && prop.getJigsaw().startsWith("classpath"))
				|| (StringUtils.isNotBlank(prop.getPicClick()) && prop.getPicClick().startsWith("classpath"))) {
			// 底图配置在项目 resources 目录下时，启动时预加载并缓存
			config.put(Const.CAPTCHA_INIT_ORIGINAL, "true");
			ImageUtils.cacheBootImage(getResourcesImagesFile(prop.getJigsaw() + "/original/*.png"),
					getResourcesImagesFile(prop.getJigsaw() + "/slidingBlock/*.png"),
					getResourcesImagesFile(prop.getPicClick() + "/*.png"));
		}
		return CaptchaServiceFactory.getInstance(config);
	}

	/** 读取 classpath 下底图，Base64 编码后放入底图缓存 */
	private Map<String, String> getResourcesImagesFile(String path) {
		Map<String, String> imgMap = new HashMap<>();
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			for (Resource resource : resolver.getResources(path)) {
				byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
				imgMap.put(resource.getFilename(), Base64.getEncoder().encodeToString(bytes));
			}
		} catch (Exception e) {
			throw new IllegalStateException("加载验证码底图失败：" + path, e);
		}
		return imgMap;
	}
}
