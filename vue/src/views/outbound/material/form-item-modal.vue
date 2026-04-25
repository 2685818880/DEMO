<template>
  <el-dialog
    :visible.sync="visible"
    :title="title"
    width="60%" >
    <el-form size="medium" label-width="120px" :ref="formName" :model="model" :rules="rules">
      <el-row>
        <el-col :span="12">
          <el-form-item prop="category_code" label="存货分类编码">
            <el-input v-model="model.category_code"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="category_name" label="存货分类名称">
            <el-input v-model="model.category_name"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item prop="sku_code" label="物料编号">
            <el-input v-model="model.sku_code"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="sku_name" label="物料名称">
            <el-input v-model="model.sku_name"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item prop="factory_code" label="工厂编号">
            <el-input v-model="model.factory_code"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="house_code" label="仓库">
              <biz-select
                v-model="model.house_code"
                bizType="warehouse"
                labelName="house_name"
                valueName="house_no"
              />
            </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item prop="batch_no" label="批次">
            <el-input v-model="model.batch_no"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="business_item_no" label="业务行号">
            <el-input v-model="model.business_item_no"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item prop="quality_status" label="质量状态">
            <el-input v-model="model.quality_status"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="primary_qty" label="需求数量">
            <el-input-number v-model="model.primary_qty" :min="0"></el-input-number>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item prop="confirm_qty" label="已安排数量">
            <el-input-number v-model="model.confirm_qty" :min="0"></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="package_type" label="包装类型">
            <el-input v-model="model.package_type"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item prop="primary_unit" label="库存单位">
            <el-select v-model="model.primary_unit">
              <el-option
                v-for="item in unitList"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="source_area" label="库存地点">
            <el-input v-model="model.source_area"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div slot="footer">
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="loading">{{ i18n('确定') }}
      </el-button>
      <el-button @click="handleCancel">{{ i18n('取消') }}</el-button>
    </div>
  </el-dialog>
</template>
<script>
import BizSelect from '@/components/BizSelect'
import BizPagingSelect from '@/components/BizPagingSelect'
import {formMixin} from '@/mixins'
import {addOrderItem} from '@/api/requisitionOrder'
import {queryUnit} from '@/api/take-stock'

export default {
  props: {
    orderInfo: {
      type: Object
    }
  },
  name: 'FormItemModal',
  comments: [BizSelect, BizPagingSelect],
  mixins: [formMixin],
  data() {
    return {
      title: this.i18n('新增出库明细单'),
      formType: '',
      formName: 'itemForm',
      unitList: [],
      model: {
        category_code: '',
        category_name: '',
        sku_code: '',
        sku_name: '',
        factory_code: '',
        house_code: '',
        batch_no: '',
        business_item_no: '',
        quality_status: '',
        primary_qty: '',
        confirm_qty: '',
        package_type: '',
        primary_unit: '',
        source_area: ''
      },
      rules: {
        category_code: [
          {required: true, message: '请输入存货分类编码', trigger: 'blur'}
        ],
        category_name: [
          {required: true, message: '请输入存货分类名称', trigger: 'blur'}
        ],
        sku_code: [
          {required: true, message: '请输入物料编号', trigger: 'blur'}
        ],
        sku_name: [
          {required: true, message: '请输入物料名称', trigger: 'blur'}
        ],
        factory_code: [
          // {required: true, message: '请输入工厂编号', trigger: 'blur'}
        ],
        house_code: [
          {required: true, message: '请选择仓库', trigger: 'change'}
        ],
        batch_no: [
          // {required: true, message: '请输入批次', trigger: 'blur'}
        ],
        business_item_no: [
          // {required: true, message: '请输入业务行号', trigger: 'blur'}
        ],
        quality_status: [
          {required: true, message: '请输入质量状态', trigger: 'blur'}
        ],
        primary_qty: [
          {required: true, message: '请输入需求数量', trigger: 'blur'}
        ],
        confirm_qty: [
          {required: true, message: '请输入已安排数量', trigger: 'blur'}
        ],
        package_type: [
          {required: true, message: '请输入包装类型', trigger: 'blur'}
        ],
        primary_unit: [
          {required: true, message: '请选择库存单位', trigger: 'change'}
        ],
        source_area: [
          // {required: true, message: '请输入库存地点', trigger: 'blur'}
        ]
      }
    }
  },
  created() {
    this.getUnits()
  },
  methods: {
    /*
    * 库存单位
    */
    async getUnits() {
      this.unitList = await queryUnit()
    },
    validateForm() {
      return this.$refs[this.formName].validate((valid) => {
        console.log(valid)
        return valid
      })
    },
    /*
     * 提交方法
     */
    async submit() {
      await addOrderItem({
        orderDto: this.orderInfo,
        itemDto: this.model
      })
    }
  }
}
</script>
<style>

</style>
