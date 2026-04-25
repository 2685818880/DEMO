<template>
  <div class="report-designer-list">
    <div class="app-container">
      <div class="filter-container">
        <el-button class="filter-item" type="primary" icon="el-icon-plus" @click="handleCreate">
          新建报表
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
        <el-table-column label="报表编码" prop="reportCode" min-width="150" />
        <el-table-column label="报表名称" prop="reportName" min-width="180" />
        <el-table-column label="报表类型" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="getTypeTagType(row.reportType)" size="small">
              {{ getTypeText(row.reportType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" prop="category" width="100" align="center" />
        <el-table-column label="版本" prop="version" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" icon="el-icon-edit" @click="handleDesign(row)">
              设计
            </el-button>
            <el-button type="text" icon="el-icon-video-play" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="text" icon="el-icon-delete" style="color: #f56c6c" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { getReportConfigList, deleteReportConfig } from '@/report-designer/api/reportConfig'

export default {
  name: 'ReportDesignerList',
  data: function() {
    return {
      list: [],
      listLoading: false
    }
  },
  created: function() {
    this.getList()
  },
  methods: {
    getList: function() {
      var self = this
      this.listLoading = true
      getReportConfigList().then(function(res) {
        var data = res.data || res
        self.list = Array.isArray(data) ? data : []
        self.listLoading = false
      }).catch(function(e) {
        console.error('获取报表列表失败', e)
        self.listLoading = false
      })
    },
    getTypeTagType: function(type) {
      var map = {
        TABLE: 'info',
        CHART: 'warning',
        MIXED: 'primary'
      }
      return map[type] || 'info'
    },
    getTypeText: function(type) {
      var map = {
        TABLE: '表格',
        CHART: '图表',
        MIXED: '混合'
      }
      return map[type] || type || '未知'
    },
    handleCreate: function() {
      this.$router.push('/report-designer/design')
    },
    handleDesign: function(row) {
      this.$router.push('/report-designer/design/' + row.reportCode)
    },
    handleView: function(row) {
      this.$router.push('/report-designer/view/' + row.reportCode)
    },
    handleDelete: function(row) {
      var self = this
      this.$confirm('确认删除报表「' + row.reportName + '」吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        return deleteReportConfig(row.reportCode)
      }).then(function() {
        self.$message.success('删除成功')
        self.getList()
      }).catch(function() {})
    }
  }
}
</script>

<style lang="scss" scoped>
.report-designer-list {
  padding: 20px;
  .filter-container {
    padding-bottom: 20px;
    .filter-item {
      margin-right: 10px;
    }
  }
}
</style>
