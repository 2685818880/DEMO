<template>
  <div class="scheduling">
    <h2 style="margin-bottom: 20px;">库存排班</h2>

    <el-card style="margin-bottom: 20px;">
      <el-form :model="form" label-width="100px" inline>
        <el-form-item label="仓库">
          <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width: 180px;">
            <el-option v-for="w in warehouses" :key="w.house_code"
                       :label="w.warehouse_name" :value="w.house_code" />
          </el-select>
        </el-form-item>
        <el-form-item label="排班日期">
          <el-date-picker v-model="form.scheduleDate" type="date" placeholder="选择日期"
                          value-format="yyyy-MM-dd" style="width: 160px;" />
        </el-form-item>
        <el-form-item label="班次">
          <el-select v-model="form.shift" placeholder="选择班次" style="width: 140px;">
            <el-option label="早班" value="MORNING" />
            <el-option label="中班" value="AFTERNOON" />
            <el-option label="晚班" value="NIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleGenerate">生成排班</el-button>
          <el-button @click="handleClear">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result">
      <div slot="header">
        <span>排班结果</span>
        <el-tag type="success" style="float: right;" v-if="result.shifts">
          {{ result.shifts.length }} 个班次
        </el-tag>
      </div>

      <el-table v-if="result.shifts && result.shifts.length" :data="result.shifts" size="small">
        <el-table-column label="日期" prop="date" width="120" />
        <el-table-column label="班次" width="100">
          <template slot-scope="scope">
            <el-tag :type="shiftTagType(scope.row.shift)" size="mini">
              {{ scope.row.shift === 'MORNING' ? '早班' : scope.row.shift === 'AFTERNOON' ? '中班' : '晚班' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="人员" prop="staff" min-width="200" />
        <el-table-column label="岗位" prop="position" width="120" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'CONFIRMED' ? 'success' : 'warning'" size="mini">
              {{ scope.row.status === 'CONFIRMED' ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="无可用的排班数据" />

      <div v-if="result.suggestions && result.suggestions.length" style="margin-top: 16px;">
        <h4>排班建议</h4>
        <ul>
          <li v-for="(s, i) in result.suggestions" :key="i">{{ s }}</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getWarehouses, generateSchedule } from '@/api/ai'

export default {
  name: 'Scheduling',
  data() {
    return {
      loading: false,
      warehouses: [],
      form: {
        warehouseId: '',
        scheduleDate: '',
        shift: ''
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
    async handleGenerate() {
      if (!this.form.warehouseId) {
        this.$message.warning('请选择仓库')
        return
      }
      if (!this.form.scheduleDate) {
        this.$message.warning('请选择排班日期')
        return
      }
      this.loading = true
      try {
        const res = await generateSchedule(this.form)
        if (res.success) {
          this.result = res.data
        }
      } catch (e) {
        console.error('排班请求失败', e)
        this.$message.error('排班生成失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    handleClear() {
      this.result = null
      this.form = { warehouseId: '', scheduleDate: '', shift: '' }
    },
    shiftTagType(shift) {
      if (shift === 'MORNING') return 'warning'
      if (shift === 'AFTERNOON') return 'primary'
      if (shift === 'NIGHT') return 'info'
      return ''
    }
  }
}
</script>

<style scoped>
.scheduling {
  padding: 20px;
}
</style>
