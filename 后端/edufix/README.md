# 校园/社区综合服务与报修工单平台

## 项目简介

这是一个基于Spring Boot + Redis的校园/社区综合服务与报修工单管理平台，支持多角色权限管理、工单生命周期流转、实时进度追踪与服务评价等功能。

## 技术栈

- **后端框架**: Spring Boot 2.7.14
- **持久层**: MyBatis-Plus 3.5.3.1
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: JWT (JSON Web Token)
- **其他**: Lombok, Validation

## 核心功能

### 1. 多角色权限系统
- **普通用户(USER)**: 提交工单、查看进度、评价服务
- **维修员(STAFF)**: 接单、处理工单、完成工单
- **管理员(ADMIN)**: 派单、发布公告、查看所有工单

### 2. 工单管理
- 工单分类: 报修(REPAIR)、建议(SUGGESTION)、失物招领(LOST_FOUND)
- 紧急程度: 低(LOW)、普通(NORMAL)、高(HIGH)、紧急(URGENT)
- 状态流转: 待处理 → 已派单 → 处理中 → 已完成 / 已取消

### 3. Redis应用场景
- **String**: JWT Token黑名单、公告列表缓存
- **Hash**: 公告详情缓存、评价标签统计
- **Set**: 按状态维护工单ID集合(待处理/处理中/已完成)
- **ZSet**: 按紧急程度+时间生成派单优先级队列

### 4. 其他功能
- 工单流转日志记录
- 服务评价与评分系统
- 公告发布与管理
- 员工绩效统计

## 项目结构

```
edufix/
├── src/main/java/org/example/edufix/
│   ├── common/              # 通用类
│   │   └── Result.java      # 统一响应结果
│   ├── config/              # 配置类
│   │   ├── RedisConfig.java
│   │   └── WebConfig.java
│   ├── controller/          # 控制器
│   │   ├── AuthController.java
│   │   ├── TicketController.java
│   │   ├── AdminController.java
│   │   ├── StaffController.java
│   │   ├── EvaluationController.java
│   │   └── NoticeController.java
│   ├── dto/                 # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── CreateTicketRequest.java
│   │   ├── AssignTicketRequest.java
│   │   └── EvaluationRequest.java
│   ├── entity/              # 实体类
│   │   ├── User.java
│   │   ├── Staff.java
│   │   ├── Ticket.java
│   │   ├── TicketLog.java
│   │   ├── Evaluation.java
│   │   └── Notice.java
│   ├── interceptor/         # 拦截器
│   │   └── JwtInterceptor.java
│   ├── mapper/              # Mapper接口
│   │   ├── UserMapper.java
│   │   ├── StaffMapper.java
│   │   ├── TicketMapper.java
│   │   ├── TicketLogMapper.java
│   │   ├── EvaluationMapper.java
│   │   └── NoticeMapper.java
│   ├── service/             # 服务层
│   │   ├── UserService.java
│   │   ├── TicketService.java
│   │   ├── StaffService.java
│   │   ├── EvaluationService.java
│   │   └── NoticeService.java
│   ├── util/                # 工具类
│   │   ├── JwtUtil.java
│   │   └── RedisUtil.java
│   └── EdufixApplication.java  # 启动类
└── src/main/resources/
    ├── application.yml      # 配置文件
    └── sql/
        └── init.sql         # 数据库初始化脚本
```

## 快速开始

### 1. 环境要求
- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 2. 数据库配置

执行SQL初始化脚本:
```bash
mysql -u root -p < src/main/resources/sql/init.sql
```

修改 `application.yml` 中的数据库和Redis配置:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/edufix?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

### 3. 编译运行

```bash
mvn clean package
java -jar target/edufix-1.0-SNAPSHOT.jar
```

或使用IDE直接运行 `EdufixApplication.java`

### 4. 测试账号

系统已预置以下测试账号(密码均为: 123456):
- 管理员: admin
- 普通用户: user1
- 维修员: staff1

## API接口文档

### 认证接口

#### 1. 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

响应:
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "role": "ADMIN",
    "realName": "系统管理员"
  }
}
```

#### 2. 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456",
  "realName": "新用户",
  "phone": "13800138000",
  "email": "user@example.com"
}
```

#### 3. 用户登出
```
POST /api/auth/logout
Authorization: Bearer {token}
```

### 工单接口

#### 1. 创建工单
```
POST /api/ticket
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "教室空调故障",
  "description": "第三教学楼301教室空调无法制冷",
  "category": "REPAIR",
  "type": "空调维修",
  "location": "第三教学楼301",
  "urgency": "HIGH",
  "contactInfo": "13800138001",
  "remark": "请尽快处理"
}
```

#### 2. 查询工单列表
```
GET /api/ticket/list?pageNum=1&pageSize=10&status=PENDING&category=REPAIR
Authorization: Bearer {token}
```

#### 3. 获取工单详情
```
GET /api/ticket/{id}
Authorization: Bearer {token}
```

#### 4. 获取工单日志
```
GET /api/ticket/{id}/logs
Authorization: Bearer {token}
```

#### 5. 取消工单
```
PUT /api/ticket/{id}/cancel?remark=不需要维修了
Authorization: Bearer {token}
```

### 管理员接口

#### 1. 派单
```
POST /api/admin/assign
Authorization: Bearer {token}
Content-Type: application/json

{
  "ticketId": 1,
  "staffId": 1,
  "remark": "请优先处理"
}
```

#### 2. 获取待处理工单(Redis Set)
```
GET /api/admin/pending-tickets
Authorization: Bearer {token}
```

#### 3. 获取高优先级工单(Redis ZSet)
```
GET /api/admin/high-priority-tickets?topN=10
Authorization: Bearer {token}
```

#### 4. 获取员工列表
```
GET /api/admin/staff/list
Authorization: Bearer {token}
```

### 维修员接口

#### 1. 接单
```
POST /api/staff/{ticketId}/accept
Authorization: Bearer {token}
```

#### 2. 完成工单
```
POST /api/staff/{ticketId}/complete?remark=已修复
Authorization: Bearer {token}
```

#### 3. 获取我的工单
```
GET /api/staff/my-tickets?pageNum=1&pageSize=10&status=IN_PROGRESS
Authorization: Bearer {token}
```

### 评价接口

#### 1. 创建评价
```
POST /api/evaluation
Authorization: Bearer {token}
Content-Type: application/json

{
  "ticketId": 1,
  "rating": 5,
  "tags": "快速响应,专业认真",
  "content": "维修师傅很专业，很快就解决了问题"
}
```

#### 2. 获取工单评价
```
GET /api/evaluation/ticket/{ticketId}
Authorization: Bearer {token}
```

#### 3. 获取员工评价列表
```
GET /api/evaluation/staff/{staffId}
Authorization: Bearer {token}
```

### 公告接口

#### 1. 获取已发布公告
```
GET /api/notice/published
```

#### 2. 获取公告详情
```
GET /api/notice/{id}
```

#### 3. 创建公告(管理员)
```
POST /api/notice
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "系统维护通知",
  "content": "系统将于今晚进行维护",
  "type": "NOTICE",
  "priority": 1
}
```

#### 4. 发布公告(管理员)
```
PUT /api/notice/{id}/publish
Authorization: Bearer {token}
```

## Redis数据结构说明

### 1. String类型
```
token:blacklist:{token} = "1"  # JWT Token黑名单，防止重复登录
notice:published:list = [公告列表JSON]  # 公告列表缓存
```

### 2. Hash类型
```
notice:detail:{id} = {公告详情JSON}  # 公告详情缓存
evaluation:tags -> {tag: count, ...}  # 评价标签统计
```

### 3. Set类型
```
ticket:status:PENDING = {ticketId1, ticketId2, ...}  # 待处理工单ID集合
ticket:status:ASSIGNED = {...}  # 已派单工单ID集合
ticket:status:IN_PROGRESS = {...}  # 处理中工单ID集合
ticket:status:COMPLETED = {...}  # 已完成工单ID集合
ticket:status:CANCELLED = {...}  # 已取消工单ID集合
```

### 4. ZSet类型
```
ticket:priority:queue -> {ticketId: priorityScore, ...}  # 工单优先级队列
# priorityScore = 紧急程度权重 * 1000000000 + (Long.MAX_VALUE - 时间戳)
# 分数越高，优先级越高
```

## 工单状态机

```
[PENDING] --派单--> [ASSIGNED] --接单--> [IN_PROGRESS] --完成--> [COMPLETED]
   |                    |                      |
   |--取消------------->|                      |
   |                                           |
   |------------------------------------------>|
                     [CANCELLED]
```

## 毕设亮点

1. **完整的业务流程**: 从工单提交、派单、接单、处理到评价，流程完整清晰
2. **状态机设计**: 工单状态流转逻辑清晰，符合实际业务场景
3. **Redis多场景应用**: 
   - 使用Set实现工单状态的快速筛选
   - 使用ZSet实现基于优先级的智能派单
   - 使用Hash缓存高频访问数据
   - 使用String实现Token黑名单
4. **多角色权限控制**: 基于JWT的多角色权限管理系统
5. **实时性**: 工单进度实时可查，支持快速响应

## 注意事项

1. 首次运行前请先执行SQL初始化脚本
2. 确保MySQL和Redis服务已启动
3. 默认密码为MD5加密的"123456"
4. 生产环境请使用BCrypt等更安全的加密方式

## 扩展建议

1. 添加WebSocket实现工单状态实时推送
2. 集成短信服务发送验证码和通知
3. 添加文件上传功能支持图片附件
4. 实现数据统计看板(工单量、完成率、满意度等)
5. 添加定时任务清理过期数据

## 许可证

本项目仅供学习和毕业设计使用。
