-- ============================================================================
-- EduFix 校园社区综合服务与报修工单平台
-- 数据库全面初始化脚本（可直接在 edufix 库下新建查询运行）
-- ============================================================================
-- 使用说明：
--   1. 连接 MySQL，USE edufix
--   2. 复制本脚本全部内容，粘贴到查询窗口执行即可
--   3. 脚本会先删表再重建，确保每次运行结果一致
--
--   测试账号（密码均为对应的英文+123）：
--   ┌────────────┬───────────┬────────┬──────────────┐
--   │ 用户名     │ 密码      │ 角色   │ 姓名         │
--   ├────────────┼───────────┼────────┼──────────────┤
--   │ admin      │ admin123  │ ADMIN  │ 系统管理员   │
--   │ staff1     │ staff123  │ STAFF  │ 维修员张三   │
--   │ staff2     │ staff123  │ STAFF  │ 维修员李四   │
--   │ staff3     │ staff123  │ STAFF  │ 维修员王五   │
--   │ student1   │ student123│ STUDENT│ 测试学生     │
--   └────────────┴───────────┴────────┴──────────────┘
-- ============================================================================

-- ============================================================================
-- 第一部分：删除所有表（先删子表再删父表）
-- ============================================================================
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `evaluation`;
DROP TABLE IF EXISTS `ticket_log`;
DROP TABLE IF EXISTS `ticket`;
DROP TABLE IF EXISTS `notice`;
DROP TABLE IF EXISTS `staff`;
DROP TABLE IF EXISTS `user`;

-- ============================================================================
-- 第二部分：创建表结构
-- ============================================================================

-- -------------------------------------------
-- 用户表
-- -------------------------------------------
CREATE TABLE `user` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`       VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`       VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name`      VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `identifier_no`  VARCHAR(50)  DEFAULT NULL COMMENT '学号/工号',
    `phone`          VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`          VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role`           VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT-学生, ADMIN-管理员, STAFF-维修员',
    `avatar`         VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------
-- 员工信息表
-- -------------------------------------------
CREATE TABLE `staff` (
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

-- -------------------------------------------
-- 工单表
-- -------------------------------------------
CREATE TABLE `ticket` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `ticket_no`       VARCHAR(50)  NOT NULL COMMENT '工单编号',
    `title`           VARCHAR(200) NOT NULL COMMENT '标题',
    `description`     TEXT         NOT NULL COMMENT '问题描述',
    `category`        VARCHAR(30)  NOT NULL COMMENT '分类: REPAIR-报修, SUGGESTION-建议, LOST_FOUND-失物招领',
    `type`            VARCHAR(50)  DEFAULT NULL COMMENT '具体类型',
    `location`        VARCHAR(200) DEFAULT NULL COMMENT '位置',
    `urgency`         VARCHAR(20)  DEFAULT 'NORMAL' COMMENT '紧急程度: LOW-低, NORMAL-普通, HIGH-高, URGENT-紧急',
    `status`          VARCHAR(30)  DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理, ASSIGNED-已派单, IN_PROGRESS-处理中, RESOLVED-已处理待确认, COMPLETED-已完成, PROCESSED-已处理, CANCELLED-已取消',
    `user_id`         BIGINT       NOT NULL COMMENT '提交用户ID',
    `staff_id`        BIGINT       DEFAULT NULL COMMENT '负责维修员ID',
    `images`          TEXT         DEFAULT NULL COMMENT '图片URL列表(逗号分隔)',
    `contact_info`    VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    `remark`          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `priority_score`  DOUBLE       DEFAULT 0 COMMENT '优先级分数(用于Redis ZSet排序)',
    `complete_time`   DATETIME     DEFAULT NULL COMMENT '完成时间',
    `resolve_content` TEXT         DEFAULT NULL COMMENT '维修员处理结果描述',
    `resolve_images`  TEXT         DEFAULT NULL COMMENT '维修后图片URL列表(逗号分隔)',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
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

-- -------------------------------------------
-- 工单流转日志表
-- -------------------------------------------
CREATE TABLE `ticket_log` (
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

-- -------------------------------------------
-- 评价表
-- -------------------------------------------
CREATE TABLE `evaluation` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `ticket_id`   BIGINT       NOT NULL COMMENT '工单ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评价用户ID',
    `staff_id`    BIGINT       DEFAULT NULL COMMENT '被评价员工ID',
    `rating`      TINYINT      NOT NULL COMMENT '评分: 1-5',
    `tags`        VARCHAR(500) DEFAULT NULL COMMENT '评价标签(逗号分隔)',
    `content`     VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_id` (`ticket_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_eval_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`),
    CONSTRAINT `fk_eval_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_eval_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- -------------------------------------------
-- 公告表
-- -------------------------------------------
CREATE TABLE `notice` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`        VARCHAR(200) NOT NULL COMMENT '标题',
    `content`      TEXT         NOT NULL COMMENT '内容',
    `type`         VARCHAR(20)  DEFAULT 'NOTICE' COMMENT '类型: NOTICE-公告, NEWS-新闻',
    `priority`     TINYINT      DEFAULT 0 COMMENT '优先级: 0-普通, 1-重要, 2-紧急',
    `publish_time` DATETIME     DEFAULT NULL COMMENT '发布时间',
    `status`       TINYINT      DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-已下架',
    `publisher_id` BIGINT       DEFAULT NULL COMMENT '发布人ID',
    `view_count`   INT          DEFAULT 0 COMMENT '浏览次数',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_publish_time` (`publish_time`),
    CONSTRAINT `fk_notice_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- -------------------------------------------
-- 消息通知表
-- -------------------------------------------
CREATE TABLE `notification` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`     BIGINT       NOT NULL COMMENT '接收用户ID',
    `title`       VARCHAR(200) NOT NULL COMMENT '标题',
    `content`     VARCHAR(1000) DEFAULT NULL COMMENT '内容',
    `type`        VARCHAR(30)  NOT NULL DEFAULT 'SYSTEM' COMMENT '类型: TICKET-工单通知, SYSTEM-系统通知',
    `related_id`  BIGINT       DEFAULT NULL COMMENT '关联ID(工单ID等)',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_read` (`user_id`, `is_read`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_notify_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- ============================================================================
-- 第三部分：插入种子数据
-- ============================================================================

-- -------------------------------------------
-- 用户种子数据（密码已由 BCryptPasswordEncoder 生成）
-- -------------------------------------------
INSERT INTO `user` (`username`, `password`, `real_name`, `identifier_no`, `role`, `status`) VALUES
('admin',    '$2a$10$HmC.ZITjNNYanbZQuG8zhOtcCIwDEXvPuikP6MMbz6P2TcP5sJuEC', '系统管理员', 'A001', 'ADMIN',  1),
('staff1',   '$2a$10$KUcCaQgUim53G79R4vHP1etNwuN4MP3e3vm2A0pNgdCkQftXVrnqK', '维修员张三', 'S001', 'STAFF',  1),
('staff2',   '$2a$10$KUcCaQgUim53G79R4vHP1etNwuN4MP3e3vm2A0pNgdCkQftXVrnqK', '维修员李四', 'S002', 'STAFF',  1),
('staff3',   '$2a$10$KUcCaQgUim53G79R4vHP1etNwuN4MP3e3vm2A0pNgdCkQftXVrnqK', '维修员王五', 'S003', 'STAFF',  1),
('student1', '$2a$10$PvjikIA7rlXIa.thPGT8wOwyJR.DyGb48H8Y093M/KBF0PuQGFdz2', '测试学生',   '2024001', 'STUDENT', 1);

-- -------------------------------------------
-- 员工详情种子数据
-- -------------------------------------------
INSERT INTO `staff` (`user_id`, `employee_no`, `department`, `specialty`, `status`) VALUES
(2, 'S001', '后勤维修部', '水电维修', 1),
(3, 'S002', '后勤维修部', '公物维修', 1),
(4, 'S003', '后勤维修部', '网络维修', 1);

-- -------------------------------------------
-- 公告种子数据
-- -------------------------------------------
INSERT INTO `notice` (`title`, `content`, `type`, `priority`, `publish_time`, `status`, `publisher_id`, `view_count`) VALUES
('系统上线公告', '欢迎使用 EduFix 校园社区综合服务与报修工单平台！本平台支持在线报修、建议反馈、失物招领等功能，如有问题请联系管理员。', 'NOTICE', 1, NOW(), 1, 1, 0),
('报修功能使用指南', '提交报修工单时，请尽量详细描述问题，并上传清晰的现场照片，以便维修人员快速定位和解决问题。', 'NOTICE', 0, NOW(), 1, 1, 0);

-- -------------------------------------------
-- 示例工单数据
-- -------------------------------------------
INSERT INTO `ticket` (`ticket_no`, `title`, `description`, `category`, `type`, `location`, `urgency`, `status`, `user_id`, `staff_id`, `contact_info`) VALUES
('TK20260501001', '教室灯管损坏', '博学楼 301 教室第二排上方灯管不亮，影响学生晚自习，请尽快维修。', 'REPAIR', '照明维修', '博学楼301教室', 'NORMAL', 'PENDING',    3, NULL, '13800000001'),
('TK20260501002', '宿舍水龙头漏水', '明德公寓 5 号楼 302 室卫生间水龙头持续滴水，请安排维修。', 'REPAIR', '水暖维修', '明德公寓5#302', 'HIGH', 'ASSIGNED', 3, 1, '13800000001'),
('TK20260501003', '食堂卫生建议', '建议在食堂入口处增设洗手池和免洗消毒液，提升就餐卫生条件。', 'SUGGESTION', '设施建议', '第一食堂', 'LOW', 'PENDING', 3, NULL, '13800000001');

-- -------------------------------------------
-- 示例工单流转日志
-- -------------------------------------------
INSERT INTO `ticket_log` (`ticket_id`, `operator_id`, `operator_name`, `action`, `from_status`, `to_status`, `content`) VALUES
(1, 3, '测试学生', 'CREATE', NULL, 'PENDING', '提交报修工单'),
(2, 3, '测试学生', 'CREATE', NULL, 'PENDING', '提交报修工单'),
(2, 1, '系统管理员', 'ASSIGN', 'PENDING', 'ASSIGNED', '指派给维修员张三'),
(3, 3, '测试学生', 'CREATE', NULL, 'PENDING', '提交建议');

-- -------------------------------------------
-- 示例消息通知
-- -------------------------------------------
INSERT INTO `notification` (`user_id`, `title`, `content`, `type`, `related_id`, `is_read`) VALUES
(3, '工单提交成功', '您的报修工单 TK20260501001（教室灯管损坏）已提交，请耐心等待处理。', 'TICKET', 1, 0),
(3, '工单已派单', '您的报修工单 TK20260501002（宿舍水龙头漏水）已指派给维修员张三。', 'TICKET', 2, 0),
(2, '新工单待处理', '您有一个新的报修工单 TK20260501002（宿舍水龙头漏水）待处理，请及时查看。', 'TICKET', 2, 0);

-- ============================================================================
-- 脚本执行完毕
-- ============================================================================
-- 验证数据：
--   SELECT COUNT(*) AS user_count FROM user;
--   SELECT COUNT(*) AS staff_count FROM staff;
--   SELECT COUNT(*) AS ticket_count FROM ticket;
--   SELECT COUNT(*) AS notice_count FROM notice;
--   SELECT COUNT(*) AS notification_count FROM notification;
