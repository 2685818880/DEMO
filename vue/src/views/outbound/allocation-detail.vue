<template>
  <drawer-modal
    modalType="drawer"
    :title="i18n('物料明细')"
    :visible.sync="visible"
    wrapperClosable>
    <resize-layout>
      <resize-col>
        <dynamic-table
          v-loading="loading"
          :columns="columns"
          :data="list"
          :selection-ids.sync="selectionKeys">
          <template
            slot-scope="{row}"
            slot="primary_qty">
            {{ row.primary_qty + '&nbsp;(&nbsp;' + row.primary_unit + '&nbsp;)&nbsp;' }}
          </template>
          <template
            slot-scope="{row}"
            slot="auxiliary_qty">
            {{ row.auxiliary_qty + '&nbsp;(&nbsp;' + row.auxiliary_unit + '&nbsp;)&nbsp;' }}
          </template>
        </dynamic-table>
      </resize-col>
    </resize-layout>
  </drawer-modal>
</template>

<script>

import {formMixin, pagedSearchMixin} from '@/mixins'
import {queryAllocationByPalletNo} from '@/api/outbound'
import {getEnumLabel} from '@utils/helper'

export default {
  name: 'allocation-detail',

  mixins: [pagedSearchMixin, formMixin],

  data() {
    return {
      columns: [
        {
          label: '物料明细单号',
          key: 'form_no',
          prop: 'form_no',
          width: 140
        },
        {
          label: '存货分类编码',
          key: 'category_code',
          prop: 'category_code',
          width: 140
        },
        {
          label: '存货分类名称',
          key: 'category_name',
          prop: 'category_name',
          width: 140
        },
        {
          label: '物料编码',
          key: 'sku_code',
          prop: 'sku_code',
          width: 160
        },
        {
          label: '物料名称',
          key: 'sku_name',
          prop: 'sku_name',
          width: 160
        },
        {
          label: '物料SN号',
          key: 'serial_no',
          prop: 'serial_no',
          width: 140
        },
        {
          label: '批次号',
          key: 'batch_no',
          prop: 'batch_no',
          width: 140
        },
        {
          label: '质量状态',
          key: 'material_status',
          prop: 'material_status',
          format(val) {
            return getEnumLabel('MaterialStatus', val)
          },
          width: 80
        },
        {
          label: '库存数量',
          key: 'primary_qty',
          prop: 'primary_qty',
          scopedSlot: 'primary_qty',
          width: 100
        },
        {
          label: '辅助数量',
          key: 'auxiliary_qty',
          prop: 'auxiliary_qty',
          scopedSlot: 'auxiliary_qty',
          width: 100
        }
      ]
    }
  },
  methods: {
    pagedUrl() {
      return queryAllocationByPalletNo(this.model.formNo, this.model.formId)
    },
    afterEdit() {
      this.handleSearch()
    }
  }

}
</script>

<style scoped>

</style>
