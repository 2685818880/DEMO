<template>
  <drawer-modal
    :title="title"
    :visible.sync="visible"
    :loading="loading"
    width="80%"
    @ok="handleSubmit"
    @close="handleCancel"
  >
    <resize-layout :height="modalTableHeight">
      <resize-col>
        <el-form
          ref="editForm"
          :model="model"
          label-width="100px"
          :rules="rules"
          size="medium">
          <el-row>
            <!-- <el-col :span="6">
              <el-form-item
                :label="i18n('出库单号')"
                prop="form_no">
                <el-input v-model="model.form_no" />
              </el-form-item>
            </el-col> -->
            <el-col :span="6">
              <el-form-item prop="form_type" :label="i18n('单据类型')">
                <enum-select v-model="formType" code="OutFormType" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item
                prop="house_code"
                :label="i18n('库区')+':'">
                <biz-select
                  filterable
                  v-model="model.house_code"
                  bizType="warehouse"
                  labelName="house_name"
                  valueName="house_no"
                  :placeholder="i18n('请选择库区')"
                  @init="handleWarehouseInit"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item prop="bizFormNo" :label="i18n('业务单号')">
                <el-input v-model="model.bizFormNo" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item prop="bizFormType" :label="i18n('业务类型')">
                <el-input v-model="model.bizFormType" />
              </el-form-item>
            </el-col>
            <el-col v-has="'/wms-platform/view/needCustomer'" :span="6">
              <el-form-item
                :label="i18n('货主')"
                prop="ownerCode"
              >
                <biz-paging-select
                  v-model="params.ownerCode"
                  bizType="customer"
                  labelName="customer_name"
                  valueName="customer_code"
                  :placeholder="i18n('请选择货主')"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <dynamic-table
          :columns="columns"
          v-loading="loading"
          :data="list"
          keyname="sku_code"
          :selectionIds.sync="selectionKeys"
          tableName="editFormTable">
          <template v-slot:primaryQty="{ row }">
            <el-input-number v-model="row.primaryQty" :min="0" />
          </template>
          <template v-slot:bizItemNo="{ row }">
            <el-input v-model="row.bizItemNo" />
          </template>
          <template v-slot:factoryCode="{ row }">
            <el-input v-model="row.factoryCode" />
          </template>
          <template v-slot:sourceArea="{ row }">
            <el-input v-model="row.sourceArea" />
          </template>
          <template v-slot:batchNo="{ row }">
            <el-input v-model="row.batchNo" />
          </template>
          <div slot="tools">
            <el-row>
              <el-button
                type="primary"
                icon="el-icon-edit"
                @click="handleAdd">{{ i18n('新增明细') }}
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                :loading="loading"
                :disabled="!selectionKeys.length"
                @click="handleDelete">{{ i18n('删除明细') }}
              </el-button>
            </el-row>
          </div>
        </dynamic-table>
        <sku-modal ref="modalForm" :selectedRows="list" @ok="handleReloadSku" />
      </resize-col>
    </resize-layout>
  </drawer-modal>
</template>

<script>
import {formMixin, pagedSearchMixin} from '@/mixins'
import {addOutbound, addScrap, queryAllHouse, outboundAmount} from '@/api/outbound'
import {queryAvailableStorage} from '@/api/warehouse'
import BizSelect from '@/components/BizSelect'
import BizPagingSelect from '@/components/BizPagingSelect'
import {getEnumLabel} from '@utils/helper'
import SkuModal from './form-sku-modal/pick-sku.vue'

export default {
  name: 'OutboundFormDetailModal',

  components: { SkuModal, BizSelect, BizPagingSelect },

  props: {
    fields: {
      type: Array
    },
    formType: {
      type: String,
      default: ''
    }
  },

  mixins: [formMixin, pagedSearchMixin],

  data() {
    return {
      title: this.i18n('新增出库单'),
      pagedUrl: '',
      limitAmount: 0,
      amount: 0,
      availableAmount: 0,
      searchFields: [],
      list: [],
      rules: {
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
          label: '存货分类',
          key: 'categoryName',
          prop: 'categoryName'
        },
        {
          label: 'SKU编号',
          key: 'skuCode',
          prop: 'skuCode',
          width: 150
        },
        {
          label: 'SKU名称',
          key: 'skuName',
          prop: 'skuName',
          width: 120,
          attrs: {
            'show-overflow-tooltip': true
          }
        },
        {
          label: '基本单位',
          key: 'baseUnit',
          prop: 'baseUnit'
        },
        {
          label: '库存单位',
          key: 'primaryUnit',
          prop: 'primaryUnit'
        },
        {
          label: '需求数量',
          key: 'primaryQty',
          prop: 'primaryQty',
          scopedSlot: 'primaryQty',
          width: 180
        },
        {
          label: '业务行号',
          key: 'bizItemNo',
          prop: 'bizItemNo',
          scopedSlot: 'bizItemNo',
          width: 180
        },
        {
          label: '工厂',
          key: 'factoryCode',
          prop: 'factoryCode',
          scopedSlot: 'factoryCode',
          width: 180
        },
        {
          label: '库存地点',
          key: 'sourceArea',
          prop: 'sourceArea',
          scopedSlot: 'sourceArea',
          width: 180
        },
        {
          label: '批次',
          key: 'batchNo',
          prop: 'batchNo',
          scopedSlot: 'batchNo',
          width: 180
        }
      ],
      isCreatedLoadData: false,
      storages: [],
      locations: [],
      defaultWarehouse: ''
    }
  },

  methods: {
    handleAdd(row) {
      row = {
        ...row,
        ...this.model
      }
      this.$refs.modalForm.add(row)
    },
    afterEdit() {
      const { model } = this
      this.$set(model, 'form_type', 'simpleOut')
      this.setWarehouse()
      this.selectionKeys = []
      this.list = []
    },

    beforeCancel() {
      this.selectionKeys = []
      this.model = {}
    },

    handleReloadSku(skuArr) {
      const res = skuArr.map((
        { sku_code: skuCode,
          sku_name: skuName,
          primary_unit: baseUnit,
          primary_unit: primaryUnit,
          category_name: categoryName,
          category_code: categoryCode,
          num: primaryQty,
          business_item_no: bizItemNo,
          factory_code: factoryCode,
          source_area: sourceArea,
          batch_no: batchNo}) => ({
        skuCode,
        skuName,
        categoryName,
        categoryCode,
        baseUnit,
        primaryUnit,
        primaryQty,
        qualityStatus: 'Q',
        bizItemNo,
        factoryCode,
        sourceArea,
        batchNo
      }))
      this.selectionKeys = []
      const list = this.list
      list.push(...res)
    },

    handleDelete() {
      const list = this.list
      list.map((item, index) => {
        if (this.selectionKeys.indexOf(item.sku_code) !== -1) {
          list.splice(index, 1)
        }
      })
    },

    async submit() {
      if (!this.list.length) {
        this.$message('请先选择物料')
        throw new Error('请先选择物料')
      }
      const model = {
        ...this.model,
        formType: this.formType,
        houseCode: this.model.house_code
      }
      this.list.forEach(s => {
        s.houseCode = this.model.house_code
        return s
      })
      await addOutbound({
        ...model,
        itemParams: this.list
      })
    }
  }
}
</script>

<style>

</style>
