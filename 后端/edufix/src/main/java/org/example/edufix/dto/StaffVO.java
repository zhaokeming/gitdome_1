package org.example.edufix.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工视图对象（含用户信息）
 */
@Data
public class StaffVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String employeeNo;
    private String department;
    private String specialty;
    private BigDecimal rating;
    private Integer completedCount;
    private Integer status;

    private String realName;
    private String phone;
    private String email;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
