import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'tickets',
        name: 'TicketList',
        component: () => import('@/views/ticket/TicketList.vue'),
        meta: { title: '工单列表', requiresAuth: true }
      },
      {
        path: 'tickets/create',
        name: 'CreateTicket',
        component: () => import('@/views/ticket/CreateTicket.vue'),
        meta: { title: '创建工单', requiresAuth: true }
      },
      {
        path: 'tickets/:id',
        name: 'TicketDetail',
        component: () => import('@/views/ticket/TicketDetail.vue'),
        meta: { title: '工单详情', requiresAuth: true }
      },
      {
        path: 'notices',
        name: 'NoticeList',
        component: () => import('@/views/notice/NoticeList.vue'),
        meta: { title: '公告列表', requiresAuth: true }
      },
      {
        path: 'notices/:id',
        name: 'NoticeDetail',
        component: () => import('@/views/notice/NoticeDetail.vue'),
        meta: { title: '公告详情', requiresAuth: true }
      },
      {
        path: 'lost-found',
        name: 'LostFoundList',
        component: () => import('@/views/lost-found/LostFoundList.vue'),
        meta: { title: '失物招领', requiresAuth: true }
      },
      // 管理员路由
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/tickets/repair',
        name: 'AdminTicketsRepair',
        component: () => import('@/views/admin/AdminTicketList.vue'),
        meta: { title: '维修工单管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/tickets/lost-found',
        name: 'AdminTicketsLostFound',
        component: () => import('@/views/admin/AdminTicketList.vue'),
        meta: { title: '失物招领管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/tickets/suggestion',
        name: 'AdminTicketsSuggestion',
        component: () => import('@/views/admin/AdminTicketList.vue'),
        meta: { title: '校园建议管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/tickets/:category',
        redirect: to => `/admin/tickets/${to.params.category || 'repair'}`
      },
      {
        path: 'admin/assign',
        name: 'AssignTicket',
        component: () => import('@/views/admin/AssignTicket.vue'),
        meta: { title: '工单派单', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/notices',
        name: 'ManageNotices',
        component: () => import('@/views/admin/ManageNotices.vue'),
        meta: { title: '公告管理', requiresAuth: true, requiresAdmin: true }
      },
      // 维修员路由
      {
        path: 'staff/dashboard',
        name: 'StaffDashboard',
        component: () => import('@/views/staff/StaffDashboard.vue'),
        meta: { title: '维修员工作台', requiresAuth: true, requiresStaff: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - EduFix` : 'EduFix'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }
  
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/dashboard')
    return
  }
  
  // 检查是否需要维修员权限
  if (to.meta.requiresStaff && !userStore.isStaff) {
    next('/dashboard')
    return
  }
  
  // 如果已登录，访问登录或注册页面时跳转到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
