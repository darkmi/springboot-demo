package com.darkmi.springboot.jwtlogin.controller;

import com.darkmi.springboot.jwtlogin.annotation.RequiresLogin;
import com.darkmi.springboot.jwtlogin.common.Result;
import com.darkmi.springboot.jwtlogin.interceptor.JwtAuthInterceptor;
import com.darkmi.springboot.jwtlogin.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class LoginController {

    /** 演示用账号，生产环境应查库并用 BCrypt 比对密文 */
    private static final Map<String, String> USERS = Map.of("admin", "admin123");

    private final JwtService jwtService;

    public LoginController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public record LoginReq(String username, String password) {
    }

    /** 登录：POST /auth/login，body: {"username":"admin","password":"admin123"} */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginReq req) {
        String password = USERS.get(req.username());
        if (password == null || !password.equals(req.password())) {
            return Result.error(1, "用户名或密码错误");
        }
        String token = jwtService.issue(req.username());
        return Result.success("登录成功", Map.of("username", req.username(), "token", token));
    }

    /** 受保护接口：登录后携带 token 访问 */
    @RequiresLogin
    @GetMapping("/profile")
    public Result<Map<String, Object>> profile(HttpServletRequest request) {
        String username = (String) request.getAttribute(JwtAuthInterceptor.ATTR_USERNAME);
        return Result.success("ok", Map.of("username", username));
    }
}
