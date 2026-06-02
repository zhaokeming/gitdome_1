package org.example.edufix.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单实体类
 */
@Data
@TableName("ticket")
public class Ticket implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String ticketNo;
    
    private String title;
    
    private String description;
    
    /**
     * 分类: REPAIR-报修, SUGGESTION-建议, LOST_FOUND-失物招领
     */
    private String category;
    
    /**
     * 具体类型
     */
    private String type;
    
    private String location;
    
    /**
     * 紧急程度: LOW-低, NORMAL-普通, HIGH-高, URGENT-紧急
     */
    private String urgency;
    
    /**
     * 状态: PENDING-待处理, ASSIGNED-已派单, IN_PROGRESS-处理中, RESOLVED-已处理待确认, COMPLETED-已完成, PROCESSED-已处理, CANCELLED-已取消
     * 维修: PENDING(待派单)→ASSIGNED(待接单)→IN_PROGRESS(处理中)→COMPLETED(已完成)
     * 建议: PENDING(未处理)→PROCESSED(已处理)
     * 失物招领: 保持原流程
     */
    private String status;
    
    private Long userId;
    
    private Long staffId;
    
    /**
     * 图片URL列表(逗号分隔)
     */
    private String images;
    
    private String contactInfo;
    
    private String remark;
    
    /**
     * 优先级分数(用于ZSet排序)
     */
    private Double priorityScore;
    
    private LocalDateTime completeTime;
    
    /**
     * 维修员处理结果描述
     */
    private String resolveContent;
    
    /**
     * 维修后图片URL列表(逗号分隔)
     */
    private String resolveImages;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
