package org.example.edufix.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    /**
     * 身份: STUDENT-学生, ADMIN-管理员, STAFF-维修员
     */
    @NotBlank(message = "身份不能为空")
    private String role;

    /**
     * 学号/工号
     */
    @NotBlank(message = "学号/工号不能为空")
    private String identifierNo;

    /**
     * 维修专长（仅STAFF角色使用）
     */
    private String specialty;
}
