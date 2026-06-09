<template>
  <div class="main-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
        <div class="logo">
          <h2 v-if="!isCollapse">EduFix</h2>
          <h2 v-else>EF</h2>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <!-- 普通用户菜单 -->
          <template v-if="userStore.isUser">
            <el-menu-item index="/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/tickets">
              <el-icon><Document /></el-icon>
              <span>我的工单</span>
            </el-menu-item>
            <el-menu-item index="/tickets/create">
              <el-icon><Plus /></el-icon>
              <span>创建工单</span>
            </el-menu-item>
            <el-menu-item index="/lost-found">
              <el-icon><Search /></el-icon>
              <span>失物招领</span>
            </el-menu-item>
            <el-menu-item index="/notices">
              <el-icon><Bell /></el-icon>
              <span>公告信息</span>
            </el-menu-item>
          </template>
          
          <!-- 管理员菜单 -->
          <template v-if="userStore.isAdmin">
            <el-menu-item index="/admin/dashboard">
              <el-icon><DataAnalysis /></el-icon>
              <span>管理后台</span>
            </el-menu-item>
            <el-menu-item index="/admin/tickets/repair">
              <el-icon><List /></el-icon>
              <span>工单管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/assign">
              <el-icon><Operation /></el-icon>
              <span>工单派单</span>
            </el-menu-item>
            <el-menu-item index="/admin/notices">
              <el-icon><Bell /></el-icon>
              <span>公告管理</span>
            </el-menu-item>
          </template>
          
          <!-- 维修员菜单 -->
          <template v-if="userStore.isStaff">
            <el-menu-item index="/staff/dashboard">
              <el-icon><Tools /></el-icon>
              <span>工作台</span>
            </el-menu-item>
            <el-menu-item index="/tickets">
              <el-icon><Document /></el-icon>
              <span>我的工单</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="toggleCollapse">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          
          <div class="header-right">
            <!-- 消息通知 -->
            <el-popover
              :visible="notificationVisible"
              placement="bottom"
              :width="360"
              trigger="click"
              @show="loadNotifications"
            >
              <template #reference>
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
                  <el-icon class="notification-icon" @click="notificationVisible = !notificationVisible">
                    <Bell />
                  </el-icon>
                </el-badge>
              </template>
              <div class="notification-popover">
                <div class="notification-header">
                  <span>消息通知</span>
                  <el-button v-if="unreadCount > 0" link type="primary" size="small" @click="handleReadAll">
                    全部已读
                  </el-button>
                </div>
                <el-empty v-if="notifications.length === 0" description="暂无通知" :image-size="60" />
                <div v-else class="notification-list">
                  <div
                    v-for="item in notifications"
                    :key="item.id"
                    class="notification-item"
                    :class="{ unread: item.isRead === 0 }"
                    @click="handleNotificationClick(item)"
                  >
                    <div class="notify-title">{{ item.title }}</div>
                    <div class="notify-content">{{ item.content }}</div>
                    <div class="notify-time">{{ formatRelativeTime(item.createTime) }}</div>
                  </div>
                </div>
              </div>
            </el-popover>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo.avatar || ''">
                  {{ userStore.userInfo.realName?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo.realName || '用户' }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import { formatRelativeTime } from '@/utils/constants'
import {
  HomeFilled,
  Document,
  Plus,
  Bell,
  DataAnalysis,
  Operation,
  Tools,
  List,
  Search,
  ChatLineSquare,
  Fold,
  Expand
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)

// 通知相关
const notificationVisible = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
let pollTimer = null

const activeMenu = computed(() => route.path)

const loadNotifications = async () => {
  try {
    const res = await getNotifications()
    notifications.value = res.data || []
  } catch (error) {
    console.error('加载通知失败:', error)
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (error) {
    // 静默处理
  }
}

const handleNotificationClick = async (item) => {
  if (item.isRead === 0) {
    await markAsRead(item.id)
    loadUnreadCount()
  }
  notificationVisible.value = false
  if (item.type === 'TICKET' && item.relatedId) {
    router.push(`/tickets/${item.relatedId}`)
  }
}

const handleReadAll = async () => {
  await markAllAsRead()
  loadUnreadCount()
  loadNotifications()
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logout()
      router.push('/login')
    } catch {
      // 用户取消
    }
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  loadUnreadCount()
  // 每30秒轮询未读通知数
  pollTimer = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  width: 100%;
}

.el-container {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}

.el-menu {
  border-right: none;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

/* 通知样式 */
.notification-badge {
  margin-right: 16px;
}

.notification-icon {
  font-size: 22px;
  cursor: pointer;
  color: #666;
  transition: color 0.3s;
}

.notification-icon:hover {
  color: #409EFF;
}

.notification-popover {
  max-height: 400px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
  margin-bottom: 8px;
}

.notification-list {
  max-height: 320px;
  overflow-y: auto;
}

.notification-item {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f0f2f5;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notify-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.notify-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 4px;
}

.notify-time {
  font-size: 12px;
  color: #999;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
