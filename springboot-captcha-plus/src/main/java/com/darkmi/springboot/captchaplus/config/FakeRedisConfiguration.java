package com.darkmi.springboot.captchaplus.config;

import com.github.fppt.jedismock.RedisServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * FakeRedis 装配（仅在 fake-redis profile 下生效）。
 *
 * <p>学员本机未安装 Redis 时，激活 {@code --spring.profiles.active=fake-redis}，
 * 启动 jedis-mock——一个进程内的内存版 Redis 服务端，监听真实 TCP 端口并说 RESP 协议，
 * Spring Data Redis（StringRedisTemplate）按正常方式连接它。</p>
 *
 * <p>因此缓存链路走的是 {@link com.darkmi.springboot.captchaplus.framework.captcha.core.RedisCaptchaServiceImpl}
 * 的真实代码路径，只有"Redis 服务端"是假的；学员装好 Redis 后切到 real-redis
 * profile（或自行改配置）即可无缝换成真实 Redis，应用代码零改动。</p>
 *
 * <p>端口统一取 {@code spring.data.redis.port}（见 application-fake-redis.properties），
 * FakeRedis 与客户端配置只有这一个数据源，改端口只动一处。</p>
 */
@Profile("fake-redis")
@Configuration(proxyBeanMethods = false)
public class FakeRedisConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FakeRedisConfiguration.class);

    @Value("${spring.data.redis.port:6399}")
    private int port;

    private RedisServer redisServer;

    /** 必须早于任何 Redis 命令执行；Lettuce 为懒连接，@PostConstruct 足够 */
    @PostConstruct
    public void start() throws Exception {
        redisServer = RedisServer.newRedisServer(port);
        redisServer.start();
        log.info("FakeRedis（jedis-mock）已启动：127.0.0.1:{}，验证码缓存将写入其中", port);
    }

    @PreDestroy
    public void stop() throws Exception {
        if (redisServer != null && redisServer.isRunning()) {
            redisServer.stop();
            log.info("FakeRedis 已停止");
        }
    }
}
