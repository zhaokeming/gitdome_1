<template>
  <div class="assign-ticket">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>工单派单</span>
          <el-tag type="info" size="small">仅显示报修类工单</el-tag>
        </div>
      </template>

      <el-empty v-if="!loading && pendingTickets.length === 0" description="暂无待派单的报修工单" />

      <el-table v-else :data="pendingTickets" style="width: 100%" v-loading="loading">
        <el-table-column prop="ticketNo" label="工单号" width="160" />
        <el-table-column label="图片" width="70" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.images"
              :src="row.images.split(',')[0]"
              :preview-src-list="row.images.split(',')"
              style="width: 36px; height: 36px; border-radius: 4px"
              fit="cover"
              preview-teleported
            />
            <span v-else style="color: #ccc; font-size: 12px">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="维修类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.type || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="urgency" label="紧急程度" width="100">
          <template #default="{ row }">
            <el-tag :type="getUrgencyType(row.urgency)" size="small">
              {{ getUrgencyText(row.urgency) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showAssignDialog(row)">派单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 派单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="工单派单"
      width="520px"
      @close="resetAssignForm"
    >
      <el-descriptions :column="1" border size="small" style="margin-bottom: 20px">
        <el-descriptions-item label="工单号">{{ currentTicket?.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentTicket?.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentTicket?.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ currentTicket?.location || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="维修员" required>
          <el-select v-model="assignForm.staffId" placeholder="请选择维修员" style="width: 100%">
            <el-option
              v-for="staff in availableStaffList"
              :key="staff.id"
              :label="`${staff.realName} - ${staff.specialty || '未设置工种'}`"
              :value="staff.id"
            >
              <div class="staff-option">
                <div class="staff-option-main">
                  <span class="staff-name">{{ staff.realName }}</span>
                  <el-tag :type="getStaffStatusType(staff.status)" size="small">
                    {{ getStaffStatusText(staff.status) }}
                  </el-tag>
                </div>
                <div class="staff-option-sub">
                  <span>{{ staff.department || '-' }}</span>
                  <span>{{ staff.specialty || '未设置工种' }}</span>
                  <span>评分: {{ staff.rating || '5.0' }}</span>
                  <span>已完成: {{ staff.completedCount || 0 }}单</span>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="assignForm.remark"
            type="textarea"
            :rows="3"
            placeholder="选填，可添加派单说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign" :loading="assigning">
          确认派单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPendingTickets, getStaffList, assignTicket } from '@/api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const assigning = ref(false)
const pendingTickets = ref([])
const staffList = ref([])
const dialogVisible = ref(false)
const currentTicket = ref(null)

const assignForm = ref({
  staffId: null,
  remark: ''
})

const availableStaffList = computed(() => {
  return staffList.value.filter(s => s.status !== 0)
})

const loadPendingTickets = async () => {
  loading.value = true
  try {
    const res = await getPendingTickets()
    const allTickets = res.data || []
    pendingTickets.value = allTickets.filter(t => t.category === 'REPAIR')
  } catch (error) {
    console.error('加载待处理工单失败:', error)
    ElMessage.error('加载待处理工单失败')
  } finally {
    loading.value = false
  }
}

const loadStaffList = async () => {
  try {
    const res = await getStaffList()
    staffList.value = res.data || []
  } catch (error) {
    console.error('加载维修员列表失败:', error)
  }
}

const showAssignDialog = (ticket) => {
  currentTicket.value = ticket
  assignForm.value = {
    staffId: null,
    remark: ''
  }
  dialogVisible.value = true
}

const resetAssignForm = () => {
  currentTicket.value = null
}

const handleAssign = async () => {
  if (!assignForm.value.staffId) {
    ElMessage.warning('请选择维修员')
    return
  }

  assigning.value = true
  try {
    await assignTicket({
      ticketId: currentTicket.value.id,
      staffId: assignForm.value.staffId,
      remark: assignForm.value.remark
    })

    ElMessage.success('派单成功')
    dialogVisible.value = false
    loadPendingTickets()
  } catch (error) {
    console.error('派单失败:', error)
  } finally {
    assigning.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getUrgencyText = (urgency) => {
  const map = { LOW: '低', NORMAL: '普通', HIGH: '高', URGENT: '紧急' }
  return map[urgency] || urgency
}

const getUrgencyType = (urgency) => {
  const map = { LOW: 'info', NORMAL: 'info', HIGH: 'warning', URGENT: 'danger' }
  return map[urgency] || 'info'
}

const getStaffStatusText = (status) => {
  const map = { 0: '离线', 1: '空闲', 2: '忙碌', 3: '繁忙' }
  return map[status] || '未知'
}

const getStaffStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

onMounted(() => {
  loadPendingTickets()
  loadStaffList()
})
</script>

<style scoped>
.assign-ticket {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.staff-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
}

.staff-option-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.staff-name {
  font-weight: 500;
}

.staff-option-sub {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
}
</style>
