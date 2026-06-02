-- ============================================
-- EduFix 校园社区综合服务与报修工单平台
-- 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS edufix
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE edufix;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name`      VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `identifier_no`  VARCHAR(50)  DEFAULT NULL COMMENT '学号/工号',
    `phone`          VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`          VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role`           VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT-学生, ADMIN-管理员, STAFF-维修员',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 员工信息表
-- ============================================
CREATE TABLE IF NOT EXISTS `staff` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '员工ID',
    `user_id`         BIGINT         NOT NULL COMMENT '关联用户ID',
    `employee_no`     VARCHAR(50)    NOT NULL COMMENT '员工编号',
    `department`      VARCHAR(100)   DEFAULT NULL COMMENT '所属部门',
    `specialty`       VARCHAR(100)   DEFAULT NULL COMMENT '专长/工种',
    `rating`          DECIMAL(3,2)   DEFAULT 5.00 COMMENT '评分(1-5)',
    `completed_count` INT            DEFAULT 0 COMMENT '完成工单数',
    `status`          TINYINT        DEFAULT 1 COMMENT '状态: 0-离线, 1-在线, 2-忙碌',
    `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_staff_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工信息表';

-- ============================================
-- 工单表
-- ============================================
CREATE TABLE IF NOT EXISTS `ticket` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `ticket_no`       VARCHAR(50)    NOT NULL COMMENT '工单编号',
    `title`           VARCHAR(200)   NOT NULL COMMENT '标题',
    `description`     TEXT           NOT NULL COMMENT '问题描述',
    `category`        VARCHAR(30)    NOT NULL COMMENT '分类: REPAIR-报修, SUGGESTION-建议, LOST_FOUND-失物招领',
    `type`            VARCHAR(50)    DEFAULT NULL COMMENT '具体类型',
    `location`        VARCHAR(200)   DEFAULT NULL COMMENT '位置',
    `urgency`         VARCHAR(20)    DEFAULT 'NORMAL' COMMENT '紧急程度: LOW-低, NORMAL-普通, HIGH-高, URGENT-紧急',
    `status`          VARCHAR(30)    DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理, ASSIGNED-已派单, IN_PROGRESS-处理中, RESOLVED-已处理待确认, COMPLETED-已完成, PROCESSED-已处理, CANCELLED-已取消',
    `user_id`         BIGINT         NOT NULL COMMENT '提交用户ID',
    `staff_id`        BIGINT         DEFAULT NULL COMMENT '负责维修员ID',
    `images`          TEXT           DEFAULT NULL COMMENT '图片URL列表(逗号分隔)',
    `contact_info`    VARCHAR(100)   DEFAULT NULL COMMENT '联系方式',
    `remark`          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    `priority_score`  DOUBLE         DEFAULT 0 COMMENT '优先级分数(用于Redis ZSet排序)',
    `complete_time`   DATETIME       DEFAULT NULL COMMENT '完成时间',
    `resolve_content` TEXT           DEFAULT NULL COMMENT '维修员处理结果描述',
    `resolve_images`  TEXT           DEFAULT NULL COMMENT '维修后图片URL列表(逗号分隔)',
    `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_no` (`ticket_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`),
    KEY `idx_urgency` (`urgency`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_ticket_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_ticket_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单表';

-- ============================================
-- 工单流转日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `ticket_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `ticket_id`     BIGINT       NOT NULL COMMENT '工单ID',
    `operator_id`   BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50)  DEFAULT NULL COMMENT '操作人姓名',
    `action`        VARCHAR(30)  NOT NULL COMMENT '操作: CREATE-创建, ASSIGN-派单, ACCEPT-接单, PROCESS-处理, RESOLVE-提交结果, COMPLETE-完成, CONFIRM-确认, CANCEL-取消, EVALUATE-评价',
    `from_status`   VARCHAR(30)  DEFAULT NULL COMMENT '操作前状态',
    `to_status`     VARCHAR(30)  DEFAULT NULL COMMENT '操作后状态',
    `content`       VARCHAR(500) DEFAULT NULL COMMENT '操作备注',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`),
    CONSTRAINT `fk_log_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单流转日志表';

-- ============================================
-- 评价表
-- ============================================
CREATE TABLE IF NOT EXISTS `evaluation` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `ticket_id`   BIGINT         NOT NULL COMMENT '工单ID',
    `user_id`     BIGINT         NOT NULL COMMENT '评价用户ID',
    `staff_id`    BIGINT         DEFAULT NULL COMMENT '被评价员工ID',
    `rating`      TINYINT        NOT NULL COMMENT '评分: 1-5',
    `tags`        VARCHAR(500)   DEFAULT NULL COMMENT '评价标签(逗号分隔)',
    `content`     VARCHAR(1000)  DEFAULT NULL COMMENT '评价内容',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_id` (`ticket_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_eval_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`),
    CONSTRAINT `fk_eval_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_eval_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ============================================
-- 公告表
-- ============================================
CREATE TABLE IF NOT EXISTS `notice` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`        VARCHAR(200)  NOT NULL COMMENT '标题',
    `content`      TEXT          NOT NULL COMMENT '内容',
    `type`         VARCHAR(20)   DEFAULT 'NOTICE' COMMENT '类型: NOTICE-公告, NEWS-新闻',
    `priority`     TINYINT       DEFAULT 0 COMMENT '优先级: 0-普通, 1-重要, 2-紧急',
    `publish_time` DATETIME      DEFAULT NULL COMMENT '发布时间',
    `status`       TINYINT       DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-已下架',
    `publisher_id` BIGINT        DEFAULT NULL COMMENT '发布人ID',
    `view_count`   INT           DEFAULT 0 COMMENT '浏览次数',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_publish_time` (`publish_time`),
    CONSTRAINT `fk_notice_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ============================================
-- 消息通知表
-- ============================================
CREATE TABLE IF NOT EXISTS `notification` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`     BIGINT        NOT NULL COMMENT '接收用户ID',
    `title`       VARCHAR(200)  NOT NULL COMMENT '标题',
    `content`     VARCHAR(1000) DEFAULT NULL COMMENT '内容',
    `type`        VARCHAR(30)   NOT NULL DEFAULT 'SYSTEM' COMMENT '类型: TICKET-工单通知, SYSTEM-系统通知',
    `related_id`  BIGINT        DEFAULT NULL COMMENT '关联ID(工单ID等)',
    `is_read`     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_read` (`user_id`, `is_read`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_notify_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- ============================================
-- 初始化种子数据
-- 密码哈希由 BCryptPasswordEncoder 生成，无法手动构造
-- 操作步骤：
--   1. 运行 PasswordGenerator.main() 生成哈希值
--      (位于 src/main/java/org/example/edufix/util/PasswordGenerator.java)
--   2. 将输出的哈希值替换下方 <PLACEHOLDER>
--   3. 取消注释 INSERT 语句并执行
-- 或直接启动应用后调用: POST /api/auth/register
-- ============================================
-- INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`)
-- VALUES ('admin', '<BCRYPT_HASH>', '系统管理员', 'ADMIN', 1);
--
-- INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`)
-- VALUES ('staff1', '<BCRYPT_HASH>', '维修员张三', 'STAFF', 1);
--
-- INSERT INTO `staff` (`user_id`, `employee_no`, `department`, `specialty`, `status`)
-- VALUES (2, 'EMP001', '后勤维修部', '水电维修', 1);
