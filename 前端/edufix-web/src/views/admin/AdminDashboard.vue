<template>
  <div class="admin-dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待派单</div>
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
      <!-- 高优先级工单 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>高优先级工单</span>
              <el-button link type="primary" @click="router.push('/admin/assign')">去派单</el-button>
            </div>
          </template>
          <el-empty v-if="highPriorityTickets.length === 0" description="暂无高优先级工单" />
          <div v-else class="ticket-list">
            <div
              v-for="ticket in highPriorityTickets"
              :key="ticket.id"
              class="ticket-item"
              @click="viewTicket(ticket.id)"
            >
              <div class="ticket-header">
                <span class="ticket-no">{{ ticket.ticketNo }}</span>
                <el-tag :type="getUrgencyType(ticket.urgency)" size="small">
                  {{ getUrgencyText(ticket.urgency) }}
                </el-tag>
              </div>
              <div class="ticket-title">{{ ticket.title }}</div>
              <div class="ticket-info">
                <span>{{ getCategoryText(ticket.category) }}</span>
                <span>{{ formatTime(ticket.createTime) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 快捷操作 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="router.push('/admin/assign')">
              <el-icon><Operation /></el-icon>
              <span>工单派单</span>
            </el-button>
            <el-button @click="router.push('/admin/notices')">
              <el-icon><Bell /></el-icon>
              <span>发布公告</span>
            </el-button>
            <el-button @click="router.push('/tickets')">
              <el-icon><Document /></el-icon>
              <span>查看所有工单</span>
            </el-button>
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
              <el-button link type="primary" @click="loadTickets">刷新</el-button>
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
            <el-table-column prop="category" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ getCategoryText(row.category) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="urgency" label="紧急程度" width="100">
              <template #default="{ row }">
                <el-tag :type="getUrgencyType(row.urgency)" size="small">
                  {{ getUrgencyText(row.urgency) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
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
                <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
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
import { getTicketList, deleteTicket } from '@/api/ticket'
import { getHighPriorityTickets } from '@/api/admin'
import { getCategoryText, getUrgencyText, getUrgencyType, getStatusText, getStatusType, formatTime } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Loading, CircleCheck, Document, Operation, Bell } from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref({
  pending: 0,
  processing: 0,
  completed: 0,
  total: 0
})

const highPriorityTickets = ref([])
const recentTickets = ref([])

const loadTickets = async () => {
  try {
    const res = await getTicketList({ pageNum: 1, pageSize: 100 })
    const tickets = res.data.records || []
    
    // 统计数据
    stats.value.total = tickets.length
    stats.value.pending = tickets.filter(t => t.status === 'PENDING' && t.category === 'REPAIR').length
    stats.value.processing = tickets.filter(t => t.status === 'IN_PROGRESS' || t.status === 'ASSIGNED').length
    stats.value.completed = tickets.filter(t => t.status === 'COMPLETED' || t.status === 'PROCESSED').length
    
    // 最近工单
    recentTickets.value = tickets.slice(0, 10)
  } catch (error) {
    console.error('加载工单失败:', error)
  }
}

const loadHighPriorityTickets = async () => {
  try {
    const res = await getHighPriorityTickets(10)
    highPriorityTickets.value = res.data || []
  } catch (error) {
    console.error('加载高优先级工单失败:', error)
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
    loadHighPriorityTickets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除工单失败:', error)
    }
  }
}

onMounted(() => {
  loadTickets()
  loadHighPriorityTickets()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
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
  min-width: 150px;
}

.ticket-list {
  max-height: 400px;
  overflow-y: auto;
}

.ticket-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.ticket-item:hover {
  background-color: #f5f7fa;
}

.ticket-item:last-child {
  border-bottom: none;
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.ticket-no {
  font-size: 12px;
  color: #999;
}

.ticket-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ticket-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}
</style>
