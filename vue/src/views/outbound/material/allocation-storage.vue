<template>
  <drawer-modal
    v-if="model.formNo"
    :title="i18n('分配明细')"
    :visible.sync="visible"
    width="80%"
  >
    <resize-layout :height="modalTableHeight">
      <resize-col>
        <search-form
          :fields="searchFields"
          :fields-value="params"
          @enter="handleSearch"
          @reset="handleReset"
        />
        <dynamic-table
          ref="allocationstore"
          v-loading="loading"
          :columns="columns"
          :data="list"
          :tableName="tableName"
          :selection-ids.sync="selectionKeys"
          :pagination="pagination"
          @size-change="handleSizeChange"
          @page-change="handlePageChange"
          @context-menu-view="handleMaterialInfo"
          @context-menu-put="handlePut"
        >
          <!-- <div class="g-toolbars" slot="tools">
            <el-button
              type="danger"
              icon="el-icon-delete"
              v-has="generatePermission"
              @click="handleGenerate"
              :disabled="!hasSelected">
              {{i18n('生成下架单')}}
            </el-button>
          </div> -->
          <template
            slot-scope="{row}"
            slot="primary_qty">
            {{ row.primary_qty + '&nbsp;(&nbsp;' + (row.unit || '') + '&nbsp;)&nbsp;' }}
          </template>
          <template
            slot-scope="{row}"
            slot="auxiliary_qty">
            {{ row.auxiliary_qty + '&nbsp;(&nbsp;' + row.auxiliary_unit + '&nbsp;)&nbsp;' }}
          </template>
          <template
            slot-scope="{ row }"
            slot="houseCode">
            <biz-label bizType="house" field-name="house_no" showFieldName="house_name" :value="row.house_code"/>
          </template>
        </dynamic-table>
        <!-- <out-modal-material ref="OutModalMaterial" level="outboundAllocation" @ok="handleReload"/> -->
      </resize-col>
    </resize-layout>
    <div slot="footer">
      <el-button @click="handleCancel">{{ i18n('取消') }}</el-button>
    </div>
  </drawer-modal>
</template>

<script>
import {confirm} from '@/decorator'
import {formMixin, pagedSearchMixin} from '@/mixins'
import {getEnumLabel} from '@utils/helper'
import {deleteAllocationList, queryAllocationByPalletNo, generateOutbound, takeOutOutboundAllocation} from '@/api/outbound'
import BizLabel from '@comp/BizLabel'
import { hasPermission } from '@/utils/auth'

const generatePermission = '/wms-platform/view/outbound/generate'
const putPermission = '/wms-platform/view/outbound/allocation/put'

export default {
  name: 'OutboundStorage',

  mixins: [pagedSearchMixin, formMixin],

  components: {
    BizLabel
  },

  data() {
    return {
      generatePermission,
      isCreatedLoadData: false,
      tableName: 'wms_outbound_allocation_pallet',
      searchFields: [
        {
          label: '托盘编号',
          prop: 'container_code'
        },
        {
          label: '物料SN',
          prop: 'serial_no'
        }
      ],
      columns: [
        // {
        //   label: '操作',
        //   type: 'selection',
        //   width: 50,
        //   isView: false
        // },
        {
          label: '详情编号',
          prop: 'detail_no',
          key: 'detail_no',
          width: 180
        },
        {
          label: '单据编号',
          prop: 'form_no',
          key: 'form_no',
          width: 180
        },
        {
          label: '波次单号',
          prop: 'wave_order_no',
          key: 'wave_order_no',
          width: 180
        },
        {
          label: '仓库号',
          key: 'house_code',
          prop: 'house_code',
          scopedSlot: 'houseCode',
          width: 100
        },
        {
          label: '容器号',
          key: 'container_code',
          prop: 'container_code',
          width: 100
        },
        {
          label: '物料SN',
          prop: 'serial_no',
          key: 'serial_no',
          width: 240
        },
        {
          label: '单据状态',
          key: 'detail_status',
          prop: 'detail_status',
          format(val) {
            return getEnumLabel('DetailStatus', val)
          },
          width: 100
        },
        // {
        //   label: '辅助数量',
        //   key: 'auxiliary_qty',
        //   prop: 'auxiliary_qty',
        //   scopedSlot: 'auxiliary_qty',
        //   width: 120
        // },
        {
          label: '出库库位',
          key: 'location_code',
          prop: 'location_code',
          width: 100
        },
        {
          label: '分配数量',
          prop: 'primary_qty',
          key: 'primary_qty',
          scopedSlot: 'primary_qty',
          width: 100
        },
        {
          label: '凭证',
          prop: 'voucher_no',
          key: 'voucher_no',
          width: 100
        },
        {
          label: '年凭证',
          prop: 'voucher_year',
          key: 'voucher_year',
          width: 100
        },
        {
          label: '明细单号',
          key: 'item_no',
          prop: 'item_no',
          width: 180
        }
        // {
        //   label: '操作',
        //   key: 'action',
        //   width: 100,
        //   attrs: {
        //     fixed: 'right'
        //   },
        //   options(row) {
        //     const options = [
        //       {
        //         label: '详情',
        //         command: 'view'
        //       }
        //     ]
        //     hasPermission(putPermission) && options.push({
        //       label: '取出托盘',
        //       command: 'put'
        //     })
        //     return options
        //   }
        // }
      ]
    }
  },

  computed: {
    hasSelected() {
      return this.selectionKeys.length > 0
    }
  },

  methods: {
    afterEdit() {
      this.handleSearch()
    },

    pagedUrl() {
      return queryAllocationByPalletNo(this.model.formNo, this.model.formId, this.model.itemId)
    },

    @confirm('是否确定生成下架单')
    async handleGenerate() {
      const detailDtoList = this.$refs['allocationstore'].$refs[this.tableName].selection
      await generateOutbound({
        detailDtoList
      })
    },

    @confirm('确定要取出托盘吗?')
    async handlePut(row) {
      if (row.target_area) {
        await takeOutOutboundAllocation({
          outboundIds: [row.id],
          toPos: row.target_area
        })
        this.handleSearch()
      } else {
        this.$refs.OutModalMaterial.edit({
          selectionKeys: [row.id],
          house_code: row.house_code
        })
      }
    },

    // @confirm('是否确定标记完成?')
    // async handleAllocationFinish({id}) {
    //   await finishAllocationList([id])
    //   this.handleSearch()
    // },
    //
    // @confirm('是否确定取消完成?')
    // async handleBatchCancel() {
    //   await cancelfinishList(this.selectionKeys)
    //   this.handleSearch()
    // },
    //
    // @confirm('是否确定取消完成?')
    // async handleAllocationCancel({id}) {
    //   await cancelfinishList([id])
    //   this.handleSearch()
    // },

    @confirm('是否确定作废?')
    async handleBatchDelete() {
      await deleteAllocationList(this.selectionKeys)
      this.handleSearch()
    },

    // @confirm('是否确定删除?')
    // async handleAllocationDelete({id}) {
    //   await deleteAllocationList([id])
    //   this.handleSearch()
    // },

    handleMaterialInfo({detail_no}) {
      this.$refs.allocationDetail.edit({
        detail_no
      })
    },
    // @confirm('是否确定取出?')
    // async handleAllocationPut(ids) {
    //   await finishPutList(ids)
    //   this.handleSearch()
    // }
    handleBatchPut() {
      this.$refs.OutModalMaterial.edit({
        selectionKeys: this.selectionKeys,
        outLocation: this.model.outLocation,
        currentStorage: this.model.currentStorage
      })
    }
  }
}
</script>
