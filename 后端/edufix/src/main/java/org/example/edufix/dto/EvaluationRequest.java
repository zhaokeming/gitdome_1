package org.example.edufix.dto;

import lombok.Data;

/**
 * 评价请求DTO
 */
@Data
public class EvaluationRequest {
    
    private Long ticketId;
    
    private Integer rating;
    
    private String tags;
    
    private String content;
}
