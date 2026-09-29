package com.darkmi.springboot.ajcaptchaplus.config;

import com.xingyuv.captcha.properties.AjCaptchaProperties;
import com.xingyuv.captcha.service.CaptchaCacheService;
import com.xingyuv.captcha.service.impl.CaptchaServiceFactory;
import com.darkmi.springboot.ajcaptchaplus.framework.captcha.core.RedisCaptchaServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 验证码缓存装配（参考芋道源码 YudaoCaptchaConfiguration）。
 *
 * <p>aj.captcha.cache-type=redis 时，CaptchaServiceFactory 通过 SPI 拿到的
 * RedisCaptchaServiceImpl 是一个"裸"实例，并未持有 RedisTemplate，
 * 这里在 Bean 装配阶段把 StringRedisTemplate 注入进去。</p>
 */
@Configuration(proxyBeanMethods = false)
public class CaptchaConfiguration {

    @Bean
    public CaptchaCacheService captchaCacheService(AjCaptchaProperties config,
                                                   StringRedisTemplate stringRedisTemplate) {
        CaptchaCacheService captchaCacheService = CaptchaServiceFactory.getCache(config.getCacheType().name());
        if (captchaCacheService instanceof RedisCaptchaServiceImpl redisCaptchaService) {
            redisCaptchaService.setStringRedisTemplate(stringRedisTemplate);
        }
        return captchaCacheService;
    }
}
