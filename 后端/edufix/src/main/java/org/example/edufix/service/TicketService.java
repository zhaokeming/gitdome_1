package org.example.edufix.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.example.edufix.dto.CreateTicketRequest;
import org.example.edufix.entity.Staff;
import org.example.edufix.entity.Ticket;
import org.example.edufix.entity.TicketLog;
import org.example.edufix.entity.User;
import org.example.edufix.exception.BusinessException;
import org.example.edufix.mapper.StaffMapper;
import org.example.edufix.mapper.TicketLogMapper;
import org.example.edufix.mapper.TicketMapper;
import org.example.edufix.mapper.UserMapper;
import org.example.edufix.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * 工单服务类
 */
@Service
public class TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private TicketLogMapper ticketLogMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserMapper userMapper;

    // 紧急程度权重
    private static final double URGENCY_LOW = 1.0;
    private static final double URGENCY_NORMAL = 2.0;
    private static final double URGENCY_HIGH = 4.0;
    private static final double URGENCY_URGENT = 8.0;

    /**
     * 创建工单
     */
    @Transactional(rollbackFor = Exception.class)
    public Ticket createTicket(CreateTicketRequest request, Long userId) {
        // 生成工单编号
        String ticketNo = "TKT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // 创建工单
        Ticket ticket = new Ticket();
        ticket.setTicketNo(ticketNo);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setType(request.getType());
        ticket.setLocation(request.getLocation());
        ticket.setUrgency(StringUtils.isNotBlank(request.getUrgency()) ? request.getUrgency() : "NORMAL");
        ticket.setStatus("PENDING");
        ticket.setUserId(userId);
        ticket.setContactInfo(request.getContactInfo());
        ticket.setImages(request.getImages());
        ticket.setRemark(request.getRemark());

        // 计算优先级分数 = 紧急程度权重 * 1000 + 时间戳(越小越新)
        double urgencyScore = getUrgencyScore(ticket.getUrgency());
        long timeScore = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        ticket.setPriorityScore(urgencyScore * 1000000000L + (Long.MAX_VALUE - timeScore));

        ticketMapper.insert(ticket);

        // 记录日志
        addTicketLog(ticket.getId(), null, null, "CREATE", null, "PENDING", "创建工单");

        // 添加到Redis Set: 待处理工单集合
        redisUtil.sAdd("ticket:status:PENDING", ticket.getId());

        // 添加到Redis ZSet: 优先级队列
        redisUtil.zAdd("ticket:priority:queue", ticket.getId(), ticket.getPriorityScore());

        // 通知所有管理员有新工单
        notifyAdmins("新工单提交",
                "有一个新的「" + getCategoryName(ticket.getCategory()) + "」工单：" + ticket.getTitle(),
                ticket.getId());

        return ticket;
    }

    /**
     * 派单
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignTicket(Long ticketId, Long staffId, Long operatorId, String remark) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if (!"PENDING".equals(ticket.getStatus())) {
            throw new BusinessException("工单状态不允许派单");
        }

        // 检查员工是否存在
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            throw new BusinessException("员工不存在");
        }

        String oldStatus = ticket.getStatus();
        ticket.setStatus("ASSIGNED");
        ticket.setStaffId(staffId);
        ticketMapper.updateById(ticket);

        // 记录日志
        addTicketLog(ticketId, operatorId, null, "ASSIGN", oldStatus, "ASSIGNED",
                StringUtils.isNotBlank(remark) ? remark : "派单给员工: " + staff.getEmployeeNo());

        // 更新Redis Set
        redisUtil.sRemove("ticket:status:PENDING", ticketId);
        redisUtil.sAdd("ticket:status:ASSIGNED", ticketId);

        // 从优先级队列移除
        redisUtil.zRemove("ticket:priority:queue", ticketId);

        // 发送通知给工单提交者
        notificationService.createNotification(ticket.getUserId(),
                "工单已派单",
                "您的工单「" + ticket.getTitle() + "」已分配给维修员处理。",
                "TICKET", ticketId);

        // 发送通知给被指派的维修员
        notificationService.createNotification(staff.getUserId(),
                "新工单待处理",
                "您有一个新的报修工单「" + ticket.getTitle() + "」待处理，请及时查看。",
                "TICKET", ticketId);
    }

    /**
     * 撤销派单（管理员将已派单但未接单的工单退回待派单状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeAssignment(Long ticketId, Long operatorId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if (!"ASSIGNED".equals(ticket.getStatus())) {
            throw new BusinessException("仅已派单但未接单的工单允许撤销派单");
        }

        Long oldStaffId = ticket.getStaffId();
        String oldStatus = ticket.getStatus();

        // 重置为待派单状态
        ticket.setStatus("PENDING");
        ticket.setStaffId(null);
        ticketMapper.updateById(ticket);

        // 记录日志
        addTicketLog(ticketId, operatorId, null, "REVOKE", oldStatus, "PENDING", "撤销派单");

        // 更新Redis Set
        redisUtil.sRemove("ticket:status:ASSIGNED", ticketId);
        redisUtil.sAdd("ticket:status:PENDING", ticketId);

        // 重新加入优先级队列
        double urgencyScore = getUrgencyScore(ticket.getUrgency());
        long timeScore = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        double priorityScore = urgencyScore * 1000000000L + (Long.MAX_VALUE - timeScore);
        ticket.setPriorityScore(priorityScore);
        ticketMapper.updateById(ticket);
        redisUtil.zAdd("ticket:priority:queue", ticketId, priorityScore);

        // 通知被撤销的维修员
        if (oldStaffId != null) {
            Staff staff = staffMapper.selectById(oldStaffId);
            if (staff != null) {
                notificationService.createNotification(staff.getUserId(),
                        "工单派单已撤销",
                        "工单「" + ticket.getTitle() + "」的派单已被管理员撤销。",
                        "TICKET", ticketId);
            }
            // 更新原维修员状态
            updateStaffStatus(oldStaffId);
        }

        // 通知工单提交者
        notificationService.createNotification(ticket.getUserId(),
                "工单重新待派单",
                "您的工单「" + ticket.getTitle() + "」的派单已被撤销，正在等待重新分配。",
                "TICKET", ticketId);
    }

    /**
     * 接单
     */
    @Transactional(rollbackFor = Exception.class)
    public void acceptTicket(Long ticketId, Long staffId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if (!"ASSIGNED".equals(ticket.getStatus())) {
            throw new BusinessException("工单状态不允许接单");
        }

        if (!staffId.equals(ticket.getStaffId())) {
            throw new BusinessException("无权接此工单");
        }

        String oldStatus = ticket.getStatus();
        ticket.setStatus("IN_PROGRESS");
        ticketMapper.updateById(ticket);

        // 记录日志
        addTicketLog(ticketId, staffId, null, "ACCEPT", oldStatus, "IN_PROGRESS", "开始处理工单");

        // 更新Redis Set
        redisUtil.sRemove("ticket:status:ASSIGNED", ticketId);
        redisUtil.sAdd("ticket:status:IN_PROGRESS", ticketId);

        // 自动更新员工状态
        updateStaffStatus(staffId);
    }

    /**
     * 完成工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTicket(Long ticketId, Long staffId, String remark) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if (!"IN_PROGRESS".equals(ticket.getStatus())) {
            throw new BusinessException("工单状态不允许完成");
        }

        String oldStatus = ticket.getStatus();
        ticket.setStatus("COMPLETED");
        ticket.setCompleteTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);

        // 记录日志
        addTicketLog(ticketId, staffId, null, "COMPLETE", oldStatus, "COMPLETED",
                StringUtils.isNotBlank(remark) ? remark : "完成工单");

        // 更新Redis Set
        redisUtil.sRemove("ticket:status:IN_PROGRESS", ticketId);
        redisUtil.sAdd("ticket:status:COMPLETED", ticketId);

        // 更新员工统计数据，自动更新状态
        Staff staff = staffMapper.selectById(staffId);
        if (staff != null) {
            staff.setCompletedCount(staff.getCompletedCount() + 1);
            staffMapper.updateById(staff);
        }
        updateStaffStatus(staffId);
    }

    /**
     * 维修员提交处理结果（状态变为已处理待确认）
     */
    @Transactional(rollbackFor = Exception.class)
    public void resolveTicket(Long ticketId, Long staffId, String content, String images) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if (!"IN_PROGRESS".equals(ticket.getStatus())) {
            throw new BusinessException("工单状态不允许提交处理结果");
        }

        if (!staffId.equals(ticket.getStaffId())) {
            throw new BusinessException("无权处理此工单");
        }

        String oldStatus = ticket.getStatus();
        ticket.setStatus("RESOLVED");
        ticket.setResolveContent(content);
        ticket.setResolveImages(images);
        ticketMapper.updateById(ticket);

        // 记录日志
        addTicketLog(ticketId, staffId, null, "RESOLVE", oldStatus, "RESOLVED",
                "提交处理结果: " + (content != null ? content : ""));

        // 更新Redis Set
        redisUtil.sRemove("ticket:status:IN_PROGRESS", ticketId);
        redisUtil.sAdd("ticket:status:RESOLVED", ticketId);

        // 自动更新员工状态
        updateStaffStatus(staffId);

        // 发送通知给工单提交者
        notificationService.createNotification(ticket.getUserId(),
                "工单已处理",
                "您的工单「" + ticket.getTitle() + "」维修员已提交处理结果，请确认是否完成。",
                "TICKET", ticketId);
    }

    /**
     * 用户确认完成（状态变为已完成）
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if (!"RESOLVED".equals(ticket.getStatus())) {
            throw new BusinessException("工单状态不允许确认完成");
        }

        if (!userId.equals(ticket.getUserId())) {
            throw new BusinessException("无权确认此工单");
        }

        String oldStatus = ticket.getStatus();
        ticket.setStatus("COMPLETED");
        ticket.setCompleteTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);

        // 记录日志
        addTicketLog(ticketId, userId, null, "CONFIRM", oldStatus, "COMPLETED", "用户确认完成");

        // 更新Redis Set
        redisUtil.sRemove("ticket:status:RESOLVED", ticketId);
        redisUtil.sAdd("ticket:status:COMPLETED", ticketId);

        // 更新员工统计数据，自动更新状态
        if (ticket.getStaffId() != null) {
            Staff staff = staffMapper.selectById(ticket.getStaffId());
            if (staff != null) {
                staff.setCompletedCount(staff.getCompletedCount() + 1);
                staffMapper.updateById(staff);
            }
            updateStaffStatus(ticket.getStaffId());
        }

        // 通知维修员工单已被确认完成
        if (ticket.getStaffId() != null) {
            Staff staff = staffMapper.selectById(ticket.getStaffId());
            if (staff != null) {
                notificationService.createNotification(staff.getUserId(),
                        "工单已确认完成",
                        "工单「" + ticket.getTitle() + "」已被用户确认完成。",
                        "TICKET", ticketId);
            }
        }
    }

    /**
     * 处理建议工单（状态变为已处理）
     */
    @Transactional(rollbackFor = Exception.class)
    public void processSuggestion(Long ticketId, Long operatorId, String content) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if (!"SUGGESTION".equals(ticket.getCategory())) {
            throw new BusinessException("仅建议类工单支持此操作");
        }

        if (!"PENDING".equals(ticket.getStatus())) {
            throw new BusinessException("工单状态不允许此操作");
        }

        String oldStatus = ticket.getStatus();
        ticket.setStatus("PROCESSED");
        ticket.setResolveContent(content);
        ticket.setCompleteTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);

        // 记录日志
        addTicketLog(ticketId, operatorId, null, "PROCESS", oldStatus, "PROCESSED",
                StringUtils.isNotBlank(content) ? content : "处理建议");

        // 更新Redis Set
        redisUtil.sRemove("ticket:status:" + oldStatus, ticketId);
        redisUtil.sAdd("ticket:status:PROCESSED", ticketId);
        redisUtil.zRemove("ticket:priority:queue", ticketId);

        // 通知工单提交者
        notificationService.createNotification(ticket.getUserId(),
                "建议已处理",
                "您的建议「" + ticket.getTitle() + "」已被处理。",
                "TICKET", ticketId);
    }

    /**
     * 取消工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTicket(Long ticketId, Long userId, String remark) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if ("COMPLETED".equals(ticket.getStatus()) || "PROCESSED".equals(ticket.getStatus()) || "CANCELLED".equals(ticket.getStatus())) {
            throw new BusinessException("工单状态不允许取消");
        }

        String oldStatus = ticket.getStatus();
        ticket.setStatus("CANCELLED");
        ticketMapper.updateById(ticket);

        // 记录日志
        addTicketLog(ticketId, userId, null, "CANCEL", oldStatus, "CANCELLED",
                StringUtils.isNotBlank(remark) ? remark : "取消工单");

        // 更新Redis Set
        redisUtil.sRemove("ticket:status:" + oldStatus, ticketId);
        redisUtil.sAdd("ticket:status:CANCELLED", ticketId);

        // 从优先级队列移除
        redisUtil.zRemove("ticket:priority:queue", ticketId);

        // 若已分配维修员，通知维修员工单被取消
        if (ticket.getStaffId() != null) {
            Staff staff = staffMapper.selectById(ticket.getStaffId());
            if (staff != null) {
                notificationService.createNotification(staff.getUserId(),
                        "工单已取消",
                        "工单「" + ticket.getTitle() + "」已被取消。",
                        "TICKET", ticketId);
            }
        }
    }

    /**
     * 删除工单（逻辑删除）
     * 管理员可直接删除，学生只能在取消后删除自己的工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTicket(Long ticketId, Long userId, String role) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if ("STUDENT".equals(role)) {
            if (!userId.equals(ticket.getUserId())) {
                throw new BusinessException(403, "无权删除此工单");
            }
            if (!"CANCELLED".equals(ticket.getStatus())) {
                throw new BusinessException("请先取消工单后再删除");
            }
        } else if ("STAFF".equals(role)) {
            throw new BusinessException(403, "无权删除工单");
        }

        // 记录日志
        addTicketLog(ticketId, userId, null, "DELETE", ticket.getStatus(), "DELETED", "删除工单");

        // 清理Redis
        redisUtil.sRemove("ticket:status:" + ticket.getStatus(), ticketId);
        redisUtil.zRemove("ticket:priority:queue", ticketId);

        // 逻辑删除
        ticketMapper.deleteById(ticketId);

        // 若管理员删除学生工单，通知工单创建者
        if ("ADMIN".equals(role) && !userId.equals(ticket.getUserId())) {
            notificationService.createNotification(ticket.getUserId(),
                    "工单已被删除",
                    "您的工单「" + ticket.getTitle() + "」已被管理员删除。",
                    "TICKET", null);
        }
    }

    /**
     * 提供线索（失物招领）
     */
    @Transactional(rollbackFor = Exception.class)
    public void provideClue(Long ticketId, Long userId, String message) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }
        if (!"LOST_FOUND".equals(ticket.getCategory())) {
            throw new BusinessException("该工单不是失物招领类型");
        }
        if (userId.equals(ticket.getUserId())) {
            throw new BusinessException("不能给自己的工单提供线索");
        }

        // 查线索提供者姓名
        User provider = userMapper.selectById(userId);
        String providerName = provider != null ? provider.getRealName() : "匿名用户";

        notificationService.createNotification(ticket.getUserId(),
                "有人提供了线索",
                providerName + " 对你的「" + ticket.getTitle() + "」提供了线索：" + message,
                "TICKET", ticketId);
    }

    /**
     * 获取工单详情
     */
    public Ticket getTicketById(Long id) {
        return ticketMapper.selectById(id);
    }

    /**
     * 分页查询工单列表（支持关键字搜索）
     */
    public Page<Ticket> getTicketList(Integer pageNum, Integer pageSize, String status,
                                      String category, String keyword, Long userId, Long staffId) {
        Page<Ticket> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(Ticket::getStatus, status);
        }
        if (StringUtils.isNotBlank(category)) {
            wrapper.eq(Ticket::getCategory, category);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w
                .like(Ticket::getTicketNo, keyword)
                .or()
                .like(Ticket::getTitle, keyword)
                .or()
                .like(Ticket::getDescription, keyword)
            );
        }
        if (userId != null) {
            wrapper.eq(Ticket::getUserId, userId);
        }
        if (staffId != null) {
            wrapper.eq(Ticket::getStaffId, staffId);
        }

        wrapper.orderByDesc(Ticket::getCreateTime);

        return ticketMapper.selectPage(page, wrapper);
    }

    /**
     * 获取待处理工单列表（返回完整工单对象）
     */
    public List<Ticket> getPendingTickets() {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ticket::getStatus, "PENDING");
        wrapper.orderByDesc(Ticket::getCreateTime);
        return ticketMapper.selectList(wrapper);
    }

    /**
     * 获取高优先级工单（返回完整工单对象）
     */
    public List<Ticket> getHighPriorityTickets(int topN) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Ticket::getStatus, "PENDING", "ASSIGNED", "IN_PROGRESS");
        wrapper.orderByDesc(Ticket::getPriorityScore);
        Page<Ticket> page = new Page<>(1, topN);
        return ticketMapper.selectPage(page, wrapper).getRecords();
    }

    /**
     * 获取工单日志
     */
    public List<TicketLog> getTicketLogs(Long ticketId) {
        LambdaQueryWrapper<TicketLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketLog::getTicketId, ticketId);
        wrapper.orderByAsc(TicketLog::getCreateTime);
        return ticketLogMapper.selectList(wrapper);
    }

    /**
     * 根据未完成工单数自动更新员工状态
     * 0个未完成→空闲(1), 1个→忙碌(2), 2个及以上→繁忙(3)
     */
    private void updateStaffStatus(Long staffId) {
        Long incompleteCount = ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStaffId, staffId)
                        .in(Ticket::getStatus, "ASSIGNED", "IN_PROGRESS"));
        Staff staff = staffMapper.selectById(staffId);
        if (staff != null) {
            if (incompleteCount >= 2) {
                staff.setStatus(3); // 繁忙
            } else if (incompleteCount == 1) {
                staff.setStatus(2); // 忙碌
            } else {
                staff.setStatus(1); // 空闲
            }
            staffMapper.updateById(staff);
        }
    }

    /**
     * 添加工单日志
     */
    private void addTicketLog(Long ticketId, Long operatorId, String operatorName,
                              String action, String fromStatus, String toStatus, String content) {
        TicketLog log = new TicketLog();
        log.setTicketId(ticketId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setContent(content);
        ticketLogMapper.insert(log);
    }

    /**
     * 获取紧急程度分数
     */
    private double getUrgencyScore(String urgency) {
        switch (urgency) {
            case "LOW":
                return URGENCY_LOW;
            case "HIGH":
                return URGENCY_HIGH;
            case "URGENT":
                return URGENCY_URGENT;
            default:
                return URGENCY_NORMAL;
        }
    }

    /**
     * 通知所有管理员
     */
    private void notifyAdmins(String title, String content, Long relatedId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "ADMIN");
        List<User> admins = userMapper.selectList(wrapper);
        for (User admin : admins) {
            notificationService.createNotification(admin.getId(), title, content, "TICKET", relatedId);
        }
    }

    /**
     * 获取分类中文名
     */
    private String getCategoryName(String category) {
        switch (category) {
            case "REPAIR": return "报修";
            case "SUGGESTION": return "建议";
            case "LOST_FOUND": return "失物招领";
            default: return category;
        }
    }

    /**
     * 获取公开失物招领列表（所有用户可见）
     */
    public Page<Ticket> getLostFoundList(Integer pageNum, Integer pageSize, String type, String keyword) {
        Page<Ticket> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();

        // 只查询失物招领类别
        wrapper.eq(Ticket::getCategory, "LOST_FOUND");

        // 只查询未取消的工单
        wrapper.ne(Ticket::getStatus, "CANCELLED");

        if (StringUtils.isNotBlank(type)) {
            wrapper.eq(Ticket::getType, type);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w
                .like(Ticket::getTitle, keyword)
                .or()
                .like(Ticket::getDescription, keyword)
                .or()
                .like(Ticket::getLocation, keyword)
            );
        }

        wrapper.orderByDesc(Ticket::getCreateTime);

        return ticketMapper.selectPage(page, wrapper);
    }

    /**
     * 获取TicketMapper(用于Controller)
     */
    public TicketMapper getTicketMapper() {
        return ticketMapper;
    }
}
