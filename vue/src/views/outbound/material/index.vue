<template>
  <resize-layout>
    <resize-col>
      <search-form
        :fields="searchFields"
        :fields-value="params"
        :visibleNum="3"
        @enter="handleSearch"
        @reset="handleReset">
      </search-form>
      <DynamicTable
        v-loading="loading"
        :selectionIds.sync="selectionKeys"
        :selectionRows.sync="selectionRows"
        :tableName="tableName"
        :columns="columns"
        :data="list"
        :pagination="pagination"
        :dblclick="handleRowClick"
        :show-filter="false"
        @size-change="handleSizeChange"
        @page-change="handlePageChange"
        @context-menu-view="handleRowClick"
        @context-menu-auto="handleAllocate"
        @context-menu-manual="handleManual"
        @context-menu-finish="handleFinish"
        @context-menu-cancel="handleOrderCancel"
        @context-menu-generate="handleGenerate"
        @context-menu-viewBound="handleViewBound"
        @context-menu-audit="handleAudit"
        @context-menu-pick="handlePick"
        @context-menu-viewPick="handleViewPick"
        @context-menu-del="handleOrderCancel"
      >
        <template v-slot:formNo="{ row }">
          <a @click="handleRowClick(row)">
            {{ row.form_no }}
          </a>
        </template>
        <div slot="tools">
          <el-row>
            <el-button
              type="primary"
              icon="el-icon-edit"
              @click="handleAdd">{{ i18n('新增') }}
            </el-button>
            <el-button
              type="primary"
              :disabled="!selectionKeys.length || disableAllocate"
              :loading="loading"
              @click="handleAllocate()">{{ i18n('自动分配') }}
            </el-button>
            <el-button
              type="warning"
              v-if="formType !== 'out_sales'"
              :disabled="!selectionKeys.length || disableAudit"
              :loading="loading"
              @click="handleAudit()">{{ i18n('提交过账') }}
            </el-button>
            <el-button
              type="danger"
              :disabled="!selectionKeys.length || disableCancel"
              :loading="loading"
              @click="handleOrderCancel()">{{ i18n('作废') }}
            </el-button>
            <el-button
              type="success"
              :disabled="!selectionKeys.length"
              :loading="loading"
              @click="handlePick()">{{ i18n('开始拣选') }}
            </el-button>
            <el-button
              v-if="formType === 'out_sales'"
              type="warning"
              @click="handleWave"
            >
              {{ i18n('创建波次单') }}
            </el-button>
            <el-button
              :disabled="!selectionKeys.length"
              type="primary"
              @click="handleOutBox"
            >
              {{ i18n('订单箱出库') }}
            </el-button>
          </el-row>
        </div>
      </DynamicTable>
      <outbound-form-detail-modal
        ref="modalForm"
        :fields="fields"
        :formType="formType"
        @ok="handleReload"/>
      <scrap-modal
        ref="scrapModal"
        :fields="fields"
        :formType="formType"
        @ok="handleReload"/>
      <out-modal-material ref="OutModalMaterial" level="outbound" @ok="handleReload"/>
      <outbound-materiel-list-modal
        ref="materielForm"
        :fields="fields"/>
      <out-v-modal ref="outModal" level="outModal" @ok="handlePickOut"/>
    </resize-col>
    <item-list v-if="itemAlive" ref="itemList" :model="model"/>
  </resize-layout>
</template>

<script>
import {pagedSearchMixin} from '@/mixins'
import {confirm, loading} from '@/decorator/index'
import {
  deleteList,
  outboundPagedUrl,
  allocateOutbound,
  finishOutbound,
  cancelOutbound,
  generateOutbound,
  submitOutbound,
  queryOutboundMaterials,
  createPickOrder,
  requisitionOrderOrderStockOut
} from '@/api/outbound/index'
import {createWave, markUrgent} from '@/api/requisition'
import {getEnumLabel} from '@utils/helper'
import OutboundFormDetailModal from './form-detail-modal'
import OutboundMaterielListModal from './materiel-list'
import OutModalMaterial from './out-modal-material'
import ManualAllocate from './manual-allocate.vue'
import OutVModal from './out-v-modal.vue'
import {hasPermission} from '@/utils/auth'
import ItemList from './item-list.vue'
import qs from 'qs'
import ScrapModal from '../scrap/form-detail-modal.vue'

const allocatePermission = '/wms-platform/view/outbound/allocate'
const finishPermission = '/wms-platform/view/outbound/finish'
const cancelPermission = '/wms-platform/view/outbound/cancel'
const generatePermission = '/wms-platform/view/outbound/generate'
const manualAllocatePermission = '/wms-platform/view/outbound/manualAllocate'
const pickPermission = '/wms-platform/view/outbound/pick'

const remarkMap = {
  za: 'ZA_国内-典型',
  xa: 'XA_海外',
  zb: 'ZB_国内-欧派',
  zc: 'ZC_国内-酒店',
  zd: 'ZD_国内-崔佧',
  ze: 'ZE_国内-科施德',
  zf: 'ZF_国内-沙发'
}

export default {
  name: 'OutboundList',

  mixins: [pagedSearchMixin],

  components: {
    OutboundFormDetailModal,
    OutboundMaterielListModal,
    OutModalMaterial,
    ManualAllocate,
    ItemList,
    ScrapModal,
    OutVModal
  },

  data() {
    return {
      tableName: 'outboundTable',
      resetModelBeforeSearch: true,
      allocatePermission,
      finishPermission,
      cancelPermission,
      pickPermission,
      model: {},
      locations: [],
      pagedUrl: outboundPagedUrl,
      formType: '',
      fields: [],
      searchFields: [
        {
          label: '业务单号',
          prop: 'business_form_no'
        },
        {
          label: '单据状态',
          prop: 'form_status',
          type: 'enum',
          props: {
            code: 'FormStatus'
          }
        },
        // {
        //   label: '是否作废',
        //   prop: 'is_active',
        //   type: 'enum',
        //   props: {
        //     code: 'IsActive'
        //   }
        // },
        {
          label: '客户编码',
          prop: 'KUNNR'
        }
      ],
      columns: [
        {
          type: 'selection',
          key: 'selection',
          width: 50,
          attrs: {
            fixed: 'left'
          }
        },
        {
          label: '业务单号',
          key: 'business_form_no',
          prop: 'business_form_no',
          width: 100
        },
        {
          label: '业务类型',
          key: 'business_form_type',
          prop: 'business_form_type',
          width: 100
        },
        {
          label: '单据类型',
          key: 'form_type',
          prop: 'form_type',
          width: 100,
          format(val) {
            return getEnumLabel('OutFormType', val)
          }
        },
        {
          label: '单据状态',
          key: 'form_status',
          prop: 'form_status',
          format(val) {
            return getEnumLabel('FormStatus', val)
          }
        },
        // {
        //   label: '是否作废',
        //   prop: 'is_active',
        //   key: 'is_active',
        //   format(val) {
        //     return getEnumLabel('IsActive', val)
        //   }
        // },
        {
          label: '开始时间',
          key: 'start_time',
          prop: 'start_time',
          width: 160
        },
        {
          label: '关闭时间',
          key: 'close_time',
          prop: 'close_time',
          width: 160
        },
        {
          label: '分配状态',
          key: 'allot_status',
          prop: 'allot_status',
          width: 100,
          format(val) {
            return getEnumLabel('AllotStatus', val)
          }
        },
        {
          label: '过账状态',
          key: 'submit_status',
          prop: 'submit_status',
          format(val) {
            return getEnumLabel('SubmitStatus', val)
          },
          width: 120
        },
        {
          label: '过账人',
          key: 'submit_by',
          prop: 'submit_by',
          width: 100
        },
        {
          label: '过账时间',
          key: 'submit_time',
          prop: 'submit_time',
          width: 160
        },
        // {
        //   label: '审核状态',
        //   key: 'audit_status',
        //   prop: 'audit_status',
        //   format(val) {
        //     return getEnumLabel('AuditStatus', val)
        //   },
        //   width: 120
        // },
        // {
        //   label: '审核人',
        //   key: 'audit_by',
        //   prop: 'audit_by',
        //   width: 100
        // },
        // {
        //   label: '审核时间',
        //   key: 'audit_datetime',
        //   prop: 'audit_datetime',
        //   width: 160
        // },
        // {
        //   label: '货主编码',
        //   prop: 'owner_code'
        // },
        // {
        //   label: '货主名称',
        //   prop: 'owner_name'
        // },
        {
          label: '出库单号',
          key: 'form_no',
          prop: 'form_no',
          width: 160,
          scopedSlot: 'formNo'
        },
        {
          label: '单据备注',
          key: 'remark',
          prop: 'remark',
          width: 120
        },
        {
          label: '创建人',
          key: 'operator',
          prop: 'operator',
          width: 120
        },
        {
          label: '操作',
          key: 'action',
          width: 100,
          attrs: {
            fixed: 'right'
          },
          options: (row) => {
            const options = [
              {
                label: '详情',
                command: 'view'
              },
              {
                label: '查看拣选单',
                command: 'viewPick'
              }
            ]
            if (row.form_status === 'Created') {
              options.push({
                label: '作废',
                command: 'del'
              })
            }
            // hasPermission(allocatePermission) && options.push({
            //   label: '自动分配',
            //   command: 'auto'
            // })
            // hasPermission(finishPermission) && options.push({
            //   label: '审核',
            //   command: 'audit'
            // })
            // // hasPermission(finishPermission) && options.push({
            // //   label: '标记完成',
            // //   command: 'finish'
            // // })
            // hasPermission(cancelPermission) && options.push({
            //   label: '作废',
            //   command: 'cancel'
            // })
            // // hasPermission(generatePermission) && options.push({
            // //   label: '生成下架单',
            // //   command: 'generate'
            // // })
            // // hasPermission(generatePermission) && options.push({
            // //   label: '查看下架单',
            // //   command: 'viewBound'
            // // })
            // hasPermission(pickPermission) && options.push({
            //   label: '生成拣选单',
            //   command: 'pick'
            // })
            return options
          }
        }
      ],
      remark: ''
    }
  },

  computed: {
    hasSelected() {
      return this.selectionKeys.length === 1
    },
    disableAllocate() {
      const selectionRows = this.selectionRows
      console.log(!!selectionRows.filter(item => item.form_status === 'Created' || item.form_status === 'Picked').length)
      return !selectionRows.filter(item => item.form_status === 'Created' || item.form_status === 'Picked').length
    },
    disableCancel() {
      const selectionRows = this.selectionRows
      return !!selectionRows.filter(item => item.form_status !== 'Created').length
    },
    disableAudit() {
      const selectionRows = this.selectionRows
      return !!selectionRows.filter(item => ['Created', 'Waiting', 'Executing', 'Finished'].indexOf(item.form_status) === -1).length
    },
    disableGenerate() {
      const selectionRows = this.selectionRows
      return !!selectionRows.filter(item => item.allot_status !== 'Allotted' && item.allot_status !== 'Allotting').length
    }
  },

  watch: {
    $route: {
      handler(route) {
        const {formType, remark} = route.params
        this.formType = formType || 'simpleOut'
        this.tableName = `outboundTable${formType || ''}${remark || ''}`
        if (this.formType === 'out_sales') {
          this.allocatePermission = 'out_sales'
          this.pickPermission = 'out_sales'
        }
        this.remark = remark
        this.extra_business_type = this.formType
        this.$set(this.params, 'form_type', this.formType)
        this.$set(this.params, 'remark', remarkMap[remark] || '')
      },
      immediate: true
    }
  },

  methods: {
    handleAdd() {
      if (this.formType === 'scrapOut') {
        this.$refs.scrapModal.edit()
        return false
      }
      this.$refs.modalForm.edit()
    },

    async handleWave() {
      window.open(`/app/wms/view/requisition-item/${this.remark}?${qs.stringify(this.params)}`, '_blank')
      // if (this.remark === 'za') {
      //   const selectionKeys = this.selectionKeys
      //   await createWave(selectionKeys.map(item => ({ requisitionOrderId: item })))
      //   this.handleSearch()
      // } else {
      //   window.open(`/app/wms/view/requisition-item/${this.remark}?${qs.stringify(this.params)}`, '_blank')
      // }
    },

    handleMaterielEdit(row) {
      this.$refs.materielForm.edit(row)
    },

    handleRowClick(row) {
      this.model = row
    },

    handleViewPick(row) {
      window.open(`/app/wms-platform/view/picking?relation_order_id=${row.id}`, '_blank')
    },

    handleViewBound(row) {
      this.$router.push({
        path: '/offshelf',
        query: {
          formNo: row.form_no
        }
      })
    },

    async handleBatchDelete() {
      await deleteList(this.selectionKeys)
      this.handleSearch()
    },

    @confirm('是否确定订单箱出库?')
    async handleOutBox() {
      const params = this.selectionRows.map(it => {
        return {
          orderId: it.id
        }
        })
      await requisitionOrderOrderStockOut(params)
      this.handleSearch()
    },

    @loading()
    async handleUrgent(row) {
      await markUrgent({
        requisitionItemId: row.id
      })
      this.handleSearch()
    },

    @loading()
    async handleCancelurgent(row) {
      await markUrgent({
        requisitionItemId: row.id
      })
      this.handleSearch()
    },

    @confirm('是否确定删除记录?')
      @loading()
    async handleDelete({id}) {
      await deleteList([id])
      this.handleSearch()
    },

    // @confirm('是否确定审核单据?')
    // async handleBatchAudit() {
    //   await finishOutboundList(this.selectionKeys)
    //   this.handleSearch()
    // },

    @confirm('是否确定提交过账?')
      @loading()
    async handleAudit(row) {
      await submitOutbound(row ? [row.id] : this.selectionKeys)
      this.handleSearch()
    },

    @confirm('是否确定创建拣选单据?')
      @loading()
    async handlePick(row) {
      this.$refs.outModal.edit(row)
    },

    handlePickOut(param) {
      const ids = this.selectionKeys
      const pickArea = param.location
      createPickOrder(ids.map(item => ({orderId: item, pickArea: pickArea})))
      this.handleSearch()
    },

    @confirm('是否确定标记完成?')
      @loading()
    async handleFinish(row) {
      await finishOutbound(row.id)
      this.handleSearch()
    },
    //
    // @confirm('是否确定审核单据?')
    // async handleAudit({id}) {
    //   await finishOutboundList([id])
    //   this.handleSearch()
    // },
    // @confirm('是否确定分配单据?')
    // async handleBatchAllocate() {
    //   await allocateOutboundList(this.selectionKeys[0])
    //   this.handleSearch()
    // },

    @confirm('是否确定分配单据')
      @loading()
    async handleAllocate(row) {
      const orderIds = row ? [row.id] : this.selectionKeys
      await allocateOutbound(orderIds.map(item => ({orderId: item})))
      this.handleRowClick(row || this.selectionRows[0])
      this.handleSearch()
    },

    handleManual(row) {
      this.$refs.manualModal.edit(row)
    },

    // @confirm('作废创建状态的出库单,是否确定?')
    // async handleBatchCancel() {
    //   await cancelOutboundList(this.selectionKeys)
    //   this.handleSearch()
    //   this.clearSelectionKeys()
    // },

    @confirm('作废创建状态的出库单,是否确定?')
      @loading()
    async handleOrderCancel(row) {
      await cancelOutbound(row ? [row.id] : this.selectionKeys)
      this.handleSearch()
    },

    handleBatchPut() {
      this.$refs.OutModalMaterial.edit({
        selectionKeys: this.selectionKeys
      })
    },

    @confirm('是否生成下架单?')
      @loading()
    async handleGenerate(row) {
      await generateOutbound(row.id)
      this.handleSearch()
    }
  }
}
</script>
<style>
</style>
