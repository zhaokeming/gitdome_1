<template>
  <div class="create-ticket">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建工单</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="ticketForm"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="工单类型" prop="category">
          <el-radio-group v-model="ticketForm.category" @change="handleCategoryChange">
            <el-radio value="REPAIR">报修</el-radio>
            <el-radio value="SUGGESTION">建议</el-radio>
            <el-radio value="LOST_FOUND">失物招领</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="具体类型" prop="type">
          <el-select v-model="ticketForm.type" placeholder="请选择具体类型" style="width: 100%">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="标题" prop="title">
          <el-input v-model="ticketForm.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="ticketForm.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述您的问题"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="位置" prop="location">
          <el-input v-model="ticketForm.location" :placeholder="locationPlaceholder" />
        </el-form-item>
        
        <el-form-item label="紧急程度" prop="urgency">
          <el-radio-group v-model="ticketForm.urgency">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="NORMAL">普通</el-radio>
            <el-radio value="HIGH">高</el-radio>
            <el-radio value="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="ticketForm.contactInfo" :placeholder="contactPlaceholder" />
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="ticketForm.remark"
            type="textarea"
            :rows="3"
            placeholder="其他补充说明（选填）"
          />
        </el-form-item>
        
        <el-form-item :label="ticketForm.category === 'LOST_FOUND' ? '物品图片' : '现场照片'">
          <el-upload
            v-model:file-list="imageList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="onImageSuccess"
            :on-remove="onImageRemove"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div v-if="ticketForm.category === 'LOST_FOUND'" style="color:#999;font-size:12px">上传物品照片更有利于辨别，帮助快速找回</div>
          <div v-else style="color:#999;font-size:12px">可上传现场照片/截图，便于维修员快速定位问题</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createTicket } from '@/api/ticket'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

// 上传配置
const uploadUrl = '/api/upload'
const uploadHeaders = {
  Authorization: 'Bearer ' + (localStorage.getItem('token') || '')
}
const imageList = ref([])
const imageUrls = ref([])

const onImageSuccess = (response, uploadFile, uploadFiles) => {
  if (response.code === 200 && response.data) {
    uploadFile.url = response.data.url
    imageUrls.value.push(response.data.url)
    imageList.value = [...uploadFiles]
  }
}

const onImageRemove = (file) => {
  const url = file.response?.data?.url || file.url
  const idx = imageUrls.value.indexOf(url)
  if (idx > -1) imageUrls.value.splice(idx, 1)
}

const ticketForm = reactive({
  category: 'REPAIR',
  type: '',
  title: '',
  description: '',
  location: '',
  urgency: 'NORMAL',
  contactInfo: '',
  remark: ''
})

// 根据分类显示不同的类型选项
const typeOptions = computed(() => {
  const options = {
    REPAIR: [
      { label: '水电维修', value: '水电维修' },
      { label: '公物维修', value: '公物维修' },
      { label: '网络维修', value: '网络维修' }
    ],
    SUGGESTION: [
      { label: '服务建议', value: '服务建议' },
      { label: '设施改进', value: '设施改进' },
      { label: '管理建议', value: '管理建议' },
      { label: '其他', value: '其他' }
    ],
    LOST_FOUND: [
      { label: '丢失物品', value: 'LOST' },
      { label: '拾到物品', value: 'FOUND' }
    ]
  }
  return options[ticketForm.category] || []
})

// 位置输入框提示文字（根据分类动态变化）
const locationPlaceholder = computed(() => {
  if (ticketForm.category === 'REPAIR') return '楼栋/宿舍号'
  if (ticketForm.category === 'LOST_FOUND') return '请输入丢失/拾取地点'
  return '请输入相关地点'
})

// 联系方式输入框提示文字（报修必填，其余选填）
const contactPlaceholder = computed(() => {
  if (ticketForm.category === 'REPAIR') return '请输入联系电话或其他联系方式'
  return '请输入联系电话或其他联系方式（选填）'
})

const rules = computed(() => ({
  category: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
  type: [{ required: true, message: '请选择具体类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在5-100个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ],
  location: [{ required: true, message: '请输入' + locationPlaceholder.value, trigger: 'blur' }],
  urgency: [{ required: true, message: '请选择紧急程度', trigger: 'change' }],
  contactInfo: ticketForm.category === 'REPAIR'
    ? [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
    : []
}))

const handleCategoryChange = () => {
  ticketForm.type = ''
  imageList.value = []
  imageUrls.value = []
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    submitting.value = true
    await createTicket({
      ...ticketForm,
      images: imageUrls.value.join(',')
    })
    
    ElMessage.success('工单创建成功')
    router.push('/tickets')
  } catch (error) {
    console.error('创建工单失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  formRef.value.resetFields()
}
</script>

<style scoped>
.create-ticket {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
