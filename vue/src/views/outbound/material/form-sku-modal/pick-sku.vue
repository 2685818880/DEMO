<template>
  <drawer-modal
    :visible.sync="visible"
    :title="i18n('选择货品')"
    width="80%"
    :modal="false"
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
          :selectionRows.sync="selectionRows"
          :keyname="keyname"
          :tableName="tableName"
          :columns="columns"
          :data="list"
          :pagination="pagination"
          @size-change="handleSizeChange"
          @page-change="handlePageChange"
        >
          <template v-slot:num="{ row }">
            <el-input-number v-model="row.num" :min="0" :max="row.available_qty"/>
          </template>
          <template v-slot:business_item_no="{ row }">
            <el-input v-model="row.business_item_no" />
          </template>
          <template v-slot:factory_code="{ row }">
            <el-input v-model="row.factory_code" />
          </template>
          <template v-slot:source_area="{ row }">
            <el-input v-model="row.source_area" />
          </template>
          <template v-slot:batch_no="{ row }">
            <el-input v-model="row.batch_no" />
          </template>
        </dynamic-table>
      </resize-col>
    </resize-layout>
  </drawer-modal>
</template>

<script>
import { pagedSearchMixin, formMixin } from '@/mixins'
import { skuSummaryUrl } from '@/api/warehouse'

export default {
  name: 'SkuModal',

  mixins: [pagedSearchMixin, formMixin],

  props: {
    selectedRows: {
      type: Array,
      default: () => []
    }
  },

  data() {
    return {
      rowData: '',
      keyname: 'markId',
      tableName: 'skuTableName',
      pagedUrl: skuSummaryUrl,
      searchFields: [
        {
          label: 'SKU编号',
          prop: 'sku_code'
        },
        {
          label: 'SKU名称',
          prop: 'sku_name'
        }
      ],
      columns: [
        {
          type: 'selection',
          key: 'selection',
          attrs: {
            fixed: 'left',
            selectable: (row, index) => {
              const selectedRows = this.selectedRows
              if (selectedRows.filter(item => item.sku_code === row.sku_code).length) {
                return false
              }
              return true
            }
          }
        },
        {
          label: '存货分类',
          key: 'category_name',
          prop: 'category_name'
        },
        {
          label: 'SKU编号',
          key: 'sku_code',
          prop: 'sku_code',
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
          label: '库存数量',
          key: 'available_qty',
          prop: 'available_qty'
        },
        // {
        //   label: '基本单位',
        //   key: 'primary_unit',
        //   prop: 'primary_unit'
        // },
        {
          label: '库存单位',
          key: 'primary_unit',
          prop: 'primary_unit'
        },
        {
          label: '需求数量',
          key: 'num',
          prop: 'num',
          scopedSlot: 'num',
          width: 180
        },
        {
          label: '业务行号',
          key: 'business_item_no',
          prop: 'business_item_no',
          scopedSlot: 'business_item_no',
          width: 180
        },
        {
          label: '工厂',
          key: 'factory_code',
          prop: 'factory_code',
          scopedSlot: 'factory_code',
          width: 180
        },
        {
          label: '库存地点',
          key: 'source_area',
          prop: 'source_area',
          scopedSlot: 'source_area',
          width: 180
        },
        {
          label: '批次',
          key: 'batch_no',
          prop: 'batch_no',
          scopedSlot: 'batch_no',
          width: 180
        }
      ]
    }
  },

  methods: {
    add(row) {
      this.edit(row)
      this.rowData = row.house_code
    },
    handleBaseMap(row, index) {
      return {
        ...row,
        markId: index
      }
    },

    handleSubmit() {
      try {
        const skuArr = this.$refs['tableSku'].$refs[this.tableName].selection
        if (!skuArr.length) {
          this.$message('请先选择物料')
          return false
        }
        if (skuArr.filter(item => !item.num).length) {
          this.$message('请输入需求数量')
          return false
        }
        let numQty = false
        skuArr.forEach((item, index) => {
          console.log(item, 'item')
           if (item.num > item.available_qty) {
            numQty = true
           }
        })
        if (numQty) {
          this.$message('需求数量不能大于库存数量')
          return false
        }
        this.$emit('ok', skuArr)
        this.handleCancel()
      } catch (e) {
        console.log(e)
      }
    },

    afterEdit() {
      this.handleSearch()
    }
  }
}
</script>
