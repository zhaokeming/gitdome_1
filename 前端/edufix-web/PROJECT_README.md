# EduFix 前端项目

校园/社区综合服务与报修工单平台 - Vue3前端

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **Vue Router 4** - 官方路由管理器
- **Pinia** - Vue状态管理库
- **Element Plus** - Vue 3组件库
- **Axios** - HTTP客户端

## 功能模块

### 1. 认证模块
- 用户登录
- 用户注册
- JWT Token管理
- 角色权限控制（用户/维修员/管理员）

### 2. 普通用户功能
- 首页Dashboard（统计信息、快捷操作）
- 创建工单（报修/建议/失物招领）
- 查看我的工单列表
- 工单详情查看
- 工单进度追踪
- 服务评价
- 公告浏览

### 3. 管理员功能
- 管理后台Dashboard
- 工单派单（分配维修员）
- 查看高优先级工单
- 公告管理（创建、发布）
- 查看所有工单
- 数据统计

### 4. 维修员功能
- 工作台Dashboard
- 查看分配的工单
- 开始处理工单
- 完成工单
- 工单详情查看

## 项目结构

```
edufix-web/
├── src/
│   ├── api/              # API接口
│   │   ├── auth.js       # 认证接口
│   │   ├── ticket.js     # 工单接口
│   │   ├── admin.js      # 管理员接口
│   │   ├── evaluation.js # 评价接口
│   │   └── notice.js     # 公告接口
│   ├── layout/           # 布局组件
│   │   └── MainLayout.vue
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # Pinia状态管理
│   │   └── user.js       # 用户状态
│   ├── styles/           # 全局样式
│   │   └── global.css
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios封装
│   ├── views/            # 页面组件
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Dashboard.vue
│   │   ├── ticket/       # 工单相关页面
│   │   ├── admin/        # 管理员页面
│   │   ├── staff/        # 维修员页面
│   │   └── notice/       # 公告页面
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── package.json
├── vite.config.js
└── README.md
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发环境运行

```bash
npm run dev
```

访问 http://localhost:3000

### 生产环境构建

```bash
npm run build
```

### 预览构建结果

```bash
npm run preview
```

## 后端API配置

在 `src/utils/request.js` 中修改后端API地址：

```javascript
const request = axios.create({
  baseURL: 'http://localhost:8080/api', // 修改为你的后端地址
  timeout: 10000
})
```

## 角色说明

### 普通用户 (USER)
- 可以创建工单
- 查看自己的工单
- 对已完成的工单进行评价

### 维修员 (STAFF)
- 查看分配给自己的工单
- 开始处理工单
- 完成工单

### 管理员 (ADMIN)
- 查看所有工单
- 派单（分配维修员）
- 发布公告
- 查看统计数据

## 主要特性

1. **响应式设计** - 支持PC和移动端访问
2. **权限控制** - 基于角色的路由守卫
3. **Token管理** - 自动处理JWT Token
4. **优雅的错误处理** - 统一的错误提示
5. **简约大气的UI** - 使用Element Plus组件库
6. **流畅的交互体验** - 页面过渡动画

## Redis应用场景（后端实现）

本项目的后端使用Redis实现了以下功能：

- **String**: 存储短信验证码、JWT Token黑名单
- **Hash**: 缓存高频公告、分类字典、服务评价标签
- **Set**: 维护"待处理/处理中/已完成"工单ID集合
- **ZSet**: 按紧急程度+提交时间生成派单优先级队列

## 注意事项

1. 确保后端服务已启动
2. 首次使用需要注册账号
3. 不同角色登录后会看到不同的菜单和功能
4. 工单评价只能在工单完成后进行

## 开发规范

- 使用 Composition API (`<script setup>`)
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case
- 使用 ESLint + Prettier 保持代码风格一致

## 浏览器支持

- Chrome (最新)
- Firefox (最新)
- Safari (最新)
- Edge (最新)

## License

MIT
