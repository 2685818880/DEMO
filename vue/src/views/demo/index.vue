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
        :loading="loading"
        :columns="columns"
        :data="list"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @page-change="handlePageChange"
        @context-menu-edit="handleEdit"
        @context-menu-del="handleDelete"
      >
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
import {pagedSearchMixin} from '@/mixins'
import {confirm} from '@/decorator'
import {del, pagedUrl} from '@/api/demo'
import FormModal from './form-modal'

export default {
  name: 'demo',
  components: { FormModal },
  mixins: [pagedSearchMixin],

  data() {
    return {
      pagedUrl: pagedUrl,
      searchFields: [
        {
          label: '编码',
          prop: 'code'
        }
      ],
      columns: [
        {
          label: '编码',
          prop: 'code',
          key: 'code'
        },
        {
          label: '姓名',
          prop: 'name',
          key: 'name'
        },
        {
          label: '描述',
          prop: 'remark',
          key: 'remark'
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
    async handleDelete(row) {
      await del(row.id)
      this.handleSearch()
    },

    handleEdit(row) {
      this.$refs.modalForm.edit(row)
    }
  }
}
</script>
