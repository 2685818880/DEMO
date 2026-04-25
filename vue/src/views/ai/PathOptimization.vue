<template>
  <div class="path-optimization">
    <h2 style="margin-bottom: 20px;">路径优化</h2>

    <el-card style="margin-bottom: 20px;">
      <el-form :model="form" label-width="120px" inline>
        <el-form-item label="仓库">
          <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width: 180px;" @change="onWarehouseChange">
            <el-option
              v-for="wh in warehouses"
              :key="wh.house_code"
              :label="wh.warehouse_name ? wh.house_code + ' - ' + wh.warehouse_name : wh.house_code"
              :value="wh.house_code" />
          </el-select>
        </el-form-item>
        <el-form-item label="优化目标">
          <el-select v-model="form.goal" placeholder="选择优化目标" style="width: 180px;">
            <el-option label="最短距离" value="MINIMIZE_DISTANCE" />
            <el-option label="最短时间" value="MINIMIZE_TIME" />
            <el-option label="最低能耗" value="MINIMIZE_ENERGY" />
            <el-option label="均衡模式" value="BALANCED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleOptimize">开始优化</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <h4>拣货任务</h4>
      <el-button size="mini" type="primary" @click="addTask">添加任务</el-button>
      <el-table :data="form.tasks" size="small" style="margin-top: 10px;">
        <el-table-column label="起始货位" min-width="160">
          <template slot-scope="scope">
            <el-autocomplete
              v-model="scope.row.fromLocation"
              :fetch-suggestions="queryLocations"
              placeholder="输入起始货位"
              style="width: 140px;"
              size="mini"
            />
          </template>
        </el-table-column>
        <el-table-column label="目标货位" min-width="160">
          <template slot-scope="scope">
            <el-autocomplete
              v-model="scope.row.toLocation"
              :fetch-suggestions="queryLocations"
              placeholder="输入目标货位"
              style="width: 140px;"
              size="mini"
            />
          </template>
        </el-table-column>
        <el-table-column label="SKU" prop="skuCode" min-width="120">
          <template slot-scope="scope">
            <el-input v-model="scope.row.skuCode" size="mini" placeholder="SKU编码" />
          </template>
        </el-table-column>
        <el-table-column label="数量" width="100">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" size="mini" style="width: 80px;" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
            <el-button size="mini" type="danger" @click="removeTask(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="result">
      <div slot="header">
        <span>优化结果</span>
      </div>

      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <div class="metric-card">
            <p class="metric-label">总距离</p>
            <p class="metric-value">{{ result.totalDistance }} m</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="metric-card">
            <p class="metric-label">预计时间</p>
            <p class="metric-value">{{ formatTime(result.totalTime) }}</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="metric-card">
            <p class="metric-label">途径节点</p>
            <p class="metric-value">{{ result.path ? result.path.length : 0 }} 个</p>
          </div>
        </el-col>
      </el-row>

      <h4>优化路径</h4>
      <el-steps :active="result.path ? result.path.length : 0" direction="vertical">
        <el-step v-for="(node, i) in result.path" :key="i"
                 :title="'节点 ' + (i + 1)"
                 :description="node" />
      </el-steps>

      <div v-if="result.improvements && result.improvements.length" style="margin-top: 20px;">
        <h4>优化建议</h4>
        <ul>
          <li v-for="(imp, i) in result.improvements" :key="i">{{ imp }}</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script>
import { optimizePath, getWarehouses, getLocations } from '@/api/ai'

export default {
  name: 'PathOptimization',
  data() {
    return {
      loading: false,
      warehouses: [],
      locations: [],
      form: {
        warehouseId: '',
        goal: 'MINIMIZE_DISTANCE',
        tasks: []
      },
      result: null
    }
  },
  mounted() {
    this.loadWarehouses()
  },
  methods: {
    async loadWarehouses() {
      try {
        const res = await getWarehouses()
        if (res.success && res.data) {
          this.warehouses = res.data
        }
      } catch (e) {
        console.error('加载仓库列表失败', e)
      }
    },
    async loadLocations() {
      try {
        const res = await getLocations(this.form.warehouseId || '%')
        if (res.success && res.data) {
          this.locations = res.data.map(loc => ({
            value: loc.loc_no,
            ...loc
          }))
        }
      } catch (e) {
        console.error('加载货位列表失败', e)
      }
    },
    onWarehouseChange() {
      this.form.tasks = []
      this.result = null
      this.loadLocations()
    },
    queryLocations(queryString, cb) {
      const results = queryString
        ? this.locations.filter(loc => loc.value.indexOf(queryString) >= 0)
        : this.locations
      cb(results.slice(0, 20))
    },
    addTask() {
      this.form.tasks.push({ fromLocation: '', toLocation: '', skuCode: '', quantity: 1 })
    },
    removeTask(index) {
      this.form.tasks.splice(index, 1)
    },
    async handleOptimize() {
      if (!this.form.warehouseId || !this.form.tasks.length) {
        this.$message.warning('请填写仓库并添加拣货任务')
        return
      }
      this.loading = true
      try {
        const res = await optimizePath(this.form)
        if (res.success) {
          this.result = res.data
        }
      } catch (e) {
        console.error('路径优化请求失败', e)
      } finally {
        this.loading = false
      }
    },
    formatTime(minutes) {
      if (!minutes && minutes !== 0) return '-'
      const h = Math.floor(minutes / 60)
      const m = minutes % 60
      return h > 0 ? h + '时' + m + '分' : m + '分钟'
    }
  }
}
</script>

<style scoped>
.path-optimization {
  padding: 20px;
}
.metric-card {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}
.metric-label {
  color: #909399;
  font-size: 13px;
  margin: 0 0 8px 0;
}
.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin: 0;
}
</style>
