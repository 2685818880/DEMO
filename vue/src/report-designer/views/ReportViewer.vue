<template>
  <div class="report-viewer">
    <div class="viewer-header">
      <h2>{{ reportName }}</h2>
      <div class="header-actions">
        <el-button icon="el-icon-edit" size="small" @click="handleDesign" v-if="reportCode">
          设计
        </el-button>
        <el-button icon="el-icon-download" size="small" type="primary" @click="handleExport" :disabled="!exportEnabled">
          导出Excel
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div v-if="filtersConfig.length" class="filter-section">
      <el-form :inline="true" size="small" class="filter-form">
        <el-form-item
          v-for="f in filtersConfig"
          :key="f.prop"
          :label="f.label"
          :required="f.required"
        >
          <el-input
            v-if="f.type === 'string' || f.type === '' || !f.type"
            v-model="searchParams[f.prop]"
            :placeholder="'请输入' + f.label"
            style="width: 160px"
            clearable
          />
          <el-input-number
            v-else-if="f.type === 'number'"
            v-model="searchParams[f.prop]"
            :placeholder="'请输入' + f.label"
            style="width: 160px"
          />
          <el-date-picker
            v-else-if="f.type === 'datePicker'"
            v-model="searchParams[f.prop]"
            :placeholder="'请选择' + f.label"
            style="width: 160px"
            value-format="yyyy-MM-dd"
          />
          <el-date-picker
            v-else-if="f.type === 'dateRange'"
            v-model="searchParams[f.prop]"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 220px"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          <el-button @click="handleReset" :loading="loading">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图表 -->
    <div v-if="hasChart && chartData.length" class="chart-section">
      <div ref="chart" class="chart-container" style="width: 100%; height: 400px"></div>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <div v-if="executionTime" class="execution-info">
        查询耗时: {{ executionTime }}ms | 总记录: {{ total }}
      </div>
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        size="small"
        max-height="700"
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column
          v-for="col in visibleColumns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label || col.prop"
          :width="col.width"
          :align="col.align || 'left'"
          :sortable="col.sortable"
          :show-overflow-tooltip="true"
          :formatter="getFormatter(col)"
        />
      </el-table>

      <div v-if="paginationEnabled" class="pagination-wrapper">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          :current-page.sync="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>

      <div v-if="!tableData.length && !loading" class="empty-tip">
        <el-empty description="暂无数据，请点击查询" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script>
import { getReportConfigDetail, executeReport, exportReport } from '@/report-designer/api/reportConfig'
import echarts from 'echarts'

export default {
  name: 'ReportViewer',
  data: function() {
    return {
      reportName: '',
      reportCode: '',
      reportType: 'TABLE',
      columnsConfig: [],
      filtersConfig: [],
      paginationEnabled: true,
      pageSize: 20,
      exportEnabled: false,
      exportFileName: '',

      searchParams: {},
      tableData: [],
      chartData: [],
      loading: false,
      currentPage: 1,
      total: 0,
      executionTime: 0,

      chartInstance: null,
      chartConfig: null
    }
  },
  computed: {
    visibleColumns: function() {
      var self = this
      return this.columnsConfig.filter(function(c) {
        return !c.hidden
      })
    },
    hasChart: function() {
      return this.reportType === 'CHART' || this.reportType === 'MIXED'
    }
  },
  created: function() {
    var code = this.$route.params.code
    if (code) {
      this.reportCode = code
      this.loadConfig(code)
    }
  },
  mounted: function() {
    // chart will be initialized after data load
  },
  beforeDestroy: function() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
  },
  methods: {
    loadConfig: function(code) {
      var self = this
      getReportConfigDetail(code).then(function(res) {
        var config = res.data || res
        self.reportName = config.reportName || ''
        self.reportType = config.reportType || 'TABLE'

        if (config.configJson) {
          try {
            var json = JSON.parse(config.configJson)
            self.columnsConfig = json.columns || []
            self.filtersConfig = json.searchParams || []
            self.paginationEnabled = json.pagination ? json.pagination.enabled !== false : true
            self.pageSize = (json.pagination && json.pagination.pageSize) || 20
            self.exportEnabled = json.export ? json.export.enabled !== false : true
            self.exportFileName = (json.export && json.export.fileName) || ''
            if (json.charts && json.charts.length) {
              self.chartConfig = json.charts[0]
            }

            // 初始化查询参数默认值
            var initParams = {}
            if (self.filtersConfig.length) {
              self.filtersConfig.forEach(function(f) {
                initParams[f.prop] = f.defaultValue !== undefined ? f.defaultValue : ''
              })
            }
            self.searchParams = initParams

            // 自动查询
            self.handleSearch()
          } catch (e) {
            console.error('解析配置失败', e)
          }
        }
      }).catch(function(e) {
        console.error('加载报表配置失败', e)
        self.$message.error('加载报表配置失败')
      })
    },

    handleSearch: function() {
      var self = this
      this.loading = true
      this.currentPage = 1

      executeReport(this.reportCode, {
        params: this.searchParams,
        page: this.currentPage,
        pageSize: this.pageSize
      }).then(function(res) {
        var data = res.data || res
        self.tableData = data.rows || []
        self.total = data.total || 0
        self.executionTime = data.executionTimeMs || 0
        self.chartData = data.rows || []

        if (self.hasChart && self.chartData.length && self.chartConfig) {
          self.$nextTick(function() {
            self.renderChart()
          })
        }
        self.loading = false
      }).catch(function(e) {
        console.error('查询失败', e)
        self.$message.error('查询失败: ' + (e.message || '执行错误'))
        self.loading = false
      })
    },

    handleReset: function() {
      var self = this
      var initParams = {}
      if (this.filtersConfig.length) {
        this.filtersConfig.forEach(function(f) {
          initParams[f.prop] = f.defaultValue !== undefined ? f.defaultValue : ''
        })
      }
      this.searchParams = initParams
      this.handleSearch()
    },

    handleSizeChange: function(val) {
      this.pageSize = val
      this.handleSearch()
    },

    handlePageChange: function(val) {
      var self = this
      this.currentPage = val
      this.loading = true

      executeReport(this.reportCode, {
        params: this.searchParams,
        page: this.currentPage,
        pageSize: this.pageSize
      }).then(function(res) {
        var data = res.data || res
        self.tableData = data.rows || []
        self.total = data.total || 0
        self.executionTime = data.executionTimeMs || 0
        self.loading = false
      }).catch(function(e) {
        console.error('分页查询失败', e)
        self.loading = false
      })
    },

    handleSortChange: function(sort) {
      console.log('sort changed:', sort)
    },

    getFormatter: function(col) {
      var self = this
      if (col.format === 'number') {
        return function(row) {
          var val = row[col.prop]
          if (val == null) return ''
          return Number(val).toLocaleString()
        }
      }
      if (col.format === 'decimal2') {
        return function(row) {
          var val = row[col.prop]
          if (val == null) return ''
          return Number(val).toFixed(2)
        }
      }
      return null
    },

    renderChart: function() {
      var chartDom = this.$refs.chart
      if (!chartDom) return

      if (this.chartInstance) {
        this.chartInstance.dispose()
      }

      this.chartInstance = echarts.init(chartDom)

      var cfg = this.chartConfig
      var data = this.chartData

      if (cfg.chartType === 'pie') {
        var pieData = []
        data.forEach(function(row) {
          if (row[cfg.dimension] != null && row[cfg.metric] != null) {
            pieData.push({
              name: String(row[cfg.dimension]),
              value: Number(row[cfg.metric])
            })
          }
        })

        this.chartInstance.setOption({
          title: { text: cfg.title || '', left: 'center' },
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          series: [{
            type: 'pie',
            radius: '55%',
            data: pieData,
            emphasis: {
              itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
            }
          }]
        })
      } else {
        var categories = []
        var values = []
        var sortedData = [].concat(data)

        if (cfg.sortBy) {
          sortedData.sort(function(a, b) {
            return (Number(b[cfg.sortBy]) || 0) - (Number(a[cfg.sortBy]) || 0)
          })
        }

        if (cfg.limit) {
          sortedData = sortedData.slice(0, cfg.limit)
        }

        sortedData.forEach(function(row) {
          categories.push(row[cfg.dimension] != null ? String(row[cfg.dimension]) : '')
          values.push(Number(row[cfg.metric]) || 0)
        })

        var option = {
          title: { text: cfg.title || '', left: 'center' },
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: categories, axisLabel: { rotate: 45 } },
          yAxis: { type: 'value' },
          series: [{
            type: cfg.chartType || 'bar',
            data: values,
            itemStyle: cfg.chartType === 'line' ? {} : {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#409EFF' },
                { offset: 1, color: '#7ec1ff' }
              ])
            }
          }]
        }

        this.chartInstance.setOption(option)
      }

      window.addEventListener('resize', function() {
        if (self.chartInstance) {
          self.chartInstance.resize()
        }
      })
    },

    handleDesign: function() {
      this.$router.push('/report-designer/design/' + this.reportCode)
    },

    handleExport: function() {
      var self = this
      exportReport(this.reportCode, this.searchParams).then(function(data) {
        // 检查 ArrayBuffer 内容是否包含 JSON 错误
        if (data.byteLength < 2000) {
          var checkBytes = new Uint8Array(data, 0, Math.min(data.byteLength, 200))
          var prefix = ''
          for (var i = 0; i < checkBytes.length; i++) {
            prefix += String.fromCharCode(checkBytes[i])
          }
          var trimmed = prefix.trim()
          if ((trimmed.startsWith('{') || trimmed.startsWith('[')) && trimmed.indexOf('"message"') !== -1) {
            try {
              var err = JSON.parse(trimmed.substring(0, trimmed.indexOf('}') + 1))
              self.$message.error(err.message || '导出失败')
              return
            } catch (ignored) {}
          }
        }

        var blob = new Blob([data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        var url = window.URL.createObjectURL(blob)
        var link = document.createElement('a')
        link.href = url
        link.setAttribute('download', (self.exportFileName || self.reportName || 'report') + '.xlsx')
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      }).catch(function(e) {
        console.error('导出失败', e)
        self.$message.error('导出失败: ' + (e.message || '未知错误'))
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.report-viewer {
  padding: 20px;

  .viewer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }
  }

  .filter-section {
    background: #fff;
    padding: 12px 16px;
    border-radius: 4px;
    border: 1px solid #e6ebf5;
    margin-bottom: 16px;

    .filter-form {
      margin-bottom: 0;
    }
  }

  .chart-section {
    background: #fff;
    padding: 16px;
    border-radius: 4px;
    border: 1px solid #e6ebf5;
    margin-bottom: 16px;

    .chart-container {
      min-height: 400px;
    }
  }

  .table-section {
    background: #fff;
    padding: 16px;
    border-radius: 4px;
    border: 1px solid #e6ebf5;

    .execution-info {
      font-size: 12px;
      color: #909399;
      margin-bottom: 8px;
    }

    .pagination-wrapper {
      margin-top: 16px;
      text-align: right;
    }

    .empty-tip {
      padding: 40px 0;
      text-align: center;
    }
  }
}
</style>
