import request from '@/utils/request'

// 获取已发布公告列表
export function getPublishedNotices() {
  return request({
    url: '/notice/published',
    method: 'get'
  })
}

// 获取公告详情
export function getNotice(id) {
  return request({
    url: `/notice/${id}`,
    method: 'get'
  })
}

// 创建公告（管理员）
export function createNotice(data) {
  return request({
    url: '/notice',
    method: 'post',
    data
  })
}

// 发布公告（管理员）
export function publishNotice(id) {
  return request({
    url: `/notice/${id}/publish`,
    method: 'put'
  })
}
