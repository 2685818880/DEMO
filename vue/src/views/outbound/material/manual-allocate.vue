<template>
  <drawer-modal
    :visible.sync="visible"
    :title="i18n('手动分配')"
    width="80%"
    :loading="loading"
    @ok="handleSubmit"
    @close="handleCancel"
  >
    <resize-layout :height="modalTableHeight">
      <resize-col>
        <search-form
          :fields="searchFields"
          :fields-value="params"
          @enter="handleSearch"
          @reset="handleReset"/>
        <dynamic-table
          ref="tableSku"
          v-loading="loading"
          :selectionIds.sync="selectionKeys"
          :tableName="tableName"
          :columns="columns"
          :data="list"
          @select="handleTableSelect"
        >
          <template v-slot:allot_qty="{ row }">
            <el-input-number :disabled="false" v-model="row.allot_qty" :min="0" :max="row.available_qty"/>
          </template>
          <div slot="tools" style="display: flex;height: 100%;align-items: center">
            需求数量：{{ actQty }} 分配数量：{{ allocationNumber }}
          </div>
        </dynamic-table>
      </resize-col>
    </resize-layout>
  </drawer-modal>
</template>

<script>
import { pagedSearchMixin, formMixin } from '@/mixins'
import { manualAllocationPagedUrl, submitAllocation } from '@/api/outbound'
import fetch from '@/utils/fetch'
import { getEnumLabel } from '@/utils/helper'

export default {
  name: 'ManualAllocate',

  mixins: [pagedSearchMixin, formMixin],

  data() {
    return {
      tableName: 'manualskutable',
      pagedUrl: manualAllocationPagedUrl,
      searchFields: [
        // {
        //   label: '存货分类编号',
        //   prop: 'category_code'
        // },
        // {
        //   label: '存货分类名称',
        //   prop: 'category_name'
        // },
        {
          label: 'sku编号',
          prop: 'sku_code',
          props: {
            readonly: true
          }
        },
        {
          label: 'sku名称',
          prop: 'sku_name',
          props: {
            readonly: true
          }
        },
        {
          label: '物料SN',
          prop: 'serial_no'
        },
        {
          label: '托盘号',
          prop: 'container_code'
        },
        {
          label: '批次号',
          prop: 'batch_no'
        }
      ],
      columns: [
        {
          label: '操作',
          type: 'selection',
          key: 'selection',
          attrs: {
            fixed: 'left'
          }
        },
        {
          label: '存货分类',
          key: 'category_name',
          prop: 'category_name',
          width: 130
        },
        {
          label: '仓库号',
          prop: 'house_code',
          key: 'house_code'
        },
        {
          label: '托盘号',
          key: 'container_code',
          prop: 'container_code',
          width: 140
        },
        {
          label: '包装等级',
          prop: 'package_level',
          key: 'package_level',
          format(val) {
            return getEnumLabel('PackageLevel', val)
          },
          width: 120
        },
        {
          label: '包装条码',
          prop: 'package_no',
          key: 'package_no',
          width: 150
        },
        {
          label: '物料号',
          key: 'sku_code',
          prop: 'sku_code',
          width: 150
        },
        {
          label: '批次号',
          key: 'batch_no',
          prop: 'batch_no',
          width: 150
        },
        {
          label: 'SKU名称',
          key: 'sku_name',
          prop: 'sku_name',
          width: 120,
          attrs: {
            'show-overflow-tooltip': true
          }
        },
        {
          label: '库存区域类型',
          prop: 'loc_type',
          key: 'loc_type',
          format(val) {
            return getEnumLabel('StorageLocType', val)
          }
        },
        {
          label: '库存数量',
          prop: 'available_qty',
          key: 'available_qty'
        },
        {
          label: '分配数量',
          key: 'allot_qty',
          prop: 'allot_qty',
          scopedSlot: 'allot_qty',
          width: 160
        },
        {
          label: '单位',
          key: 'primary_unit',
          prop: 'primary_unit'
        },
        {
          label: '生产日期',
          prop: 'product_date',
          key: 'product_date',
          width: 150
        },
        {
          label: '可用日期',
          prop: 'availability_date',
          key: 'availability_date',
          width: 150
        },
        {
          label: '有效日期',
          prop: 'expire_date',
          key: 'expire_date',
          width: 150
        },
        {
          label: '重检日期',
          prop: 're_inspection_date',
          key: 're_inspection_date',
          width: 150
        },
        {
          label: '警告日期',
          prop: 'warning_date',
          key: 'warning_date',
          width: 150
        }
      ],
      allocationNumber: 0,
      isCreatedLoadData: false
    }
  },

  computed: {
    actQty() {
      return this.model.primary_qty - this.model.confirm_qty
    }
  },

  methods: {
    async handleSubmit() {
      try {
        const skuArr = this.$refs['tableSku'].$refs[this.tableName].selection
        if (skuArr.filter(item => !item.allot_qty).length) {
          this.$message('请输入分配数量')
          return false
        }
        if (!this.validateNumber()) {
          return false
        }
        await submitAllocation({
          item_id: this.model.id,
          form_id: this.model.form_id,
          form_no: this.model.form_no,
          item_no: this.model.item_no,
          availableStorageDtos: skuArr
        })
        this.handleCancel()
        this.$emit('ok')
      } catch (e) {
        console.log(e)
      }
    },

    handleTableSelect(selection, row) {
      console.log(selection)
      this.allocationNumber = selection.reduce((total, currentValue) => total + parseInt(currentValue.allot_qty), 0) || 0
    },

    validateNumber() {
      const selection = this.$refs['tableSku'].$refs[this.tableName].selection
      const allocationNumber = selection.reduce((total, currentValue) => total + parseInt(currentValue.allot_qty), 0) || 0
      if (allocationNumber > this.actQty) {
        this.$message('不能分配超过需求数量的物料')
        return false
      }
      return true
    },

    afterEdit() {
      this.$set(this.params, 'item_id', this.model.id)
      this.$set(this.params, 'form_id', this.model.form_id)
      this.$set(this.params, 'sku_code', this.model.sku_code)
      this.allocationNumber = 0
      this.handleSearch()
    },

    async loadData() {
      const params = this.generateQueryParams()
      try {
        const rows = await fetch.get(this.pagedUrl, { params })
        this.list = rows.rows.map(item => ({
          ...item,
          allot_qty: ''
        }))
        this.loading = false
      } catch (e) {
        console.error(e)
        this.loading = false
      }
    }
  }
}
</script>
