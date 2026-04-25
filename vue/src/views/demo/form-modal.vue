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
      <el-form-item prop="code" :label="i18n('编码')">
        <el-input v-model="model.code"/>
      </el-form-item>
      <el-form-item prop="name" :label="i18n('名称')">
        <el-input v-model="model.name"/>
      </el-form-item>
      <el-form-item prop="remark" :label="i18n('描述')">
        <el-input v-model="model.remark"/>
      </el-form-item>
    </el-form>
  </drawer-modal>
</template>

<script>
import {formMixin} from '@/mixins'
import {save} from '@/api/demo'

export default {
  name: 'FormModal',
  mixins: [formMixin],
  components: {},
  data() {
    return {
      title: '',
      rules: {
        code: [
          {required: true, message: '请输入编码', trigger: 'blur'}
        ],
        name: [
          {required: true, message: '请输入名称', trigger: 'blur'}
        ]
      }
    }
  },

  methods: {
    async submit() {
      const {model} = this
      await save(model)
    },

    afterEdit() {
      this.title = this.model.id ? this.i18n('编辑') : this.i18n('新增')
    }
  }
}
</script>
