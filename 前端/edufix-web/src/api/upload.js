import request from '@/utils/request'

// 上传单个文件
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 上传多个文件
export function uploadFiles(files) {
  const formData = new FormData()
  files.forEach(file => formData.append('files', file))
  return request({
    url: '/upload/batch',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
