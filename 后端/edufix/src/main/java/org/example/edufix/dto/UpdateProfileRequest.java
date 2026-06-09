package org.example.edufix.dto;

import lombok.Data;

/**
 * 更新个人信息请求DTO
 */
@Data
public class UpdateProfileRequest {

    private String realName;

    private String phone;

    private String email;

    private String avatar;
}