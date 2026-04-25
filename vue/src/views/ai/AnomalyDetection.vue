<template>
  <div class="anomaly-detection">
    <h2 style="margin-bottom: 20px;">异常检测</h2>

    <el-card style="margin-bottom: 20px;">
      <el-form :model="form" label-width="100px" inline>
        <el-form-item label="仓库">
          <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width: 180px;">
            <el-option v-for="w in warehouses" :key="w.house_code"
                       :label="w.warehouse_name" :value="w.house_code" />
          </el-select>
        </el-form-item>
        <el-form-item label="检测类型">
          <el-select v-model="form.detectionType" placeholder="选择类型" style="width: 180px;">
            <el-option label="库存异常" value="INVENTORY" />
            <el-option label="路径异常" value="PATH" />
            <el-option label="操作异常" value="OPERATION" />
            <el-option label="全部" value="ALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="开始时间"
                          value-format="yyyy-MM-dd HH:mm:ss" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="结束时间"
                          value-format="yyyy-MM-dd HH:mm:ss" style="width: 180px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleDetect">开始检测</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result">
      <div slot="header">
        <span>检测结果</span>
        <el-tag :type="result.totalCount > 0 ? 'danger' : 'success'" style="float: right;">
          发现 {{ result.totalCount }} 项异常
        </el-tag>
      </div>

      <el-table v-if="result.anomalies && result.anomalies.length" :data="result.anomalies" size="small">
        <el-table-column label="类型" prop="type" width="160" />
        <el-table-column label="描述" prop="description" min-width="200" />
        <el-table-column label="严重程度" width="100">
          <template slot-scope="scope">
            <el-tag :type="severityType(scope.row.severity)" size="mini">
              {{ scope.row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置信度" width="100">
          <template slot-scope="scope">
            {{ (scope.row.score * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column label="时间" prop="timestamp" width="170" />
      </el-table>

      <el-empty v-else description="未检测到异常" />
    </el-card>
  </div>
</template>

<script>
import {getWarehouses, detectAnomalies} from '@/api/ai'

export default {
  name: 'AnomalyDetection',
  data() {
    return {
      loading: false,
      warehouses: [],
      form: {
        warehouseId: '',
        detectionType: 'ALL',
        startTime: '',
        endTime: ''
      },
      result: null
    }
  },
  created() {
    this.loadWarehouses()
  },
  methods: {
    async loadWarehouses() {
      try {
        const res = await getWarehouses()
        if (res.success) this.warehouses = res.data || []
      } catch (e) {
        console.error('加载仓库列表失败', e)
      }
    },
    async handleDetect() {
      if (!this.form.warehouseId) {
        this.$message.warning('请选择仓库')
        return
      }
      this.loading = true
      try {
        const res = await detectAnomalies(this.form)
        if (res.success) {
          this.result = res.data
        }
      } catch (e) {
        console.error('异常检测请求失败', e)
      } finally {
        this.loading = false
      }
    },
    severityType(severity) {
      if (!severity) return 'info'
      if (severity === 'HIGH') return 'danger'
      if (severity === 'MEDIUM') return 'warning'
      return 'info'
    }
  }
}
</script>

<style scoped>
.anomaly-detection {
  padding: 20px;
}
</style>
