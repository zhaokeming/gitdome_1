<template>
  <div class="ticket-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>工单列表</span>
          <el-button v-if="!userStore.isStaff" type="primary" @click="router.push('/tickets/create')">
            <el-icon><Plus /></el-icon>
            创建工单
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索工单号/标题/描述"
            clearable
            style="width: 220px"
            @keyup.enter="loadTickets"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
            <template v-if="userStore.isStaff">
              <el-option label="已派单" value="ASSIGNED" />
              <el-option label="处理中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </template>
            <template v-else>
              <el-option label="待处理" value="PENDING" />
              <el-option label="已派单" value="ASSIGNED" />
              <el-option label="处理中" value="IN_PROGRESS" />
              <el-option label="已处理待确认" value="RESOLVED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已处理" value="PROCESSED" />
              <el-option label="已取消" value="CANCELLED" />
            </template>
          </el-select>
        </el-form-item>

        <el-form-item v-if="!userStore.isStaff" label="类型">
          <el-select v-model="searchForm.category" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="报修" value="REPAIR" />
            <el-option label="建议" value="SUGGESTION" />
            <el-option label="失物招领" value="LOST_FOUND" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadTickets">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 工单表格 -->
      <el-table :data="ticketList" style="width: 100%" v-loading="loading">
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
        <el-table-column prop="category" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getCategoryText(row.category) }}</el-tag>
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
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.id)">详情</el-button>
            <!-- 维修人员操作 -->
            <template v-if="userStore.isStaff">
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
            <!-- 学生操作 -->
            <template v-else>
              <el-button
                v-if="row.status === 'PENDING'"
                link
                type="danger"
                @click="handleCancel(row.id)"
              >
                取消
              </el-button>
              <el-button
                v-if="row.status === 'CANCELLED'"
                link
                type="danger"
                @click="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadTickets"
        @current-change="loadTickets"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTicketList, cancelTicket, deleteTicket } from '@/api/ticket'
import { acceptTicket, completeTicket } from '@/api/staff'
import { getCategoryText, getUrgencyText, getUrgencyType, getStatusText, getStatusType, formatTime } from '@/utils/constants'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const ticketList = ref([])

const searchForm = reactive({
  keyword: '',
  status: '',
  category: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadTickets = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    }

    const res = await getTicketList(params)
    ticketList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载工单失败:', error)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.category = ''
  pagination.pageNum = 1
  loadTickets()
}

const viewDetail = (id) => {
  router.push(`/tickets/${id}`)
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该工单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelTicket(id, '用户主动取消')
    ElMessage.success('工单已取消')
    loadTickets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消工单失败:', error)
    }
  }
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

const handleAccept = async (id) => {
  try {
    await ElMessageBox.confirm('确定接单并开始处理该工单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    await acceptTicket(id)
    ElMessage.success('已接单，开始处理')
    loadTickets()
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
    loadTickets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
    }
  }
}

onMounted(() => {
  loadTickets()
})
</script>

<style scoped>
.ticket-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
