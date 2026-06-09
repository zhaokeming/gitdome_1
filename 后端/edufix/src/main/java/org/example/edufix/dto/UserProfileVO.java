package org.example.edufix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资料视图对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileVO {

    private Long id;

    private String username;

    private String realName;

    private String identifierNo;

    private String phone;

    private String email;

    private String role;

    private String specialty;

    private String avatar;
}
