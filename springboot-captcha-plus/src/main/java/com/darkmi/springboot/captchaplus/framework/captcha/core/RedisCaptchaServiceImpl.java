package com.darkmi.springboot.captchaplus.framework.captcha.core;

import com.xingyuv.captcha.service.CaptchaCacheService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的验证码缓存实现（参考芋道源码 RedisCaptchaServiceImpl）。
 *
 * <p>AJ-Captcha 默认只内置内存缓存（单机），分布式部署时验证码需要集中存储，
 * 通过实现 {@link CaptchaCacheService} 并按 Java SPI 注册（META-INF/services 文件），
 * 即可将缓存切换为 Redis，多实例共享验证码数据。</p>
 */
public class RedisCaptchaServiceImpl implements CaptchaCacheService {

    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String type() {
        // 对应配置项 aj.captcha.cache-type=redis
        return "redis";
    }

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Long increment(String key, long val) {
        return stringRedisTemplate.opsForValue().increment(key, val);
    }

    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
}
