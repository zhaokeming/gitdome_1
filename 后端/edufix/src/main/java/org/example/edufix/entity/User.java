package org.example.edufix.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;

    private String realName;

    /**
     * 学号/工号（学生用学号，管理员/维修员用工号）
     */
    private String identifierNo;

    private String phone;
    
    private String email;
    
    /**
     * 角色: USER-普通用户, STAFF-维修员, ADMIN-管理员
     */
    private String role;
    
    private String avatar;
    
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
