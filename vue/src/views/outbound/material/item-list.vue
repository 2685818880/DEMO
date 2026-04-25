<template>
  <resize-col>
    <search-form
      :fields="searchFields"
      :fields-value="params"
      @enter="handleSearch"
      @reset="handleReset">
    </search-form>
    <DynamicTable
      v-loading="loading"
      :data="list"
      :selectionIds.sync="selectionKeys"
      :selectionRows.sync="selectionRows"
      :columns="columns"
      :tableName="tableName"
      :pagination="pagination"
      :dblclick="handleViewStorage"
      @size-change="handleSizeChange"
      @page-change="handlePageChange"
      @context-menu-manual="handleManual"
      @context-menu-auto="handleAuto"
      @context-menu-view="handleViewStorage"
      @context-menu-generate="handleGenerate"
      @context-menu-urgent="handleUrgent"
      @context-menu-viewPick="handleViewPick"
    >
      <div slot="tools">
        <el-row>
          <el-button
            type="primary"
            v-if="model.form_type !== 'out_sales'"
            v-has="allocatePermission"
            :disabled="!selectionKeys.length"
            :loading="loading"
            @click="handleAuto()">{{ i18n('自动分配') }}
          </el-button>
          <el-button
            type="danger"
            v-if="model.form_type !== 'out_sales'"
            :disabled="!selectionKeys.length"
            :loading="loading"
            @click="handleCancelAllot()">{{ i18n('取消分配') }}
          </el-button>
        </el-row>
      </div>
      <template
        slot-scope="{row}"
        slot="primary_qty_slot">
        {{ row.primary_qty + '&nbsp;(&nbsp;' + (row.primary_unit || '') + '&nbsp;)&nbsp;' }}
      </template>
      <template
        slot-scope="{row}"
        slot="confirm_qty_slot">
        {{ row.confirm_qty + '&nbsp;(&nbsp;' + (row.primary_unit || '') + '&nbsp;)&nbsp;' }}
      </template>
      <template
        slot-scope="{row}"
        slot="not_confirm_qty_slot">
        {{ row.not_confirm_qty + '&nbsp;(&nbsp;' + (row.primary_unit || '') + '&nbsp;)&nbsp;' }}
      </template>
      <template
        slot-scope="{row}"
        slot="wave_qty_slot">
        {{ row.wave_qty + '&nbsp;(&nbsp;' + (row.primary_unit || '') + '&nbsp;)&nbsp;' }}
      </template>
      <template
        slot-scope="{row}"
        slot="allotted_qty_slot">
        {{ row.allotted_qty + '&nbsp;(&nbsp;' + (row.primary_unit || '') + '&nbsp;)&nbsp;' }}
      </template>
    </DynamicTable>
    <outbound-allocation
      ref="modalForm"
      @ok="handleReload"/>
    <outbound-storage ref="storageModal"/>
    <out-modal
      ref="outModal"
      level="outboundItem"
      @ok="handleReload"/>
      <urgent-modal ref="urgentModal" @ok="handleReload" />
    <manual-allocate ref="manualModal" @ok="handleReload" />
  </resize-col>
</template>

<script>
import {pagedSearchMixin} from '@/mixins'
import {allocateOutbound, cancelAllot, deleteItemList, querySkuListByFormNo, getOutboundMaterials, generateOutbound, getAllocation} from '@/api/outbound'
import { markUrgent } from '@/api/requisition'
import {confirm, loading} from '@/decorator'
import {getEnumLabel} from '@utils/helper'
import OutboundAllocation from './allocation-modal'
import OutboundStorage from './allocation-storage'
import ManualAllocate from './manual-allocate.vue'
import OutModal from './out-modal'
import { hasPermission } from '@/utils/auth'
import UrgentModal from './urgent-modal.vue'
import qs from 'qs'

const allocatePermission = '/wms-platform/view/outbound/allocate'
const generatePermission = '/wms-platform/view/outbound/generate'
const manualAllocatePermission = '/wms-platform/view/outbound/manualAllocate'

export default {
  name: 'OutboundItem',

  mixins: [pagedSearchMixin],

  components: {
    OutboundStorage,
    OutModal,
    OutboundAllocation,
    ManualAllocate,
    UrgentModal
  },

  props: {
    model: {
      type: Object,
      default: () => ({})
    }
  },

  data() {
    const {formType: form_type} = this.$route.params

    return {
      allocatePermission,
      isAllocate: false,
      businessType: form_type,
      searchFields: [
        {
          label: '出库单号',
          prop: 'form_no',
          props: {
            readonly: true
          }
        },
        {
          label: '明细单据号',
          prop: 'item_no'
        },
        {
          label: '物料编号',
          prop: 'sku_code'
        }
      ],
      columns: [
        {
          type: 'selection',
          key: 'selection',
          width: 50,
          attrs: {
            fixed: 'left'
          },
          isView: false
        },
        {
          label: '业务行号',
          key: 'business_item_no',
          prop: 'business_item_no',
          width: 160
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
          label: '物料编号',
          key: 'sku_code',
          prop: 'sku_code',
          width: 140
        },
        {
          label: '物料名称',
          key: 'sku_name',
          prop: 'sku_name',
          width: 140
        },
        {
          label: '工厂',
          prop: 'factory_code',
          key: 'factory_code',
          width: 140
        },
        {
          label: '库存地点',
          prop: 'source_area',
          key: 'source_area',
          width: 140
        },
        {
          label: '批次',
          key: 'batch_no',
          prop: 'batch_no',
          width: 140
        },
        {
          label: '质量状态',
          key: 'quality_status',
          prop: 'quality_status',
          format(val) {
            return getEnumLabel('MaterialStatus', val)
          }
        },
        {
          label: '需求数量',
          key: 'primary_qty',
          prop: 'primary_qty',
          scopedSlot: 'primary_qty_slot',
          width: 140
        },
        {
          label: '已分配数量',
          prop: 'allotted_qty',
          key: 'allotted_qty',
          scopedSlot: 'allotted_qty_slot',
          width: 140
        },
        {
          label: '波次执行中数量',
          prop: 'wave_qty',
          key: 'wave_qty',
          scopedSlot: 'wave_qty_slot',
          width: 140
        },
        {
          label: '已安排数量',
          key: 'confirm_qty',
          prop: 'confirm_qty',
          scopedSlot: 'confirm_qty_slot',
          width: 140
        },
        // {
        //   label: '未安排数量',
        //   key: 'not_confirm_qty',
        //   prop: 'not_confirm_qty',
        //   scopedSlot: 'not_confirm_qty_slot'
        // },
        {
          label: '出库明细单号',
          key: 'item_no',
          prop: 'item_no',
          width: 180
        },
        {
          label: '是否紧急',
          prop: 'jjbs',
          key: 'jjbs',
          format(val) {
            return val ? '是' : '否'
          }
        },
        {
          label: '包装类型',
          key: 'package_type',
          prop: 'package_type',
          format(val) {
            return getEnumLabel('PackageType', val)
          },
          width: 100
        },
        {
          label: '操作',
          key: 'action',
          // scopedSlot: 'action',
          width: 100,
          attrs: {
            fixed: 'right'
          },
          options: (row) => {
            const options = []
            row.confirm_qty > 0 && options.push({
              label: '分配详情',
              command: 'view'
            })
            // hasPermission(allocatePermission) && options.push({
            //   label: '自动分配',
            //   command: 'auto'
            // })
            row.confirm_qty !== row.primary_qty && this.model.form_type !== 'out_sales' && options.push({
              label: '手动分配',
              command: 'manual'
            })
            if (this.model.form_type === 'out_sales') {
              options.push({
                label: '加急',
                command: 'urgent'
              })
            }
            this.model.form_type !== 'out_sales' && options.push({
              label: '查看分配结果',
              command: 'viewPick'
            })
            // hasPermission(generatePermission) && options.push({
            //   label: '生成下架单',
            //   command: 'generate'
            // })
            return options
          }
        }
      ],
      isCreatedLoadData: false
    }
  },

  computed: {
    form_no() {
      return this.model.form_no
    },
    form_id() {
      return this.model.id
    },
    tableName() {
      return 'wms_outbound_item_' + this.businessType
    },
    title() {
      return `出库单据明细表， 上游单号:${this.form_no}`
    },
    hasSelected() {
      return this.selectionKeys.length > 0
    }
  },

  watch: {
    model: {
      handler(val) {
        this.extra_business_type = val.form_type || ''
        this.afterEdit()
      },
      deep: true
    }
  },

  methods: {
    handleViewPick({ business_item_no, id }) {
      const { business_form_no } = this.model
      const href = `/app/wms-platform/view/picking-item?${qs.stringify({
        business_form_no,
        business_item_no,
        id
      })}`
      window.open(href, '_blank')
    },

    handleUrgent(row) {
      this.$refs.urgentModal.edit({ requisitionItemId: row.id })
    },

    pagedUrl() {
      return getOutboundMaterials(this.form_no)
    },

    beforeSearch() {
      this.params.form_id = this.model.id
    },

    async afterEdit() {
      await this.$nextTick()
      const { model } = this
      if (!model.form_no) {
        return false
      }
      this.$set(this.params, 'form_no', model.form_no)
      this.handleSearch()
      this.checkAllocate()
    },

    async checkAllocate() {
      const { rows } = await getAllocation(this.form_no, this.form_id)
      this.isAllocate = !!rows.length
    },

    @confirm('是否确定安排')
    @loading()
    async handleAllocate({id}) {
      await autoAllocate([id])
      this.handleReload()
    },

    @confirm('是否确定安排')
    @loading()
    async handleBatchAllocate() {
      await autoAllocate(this.selectionKeys)
      this.handleReload()
    },

    @confirm('是否确定删除记录?')
    @loading()
    async handleDelete({id}) {
      await deleteItemList([id])
      this.handleReload()
    },

    handleManual(row) {
      this.$refs.manualModal.edit(row)
    },

    @confirm('是否确定分配')
    @loading()
    async handleAuto(row) {
      const selectionRows = this.selectionRows
      const orderId = row ? row.form_id : selectionRows.map(item => item.form_id)[0]
      const itemList = row ? [row.id] : selectionRows.map(item => item.id)
      await allocateOutbound([{
        orderId,
        itemList
      }])
      this.handleSearch()
    },

    @confirm('是否取消分配')
    @loading()
    async handleCancelAllot() {
      await cancelAllot([
        {
          orderId: this.form_id,
          itemIdList: this.selectionKeys
        }
      ])
      this.handleSearch()
    },

    @confirm('是否生成下架单?')
    @loading()
    async handleGenerate(row) {
      await generateOutbound(row.form_id)
      this.handleSearch()
    },

    handleViewStorage(row) {
      this.$refs.storageModal.edit({
        formNo: row.form_no,
        formId: row.form_id,
        itemId: row.id
      })
    },

    handleBatchPut() {
      this.$refs.outModal.edit({
        selectionKeys: this.selectionKeys
      })
    }
  }
}
</script>
