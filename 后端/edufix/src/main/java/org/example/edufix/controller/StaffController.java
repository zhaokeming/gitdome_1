package org.example.edufix.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.edufix.common.Result;
import org.example.edufix.dto.StaffVO;
import org.example.edufix.entity.Staff;
import org.example.edufix.entity.Ticket;
import org.example.edufix.exception.BusinessException;
import org.example.edufix.service.StaffService;
import org.example.edufix.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 维修员控制器
 */
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private StaffService staffService;

    /**
     * 获取员工详情（含用户信息）
     */
    @GetMapping("/{id}")
    public Result<StaffVO> getStaffById(@PathVariable Long id) {
        StaffVO staffVO = staffService.getStaffVOById(id);
        if (staffVO == null) {
            throw new BusinessException("员工不存在");
        }
        return Result.success(staffVO);
    }

    /**
     * 接单
     */
    @PostMapping("/{ticketId}/accept")
    public Result<Void> acceptTicket(@PathVariable Long ticketId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");

        if (!"STAFF".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        Staff staff = staffService.getStaffByUserId(userId);
        if (staff == null) {
            throw new BusinessException("员工信息不存在");
        }

        ticketService.acceptTicket(ticketId, staff.getId());
        return Result.success("接单成功", null);
    }

    /**
     * 完成工单
     */
    @PostMapping("/{ticketId}/complete")
    public Result<Void> completeTicket(@PathVariable Long ticketId,
                                       @RequestParam(required = false) String remark,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");

        if (!"STAFF".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        Staff staff = staffService.getStaffByUserId(userId);
        if (staff == null) {
            throw new BusinessException("员工信息不存在");
        }

        ticketService.completeTicket(ticketId, staff.getId(), remark);
        return Result.success("工单已完成", null);
    }

    /**
     * 获取分配给当前维修员的工单列表
     */
    @GetMapping("/my-tickets")
    public Result<Page<Ticket>> getMyTickets(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");

        if (!"STAFF".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        Staff staff = staffService.getStaffByUserId(userId);
        if (staff == null) {
            throw new BusinessException("员工信息不存在");
        }

        Page<Ticket> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ticket::getStaffId, staff.getId());

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Ticket::getStatus, status);
        }

        wrapper.orderByDesc(Ticket::getCreateTime);

        Page<Ticket> result = ticketService.getTicketMapper().selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 获取当前维修员工单统计数据
     */
    @GetMapping("/my-tickets/stats")
    public Result<java.util.Map<String, Long>> getMyTicketStats(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");

        if (!"STAFF".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        Staff staff = staffService.getStaffByUserId(userId);
        if (staff == null) {
            throw new BusinessException("员工信息不存在");
        }

        java.util.Map<String, Long> stats = new java.util.HashMap<>();

        // 已分配
        stats.put("assigned", ticketService.getTicketMapper().selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStaffId, staff.getId())
                        .eq(Ticket::getStatus, "ASSIGNED")));

        // 处理中
        stats.put("inProgress", ticketService.getTicketMapper().selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStaffId, staff.getId())
                        .eq(Ticket::getStatus, "IN_PROGRESS")));

        // 已处理待确认
        stats.put("resolved", ticketService.getTicketMapper().selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStaffId, staff.getId())
                        .eq(Ticket::getStatus, "RESOLVED")));

        // 已完成
        stats.put("completed", ticketService.getTicketMapper().selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStaffId, staff.getId())
                        .eq(Ticket::getStatus, "COMPLETED")));

        // 总计
        stats.put("total", ticketService.getTicketMapper().selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStaffId, staff.getId())));

        return Result.success(stats);
    }
}
