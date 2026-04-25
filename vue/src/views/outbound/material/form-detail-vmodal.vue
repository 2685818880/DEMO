<template>
  <el-dialog
    :visible.sync="visible"
    :title="title"
    width="60%" >
    <el-form size="medium" label-width="120px" :ref="formName" :model="model" :rules="rules">
      <el-row>
        <el-col :span="12">
          <el-form-item prop="business_form_no" label="业务单据号">
            <el-input v-model="model.business_form_no"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="business_form_type" label="业务单据类型">
            <el-input v-model="model.business_form_type"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item prop="owner_code" label="货主编码">
            <el-input v-model="model.owner_code"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="owner_name" label="货主名称">
            <el-input v-model="model.owner_name"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item prop="form_type" label="单据类型">
           <enum-select v-model="model.form_type" code="OutFormType" />
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
import {addOrder} from '@/api/requisitionOrder'

export default {
  name: 'OutboundFormDetailVModal',
  comments: [BizSelect, BizPagingSelect],
  mixins: [formMixin],
  data() {
    return {
      title: this.i18n('新增出库单'),
      formType: '',
      formName: 'detailForm',
      model: {
        form_type: '',
        business_form_no: '',
        business_form_type: '',
        owner_code: '',
        owner_name: ''
      },
      rules: {
        form_type: [
          {required: true, message: '请选择单据类型', trigger: 'change'}
        ],
        business_form_no: [
          {required: true, message: '请输入业务单据号', trigger: 'blur'}
        ],
        business_form_type: [
          {required: true, message: '请输入业务单据类型', trigger: 'blur'}
        ],
        owner_code: [
          // {required: true, message: '请输入货主编码', trigger: 'blur'}
        ],
        owner_name: [
          // {required: true, message: '请输入货主名称', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    async submit() {
      console.log(this.model)
      await addOrder(this.model)
    }
  }
}
</script>
<style>

</style>
