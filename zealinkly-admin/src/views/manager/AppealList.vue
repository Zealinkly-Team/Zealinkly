<template>
  <div class="appeal-list-container">
    <div class="page-header">
      <h3>申诉管理</h3>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="申诉人姓名">
          <el-input v-model="searchForm.appellantName" placeholder="请输入申诉人姓名" />
        </el-form-item>
        <el-form-item label="申诉类型">
          <el-select v-model="searchForm.type" placeholder="请选择申诉类型">
            <el-option label="任务申诉" :value="'TASK'" />
            <el-option label="积分申诉" :value="'POINT'" />
            <el-option label="兑换申诉" :value="'EXCHANGE'" />
            <el-option label="其他申诉" :value="'OTHER'" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="待处理" :value="'PENDING'" />
            <el-option label="处理中" :value="'PROCESSING'" />
            <el-option label="已解决" :value="'RESOLVED'" />
            <el-option label="已驳回" :value="'REJECTED'" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 申诉列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>申诉记录</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="appeals"
        style="width: 100%"
      >
        <el-table-column prop="id" label="申诉ID" width="80" />
        <el-table-column prop="appellantName" label="申诉人姓名" width="120" />
        <el-table-column prop="appellantType" label="申诉人类型" width="100">
          <template #default="scope">
            <span>{{ scope.row.appellantType === 'ELDER' ? '老人' : '志愿者' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="申诉类型" width="120">
          <template #default="scope">
            <span>{{ getTypeText(scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="申诉标题" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申诉时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewAppeal(scope.row.id)">查看</el-button>
            <el-button size="small" type="primary" @click="processAppeal(scope.row.id)" v-if="scope.row.status === 'PENDING'">
              处理
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const appeals = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(20)

const searchForm = reactive({
  appellantName: '',
  type: '',
  status: ''
})

const getAppeals = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('page', page.value)
    params.append('size', size.value)
    if (searchForm.appellantName) params.append('appellantName', searchForm.appellantName)
    if (searchForm.type) params.append('type', searchForm.type)
    if (searchForm.status) params.append('status', searchForm.status)
    
    const response = await fetch(`/api/admin/appeals?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      appeals.value = data.data.content || []
      total.value = data.data.totalElements || 0
    } else {
      ElMessage.error(data.message || '获取申诉列表失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 0
  getAppeals()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  page.value = 0
  getAppeals()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  getAppeals()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage
  getAppeals()
}

const getTypeText = (type) => {
  const typeMap = {
    TASK: '任务申诉',
    POINT: '积分申诉',
    EXCHANGE: '兑换申诉',
    OTHER: '其他申诉'
  }
  return typeMap[type] || type
}

const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    RESOLVED: 'success',
    REJECTED: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    REJECTED: '已驳回'
  }
  return statusMap[status] || status
}

const viewAppeal = (id) => {
  router.push(`/manager/appeals/${id}`)
}

const processAppeal = (id) => {
  router.push(`/manager/appeals/${id}/process`)
}

onMounted(() => {
  getAppeals()
})
</script>

<style scoped>
.appeal-list-container {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.list-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>