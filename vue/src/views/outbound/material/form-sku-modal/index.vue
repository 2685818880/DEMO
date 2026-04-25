<template>
  <drawer-modal
    :title="i18n('新增明细')"
    :visible.sync="visible"
    :loading="loading"
    @ok="handleSubmit"
    @close="handleCancel"
  >
    <el-form
      ref="editForm"
      :model="model"
      :rules="rules"
    >
      <el-form-item prop="row_index" :label="i18n('行号')">
        <el-input
          v-model="model.row_index"
          readonly
        />
      </el-form-item>
      <el-form-item prop="sku_name" :label="i18n('货品')">
        <el-input v-model="model.sku_name" readonly />
      </el-form-item>
      <el-form-item prop="primary_unit_name" :label="i18n('库存主单位')">
        <el-input v-model="model.primary_unit_name" readonly />
      </el-form-item>
      <el-form-item prop="num" :label="i18n('数量')">
        <el-input-number v-model="model.num" :min="0" />
      </el-form-item>
    </el-form>
  </drawer-modal>
</template>

<script>
import { formMixin } from '@/mixins'

export default {
  name: 'SkuModal',

  mixins: [formMixin],

  data() {
    return {

    }
  }
}
</script>
