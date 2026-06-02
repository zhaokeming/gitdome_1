package org.example.edufix.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Data
@TableName("notice")
public class Notice implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    /**
     * 类型: NOTICE-公告, NEWS-新闻
     */
    private String type;
    
    /**
     * 优先级: 0-普通, 1-重要, 2-紧急
     */
    private Integer priority;
    
    private LocalDateTime publishTime;
    
    /**
     * 状态: 0-草稿, 1-已发布, 2-已下架
     */
    private Integer status;
    
    private Long publisherId;
    
    private Integer viewCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
