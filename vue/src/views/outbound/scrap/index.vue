<template>
  <resize-layout>
    <resize-col>
      <search-form
        :fields="searchFields"
        :fields-value="params"
        @enter="handleSearch"
        @reset="handleReset">
      </search-form>
      <DynamicTable
        :selectionIds.sync="selectionKeys"
        v-loading="loading"
        tableName="outboundTable"
        :columns="columns"
        :data="list"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @page-change="handlePageChange"
        @context-menu-view="handleRowClick"
        @context-menu-auto="handleAllocate"
        @context-menu-manual="handleManual"
        @context-menu-finish="handleFinish"
        @context-menu-cancel="handleOrderCancel"
        @context-menu-generate="handleGenerate"
        @context-menu-viewBound="handleViewBound"
        @context-menu-audit="handleAudit">
        <template v-slot:formNo="{ row }">
          <a @click="handleRowClick(row)">
            {{row.form_no}}
          </a>
        </template>
        <div slot="tools">
          <el-row>
            <el-button
              type="primary"
              icon="el-icon-edit"
              @click="handleAdd">{{ i18n('新增') }}
            </el-button>
          </el-row>
        </div>
      </DynamicTable>
      <outbound-form-detail-modal
        ref="modalForm"
        :fields="fields"
        @ok="handleReload"/>
      <out-modal-material ref="OutModalMaterial" level="outbound" @ok="handleReload"/>
      <outbound-materiel-list-modal
        ref="materielForm"
        :fields="fields"/>
    </resize-col>
    <item-list ref="itemList" :model="model" />
  </resize-layout>
</template>

<script>
import {pagedSearchMixin} from '@/mixins'
import {confirm} from '@/decorator/index'
import {
  deleteList,
  outboundPagedUrl,
  allocateOutbound,
  finishOutbound,
  cancelOutbound,
  generateOutbound,
  submitOutbound
} from '@/api/outbound/index'
import {getEnumLabel} from '@utils/helper'
import OutboundFormDetailModal from './form-detail-modal'
import OutboundMaterielListModal from '../material/materiel-list'
import OutModalMaterial from '../material/out-modal-material'
import { hasPermission } from '@/utils/auth'
import ItemList from '../material/item-list.vue'

const allocatePermission = '/wms-platform/view/outbound/allocate'
const finishPermission = '/wms-platform/view/outbound/finish'
const cancelPermission = '/wms-platform/view/outbound/cancel'
const generatePermission = '/wms-platform/view/outbound/generate'
const manualAllocatePermission = '/wms-platform/view/outbound/manualAllocate'

export default {
  name: 'OutboundList',

  mixins: [pagedSearchMixin],

  components: {
    OutboundFormDetailModal,
    OutboundMaterielListModal,
    OutModalMaterial,
    ItemList
  },

  data() {
    const {formType: form_type} = this.$route.params
    return {
      model: {},
      locations: [],
      pagedUrl: `${outboundPagedUrl}?form_type=scrapOut`,
      form_type,
      fields: [],
      params: {
        form_type,
        form_status: 'CreatedAndExecuting'
      },
      searchFields: [
        {
          label: '单号',
          prop: 'form_no'
        },
        {
          label: '单据状态',
          prop: 'form_status',
          type: 'enum',
          props: {
            code: 'FormStatus'
          }
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
        // {
        //   label: '库号',
        //   key: 'house_code',
        //   prop: 'house_code'
        // },
        {
          label: '单据类型',
          key: 'form_type',
          prop: 'form_type',
          format(val) {
            return getEnumLabel('FormType', val)
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
        {
          label: '出库单号',
          key: 'form_no',
          prop: 'form_no',
          width: 160,
          scopedSlot: 'formNo'
        },
        {
          label: '单据开始时间',
          key: 'start_time',
          prop: 'start_time',
          width: 160
        },
        {
          label: '单据关闭时间',
          key: 'finish_time',
          prop: 'finish_time',
          width: 160
        },
        {
          label: '单据创建人',
          key: 'operator',
          prop: 'operator',
          width: 120
        },
        {
          label: '接口过账状态',
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
          key: 'submit_datetime',
          prop: 'submit_datetime',
          width: 160
        },

        {
          label: '审核状态',
          key: 'audit_status',
          prop: 'audit_status',
          format(val) {
            return getEnumLabel('AuditStatus', val)
          },
          width: 120
        },
        {
          label: '审核人',
          key: 'audit_by',
          prop: 'audit_by',
          width: 100
        },
        {
          label: '审核时间',
          key: 'audit_datetime',
          prop: 'audit_datetime',
          width: 160
        },
        {
          label: '单据备注',
          key: 'remark',
          prop: 'remark',
          width: 120
        },
        {
          label: '操作',
          key: 'action',
          width: 100,
          attrs: {
            fixed: 'right'
          },
          options(row) {
            const options = [
              {
                label: '详情',
                command: 'view'
              }
            ]
            hasPermission(allocatePermission) && options.push({
              label: '自动分配',
              command: 'auto'
            })
            // hasPermission(manualAllocatePermission) && options.push({
            //   label: '手动分配',
            //   command: 'manual'
            // })
            hasPermission(finishPermission) && options.push({
              label: '审核',
              command: 'audit'
            })
            // hasPermission(finishPermission) && options.push({
            //   label: '标记完成',
            //   command: 'finish'
            // })
            hasPermission(cancelPermission) && options.push({
              label: '作废',
              command: 'cancel'
            })
            // hasPermission(generatePermission) && options.push({
            //   label: '生成下架单',
            //   command: 'generate'
            // })
            // hasPermission(generatePermission) && options.push({
            //   label: '查看下架单',
            //   command: 'viewBound'
            // })
            return options
          }
        }
      ]
    }
  },

  computed: {
    hasSelected() {
      return this.selectionKeys.length === 1
    }
  },

  methods: {
    handleReset() {
      const {formType: form_type} = this.$route.params
      this.params = {
        form_type,
        form_status: 'CreatedAndExecuting'
      }
    },
    handleMaterielEdit(row) {
      this.$refs.materielForm.edit(row)
    },

    handleRowClick(row) {
      this.model = row
      // this.$refs.materielForm.edit(row)
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

    @confirm('是否确定删除记录?')
    async handleDelete({id}) {
      await deleteList([id])
      this.handleSearch()
    },

    // @confirm('是否确定审核单据?')
    // async handleBatchAudit() {
    //   await finishOutboundList(this.selectionKeys)
    //   this.handleSearch()
    // },

    @confirm('是否确定审核单据?')
    async handleAudit(row) {
      await submitOutbound(row.id)
      this.handleSearch()
    },

    @confirm('是否确定标记完成?')
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
    async handleAllocate(row) {
      await allocateOutbound(row.id)
      this.handleSearch()
    },

    handleManual(row) {

    },

    // @confirm('作废创建状态的出库单,是否确定?')
    // async handleBatchCancel() {
    //   await cancelOutboundList(this.selectionKeys)
    //   this.handleSearch()
    //   this.clearSelectionKeys()
    // },

    @confirm('作废创建状态的出库单,是否确定?')
    async handleOrderCancel(row) {
      await cancelOutbound(row.id)
      this.handleSearch()
    },

    handleBatchPut() {
      this.$refs.OutModalMaterial.edit({
        selectionKeys: this.selectionKeys
      })
    },

    @confirm('是否生成下架单?')
    async handleGenerate(row) {
      await generateOutbound(row.id)
      this.handleSearch()
    }
  }
}
</script>
<style>
</style>
