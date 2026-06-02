<template>
  <div class="manage-notices">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            发布公告
          </el-button>
        </div>
      </template>
      
      <el-table :data="notices" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop" type="danger" size="small">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.id)">查看</el-button>
            <el-button 
              v-if="row.status !== 'PUBLISHED'" 
              link 
              type="success" 
              @click="handlePublish(row.id)"
            >
              发布
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 创建公告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="发布公告"
      width="700px"
    >
      <el-form :model="noticeForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="noticeForm.isTop" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublishedNotices, createNotice, publishNotice } from '@/api/notice'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const notices = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const noticeForm = reactive({
  title: '',
  content: '',
  isTop: false
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在5-100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

const loadNotices = async () => {
  loading.value = true
  try {
    // 这里应该调用获取所有公告的API，暂时使用已发布公告
    const res = await getPublishedNotices()
    notices.value = res.data || []
  } catch (error) {
    console.error('加载公告失败:', error)
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  noticeForm.title = ''
  noticeForm.content = ''
  noticeForm.isTop = false
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    submitting.value = true
    await createNotice(noticeForm)
    
    ElMessage.success('公告创建成功')
    dialogVisible.value = false
    loadNotices()
  } catch (error) {
    console.error('创建公告失败:', error)
  } finally {
    submitting.value = false
  }
}

const handlePublish = async (id) => {
  try {
    await ElMessageBox.confirm('确定要发布该公告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await publishNotice(id)
    ElMessage.success('发布成功')
    loadNotices()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布公告失败:', error)
    }
  }
}

const viewDetail = (id) => {
  router.push(`/notices/${id}`)
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.manage-notices {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
