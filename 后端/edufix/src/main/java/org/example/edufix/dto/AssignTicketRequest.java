package org.example.edufix.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 派单请求DTO
 */
@Data
public class AssignTicketRequest {
    
    @NotNull(message = "工单ID不能为空")
    private Long ticketId;
    
    @NotNull(message = "员工ID不能为空")
    private Long staffId;
    
    private String remark;
}
