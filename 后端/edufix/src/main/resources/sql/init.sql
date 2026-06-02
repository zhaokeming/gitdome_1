-- 创建数据库
CREATE DATABASE IF NOT EXISTS edufix DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE edufix;

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER-普通用户, STAFF-维修员, ADMIN-管理员',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 员工信息表(维修员详细信息)
CREATE TABLE `staff` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '员工ID',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `employee_no` VARCHAR(50) NOT NULL COMMENT '工号',
    `department` VARCHAR(100) COMMENT '所属部门',
    `specialty` VARCHAR(200) COMMENT '专业领域',
    `rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '评分',
    `completed_count` INT DEFAULT 0 COMMENT '完成工单数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-离线, 1-在线, 2-忙碌',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工信息表';

-- 工单表
CREATE TABLE `ticket` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `ticket_no` VARCHAR(50) NOT NULL COMMENT '工单编号',
    `title` VARCHAR(200) NOT NULL COMMENT '工单标题',
    `description` TEXT COMMENT '问题描述',
    `category` VARCHAR(50) NOT NULL COMMENT '分类: REPAIR-报修, SUGGESTION-建议, LOST_FOUND-失物招领',
    `type` VARCHAR(50) COMMENT '具体类型',
    `location` VARCHAR(200) COMMENT '地点',
    `urgency` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '紧急程度: LOW-低, NORMAL-普通, HIGH-高, URGENT-紧急',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理, ASSIGNED-已派单, IN_PROGRESS-处理中, COMPLETED-已完成, CANCELLED-已取消',
    `user_id` BIGINT NOT NULL COMMENT '提交用户ID',
    `staff_id` BIGINT COMMENT '指派员工ID',
    `images` TEXT COMMENT '图片URL列表(逗号分隔)',
    `contact_info` VARCHAR(100) COMMENT '联系方式',
    `remark` TEXT COMMENT '备注',
    `priority_score` DOUBLE DEFAULT 0 COMMENT '优先级分数(用于ZSet排序)',
    `resolve_content` TEXT COMMENT '维修处理结果描述',
    `resolve_images` TEXT COMMENT '维修后图片URL列表(逗号分隔)',
    `complete_time` DATETIME COMMENT '完成时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_no` (`ticket_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`),
    KEY `idx_urgency` (`urgency`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 工单流转日志表
CREATE TABLE `ticket_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(50) COMMENT '操作人姓名',
    `action` VARCHAR(50) NOT NULL COMMENT '操作: CREATE-创建, ASSIGN-派单, ACCEPT-接单, PROCESS-处理, COMPLETE-完成, CANCEL-取消, EVALUATE-评价',
    `from_status` VARCHAR(20) COMMENT '原状态',
    `to_status` VARCHAR(20) COMMENT '新状态',
    `content` TEXT COMMENT '操作内容/备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转日志表';

-- 评价表
CREATE TABLE `evaluation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
    `user_id` BIGINT NOT NULL COMMENT '评价用户ID',
    `staff_id` BIGINT COMMENT '被评价员工ID',
    `rating` TINYINT NOT NULL COMMENT '评分: 1-5星',
    `tags` VARCHAR(200) COMMENT '评价标签(逗号分隔)',
    `content` TEXT COMMENT '评价内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_id` (`ticket_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_staff_id` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 公告表
CREATE TABLE `notice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `type` VARCHAR(20) NOT NULL DEFAULT 'NOTICE' COMMENT '类型: NOTICE-公告, NEWS-新闻',
    `priority` TINYINT DEFAULT 0 COMMENT '优先级: 0-普通, 1-重要, 2-紧急',
    `publish_time` DATETIME COMMENT '发布时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-已下架',
    `publisher_id` BIGINT COMMENT '发布人ID',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_publish_time` (`publish_time`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 消息通知表


-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 'admin@edufix.com', 'ADMIN', 1),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '13800138001', 'zhangsan@example.com', 'USER', 1),
('staff1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李师傅', '13800138002', 'lisi@example.com', 'STAFF', 1);

INSERT INTO `staff` (`user_id`, `employee_no`, `department`, `specialty`, `rating`, `completed_count`, `status`) VALUES
(3, 'EMP001', '维修部', '水电维修,家具维修', 4.50, 100, 1);

INSERT INTO `notice` (`title`, `content`, `type`, `priority`, `publish_time`, `status`, `publisher_id`) VALUES
('系统维护通知', '系统将于今晚22:00-24:00进行维护，期间可能无法访问。', 'NOTICE', 1, NOW(), 1, 1),
('新功能上线', '新增了失物招领功能，欢迎大家使用！', 'NEWS', 0, NOW(), 1, 1);

