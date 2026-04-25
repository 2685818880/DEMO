<template>
  <div class="database-config">
    <div class="app-container">
      <div class="filter-container">
        <el-button class="filter-item" type="primary" icon="el-icon-plus" @click="handleAdd">
          新增配置
        </el-button>
      </div>

      <el-table
        v-loading="listLoading"
        :data="list"
        border
        fit
        highlight-current-row
        style="width: 100%"
      >
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="数据库编码" prop="dbCode" min-width="120" />
        <el-table-column label="数据库类型" prop="dbType" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag>{{ row.dbType || 'MySQL' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="主机地址" prop="host" min-width="140" />
        <el-table-column label="端口" prop="port" width="80" align="center" />
        <el-table-column label="数据库名" prop="dbName" min-width="120" />
        <el-table-column label="用户名" prop="username" width="100" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.connected ? 'success' : 'danger'" size="small">
              {{ row.connected ? '已连接' : '未连接' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" icon="el-icon-connection" @click="handleTest(row)">
              测试连接
            </el-button>
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="text" icon="el-icon-delete" style="color: #f56c6c" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 新增/编辑对话框 -->
      <el-dialog
        :title="dialogStatus === 'create' ? '新增数据库配置' : '编辑数据库配置'"
        :visible.sync="dialogVisible"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="dataForm"
          :model="formData"
          :rules="rules"
          label-width="100px"
          size="small"
        >
          <el-form-item label="数据库编码" prop="dbCode">
            <el-input v-model="formData.dbCode" placeholder="请输入数据库编码" :disabled="dialogStatus === 'update'" />
          </el-form-item>
          <el-form-item label="数据库类型" prop="dbType">
            <el-select v-model="formData.dbType" placeholder="请选择数据库类型" style="width: 100%">
              <el-option label="MySQL" value="MYSQL" />
              <el-option label="Oracle" value="ORACLE" />
              <el-option label="PostgreSQL" value="POSTGRESQL" />
              <el-option label="SQL Server" value="SQLSERVER" />
            </el-select>
          </el-form-item>
          <el-form-item label="主机地址" prop="host">
            <el-input v-model="formData.host" placeholder="请输入主机地址" />
          </el-form-item>
          <el-form-item label="端口" prop="port">
            <el-input-number v-model="formData.port" :min="1" :max="65535" placeholder="端口" />
          </el-form-item>
          <el-form-item label="数据库名" prop="dbName">
            <el-input v-model="formData.dbName" placeholder="请输入数据库名" />
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="formData.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
        </el-form>
        <span slot="footer">
          <el-button size="small" @click="dialogVisible = false">取消</el-button>
          <el-button size="small" type="primary" @click="handleConfirm">确定</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getDatabaseConfigList, saveDatabaseConfig, testDatabaseConnection, deleteDatabaseConfig } from '@/form-designer/api/database'

export default {
  name: 'DatabaseConfig',
  data() {
    return {
      list: [],
      listLoading: false,
      dialogVisible: false,
      dialogStatus: 'create',
      formData: {
        dbCode: '',
        dbType: 'MYSQL',
        host: '',
        port: 3306,
        dbName: '',
        username: '',
        password: ''
      },
      rules: {
        dbCode: [
          { required: true, message: '请输入数据库编码', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]+$/, message: '编码只能包含字母、数字和下划线', trigger: 'blur' }
        ],
        dbType: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
        host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
        port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
        dbName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.listLoading = true
      try {
        const res = await getDatabaseConfigList()
        this.list = res.data || []
      } catch (e) {
        console.error('获取数据库配置列表失败', e)
      } finally {
        this.listLoading = false
      }
    },
    handleAdd() {
      this.dialogStatus = 'create'
      this.formData = {
        dbCode: '',
        dbType: 'MYSQL',
        host: '',
        port: 3306,
        dbName: '',
        username: '',
        password: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.dataForm && this.$refs.dataForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogStatus = 'update'
      this.formData = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.dataForm && this.$refs.dataForm.clearValidate()
      })
    },
    handleConfirm() {
      this.$refs.dataForm.validate(async valid => {
        if (!valid) return
        try {
          await saveDatabaseConfig(this.formData)
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.getList()
        } catch (e) {
          console.error('保存数据库配置失败', e)
        }
      })
    },
    async handleTest(row) {
      try {
        await testDatabaseConnection(row.dbCode)
        this.$message.success('连接测试成功')
        this.getList()
      } catch (e) {
        this.$message.error('连接测试失败')
      }
    },
    async handleDelete(row) {
      this.$confirm(`确认删除数据库配置「${row.dbCode}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteDatabaseConfig(row.dbCode)
          this.$message.success('删除成功')
          this.getList()
        } catch (e) {
          console.error('删除数据库配置失败', e)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.database-config {
  padding: 20px;
  .filter-container {
    padding-bottom: 20px;
    .filter-item {
      margin-right: 10px;
    }
  }
}
</style>
