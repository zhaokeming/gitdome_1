package org.example.edufix.controller;

import org.example.edufix.common.Result;
import org.example.edufix.dto.EvaluationRequest;
import org.example.edufix.entity.Evaluation;
import org.example.edufix.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 创建评价
     */
    @PostMapping
    public Result<Void> createEvaluation(@Validated @RequestBody EvaluationRequest request,
                                         HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        evaluationService.createEvaluation(request, userId);
        return Result.success("评价成功", null);
    }

    /**
     * 获取工单评价
     */
    @GetMapping("/ticket/{ticketId}")
    public Result<Evaluation> getEvaluationByTicketId(@PathVariable Long ticketId) {
        Evaluation evaluation = evaluationService.getEvaluationByTicketId(ticketId);
        return Result.success(evaluation);
    }

    /**
     * 获取员工评价列表
     */
    @GetMapping("/staff/{staffId}")
    public Result<List<Evaluation>> getEvaluationsByStaffId(@PathVariable Long staffId) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsByStaffId(staffId);
        return Result.success(evaluations);
    }
}
