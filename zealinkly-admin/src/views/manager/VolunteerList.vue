<template>
  <div class="volunteer-list-container">
    <div class="page-header">
      <h3>志愿者管理</h3>
      <el-button type="primary" @click="$router.push('/manager/volunteers/create')">新增志愿者</el-button>
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
          <div>
            <el-button size="small" @click="handleBulkDelete" :disabled="selectedVolunteers.length === 0">批量删除</el-button>
            <el-button size="small" @click="handleBulkImport">批量导入</el-button>
            <el-button size="small" @click="exportTemplate">导出模板</el-button>
          </div>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="volunteers"
        style="width: 100%"
        @selection-change="handleSelectionChange"
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
        :action="`${API_BASE_URL}/api/admin/volunteers/bulk-import`"
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
import { volunteerAPI, API_BASE_URL } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const volunteers = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(20)
const selectedVolunteers = ref([])
const importDialogVisible = ref(false)

const searchForm = reactive({
  username: '',
  realName: '',
  enabled: null
})

const getVolunteers = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.realName) params.realName = searchForm.realName
    if (searchForm.enabled !== null) params.enabled = searchForm.enabled
    
    const response = await volunteerAPI.getList(params)
    if (response.code === 200) {
      volunteers.value = response.data.content || []
      total.value = response.data.totalElements || 0
    }
  } catch (error) {
    // 错误已在API拦截器中处理
  } finally {
    loading.value = false
  }
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
    const response = await (enabled ? volunteerAPI.enable(id) : volunteerAPI.disable(id))
    if (response.code === 200) {
      ElMessage.success(enabled ? '已启用' : '已禁用')
    }
  } catch (error) {
    // 恢复原状态
    const volunteer = volunteers.value.find(v => v.id === id)
    if (volunteer) volunteer.enabled = !enabled
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
    
    const response = await volunteerAPI.delete(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      getVolunteers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      // 错误已在API拦截器中处理
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
    const response = await volunteerAPI.bulkDelete(ids)
    if (response.code === 200) {
      ElMessage.success('批量删除成功')
      selectedVolunteers.value = []
      getVolunteers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      // 错误已在API拦截器中处理
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
  // 实际项目中应该提供模板下载链接
  ElMessage.info('模板下载功能开发中')
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