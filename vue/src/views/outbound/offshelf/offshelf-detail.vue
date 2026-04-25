<template>
  <drawer-modal
    :visible.sync="visible"
    :title="i18n('下架单详情')"
    :fullscreen="true"
  >
    <resize-layout height="calc(100vh - 200px)">
      <resize-col>
        <dynamic-table
          :columns="columns"
          v-loading="loading"
          :data="list"
          :pagination="pagination"
          @size-change="handleSizeChange"
          @page-change="handlePageChange"
        >
          <template v-slot:primary_qty="{ row }">
            {{`${row.primary_qty}( ${row.unit || ''} )`}}
          </template>
        </dynamic-table>
      </resize-col>
    </resize-layout>
    <div slot="footer">
      <el-button @click="handleCancel">{{$t('取消')}}</el-button>
    </div>
</drawer-modal>
</template>

<script>
import { pagedSearchMixin, formMixin } from '@/mixins'
import { offshelfDetailPagedUrl } from '@/api/outbound'
import { getEnumLabel } from '@/utils/helper'

export default {
  name: 'OffSelfDetail',

  mixins: [pagedSearchMixin, formMixin],

  data() {
    return {
      columns: [
        {
          label: '出库单项目',
          prop: 'item_no',
          key: 'item_no'
        },
        {
          label: '单据号',
          prop: 'form_no',
          key: 'form_no'
        },
        {
          label: '仓库号',
          prop: 'house_code',
          key: 'house_code'
        },
        {
          label: '序列号',
          prop: 'serial_no',
          key: 'serial_no'
        },
        {
          label: '主计量数量',
          prop: 'primary_qty',
          key: 'primary_qty',
          scopedSlot: 'primary_qty'
        },
        {
          label: '需求详情单号',
          prop: 'requisition_detail_no',
          key: 'requisition_detail_no'
        },
        {
          label: '订单托盘',
          prop: 'order_box',
          key: 'order_box'
        },
        {
          label: '是否拣选',
          prop: 'pick_flag',
          key: 'pick_flag',
          format(val) {
            return val ? '是' : '否'
          }
        },
        {
          label: '拣选完成时间',
          prop: 'pick_finish_time',
          key: 'pick_finish_time',
          width: 100
        },
        {
          label: '描述',
          prop: 'remark',
          key: 'remark'
        }
      ]
    }
  },

  methods: {
    pagedUrl() {
      return offshelfDetailPagedUrl(this.model.form_no)
    },

    afterEdit() {
      this.handleSearch()
    }
  }
}
</script>
