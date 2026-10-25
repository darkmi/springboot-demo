package com.darkmi.springboot.jwtlogin.interceptor;

import com.darkmi.springboot.jwtlogin.annotation.RequiresLogin;
import com.darkmi.springboot.jwtlogin.common.Result;
import com.darkmi.springboot.jwtlogin.service.JwtService;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    /** 当前用户在请求属性中的 key，业务代码由此取登录人 */
    public static final String ATTR_USERNAME = "jwtUsername";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthInterceptor(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 1. 只拦 Controller 方法，静态资源等直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 2. 没有 @RequiresLogin 注解的接口不做认证
        if (!handlerMethod.hasMethodAnnotation(RequiresLogin.class)) {
            return true;
        }

        // 3. 取出并解析 token
        String token = resolveToken(request.getHeader("Authorization"));
        if (token == null) {
            write401(response, "未登录或 token 缺失");
            return false;
        }
        try {
            Claims claims = jwtService.parse(token);
            // 4. 用户标识放入请求属性，业务代码直接读取
            request.setAttribute(ATTR_USERNAME, claims.getSubject());
            return true;
        } catch (ExpiredJwtException e) {
            write401(response, "token 已过期，请重新登录");
            return false;
        } catch (JwtException e) {
            write401(response, "token 非法");
            return false;
        }
    }

    private String resolveToken(String authorization) {
        // scheme 名大小写不敏感（RFC 6750），bearer / Bearer / BEARER 均可
        if (StringUtils.hasText(authorization)
                && authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void write401(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}
