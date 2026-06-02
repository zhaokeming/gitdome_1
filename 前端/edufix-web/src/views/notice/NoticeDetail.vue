<template>
  <div class="notice-detail" v-loading="loading">
    <el-card v-if="notice">
      <template #header>
        <div class="card-header">
          <span>公告详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <div class="notice-content">
        <h1 class="notice-title">{{ notice.title }}</h1>
        
        <div class="notice-meta">
          <span>发布人：{{ notice.publisherName || '管理员' }}</span>
          <span>发布时间：{{ formatTime(notice.createTime) }}</span>
          <span v-if="notice.updateTime !== notice.createTime">
            更新时间：{{ formatTime(notice.updateTime) }}
          </span>
        </div>
        
        <el-divider />
        
        <div class="notice-body" v-html="notice.content"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getNotice } from '@/api/notice'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const notice = ref(null)

const loadNotice = async () => {
  loading.value = true
  try {
    const res = await getNotice(route.params.id)
    notice.value = res.data
  } catch (error) {
    console.error('加载公告失败:', error)
    ElMessage.error('加载公告失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadNotice()
})
</script>

<style scoped>
.notice-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-content {
  max-width: 900px;
  margin: 0 auto;
}

.notice-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

.notice-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: #999;
  font-size: 14px;
  flex-wrap: wrap;
}

.notice-body {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  min-height: 200px;
}

.notice-body :deep(p) {
  margin: 10px 0;
}

.notice-body :deep(img) {
  max-width: 100%;
  height: auto;
}
</style>
