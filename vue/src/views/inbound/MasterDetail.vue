<template>
  <MasterDetailBase
    :config="config"
    :apis="apis"
  >
    <template #master-buttons>
      <el-button
        type="primary"
        icon="el-icon-plus"
        @click="handleAdd"
        :loading="masterLoading"
      >
        新增
      </el-button>
      <el-button
        type="warning"
        :disabled="!canSubmit"
        @click="handleSubmit"
        :loading="masterLoading"
      >
        提交过账
      </el-button>
      <el-button
        type="danger"
        :disabled="!canCancel"
        @click="handleCancel"
        :loading="masterLoading"
      >
        作废
      </el-button>
      <el-button
        type="default"
        icon="el-icon-setting"
        @click="handleColumnConfig"
        class="column-config-btn"
      >
        列配置
      </el-button>
    </template>

    <!-- 单据号可点击 -->
    <template #formNo="{ row }">
      <a @click="handleMasterRowClick(row)">{{ row.form_no }}</a>
    </template>

    <!-- 状态标签 -->
    <template #status="{ row }">
      <el-tag :type="getStatusTagType(row.status)" size="small">
        {{ getStatusText(row.status) }}
      </el-tag>
    </template>

    <!-- 需求数右对齐 -->
    <template #detail-requiredQty="{ row }">
      <div style="text-align: right;">{{ row.required_qty }}</div>
    </template>
  </MasterDetailBase>
</template>

<script>
import MasterDetailBase from '@/components/MasterDetailBase'
import {
  getInboundMasterList,
  getInboundDetailList,
  submitInbound,
  cancelInbound,
  createInbound
} from '@/api/inbound/index'

// 权限定义
const submitPermission = '/wms-platform/view/inbound/submit'
const cancelPermission = '/wms-platform/view/inbound/cancel'

export default {
  name: 'InboundMasterDetail',

  components: { MasterDetailBase },

  data() {
    return {
      // 配置对象
      config: {
        masterTableName: 'inboundMasterTable',
        detailSearchFields: [
          { prop: 'asnNo', label: 'ASN单据号', placeholder: '请输入ASN单据号', disableOnNoMaster: true },
          { prop: 'skuCode', label: 'SKU编码', placeholder: '请输入SKU编码' },
          { prop: 'batchNo', label: '批次', placeholder: '请输入批次' }
        ],
        masterDefaultParams: {
          formNo: '',
          sourceOrderNo: '',
          status: '1'
        },
        masterColumns: [
          { type: 'selection', width: 55, fixed: 'left' },
          { label: '仓库', prop: 'warehouse_code', width: 100, fixed: 'left' },
          { label: '来源业务单据号', prop: 'source_order_no', width: 150 },
          { label: '来源业务单据类型', prop: 'source_order_type', width: 150 },
          { label: '单据类型', prop: 'form_type', width: 120 },
          { label: '单据状态', prop: 'status', width: 100, scopedSlot: 'status' },
          { label: '收货开始时间', prop: 'receive_start_time', width: 160 },
          { label: '收货结束时间', prop: 'receive_end_time', width: 160 },
          { label: '供应商代码', prop: 'supplier_code', width: 120 },
          { label: '供应商名称', prop: 'supplier_name', width: 150 },
          { label: '作废人', prop: 'cancel_user', width: 100 },
          { label: '操作', key: 'action', width: 100, options: [
            { label: '查看', command: 'view' },
            { label: '编辑', command: 'edit' }
          ]}
        ],
        detailColumns: [
          { label: '行号', prop: 'line_no', width: 80 },
          { label: 'ASN单据号', prop: 'asn_no', width: 150 },
          { label: '业务明细号', prop: 'business_detail_no', width: 150 },
          { label: '存货编码', prop: 'inventory_code', width: 120 },
          { label: 'SKU编码', prop: 'sku_code', width: 120 },
          { label: 'SKU名称', prop: 'sku_name', width: 150 },
          { label: '批次', prop: 'batch_no', width: 120 },
          { label: '工厂', prop: 'factory', width: 100 },
          { label: '库存地点', prop: 'storage_location', width: 120 },
          { label: '需求数', prop: 'required_qty', width: 100, scopedSlot: 'requiredQty' }
        ]
      },

      // API对象
      apis: {
        getMasterList: getInboundMasterList,
        getDetailList: getInboundDetailList
      }
    }
  },

  computed: {
    canSubmit() {
      const rows = this.masterSelectionRows
      if (!rows.length) return false
      return rows.every(row => row.status === 1)
    },
    canCancel() {
      const rows = this.masterSelectionRows
      if (!rows.length) return false
      return rows.every(row => row.status === 1 || row.status === 2)
    },
    masterLoading() {
      const base = this.$children[0]
      return base ? base.masterLoading : false
    },
    masterSelectionRows() {
      const base = this.$children[0]
      return base ? base.masterSelectionRows : []
    },
    masterSelectionKeys() {
      const base = this.$children[0]
      return base ? base.masterSelectionKeys : []
    },
    selectedMasterRow() {
      const base = this.$children[0]
      return base ? base.selectedMasterRow : null
    }
  },

  methods: {
    handleMasterRowClick(row) {
      const base = this.$children[0]
      if (base) base.setSelectedRow(row)
    },

    getStatusTagType(status) {
      const map = {
        1: 'info',
        2: 'warning',
        3: 'primary',
        4: 'success',
        5: 'danger'
      }
      return map[status] || 'info'
    },

    getStatusText(status) {
      const map = {
        1: '已创建',
        2: '待审核',
        3: '执行中',
        4: '已完成',
        5: '已作废'
      }
      return map[status] || '未知'
    },

    handleAdd() {
      createInbound({}).then(() => {
        this.$message.success('创建成功')
        this.refreshMaster()
      }).catch(error => {
        console.error('创建单据失败:', error)
        this.$message.error('创建失败: ' + (error.message || '未知错误'))
      })
    },

    handleSubmit() {
      const base = this.$children[0]
      this.$confirm('确定要提交选中的单据吗？', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then(() => {
        return submitInbound({ ids: base.masterSelectionKeys })
      }).then(() => {
        this.$message.success('提交成功')
        base.masterSelectionKeys = []
        base.masterSelectionRows = []
        base.refreshMaster()
      }).catch(() => {})
    },

    handleCancel() {
      const base = this.$children[0]
      this.$confirm('确定要作废选中的单据吗？', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then(() => {
        return cancelInbound({ ids: base.masterSelectionKeys })
      }).then(() => {
        this.$message.success('作废成功')
        base.masterSelectionKeys = []
        base.masterSelectionRows = []
        base.refreshMaster()
      }).catch(() => {})
    },

    handleColumnConfig() {
      this.$message.info('列配置功能开发中')
    },

    refreshMaster() {
      const base = this.$children[0]
      if (base) base.refreshMaster()
    }
  }
}
</script>

<style lang="scss" scoped>
.column-config-btn {
  float: right;
}
</style>
