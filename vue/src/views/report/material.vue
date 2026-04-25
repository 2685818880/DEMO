<template>
  <resize-layout>
    <resize-col>
      <search-form
        :fields="searchFields"
        :fields-value="params"
        @enter="handleSearch"
        @reset="handleReset"
      />
      <dynamic-table
        tableName="materialreporttable"
        v-loading="loading"
        :columns="columns"
        :data="list"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @page-change="handlePageChange"
      >
      </dynamic-table>
    </resize-col>
  </resize-layout>
</template>

<script>
import { materialReportPagedUrl } from '@/api/report'
import { pagedSearchMixin } from '@/mixins'
export default {
  name: 'MaterialReport',

  mixins: [pagedSearchMixin],

  data() {
    return {
      pagedUrl: materialReportPagedUrl,
      searchFields: [
        {
          label: 'sku编码',
          prop: 'skuCode'
        },
        {
          label: 'SN',
          prop: 'serialNo'
        },
        {
          label: '工厂编号',
          prop: 'factoryCode'
        },
        {
          label: '批次号',
          prop: 'batchNo'
        },
        {
          label: '入库位置',
          prop: 'enterLoc'
        },
        {
          label: '出库位置',
          prop: 'exitLoc'
        },
        {
          label: '入库时间',
          prop: 'datepicker',
          key: 'datepicker',
          type: 'datePicker',
          props: {
            type: 'datetimerange',
            format: 'yyyy-MM-dd HH:mm:ss',
            'value-format': 'yyyy-MM-dd HH:mm:ss',
            pickerOptions: {}
          }
        },
        {
          label: '出库时间',
          prop: 'exittimepicker',
          key: 'exittimepicker',
          type: 'datePicker',
          props: {
            type: 'datetimerange',
            format: 'yyyy-MM-dd HH:mm:ss',
            'value-format': 'yyyy-MM-dd HH:mm:ss',
            pickerOptions: {}
          }
        },
        {
          label: '存货分类',
          prop: 'categoryCode',
          type: 'bizSelect',
          props: {
            bizType: 'skuCategory',
            labelName: 'category_name',
            valueName: 'category_code'
          }
        }
      ],
      columns: [
        {
          label: '仓库编号',
          prop: 'houseCode',
          key: 'houseCode'
        },
        {
          label: '托盘号',
          prop: 'container_code',
          key: 'container_code'
        },
        {
          label: 'SN',
          prop: 'serialNo',
          key: 'serialNo',
          width: 200
        },
        {
          label: '主数量',
          prop: 'primaryQty',
          key: 'primaryQty'
        },
        {
          label: '入库位置',
          prop: 'enterLoc',
          key: 'enterLoc'
        },
        {
          label: '入库时间',
          prop: 'enterTime',
          key: 'enterTime',
          width: 160
        },
        {
          label: '出库位置',
          prop: 'exitLoc',
          key: 'exitLoc'
        },
        {
          label: '出库时间',
          prop: 'exitTime',
          key: 'exitTime',
          width: 160
        },
        {
          label: '存货分类编号',
          prop: 'categoryCode',
          key: 'categoryCode',
          width: 120
        },
        {
          label: '存货分类',
          prop: 'categoryName',
          key: 'categoryName',
          width: 120
        },
        {
          label: 'sku编号',
          prop: 'skuCode',
          key: 'skuCode',
          width: 140
        },
        {
          label: 'sku名称',
          prop: 'skuName',
          key: 'skuName',
          width: 140
        },
        {
          label: '工厂',
          prop: 'factoryCode',
          key: 'factoryCode',
          width: 140
        },
        {
          label: '批次号',
          prop: 'batchNo',
          key: 'batchNo',
          width: 140
        }
      ]
    }
  },

  methods: {
    beforeSearch() {
      const { datepicker, exittimepicker } = this.params
      // 处理时间范围
      if (datepicker) {
        this.$set(this.params, 'startEnterTime', datepicker[0] || '')
        this.$set(this.params, 'endEnterTime', datepicker[1] || '')
      } else {
        this.$set(this.params, 'startEnterTime')
        this.$set(this.params, 'endEnterTime')
      }
      if (exittimepicker) {
        this.$set(this.params, 'startExitTime', exittimepicker[0] || '')
        this.$set(this.params, 'endExitTime', exittimepicker[1] || '')
      } else {
        this.$set(this.params, 'startExitTime')
        this.$set(this.params, 'endExitTime')
      }
    },

    generateQueryParams() {
      const {page, row} = this.pagination
      const params = {}
      Object.keys(this.params).map(key => {
        ['datepicker', 'finishtimepicker', 'exittimepicker'].indexOf(key) === -1 && (params[key] = this.params[key])
      })
      return {
        ...params,
        page,
        row
      }
    }
  }
}
</script>
