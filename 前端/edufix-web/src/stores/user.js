import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 计算属性：是否已登录
  const isLoggedIn = computed(() => !!token.value)
  
  // 计算属性：用户角色
  const userRole = computed(() => userInfo.value.role || '')
  
  // 计算属性：是否为管理员
  const isAdmin = computed(() => userInfo.value.role === 'ADMIN')
  
  // 计算属性：是否为维修员
  const isStaff = computed(() => userInfo.value.role === 'STAFF')
  
  // 计算属性：是否为普通用户
  const isUser = computed(() => userInfo.value.role === 'STUDENT')

  // 登录
  async function login(loginForm) {
    try {
      const res = await loginApi(loginForm)
      token.value = res.data.token
      userInfo.value = res.data
      
      // 保存到localStorage
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      
      return res
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 注册
  async function register(registerForm) {
    try {
      const res = await registerApi(registerForm)
      return res
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 登出
  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 清除本地存储
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userRole,
    isAdmin,
    isStaff,
    isUser,
    login,
    register,
    logout
  }
})
