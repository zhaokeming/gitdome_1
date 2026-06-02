<template>
  <div class="notice-list">
    <el-card>
      <template #header>
        <span>公告列表</span>
      </template>
      
      <el-empty v-if="notices.length === 0" description="暂无公告" />
      
      <div v-else class="notice-container">
        <el-card
          v-for="notice in notices"
          :key="notice.id"
          class="notice-card"
          shadow="hover"
          @click="viewDetail(notice.id)"
        >
          <div class="notice-item">
            <div class="notice-header">
              <h3 class="notice-title">{{ notice.title }}</h3>
              <el-tag v-if="notice.isTop" type="danger" size="small">置顶</el-tag>
            </div>
            <p class="notice-summary">{{ notice.content?.substring(0, 150) }}...</p>
            <div class="notice-footer">
              <span class="notice-publisher">发布人：{{ notice.publisherName || '管理员' }}</span>
              <span class="notice-time">{{ formatTime(notice.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublishedNotices } from '@/api/notice'
import { ElMessage } from 'element-plus'

const router = useRouter()
const notices = ref([])

const loadNotices = async () => {
  try {
    const res = await getPublishedNotices()
    notices.value = res.data || []
  } catch (error) {
    console.error('加载公告失败:', error)
    ElMessage.error('加载公告失败')
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
.notice-list {
  padding: 20px;
}

.notice-container {
  display: grid;
  gap: 15px;
}

.notice-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.notice-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notice-item {
  padding: 10px;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notice-title {
  margin: 0;
  font-size: 18px;
  color: #333;
  flex: 1;
}

.notice-summary {
  margin: 10px 0;
  color: #666;
  line-height: 1.6;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.notice-publisher {
  font-size: 14px;
  color: #999;
}

.notice-time {
  font-size: 14px;
  color: #999;
}
</style>
