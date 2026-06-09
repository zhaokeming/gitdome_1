package org.example.edufix.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.edufix.dto.LoginRequest;
import java.math.BigDecimal;
import org.example.edufix.dto.LoginResponse;
import org.example.edufix.dto.RegisterRequest;
import org.example.edufix.dto.UpdateProfileRequest;
import org.example.edufix.dto.UserProfileVO;
import org.example.edufix.entity.Staff;
import org.example.edufix.entity.User;
import org.example.edufix.exception.BusinessException;
import org.example.edufix.mapper.StaffMapper;
import org.example.edufix.mapper.UserMapper;
import org.example.edufix.util.JwtUtil;
import org.example.edufix.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private StaffMapper staffMapper;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getRealName()
        );
    }

    /**
     * 用户注册
     */
    public void register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 校验角色合法性
        String role = request.getRole();
        if (!"STUDENT".equals(role) && !"ADMIN".equals(role) && !"STAFF".equals(role)) {
            throw new BusinessException("无效的用户身份");
        }

        // 检查学号/工号是否已被使用
        LambdaQueryWrapper<User> idWrapper = new LambdaQueryWrapper<>();
        idWrapper.eq(User::getIdentifierNo, request.getIdentifierNo());
        Long idCount = userMapper.selectCount(idWrapper);
        if (idCount > 0) {
            throw new BusinessException("该学号/工号已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setIdentifierNo(request.getIdentifierNo());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setSpecialty(request.getSpecialty());
        user.setStatus(1);

        userMapper.insert(user);

        // 维修员角色：自动创建员工记录
        if ("STAFF".equals(role)) {
            Staff staff = new Staff();
            staff.setUserId(user.getId());
            staff.setEmployeeNo(request.getIdentifierNo());
            staff.setDepartment("维修部");
            staff.setSpecialty(request.getSpecialty() != null ? request.getSpecialty() : "水电维修");
            staff.setStatus(1);
            staff.setRating(BigDecimal.valueOf(5.0));
            staff.setCompletedCount(0);
            staffMapper.insert(staff);
        }
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        String blacklistKey = "token:blacklist:" + token;
        redisUtil.setEx(blacklistKey, "1", 86400);
    }

    /**
     * 根据ID获取用户信息
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 验证Token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String blacklistKey = "token:blacklist:" + token;
        return redisUtil.hasKey(blacklistKey);
    }

    /**
     * 获取用户完整资料
     */
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return new UserProfileVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getIdentifierNo(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getSpecialty(),
                user.getAvatar()
        );
    }

    /**
     * 更新个人信息
     */
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return jwtUtil.getUserIdFromToken(token);
    }
}
