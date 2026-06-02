# 快速启动指南 (5分钟上手)

## 前置条件检查清单

- [ ] JDK 8 已安装
- [ ] Maven 3.6+ 已安装
- [ ] MySQL 8.0+ 已安装并运行
- [ ] Redis 5.0+ 已安装并运行

## 步骤1: 初始化数据库 (2分钟)

```bash
# 在MySQL中执行
mysql -u root -p < src/main/resources/sql/init.sql
```

或者使用MySQL Workbench等工具打开并执行 `src/main/resources/sql/init.sql`

## 步骤2: 修改配置 (1分钟)

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: root          # 改成你的MySQL用户名
    password: your_password # 改成你的MySQL密码
  
  redis:
    password:              # 如果Redis有密码，填在这里
```

## 步骤3: 启动项目 (1分钟)

### 方式1: 使用IDE (推荐)
1. 用IntelliJ IDEA或Eclipse打开项目
2. 等待Maven依赖下载完成
3. 运行 `EdufixApplication.java`

### 方式2: 使用命令行
```bash
mvn spring-boot:run
```

看到以下输出表示启动成功:
```
========================================
校园社区综合服务与报修工单平台启动成功!
访问地址: http://localhost:8080
========================================
```

## 步骤4: 测试API (1分钟)

### 测试1: 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

预期响应:
```json
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

### 测试2: 获取公告列表

```bash
curl http://localhost:8080/api/notice/published
```

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | ADMIN | 管理员 |
| user1 | 123456 | USER | 普通用户 |
| staff1 | 123456 | STAFF | 维修员 |

## 完整业务流程测试

### 1. 普通用户创建工单

```bash
# 先登录获取user1的token
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"123456"}' | grep token | cut -d'"' -f4)

# 创建工单
curl -X POST http://localhost:8080/api/ticket \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "title": "教室空调故障",
    "description": "第三教学楼301教室空调无法制冷",
    "category": "REPAIR",
    "urgency": "HIGH",
    "location": "第三教学楼301"
  }'
```

### 2. 管理员派单

```bash
# 管理员登录
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | grep token | cut -d'"' -f4)

# 查看待处理工单
curl http://localhost:8080/api/admin/pending-tickets \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 派单 (假设工单ID为1, 员工ID为1)
curl -X POST http://localhost:8080/api/admin/assign \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"ticketId":1,"staffId":1}'
```

### 3. 维修员接单并完成

```bash
# 维修员登录
STAFF_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"staff1","password":"123456"}' | grep token | cut -d'"' -f4)

# 接单
curl -X POST http://localhost:8080/api/staff/1/accept \
  -H "Authorization: Bearer $STAFF_TOKEN"

# 完成工单
curl -X POST http://localhost:8080/api/staff/1/complete \
  -H "Authorization: Bearer $STAFF_TOKEN"
```

### 4. 用户评价

```bash
# 评价工单
curl -X POST http://localhost:8080/api/evaluation \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "ticketId": 1,
    "rating": 5,
    "content": "服务很好"
  }'
```

## 使用Postman测试 (推荐)

1. 打开Postman
2. 导入 `postman-collection.json`
3. 设置环境变量:
   - base_url: http://localhost:8080
4. 按顺序执行请求

## 验证Redis数据

```bash
redis-cli

# 查看待处理工单
SMEMBERS ticket:status:PENDING

# 查看优先级队列
ZREVRANGE ticket:priority:queue 0 -1 WITHSCORES

# 查看公告缓存
GET notice:published:list
```

## 常见问题速查

### Q1: 端口8080被占用
**解决**: 修改 `application.yml` 中的 `server.port`

### Q2: 连接MySQL失败
**解决**: 
- 检查MySQL是否启动
- 检查用户名密码是否正确
- 确认数据库edufix已创建

### Q3: 连接Redis失败
**解决**:
- Windows: 运行 `redis-server.exe`
- Linux: `sudo systemctl start redis`

### Q4: Maven依赖下载慢
**解决**: 配置阿里云镜像

## 下一步

- 阅读 [README.md](README.md) 了解完整功能
- 查看 [ARCHITECTURE.md](ARCHITECTURE.md) 了解架构设计
- 参考 [DEPLOYMENT.md](DEPLOYMENT.md) 进行生产部署

## 需要帮助？

检查以下文件:
- 启动日志中的错误信息
- `application.yml` 配置是否正确
- 数据库和Redis是否正常连接

祝使用愉快！🚀
