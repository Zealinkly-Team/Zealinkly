<template>
  <div class="points-management-container">
    <el-card class="points-card">
      <template #header>
        <div class="card-header">
          <span>积分管理</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <!-- 积分查询 -->
      <el-form
        :model="queryForm"
        :rules="queryRules"
        ref="queryFormRef"
        label-width="120px"
        class="query-form"
      >
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="queryForm.userType" placeholder="请选择用户类型">
            <el-option label="老人" :value="'ELDER'" />
            <el-option label="志愿者" :value="'VOLUNTEER'" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="queryForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="getPointsTotal" :loading="loading">
            查询积分
          </el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 积分总数 -->
      <el-card v-if="pointsTotal !== null" class="points-total-card">
        <div class="points-total">
          <h3>当前积分</h3>
          <div class="points-amount">{{ pointsTotal }} 积分</div>
        </div>
      </el-card>
      
      <!-- 积分流水 -->
      <el-card v-if="pointsHistory.length > 0" class="points-history-card">
        <template #header>
          <span>积分流水</span>
        </template>
        <el-table
          v-loading="loading"
          :data="pointsHistory"
          style="width: 100%"
        >
          <el-table-column prop="id" label="记录ID" width="80" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)">
                {{ getTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="积分" width="100">
            <template #default="scope">
              <span :class="scope.row.amount > 0 ? 'points-increase' : 'points-decrease'">
                {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="余额" width="100" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="createdAt" label="时间" width="180" />
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="historyPage"
            v-model:page-size="historySize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="historyTotal"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pointsAPI } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const queryFormRef = ref(null)
const pointsTotal = ref(null)
const pointsHistory = ref([])
const historyTotal = ref(0)
const historyPage = ref(1)
const historySize = ref(20)

const queryForm = reactive({
  userType: 'ELDER',
  userId: ''
})

const queryRules = {
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ]
}

const getPointsTotal = async () => {
  if (!queryFormRef.value) return
  
  try {
    await queryFormRef.value.validate()
    loading.value = true
    
    // 获取积分总数
    const totalResponse = await pointsAPI.getTotal(queryForm.userType, queryForm.userId)
    if (totalResponse.code === 200) {
      // 检查返回的数据格式，提取total值
      pointsTotal.value = typeof totalResponse.data === 'object' && totalResponse.data.total !== undefined 
        ? totalResponse.data.total 
        : totalResponse.data
    }
    
    // 获取积分流水
    await getPointsHistory()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const getPointsHistory = async () => {
  try {
    const historyResponse = await pointsAPI.getHistory(queryForm.userType, queryForm.userId, {
      page: historyPage.value - 1,
      size: historySize.value
    })
    
    if (historyResponse.code === 200) {
      pointsHistory.value = historyResponse.data.content
      historyTotal.value = historyResponse.data.totalElements
    }
  } catch (error) {
    ElMessage.error('获取积分流水失败')
  }
}

const getTypeText = (type) => {
  const typeMap = {
    TASK_COMPLETION: '任务完成',
    EMERGENCY_HANDLING: '紧急处理',
    POINTS_GRANT: '积分发放',
    EXCHANGE: '商品兑换',
    SYSTEM_ADJUSTMENT: '系统调整'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    TASK_COMPLETION: 'success',
    EMERGENCY_HANDLING: 'success',
    POINTS_GRANT: 'success',
    EXCHANGE: 'danger',
    SYSTEM_ADJUSTMENT: 'warning'
  }
  return typeMap[type] || 'info'
}

const resetQuery = () => {
  if (queryFormRef.value) {
    queryFormRef.value.resetFields()
  }
  pointsTotal.value = null
  pointsHistory.value = []
  historyTotal.value = 0
  historyPage.value = 1
  historySize.value = 20
}

const handleSizeChange = (size) => {
  historySize.value = size
  getPointsHistory()
}

const handleCurrentChange = (current) => {
  historyPage.value = current
  getPointsHistory()
}

onMounted(() => {
})
</script>

<style scoped>
.points-management-container {
  padding: 20px 0;
}

.points-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.query-form .el-form-item {
  margin-bottom: 20px;
}

.points-total-card {
  margin-bottom: 20px;
}

.points-total {
  text-align: center;
  padding: 20px 0;
}

.points-total h3 {
  margin-bottom: 10px;
  color: #606266;
}

.points-amount {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
}

.points-history-card {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.points-increase {
  color: #67c23a;
  font-weight: bold;
}

.points-decrease {
  color: #f56c6c;
  font-weight: bold;
}
</style>