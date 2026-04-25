<template>
  <drawer-modal
    :visible.sync="visible"
    :title="i18n('加急')"
    :loading="loading"
    @ok="handleSubmit"
    @close="handleCancel"
  >
    <el-form
      ref="editForm"
      :model="model"
      :rules="rules"
      label-width="140px"
    >
      <el-form-item prop="targetLocation" :label="i18n('目标位置')">
        <el-input v-model="model.targetLocation" />
      </el-form-item>
      <el-form-item prop="priority" :label="i18n('优先级')">
        <el-input type="number" v-model="model.priority" />
      </el-form-item>
    </el-form>
  </drawer-modal>
</template>

<script>
import { formMixin } from '@/mixins'
import { markUrgent } from '@/api/requisition'

export default {
  name: 'UrgentForm',

  mixins: [formMixin],

  data() {
    return {
      rules: {
        priority: [
          { required: true, message: '请输入优先级', trigger: 'blur' }
        ]
      }
    }
  },

  methods: {
    async submit() {
      await markUrgent(this.model)
    }
  }
}
</script>
