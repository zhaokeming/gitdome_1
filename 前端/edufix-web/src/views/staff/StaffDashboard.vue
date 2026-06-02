<template>
  <div class="staff-dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon assigned">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.assigned }}</div>
              <div class="stat-label">待接单</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon processing">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inProgress }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
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
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总工单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工单标签页 -->
    <el-card style="margin-top: 20px">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待处理工单" name="current">
          <el-table :data="currentTicketList" style="width: 100%" v-loading="loading">
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
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
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
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status, row.category) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="位置" width="150" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewDetail(row.id)">详情</el-button>
                <el-button
                  v-if="row.status === 'ASSIGNED'"
                  link
                  type="success"
                  @click="handleAccept(row.id)"
                >
                  接单
                </el-button>
                <el-button
                  v-if="row.status === 'IN_PROGRESS'"
                  link
                  type="warning"
                  @click="handleComplete(row.id)"
                >
                  完成
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loading && currentTicketList.length === 0" description="暂无待处理工单" />

          <el-pagination
            v-if="currentTicketList.length > 0"
            v-model:current-page="currentPagination.pageNum"
            v-model:page-size="currentPagination.pageSize"
            :total="currentPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadCurrentTickets"
            @current-change="loadCurrentTickets"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>

        <el-tab-pane label="已完成工单" name="history">
          <el-table :data="historyTicketList" style="width: 100%" v-loading="historyLoading">
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
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
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
            <el-table-column prop="location" label="位置" width="150" show-overflow-tooltip />
            <el-table-column prop="completeTime" label="完成时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.completeTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewDetail(row.id)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!historyLoading && historyTicketList.length === 0" description="暂无已完成工单" />

          <el-pagination
            v-if="historyTicketList.length > 0"
            v-model:current-page="historyPagination.pageNum"
            v-model:page-size="historyPagination.pageSize"
            :total="historyPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadHistoryTickets"
            @current-change="loadHistoryTickets"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyTickets, getMyTicketStats, acceptTicket, completeTicket } from '@/api/staff'
import { getCategoryText, getUrgencyText, getUrgencyType, getStatusText, getStatusType, formatTime } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Loading, CircleCheck, Tickets } from '@element-plus/icons-vue'

const router = useRouter()

const activeTab = ref('current')
const loading = ref(false)
const historyLoading = ref(false)

const currentTicketList = ref([])
const historyTicketList = ref([])

const stats = ref({
  assigned: 0,
  inProgress: 0,
  completed: 0,
  total: 0
})

const currentPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const historyPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadStats = async () => {
  try {
    const res = await getMyTicketStats()
    stats.value.assigned = res.data.assigned || 0
    stats.value.inProgress = res.data.inProgress || 0
    stats.value.completed = res.data.completed || 0
    stats.value.total = res.data.total || 0
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadCurrentTickets = async () => {
  loading.value = true
  try {
    const res = await getMyTickets({
      pageNum: currentPagination.pageNum,
      pageSize: currentPagination.pageSize,
      status: '' // 不传状态，后端返回所有工单；前端按待处理过滤
    })
    const allRecords = res.data.records || []
    currentTicketList.value = allRecords.filter(t => t.status === 'ASSIGNED' || t.status === 'IN_PROGRESS')
    currentPagination.total = currentTicketList.value.length
  } catch (error) {
    console.error('加载工单失败:', error)
  } finally {
    loading.value = false
  }
}

const loadHistoryTickets = async () => {
  historyLoading.value = true
  try {
    const res = await getMyTickets({
      pageNum: historyPagination.pageNum,
      pageSize: historyPagination.pageSize,
      status: 'COMPLETED'
    })
    historyTicketList.value = res.data.records || []
    historyPagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载历史工单失败:', error)
  } finally {
    historyLoading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab === 'current') {
    loadCurrentTickets()
  } else if (tab === 'history') {
    loadHistoryTickets()
  }
}

const viewDetail = (id) => {
  router.push(`/tickets/${id}`)
}

const handleAccept = async (id) => {
  try {
    await ElMessageBox.confirm('确定接单并开始处理该工单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    await acceptTicket(id)
    ElMessage.success('已接单，开始处理')
    loadStats()
    loadCurrentTickets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
    }
  }
}

const handleComplete = async (id) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入完成说明（可选）', '完成工单', {
      confirmButtonText: '确定完成',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPattern: /.{0,200}/,
      inputErrorMessage: '最多200个字符'
    })

    await completeTicket(id, remark || '')
    ElMessage.success('工单已完成')
    loadStats()
    loadCurrentTickets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
    }
  }
}

onMounted(() => {
  loadStats()
  loadCurrentTickets()
})
</script>

<style scoped>
.staff-dashboard {
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

.stat-icon.assigned {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
</style>
