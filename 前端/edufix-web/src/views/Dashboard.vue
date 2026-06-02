<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h2>欢迎回来，{{ userStore.userInfo.realName || '用户' }}</h2>
            <p>{{ getWelcomeMessage() }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon processing">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.processing }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon completed">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总工单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 快捷操作 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="router.push('/tickets/create')">
              <el-icon><Plus /></el-icon>
              <span>创建工单</span>
            </el-button>
            <el-button @click="router.push('/tickets')">
              <el-icon><Document /></el-icon>
              <span>查看工单</span>
            </el-button>
            <el-button @click="router.push('/notices')">
              <el-icon><Bell /></el-icon>
              <span>查看公告</span>
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 最新公告 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新公告</span>
              <el-button link type="primary" @click="router.push('/notices')">查看更多</el-button>
            </div>
          </template>
          <el-empty v-if="notices.length === 0" description="暂无公告" />
          <div v-else class="notice-list">
            <div
              v-for="notice in notices"
              :key="notice.id"
              class="notice-item"
              @click="viewNotice(notice.id)"
            >
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-time">{{ formatTime(notice.createTime) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 最近工单 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近工单</span>
              <el-button link type="primary" @click="router.push('/tickets')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentTickets" style="width: 100%">
            <el-table-column prop="ticketNo" label="工单号" width="180" />
            <el-table-column label="图片" width="80" align="center">
              <template #default="{ row }">
                <el-image
                  v-if="row.images"
                  :src="row.images.split(',')[0]"
                  :preview-src-list="row.images.split(',')"
                  style="width: 40px; height: 40px; border-radius: 4px"
                  fit="cover"
                  preview-teleported
                />
                <span v-else style="color: #ccc; font-size: 12px">无</span>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="category" label="类型" width="120">
              <template #default="{ row }">
                <el-tag>{{ getCategoryText(row.category) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status, row.category) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewTicket(row.id)">详情</el-button>
                <el-button
                  v-if="row.status === 'CANCELLED'"
                  link
                  type="danger"
                  @click="handleDelete(row.id)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTicketList, deleteTicket } from '@/api/ticket'
import { getPublishedNotices } from '@/api/notice'
import { getCategoryText, getStatusText, getStatusType, formatTime } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Loading, CircleCheck, Document, Plus, Bell } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
  pending: 0,
  processing: 0,
  completed: 0,
  total: 0
})

const recentTickets = ref([])
const notices = ref([])

const getWelcomeMessage = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息'
  if (hour < 9) return '早上好，开启美好的一天'
  if (hour < 12) return '上午好，工作顺利'
  if (hour < 14) return '中午好，记得休息'
  if (hour < 18) return '下午好，继续加油'
  return '晚上好，辛苦了'
}

const loadTickets = async () => {
  try {
    const res = await getTicketList({ pageNum: 1, pageSize: 100 })
    const tickets = res.data.records || []
    
    // 统计数据
    stats.value.total = tickets.length
    stats.value.pending = tickets.filter(t => t.status === 'PENDING').length
    stats.value.processing = tickets.filter(t => t.status === 'IN_PROGRESS' || t.status === 'ASSIGNED').length
    stats.value.completed = tickets.filter(t => t.status === 'COMPLETED' || t.status === 'PROCESSED').length
    
    // 最近工单
    recentTickets.value = tickets.slice(0, 5)
  } catch (error) {
    console.error('加载工单失败:', error)
  }
}

const loadNotices = async () => {
  try {
    const res = await getPublishedNotices()
    notices.value = (res.data || []).slice(0, 5)
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

const viewTicket = (id) => {
  router.push(`/tickets/${id}`)
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该工单吗？删除后不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteTicket(id)
    ElMessage.success('工单已删除')
    loadTickets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除工单失败:', error)
    }
  }
}

const viewNotice = (id) => {
  router.push(`/notices/${id}`)
}

onMounted(() => {
  loadTickets()
  loadNotices()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-content h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.welcome-content p {
  margin: 0;
  opacity: 0.9;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.processing {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.completed {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon.total {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 120px;
}

.notice-list {
  max-height: 300px;
  overflow-y: auto;
}

.notice-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notice-item:hover {
  background-color: #f5f7fa;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 12px;
  color: #999;
}
</style>
