<template>
  <drawer-modal
    v-if="visible"
    :title="title"
    :visible.sync="visible">
    <resize-layout height="100%">
      <resize-col type="fixed">
        <el-form
          ref="editForm"
          :model="model"
          label-width="80px"
          :rules="rules"
          size="medium">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-form-item
                :label="i18n('制单人')"
                prop="operator">
                <el-input v-model="model.operator"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item
                :label="i18n('单据备注')"
                prop="remark">
                <el-input v-model="model.remark"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item
                :label=" i18n('目标出口')"
                prop="outLocation">
                <biz-entry-select v-model="model.exit_area" @change="handleChange"></biz-entry-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item
                :label="i18n('制单日期')"
                prop="outbound_time"
                class="tight-form-item">
                <el-date-picker
                  v-model="model.outbound_time"
                  type="date"
                  :placeholder="i18n('请输入')"
                  value-format="yyyy-MM-dd">
                </el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>

        </el-form>
      </resize-col>
      <resize-col :header=" i18n('选择物料(请先选择目标出口)')" class="mt5">
        <search-form
          :fields="searchFields"
          :fields-value="params"
          @enter="handleSearch"
          @reset="handleReset">
        </search-form>

        <dynamic-table
          :columns="columns"
          v-loading="loading"
          :data="list"
          keyname="container_code"
          :selectionIds.sync="selectionKeys"
          tableName="editFormTable"
          :pagination="pagination"
          @size-change="handleSizeChange"
          @page-change="handlePageChange">
        </dynamic-table>
      </resize-col>
    </resize-layout>

    <div slot="footer">
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="loading">{{ i18n('确定') }}
      </el-button>
      <el-button @click="handleCancel">{{ i18n('取消') }}</el-button>
    </div>
  </drawer-modal>
</template>

<script>
import {formMixin, pagedSearchMixin} from '@/mixins'
import {addOutbound} from '@/api/outbound'
import {queryInventoryStorageMaterials} from '@/api/warehouse'
import LocationTree from '@comp/LocationTree/index'
// import { getWorkSites } from '@/api/palletize'
import BizEntrySelect from '@comp/Business/entry'
export default {
  name: 'OutboundFormDetailModal',

  components: {
    LocationTree,
    BizEntrySelect
  },

  props: {
    fields: {
      type: Array
    }
  },

  mixins: [formMixin, pagedSearchMixin],

  data() {
    const {formType: form_type} = this.$route.params

    return {
      form_type,
      title: '新增出库单',
      params: {
        loc_type: 'cubic',
        inventory_status: 'A',
        exist: ''
      },
      pagedUrl: queryInventoryStorageMaterials,
      searchFields: [
        {
          label: 'sku编码',
          prop: 'sku_code'
        },
        {
          label: '托盘号',
          prop: 'container_code'
        }
      ],
      rules: {
        remark: [
          {required: true, message: this.i18n('请输入单据备注'), trigger: 'blur'}
        ],
        operator: [
          {required: true, message: this.i18n('请输入制单人'), trigger: 'blur'}
        ],
        exit_area: [
          {required: true, message: this.i18n('请选择出口'), trigger: 'change'}
        ]
      },
      columns: [
        {
          type: 'selection',
          key: 'selection',
          attrs: {
            fixed: 'left'
          }
        },
        {
          label: '库位',
          key: 'location_code',
          prop: 'location_code'
        },
        {
          label: '托盘号',
          key: 'container_code',
          prop: 'container_code'
        },
        {
          label: 'SKU编码',
          key: 'sku_code',
          prop: 'sku_code'
        },
        {
          label: 'SKU名称',
          key: 'sku_name',
          prop: 'sku_name'
        },
        {
          label: '数量',
          key: 'primary_qty',
          prop: 'primary_qty'
        },
        {
          label: '单位',
          key: 'primary_unit',
          prop: 'primary_unit'
        },
        {
          label: '源库位',
          key: 'inventory_area',
          prop: 'inventory_area'
        }
      ],
      isCreatedLoadData: false,
      workSites: [],
      storages: []
    }
  },

  created() {
    // this.getOutLocations()
  },

  methods: {
    handleReset() {
      this.params = {
        loc_type: 'cubic',
        inventory_status: 'A',
        exist: ''
      }
    },
    afterEdit() {
      this.handleSearch()
    },

    beforeCancel() {
      this.selectionKeys = []
      this.model = {}
    },

    handleSearch() {
      if (this.model.exit_area) {
        this.pagination.page = 1
        this.params.exist = this.model.exit_area
        this.loadData()
      }
    },

    handleChange() {
      this.selectionKeys = []
      this.list = []
      this.handleSearch()
    },

    async getOutLocations() {
      // const workSites = await getWorkSites()
      // this.workSites = workSites
    },

    async submit() {
      const selectedStocks = this.list.filter(item => {
        return this.selectionKeys.indexOf(item.container_code) > -1
      })

      this.model.outbound_form_type = this.form_type
      await addOutbound({
        outboundDto: this.model,
        outboundItems: selectedStocks
      })
    }
  }
}
</script>

<style>

</style>
