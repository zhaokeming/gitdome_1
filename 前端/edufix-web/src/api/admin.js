import request from '@/utils/request'

// 派单
export function assignTicket(data) {
  return request({
    url: '/admin/assign',
    method: 'post',
    data
  })
}

// 获取待处理工单
export function getPendingTickets() {
  return request({
    url: '/admin/pending-tickets',
    method: 'get'
  })
}

// 获取高优先级工单
export function getHighPriorityTickets(topN = 10) {
  return request({
    url: '/admin/high-priority-tickets',
    method: 'get',
    params: { topN }
  })
}

// 获取员工列表
export function getStaffList() {
  return request({
    url: '/admin/staff/list',
    method: 'get'
  })
}

// 撤销派单
export function revokeAssignment(ticketId) {
  return request({
    url: '/admin/revoke-assignment',
    method: 'post',
    data: { ticketId }
  })
}

// 获取员工详情
export function getStaffById(id) {
  return request({
    url: `/staff/${id}`,
    method: 'get'
  })
}
