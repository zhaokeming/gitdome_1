import request from '@/utils/request'

// 创建评价
export function createEvaluation(data) {
  return request({
    url: '/evaluation',
    method: 'post',
    data
  })
}

// 获取工单评价
export function getEvaluationByTicketId(ticketId) {
  return request({
    url: `/evaluation/ticket/${ticketId}`,
    method: 'get'
  })
}

// 获取员工评价列表
export function getEvaluationsByStaffId(staffId) {
  return request({
    url: `/evaluation/staff/${staffId}`,
    method: 'get'
  })
}
