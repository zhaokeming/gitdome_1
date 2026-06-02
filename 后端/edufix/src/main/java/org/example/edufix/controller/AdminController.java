package org.example.edufix.controller;

import org.example.edufix.common.Result;
import org.example.edufix.dto.AssignTicketRequest;
import org.example.edufix.dto.StaffVO;
import org.example.edufix.entity.Ticket;
import org.example.edufix.exception.BusinessException;
import org.example.edufix.service.StaffService;
import org.example.edufix.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private StaffService staffService;

    /**
     * 派单
     */
    @PostMapping("/assign")
    public Result<Void> assignTicket(@Validated @RequestBody AssignTicketRequest request,
                                     HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        ticketService.assignTicket(request.getTicketId(), request.getStaffId(),
                operatorId, request.getRemark());
        return Result.success("派单成功", null);
    }

    /**
     * 处理建议工单
     */
    @PostMapping("/process-suggestion")
    public Result<Void> processSuggestion(@RequestBody java.util.Map<String, Object> request,
                                          HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        Long ticketId = Long.valueOf(request.get("ticketId").toString());
        String content = request.getOrDefault("content", "").toString();
        ticketService.processSuggestion(ticketId, operatorId, content);
        return Result.success("建议已处理", null);
    }

    /**
     * 获取待处理工单列表（返回完整工单对象）
     */
    @GetMapping("/pending-tickets")
    public Result<List<Ticket>> getPendingTickets(HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        List<Ticket> pendingTickets = ticketService.getPendingTickets();
        return Result.success(pendingTickets);
    }

    /**
     * 获取高优先级工单（返回完整工单对象）
     */
    @GetMapping("/high-priority-tickets")
    public Result<List<Ticket>> getHighPriorityTickets(
            @RequestParam(defaultValue = "10") int topN,
            HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        List<Ticket> highPriorityTickets = ticketService.getHighPriorityTickets(topN);
        return Result.success(highPriorityTickets);
    }

    /**
     * 获取员工列表（含用户信息）
     */
    @GetMapping("/staff/list")
    public Result<List<StaffVO>> getStaffList() {
        List<StaffVO> staffList = staffService.getAllStaff();
        return Result.success(staffList);
    }
}
