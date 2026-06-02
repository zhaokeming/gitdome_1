package org.example.edufix.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工信息实体类
 */
@Data
@TableName("staff")
public class Staff implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String employeeNo;
    
    private String department;
    
    private String specialty;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 完成工单数
     */
    private Integer completedCount;
    
    /**
     * 状态: 0-离线, 1-空闲, 2-忙碌(1个未完成), 3-繁忙(2个及以上未完成)
     */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
