package org.example.edufix.controller;

import org.example.edufix.common.Result;
import org.example.edufix.dto.LoginRequest;
import org.example.edufix.dto.LoginResponse;
import org.example.edufix.dto.RegisterRequest;
import org.example.edufix.dto.UpdateProfileRequest;
import org.example.edufix.dto.UserProfileVO;
import org.example.edufix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userService.logout(token);
        }
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前用户个人信息
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        UserProfileVO profile = userService.getProfile(userId);
        return Result.success(profile);
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Validated @RequestBody UpdateProfileRequest updateRequest,
                                      HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        userService.updateProfile(userId, updateRequest);
        return Result.success("更新成功", null);
    }

    /**
     * 从请求中解析当前用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return userService.getUserIdFromToken(token);
        }
        throw new RuntimeException("未登录或Token无效");
    }
}
