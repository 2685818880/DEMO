<template>
  <div class="report-designer-edit">
    <div class="design-toolbar">
      <el-form :inline="true" size="small" class="toolbar-form">
        <el-form-item label="报表名称">
          <el-input v-model="reportName" placeholder="请输入报表名称" style="width: 160px" />
        </el-form-item>
        <el-form-item label="报表编码">
          <el-input v-model="reportCode" placeholder="自动生成" style="width: 160px" :disabled="isEditing" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-check" @click="handleSave" :loading="saving">
            保存
          </el-button>
          <el-button icon="el-icon-view" @click="handlePreview" :loading="previewLoading">
            预览
          </el-button>
          <el-button icon="el-icon-back" @click="handleBack">
            返回列表
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="design-body">
      <div class="config-panel">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 1. 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form label-width="100px" size="small">
              <el-form-item label="报表名称">
                <el-input v-model="reportName" placeholder="请输入报表名称" style="width: 300px" />
              </el-form-item>
              <el-form-item label="报表编码">
                <el-input v-model="reportCode" placeholder="英文编码，如 inventory_report" style="width: 300px" :disabled="isEditing" />
              </el-form-item>
              <el-form-item label="报表类型">
                <el-radio-group v-model="reportType">
                  <el-radio label="TABLE">表格</el-radio>
                  <el-radio label="CHART">图表</el-radio>
                  <el-radio label="MIXED">混合</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="分类">
                <el-input v-model="category" placeholder="可选，如：库存、出入库" style="width: 300px" />
              </el-form-item>
              <el-form-item label="数据源">
                <el-select v-model="dbCode" placeholder="默认数据源" clearable style="width: 300px">
                  <el-option label="默认数据源" value="" />
                </el-select>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="remark" type="textarea" :rows="3" style="width: 300px" />
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 2. SQL编辑器 -->
          <el-tab-pane label="SQL编辑器" name="sql">
            <div class="sql-editor-section">
              <div class="sql-toolbar">
                <el-button type="primary" size="small" @click="handleDetectParams" :disabled="!sqlTemplate.trim()">
                  检测参数
                </el-button>
                <el-button type="success" size="small" @click="handleAnalyzeColumns" :disabled="!sqlTemplate.trim()">
                  自动探测列
                </el-button>
              </div>
              <el-input
                v-model="sqlTemplate"
                type="textarea"
                :rows="15"
                placeholder="输入SQL查询语句，使用 :paramName 作为参数占位符&#10;例如: SELECT * FROM wms_inventory WHERE house_code = :houseCode"
                class="sql-textarea"
              />
              <div v-if="detectedParams.length" class="param-hints">
                <span class="param-hint-label">检测到参数：</span>
                <el-tag
                  v-for="p in detectedParams"
                  :key="p"
                  size="small"
                  type="warning"
                  style="margin-right: 5px"
                >{{ p }}</el-tag>
              </div>
              <div v-if="analyzedColumns.length" class="analyzed-columns">
                <span class="param-hint-label">探测到列（{{ analyzedColumns.length }}个）：</span>
                <el-tag
                  v-for="c in analyzedColumns"
                  :key="c.columnName"
                  size="small"
                  style="margin-right: 4px; margin-top: 4px"
                >{{ c.columnName }} <span class="col-type">({{ c.type }})</span></el-tag>
              </div>
            </div>
          </el-tab-pane>

          <!-- 3. 列配置 -->
          <el-tab-pane label="列配置" name="columns">
            <div class="column-config-toolbar">
              <el-button size="small" @click="addColumn">添加列</el-button>
              <el-button size="small" @click="syncFromAnalyzed" :disabled="!analyzedColumns.length">
                从探测结果导入
              </el-button>
            </div>
            <el-table :data="columnsConfig" border size="small" max-height="500" style="width: 100%">
              <el-table-column label="字段名(prop)" width="160">
                <template slot-scope="{ row, $index }">
                  <el-input v-model="row.prop" size="mini" placeholder="字段名" @input="handleColumnChange($index)" />
                </template>
              </el-table-column>
              <el-table-column label="显示名(label)" width="160">
                <template slot-scope="{ row }">
                  <el-input v-model="row.label" size="mini" placeholder="显示名称" />
                </template>
              </el-table-column>
              <el-table-column label="宽度" width="80">
                <template slot-scope="{ row }">
                  <el-input-number v-model="row.width" size="mini" :min="60" :max="500" :step="10" controls-position="right" style="width: 70px" />
                </template>
              </el-table-column>
              <el-table-column label="对齐" width="80">
                <template slot-scope="{ row }">
                  <el-select v-model="row.align" size="mini">
                    <el-option label="左" value="left" />
                    <el-option label="中" value="center" />
                    <el-option label="右" value="right" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="格式化" width="120">
                <template slot-scope="{ row }">
                  <el-select v-model="row.format" size="mini" clearable>
                    <el-option label="无" value="" />
                    <el-option label="数字 #,##0" value="number" />
                    <el-option label="小数 #,##0.00" value="decimal2" />
                    <el-option label="百分比" value="percent" />
                    <el-option label="日期" value="date" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="排序" width="60" align="center">
                <template slot-scope="{ row }">
                  <el-checkbox v-model="row.sortable" />
                </template>
              </el-table-column>
              <el-table-column label="隐藏" width="60" align="center">
                <template slot-scope="{ row }">
                  <el-checkbox v-model="row.hidden" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template slot-scope="{ $index }">
                  <el-button type="text" size="mini" style="color: #f56c6c" @click="removeColumn($index)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 4. 筛选条件 -->
          <el-tab-pane label="筛选条件" name="filters">
            <div class="filter-toolbar">
              <el-button size="small" @click="addFilter">添加筛选条件</el-button>
            </div>
            <el-table :data="filtersConfig" border size="small" max-height="500" style="width: 100%">
              <el-table-column label="参数名(prop)" width="160">
                <template slot-scope="{ row, $index }">
                  <el-select v-model="row.prop" size="mini" filterable allow-create clearable @change="onFilterPropChange($index)">
                    <el-option v-for="p in detectedParams" :key="p" :label="p" :value="p" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="显示名(label)" width="150">
                <template slot-scope="{ row }">
                  <el-input v-model="row.label" size="mini" placeholder="显示名称" />
                </template>
              </el-table-column>
              <el-table-column label="类型" width="120">
                <template slot-scope="{ row }">
                  <el-select v-model="row.type" size="mini">
                    <el-option label="文本" value="string" />
                    <el-option label="数字" value="number" />
                    <el-option label="日期" value="datePicker" />
                    <el-option label="日期范围" value="dateRange" />
                    <el-option label="下拉选择" value="select" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="必填" width="60" align="center">
                <template slot-scope="{ row }">
                  <el-checkbox v-model="row.required" />
                </template>
              </el-table-column>
              <el-table-column label="默认值" width="120">
                <template slot-scope="{ row }">
                  <el-input v-model="row.defaultValue" size="mini" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template slot-scope="{ $index }">
                  <el-button type="text" size="mini" style="color: #f56c6c" @click="removeFilter($index)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 5. 图表配置 -->
          <el-tab-pane label="图表配置" name="charts" :disabled="reportType === 'TABLE'">
            <div v-if="reportType === 'TABLE'" style="padding: 20px; color: #999">
              请选择「图表」或「混合」类型以启用图表配置
            </div>
            <div v-else>
              <el-form label-width="100px" size="small">
                <el-form-item label="图表类型">
                  <el-select v-model="chartConfig.chartType" style="width: 200px">
                    <el-option label="柱状图" value="bar" />
                    <el-option label="折线图" value="line" />
                    <el-option label="饼图" value="pie" />
                  </el-select>
                </el-form-item>
                <el-form-item label="图表标题">
                  <el-input v-model="chartConfig.title" style="width: 300px" placeholder="图表标题" />
                </el-form-item>
                <el-form-item label="维度字段">
                  <el-select v-model="chartConfig.dimension" style="width: 300px" clearable>
                    <el-option v-for="c in columnsConfig" :key="c.prop" :label="c.label || c.prop" :value="c.prop" />
                  </el-select>
                </el-form-item>
                <el-form-item label="指标字段">
                  <el-select v-model="chartConfig.metric" style="width: 300px" clearable>
                    <el-option v-for="c in columnsConfig" :key="c.prop" :label="c.label || c.prop" :value="c.prop" />
                  </el-select>
                </el-form-item>
                <el-form-item label="排序字段">
                  <el-select v-model="chartConfig.sortBy" style="width: 300px" clearable>
                    <el-option v-for="c in columnsConfig" :key="c.prop" :label="c.label || c.prop" :value="c.prop" />
                  </el-select>
                </el-form-item>
                <el-form-item label="显示条数">
                  <el-input-number v-model="chartConfig.limit" :min="5" :max="100" :step="5" />
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 6. 导出/显示 -->
          <el-tab-pane label="导出配置" name="export">
            <el-form label-width="120px" size="small">
              <el-form-item label="启用分页">
                <el-switch v-model="paginationEnabled" />
              </el-form-item>
              <el-form-item v-if="paginationEnabled" label="每页条数">
                <el-input-number v-model="pageSize" :min="10" :max="200" :step="10" />
              </el-form-item>
              <el-form-item label="启用导出">
                <el-switch v-model="exportEnabled" />
              </el-form-item>
              <el-form-item v-if="exportEnabled" label="导出文件名">
                <el-input v-model="exportFileName" style="width: 300px" placeholder="默认为报表名称" />
              </el-form-item>
              <el-form-item v-if="exportEnabled" label="最大导出行">
                <el-input-number v-model="exportMaxRows" :min="1000" :max="100000" :step="1000" />
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div v-if="showPreview" class="preview-panel">
        <div class="preview-header">
          <span>预览</span>
          <el-button size="mini" type="text" @click="showPreview = false">关闭</el-button>
        </div>
        <div class="preview-body">
          <el-table
            v-loading="previewLoading"
            :data="previewData"
            border
            stripe
            size="small"
            max-height="600"
            style="width: 100%"
          >
            <el-table-column
              v-for="col in columnsConfig"
              :key="col.prop"
              :prop="col.prop"
              :label="col.label || col.prop"
              :width="col.width"
              :align="col.align || 'left'"
              :sortable="col.sortable"
              :show-overflow-tooltip="true"
              v-if="!col.hidden"
            />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { saveReportConfig, getReportConfigDetail, executeReport, analyzeSql } from '@/report-designer/api/reportConfig'

export default {
  name: 'ReportDesignerEdit',
  data: function() {
    return {
      // 基本信息
      reportName: '',
      reportCode: '',
      reportType: 'TABLE',
      category: '',
      dbCode: '',
      remark: '',
      isEditing: false,
      saving: false,

      // SQL
      sqlTemplate: '',
      detectedParams: [],
      analyzedColumns: [],

      // 列配置
      columnsConfig: [],

      // 筛选条件
      filtersConfig: [],

      // 图表
      chartConfig: {
        chartType: 'bar',
        title: '',
        dimension: '',
        metric: '',
        sortBy: '',
        limit: 20
      },

      // 导出/显示
      paginationEnabled: true,
      pageSize: 20,
      exportEnabled: true,
      exportFileName: '',
      exportMaxRows: 10000,

      // UI
      activeTab: 'basic',
      showPreview: false,
      previewLoading: false,
      previewData: []
    }
  },
  created: function() {
    var code = this.$route.params.code
    if (code) {
      this.isEditing = true
      this.reportCode = code
      this.loadReportConfig(code)
    }
  },
  methods: {
    loadReportConfig: function(code) {
      var self = this
      getReportConfigDetail(code).then(function(res) {
        var config = res.data || res
        self.reportName = config.reportName || ''
        self.reportCode = config.reportCode || ''
        self.category = config.category || ''
        self.dbCode = config.dbCode || ''
        self.remark = config.remark || ''
        self.reportType = config.reportType || 'TABLE'

        if (config.configJson) {
          try {
            var json = JSON.parse(config.configJson)
            self.sqlTemplate = json.sql || ''
            self.columnsConfig = json.columns || []
            self.filtersConfig = json.searchParams || []
            self.paginationEnabled = json.pagination ? json.pagination.enabled !== false : true
            self.pageSize = (json.pagination && json.pagination.pageSize) || 20
            self.exportEnabled = json.export ? json.export.enabled !== false : true
            self.exportFileName = (json.export && json.export.fileName) || ''
            self.exportMaxRows = (json.export && json.export.maxRows) || 10000
            if (json.charts && json.charts.length) {
              self.chartConfig = json.charts[0]
            }
            self.detectParams()
          } catch (e) {
            console.error('解析配置JSON失败', e)
          }
        }
      }).catch(function(e) {
        console.error('加载报表配置失败', e)
        self.$message.error('加载报表配置失败: ' + (e.message || '未知错误'))
      })
    },

    detectParams: function() {
      var sql = this.sqlTemplate
      var params = []
      if (!sql) {
        this.detectedParams = []
        return
      }
      var regex = /:(\w+)/g
      var match
      while ((match = regex.exec(sql)) !== null) {
        var name = match[1]
        // 避免匹配到字符串字面量内的参数
        if (params.indexOf(name) === -1) {
          params.push(name)
        }
      }
      this.detectedParams = params
    },

    handleDetectParams: function() {
      this.detectParams()
      if (this.detectedParams.length) {
        this.$message.success('检测到 ' + this.detectedParams.length + ' 个参数')
      } else {
        this.$message.info('未检测到参数')
      }
    },

    handleAnalyzeColumns: function() {
      var self = this
      analyzeSql(this.sqlTemplate, this.dbCode).then(function(res) {
        var data = res.data || res
        if (Array.isArray(data)) {
          self.analyzedColumns = data
          self.$message.success('探测到 ' + data.length + ' 个列')
        } else {
          self.$message.warning('探测结果格式异常')
        }
      }).catch(function(e) {
        console.error('探测列失败', e)
        self.$message.error('探测列失败: ' + (e.message || '无法执行SQL'))
      })
    },

    syncFromAnalyzed: function() {
      var self = this
      this.analyzedColumns.forEach(function(col) {
        var exists = self.columnsConfig.some(function(c) {
          return c.prop === col.columnName
        })
        if (!exists) {
          self.columnsConfig.push({
            prop: col.columnName,
            label: col.columnName,
            width: 120,
            align: 'left',
            format: '',
            sortable: false,
            hidden: false
          })
        }
      })
      this.$message.success('已从探测结果导入 ' + this.analyzedColumns.length + ' 个列')
    },

    addColumn: function() {
      this.columnsConfig.push({
        prop: '',
        label: '',
        width: 120,
        align: 'left',
        format: '',
        sortable: false,
        hidden: false
      })
    },

    removeColumn: function(index) {
      this.columnsConfig.splice(index, 1)
    },

    handleColumnChange: function(index) {
      var col = this.columnsConfig[index]
      if (col && !col.label) {
        col.label = col.prop
      }
    },

    addFilter: function() {
      this.filtersConfig.push({
        prop: '',
        label: '',
        type: 'string',
        required: false,
        defaultValue: ''
      })
    },

    removeFilter: function(index) {
      this.filtersConfig.splice(index, 1)
    },

    onFilterPropChange: function(index) {
      var filter = this.filtersConfig[index]
      if (filter && filter.prop && !filter.label) {
        filter.label = filter.prop
      }
    },

    buildConfigJson: function() {
      var config = {
        sql: this.sqlTemplate,
        columns: this.columnsConfig,
        searchParams: this.filtersConfig,
        pagination: {
          enabled: this.paginationEnabled,
          pageSize: this.pageSize
        },
        export: {
          enabled: this.exportEnabled,
          fileName: this.exportFileName || this.reportName,
          maxRows: this.exportMaxRows
        }
      }
      if (this.reportType !== 'TABLE') {
        config.charts = [this.chartConfig]
      }
      return JSON.stringify(config)
    },

    handleSave: function() {
      var self = this
      if (!this.reportName.trim()) {
        this.$message.warning('请输入报表名称')
        return
      }
      if (!this.reportCode.trim()) {
        this.$message.warning('请输入报表编码')
        return
      }
      if (!this.sqlTemplate.trim()) {
        this.$message.warning('请输入SQL语句')
        return
      }

      var saveData = {
        reportName: this.reportName,
        reportCode: this.reportCode,
        reportType: this.reportType,
        category: this.category,
        dbCode: this.dbCode,
        remark: this.remark,
        configJson: this.buildConfigJson(),
        version: 1,
        status: 1
      }

      this.saving = true
      saveReportConfig(saveData).then(function() {
        self.$message.success('保存成功')
        if (!self.isEditing) {
          self.isEditing = true
          self.$router.replace('/report-designer/design/' + self.reportCode)
        }
        self.saving = false
      }).catch(function(e) {
        console.error('保存报表配置失败', e)
        self.$message.error('保存失败: ' + (e.message || '未知错误'))
        self.saving = false
      })
    },

    handlePreview: function() {
      if (!this.sqlTemplate.trim()) {
        this.$message.warning('请先输入SQL语句')
        return
      }
      if (!this.reportCode && !this.isEditing) {
        // 未保存无法预览，先生成一个临时编码用于预览
        this.$message.warning('请先保存报表')
        return
      }

      var self = this
      this.showPreview = true
      this.previewLoading = true

      var params = {}
      this.filtersConfig.forEach(function(f) {
        if (f.defaultValue) {
          params[f.prop] = f.defaultValue
        }
      })

      executeReport(this.reportCode, {
        params: params,
        page: 1,
        pageSize: 20
      }).then(function(res) {
        var data = res.data || res
        self.previewData = data.rows || []
        self.previewLoading = false
      }).catch(function(e) {
        console.error('预览失败', e)
        self.$message.error('预览失败: ' + (e.message || '查询执行失败'))
        self.previewLoading = false
      })
    },

    handleBack: function() {
      this.$router.push('/report-designer/list')
    }
  }
}
</script>

<style lang="scss" scoped>
.report-designer-edit {
  height: 100%;
  display: flex;
  flex-direction: column;

  .design-toolbar {
    padding: 12px 20px;
    background: #fff;
    border-bottom: 1px solid #e6ebf5;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .toolbar-form {
      margin-bottom: 0;
    }
  }

  .design-body {
    flex: 1;
    display: flex;
    overflow: hidden;

    .config-panel {
      flex: 1;
      padding: 10px;
      overflow: auto;
    }

    .preview-panel {
      width: 500px;
      border-left: 1px solid #e6ebf5;
      display: flex;
      flex-direction: column;

      .preview-header {
        padding: 8px 12px;
        border-bottom: 1px solid #e6ebf5;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: bold;
        background: #fafafa;
      }

      .preview-body {
        flex: 1;
        padding: 10px;
        overflow: auto;
      }
    }
  }
}

.sql-editor-section {
  .sql-toolbar {
    margin-bottom: 10px;
  }
  .sql-textarea {
    font-family: 'Consolas', 'Courier New', monospace;
    font-size: 13px;
  }
  .param-hints {
    margin-top: 8px;
    .param-hint-label {
      font-size: 12px;
      color: #909399;
      margin-right: 8px;
    }
  }
  .analyzed-columns {
    margin-top: 8px;
    .param-hint-label {
      font-size: 12px;
      color: #909399;
      margin-right: 8px;
    }
    .col-type {
      color: #909399;
      font-size: 11px;
    }
  }
}

.column-config-toolbar {
  margin-bottom: 10px;
}

.filter-toolbar {
  margin-bottom: 10px;
}
</style>
