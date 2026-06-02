import request from '@/utils/request'

// 获取我的工单列表
export function getMyTickets(params) {
  return request({
    url: '/staff/my-tickets',
    method: 'get',
    params
  })
}

// 获取工单统计数据
export function getMyTicketStats() {
  return request({
    url: '/staff/my-tickets/stats',
    method: 'get'
  })
}

// 接单
export function acceptTicket(ticketId) {
  return request({
    url: `/staff/${ticketId}/accept`,
    method: 'post'
  })
}

// 完成工单
export function completeTicket(ticketId, remark) {
  return request({
    url: `/staff/${ticketId}/complete`,
    method: 'post',
    params: { remark }
  })
}
