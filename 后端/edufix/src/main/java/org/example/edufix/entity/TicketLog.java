package org.example.edufix.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单流转日志实体类
 */
@Data
@TableName("ticket_log")
public class TicketLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long ticketId;
    
    private Long operatorId;
    
    private String operatorName;
    
    /**
     * 操作: CREATE-创建, ASSIGN-派单, ACCEPT-接单, PROCESS-处理, COMPLETE-完成, CANCEL-取消, EVALUATE-评价
     */
    private String action;
    
    private String fromStatus;
    
    private String toStatus;
    
    private String content;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
