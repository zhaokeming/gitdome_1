<template>
  <div class="admin-ticket-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>工单管理 - {{ categoryTitle }}</span>
        </div>
      </template>

      <!-- 分类切换标签 -->
      <el-tabs v-model="activeCategory" @tab-change="onCategoryChange" class="category-tabs">
        <el-tab-pane label="报修" name="REPAIR" />
        <el-tab-pane label="失物招领" name="LOST_FOUND" />
        <el-tab-pane label="校园建议" name="SUGGESTION" />
      </el-tabs>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="工单号/标题"
            clearable
            style="width: 200px"
            @keyup.enter="loadTickets"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option
              v-for="opt in statusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="紧急程度">
          <el-select v-model="searchForm.urgency" placeholder="全部" clearable style="width: 120px">
            <el-option label="紧急" value="URGENT" />
            <el-option label="高" value="HIGH" />
            <el-option label="普通" value="NORMAL" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="searchTickets">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 工单表格 -->
      <el-table :data="ticketList" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="ticketNo" label="工单号" width="170" />
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
        <el-table-column prop="urgency" label="紧急程度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getUrgencyType(row.urgency)" size="small">
              {{ getUrgencyText(row.urgency) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status, row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row.id)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button
              v-if="row.status === 'PENDING' && row.category === 'REPAIR'"
              link
              type="success"
              size="small"
              @click="goAssign(row.id)"
            >
              <el-icon><Operation /></el-icon>
              派单
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              link
              type="danger"
              size="small"
              @click="handleCancel(row.id)"
            >
              取消
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && ticketList.length === 0" description="暂无工单数据" />

      <!-- 分页 -->
      <el-pagination
        v-if="ticketList.length > 0"
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getTicketList, cancelTicket, deleteTicket } from '@/api/ticket'
import { getUrgencyText, getUrgencyType, getStatusText, getStatusType, formatTime, CATEGORY_MAP } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Operation } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const ticketList = ref([])

// 当前激活的分类（从路由获取）
const activeCategory = ref(route.params.category || 'REPAIR')

const categoryTitle = computed(() => CATEGORY_MAP[activeCategory.value] || '全部')

const statusOptions = computed(() => {
  const repairOptions = [
    { label: '待派单', value: 'PENDING' },
    { label: '待接单', value: 'ASSIGNED' },
    { label: '处理中', value: 'IN_PROGRESS' },
    { label: '已完成', value: 'COMPLETED' },
    { label: '已取消', value: 'CANCELLED' }
  ]
  const suggestionOptions = [
    { label: '未处理', value: 'PENDING' },
    { label: '已处理', value: 'PROCESSED' },
    { label: '已取消', value: 'CANCELLED' }
  ]
  const lostFoundOptions = [
    { label: '待处理', value: 'PENDING' },
    { label: '已派单', value: 'ASSIGNED' },
    { label: '处理中', value: 'IN_PROGRESS' },
    { label: '已处理待确认', value: 'RESOLVED' },
    { label: '已完成', value: 'COMPLETED' },
    { label: '已取消', value: 'CANCELLED' }
  ]
  switch (activeCategory.value) {
    case 'SUGGESTION': return suggestionOptions
    case 'LOST_FOUND': return lostFoundOptions
    default: return repairOptions
  }
})

const searchForm = reactive({
  keyword: '',
  status: '',
  urgency: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 切换分类
const onCategoryChange = (category) => {
  activeCategory.value = category
  router.replace(`/admin/tickets/${category}`)
  searchForm.status = ''
  pagination.pageNum = 1
  loadTickets()
}

const loadTickets = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      category: activeCategory.value,
      status: searchForm.status || undefined,
      keyword: searchForm.keyword || undefined
    }

    const res = await getTicketList(params)
    const data = res.data || {}

    let records = data.records || []
    pagination.total = data.total || 0

    // 前端按紧急程度二次过滤（后端若支持则移除）
    if (searchForm.urgency) {
      records = records.filter(t => t.urgency === searchForm.urgency)
    }

    // 前端按关键字二次过滤（后端若支持则移除）
    if (searchForm.keyword) {
      const kw = searchForm.keyword.toLowerCase()
      records = records.filter(t =>
        (t.ticketNo && t.ticketNo.toLowerCase().includes(kw)) ||
        (t.title && t.title.toLowerCase().includes(kw))
      )
    }

    ticketList.value = records
  } catch (error) {
    console.error('加载工单失败:', error)
    ElMessage.error('加载工单失败')
  } finally {
    loading.value = false
  }
}

const searchTickets = () => {
  pagination.pageNum = 1
  loadTickets()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.urgency = ''
  pagination.pageNum = 1
  loadTickets()
}

const viewDetail = (id) => {
  router.push(`/tickets/${id}`)
}

const goAssign = (id) => {
  router.push({ path: '/admin/assign', query: { ticketId: id } })
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该工单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelTicket(id, '管理员取消')
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

// 监听路由变化，同步分类
watch(() => route.params.category, (newVal) => {
  if (newVal && newVal !== activeCategory.value) {
    activeCategory.value = newVal
    pagination.pageNum = 1
    loadTickets()
  }
})

onMounted(() => {
  loadTickets()
})
</script>

<style scoped>
.admin-ticket-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-tabs {
  margin-bottom: 10px;
}

.category-tabs :deep(.el-tabs__header) {
  margin-bottom: 10px;
}

.search-form {
  margin-bottom: 10px;
}

.search-form .el-form-item {
  margin-bottom: 10px;
}
</style>
