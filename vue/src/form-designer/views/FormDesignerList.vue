<template>
  <div class="form-designer-list">
    <div class="app-container">
      <div class="filter-container">
        <el-button class="filter-item" type="primary" icon="el-icon-plus" @click="handleCreate">
          新建表单
        </el-button>
        <el-button class="filter-item" type="success" icon="el-icon-s-data" @click="handleDatabase">
          数据库配置
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
        <el-table-column label="表单编码" prop="code" min-width="150" />
        <el-table-column label="表单名称" prop="name" min-width="180" />
        <el-table-column label="版本" prop="version" width="80" align="center" />
        <el-table-column label="状态" prop="status" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" width="170" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" icon="el-icon-edit" @click="handleDesign(row)">
              设计
            </el-button>
            <el-button type="text" icon="el-icon-view" @click="handlePreview(row)">
              预览
            </el-button>
            <el-button type="text" icon="el-icon-delete" style="color: #f56c6c" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script>
import { getFormConfigList, saveFormConfig } from '@/form-designer/api/formConfig'
import Pagination from '@/components/Pagination'

export default {
  name: 'FormDesignerList',
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        page: 1,
        limit: 20
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
        const res = await getFormConfigList()
        this.list = res.data || []
        this.total = this.list.length
      } catch (e) {
        console.error('获取表单配置列表失败', e)
      } finally {
        this.listLoading = false
      }
    },
    handleCreate() {
      this.$router.push('/form-designer/design')
    },
    handleDesign(row) {
      this.$router.push(`/form-designer/design/${row.code}`)
    },
    handlePreview(row) {
      // TODO: 跳转到表单预览页面
      this.$message.info('表单预览功能开发中')
    },
    handleDelete(row) {
      this.$confirm(`确认删除表单「${row.name}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO: 调用删除API
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {})
    },
    handleDatabase() {
      this.$router.push('/form-designer/database')
    }
  }
}
</script>

<style lang="scss" scoped>
.form-designer-list {
  padding: 20px;
  .filter-container {
    padding-bottom: 20px;
    .filter-item {
      margin-right: 10px;
    }
  }
}
</style>
