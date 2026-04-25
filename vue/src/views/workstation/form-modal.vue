<template>
  <drawer-modal
    :visible.sync="visible"
    :title="title"
    :loading="loading"
    @ok="handleSubmit"
    @close="handleCancel"
  >
    <el-form
      ref="editForm"
      :rules="rules"
      :model="model"
      label-width="110px">
      <el-form-item prop="workstationName" :label="i18n('工作站名称')">
        <el-input v-model="model.workstationName" />
      </el-form-item>
      <el-form-item v-if="!model.id" prop="workstationCode" :label="i18n('工作站编号')">
        <el-input v-model="model.workstationCode" />
      </el-form-item>
      <el-form-item prop="workstationIp" :label="i18n('IP地址')">
        <el-input v-model="model.workstationIp" />
      </el-form-item>
      <el-form-item v-if="model.id" prop="workstationMode" :label="i18n('类型')">
         <enum-select
          v-model="model.workstationMode"
          code="workstationModeType"
        />
      </el-form-item>
      <el-form-item v-if="model.id" prop="isOpen" :label="i18n('是否开启')">
        <el-radio-group v-model="model.isOpen">
          <el-radio :label="true">是</el-radio>
          <el-radio :label="false">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="workstationDescribe" :label="i18n('工作站描述')">
        <el-input type="textarea" v-model="model.workstationDescribe" />
      </el-form-item>
    </el-form>
  </drawer-modal>
</template>

<script>
import { formMixin } from '@/mixins'
import BizSelect from '@/components/BizSelect'
import { updateWorkstation, addWorkstation } from '@/api/workstation'

export default {
  name: 'FormModal',

  mixins: [formMixin],

  components: { BizSelect },

  data() {
    return {
      title: '',
      rules: {
        workstationName: [
          { required: true, message: '请输入工作站名称', trigger: 'blur' }
        ],
        workstationCode: [
          { required: true, message: '请输入工作站编号', trigger: 'blur' }
        ],
        workstationIp: [
          { required: true, message: '请输入IP地址', trigger: 'blur' }
        ],
        workstationMode: [
          { required: true, message: '请选择类型', trigger: 'change' }
        ],
        isOpen: [
          { required: true, message: '请选择是否开启', trigger: 'change' }
        ]
      }
    }
  },

  methods: {
    async submit() {
      const { model } = this
      if (!model.id) {
        await addWorkstation(model)
      } else {
        await updateWorkstation(model)
      }
    },

    afterEdit() {
      this.title = this.model.id ? this.i18n('编辑') : this.i18n('新增')
      !this.model.isOpen && this.$set(this.model, 'isOpen', false)
    }
  }
}
</script>
