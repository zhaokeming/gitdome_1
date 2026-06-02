import request from '@/utils/request'

// 创建工单
export function createTicket(data) {
  return request({
    url: '/ticket',
    method: 'post',
    data
  })
}

// 获取工单详情
export function getTicket(id) {
  return request({
    url: `/ticket/${id}`,
    method: 'get'
  })
}

// 获取工单列表
export function getTicketList(params) {
  return request({
    url: '/ticket/list',
    method: 'get',
    params
  })
}

// 获取工单日志
export function getTicketLogs(id) {
  return request({
    url: `/ticket/${id}/logs`,
    method: 'get'
  })
}

// 删除工单
export function deleteTicket(id) {
  return request({
    url: `/ticket/${id}/delete`,
    method: 'post'
  })
}

// 取消工单
export function cancelTicket(id, remark) {
  return request({
    url: `/ticket/${id}/cancel`,
    method: 'put',
    params: { remark }
  })
}

// 维修员提交处理结果
export function resolveTicket(id, data) {
  return request({
    url: `/ticket/${id}/resolve`,
    method: 'put',
    data
  })
}

// 用户确认完成
export function confirmTicket(id) {
  return request({
    url: `/ticket/${id}/confirm`,
    method: 'put'
  })
}

// 提供线索（失物招领）
export function provideClue(ticketId, data) {
  return request({
    url: `/ticket/${ticketId}/clue`,
    method: 'post',
    data
  })
}

// 获取公开失物招领列表（所有用户可见）
export function getLostFoundList(params) {
  return request({
    url: '/ticket/lost-found',
    method: 'get',
    params
  })
}
