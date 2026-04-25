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
            <el-col :span="6">
              <el-form-item prop="form_type" :label="i18n('单据类型')">
                <enum-select disabled v-model="formType" code="OutFormType" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item prop="cost_center_code" :label="i18n('成本中心')">
                <enum-select
                  v-model="model.cost_center_code"
                  filterable
                  code="CostCenter"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <dynamic-table
          :columns="columns"
          v-loading="loading"
          :data="list"
          keyname="serial_no"
          :selectionIds.sync="selectionKeys"
          tableName="editFormTable">
          <template v-slot:primary_qty="{ row }">
            <el-input-number v-model="row.primary_qty" :min="0" />
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
import {addScrap, queryAllHouse, outboundAmount} from '@/api/outbound'
import {queryAvailableStorage} from '@/api/warehouse'
import BizSelect from '@/components/BizSelect'
import {getEnumLabel} from '@utils/helper'
import SkuModal from './form-sku-modal/pick-sku.vue'
import FormDetailModal from '../material/form-detail-modal.vue'

export default {
  name: 'OutboundFormDetailModal',

  components: { SkuModal, BizSelect },

  extends: FormDetailModal,

  data() {
    const {formType: form_type} = this.$route.params

    return {
      rules: {
        cost_center_code: [
          { required: true, message: '请选择成本中心', trigger: 'blur' }
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
          label: '库区',
          key: 'house_code',
          prop: 'house_code'
        },
        {
          label: '库位号',
          key: 'location_code',
          prop: 'location_code',
          width: 120
        },
        {
          label: '库位类型',
          key: 'loc_type',
          prop: 'loc_type',
          format(val) {
            return getEnumLabel('StorageType', val)
          }
        },
        {
          label: '托盘编号',
          key: 'container_code',
          prop: 'container_code',
          width: 120
        },
        {
          label: 'SN',
          key: 'serial_no',
          prop: 'serial_no',
          width: 160
        },
        {
          label: '存货编码',
          key: 'category_code',
          prop: 'category_code',
          width: 120
        },
        {
          label: '存货名称',
          key: 'category_name',
          prop: 'category_name',
          width: 120
        },
        {
          label: '物料编码',
          key: 'sku_code',
          prop: 'sku_code',
          width: 120
        },
        {
          label: '物料名称',
          key: 'sku_name',
          prop: 'sku_name',
          width: 120
        },
        {
          label: '库存数量',
          key: 'primary_qty',
          prop: 'primary_qty'
        },
        {
          label: '库存单位',
          key: 'primary_unit',
          prop: 'primary_unit'
        },
        {
          label: '辅助数量',
          key: 'auxiliary_qty',
          prop: 'auxiliary_qty'
        },
        {
          label: '辅助计量单位',
          key: 'auxiliary_unit',
          prop: 'auxiliary_unit'
        },
        {
          label: '库存可用数量',
          key: 'available_qty',
          prop: 'available_qty'
        },
        {
          label: '质量状态',
          key: 'material_status',
          prop: 'material_status',
          format(val) {
            return getEnumLabel('MaterialStatus', val)
          }
        },
        {
          label: '批次状态',
          key: 'batch_status',
          prop: 'batch_status',
          format(val) {
            return getEnumLabel('BatchStatus', val)
          }
        },
        {
          label: '库存状态',
          key: 'inventory_status',
          prop: 'inventory_status',
          format(val) {
            return getEnumLabel('InventoryStatus', val)
          }
        }
      ]
    }
  },

  methods: {
    afterEdit() {
      const { model } = this
      this.selectionKeys = []
      this.list = []
    },

    beforeCancel() {
      this.selectionKeys = []
      this.model = {}
    },

    handleReloadSku(skuArr) {
      const res = skuArr
      this.selectionKeys = []
      const list = this.list
      list.push(...res)
    },

    handleDelete() {
      const list = this.list
      list.map((item, index) => {
        if (this.selectionKeys.indexOf(item.serial_no) !== -1) {
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
        form_type: this.formType
      }
      await addScrap({
        ...model,
        assignStorageMaterialList: this.list
      })
    }
  }
}
</script>

<style>

</style>
