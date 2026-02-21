<template>
  <div class="notice-list-container">
    <div class="page-header">
      <h3>通知管理</h3>
      <el-button type="primary" @click="$router.push('/manager/notices/create')">新增通知</el-button>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="通知标题">
          <el-input v-model="searchForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="searchForm.type" placeholder="请选择通知类型">
            <el-option label="系统通知" :value="'SYSTEM'" />
            <el-option label="任务通知" :value="'TASK'" />
            <el-option label="积分通知" :value="'POINT'" />
            <el-option label="活动通知" :value="'ACTIVITY'" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 通知列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>通知列表</span>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedNoticeIds.length === 0">
            批量删除
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="notices"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="通知ID" width="80" />
        <el-table-column prop="title" label="通知标题" />
        <el-table-column prop="type" label="通知类型" width="120">
          <template #default="scope">
            <span>{{ getTypeText(scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="120">
          <template #default="scope">
            <span>{{ getTargetTypeText(scope.row.targetType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewNotice(scope.row.id)">查看</el-button>
            <el-button size="small" type="primary" @click="editNotice(scope.row.id)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteNotice(scope.row.id)">删除</el-button>
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
const notices = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(20)
const selectedNoticeIds = ref([])

const searchForm = reactive({
  title: '',
  type: ''
})

const getNotices = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('page', page.value)
    params.append('size', size.value)
    if (searchForm.title) params.append('title', searchForm.title)
    if (searchForm.type) params.append('type', searchForm.type)
    
    const response = await fetch(`/api/admin/notices?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      notices.value = data.data.content || []
      total.value = data.data.totalElements || 0
    } else {
      ElMessage.error(data.message || '获取通知列表失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 0
  getNotices()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  page.value = 0
  getNotices()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  getNotices()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage
  getNotices()
}

const handleSelectionChange = (selection) => {
  selectedNoticeIds.value = selection.map(item => item.id)
}

const getTypeText = (type) => {
  const typeMap = {
    SYSTEM: '系统通知',
    TASK: '任务通知',
    POINT: '积分通知',
    ACTIVITY: '活动通知'
  }
  return typeMap[type] || type
}

const getTargetTypeText = (targetType) => {
  const targetTypeMap = {
    ALL: '全体用户',
    ELDER: '老人用户',
    VOLUNTEER: '志愿者用户'
  }
  return targetTypeMap[targetType] || targetType
}

const viewNotice = (id) => {
  router.push(`/manager/notices/${id}`)
}

const editNotice = (id) => {
  router.push(`/manager/notices/${id}/edit`)
}

const deleteNotice = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个通知吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/notices/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('通知已删除')
      getNotices()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedNoticeIds.value.length === 0) {
    ElMessage.warning('请选择要删除的通知')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedNoticeIds.value.length} 个通知吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch('/api/admin/notices/batch', {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(selectedNoticeIds.value)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('通知已批量删除')
      selectedNoticeIds.value = []
      getNotices()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

onMounted(() => {
  getNotices()
})
</script>

<style scoped>
.notice-list-container {
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