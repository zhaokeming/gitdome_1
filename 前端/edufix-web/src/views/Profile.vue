<template>
  <div class="profile-page">
    <el-card class="profile-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">个人信息</span>
          <el-button :type="isEditing ? 'default' : 'primary'" size="small" @click="toggleEdit">
            {{ isEditing ? '取消' : '编辑' }}
          </el-button>
        </div>
      </template>

      <div v-loading="loading" class="profile-body">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-avatar :size="80" :src="avatarUrl">
            {{ form.realName?.charAt(0) || 'U' }}
          </el-avatar>
          <div v-if="isEditing" class="avatar-upload-area">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
            >
              <el-button size="small" :loading="uploading">
                {{ uploading ? '上传中...' : '更换头像' }}
              </el-button>
            </el-upload>
            <span class="upload-hint">支持 JPG/PNG/GIF，大小不超过 5MB</span>
          </div>
        </div>

        <!-- 信息展示/编辑区域 -->
        <el-form
          :model="form"
          label-width="100px"
          label-position="right"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <span class="form-value">{{ form.username }}</span>
          </el-form-item>

          <el-form-item label="姓名">
            <el-input v-if="isEditing" v-model="form.realName" placeholder="请输入姓名" />
            <span v-else class="form-value">{{ form.realName || '未设置' }}</span>
          </el-form-item>

          <el-form-item label="学号/工号">
            <span class="form-value">{{ form.identifierNo || '未设置' }}</span>
          </el-form-item>

          <el-form-item label="手机号">
            <el-input v-if="isEditing" v-model="form.phone" placeholder="请输入手机号" />
            <span v-else class="form-value">{{ form.phone || '未设置' }}</span>
          </el-form-item>

          <el-form-item label="邮箱">
            <el-input v-if="isEditing" v-model="form.email" placeholder="请输入邮箱" />
            <span v-else class="form-value">{{ form.email || '未设置' }}</span>
          </el-form-item>

          <el-form-item label="角色">
            <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag>
          </el-form-item>

          <el-form-item v-if="form.role === 'STAFF'" label="维修专职">
            <el-tag type="warning" size="small">{{ form.specialty || '未设置' }}</el-tag>
          </el-form-item>

          <el-form-item v-if="isEditing" class="form-actions">
            <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
            <el-button @click="toggleEdit">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/auth'
import { uploadFile } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const BASE_URL = 'http://localhost:8081'
const MAX_AVATAR_SIZE = 5 * 1024 * 1024 // 5MB

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const isEditing = ref(false)

const form = reactive({
  username: '',
  realName: '',
  identifierNo: '',
  phone: '',
  email: '',
  role: '',
  specialty: '',
  avatar: ''
})

const avatarUrl = computed(() => {
  if (!form.avatar) return ''
  // 如果是完整URL则直接返回，否则拼接后端地址
  if (form.avatar.startsWith('http')) return form.avatar
  return BASE_URL + form.avatar
})

const roleText = computed(() => {
  const roleMap = { STUDENT: '学生', STAFF: '维修员', ADMIN: '管理员' }
  return roleMap[form.role] || form.role || '未知'
})

const roleTagType = computed(() => {
  const typeMap = { STUDENT: 'success', STAFF: 'warning', ADMIN: 'danger' }
  return typeMap[form.role] || 'info'
})

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await getProfile()
    const data = res.data
    Object.assign(form, {
      username: data.username || '',
      realName: data.realName || '',
      identifierNo: data.identifierNo || '',
      phone: data.phone || '',
      email: data.email || '',
      role: data.role || '',
      specialty: data.specialty || '',
      avatar: data.avatar || ''
    })
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  } finally {
    loading.value = false
  }
}

const beforeAvatarUpload = (file) => {
  // 校验文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('头像仅支持 JPG、PNG、GIF、BMP、WebP 格式')
    return false
  }
  // 校验文件大小（不超过 5MB）
  if (file.size > MAX_AVATAR_SIZE) {
    ElMessage.error('头像图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options) => {
  uploading.value = true
  try {
    const res = await uploadFile(options.file)
    const uploadedUrl = res.data?.url || ''
    if (uploadedUrl) {
      form.avatar = uploadedUrl
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

const toggleEdit = () => {
  if (isEditing.value) {
    // 取消编辑，重新加载数据
    loadProfile()
  }
  isEditing.value = !isEditing.value
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateProfile({
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar
    })
    ElMessage.success('个人信息更新成功')
    // 同步更新 store 中的用户信息
    userStore.userInfo.realName = form.realName
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    isEditing.value = false
    loadProfile()
  } catch (error) {
    ElMessage.error('更新个人信息失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  max-width: 640px;
  margin: 0 auto;
}

.profile-card {
  background-color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.profile-body {
  padding: 0 20px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 12px;
  gap: 6px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.profile-form {
  margin-top: 10px;
}

.form-value {
  color: #606266;
  font-size: 14px;
}

.form-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.form-actions :deep(.el-form-item__content) {
  display: flex;
  gap: 12px;
}
</style>
