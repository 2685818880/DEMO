<template>
  <resize-layout>
    <resize-col>
      <dynamic-table
        tableName="materialreportsumtable"
        v-loading="loading"
        :columns="columns"
        :data="list"
      >
      </dynamic-table>
    </resize-col>
  </resize-layout>
</template>

<script>
import { queryMaterialSum } from '@/api/report'
import { pagedSearchMixin } from '@/mixins'

export default {
  name: 'MaterialReport',

  mixins: [pagedSearchMixin],

  data() {
    return {
      columns: [
        {
          label: '库位',
          prop: 'inventory_location',
          key: 'inventory_location'
        },
        {
          label: '物料编码',
          prop: 'sku_code',
          key: 'sku_code'
        },
        {
          label: '存货分类编码',
          prop: 'category_code',
          key: 'category_code'
        },
        {
          label: '数量',
          prop: 'count',
          key: 'count'
        },
        {
          label: '可用数量',
          prop: 'canUse',
          key: 'canUse'
        },
        {
          label: '锁定数量',
          prop: 'lockedCount',
          key: 'lockedCount'
        }
      ]
    }
  },

  methods: {
    async loadData() {
      const rows = await queryMaterialSum()
      this.list = rows
    }
  }
}
</script>
