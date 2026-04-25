<template>
  <div class="inventory-prediction">
    <h2 style="margin-bottom: 20px;">库存预测</h2>

    <el-card style="margin-bottom: 20px;">
      <el-form :model="form" label-width="100px" inline>
        <el-form-item label="仓库">
          <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width: 180px;">
            <el-option v-for="w in warehouses" :key="w.house_code"
                       :label="w.warehouse_name" :value="w.house_code" />
          </el-select>
        </el-form-item>
        <el-form-item label="SKU编码">
          <el-input v-model="form.skuCode" placeholder="输入SKU编码" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期"
                          value-format="yyyy-MM-dd" style="width: 160px;" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期"
                          value-format="yyyy-MM-dd" style="width: 160px;" />
        </el-form-item>
        <el-form-item label="粒度">
          <el-select v-model="form.granularity" placeholder="预测粒度" style="width: 120px;">
            <el-option label="日" value="daily" />
            <el-option label="周" value="weekly" />
            <el-option label="月" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handlePredict">开始预测</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result">
      <div slot="header">
        <span>预测结果</span>
        <el-tag :type="confidenceLevel" style="float: right;">
          置信度: {{ (result.confidence * 100).toFixed(1) }}%
        </el-tag>
      </div>

      <div ref="chart" style="width: 100%; height: 400px;"></div>

      <div v-if="result.suggestions && result.suggestions.length" style="margin-top: 20px;">
        <h4>建议</h4>
        <ul>
          <li v-for="(s, i) in result.suggestions" :key="i">{{ s }}</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script>
import {predictInventory, getWarehouses} from '@/api/ai'
import echarts from 'echarts'

export default {
  name: 'InventoryPrediction',
  data() {
    return {
      loading: false,
      warehouses: [],
      form: {
        warehouseId: '',
        skuCode: '',
        startDate: '',
        endDate: '',
        granularity: 'daily'
      },
      result: null,
      chart: null
    }
  },
  computed: {
    confidenceLevel() {
      if (!this.result) return 'info'
      const c = this.result.confidence
      if (c >= 0.8) return 'success'
      if (c >= 0.6) return 'warning'
      return 'danger'
    }
  },
  watch: {
    result() {
      this.$nextTick(() => this.renderChart())
    }
  },
  created() {
    this.loadWarehouses()
  },
  methods: {
    async loadWarehouses() {
      try {
        const res = await getWarehouses()
        if (res.success) {
          this.warehouses = res.data
        }
      } catch (e) {
        console.error('加载仓库列表失败', e)
      }
    },
    async handlePredict() {
      if (!this.form.warehouseId || !this.form.skuCode) {
        this.$message.warning('请填写仓库和SKU编码')
        return
      }
      if (!this.form.startDate) {
        this.$message.warning('请选择开始日期')
        return
      }
      if (!this.form.endDate) {
        this.$message.warning('请选择结束日期')
        return
      }
      this.loading = true
      try {
        const res = await predictInventory(this.form)
        if (res.success) {
          this.result = res.data
        }
      } catch (e) {
        console.error('预测请求失败', e)
      } finally {
        this.loading = false
      }
    },
    renderChart() {
      if (!this.result || !this.result.predictions) return
      if (this.chart) this.chart.dispose()

      this.chart = echarts.init(this.$refs.chart)
      const dates = this.result.predictions.map(p => p.date)
      const values = this.result.predictions.map(p => p.value)
      const upper = this.result.predictions.map(p => p.upperBound)
      const lower = this.result.predictions.map(p => p.lowerBound)

      this.chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['预测值', '上限', '下限'] },
        xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45 } },
        yAxis: { type: 'value' },
        series: [
          {
            name: '预测值',
            type: 'line',
            data: values,
            smooth: true,
            lineStyle: { width: 2 },
            itemStyle: { color: '#409eff' }
          },
          {
            name: '上限',
            type: 'line',
            data: upper,
            smooth: true,
            lineStyle: { width: 1, type: 'dashed' },
            itemStyle: { color: '#67c23a' }
          },
          {
            name: '下限',
            type: 'line',
            data: lower,
            smooth: true,
            lineStyle: { width: 1, type: 'dashed' },
            itemStyle: { color: '#f56c6c' }
          }
        ]
      })
    }
  },
  beforeDestroy() {
    if (this.chart) this.chart.dispose()
  }
}
</script>
