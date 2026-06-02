package org.example.edufix.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.edufix.common.Result;
import org.example.edufix.dto.CreateTicketRequest;
import org.example.edufix.entity.Ticket;
import org.example.edufix.entity.TicketLog;
import org.example.edufix.exception.BusinessException;
import org.example.edufix.service.StaffService;
import org.example.edufix.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 工单控制器
 */
@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private StaffService staffService;

    /**
     * 创建工单
     */
    @PostMapping
    public Result<Ticket> createTicket(@Validated @RequestBody CreateTicketRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Ticket ticket = ticketService.createTicket(request, userId);
        return Result.success("工单创建成功", ticket);
    }

    /**
     * 获取工单详情
     */
    @GetMapping("/{id}")
    public Result<Ticket> getTicket(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }
        return Result.success(ticket);
    }

    /**
     * 分页查询工单列表
     */
    @GetMapping("/list")
    public Result<Page<Ticket>> getTicketList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");

        if ("STUDENT".equals(role)) {
            Page<Ticket> page = ticketService.getTicketList(pageNum, pageSize, status, category, keyword, userId, null);
            return Result.success(page);
        }

        Long staffId = null;
        if ("STAFF".equals(role)) {
            org.example.edufix.entity.Staff staff = staffService.getStaffByUserId(userId);
            if (staff != null) {
                staffId = staff.getId();
            }
            category = "REPAIR";
        }

        Page<Ticket> page = ticketService.getTicketList(pageNum, pageSize, status, category, keyword, null, staffId);
        return Result.success(page);
    }

    /**
     * 获取工单日志
     */
    @GetMapping("/{id}/logs")
    public Result<List<TicketLog>> getTicketLogs(@PathVariable Long id) {
        List<TicketLog> logs = ticketService.getTicketLogs(id);
        return Result.success(logs);
    }

    /**
     * 取消工单
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelTicket(@PathVariable Long id,
                                     @RequestParam(required = false) String remark,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ticketService.cancelTicket(id, userId, remark);
        return Result.success("工单已取消", null);
    }

    /**
     * 删除工单（逻辑删除）
     */
    @PostMapping("/{id}/delete")
    public Result<Void> deleteTicket(@PathVariable Long id,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        ticketService.deleteTicket(id, userId, role);
        return Result.success("工单已删除", null);
    }

    /**
     * 维修员提交处理结果
     */
    @PutMapping("/{id}/resolve")
    public Result<Void> resolveTicket(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        if (!"STAFF".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }
        org.example.edufix.entity.Staff staff = staffService.getStaffByUserId(userId);
        if (staff == null) {
            throw new BusinessException("员工信息不存在");
        }
        String content = body.getOrDefault("content", "");
        String images = body.getOrDefault("images", "");
        ticketService.resolveTicket(id, staff.getId(), content, images);
        return Result.success("处理结果已提交", null);
    }

    /**
     * 公开失物招领列表
     */
    @GetMapping("/lost-found")
    public Result<Page<Ticket>> getLostFoundList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        Page<Ticket> page = ticketService.getLostFoundList(pageNum, pageSize, type, keyword);
        return Result.success(page);
    }

    /**
     * 提供线索（失物招领）
     */
    @PostMapping("/{id}/clue")
    public Result<Void> provideClue(@PathVariable Long id,
                                     @RequestBody Map<String, String> body,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String message = body.getOrDefault("message", "");
        if (message.trim().isEmpty()) {
            throw new BusinessException("线索内容不能为空");
        }
        ticketService.provideClue(id, userId, message);
        return Result.success("线索已提供", null);
    }

    /**
     * 用户确认完成
     */
    @PutMapping("/{id}/confirm")
    public Result<Void> confirmTicket(@PathVariable Long id,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ticketService.confirmTicket(id, userId);
        return Result.success("已确认完成", null);
    }
}
