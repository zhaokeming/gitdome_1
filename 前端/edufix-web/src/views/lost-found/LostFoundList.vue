<template>
  <div class="lost-found-page">
    <!-- 顶部操作区 -->
    <el-card class="header-card">
      <div class="page-header">
        <div class="header-title">
          <el-icon :size="28" color="#409EFF"><Search /></el-icon>
          <h2>失物招领</h2>
        </div>
        <el-button type="primary" @click="router.push('/tickets/create?category=LOST_FOUND')">
          <el-icon><Plus /></el-icon>
          发布招领
        </el-button>
      </div>

      <!-- 搜索 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 140px" @change="loadData">
            <el-option label="全部" value="" />
            <el-option label="丢失物品" value="LOST" />
            <el-option label="拾到物品" value="FOUND" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索物品名称/描述/地点"
            clearable
            style="width: 320px"
            @keyup.enter="loadData"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 卡片列表 -->
    <div v-loading="loading" class="card-grid">
      <el-empty v-if="!loading && list.length === 0" description="暂无失物招领信息" :image-size="120" />

      <el-card
        v-for="item in list"
        :key="item.id"
        class="item-card"
        shadow="hover"
        @click="viewDetail(item.id)"
      >
        <!-- 图片区域 -->
        <div class="card-image" @click.stop>
          <el-image
            v-if="item.images"
            :src="item.images.split(',')[0]"
            :preview-src-list="item.images.split(',')"
            fit="cover"
            class="cover-image"
            :preview-teleported="true"
          >
            <template #error>
              <div class="image-placeholder" @click="viewDetail(item.id)">
                <el-icon :size="40" color="#c0c4cc"><Box /></el-icon>
              </div>
            </template>
          </el-image>
          <div v-else class="image-placeholder" @click="viewDetail(item.id)">
            <el-icon :size="40" color="#c0c4cc"><Box /></el-icon>
          </div>
          <!-- 多图指示器 -->
          <div v-if="item.images && item.images.split(',').length > 1" class="image-counter">
            <el-icon :size="12"><Picture /></el-icon>
            {{ item.images.split(',').length }}
          </div>
          <!-- 失物招领类型标签 -->
          <el-tag
            :type="item.type === 'LOST' ? 'danger' : 'success'"
            class="card-type-tag"
            size="small"
            effect="dark"
          >
            {{ getLostFoundTypeText(item.type) || '未分类' }}
          </el-tag>
          <!-- 状态标签 -->
          <el-tag
            :type="getStatusType(item.status)"
            class="card-status-tag"
            size="small"
            effect="dark"
          >
            {{ getStatusText(item.status) }}
          </el-tag>
        </div>

        <!-- 信息区域 -->
        <div class="card-info">
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-desc">{{ item.description }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Location /></el-icon>
              {{ item.location || '未标注' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatRelativeTime(item.createTime) }}
            </span>
          </div>
          <div class="card-footer">
            <el-tag size="small" :type="getUrgencyType(item.urgency)">
              {{ getUrgencyText(item.urgency) }}
            </el-tag>
            <span v-if="item.contactInfo" class="contact-hint">
              <el-icon><Phone /></el-icon>
              有联系方式
            </span>
            <el-button
              size="small"
              type="primary"
              link
              @click.stop="openClueDialog(item)"
            >
              <el-icon><ChatLineRound /></el-icon>
              提供线索
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="pagination.total > 0"
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[12, 24, 48]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
      class="pagination"
    />

    <!-- 提供线索对话框 -->
    <el-dialog v-model="clueDialogVisible" title="提供线索" width="480px" :close-on-click-modal="false">
      <el-form :model="clueForm" label-width="80px">
        <el-form-item label="目标物品">
          <span class="clue-target">{{ clueTarget?.title }}</span>
        </el-form-item>
        <el-form-item label="线索内容" prop="message">
          <el-input
            v-model="clueForm.message"
            type="textarea"
            :rows="4"
            placeholder="请描述您了解的线索信息，例如：在哪里见过这个物品、可能的主人是谁等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clueDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="clueSubmitting" @click="submitClue">提交线索</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getLostFoundList, provideClue } from '@/api/ticket'
import { getStatusText, getStatusType, getUrgencyText, getUrgencyType, formatRelativeTime, getLostFoundTypeText } from '@/utils/constants'
import { ElMessage } from 'element-plus'
import { Search, Plus, Box, Location, Clock, Phone, Picture, ChatLineRound } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const list = ref([])

const searchForm = reactive({
  keyword: '',
  type: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 线索对话框
const clueDialogVisible = ref(false)
const clueSubmitting = ref(false)
const clueTarget = ref(null)
const clueForm = reactive({
  message: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      type: searchForm.type || undefined
    }
    const res = await getLostFoundList(params)
    list.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载失物招领列表失败:', error)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.type = ''
  pagination.pageNum = 1
  loadData()
}

const viewDetail = (id) => {
  router.push(`/tickets/${id}`)
}

const openClueDialog = (item) => {
  clueTarget.value = item
  clueForm.message = ''
  clueDialogVisible.value = true
}

const submitClue = async () => {
  if (!clueForm.message.trim()) {
    ElMessage.warning('请输入线索内容')
    return
  }
  clueSubmitting.value = true
  try {
    await provideClue(clueTarget.value.id, { message: clueForm.message })
    ElMessage.success('线索已提供，感谢您的帮助！')
    clueDialogVisible.value = false
  } catch (error) {
    console.error('提交线索失败:', error)
  } finally {
    clueSubmitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.lost-found-page {
  padding: 0;
}

.header-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.search-form {
  margin-bottom: 12px;
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.item-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 8px;
  overflow: hidden;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.item-card :deep(.el-card__body) {
  padding: 0;
}

/* 图片区域 */
.card-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.image-counter {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 3px;
}

.card-type-tag {
  position: absolute;
  top: 10px;
  left: 10px;
}

.card-status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

/* 信息区域 */
.card-info {
  padding: 14px 16px;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.card-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contact-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #67c23a;
}

.clue-target {
  font-weight: 600;
  color: #303133;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  justify-content: center;
}
</style>
