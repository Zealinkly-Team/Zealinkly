<template>
  <div class="volunteer-list-container">
    <div class="page-header">
      <h3>志愿者管理</h3>
      <div>
        <el-button type="primary" @click="$router.push('/manager/volunteers/create')">新增志愿者</el-button>
        <el-button @click="refreshVolunteers">刷新列表</el-button>
      </div>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.enabled" placeholder="请选择状态">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 志愿者列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>志愿者列表</span>
          <div class="header-actions">
            <span class="volunteer-count" v-if="volunteers.length > 0">
              共 {{ volunteers.length }} 条记录
            </span>
            <div class="header-buttons">
              <el-button size="small" @click="handleBulkDelete" :disabled="selectedVolunteers.length === 0">批量删除</el-button>
              <el-button size="small" @click="handleBulkImport">批量导入</el-button>
              <el-button size="small" @click="exportTemplate">导出模板</el-button>
            </div>
          </div>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="volunteers"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        empty-text="暂无志愿者记录"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="points" label="积分" width="80" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.enabled"
              @change="toggleStatus(scope.row.id, scope.row.enabled)"
              active-color="#13ce66"
              inactive-color="#ff4949"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewVolunteer(scope.row.id)">查看</el-button>
            <el-button size="small" type="primary" @click="editVolunteer(scope.row.id)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteVolunteer(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 批量导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入志愿者"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        :action="`/api/admin/volunteers/bulk-import`"
        :headers="{ 'Authorization': `Bearer ${localStorage.getItem('token')}` }"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :auto-upload="false"
        ref="upload"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-button type="primary">选择Excel文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            请上传Excel格式文件，第一行为表头：用户名、密码、真实姓名、手机号、地址、身份证号、社区卡号、纬度、经度
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="$refs.upload.submit()">导入</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, volunteerAPI } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const volunteers = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(20)
const selectedVolunteers = ref([])
const importDialogVisible = ref(false)

// 模拟测试数据
const mockVolunteers = [
  {
    id: 1,
    username: 'test123',
    realName: '张三',
    phone: '13800138000',
    address: '北京市朝阳区',
    points: 0,
    enabled: true,
    createdAt: new Date().toISOString()
  },
  {
    id: 2,
    username: 'test456',
    realName: '李四',
    phone: '13900139000',
    address: '上海市浦东新区',
    points: 10,
    enabled: true,
    createdAt: new Date().toISOString()
  }
]

const searchForm = reactive({
  username: '',
  realName: '',
  enabled: null
})

const getVolunteers = async () => {
  loading.value = true
  try {
    console.log('=== 获取志愿者列表开始 ===')
    
    // 检查 volunteerAPI 实例
    console.log('检查 volunteerAPI 实例:', {
      volunteerAPI: typeof volunteerAPI,
      getList: typeof volunteerAPI?.getList
    })
    
    // 构建参数
    const params = {
      page: page.value,
      size: size.value
    }
    
    // 只添加非空参数
    if (searchForm.username && searchForm.username.trim()) {
      params.username = searchForm.username.trim()
    }
    if (searchForm.realName && searchForm.realName.trim()) {
      params.realName = searchForm.realName.trim()
    }
    if (searchForm.enabled !== null) {
      params.enabled = searchForm.enabled
    }
    
    console.log('请求参数:', params)
    
    // 使用 volunteerAPI.getList 方法调用 API
    try {
      console.log('尝试调用 volunteerAPI.getList')
      const response = await volunteerAPI.getList(params)
      
      console.log('=== 志愿者列表响应 ===')
      console.log('响应完整内容:', response)
      console.log('响应数据:', response.data)
      
      // 检查响应数据结构
      if (response.data) {
        console.log('响应数据结构:', {
          hasContent: 'content' in response.data,
          contentIsArray: Array.isArray(response.data.content),
          contentLength: response.data.content?.length || 0
        })
        
        if (Array.isArray(response.data.content)) {
          console.log('原始数据长度:', response.data.content.length)
          console.log('原始数据:', response.data.content)
          
          // 确保所有必要字段都存在，即使后端没有返回
          volunteers.value = (response.data.content || []).map((volunteer, index) => {
            console.log(`处理第 ${index} 个志愿者:`)
            console.log('原始志愿者数据:', volunteer)
            
            // 检查所有可能的地址字段
            const possibleAddressFields = [
              'address', 'addr', 'location', 'city', 'region', 'province', 'district'
            ]
            let addressValue = ''
            
            for (const field of possibleAddressFields) {
              if (volunteer[field]) {
                addressValue = volunteer[field]
                console.log(`找到地址字段 ${field}:`, addressValue)
                break
              }
            }
            
            if (!addressValue) {
              console.log(`志愿者 ${volunteer.id} 没有地址信息`)
            }
            
            const processedVolunteer = {
              id: volunteer.id || '',
              username: volunteer.username || '',
              realName: volunteer.realName || '',
              phone: volunteer.phone || '',
              address: addressValue,
              idCardNumber: volunteer.idCardNumber || '',
              communityCardNumber: volunteer.communityCardNumber || '',
              lat: volunteer.lat || null,
              lng: volunteer.lng || null,
              points: volunteer.points || 0,
              enabled: volunteer.enabled !== false,
              createdAt: volunteer.createdAt || ''
            }
            
            console.log('处理后的志愿者数据:', processedVolunteer)
            return processedVolunteer
          })
          
          total.value = response.data.totalElements || 0
          console.log('=== 处理完成 ===')
          console.log('处理后的志愿者数量:', volunteers.value.length)
          console.log('处理后的志愿者列表:', volunteers.value)
          
          // 如果没有数据，使用模拟数据
          if (volunteers.value.length === 0) {
            ElMessage.info('当前没有志愿者记录，显示测试数据')
            volunteers.value = mockVolunteers
            total.value = mockVolunteers.length
          }
        } else {
          console.error('响应数据格式不正确: content 不是数组', response.data.content)
          ElMessage.error('获取志愿者列表失败: 响应数据格式不正确')
          // 使用模拟数据确保页面正常显示
          volunteers.value = mockVolunteers
          total.value = mockVolunteers.length
        }
      } else {
        console.error('响应数据为空:', response.data)
        ElMessage.error('获取志愿者列表失败: 响应数据为空')
        // 使用模拟数据确保页面正常显示
        volunteers.value = mockVolunteers
        total.value = mockVolunteers.length
      }
    } catch (apiError) {
      console.error('=== API 调用错误 ===')
      console.error('错误对象:', apiError)
      console.error('错误消息:', apiError.message)
      console.error('错误响应:', apiError.response)
      console.error('错误配置:', apiError.config)
      
      // 显示友好的错误信息
      let errorMessage = '获取志愿者列表失败'
      if (apiError.response) {
        errorMessage = `${errorMessage}: ${apiError.response.data?.message || `服务器错误 (${apiError.response.status})`}`
      } else if (apiError.request) {
        errorMessage = `${errorMessage}: 服务器无响应，请检查网络连接`
      } else {
        errorMessage = `${errorMessage}: ${apiError.message}`
      }
      ElMessage.error(errorMessage)
      
      // 使用模拟数据确保页面正常显示
      volunteers.value = mockVolunteers
      total.value = mockVolunteers.length
    }
  } catch (error) {
    console.error('=== 发生错误 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('错误堆栈:', error.stack)
    
    ElMessage.error(`获取志愿者列表失败: ${error.message || '未知错误'}`)
    
    // 使用模拟数据确保页面正常显示
    volunteers.value = mockVolunteers
    total.value = mockVolunteers.length
  } finally {
    loading.value = false
    console.log('=== 获取志愿者列表结束 ===')
  }
}

const refreshVolunteers = () => {
  getVolunteers()
}

const handleSearch = () => {
  page.value = 0
  getVolunteers()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.enabled = null
  page.value = 0
  getVolunteers()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  getVolunteers()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage
  getVolunteers()
}

const handleSelectionChange = (val) => {
  selectedVolunteers.value = val
}

const toggleStatus = async (id, enabled) => {
  try {
    // 直接调用 API
    const response = await api.put(`/api/admin/volunteers/${id}`, { enabled })
    if (response.code === 200) {
      ElMessage.success(enabled ? '已启用' : '已禁用')
    }
  } catch (error) {
    // 恢复原状态
    const volunteer = volunteers.value.find(v => v.id === id)
    if (volunteer) volunteer.enabled = !enabled
    ElMessage.error('操作失败，请稍后重试')
  }
}

const viewVolunteer = (id) => {
  router.push(`/manager/volunteers/${id}`)
}

const editVolunteer = (id) => {
  router.push(`/manager/volunteers/${id}/edit`)
}

const deleteVolunteer = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这位志愿者吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 直接调用 API
    const response = await api.delete(`/api/admin/volunteers/${id}`)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      getVolunteers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const handleBulkDelete = async () => {
  if (selectedVolunteers.value.length === 0) {
    ElMessage.warning('请选择要删除的志愿者')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的${selectedVolunteers.value.length}位志愿者吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const ids = selectedVolunteers.value.map(v => v.id)
    // 直接调用 API
    const response = await api.delete('/api/admin/volunteers/batch', { data: ids })
    if (response.code === 200) {
      ElMessage.success('批量删除成功')
      selectedVolunteers.value = []
      getVolunteers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败，请稍后重试')
    }
  }
}

const handleBulkImport = () => {
  importDialogVisible.value = true
}

const handleImportSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success(`导入成功：成功${response.data.successCount}条，失败${response.data.failCount}条`)
    if (response.data.failCount > 0) {
      ElMessage.warning(`失败原因：${response.data.errors.join('; ')}`)
    }
    importDialogVisible.value = false
    getVolunteers()
  } else {
    ElMessage.error(response.message || '导入失败')
  }
}

const handleImportError = () => {
  ElMessage.error('导入失败，请检查文件格式')
}

const exportTemplate = () => {
  // 生成CSV格式的模板文件
  const headers = ['用户名', '密码', '真实姓名', '手机号', '地址', '身份证号', '社区卡号', '纬度', '经度']
  const csvContent = headers.join(',') + '\n'
  
  // 创建Blob对象
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  
  // 创建下载链接
  const link = document.createElement('a')
  link.setAttribute('href', url)
  link.setAttribute('download', '志愿者模板.csv')
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  
  // 触发下载
  link.click()
  
  // 清理
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  
  ElMessage.success('模板导出成功')
}

onMounted(() => {
  getVolunteers()
})
</script>

<style scoped>
.volunteer-list-container {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.volunteer-count {
  font-size: 14px;
  color: #606266;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.list-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>