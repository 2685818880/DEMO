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
        ref="offshelf"
        v-loading="loading"
        :tableName="tableName"
        :selectionIds.sync="selectionKeys"
        :columns="columns"
        :data="list"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @page-change="handlePageChange"
        @context-menu-detail="handleRowClick"
        @context-menu-put="handlePut"
      >
        <div slot="tools">
          <el-row>
            <el-button
              v-has="cancelPermission"
              type="danger"
              icon="el-icon-delete"
              :loading="loading"
              @click="handleBatchCancel"
              :disabled="!hasSelected">
              {{ i18n('作废') }}
            </el-button>
          </el-row>
        </div>
      </dynamic-table>
      <!-- <offshelf-detail ref="modalForm" /> -->
      <out-modal-material
        ref="OutModalMaterial"
        level="outbound"
        @ok="handleReload"/>
    </resize-col>
  </resize-layout>
</template>

<script>
import { pagedSearchMixin } from '@/mixins'
import { offshelfPagedUrl, cancelOutboundList, takeOutOutbound } from '@/api/outbound'
import { getEnumLabel } from '@/utils/helper'
// import OffshelfDetail from './offshelf-detail.vue'
import OutModalMaterial from '../material/out-modal-material.vue'
import { confirm, loading } from '@/decorator'
import { hasPermission } from '@/utils/auth'

const cancelPermission = '/wms-platform/view/outbound/offshelf/cancel'
const putPermission = '/wms-platform/view/outbound/offshelf/put'

export default {
  name: 'OffSelf',

  components: { OutModalMaterial },

  mixins: [pagedSearchMixin],

  data() {
    return {
      pagedUrl: offshelfPagedUrl,
      cancelPermission,
      tableName: 'offshelTable',
      formNo: '',
      params: {
        form_status: 'CreatedAndExecuting'
      },
      searchFields: [
        {
          label: '托盘号',
          prop: 'container_code'
        },
        {
          label: '单据状态',
          prop: 'form_status',
          type: 'enum',
          props: {
            code: 'OutboundFormStatus'
          }
        }
      ],
      columns: [
        {
          type: 'selection',
          key: 'selection',
          attrs: {
            fixed: 'left'
          }
        },
        {
          label: '单据号',
          prop: 'form_no',
          key: 'form_no',
          width: 160
        },
        {
          label: '出库单类型',
          prop: 'form_type',
          key: 'form_type',
          format(val) {
            return getEnumLabel('FormType', val)
          },
          width: 120
        },
        {
          label: '单据状态',
          prop: 'form_status',
          key: 'form_status',
          format(val) {
            return getEnumLabel('OutBoundStatus', val)
          },
          width: 120
        },
        {
          label: '仓库号',
          prop: 'house_code',
          key: 'house_code',
          width: 120
        },
        {
          label: '托盘号',
          prop: 'container_code',
          key: 'container_code',
          width: 120
        },
        {
          label: '原库位',
          prop: 'location_code',
          key: 'location_code',
          width: 120
        },
        {
          label: '出库任务号',
          prop: 'out_task_no',
          key: 'out_task_no',
          width: 120
        },
        {
          label: '出库口',
          prop: 'out_station',
          key: 'out_station',
          width: 120
        },
        {
          label: '出库状态',
          prop: 'out_status',
          key: 'out_status',
          format(val) {
            return getEnumLabel('TaskStatus', val)
          },
          width: 120
        },
        {
          label: '出库任务下发时间',
          prop: 'out_start_datetime',
          key: 'out_start_datetime',
          width: 140
        },
        {
          label: '出库任务完成时间',
          prop: 'out_finish_datetime',
          key: 'out_finish_datetime',
          width: 140
        },
        {
          label: '入库任务号',
          prop: 'in_task_no',
          key: 'in_task_no',
          width: 140
        },
        {
          label: '回库库位',
          prop: 'in_location_code',
          key: 'in_location_code',
          width: 120
        },
        {
          label: '回库状态',
          prop: 'in_status',
          key: 'in_status',
          format(val) {
            return getEnumLabel('TaskStatus', val)
          },
          width: 120
        },
        {
          label: '入库任务下发时间',
          prop: 'in_start_datetime',
          key: 'in_start_datetime',
          width: 140
        },
        {
          label: '入库任务完成时间',
          prop: 'in_finish_datetime',
          key: 'in_finish_datetime',
          width: 140
        },
        {
          label: '描述',
          prop: 'remark',
          key: 'remark'
        },
        {
          label: '操作',
          key: 'action',
          options(row) {
            const options = []
            // const options = [
            //   {
            //     label: '详情',
            //     command: 'detail'
            //   }
            // ]
            hasPermission(putPermission) && options.push({
              label: '取出托盘',
              command: 'put'
            })
            return options
          }
        }
      ]
    }
  },

  computed: {
    hasSelected() {
      return this.selectionKeys.length > 0
    }
  },

  watch: {
    $route: {
      handler: function(route) {
        this.params = {
          ...this.params,
          ...route.query
        }
        const { formNo, pickOrderId } = route.query
        this.$set(this.params, 'requisition_order_no', formNo)
        this.$set(this.params, 'pickOrderId', pickOrderId)
      },
      immediate: true
    }
  },

  methods: {
    handleReset() {
      this.params = {
        form_status: 'CreatedAndExecuting'
      }
    },
    handleRowClick(row) {
      this.handleEdit(row)
    },

    afterEdit() {
      this.handleSearch()
    },

    handleBatchPut() {
      const rows = this.$refs['offshelf'].$refs[this.tableName].selection

      this.$refs.OutModalMaterial.edit({
        selectionKeys: this.selectionKeys
      })
    },

    @confirm('确定要作废吗?')
    @loading()
    async handleBatchCancel() {
      await cancelOutboundList(this.selectionKeys)
      this.handleSearch()
      this.clearSelectionKeys()
    },

    @confirm('确定要取出托盘吗?')
    @loading()
    async handlePut(row) {
      this.$refs.OutModalMaterial.edit({
        selectionKeys: [row.id],
        house_code: row.house_code,
        outLocation: row.out_station
      })
      // if (row.out_station) {
      //   await takeOutOutbound({
      //     outboundIds: [row.id],
      //     toPos: row.out_station
      //   })
      //   this.handleSearch()
      // } else {
      //   this.$refs.OutModalMaterial.edit({
      //     selectionKeys: [row.id],
      //     house_code: row.house_code
      //   })
      // }
    }
  }
}
</script>
