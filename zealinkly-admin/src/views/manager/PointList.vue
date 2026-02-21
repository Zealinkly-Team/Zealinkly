<template>
  <div class="point-list-container">
    <div class="page-header">
      <h3>积分管理</h3>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="用户姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入用户姓名" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="searchForm.userType" placeholder="请选择用户类型">
            <el-option label="老人" :value="'ELDER'" />
            <el-option label="志愿者" :value="'VOLUNTEER'" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分类型">
          <el-select v-model="searchForm.type" placeholder="请选择积分类型">
            <el-option label="任务奖励" :value="'TASK_REWARD'" />
            <el-option label="签到奖励" :value="'CHECKIN_REWARD'" />
            <el-option label="兑换扣除" :value="'EXCHANGE_DEDUCTION'" />
            <el-option label="手动调整" :value="'MANUAL_ADJUSTMENT'" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 积分列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>积分记录</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="points"
        style="width: 100%"
      >
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="userName" label="用户姓名" width="120" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="scope">
            <span>{{ scope.row.userType === 'ELDER' ? '老人' : '志愿者' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="积分类型" width="120">
          <template #default="scope">
            <span>{{ getTypeText(scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="积分数量" width="100">
          <template #default="scope">
            <span :class="{ 'text-success': scope.row.amount > 0, 'text-danger': scope.row.amount < 0 }">
              {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="currentPoints" label="当前积分" width="100" />
        <el-table-column prop="description" label="积分描述" />
        <el-table-column prop="createdAt" label="记录时间" width="180" />
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
    
    <!-- 积分调整 -->
    <el-card class="adjust-card">
      <template #header>
        <span>积分调整</span>
      </template>
      <el-form
        :model="adjustForm"
        :rules="adjustRules"
        ref="adjustFormRef"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="adjustForm.userType" placeholder="请选择用户类型">
            <el-option label="老人" :value="'ELDER'" />
            <el-option label="志愿者" :value="'VOLUNTEER'" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="adjustForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="调整类型" prop="adjustType">
          <el-select v-model="adjustForm.adjustType" placeholder="请选择调整类型">
            <el-option label="增加积分" :value="'INCREASE'" />
            <el-option label="减少积分" :value="'DECREASE'" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整数量" prop="amount">
          <el-input-number v-model="adjustForm.amount" :min="1" :step="1" placeholder="请输入调整数量" />
        </el-form-item>
        <el-form-item label="调整原因" prop="description">
          <el-input
            v-model="adjustForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="adjustPoints" :loading="adjustLoading">
            执行调整
          </el-button>
          <el-button @click="resetAdjustForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const adjustLoading = ref(false)
const points = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(20)

const searchForm = reactive({
  userName: '',
  userType: '',
  type: ''
})

const adjustForm = reactive({
  userType: 'ELDER',
  userId: '',
  adjustType: 'INCREASE',
  amount: 1,
  description: ''
})

const adjustRules = {
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ],
  adjustType: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入调整数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '调整数量必须大于 0', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入调整原因', trigger: 'blur' }
  ]
}

const adjustFormRef = ref(null)

const getPoints = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('page', page.value)
    params.append('size', size.value)
    if (searchForm.userName) params.append('userName', searchForm.userName)
    if (searchForm.userType) params.append('userType', searchForm.userType)
    if (searchForm.type) params.append('type', searchForm.type)
    
    const response = await fetch(`/api/admin/points?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      points.value = data.data.content || []
      total.value = data.data.totalElements || 0
    } else {
      ElMessage.error(data.message || '获取积分记录失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 0
  getPoints()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  page.value = 0
  getPoints()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  getPoints()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage
  getPoints()
}

const getTypeText = (type) => {
  const typeMap = {
    TASK_REWARD: '任务奖励',
    CHECKIN_REWARD: '签到奖励',
    EXCHANGE_DEDUCTION: '兑换扣除',
    MANUAL_ADJUSTMENT: '手动调整'
  }
  return typeMap[type] || type
}

const adjustPoints = async () => {
  if (!adjustFormRef.value) return
  
  try {
    await adjustFormRef.value.validate()
    adjustLoading.value = true
    
    const amount = adjustForm.adjustType === 'DECREASE' ? -adjustForm.amount : adjustForm.amount
    
    const response = await fetch('/api/admin/points/adjust', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        userType: adjustForm.userType,
        userId: adjustForm.userId,
        amount: amount,
        description: adjustForm.description
      })
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('积分调整成功')
      resetAdjustForm()
      getPoints()
    } else {
      ElMessage.error(data.message || '调整失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    adjustLoading.value = false
  }
}

const resetAdjustForm = () => {
  if (adjustFormRef.value) {
    adjustFormRef.value.resetFields()
  }
  adjustForm.userType = 'ELDER'
  adjustForm.adjustType = 'INCREASE'
  adjustForm.amount = 1
}

onMounted(() => {
  getPoints()
})
</script>

<style scoped>
.point-list-container {
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
  margin-bottom: 30px;
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

.adjust-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.demo-ruleForm {
  padding: 20px 0;
}

.demo-ruleForm .el-form-item {
  margin-bottom: 20px;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}
</style>