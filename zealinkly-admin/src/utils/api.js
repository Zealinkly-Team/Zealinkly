import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// API 服务器地址
export const API_BASE_URL = '/api'

// 创建axios实例
export const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
api.interceptors.request.use(
    config => {
        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
api.interceptors.response.use(
    response => {
        const res = response.data

        // 统一处理响应
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }

        return res
    },
    error => {
        // 统一处理错误
        let message = '网络错误，请稍后重试'

        if (error.response) {
            const status = error.response.status
            switch (status) {
                case 401:
                    message = '未认证，请重新登录'
                    // 跳转到登录页
                    localStorage.removeItem('token')
                    window.location.href = '/login'
                    break
                case 403:
                    message = '无权限操作'
                    break
                case 404:
                    message = '资源不存在'
                    break
                case 500:
                    message = '服务器内部错误'
                    break
                default:
                    message = error.response.data?.message || message
            }
        }

        ElMessage.error(message)
        return Promise.reject(error)
    }
)

// 导出API方法
export const authAPI = {
    // 登录
    login: (data) => api.post('/auth/login', data),
    // 注册
    register: (data) => api.post('/auth/register/admin', data)
}

export const elderAPI = {
    // 获取老人列表
    getList: (params) => api.get('/admin/elders', { params }),
    // 获取老人详情
    getDetail: (id) => api.get(`/admin/elders/${id}`),
    // 创建老人
    create: (data) => api.post('/admin/elders', data),
    // 更新老人
    update: (id, data) => api.put(`/admin/elders/${id}`, data),
    // 删除老人
    delete: (id) => api.delete(`/admin/elders/${id}`),
    // 批量删除老人
    bulkDelete: (ids) => api.post('/admin/elders/bulk-delete', ids),
    // 批量导入老人
    bulkImport: (formData) => api.post('/admin/elders/bulk-import', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    }),
    // 启用老人
    enable: (id) => api.patch(`/admin/elders/${id}/enable`),
    // 禁用老人
    disable: (id) => api.patch(`/admin/elders/${id}/disable`),
    // 发放积分
    grantPoints: (id, data) => api.post(`/admin/elders/${id}/grant-points`, data)
}

export const volunteerAPI = {
    // 获取志愿者列表
    getList: (params) => api.get('/admin/volunteers', { params }),
    // 获取志愿者详情
    getDetail: (id) => api.get(`/admin/volunteers/${id}`),
    // 创建志愿者
    create: (data) => api.post('/admin/volunteers', data),
    // 更新志愿者
    update: (id, data) => api.put(`/admin/volunteers/${id}`, data),
    // 删除志愿者
    delete: (id) => api.delete(`/admin/volunteers/${id}`),
    // 批量删除志愿者
    bulkDelete: (ids) => api.post('/admin/volunteers/bulk-delete', ids),
    // 批量导入志愿者
    bulkImport: (formData) => api.post('/admin/volunteers/bulk-import', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    }),
    // 启用志愿者
    enable: (id) => api.patch(`/admin/volunteers/${id}/enable`),
    // 禁用志愿者
    disable: (id) => api.patch(`/admin/volunteers/${id}/disable`),
    // 发放积分
    grantPoints: (id, data) => api.post(`/admin/volunteers/${id}/grant-points`, data)
}

export const taskAPI = {
    // 获取任务列表
    getList: (params) => api.get('/admin/tasks', { params }),
    // 获取任务详情
    getDetail: (id) => api.get(`/admin/tasks/${id}`),
    // 更新任务
    update: (id, data) => api.put(`/admin/tasks/${id}`, data)
}

export const emergencyAPI = {
    // 获取待处理报警列表
    getPendingList: () => api.get('/emergency/pending'),
    // 获取紧急报警详情
    getDetail: (id) => api.get(`/emergency/${id}/detail`),
    // 开始处理紧急报警
    start: (id) => api.patch(`/emergency/${id}/start`),
    // 完成处理紧急报警
    complete: (id, data) => api.patch(`/emergency/${id}/complete`, data)
}

export const productAPI = {
    // 获取商品列表
    getList: (params) => api.get('/admin/products', { params }),
    // 获取已启用商品列表
    getEnabledList: (params) => api.get('/admin/products/enabled', { params }),
    // 获取商品详情
    getDetail: (id) => api.get(`/admin/products/${id}`),
    // 创建商品
    create: (data) => api.post('/admin/products', data),
    // 更新商品
    update: (id, data) => api.put(`/admin/products/${id}`, data),
    // 删除商品
    delete: (id) => api.delete(`/admin/products/${id}`)
}

export const exchangeAPI = {
    // 兑换商品
    exchange: (data) => api.post('/admin/exchanges/exchange', data),
    // 获取兑换记录列表
    getList: (params) => api.get('/admin/exchanges', { params }),
    // 获取兑换记录详情
    getDetail: (id) => api.get(`/admin/exchanges/${id}`),
    // 扫描卡片
    scanCard: (data) => api.post('/admin/exchanges/scan-card', data)
}

export const appealAPI = {
    // 获取申诉列表
    getList: (params) => api.get('/admin/appeals', { params }),
    // 获取待处理申诉列表
    getPendingList: () => api.get('/admin/appeals/pending'),
    // 获取申诉详情
    getDetail: (id) => api.get(`/admin/appeals/${id}`),
    // 处理申诉
    resolve: (id, data) => api.patch(`/admin/appeals/${id}/resolve`, data)
}

export const notificationAPI = {
    // 广播通知
    broadcast: (data) => api.post('/admin/notifications/broadcast', data)
}

export const emergencyContactAPI = {
    // 添加紧急联系人
    add: (elderId, data) => api.post(`/admin/elders/${elderId}/emergency-contacts`, data),
    // 获取紧急联系人列表
    getList: (elderId) => api.get(`/admin/elders/${elderId}/emergency-contacts`),
    // 更新紧急联系人
    update: (elderId, contactId, data) => api.put(`/admin/elders/${elderId}/emergency-contacts/${contactId}`, data),
    // 删除紧急联系人
    delete: (elderId, contactId) => api.delete(`/admin/elders/${elderId}/emergency-contacts/${contactId}`)
}

export const pointsAPI = {
    // 获取用户积分总数
    getTotal: (userType, userId) => api.get(`/admin/points/users/${userType}/${userId}/total`),
    // 获取用户积分流水
    getHistory: (userType, userId, params) => api.get(`/admin/points/users/${userType}/${userId}/history`, { params })
}

export const ocrAPI = {
    // 识别卡片号码
    recognize: (data) => api.post('/admin/ocr/recognize', data)
}

// 默认导出，保持向后兼容
export default api