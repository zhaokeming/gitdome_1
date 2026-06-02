/**
 * 工单分类映射
 */
export const CATEGORY_MAP = {
  REPAIR: '报修',
  SUGGESTION: '建议',
  LOST_FOUND: '失物招领'
}

export function getCategoryText(category) {
  return CATEGORY_MAP[category] || category || '-'
}

/**
 * 工单状态映射（通用/失物招领默认）
 */
export const STATUS_MAP = {
  PENDING: '待处理',
  ASSIGNED: '已派单',
  IN_PROGRESS: '处理中',
  RESOLVED: '已处理待确认',
  COMPLETED: '已完成已评价',
  CANCELLED: '已取消',
  PROCESSED: '已处理'
}

/**
 * 维修工单状态映射
 */
const REPAIR_STATUS_MAP = {
  PENDING: '待派单',
  ASSIGNED: '待接单',
  IN_PROGRESS: '处理中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

/**
 * 建议工单状态映射
 */
const SUGGESTION_STATUS_MAP = {
  PENDING: '未处理',
  PROCESSED: '已处理',
  CANCELLED: '已取消'
}

export function getStatusText(status, category) {
  if (category === 'REPAIR' && REPAIR_STATUS_MAP[status]) {
    return REPAIR_STATUS_MAP[status]
  }
  if (category === 'SUGGESTION' && SUGGESTION_STATUS_MAP[status]) {
    return SUGGESTION_STATUS_MAP[status]
  }
  return STATUS_MAP[status] || status || '-'
}

export function getStatusType(status) {
  const map = {
    PENDING: 'warning',
    ASSIGNED: 'info',
    IN_PROGRESS: 'primary',
    RESOLVED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'danger',
    PROCESSED: 'success'
  }
  return map[status] || 'info'
}

/**
 * 紧急程度映射
 */
export function getUrgencyText(urgency) {
  const map = {
    LOW: '低',
    NORMAL: '普通',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[urgency] || urgency || '-'
}

export function getUrgencyType(urgency) {
  const map = {
    LOW: 'info',
    NORMAL: 'info',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return map[urgency] || 'info'
}

/**
 * 工单操作日志映射
 */
export function getActionText(action) {
  const map = {
    CREATE: '创建工单',
    ASSIGN: '派单',
    ACCEPT: '接单',
    PROCESS: '处理',
    RESOLVE: '提交处理结果',
    CONFIRM: '确认完成',
    COMPLETE: '完成',
    CANCEL: '取消',
    EVALUATE: '评价'
  }
  return map[action] || action || '-'
}

/**
 * 失物招领类型映射
 */
export const LOST_FOUND_TYPE_MAP = {
  LOST: '丢失物品',
  FOUND: '拾到物品'
}

export function getLostFoundTypeText(type) {
  return LOST_FOUND_TYPE_MAP[type] || type || '-'
}

/**
 * 时间格式化
 */
export function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

/**
 * 相对时间格式化
 */
export function formatRelativeTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN')
}
