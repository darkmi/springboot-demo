plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.darkmi"
version = "0.0.1-SNAPSHOT"
description = "springboot-sms-login"

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
	// Hutool 工具库：手机号校验（PhoneUtil）、UUID（IdUtil）等
	implementation("cn.hutool:hutool-core:5.8.47")
	// 阿里云短信 SDK V2.0（dysmsapi 2017-05-25 模型），sms.provider=aliyun 时使用
	implementation("com.aliyun:dysmsapi20170525:4.1.1")
	// sms4j 短信聚合框架：一套 API 对接阿里云/腾讯云/华为云等多厂商，sms.provider=sms4j 时使用
	implementation("org.dromara.sms4j:sms4j-spring-boot-starter:3.3.5")
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
