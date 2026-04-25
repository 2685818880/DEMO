<template>
  <resize-layout>
    <resize-col>
      <search-form
        :fields="searchFields"
        :fields-value="params"
        @enter="handleSearch"
        @reset="handleReset" />
      <dynamic-table
        v-loading="loading"
        tableName="container"
        :columns="columns"
        :data="list"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @page-change="handlePageChange"
        @context-menu-edit="handleEdit"
        @context-menu-del="handleDelete"
        @context-menu-sku="handleEditSku">
        <div slot="tools">
          <el-row>
            <el-button type="primary" @click="handleAdd">{{ i18n('新增') }}
            </el-button>
          </el-row>
        </div>
      </dynamic-table>
      <form-modal ref="modalForm" @ok="handleReload" />
    </resize-col>
  </resize-layout>
</template>

<script>
import { workstationPagedUrl, delWorkstation } from '@/api/workstation'
import { confirm, loading } from '@/decorator'
import { pagedSearchMixin } from '@/mixins'
import { getEnum, getEnumLabel } from '@utils/helper'
import FormModal from './form-modal'

export default {
  name: 'Workstation',

  mixins: [pagedSearchMixin],

  components: { FormModal },

  data() {
    return {
      pagedUrl: workstationPagedUrl,
      searchFields: [
        {
          label: '是否开启',
          prop: 'isOpen',
          type: 'enum',
          props: {
            code: 'YesOrNoB'
          }
        },
        {
          label: 'IP地址',
          prop: 'workstationIp'
        },
        {
          label: '工作站模式',
          prop: 'workstationMode',
          type: 'enum',
          props: {
            code: 'workstationModeType'
          }
        }
      ],
      columns: [
        {
          label: '工作站名称',
          prop: 'workstationName',
          key: 'workstationName'
        },
        {
          label: '工作站编号',
          prop: 'workstationCode',
          key: 'workstationCode'
        },
        {
          label: '工作站描述',
          prop: 'workstationDescribe',
          key: 'workstationDescribe'
        },
        {
          label: '是否开启',
          prop: 'isOpen',
          key: 'isOpen'
        },
        {
          label: 'IP地址',
          prop: 'workstationIp',
          key: 'workstationIp'
        },
        {
          label: '工作站状态',
          prop: 'workstationStatus',
          key: 'workstationStatus'
        },
        {
          label: '工作站模式',
          prop: 'workstationMode',
          key: 'workstationMode',
          format(val) {
            return getEnumLabel('workstationModeType', val)
          }
        },
        {
          label: '操作人',
          prop: 'operator',
          key: 'operator'
        },
        {
          label: '操作',
          key: 'action',
          options: [
            {
              label: '编辑',
              command: 'edit'
            },
            {
              label: '删除',
              command: 'del'
            }
          ]
        }
      ]
    }
  },

  methods: {
    @confirm('确定要删除吗？')
    @loading()
    async handleDelete(row) {
      await delWorkstation({ id: row.id })
      this.handleSearch()
    },

    handleEditSku(row) {
      this.$refs.editForm.edit(row)
    }
  }
}
</script>
