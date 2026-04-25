<template>
  <div class="provider-management">
    <h2 style="margin-bottom: 20px;">API 提供商管理</h2>

    <!-- 配置属性 -->
    <div v-if="false" class="page-config">
      {{ enableAdd }}{{ enableDelete }}{{ enableBatchOperation }}{{ searchEnabled }}{{ refreshEnabled }}
    </div>

    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button v-if="enableAdd" type="primary" icon="el-icon-plus" @click="openAddDialog">新增提供商</el-button>
        <el-dropdown v-if="enableBatchOperation" @command="handleBatchCommand" style="margin-left: 10px;">
          <el-button>
            批量操作<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="batchDelete" icon="el-icon-delete">批量删除</el-dropdown-item>
            <el-dropdown-item command="batchUpdateKey" icon="el-icon-key">批量更新密钥</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <div class="toolbar-right">
        <el-button v-if="refreshEnabled" icon="el-icon-refresh" :loading="loading" @click="fetchData">刷新</el-button>
        <el-input v-if="searchEnabled"
          v-model="searchKeyword"
          placeholder="搜索提供商名称"
          prefix-icon="el-icon-search"
          clearable
          style="width: 220px; margin-left: 10px;"
          @input="handleSearch"
        />
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
      ref="table"
      v-loading="loading"
      :data="providers"
      border
      stripe
      size="small"
      style="width: 100%; margin-top: 16px;"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="45" fixed></el-table-column>
      <el-table-column label="外部提供方" min-width="200" fixed>
        <template slot-scope="scope">
          <div class="provider-cell">
            <div class="provider-logo">
              <img v-if="scope.row.logo" :src="scope.row.logo" alt="logo" class="logo-img" />
              <i v-else class="el-icon-office-building" style="font-size: 28px; color: #c0c4cc;"></i>
            </div>
            <div class="provider-info">
              <span class="provider-name">{{ scope.row.name }}</span>
              <span class="provider-url">{{ scope.row.apiBaseUrl }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="API密钥状态" width="150" sortable prop="apiKeyStatus">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.apiKeyStatus)" size="small" effect="dark">
            <i :class="statusIcon(scope.row.apiKeyStatus)"></i>
            {{ statusLabel(scope.row.apiKeyStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="170" sortable prop="lastUpdateTime">
        <template slot-scope="scope">
          {{ scope.row.lastUpdateTime ? formatTime(scope.row.lastUpdateTime) : '--' }}
        </template>
      </el-table-column>
      <el-table-column label="支持模型" width="110">
        <template slot-scope="scope">
          <el-button type="text" @click="openModelDialog(scope.row)">查看模型</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template slot-scope="scope">
          <template v-if="scope.row.apiKeyStatus === 'not_hosted'">
            <el-button type="primary" size="mini" @click="openKeyDialog(scope.row)">托管密钥</el-button>
          </template>
          <template v-else>
            <el-button type="warning" size="mini" @click="openUpdateKeyDialog(scope.row)">更新密钥</el-button>
            <el-button v-if="enableDelete" type="danger" size="mini" @click="handleDeleteKey(scope.row)">删除密钥</el-button>
          </template>
          <el-dropdown @command="(cmd) => handleRowCommand(cmd, scope.row)" style="margin-left: 5px;">
            <el-button size="mini" icon="el-icon-more"></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="edit" icon="el-icon-edit">编辑</el-dropdown-item>
              <el-dropdown-item v-if="enableDelete" command="delete" icon="el-icon-delete" divided>删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="formRules" label-width="120px" size="small">
        <el-form-item label="提供商名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：OpenAI" maxlength="100" />
        </el-form-item>
        <el-form-item label="API Base URL" prop="apiBaseUrl">
          <el-input v-model="form.apiBaseUrl" placeholder="例如：https://api.openai.com" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="form.apiKey" type="password" show-password placeholder="请输入API密钥（可选）" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="form.logo" placeholder="提供商Logo地址（可选）" />
        </el-form-item>
        <el-form-item label="支持模型">
          <div v-for="(m, idx) in form.models" :key="idx" class="model-item">
            <el-input v-model="m.name" placeholder="模型名称" style="width: 200px; margin-right: 8px;" />
            <el-select v-model="m.type" placeholder="类型" style="width: 120px; margin-right: 8px;">
              <el-option label="LLM" value="llm" />
              <el-option label="Embedding" value="embedding" />
              <el-option label="Vision" value="vision" />
              <el-option label="其他" value="other" />
            </el-select>
            <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="form.models.splice(idx, 1)" />
          </div>
          <el-button type="text" icon="el-icon-plus" @click="form.models.push({ name: '', type: 'llm', isEnabled: true })">添加模型</el-button>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注（可选）" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </span>
    </el-dialog>

    <!-- 托管/更新密钥 弹窗 -->
    <el-dialog :title="keyDialogTitle" :visible.sync="keyDialogVisible" width="480px" :close-on-click-modal="false">
      <el-form label-width="100px" size="small">
        <el-form-item :label="keyDialogTitle">
          <el-input
            v-model="keyInput"
            type="password"
            show-password
            :rows="3"
            placeholder="粘贴或输入API密钥"
          />
        </el-form-item>
        <div v-if="keyStrength !== null" style="margin-top: 8px; font-size: 12px;">
          密钥强度：
          <el-tag :type="keyStrength >= 80 ? 'success' : keyStrength >= 40 ? 'warning' : 'danger'" size="mini">
            {{ keyStrength >= 80 ? '强' : keyStrength >= 40 ? '中' : '弱' }}
          </el-tag>
        </div>
      </el-form>
      <span slot="footer">
        <el-button @click="keyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveKey">确认</el-button>
      </span>
    </el-dialog>

    <!-- 查看模型 弹窗 -->
    <el-dialog title="支持模型" :visible.sync="modelDialogVisible" width="660px">
      <el-table :data="currentModels" border stripe size="small" style="width: 100%">
        <el-table-column prop="name" label="模型名称" min-width="180" />
        <el-table-column prop="type" label="类型" width="120">
          <template slot-scope="scope">
            <el-tag size="mini">{{ scope.row.type || '--' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.isEnabled"
              active-color="#409eff"
              inactive-color="#c0c4cc"
              @change="handleToggleModel(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" :loading="scope.row._testing" @click="handleTestModel(scope.row)">测试</el-button>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer">
        <el-button @click="modelDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 空状态 -->
    <div v-if="!loading && providers.length === 0" class="empty-state">
      <i class="el-icon-office-building" style="font-size: 60px; color: #dcdfe6;"></i>
      <p style="color: #909399; margin-top: 12px;">暂无提供商，点击上方按钮新增</p>
    </div>
  </div>
</template>

<script>
import {
  listProviders, createProvider, updateProvider, deleteProvider,
  hostApiKey, updateApiKey, deleteApiKey,
  batchDeleteProviders, batchUpdateApiKey,
  listModels, toggleModel, testModel
} from '@/api/ai'

export default {
  name: 'ProviderManagement',
  props: {
    enableAdd: { type: Boolean, default: true },
    enableDelete: { type: Boolean, default: true },
    enableBatchOperation: { type: Boolean, default: true },
    searchEnabled: { type: Boolean, default: true },
    refreshEnabled: { type: Boolean, default: true }
  },
  data() {
    return {
      providers: [],
      loading: false,
      saving: false,
      searchKeyword: '',
      searchTimer: null,
      selectedIds: [],

      // Dialog
      dialogVisible: false,
      dialogTitle: '',
      editingId: null,
      form: {
        name: '', apiBaseUrl: '', apiKey: '', logo: '', remark: '',
        models: []
      },
      formRules: {
        name: [{ required: true, message: '请输入提供商名称', trigger: 'blur' }],
        apiBaseUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }]
      },

      // Key dialog
      keyDialogVisible: false,
      keyDialogTitle: '',
      keyDialogMode: 'host',
      keyProviderId: null,
      keyInput: '',
      keyStrength: null,

      // Model dialog
      modelDialogVisible: false,
      currentProviderId: null,
      currentProviderName: '',
      currentModels: []
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await listProviders({ keyword: this.searchKeyword || undefined })
        if (res.success) {
          this.providers = res.data || []
        }
      } catch (e) {
        console.error('获取提供商列表失败', e)
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      if (this.searchTimer) clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.fetchData()
      }, 300)
    },

    handleSelectionChange(selection) {
      this.selectedIds = selection.map(s => s.id)
    },

    // Batch operations
    handleBatchCommand(cmd) {
      if (this.selectedIds.length === 0) {
        this.$message.warning('请先选择提供商')
        return
      }
      if (cmd === 'batchDelete') {
        this.$confirm('确认删除选中的 ' + this.selectedIds.length + ' 个提供商？', '提示', {
          type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消'
        }).then(() => {
          batchDeleteProviders(this.selectedIds).then(res => {
            if (res.success) {
              this.$message.success('批量删除成功')
              this.fetchData()
            }
          })
        }).catch(() => {})
      } else if (cmd === 'batchUpdateKey') {
        this.$prompt('请输入新的API密钥', '批量更新密钥', {
          inputType: 'password', confirmButtonText: '确认', cancelButtonText: '取消'
        }).then(({ value }) => {
          if (value && value.trim()) {
            batchUpdateApiKey(this.selectedIds, value.trim()).then(res => {
              if (res.success) {
                this.$message.success('批量更新成功')
                this.fetchData()
              }
            })
          }
        }).catch(() => {})
      }
    },

    handleRowCommand(cmd, row) {
      if (cmd === 'edit') this.openEditDialog(row)
      else if (cmd === 'delete') this.handleDelete(row)
    },

    // Add / Edit
    openAddDialog() {
      this.dialogTitle = '新增提供商'
      this.editingId = null
      this.form = { name: '', apiBaseUrl: '', apiKey: '', logo: '', remark: '', models: [] }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
    },

    openEditDialog(row) {
      this.dialogTitle = '编辑提供商'
      this.editingId = row.id
      this.form = {
        name: row.name,
        apiBaseUrl: row.apiBaseUrl,
        apiKey: '',
        logo: row.logo || '',
        remark: row.remark || '',
        models: []
      }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
      // Load models
      listModels(row.id).then(res => {
        if (res.success && res.data) {
          this.form.models = res.data.map(m => ({
            name: m.name,
            type: m.type,
            isEnabled: m.isEnabled !== false
          }))
        }
      })
    },

    handleSave() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.saving = true
        const data = { ...this.form }
        if (!data.apiKey) delete data.apiKey

        const action = this.editingId
          ? updateProvider(this.editingId, data)
          : createProvider(data)

        action.then(res => {
          if (res.success) {
            this.$message.success(this.editingId ? '更新成功' : '新增成功')
            this.dialogVisible = false
            this.fetchData()
          }
        }).catch(e => {
          console.error('保存失败', e)
        }).finally(() => {
          this.saving = false
        })
      })
    },

    handleDelete(row) {
      this.$confirm('确认删除提供商 "' + row.name + '"？', '提示', {
        type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消'
      }).then(() => {
        deleteProvider(row.id).then(res => {
          if (res.success) {
            this.$message.success('删除成功')
            this.fetchData()
          }
        })
      }).catch(() => {})
    },

    // Key operations
    openKeyDialog(row) {
      this.keyDialogTitle = '托管密钥 - ' + row.name
      this.keyDialogMode = 'host'
      this.keyProviderId = row.id
      this.keyInput = ''
      this.keyStrength = null
      this.keyDialogVisible = true
    },

    openUpdateKeyDialog(row) {
      this.keyDialogTitle = '更新密钥 - ' + row.name
      this.keyDialogMode = 'update'
      this.keyProviderId = row.id
      this.keyInput = ''
      this.keyStrength = null
      this.keyDialogVisible = true
    },

    handleSaveKey() {
      if (!this.keyInput || !this.keyInput.trim()) {
        this.$message.warning('请输入API密钥')
        return
      }
      this.saving = true
      const apiKey = this.keyInput.trim()
      const action = this.keyDialogMode === 'host'
        ? hostApiKey(this.keyProviderId, apiKey)
        : updateApiKey(this.keyProviderId, apiKey)

      action.then(res => {
        if (res.success) {
          this.$message.success('密钥' + (this.keyDialogMode === 'host' ? '托管' : '更新') + '成功')
          this.keyDialogVisible = false
          this.fetchData()
        }
      }).finally(() => { this.saving = false })
    },

    handleDeleteKey(row) {
      this.$confirm('确认删除 "' + row.name + '" 的API密钥？', '提示', {
        type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消'
      }).then(() => {
        deleteApiKey(row.id).then(res => {
          if (res.success) {
            this.$message.success('密钥已删除')
            this.fetchData()
          }
        })
      }).catch(() => {})
    },

    // Model dialog
    openModelDialog(row) {
      this.currentProviderName = row.name
      this.currentProviderId = row.id
      this.currentModels = []
      this.modelDialogVisible = true
      listModels(row.id).then(res => {
        if (res.success) {
          this.currentModels = res.data || []
        }
      })
    },

    handleToggleModel(row) {
      if (!this.currentProviderId) return
      toggleModel(this.currentProviderId, row.id, row.isEnabled).then(res => {
        if (res.success) {
          this.$message.success(row.isEnabled ? '已启用' : '已禁用')
        }
      }).catch(() => {
        row.isEnabled = !row.isEnabled
      })
    },

    async handleTestModel(row) {
      this.$set(row, '_testing', true)
      try {
        const res = await testModel(row.id)
        if (res.success && res.data) {
          if (res.data.success) {
            this.$message.success(res.data.message || '连接成功')
          } else {
            this.$message.error(res.data.message || '连接失败')
          }
        }
      } catch (e) {
        this.$message.error('测试请求失败')
      } finally {
        this.$set(row, '_testing', false)
      }
    },

    // Status helpers
    statusTagType(status) {
      if (status === 'hosted') return 'success'
      if (status === 'expired') return 'danger'
      return 'warning'
    },

    statusIcon(status) {
      if (status === 'hosted') return 'el-icon-check'
      if (status === 'expired') return 'el-icon-circle-close'
      return 'el-icon-warning'
    },

    statusLabel(status) {
      if (status === 'hosted') return '已托管'
      if (status === 'expired') return '已过期'
      return '未托管'
    },

    formatTime(dateStr) {
      if (!dateStr) return '--'
      const d = new Date(dateStr)
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
        ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    }
  }
}
</script>

<style scoped>
.provider-management {
  padding: 20px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
}
.provider-cell {
  display: flex;
  align-items: center;
}
.provider-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 10px;
  overflow: hidden;
}
.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.provider-info {
  display: flex;
  flex-direction: column;
}
.provider-name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}
.provider-url {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.model-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.empty-state {
  padding: 60px 0;
  text-align: center;
}
</style>
