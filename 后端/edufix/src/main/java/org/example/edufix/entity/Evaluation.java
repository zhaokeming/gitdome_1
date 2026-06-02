package org.example.edufix.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
@TableName("evaluation")
public class Evaluation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long ticketId;
    
    private Long userId;
    
    private Long staffId;
    
    /**
     * 评分: 1-5星
     */
    private Integer rating;
    
    /**
     * 评价标签(逗号分隔)
     */
    private String tags;
    
    private String content;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
