package org.example.edufix.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 创建工单请求DTO
 */
@Data
public class CreateTicketRequest {
    
    @NotBlank(message = "工单标题不能为空")
    private String title;
    
    private String description;
    
    @NotBlank(message = "分类不能为空")
    private String category;
    
    private String type;
    
    private String location;
    
    private String urgency;
    
    private String contactInfo;
    
    private String images;
    
    private String remark;
}
