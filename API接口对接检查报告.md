# API接口对接检查报告

## ✅ 已正确对接的接口

### 1. 认证模块 (Auth)
| 前端API | 后端端点 | 状态 |
|---------|----------|------|
| `/auth/login` (POST) | `/api/auth/login` | ✅ 正确 |
| `/auth/register` (POST) | `/api/auth/register` | ✅ 正确 |
| `/auth/logout` (POST) | `/api/auth/logout` | ✅ 正确 |

### 2. 公告模块 (Notice)
| 前端API | 后端端点 | 状态 |
|---------|----------|------|
| `/notice/published` (GET) | `/api/notice/published` | ✅ 正确 |
| `/notice/:id` (GET) | `/api/notice/{id}` | ✅ 正确 |
| `/notice` (POST) | `/api/notice` | ✅ 正确 |
| `/notice/:id/publish` (PUT) | `/api/notice/{id}/publish` | ✅ 正确 |

### 3. 评价模块 (Evaluation)
| 前端API | 后端端点 | 状态 |
|---------|----------|------|
| `/evaluation` (POST) | `/api/evaluation` | ✅ 正确 |
| `/evaluation/ticket/:ticketId` (GET) | `/api/evaluation/ticket/{ticketId}` | ✅ 正确 |
| `/evaluation/staff/:staffId` (GET) | `/api/evaluation/staff/{staffId}` | ✅ 正确 |

---

## ⚠️ 需要修复的接口

### 4. 工单模块 (Ticket)

#### 4.1 工单列表 - 普通用户
**前端调用**: `getTicketList(params)` → `/api/ticket/list`  
**后端端点**: `/api/ticket/list` (GET)  
**状态**: ⚠️ **逻辑问题**

**问题分析**:
- 后端根据角色返回不同数据：
  - USER: 只返回当前用户的工单
  - ADMIN/STAFF: 返回所有工单
- **维修员(STAFF)应该只看到分配给自己的工单，而不是所有工单**

**解决方案**: 
维修员应该使用专门的 `/api/staff/my-tickets` 端点

---

#### 4.2 工单列表 - 维修员专用
**前端**: StaffDashboard.vue 使用 `getTicketList()`  
**后端存在**: `/api/staff/my-tickets` (GET)  
**状态**: ❌ **前端未使用**

**问题**: 
- StaffDashboard.vue 调用了通用的 `getTicketList()` API
- 后端有专门的 `/api/staff/my-tickets` 端点用于获取维修员的工单
- 前端缺少对应的API封装

**需要添加前端API**:
```javascript
// src/api/staff.js
import request from '@/utils/request'

// 获取我的工单列表
export function getMyTickets(params) {
  return request({
    url: '/staff/my-tickets',
    method: 'get',
    params
  })
}

// 接单
export function acceptTicket(ticketId) {
  return request({
    url: `/staff/${ticketId}/accept`,
    method: 'post'
  })
}

// 完成工单
export function completeTicket(ticketId, remark) {
  return request({
    url: `/staff/${ticketId}/complete`,
    method: 'post',
    params: { remark }
  })
}
```

---

### 5. 管理员模块 (Admin)

#### 5.1 管理员API
| 前端API | 后端端点 | 状态 |
|---------|----------|------|
| `/admin/assign` (POST) | `/api/admin/assign` | ✅ 正确 |
| `/admin/pending-tickets` (GET) | `/api/admin/pending-tickets` | ✅ 正确 |
| `/admin/high-priority-tickets` (GET) | `/api/admin/high-priority-tickets` | ✅ 正确 |
| `/admin/staff/list` (GET) | `/api/admin/staff/list` | ✅ 正确 |

---

## 🔧 需要修复的问题汇总

### 问题1: 维修员工单列表API缺失
**文件**: `前端/edufix-web/src/api/staff.js`  
**操作**: 创建新文件，添加维修员专用API

### 问题2: StaffDashboard使用错误的API
**文件**: `前端/edufix-web/src/views/staff/StaffDashboard.vue`  
**当前**: 使用 `getTicketList()`  
**应该**: 使用 `getMyTickets()`

### 问题3: 接单和完成工单功能未实现
**文件**: `前端/edufix-web/src/views/staff/StaffDashboard.vue`  
**位置**: 第221-254行  
**状态**: TODO注释，需要调用真实API

### 问题4: 前端响应拦截器可能误判
**文件**: `前端/edufix-web/src/utils/request.js`  
**位置**: 第31行  
**问题**: 检查 `res.code !== 200`，但实际后端返回的是标准Result格式

---

## 📋 完整API对照表

### 基础URL
- 前端配置: `http://localhost:8081/api`
- 后端监听: `http://localhost:8081`

### 公开接口（无需Token）
| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 登录 |
| `/api/auth/register` | POST | 注册 |
| `/api/notice/published` | GET | 获取已发布公告 |
| `/api/notice/{id}` | GET | 获取公告详情 |

### 需要认证的接口

#### 通用接口
| 端点 | 方法 | 角色 | 说明 |
|------|------|------|------|
| `/api/auth/logout` | POST | 所有 | 登出 |
| `/api/ticket` | POST | 所有 | 创建工单 |
| `/api/ticket/{id}` | GET | 所有 | 获取工单详情 |
| `/api/ticket/list` | GET | 所有 | 获取工单列表（根据角色过滤） |
| `/api/ticket/{id}/logs` | GET | 所有 | 获取工单日志 |
| `/api/ticket/{id}/cancel` | PUT | 所有 | 取消工单 |
| `/api/evaluation` | POST | USER | 创建评价 |
| `/api/evaluation/ticket/{ticketId}` | GET | 所有 | 获取工单评价 |
| `/api/evaluation/staff/{staffId}` | GET | 所有 | 获取员工评价列表 |

#### 管理员接口
| 端点 | 方法 | 角色 | 说明 |
|------|------|------|------|
| `/api/admin/assign` | POST | ADMIN | 派单 |
| `/api/admin/pending-tickets` | GET | ADMIN/STAFF | 获取待处理工单 |
| `/api/admin/high-priority-tickets` | GET | ADMIN/STAFF | 获取高优先级工单 |
| `/api/admin/staff/list` | GET | ADMIN | 获取员工列表 |
| `/api/notice` | POST | ADMIN | 创建公告 |
| `/api/notice/{id}/publish` | PUT | ADMIN | 发布公告 |

#### 维修员接口
| 端点 | 方法 | 角色 | 说明 |
|------|------|------|------|
| `/api/staff/{ticketId}/accept` | POST | STAFF | 接单 |
| `/api/staff/{ticketId}/complete` | POST | STAFF | 完成工单 |
| `/api/staff/my-tickets` | GET | STAFF | 获取我的工单列表 |

---

## 🎯 下一步行动

1. **创建 staff.js API文件** - 添加维修员专用API
2. **修改 StaffDashboard.vue** - 使用正确的API
3. **实现接单和完成功能** - 调用后端API
4. **测试所有接口** - 确保前后端正常通信
