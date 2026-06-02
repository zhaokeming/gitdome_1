# API接口测试指南

## 测试环境
- 后端地址: http://localhost:8081
- 前端地址: http://localhost:5173 (或其他Vite默认端口)
- API基础路径: /api

## 快速测试步骤

### 1. 测试后端是否正常运行

在浏览器或Postman中访问:
```
GET http://localhost:8081/api/notice/published
```

**预期结果**: 返回JSON格式的公告列表，code为200

---

### 2. 测试登录接口

使用curl或Postman发送POST请求:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**预期结果**: 
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

**保存token用于后续测试**

---

### 3. 测试需要认证的接口

使用上一步获取的token:

```bash
curl -X GET http://localhost:8081/api/ticket/list?pageNum=1&pageSize=10 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**预期结果**: 返回工单列表，包含records和total字段

---

### 4. 前端调试步骤

#### 4.1 打开浏览器开发者工具

1. 按 F12 打开开发者工具
2. 切换到 **Network** 标签
3. 刷新页面或执行操作

#### 4.2 检查请求

找到失败的请求（通常是红色的），检查：

**Request Headers**:
- 是否有 `Authorization: Bearer xxx` 头？
- Content-Type 是否正确？

**Response**:
- 状态码是多少？（200, 401, 403, 500?）
- 返回的内容是什么？

**Console**:
- 是否有JavaScript错误？
- 是否有CORS错误？

#### 4.3 常见错误及解决方案

**错误1: CORS Error**
```
Access to XMLHttpRequest at 'http://localhost:8081/api/...' 
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**解决**: 
- 检查后端 WebConfig.java 的CORS配置
- 确保后端服务正在运行
- 重启后端服务

**错误2: 401 Unauthorized**
```
{ code: 401, message: "未授权，请登录" }
```

**解决**:
- 检查localStorage中是否有token: `console.log(localStorage.getItem('token'))`
- Token可能已过期，重新登录
- 检查Token格式是否正确

**错误3: Network Error**
```
Error: Network Error
```

**解决**:
- 检查后端服务是否启动
- 检查前端配置的baseURL是否正确（应该是 http://localhost:8081/api）
- 检查防火墙设置

**错误4: 500 Internal Server Error**
```
{ code: 500, message: "具体错误信息" }
```

**解决**:
- 查看后端控制台的错误日志
- 检查数据库连接
- 检查Redis连接

---

### 5. 前端代码调试技巧

在浏览器控制台执行以下命令：

```javascript
// 检查Token
console.log('Token:', localStorage.getItem('token'))
console.log('UserInfo:', JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 手动测试API
fetch('http://localhost:8081/api/ticket/list?pageNum=1&pageSize=10', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})
.then(res => res.json())
.then(data => console.log('Response:', data))
.catch(err => console.error('Error:', err))
```

---

### 6. 各角色测试账号

根据init.sql中的初始数据：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 可以管理所有功能 |
| 维修员 | staff001 | 123456 | 只能处理分配的工单 |
| 普通用户 | user001 | 123456 | 只能创建和查看自己的工单 |

---

### 7. 完整的API测试清单

#### 认证模块
- [ ] POST /api/auth/login - 登录
- [ ] POST /api/auth/register - 注册
- [ ] POST /api/auth/logout - 登出

#### 公告模块（公开）
- [ ] GET /api/notice/published - 获取已发布公告
- [ ] GET /api/notice/{id} - 获取公告详情

#### 工单模块
- [ ] POST /api/ticket - 创建工单
- [ ] GET /api/ticket/{id} - 获取工单详情
- [ ] GET /api/ticket/list - 获取工单列表
- [ ] GET /api/ticket/{id}/logs - 获取工单日志
- [ ] PUT /api/ticket/{id}/cancel - 取消工单

#### 管理员模块
- [ ] POST /api/admin/assign - 派单
- [ ] GET /api/admin/pending-tickets - 获取待处理工单
- [ ] GET /api/admin/high-priority-tickets - 获取高优先级工单
- [ ] GET /api/admin/staff/list - 获取员工列表

#### 维修员模块
- [ ] POST /api/staff/{ticketId}/accept - 接单
- [ ] POST /api/staff/{ticketId}/complete - 完成工单
- [ ] GET /api/staff/my-tickets - 获取我的工单

#### 评价模块
- [ ] POST /api/evaluation - 创建评价
- [ ] GET /api/evaluation/ticket/{ticketId} - 获取工单评价
- [ ] GET /api/evaluation/staff/{staffId} - 获取员工评价

---

### 8. 排查"网络错误"的具体步骤

如果前端显示"网络错误"，请按以下步骤排查：

1. **检查后端服务是否运行**
   ```bash
   # 在命令行执行
   netstat -ano | findstr :8081
   ```
   应该能看到8081端口被监听

2. **检查前端配置**
   查看 `前端/edufix-web/src/utils/request.js` 第6行：
   ```javascript
   baseURL: 'http://localhost:8081/api'
   ```

3. **直接在浏览器访问后端API**
   ```
   http://localhost:8081/api/notice/published
   ```
   应该能看到JSON响应

4. **检查浏览器控制台**
   - Network标签：查看具体哪个请求失败
   - Console标签：查看JavaScript错误
   - 查看失败的请求详情（Headers, Response）

5. **检查后端日志**
   - 查看后端控制台输出
   - 是否有异常堆栈信息
   - 请求是否到达后端

6. **使用curl测试**
   ```bash
   curl -v http://localhost:8081/api/notice/published
   ```
   查看详细请求过程

---

## 常见问题FAQ

**Q: 前端能访问，但显示"网络错误"？**
A: 通常是Token问题或响应解析问题。检查浏览器控制台的Network标签，看实际返回的状态码和内容。

**Q: 后端日志显示返回了数据，但前端收不到？**
A: 可能是CORS问题或响应拦截器问题。检查响应拦截器的逻辑，特别是code的判断。

**Q: 某些接口401，某些接口正常？**
A: 检查WebConfig.java中的excludePathPatterns配置，确认哪些接口不需要认证。

**Q: 登录后立即调用其他接口失败？**
A: 检查Token是否正确保存到localStorage，以及请求拦截器是否正确添加了Authorization头。
