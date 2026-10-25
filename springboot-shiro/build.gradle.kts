plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.darkmi"
version = "0.0.1-SNAPSHOT"
description = "springboot-shiro"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Web 支持：内嵌 Tomcat + Spring MVC（Spring Boot 4.x 中由原 spring-boot-starter-web 更名而来）
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	// Thymeleaf 服务端模板：登录页与后台页面
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	// JDBC 支持：JdbcTemplate + HikariCP 连接池
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	// AOP（Boot 4 中由 spring-boot-starter-aop 更名而来）：AopAutoConfiguration 依赖 AspectJ 才会启用自动代理，
	// 没有它 AuthorizationAttributeSourceAdvisor 不会织入 @RequiresPermissions 注解
	implementation("org.springframework.boot:spring-boot-starter-aspectj")
	// SQLite 驱动（文件数据库，免安装）
	runtimeOnly("org.xerial:sqlite-jdbc:3.47.2.0")
	// Apache Shiro 3.x：手动装配 Realm/SecurityManager/过滤器链。
	// 注意必须用 3.0+——2.x 全系仍是 javax.servlet，与 Boot 4 的 Tomcat 11（jakarta）运行时不兼容；
	// 也不用 shiro-spring-boot-web-starter，其自动配置绑定 Boot 3 时代类结构
	implementation("org.apache.shiro:shiro-spring:3.0.1")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.bootRun {
	// 子 JVM 显式使用 UTF-8 输出，配合 org.gradle.jvmargs 保证 Gradle 链路中文日志不乱码
	jvmArgs("-Dfile.encoding=UTF-8")
}
