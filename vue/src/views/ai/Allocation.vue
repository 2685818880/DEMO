<template>
  <div class="allocation">
    <h2 style="margin-bottom: 20px;">库存调拨</h2>

    <el-card style="margin-bottom: 20px;">
      <el-form :model="form" label-width="120px" inline>
        <el-form-item label="源仓库">
          <el-select v-model="form.sourceWarehouseId" placeholder="选择源仓库" style="width: 180px;">
            <el-option v-for="w in warehouses" :key="w.house_code"
                       :label="w.warehouse_name" :value="w.house_code" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标仓库">
          <el-select v-model="form.targetWarehouseId" placeholder="选择目标仓库" style="width: 180px;">
            <el-option v-for="w in warehouses" :key="w.house_code"
                       :label="w.warehouse_name" :value="w.house_code" />
          </el-select>
        </el-form-item>
        <el-form-item label="SKU编码">
          <el-input v-model="form.skuCode" placeholder="输入SKU编码" style="width: 160px;" />
        </el-form-item>
        <el-form-item label="调拨数量">
          <el-input-number v-model="form.quantity" :min="1" :max="99999" style="width: 160px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleAllocate">执行调拨</el-button>
          <el-button @click="handleClear">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result">
      <div slot="header">
        <span>调拨结果</span>
        <el-tag :type="result.status === 'COMPLETED' ? 'success' : 'warning'" style="float: right;">
          {{ result.status === 'COMPLETED' ? '已完成' : '处理中' }}
        </el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="调拨单号">{{ result.allocationNo || '--' }}</el-descriptions-item>
        <el-descriptions-item label="SKU编码">{{ result.skuCode || '--' }}</el-descriptions-item>
        <el-descriptions-item label="源仓库">{{ result.sourceWarehouse || '--' }}</el-descriptions-item>
        <el-descriptions-item label="目标仓库">{{ result.targetWarehouse || '--' }}</el-descriptions-item>
        <el-descriptions-item label="调拨数量">{{ result.quantity || 0 }}</el-descriptions-item>
        <el-descriptions-item label="预计完成">{{ result.estimatedCompletion || '--' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="result.suggestions && result.suggestions.length" style="margin-top: 16px;">
        <h4>调拨建议</h4>
        <ul>
          <li v-for="(s, i) in result.suggestions" :key="i">{{ s }}</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getWarehouses, executeAllocation } from '@/api/ai'

export default {
  name: 'Allocation',
  data() {
    return {
      loading: false,
      warehouses: [],
      form: {
        sourceWarehouseId: '',
        targetWarehouseId: '',
        skuCode: '',
        quantity: 1
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
    async handleAllocate() {
      if (!this.form.sourceWarehouseId) {
        this.$message.warning('请选择源仓库')
        return
      }
      if (!this.form.targetWarehouseId) {
        this.$message.warning('请选择目标仓库')
        return
      }
      if (this.form.sourceWarehouseId === this.form.targetWarehouseId) {
        this.$message.warning('源仓库和目标仓库不能相同')
        return
      }
      if (!this.form.skuCode) {
        this.$message.warning('请输入SKU编码')
        return
      }
      this.loading = true
      try {
        const res = await executeAllocation(this.form)
        if (res.success) {
          this.result = res.data
        }
      } catch (e) {
        console.error('调拨请求失败', e)
        this.$message.error('调拨执行失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    handleClear() {
      this.result = null
      this.form = { sourceWarehouseId: '', targetWarehouseId: '', skuCode: '', quantity: 1 }
    }
  }
}
</script>

<style scoped>
.allocation {
  padding: 20px;
}
</style>
