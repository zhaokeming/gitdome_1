<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>用户注册</h1>
        <p>创建您的EduFix账号</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        class="register-form"
      >
        <el-form-item>
          <div class="role-tabs">
            <div
              v-for="item in roleOptions"
              :key="item.value"
              class="role-tab"
              :class="{ active: registerForm.role === item.value }"
              @click="registerForm.role = item.value"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            prefix-icon="UserFilled"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="identifierNo">
          <el-input
            v-model="registerForm.identifierNo"
            :placeholder="identifierPlaceholder"
            prefix-icon="Postcard"
            size="large"
          />
        </el-form-item>

        <el-form-item v-if="registerForm.role === 'STAFF'" prop="specialty">
          <el-select
            v-model="registerForm.specialty"
            placeholder="请选择维修专长"
            size="large"
            style="width: 100%"
          >
            <el-option label="水电维修" value="水电维修" />
            <el-option label="公物维修" value="公物维修" />
            <el-option label="网络维修" value="网络维修" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)

const roleOptions = [
  { value: 'STUDENT', label: '学生', icon: 'School' },
  { value: 'ADMIN', label: '管理员', icon: 'Avatar' },
  { value: 'STAFF', label: '维修员', icon: 'Setting' }
]

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  role: 'STUDENT',
  identifierNo: '',
  specialty: ''
})

const identifierPlaceholder = computed(() => {
  if (registerForm.role === 'STUDENT') return '请输入学号'
  return '请输入工号'
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  identifierNo: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' }
  ],
  specialty: [
    { required: true, message: '请选择维修专长', trigger: 'change' }
  ]
}

const handleRegister = async () => {
  try {
    const valid = await registerFormRef.value.validate()
    if (!valid) return

    loading.value = true

    const { confirmPassword, ...registerData } = registerForm
    await userStore.register(registerData)

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.register-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.register-header p {
  font-size: 14px;
  color: #999;
}

.register-form {
  margin-top: 20px;
}

.role-tabs {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}

.role-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 6px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  background: #f5f7fa;
  transition: all 0.2s;
  white-space: nowrap;
  min-width: 0;
}

.role-tab:not(:last-child) {
  border-right: 1px solid #dcdfe6;
}

.role-tab:hover {
  color: #667eea;
}

.role-tab.active {
  background: #667eea;
  color: #fff;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
  margin-left: 5px;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
