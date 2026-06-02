<template>
  <div class="ticket-detail" v-loading="loading">
    <el-card v-if="ticket">
      <template #header>
        <div class="card-header">
          <span>工单详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <!-- 工单图片 -->
      <div v-if="ticket.images" class="images-section">
        <el-image
          v-for="(img, idx) in ticket.images.split(',')"
          :key="idx"
          :src="img"
          :preview-src-list="ticket.images.split(',')"
          style="width: 100px; height: 100px; margin-right: 8px"
          fit="cover"
        />
      </div>

      <!-- 工单基本信息 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单号">{{ ticket.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(ticket.status)">
            {{ getStatusText(ticket.status, ticket.category) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ ticket.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag>{{ getCategoryText(ticket.category) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="具体类型">
          <el-tag v-if="ticket.category === 'LOST_FOUND'" :type="ticket.type === 'LOST' ? 'danger' : 'success'">
            {{ getLostFoundTypeText(ticket.type) || '未分类' }}
          </el-tag>
          <span v-else>{{ ticket.type || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="紧急程度">
          <el-tag :type="getUrgencyType(ticket.urgency)">
            {{ getUrgencyText(ticket.urgency) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="位置">{{ ticket.location }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ ticket.contactInfo }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(ticket.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(ticket.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">
          {{ ticket.description }}
        </el-descriptions-item>
        <el-descriptions-item v-if="ticket.remark" label="备注" :span="2">
          {{ ticket.remark }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 处理结果（RESOLVED/COMPLETED时显示） -->
      <template v-if="ticket.status === 'RESOLVED' || ticket.status === 'COMPLETED' || ticket.status === 'PROCESSED'">
        <el-divider />
        <h3>处理结果</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="处理说明">{{ ticket.resolveContent || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="ticket.resolveImages" class="images-section" style="margin-top: 10px">
          <el-image
            v-for="(img, idx) in ticket.resolveImages.split(',')"
            :key="idx"
            :src="img"
            :preview-src-list="ticket.resolveImages.split(',')"
            style="width: 100px; height: 100px; margin-right: 8px"
            fit="cover"
          />
        </div>
      </template>
      
      <!-- 处理人员信息（失物招领不显示） -->
      <template v-if="ticket.category !== 'LOST_FOUND'">
        <el-divider />
        <el-descriptions :column="2" border>
          <el-descriptions-item label="维修员">
            {{ staffInfo?.realName || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ staffInfo?.phone || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
      
      <!-- 操作按钮区域 -->
      <el-divider />
      <div class="action-section">
        <!-- 维修员：提交处理结果 -->
        <template v-if="userStore.isStaff && ticket.status === 'IN_PROGRESS' && ticket.category === 'REPAIR'">
          <h3>提交处理结果</h3>
          <el-form :model="resolveForm" label-width="100px">
            <el-form-item label="处理说明">
              <el-input
                v-model="resolveForm.content"
                type="textarea"
                :rows="4"
                placeholder="请描述处理过程和结果"
              />
            </el-form-item>
            <el-form-item label="现场照片">
              <el-upload
                v-model:file-list="resolveImagesList"
                :action="uploadUrl"
                :headers="uploadHeaders"
                list-type="picture-card"
                :on-success="onResolveImageSuccess"
                :on-remove="onResolveImageRemove"
                multiple
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleResolve" :loading="resolving">
                提交处理结果
              </el-button>
            </el-form-item>
          </el-form>
        </template>
        
        <!-- 用户：确认完成 -->
        <template v-if="userStore.isUser && ticket.status === 'RESOLVED'">
          <el-button type="success" size="large" @click="handleConfirm" :loading="confirming">
            确认完成
          </el-button>
          <span style="margin-left: 12px; color: #999">确认后即可对服务进行评价</span>
        </template>
      </div>
      
      <!-- 工单日志 -->
      <el-divider />
      <h3>处理进度</h3>
      <el-timeline>
        <el-timeline-item
          v-for="(log, index) in logs"
          :key="index"
          :timestamp="formatTime(log.createTime)"
          placement="top"
        >
          <el-card>
            <h4>{{ getActionText(log.action) }}</h4>
            <p v-if="log.operatorId">操作人ID：{{ log.operatorId }}</p>
            <p v-if="log.content">备注：{{ log.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      
      <!-- 评价区域 -->
      <el-divider v-if="ticket.status === 'COMPLETED'" />
      <div v-if="ticket.status === 'COMPLETED'" class="evaluation-section">
        <h3>服务评价</h3>
        <div v-if="evaluation">
          <el-rate v-model="evaluation.rating" disabled show-score text-color="#ff9900" />
          <div class="evaluation-tags" v-if="evaluation.tags">
            <el-tag v-for="tag in evaluation.tags.split(',')" :key="tag" style="margin-right: 8px">
              {{ tag }}
            </el-tag>
          </div>
          <p class="evaluation-content">{{ evaluation.content }}</p>
          <p class="evaluation-time">评价时间：{{ formatTime(evaluation.createTime) }}</p>
        </div>
        <el-form v-else-if="userStore.isUser" :model="evaluationForm" label-width="100px">
          <el-form-item label="评分">
            <el-rate v-model="evaluationForm.rating" show-text />
          </el-form-item>
          <el-form-item label="评价标签">
            <el-checkbox-group v-model="evaluationForm.tags">
              <el-checkbox value="态度好">态度好</el-checkbox>
              <el-checkbox value="效率高">效率高</el-checkbox>
              <el-checkbox value="技术好">技术好</el-checkbox>
              <el-checkbox value="认真负责">认真负责</el-checkbox>
              <el-checkbox value="沟通顺畅">沟通顺畅</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
              v-model="evaluationForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入评价内容"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitEvaluation" :loading="evaluating">提交评价</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getTicket, getTicketLogs, resolveTicket, confirmTicket } from '@/api/ticket'
import { getStaffById } from '@/api/admin'
import { getEvaluationByTicketId, createEvaluation } from '@/api/evaluation'
import { useUserStore } from '@/stores/user'
import { getCategoryText, getUrgencyText, getUrgencyType, getStatusText, getStatusType, getActionText, formatTime, getLostFoundTypeText } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const ticket = ref(null)
const logs = ref([])
const staffInfo = ref(null)
const evaluation = ref(null)
const evaluating = ref(false)
const resolving = ref(false)
const confirming = ref(false)

// 上传配置
const uploadUrl = '/api/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + (localStorage.getItem('token') || '')
}))

// 处理结果表单
const resolveForm = reactive({
  content: '',
  images: []
})
const resolveImagesList = ref([])
const resolveImageUrls = ref([])

const onResolveImageSuccess = (response, uploadFile, uploadFiles) => {
  if (response.code === 200 && response.data) {
    uploadFile.url = response.data.url
    resolveImageUrls.value.push(response.data.url)
    resolveImagesList.value = [...uploadFiles]
  }
}

const onResolveImageRemove = (file) => {
  const url = file.response?.data?.url || file.url
  const idx = resolveImageUrls.value.indexOf(url)
  if (idx > -1) resolveImageUrls.value.splice(idx, 1)
}

const handleResolve = async () => {
  resolving.value = true
  try {
    await resolveTicket(ticket.value.id, {
      content: resolveForm.content,
      images: resolveImageUrls.value.join(',')
    })
    ElMessage.success('处理结果已提交')
    loadTicketDetail()
    loadLogs()
  } catch (error) {
    console.error('提交处理结果失败:', error)
  } finally {
    resolving.value = false
  }
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认工单已处理完成？确认后可进行评价。', '确认完成', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })
    confirming.value = true
    await confirmTicket(ticket.value.id)
    ElMessage.success('已确认完成，请对服务进行评价')
    loadTicketDetail()
    loadLogs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认完成失败:', error)
    }
  } finally {
    confirming.value = false
  }
}

const evaluationForm = ref({
  rating: 5,
  tags: [],
  content: ''
})

const loadTicketDetail = async () => {
  loading.value = true
  try {
    const res = await getTicket(route.params.id)
    ticket.value = res.data
    
    if (ticket.value.staffId) {
      try {
        const staffRes = await getStaffById(ticket.value.staffId)
        staffInfo.value = staffRes.data
      } catch (e) {
        console.error('加载维修员信息失败:', e)
      }
    }
  } catch (error) {
    console.error('加载工单详情失败:', error)
  } finally {
    loading.value = false
  }
}

const loadLogs = async () => {
  try {
    const res = await getTicketLogs(route.params.id)
    logs.value = res.data || []
  } catch (error) {
    console.error('加载工单日志失败:', error)
  }
}

const loadEvaluation = async () => {
  try {
    const res = await getEvaluationByTicketId(route.params.id)
    if (res.data) {
      evaluation.value = res.data
    }
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

const submitEvaluation = async () => {
  if (!evaluationForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }

  evaluating.value = true
  try {
    await createEvaluation({
      ticketId: ticket.value.id,
      staffId: ticket.value.staffId,
      rating: evaluationForm.value.rating,
      tags: evaluationForm.value.tags.join(','),
      content: evaluationForm.value.content
    })

    ElMessage.success('评价成功')
    loadEvaluation()
  } catch (error) {
    console.error('提交评价失败:', error)
  } finally {
    evaluating.value = false
  }
}

onMounted(() => {
  loadTicketDetail()
  loadLogs()
  loadEvaluation()
})
</script>

<style scoped>
.ticket-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.images-section {
  margin-bottom: 16px;
}

.action-section {
  padding: 12px 0;
}

.evaluation-section {
  margin-top: 20px;
}

.evaluation-tags {
  margin: 10px 0;
}

.evaluation-content {
  margin: 10px 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.evaluation-time {
  color: #999;
  font-size: 12px;
}
</style>
